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
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.sql.*;

/**
 * @author Alessio Iannone
 */
public class MapBoxTileLayer extends ProceduralTiledImageLayer
{
    public final static String DEFAULT_TEST_CLASS = "org.sqlite.JDBC";
    public final static String TEST_CLASS_PROPERTY = "testClass";

    private final MBTileVersion mbTileVersion;

    public enum MBTileVersion
    {
        LEGACY,
        NEW
    }

    /**
     * Test class to use for check existence of JDBC drivers.
     */
    protected String testClass = DEFAULT_TEST_CLASS;
    private boolean driverFound;
    private Connection conn;
    private Statement stat;
    private final static BufferedImage emptyBuffer = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

    public MapBoxTileLayer(LevelSet levelSet, String shortName, String prettyName, 
        String mbtileRootDir, MBTileVersion mbTileVersion)
    {
        super(levelSet);
        setName(prettyName);
        setImageTransformation(false);
        if (mbtileRootDir == null || mbtileRootDir.length() == 0)
        {
            mbtileRootDir = System.getProperty(shortName.concat(".").concat("mbTileRootDir"));
        }
        this.mbTileVersion = mbTileVersion;
        initDBConnection(mbtileRootDir);
    }

    public MapBoxTileLayer(LevelSet levelSet, String shortName, String prettyName,
        String mbtileRootDir)
    {
        this(levelSet, shortName, prettyName, mbtileRootDir, MBTileVersion.LEGACY);
    }

    /**
     * @param shortName
     * @param prettyName
     * @param detailHint
     * @param mbtileRootDir
     */
    public MapBoxTileLayer(String shortName, String prettyName, String mbtileRootDir)
    {
        this(makeLevels(shortName), shortName, prettyName, mbtileRootDir);
    }

    /**
     * @param shortName
     * @param prettyName
     * @param detailHint
     * @param mbtileRootDir
     * @param mbTileVersion specify the MBTileVersion we are using, this will change accordingly the SQL to retrieve the databse data
     */
    public MapBoxTileLayer(String shortName, String prettyName, String mbtileRootDir,
        MBTileVersion mbTileVersion)
    {
        this(makeLevels(shortName), shortName, prettyName, mbtileRootDir, mbTileVersion);
    }

    /**
     * Create default level set
     *
     * @param shortName
     *
     * @return
     */
    public static LevelSet makeLevels(String shortName)
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/MBTile/".concat(shortName));
        // params.setValue(AVKey.SERVICE, service);
        params.setValue(AVKey.DATASET_NAME, "h");
        params.setValue(AVKey.FORMAT_SUFFIX, ".png");
        params.setValue(AVKey.NUM_LEVELS, 9);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 2);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(22.5d), Angle.fromDegrees(45d)));
        params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));
        return new LevelSet(params);
    }

    /**
     * Initialize connection with the sql lite db. An MBTile is a sql lite
     * database
     *
     * @param root
     */
    private void initDBConnection(String root)
    {
        try
        {
            Class.forName(testClass);
            driverFound = true;
        }
        catch (Exception e)
        {
            Logging.logger().warning("can't locate sqlite JDBC components");
            driverFound = false;
        }
        if (root == null || root.length() == 0)
        {
            System.out.println("No rootDir has been defined");
            return;
        }
        try
        {
            conn = DriverManager.getConnection(root);
        }
        catch (SQLException e)
        {
            System.out.println("Unable to open connection with rootDir:" + root);
        }

        try
        {
            stat = conn.createStatement();
        }
        catch (SQLException e)
        {
            System.out.println("Unable to create statement from connection");
        }
    }

    /**
     * @param zoomLevel
     * @param x         - tile column
     * @param y         - tile row
     *
     * @return
     */
    private synchronized BufferedImage getBuffered(int zoomLevel, int x, int y)
    {
        // System.out.println("MBTileLayer.getBuffered() zoomLevel:" +
        // zoomLevel+ " x:" + x + " y:" + y);
        if (!driverFound)
        {
            return emptyBuffer;
        }
        String statement = getStatement(mbTileVersion, zoomLevel, x, y);
        BufferedImage bi = null;
        try (ResultSet rs = stat.executeQuery(statement))
        {
            while (rs.next())
            {
                byte[] imageBytes = rs.getBytes(1);

                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                bi = ImageIO.read(bis);

                if (bi != null)
                {
                    return bi;
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("something went wrong fetching image from database: " + e.getMessage());
            e.printStackTrace();
        }
        return emptyBuffer;
    }

    private String getStatement(MBTileVersion mbTileVersion, int zoomLevel, int x, int y)
    {
        int level = zoomLevel + 3;
        int row = y;
        int column = x;
        if (mbTileVersion == MBTileVersion.LEGACY)
        {
            return "select tile_data,map.tile_id from map, images where zoom_level = " + level
                + " and tile_column = " + column + " and tile_row = " + row + " and map.tile_id = images.tile_id;";
        }
        return "select tile_data from tiles where zoom_level = " + level
            + " and tile_column = " + column + " and tile_row = " + row + ";";
    }

    /**
     *
     */
    @Override
    public BufferedImage createTileImage(MercatorTextureTile tile, BufferedImage image)
    {
        int width = tile.getLevel().getTileWidth();
        int height = tile.getLevel().getTileHeight();
        Sector sector = tile.getSector();
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        BufferedImage bim = getBuffered(tile.getLevelNumber(), tile.getColumn(), tile.getRow());
        g2.drawImage(bim, 0, 0, width, height, null);

        g2.dispose();
        return image;
    }
}