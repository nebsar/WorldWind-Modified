/*
 * Copyright (C) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.mercator.mbtiles;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.layers.mercator.MercatorTextureTile;
import gov.nasa.worldwind.layers.mercator.ProceduralTiledImageLayer;
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;

/**
 * @author Nebi Sarikaya - adapt to mapbox version 1.3 and WorldWind 2.2.1
 * @author Alessio Iannone
 */
public class MapBoxTileLayer extends ProceduralTiledImageLayer {

    private final static BufferedImage emptyBuffer = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
    private static MBTilesReader mBTilesReader;

    public enum MBTileVersion {
        v10,
        v11,
        v12,
        v13
    }

    public MapBoxTileLayer(LevelSet levelSet, String shortName, String prettyName,
            String mbtileRootDir) {
        super(initParams(readMBTilesFile(mbtileRootDir)));
        if (mbtileRootDir == null || mbtileRootDir.length() == 0) {
            mbtileRootDir = System.getProperty(shortName.concat(".").concat("mbTileRootDir"));
        }

        setName(prettyName);
        setImageTransformation(false);
    }

    /**
     * @param shortName
     * @param prettyName
     * @param detailHint
     * @param mbtileRootDir
     */
    public MapBoxTileLayer(String shortName, String prettyName, String mbtileRootDir) {
        this(initParams(readMBTilesFile(mbtileRootDir)), shortName, prettyName, mbtileRootDir);
    }

    /**
     *
     * @param mBTilesData
     * @return LevelSet
     */
    private static LevelSet initParams(MBTilesData mBTilesData) {
        AVList params = new AVListImpl();
        switch (mBTilesData.getMBTileVersion()) {
            case v10: {
                params.setValue(AVKey.TILE_WIDTH, 256);
                params.setValue(AVKey.TILE_HEIGHT, 256);
                params.setValue(AVKey.DATA_CACHE_NAME, "Earth/MBTile/".concat(mBTilesData.getName()));
                // params.setValue(AVKey.SERVICE, service);
                params.setValue(AVKey.DATASET_NAME, "h");
                params.setValue(AVKey.FORMAT_SUFFIX, ".png");
                params.setValue(AVKey.NUM_LEVELS, 9);
                params.setValue(AVKey.NUM_EMPTY_LEVELS, 2);
                params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(22.5d), Angle.fromDegrees(45d)));
                params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));
                break;
            }
            case v13: {
                params.setValue(AVKey.SERVICE, "file://" + mBTilesData.getName() + "?");
                params.setValue(AVKey.TILE_WIDTH, 256);
                params.setValue(AVKey.TILE_HEIGHT, 256);
                params.setValue(AVKey.DATA_CACHE_NAME, "Earth/MBTile/".concat(mBTilesData.getName()));
                params.setValue(AVKey.DATASET_NAME, "h");
                params.setValue(AVKey.FORMAT_SUFFIX, mBTilesData.getImageType());
                params.setValue(AVKey.NUM_LEVELS, mBTilesData.getMaxLevel() - 2);
                params.setValue(AVKey.NUM_EMPTY_LEVELS, mBTilesData.getMinLevel() - 3);
                params.setValue(AVKey.DETAIL_HINT, 0.8f);
                params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(22.5d), Angle.fromDegrees(45.0d)));
                params.setValue(AVKey.RETAIN_LEVEL_ZERO_TILES, true);
                params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.fromDegrees(mBTilesData.getMinLon()), Angle.fromDegrees(mBTilesData.getMaxLon())));
                break;
            }
        }

        return new LevelSet(params);
    }

    /**
     *
     * @param fileString
     * @return MBTilesData
     */
    private static MBTilesData readMBTilesFile(String fileString) {

        try {
            mBTilesReader = new MBTilesReader(new File(fileString));
            MBTilesData mBTilesData = new MBTilesData(mBTilesReader);

            return mBTilesData;

        } catch (Exception e) {
            Logging.logger().warning("can't locate sqlite JDBC components");
        }
        if (fileString == null || fileString.length() == 0) {
            System.out.println("File is not defined!");
            return null;
        }
        return null;
    }

    /**
     * @param zoomLevel
     * @param x - tile column
     * @param y - tile row
     *
     * @return BufferedImage
     */
    private synchronized BufferedImage getBuffered(int zoomLevel, int x, int y) {
        int zoom = zoomLevel + 3;
        MapboxTile tile = null;
        BufferedImage bi = null;
        try {
            tile = mBTilesReader.getTile(zoom, x, y);
            bi = ImageIO.read(tile.getData());
            if (bi != null) {
                return bi;
            }
        } catch (Exception e) {
        }

        return emptyBuffer;
    }

    /**
     *
     * @param tile
     * @param image
     * @return BufferedImage
     */
    @Override
    public BufferedImage createTileImage(MercatorTextureTile tile, BufferedImage image) {
        int width = tile.getLevel().getTileWidth();
        int height = tile.getLevel().getTileHeight();
       
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        BufferedImage bim = getBuffered(tile.getLevelNumber(), tile.getColumn(), tile.getRow());
        g2.drawImage(bim, 0, 0, width, height, null);

        g2.dispose();
        return image;

    }

    static class MBTilesData {

        private double minLat;
        private double minLon;
        private double maxLat;
        private double maxLon;
        private MetadataEntry.TileMimeType imageType;
        private int minLevel;
        private int maxLevel;
        private String name;
        private MBTileVersion mBTileVersion;

        public MBTilesData(MBTilesReader mBTilesReader) {
            MetadataEntry metadataEntry = mBTilesReader.getMetadata();
            this.setName(metadataEntry.getTilesetName());
            this.setMaxLat(metadataEntry.getTilesetBounds().getTop());
            this.setMaxLon(metadataEntry.getTilesetBounds().getRight());
            this.setMinLat(metadataEntry.getTilesetBounds().getBottom());
            this.setMinLon(metadataEntry.getTilesetBounds().getLeft());
            this.setImageType(metadataEntry.getTileMimeType());
            this.setMinLevel(mBTilesReader.getMinZoom());
            this.setMaxLevel(mBTilesReader.getMaxZoom());
            this.setMBTileVersion(mBTilesReader.getmBTileVersion());
        }

        public MBTileVersion getMBTileVersion() {
            return mBTileVersion;
        }

        public void setMBTileVersion(MBTileVersion mBTileVersion) {
            this.mBTileVersion = mBTileVersion;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImageType() {
            switch (imageType) {
                case JPG:
                    return ".jpg";
                case PNG:
                    return ".png";
                default:
                    return null;
            }
        }

        public void setImageType(MetadataEntry.TileMimeType imageType) {
            this.imageType = imageType;
        }

        public int getMinLevel() {
            return minLevel;
        }

        public void setMinLevel(int minLevel) {
            this.minLevel = minLevel;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public void setMaxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
        }

        public double getMinLat() {
            return minLat;
        }

        public void setMinLat(double minLat) {
            this.minLat = minLat;
        }

        public double getMinLon() {
            return minLon;
        }

        public void setMinLon(double minLon) {
            this.minLon = minLon;
        }

        public double getMaxLat() {
            return maxLat;
        }

        public void setMaxLat(double maxLat) {
            this.maxLat = maxLat;
        }

        public double getMaxLon() {
            return maxLon;
        }

        public void setMaxLon(double maxLon) {
            this.maxLon = maxLon;
        }

    }
}
