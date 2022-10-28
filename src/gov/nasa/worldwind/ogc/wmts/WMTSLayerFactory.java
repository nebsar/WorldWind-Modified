package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Earth.WMTS3857Layer;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.wmts.WMTSImageLayer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WMTSLayerFactory {

    protected static List<String> compatibleImageFormats =
            Arrays.asList("image/png",
                    "image/jpg",
                    "image/jpeg",
                    "image/gif",
                    "image/bmp");
    protected static List<String> compatibleWGS84CoordinateSystems =
            Arrays.asList("urn:ogc:def:crs:OGC:1.3:CRS84",
                    "urn:ogc:def:crs:EPSG::4326",
                    "http://www.opengis.net/def/crs/OGC/1.3/CRS84");

    protected static List<String> compatibleOtherCoordinatSystems = Arrays.asList("urn:ogc:def:crs:EPSG::3857");

    public static AbstractLayer createWMTSImageLayer(Object source, String layerName) {

        WMTS100Capabilities wmtsCaps = null;

        if (source instanceof String) {
            try {
                wmtsCaps = WMTS100Capabilities.retrieve(URI.create((String) source));
                wmtsCaps = wmtsCaps.parse();
            } catch (Exception ex) {

            }
        } else if (source instanceof URI) {
            try {
                wmtsCaps = WMTS100Capabilities.retrieve((URI) source);
                wmtsCaps = wmtsCaps.parse();
            } catch (Exception ex) {

            }
        }

        WmtsLayer wmtsLayer = wmtsCaps.getLayerByName(layerName);

        try {
            // Determine if there is a TileMatrixSet which matches our Coordinate System compatibility and tiling scheme
            List<String> compatibleTileMatrixSets = determineWGS84CoordSysCompatibleTileMatrixSets(wmtsLayer);
            if (compatibleTileMatrixSets.isEmpty()) {
                throw new RuntimeException("LayerFactory: createWmtsLayer: Coordinate Systems Not Compatible with WGS84");
            }

            CompatibleTileMatrixSet compatibleTileMatrixSet = determineTileSchemeCompatibleTileMatrixSetForWGS84(wmtsLayer.getCapabilities(), compatibleTileMatrixSets);
            if (compatibleTileMatrixSet == null) {
                throw new RuntimeException("LayerFactory: createWmtsLayer: Coordinate Systems Not Compatible with WGS84");
            }

            List<WmtsResourceUrl> resourceUrls = wmtsLayer.getResourceUrls();
            String template = null;
            if (resourceUrls != null) {
                // Attempt to find a supported image format
                for (WmtsResourceUrl resourceUrl : resourceUrls) {
                    if (compatibleImageFormats.contains(resourceUrl.getFormat())) {
                        template = resourceUrl.getTemplate().replace("{TileMatrixSet}", compatibleTileMatrixSet.tileMatrixSetId);
                    }
                }

                AVList params = createWmtsLevelSet(wmtsLayer, compatibleTileMatrixSet, template);
                WMTSImageLayer wmtsImageLayer = new WMTSImageLayer(params);
                wmtsImageLayer.setName(layerName);
                return wmtsImageLayer;
            }

            //TODO: Complete KVP if there is not template!
            String baseUrl = determineKvpUrl(wmtsLayer);


        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        //TODO: this means the coordinate system is different that EPSG:4326 (WGS84)
        WMTS3857Layer wmts3857Layer = new WMTS3857Layer(wmtsCaps, layerName);
        return wmts3857Layer;
        //TODO: KVP should also be added here as well.
        //TODO: If wmts3857Layer cannot be created then return null

        //return null;

    }

    protected static String determineKvpUrl(WmtsLayer layer) {
        WMTS100Capabilities capabilities = layer.getCapabilities();
        OwsOperationsMetadata operationsMetadata = capabilities.getOperationsMetadata();
        if (operationsMetadata == null) {
            return null;
        }
        OwsOperation getTileOperation = operationsMetadata.getGetTile();
        if (getTileOperation == null) {
            return null;
        }
        List<OwsDcp> dcp = getTileOperation.getDCPs();
        if (dcp == null || dcp.isEmpty()) {
            return null;
        }

        List<OwsHttpMethod> getMethods = dcp.get(0).getGetMethods();
        if (getMethods == null || getMethods.isEmpty()) {
            return null;
        }

        List<OwsConstraint> constraints = getMethods.get(0).getConstraints();
        if (constraints == null || constraints.isEmpty()) {
            return null;
        }

        List<String> allowedValues = constraints.get(0).getAllowedValues();
        if (allowedValues != null && allowedValues.contains("KVP")) {
            return getMethods.get(0).getUrl();
        } else {
            return null;
        }
    }

    protected static List<String> determineWGS84CoordSysCompatibleTileMatrixSets(WmtsLayer layer) {
        List<String> compatibleTileMatrixSets = new ArrayList<>();

        // Look for compatible coordinate system types
        List<WmtsTileMatrixSet> tileMatrixSets = layer.getLayerSupportedTileMatrixSets();
        for (WmtsTileMatrixSet tileMatrixSet : tileMatrixSets) {
            if (compatibleWGS84CoordinateSystems.contains(tileMatrixSet.getSupportedCrs())) {
                compatibleTileMatrixSets.add(tileMatrixSet.getIdentifier());
            }
        }

        return compatibleTileMatrixSets;
    }

    protected static CompatibleTileMatrixSet determineTileSchemeCompatibleTileMatrixSetForWGS84(WMTS100Capabilities capabilities, List<String> tileMatrixSetIds) {
        CompatibleTileMatrixSet compatibleSet = new CompatibleTileMatrixSet();

        // Iterate through each provided tile matrix set
        for (String tileMatrixSetId : tileMatrixSetIds) {
            compatibleSet.tileMatrixSetId = tileMatrixSetId;
            compatibleSet.tileMatrices.clear();
            WmtsTileMatrixSet tileMatrixSet = capabilities.getTileMatrixSet(tileMatrixSetId);
            int previousHeight = 0;
            // Walk through the associated tile matrices and check for compatibility with WWA tiling scheme
            for (WmtsTileMatrix tileMatrix : tileMatrixSet.getTileMatrices()) {
                int height = tileMatrix.getMatrixHeight();
                int width = tileMatrix.getMatrixWidth();
                // Aspect and symmetry check of current matrix
                if ((2 * tileMatrix.getMatrixHeight()) != tileMatrix.getMatrixWidth()) {
                    continue;
                    // Quad division check
                } else if ((tileMatrix.getMatrixWidth() % 2 != 0) || (tileMatrix.getMatrixHeight() % 2 != 0)) {
                    continue;
                    // Square image check
                } else if (tileMatrix.getTileHeight() != tileMatrix.getTileWidth()) {
                    continue;
                    // Minimum row check
                } else if (tileMatrix.getMatrixHeight() < 2) {
                    continue;
                }

                // Parse top left corner values
                String[] topLeftCornerValue = tileMatrix.getTopLeftCorner().split("\\s+");
                if (topLeftCornerValue.length != 2) {
                    continue;
                }

                // Convert Values
                double[] topLeftCorner;
                try {
                    topLeftCorner = new double[]{
                            Double.parseDouble(topLeftCornerValue[0]),
                            Double.parseDouble(topLeftCornerValue[1])};
                } catch (Exception e) {
                    System.err.println("LayerFactory: determineTileSchemeCompatibleTileMatrixSet:" +
                            " Unable to parse TopLeftCorner values");
                    continue;
                }

                // Check top left corner values
                if (tileMatrixSet.getSupportedCrs().equals("urn:ogc:def:crs:OGC:1.3:CRS84")
                        || tileMatrixSet.getSupportedCrs().equals("http://www.opengis.net/def/crs/OGC/1.3/CRS84")) {
                    if (Math.abs(topLeftCorner[0] + 180) > 1e-9) {
                        continue;
                    } else if (Math.abs(topLeftCorner[1] - 90) > 1e-9) {
                        continue;
                    }
                } else if (tileMatrixSet.getSupportedCrs().equals("urn:ogc:def:crs:EPSG::4326")) {
                    if (Math.abs(topLeftCorner[1] + 180) > 1e-9) {
                        continue;
                    } else if (Math.abs(topLeftCorner[0] - 90) > 1e-9) {
                        continue;
                    }
                } else {
                    // The provided list of tile matrix set ids should adhere to either EPGS:4326 or CRS84
                    continue;
                }

                // Ensure quad division behavior from previous tile matrix and add compatible tile matrix
                if (previousHeight == 0) {
                    previousHeight = tileMatrix.getMatrixHeight();
                    compatibleSet.tileMatrices.add(tileMatrix.getIdentifier());
                } else if ((2 * previousHeight) == tileMatrix.getMatrixHeight()) {
                    previousHeight = tileMatrix.getMatrixHeight();
                    compatibleSet.tileMatrices.add(tileMatrix.getIdentifier());
                }

            }

            // Return the first compatible tile matrix set
            if (compatibleSet.tileMatrices.size() > 2) {
                return compatibleSet;
            }
        }

        return null;
    }

    protected static AVList createWmtsLevelSet(WmtsLayer wmtsLayer, CompatibleTileMatrixSet compatibleTileMatrixSet, String template) {

        Sector boundingBox = null;
        OwsWgs84BoundingBox wgs84BoundingBox = wmtsLayer.getWgs84BoundingBox();
        if (wgs84BoundingBox == null) {
            System.err.println("LayerFactory createWmtsLevelSet WGS84BoundingBox not defined for layer: " + wmtsLayer.getIdentifier());
        } else {
            boundingBox = wgs84BoundingBox.getSector();
        }

        WmtsTileMatrixSet tileMatrixSet = wmtsLayer.getCapabilities().getTileMatrixSet(compatibleTileMatrixSet.tileMatrixSetId);
        if (tileMatrixSet == null) {
            throw new RuntimeException(
                    "LayerFactory: createWmtsLevelSet: Compatible TileMatrixSet not found for: " + compatibleTileMatrixSet);
        }

        int imageSize = tileMatrixSet.getTileMatrices().get(0).getTileHeight();
        WMTS100Capabilities wmtsCaps = wmtsLayer.getCapabilities();

        String styleIdentifier = wmtsLayer.getStyles().get(0).getIdentifier();

        AVList params = new AVListImpl();
        params.setValue(AVKey.WMTS_VERSION, wmtsCaps.getVersion());
        params.setValue(AVKey.LAYER_NAME, wmtsLayer.getTitle());
        //TODO: sadece dummy olarak koydum dataset i
        params.setValue(AVKey.DATASET_NAME, "dataSet");
        params.setValue(AVKey.STYLE_NAMES, styleIdentifier);
        params.setValue(AVKey.FORMAT_SUFFIX, ".png");
        params.setValue(AVKey.TILE_WIDTH, imageSize);
        params.setValue(AVKey.TILE_HEIGHT, imageSize);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/WMTS/".concat(wmtsLayer.getTitle()));
        params.setValue(AVKey.NUM_LEVELS, compatibleTileMatrixSet.tileMatrices.size());
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(90.0d), Angle.fromDegrees(90.0d)));
        params.setValue(AVKey.SECTOR, boundingBox);
        params.setValue("URL_Template", template);
        params.setValue("tileMatrixIdentifiers", compatibleTileMatrixSet.tileMatrices);
        params.setValue(AVKey.TILE_URL_BUILDER, new WMTSImageLayer.URLBuilder(params));

        return params;
    }

    protected static LevelSet createKVPWmtsLevelSet(WmtsLayer wmtsLayer, CompatibleTileMatrixSet compatibleTileMatrixSet, String template) {
        Sector boundingBox = null;
        OwsWgs84BoundingBox wgs84BoundingBox = wmtsLayer.getWgs84BoundingBox();
        if (wgs84BoundingBox == null) {
            System.err.println("LayerFactory createWmtsLevelSet WGS84BoundingBox not defined for layer: " + wmtsLayer.getIdentifier());
        } else {
            boundingBox = wgs84BoundingBox.getSector();
        }

        WmtsTileMatrixSet tileMatrixSet = wmtsLayer.getCapabilities().getTileMatrixSet(compatibleTileMatrixSet.tileMatrixSetId);
        if (tileMatrixSet == null) {
            throw new RuntimeException(
                    "LayerFactory: createWmtsLevelSet: Compatible TileMatrixSet not found for: " + compatibleTileMatrixSet);
        }
        int imageSize = tileMatrixSet.getTileMatrices().get(0).getTileHeight();

        WMTS100Capabilities wmtsCaps = wmtsLayer.getCapabilities();

        String styleIdentifier = wmtsLayer.getStyles().get(0).getIdentifier();

        AVList params = new AVListImpl();
        params.setValue(AVKey.WMTS_VERSION, wmtsCaps.getVersion());
        params.setValue(AVKey.LAYER_NAME, wmtsLayer.getTitle());
        params.setValue(AVKey.TILE_WIDTH, imageSize);
        params.setValue(AVKey.TILE_HEIGHT, imageSize);
        params.setValue(AVKey.STYLE_NAMES, styleIdentifier);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/WMTS/".concat(wmtsLayer.getTitle()));
        params.setValue(AVKey.NUM_LEVELS, compatibleTileMatrixSet.tileMatrices.size());
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(90.0d), Angle.fromDegrees(90.0d)));
        params.setValue(AVKey.SECTOR, boundingBox);
        params.setValue(AVKey.TILE_URL_BUILDER, new WMTSImageLayer.URLBuilder(params));

        return new LevelSet(params);
    }


    protected static String buildWmtsKvpTemplate(String kvpServiceAddress, String layer, String format, String styleIdentifier, String tileMatrixSet) {
        StringBuilder urlTemplate = new StringBuilder(kvpServiceAddress);

        int index = urlTemplate.indexOf("?");
        if (index < 0) { // if service address contains no query delimiter
            urlTemplate.append("?"); // add one
        } else if (index != urlTemplate.length() - 1) { // else if query delimiter not at end of string
            index = urlTemplate.lastIndexOf("&");
            if (index != urlTemplate.length() - 1) {
                urlTemplate.append("&"); // add a parameter delimiter
            }
        }

        urlTemplate.append("SERVICE=WMTS&");
        urlTemplate.append("REQUEST=GetTile&");
        urlTemplate.append("VERSION=1.0.0&");
        urlTemplate.append("LAYER=").append(layer).append("&");
        urlTemplate.append("STYLE=").append(styleIdentifier).append("&");
        urlTemplate.append("FORMAT=").append(format).append("&");
        urlTemplate.append("TILEMATRIXSET=").append(tileMatrixSet).append("&");
//        urlTemplate.append("TILEMATRIX=").append(WmtsTileFactory.TILEMATRIX_TEMPLATE).append("&");
//        urlTemplate.append("TILEROW=").append(WmtsTileFactory.TILEROW_TEMPLATE).append("&");
//        urlTemplate.append("TILECOL=").append(WmtsTileFactory.TILECOL_TEMPLATE);

        return urlTemplate.toString();
    }

    protected static class CompatibleTileMatrixSet {

        protected String tileMatrixSetId;
        protected List<String> tileMatrices = new ArrayList<>();

    }

}
