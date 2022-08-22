/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.layers.mercator.tpk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author EagleEye
 */
public class TPKTrial {

    // the TPK file (a TPK file is a ZIP file)
    private ZipFile theTPK;

    // circumference of the earth in metres at the equator
    private static double WORLD_CIRCUMFERENCE = 40075016.69;
    private static double ORIGIN_OFFSET = WORLD_CIRCUMFERENCE / 2.0;
    // in a web map service tileset all tiles are 256x256 pixels
    private static long TILE_PIXEL_SIZE = 256;

    // map of contents of TPK file by file "path/name"
    private Map<String, ZipEntry> zipEntryMap = new HashMap<>();

    // parsing the conf.xml file
    private static String CONFIGURATION_FILE = "/conf.xml";
    private static String TAG_STORAGE_FORMAT = "StorageFormat";
    private static String TAG_LOD_INFO = "LODInfo";
    private static String TAG_TILE_FORMAT = "CacheTileFormat";
    private static String TAG_LEVEL_ID = "LevelID";
    private static String TAG_RESOLUTION = "Resolution";

    // maps conf.xml//CacheInfo/TileCacheInfo/LODInfos/LODInfo/LevelID values to actual Web Map
    // Tile Service zoom levels (only used in initial open of TPK)
    private Map<Long, Long> zoomLevelMapping;

    // individual tiles stored in this format
    private String imageFormat;

    // holds the Geographical bounds of the map coverage
    // private Envelope bounds;
    // maps WMTS zoom level to it's resolution (only used in initial open of TPK)
    private Map<Long, Double> zoomLevelResolutionMap;

    // TPKZoomLevel object for each WTMS zoom level in file
    private Map<Long, TPKZoomLevel> zoomLevelMap = new HashMap<>(); //!!! Simdilik burada instatiate yapiroeum

    // support for compact cache v1 and v2!!
    private static String COMPACT_CACHE_V1 = "esriMapCacheStorageModeCompact";
    private static String COMPACT_CACHE_V2 = "esriMapCacheStorageModeCompactV2";

    // finding zoom levels and their bundles/indexes
    private static String LEVEL_FOLDER = "_alllayers/L%02d/";
    private static String BUNDLE_DATA_EXTENSION = ".bundle";
    private static String BUNDLE_INDEX_EXTENSION = ".bundlx";

    // support the original compact cache format as well as the new one
    private enum CacheType {
        V1, // bundle data and index files are separate
        V2 // index is included within the bundle data file and is different format
    }

    private CacheType cacheType;

    private void openTPK(File theFile) {
        // open the TPK file as a ZIP archive
        try {
            theTPK = new ZipFile(theFile);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to open TPK file", ex);
        }

        // build a map of file path/name -> ZipEntry
        Enumeration<? extends ZipEntry> zipEntries = theTPK.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry entry = zipEntries.nextElement();
            System.out.println(entry.getName());
            zipEntryMap.put(entry.getName(), entry);
        }

        String xmlConf
                = zipEntryMap.keySet().stream()
                        .filter(s -> s.endsWith(CONFIGURATION_FILE))
                        .findFirst()
                        .orElse(null);

        System.out.println("bu conf file: " + xmlConf);

        parseConfigurationFile(zipEntryMap.get(xmlConf));
        
        loadZoomLevels();
    }

    private void parseConfigurationFile(ZipEntry confFile) {
        zoomLevelMapping = new HashMap<>();
        zoomLevelResolutionMap = new HashMap<>();

        try (InputStream is = theTPK.getInputStream(confFile)) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document confDoc = dBuilder.parse(is);

            // find the "StorageFormat" element to determine compact cache version/format
            NodeList sfs = confDoc.getElementsByTagName(TAG_STORAGE_FORMAT);
            Node sf = sfs.item(0);
            if (sf.getNodeType() == Node.ELEMENT_NODE) {
                String storageFormat = sf.getTextContent();
                //System.out.println("Storage Format: "+ storageFormat);
                if (storageFormat.equals(COMPACT_CACHE_V1)) {
                    cacheType = CacheType.V1;
                } else if (storageFormat.equals(COMPACT_CACHE_V2)) {
                    cacheType = CacheType.V2;
                } else {
                    throw new RuntimeException("Unknown value for StorageFormat element");
                }
            }

            // get the format of each tile stored in the cache
            NodeList ctfs = confDoc.getElementsByTagName(TAG_TILE_FORMAT);
            Node ctf = ctfs.item(0);
            if (ctf.getNodeType() == Node.ELEMENT_NODE) {
                imageFormat = ctf.getTextContent();
            }

            // get a list of the zoom-level information elements and go parse them
            NodeList lods = confDoc.getElementsByTagName(TAG_LOD_INFO);
            for (int i = 0; i < lods.getLength(); i++) {
                parseLodInfo(lods.item(i));
            }

        } catch (Exception ex) {
            throw new RuntimeException("Caught exception opening/processing conf.xml", ex);
        }
    }

    /**
     * parse LODInfo/LevelId and LODInfo/Resolution in order to map the nominal
     * LevelID value to an actual zoom level
     *
     * <p>
     * Note: this method loads the zoomLevelMapping and zoomLevelResolutionMap
     * hashmaps
     *
     * @param lodInfo -- "Level of Detail Information" xml element
     */
    private void parseLodInfo(Node lodInfo) {
        if (lodInfo.getNodeType() == Node.ELEMENT_NODE) {
            Element lod = (Element) lodInfo;
            NodeList lidList = lod.getElementsByTagName(TAG_LEVEL_ID);
            NodeList resList = lod.getElementsByTagName(TAG_RESOLUTION);
            Node lid = lidList.item(0);
            Node res = resList.item(0);
            if (lid.getNodeType() == Node.ELEMENT_NODE && res.getNodeType() == Node.ELEMENT_NODE) {
                String levelString = lid.getTextContent();
                System.out.println("Level String: " + levelString);
                Long level = Long.valueOf(levelString);

                String resString = res.getTextContent();
                System.out.println("resolution: " + resString);
                Double resolution = Double.valueOf(resString);
                long zoom_level
                        = Math.round(log2(WORLD_CIRCUMFERENCE / (resolution * TILE_PIXEL_SIZE)));
                System.out.println("Zoom Level: " + zoom_level);
                zoomLevelMapping.put(level, zoom_level);
                zoomLevelResolutionMap.put(zoom_level, resolution);
            }
        }
    }

    /**
     * @param number -- number in question
     * @return -- log base 2 of the given number
     */
    private double log2(double number) {
        return Math.log(number) / Math.log(2.0);
    }

    /**
     * Iterate over the Zoom Levels contained in the TPK archive and build a
     * TPKZoomLevel object for each
     *
     * <p>
     * The TPKZoomLevel object caches control information about the zoom level
     * and each bundle that comprises the zoom level. Access to individual tile
     * data is done via this object.
     */
    private void loadZoomLevels() {

        long startLoad = System.currentTimeMillis();
        for (Long levelId : zoomLevelMapping.keySet()) {

            // "LevelID" folder
            String levelFolder = String.format(LEVEL_FOLDER, levelId);

            List<String> indexes = null;

            // find names of all bundles for level
            List<String> bundles
                    = zipEntryMap.keySet().stream()
                            .filter(s -> s.contains(levelFolder))
                            .filter(s -> s.endsWith(BUNDLE_DATA_EXTENSION))
                            .collect(Collectors.toList());

            // find names of all bundle indexes for level
            if (cacheType == CacheType.V1) { // V2 caches don't have independent indexes
                indexes
                        = zipEntryMap.keySet().stream()
                                .filter(s -> s.contains(levelFolder))
                                .filter(s -> s.endsWith(BUNDLE_INDEX_EXTENSION))
                                .collect(Collectors.toList());
            }

            if (!bundles.isEmpty()) {

                // get the LODInfo/LevelID mapping to actual WTMS zoom level
                Long zoomLevel = zoomLevelMapping.get(levelId);

                // go build a zoom level object using the related bundles and bundle-indexes
                TPKZoomLevel zlObj = null;
                if (cacheType == CacheType.V1) {
                    zlObj = new TPKZoomLevelV1(theTPK, zipEntryMap, bundles, indexes, zoomLevel);
                } else if (cacheType == CacheType.V2) {
                    zlObj = new TPKZoomLevelV2(theTPK, zipEntryMap, bundles, zoomLevel);
                }

                // keep track of it
                zoomLevelMap.put(zoomLevel, zlObj);
            }
        }

        String msg
                = String.format(
                        "Loaded zoom levels in %d milliseconds",
                        System.currentTimeMillis() - startLoad);
       // LOGGER.fine(msg);
    }
    
        /**
     * Return a list of tile objects, each with its intended location, format and raw data
     *
     * @param zoomLevel -- zoom level of tiles to return
     * @param top -- topmost row of tiles (lattitude)
     * @param bottom -- bottommost row of tiles
     * @param left -- leftmost column of tiles (longitude)
     * @param right -- rightmost column of tiles
     * @param format -- format to interpret tile data (PNG, JPEG)
     * @return -- list of TPKTile objects
     */
    public List<TPKTile> getTiles(
            long zoomLevel, long top, long bottom, long left, long right, String format) {
        if (zoomLevelMap.containsKey(zoomLevel)) {
            return zoomLevelMap.get(zoomLevel).getTiles(top, bottom, left, right, format);
        }
        return null;
    }

    public static void main(String[] args) {
        TPKTrial tPKTrial = new TPKTrial();
        tPKTrial.openTPK(new File("tpk/world-all.tpk"));
    }

}
