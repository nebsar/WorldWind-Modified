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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.*;

/**
 * @author Nebi Sarikaya - adapt to mapbox version 1.3 and WorldWind 2.2.1
 * @author Alessio Iannone
 */
public class MapBoxTileLayerNew extends ProceduralTiledImageLayer {

    public final static String DEFAULT_TEST_CLASS = "org.sqlite.JDBC";
    public final static String TEST_CLASS_PROPERTY = "testClass";
    public final static String CONNECTION_PREFIX = "jdbc:sqlite:";

    /**
     * Test class to use for check existence of JDBC drivers.
     */
    protected String testClass = DEFAULT_TEST_CLASS;
    private boolean driverFound;
    private Connection conn;
    private Statement stat;
    private final static BufferedImage emptyBuffer = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

    enum MBTileVersion {
        v10,
        v11,
        v12,
        v13
    }

    private MBTileVersion mbTileVersion;

    public MapBoxTileLayerNew(LevelSet levelSet, String shortName, String prettyName, String mbtileRootDir) {
        super(levelSet);
        setName(prettyName);
        setImageTransformation(false);
        if (mbtileRootDir == null || mbtileRootDir.length() == 0) {
            mbtileRootDir = System.getProperty(shortName.concat(".").concat("mbTileRootDir"));
        }

        initDBConnection(mbtileRootDir);
    }

    /**
     * @param shortName
     * @param prettyName
     * @param detailHint
     * @param mbtileRootDir
     */
    public MapBoxTileLayerNew(String shortName, String prettyName, String mbtileRootDir) {
        this(makeLevels(shortName, mbtileRootDir), shortName, prettyName, mbtileRootDir);
    }

    /**
     * Create default level set
     *
     * @param shortName
     * @param mbTileFile
     *
     * @return
     */
    public static LevelSet makeLevels(String shortName, String mbTileFile) {
        Connection connection = null;
        Statement stmt = null;
        String mapBoxTileVersion = null;

        AVList params = new AVListImpl();

        try {
            Class.forName(DEFAULT_TEST_CLASS);
            connection = DriverManager.getConnection(CONNECTION_PREFIX + new File(mbTileFile).getAbsolutePath());
        } catch (ClassNotFoundException | SQLException exception) {
        }

        String typeQueryString = "SELECT name FROM sqlite_master WHERE type='table' AND name='map';";

        try {
            stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(typeQueryString);

            if (resultSet.getString("name").equalsIgnoreCase("map")) {
                mapBoxTileVersion = "v1.0";
            }
        } catch (SQLException sqlException) {
            mapBoxTileVersion = "v1.3";
        }

        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/MBTile/".concat(shortName));
        // params.setValue(AVKey.SERVICE, service);
        params.setValue(AVKey.DATASET_NAME, "h");
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(22.5d), Angle.fromDegrees(45d)));

        switch (mapBoxTileVersion) {
            case "v1.0": {
                params.setValue(AVKey.FORMAT_SUFFIX, ".png");
                params.setValue(AVKey.NUM_LEVELS, 9);
                params.setValue(AVKey.NUM_EMPTY_LEVELS, 2);
                params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));
                break;
            }
            case "v1.3": {
                try {
                    String sql = "SELECT MAX(zoom_level) FROM tiles";
                    ResultSet resultSet = stmt.executeQuery(sql);
                    params.setValue(AVKey.NUM_LEVELS, resultSet.getInt(1) - 2);

                    sql = "SELECT MIN(zoom_level) FROM tiles";
                    resultSet = stmt.executeQuery(sql);
                    params.setValue(AVKey.NUM_EMPTY_LEVELS, resultSet.getInt(1) - 3);

                    sql = "SELECT * from metadata;";
                    resultSet = stmt.executeQuery(sql);

                    MetadataEntry entry = new MetadataEntry();
                    while (resultSet.next()) {
                        String name = resultSet.getString("name");
                        String value = resultSet.getString("value");
                        entry.addKeyValue(name, value);
                    }

                    params.setValue(AVKey.FORMAT_SUFFIX, entry.getTileMimeType().toString());

                    Angle minLon = Angle.fromDegrees(entry.getTilesetBounds().getLeft());
                    Angle maxLon = Angle.fromDegrees(entry.getTilesetBounds().getRight());
                    params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, minLon, maxLon));

                    break;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        try {
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return new LevelSet(params);
    }

    /**
     * Initialize connection with the sql lite db. An MBTile is a sql lite
     * database
     *
     * @param root
     */
    private void initDBConnection(String root) {
        try {
            Class.forName(testClass);
            driverFound = true;
        } catch (Exception e) {
            Logging.logger().warning("can't locate sqlite JDBC components");
            driverFound = false;
        }
        if (root == null || root.length() == 0) {
            System.out.println("No rootDir has been defined");
            return;
        }
        try {
            conn = DriverManager.getConnection(CONNECTION_PREFIX + new File(root).getAbsolutePath());
        } catch (SQLException e) {
            System.out.println("Unable to open connection with rootDir:" + root);
        }

        try {
            stat = conn.createStatement();
            String typeQueryString = "SELECT name FROM sqlite_master WHERE type='table' AND name='map';";
            ResultSet resultSet = stat.executeQuery(typeQueryString);
            if (resultSet.getString("name").equalsIgnoreCase("map")) {
                mbTileVersion = MBTileVersion.v10;
            }
        } catch (SQLException e) {
            mbTileVersion = MBTileVersion.v13;
            System.out.println("Unable to create statement from connection");
        }
    }

    /**
     * @param zoomLevel
     * @param x - tile column
     * @param y - tile row
     *
     * @return
     */
    private synchronized BufferedImage getBuffered(int zoomLevel, int x, int y) {
        // System.out.println("MBTileLayer.getBuffered() zoomLevel:" +
        // zoomLevel+ " x:" + x + " y:" + y);
        if (!driverFound) {
            return emptyBuffer;
        }
        String statement = getStatement(mbTileVersion, zoomLevel, x, y);
        BufferedImage bi = null;
        try (ResultSet rs = stat.executeQuery(statement)) {
            while (rs.next()) {
                byte[] imageBytes = rs.getBytes(1);

                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                bi = ImageIO.read(bis);

                if (bi != null) {
                    return bi;
                }
            }
        } catch (Exception e) {
            System.err.println("something went wrong fetching image from database: " + e.getMessage());
            e.printStackTrace();
        }
        return emptyBuffer;
    }

    private String getStatement(MBTileVersion mbTileVersion, int zoomLevel, int x, int y) {
        int level = zoomLevel + 3;
        int row = y;
        int column = x;
        
        switch (mbTileVersion) {
            case v10: {
                return "select tile_data,map.tile_id from map, images where zoom_level = " + level
                        + " and tile_column = " + column + " and tile_row = " + row + " and map.tile_id = images.tile_id;";
            }
            case v13: {
                return "select tile_data from tiles where zoom_level = " + level
                        + " and tile_column = " + column + " and tile_row = " + row + ";";
            }
        }
        
        return null;
    }

    /**
     *
     */
    @Override
    public BufferedImage createTileImage(MercatorTextureTile tile, BufferedImage image) {
        int width = tile.getLevel().getTileWidth();
        int height = tile.getLevel().getTileHeight();
        //Sector sector = tile.getSector();
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
