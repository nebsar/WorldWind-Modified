package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.mercator.*;
import gov.nasa.worldwind.ogc.wmts.WMTS100Capabilities;
import gov.nasa.worldwind.ogc.wmts.WmtsLayer;
import gov.nasa.worldwind.ogc.wmts.WmtsResourceUrl;
import gov.nasa.worldwind.ogc.wmts.WmtsTileMatrixSet;
import gov.nasa.worldwind.util.*;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WMTS3857Layer extends BasicMercatorTiledImageLayer {

    protected static List<String> compatibleImageFormats
            = Arrays.asList("image/png",
                    "image/jpg",
                    "image/jpeg",
                    "image/gif",
                    "image/bmp");

    protected static List<String> compatibleCoordinateSystems
            = Arrays.asList("urn:ogc:def:crs:EPSG::3857");
    
    private String layerName = getClass().getCanonicalName();

    public WMTS3857Layer(WMTS100Capabilities caps, String layerName) {
        super(makeLevels(caps, layerName));
        this.layerName = layerName;
    }

    protected static List<String> determineCoordSysCompatibleTileMatrixSets(WmtsLayer layer) {
        List<String> compatibleTileMatrixSets = new ArrayList<>();

        // Look for compatible coordinate system types
        List<WmtsTileMatrixSet> tileMatrixSets = layer.getLayerSupportedTileMatrixSets();
        for (WmtsTileMatrixSet tileMatrixSet : tileMatrixSets) {
            if (tileMatrixSet.getSupportedCrs().contains("EPSG::3857")) {
                compatibleTileMatrixSets.add(tileMatrixSet.getIdentifier());
            }
        }

        return compatibleTileMatrixSets;
    }

    private static LevelSet makeLevels(WMTS100Capabilities caps, String layerName) {
        WmtsLayer wmtsLayer = caps.getLayerByName(layerName);

        List<WmtsResourceUrl> resourceUrls = wmtsLayer.getResourceUrls();
        String template = null;
        if (resourceUrls != null) {
            for (WmtsResourceUrl resourceUrl : resourceUrls) {
                if (compatibleImageFormats.contains(resourceUrl.getFormat())) {
                    template = resourceUrl.getTemplate().replace("{TileMatrixSet}", "EPSG:3857");

                }
            }
        }

        WmtsTileMatrixSet tileMatrixSet = wmtsLayer.getCapabilities().getTileMatrixSet("GoogleMapsCompatible");

        int imageSize = tileMatrixSet.getTileMatrices().get(0).getTileHeight();

        String styleIdentifier = wmtsLayer.getStyles().get(0).getIdentifier();

        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, imageSize);
        params.setValue(AVKey.TILE_HEIGHT, imageSize);
        params.setValue(AVKey.STYLE_NAMES, styleIdentifier);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/WMTS/" + layerName);
        params.setValue("URLTemplate", template);
        params.setValue(AVKey.DATASET_NAME, "*");
        params.setValue(AVKey.LAYER_NAME, layerName);
        params.setValue(AVKey.FORMAT_SUFFIX, ".jpg");
        params.setValue(AVKey.NUM_LEVELS, 17);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
                .fromDegrees(22.5d), Angle.fromDegrees(45d)));
        params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0,
                Angle.NEG180, Angle.POS180));
        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder(params));

        return new LevelSet(params);
    }

    private static class URLBuilder implements TileUrlBuilder {

        private static String TILEMATRIX_TEMPLATE = "{TileMatrix}";
        private static String TILEROW_TEMPLATE = "{TileRow}";
        private static String TILECOL_TEMPLATE = "{TileCol}";
        private static String STYLE_TEMPLATE = "{style}";
        private String URLTemplate;
        private String style;
        private String layerName;

        public URLBuilder(AVList params) {
            URLTemplate = params.getStringValue("URLTemplate");
            this.style = params.getStringValue(AVKey.STYLE_NAMES);
            this.layerName = params.getStringValue(AVKey.LAYER_NAME);
        }

        public URL getURL(Tile tile, String imageFormat)
                throws MalformedURLException {

            String url = this.URLTemplate.replace(TILEMATRIX_TEMPLATE, (tile.getLevelNumber() + 3) + "");
            url = url.replace(TILEROW_TEMPLATE, ((1 << (tile.getLevelNumber()) + 3) - 1 - tile.getRow()) + "");
            url = url.replace(TILECOL_TEMPLATE, tile.getColumn() + "");
            if (style != null) {
                url = url.replace(STYLE_TEMPLATE, this.style);
            } else {

                url = url.replace("{", "%7B");
                url = url.replace("}", "%7D");
            }

            return new URL(url);
        }
    }

    @Override
    public String toString() {
        return layerName;
    }
}
