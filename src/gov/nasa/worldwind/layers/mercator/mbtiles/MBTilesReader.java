package gov.nasa.worldwind.layers.mercator.mbtiles;

import gov.nasa.worldwind.layers.mercator.sqlutil.SQLHelper;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Nebi Sarikaya
 */
public class MBTilesReader {

    private File file;
    private Connection connection;
    private MapBoxTileLayerNew.MBTileVersion mBTileVersion;

    public MBTilesReader(File file) {
        try {

            connection = SQLHelper.establishConnection(file);

            String typeQueryString = "SELECT name FROM sqlite_master WHERE type='table' AND name='map';";
            ResultSet resultSet = SQLHelper.executeQuery(connection, typeQueryString);

            if (resultSet.getString("name").equalsIgnoreCase("map")) {
                this.mBTileVersion = MapBoxTileLayerNew.MBTileVersion.v10;
            }

        } catch (SQLException se) {
            this.mBTileVersion = MapBoxTileLayerNew.MBTileVersion.v13;
        }
        this.file = file;
    }

    public File close() {
        try {
            connection.close();
        } catch (SQLException e) {
        }
        return this.file;
    }

    public MapBoxTileLayerNew.MBTileVersion getmBTileVersion() {
        return mBTileVersion;
    }

    public MetadataEntry getMetadata() {
        String sql = "SELECT * from metadata;";
        try {
            ResultSet resultSet = SQLHelper.executeQuery(connection, sql);
            MetadataEntry entry = new MetadataEntry();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String value = resultSet.getString("value");
                entry.addKeyValue(name, value);
            }
            return entry;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TileIterator getTiles() {
        String sql = "SELECT * from tiles;";
        ResultSet resultSet = SQLHelper.executeQuery(connection, sql);
        return new TileIterator(resultSet);
    }

    public MapboxTile getTile(int zoom, int column, int row) {
        String sql = null;
        switch (this.mBTileVersion) {
            case v10: {
                sql = String.format("SELECT tile_data, map.tile_id from map, images WHERE zoom_level = %d AND tile_column = %d AND tile_row = %d AND"
                        + "map.tile_id = images.tile_id;", zoom, column, row);
                break;
            }
            case v13: {
                sql = String.format("SELECT tile_data FROM tiles WHERE zoom_level = %d AND tile_column = %d AND tile_row = %d", zoom, column, row);
                break;
            }
        }
        try {
            ResultSet resultSet = SQLHelper.executeQuery(connection, sql);
            InputStream tileDataInputStream = null;
            tileDataInputStream = resultSet.getBinaryStream("tile_data");

            return new MapboxTile(zoom, column, row, tileDataInputStream);
        } catch (SQLException e) {

        }
        return null;
    }

    //    public MapboxTile getTileV10(int zoom, int column, int row) throws MBTilesReadException {
//        String sql = String.format("SELECT tile_data, map.tile_id from map, images WHERE zoom_level = %d AND tile_column = %d AND tile_row = %d AND"
//                + "map.tile_id = images.tile_id;", zoom, column, row);
//
//        try {
//            ResultSet resultSet = SQLHelper.executeQuery(connection, sql);
//            System.out.println(resultSet);
//            InputStream tileDataInputStream = null;
//            tileDataInputStream = resultSet.getBinaryStream("tile_data");
//
//            return new MapboxTile(zoom, column, row, tileDataInputStream);
//        } catch (MBTilesException | SQLException e) {
//            throw new MBTilesReadException(String.format("Could not get Tile for z:%d, column:%d, row:%d", zoom, column, row), e);
//        }
//    }
//
//    public MapboxTile getTileV13(int zoom, int column, int row) throws MBTilesReadException {
//        String sql = String.format("SELECT tile_data FROM tiles WHERE zoom_level = %d AND tile_column = %d AND tile_row = %d", zoom, column, row);
//        try {
//            ResultSet resultSet = SQLHelper.executeQuery(connection, sql);
//            System.out.println(resultSet);
//            InputStream tileDataInputStream = null;
//            String name = resultSet.getString(1);
//            System.out.println(name);
//            return new MapboxTile(zoom, column, row, tileDataInputStream);
//        } catch (MBTilesException | SQLException e) {
//            throw new MBTilesReadException(String.format("Could not get Tile for z:%d, column:%d, row:%d", zoom, column, row), e);
//        }
//    }
    public int getMaxZoom() {
        String sql = "SELECT MAX(zoom_level) FROM tiles";

        try {
            ResultSet resultSet = SQLHelper.executeQuery(connection, sql);
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getMinZoom() {
        String sql = "SELECT MIN(zoom_level) FROM tiles";

        try {
            ResultSet resultSet = SQLHelper.executeQuery(connection, sql);
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Connection getConnection() {
        return connection;
    }
}
