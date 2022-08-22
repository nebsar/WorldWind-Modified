/*
 * Copyright (C) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.mercator.sqlitemap;

import gov.nasa.worldwind.layers.mercator.ProceduralTiledImageLayer;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.layers.mercator.MercatorTextureTile;
import gov.nasa.worldwind.layers.mercator.mbtiles.MetadataEntry;
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;

/**
 * @author Nebi Sarikaya - adapt to mapbox version 1.3 and WorldWind 2.2.1
 * @author Alessio Iannone
 */
public class SQLiteMapLayer extends ProceduralTiledImageLayer {

    private final static BufferedImage emptyBuffer = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
    private static SQLiteMapReader mBTilesReader;

    public enum MBTileVersion {
        v10,
        v11,
        v12,
        v13
    }

    public SQLiteMapLayer(LevelSet levelSet, String shortName, String prettyName,
            String mbtileRootDir) {
        super(initParams(mbtileRootDir, prettyName));
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
    public SQLiteMapLayer(String shortName, String prettyName, String mbtileRootDir) {
        this(initParams(mbtileRootDir, prettyName), shortName, prettyName, mbtileRootDir);
    }

    /**
     *
     * @param mBTilesData
     * @return LevelSet
     */
    private static LevelSet initParams(String fileString, String layerName) {
        mBTilesReader = new SQLiteMapReader(new File(fileString));
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/SQLiteMap/".concat(layerName));
        // params.setValue(AVKey.SERVICE, service);
        params.setValue(AVKey.DATASET_NAME, "h");
        params.setValue(AVKey.FORMAT_SUFFIX, ".png");
        params.setValue(AVKey.NUM_LEVELS, mBTilesReader.getMaxZoom() - 2);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, mBTilesReader.getMinZoom() - 3);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(22.5d), Angle.fromDegrees(45d)));
        params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));

        return new LevelSet(params);
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
        SQLiteMapTile tile = null;
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
    public BufferedImage createTileImage(MercatorTextureTile tile, BufferedImage image
    ) {
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

}
