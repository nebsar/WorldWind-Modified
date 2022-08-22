package gov.nasa.worldwind.layers.mercator.sqlitemap;

import gov.nasa.worldwind.layers.mercator.mbtiles.*;
import gov.nasa.worldwind.layers.mercator.mbtiles.MetadataEntry;
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
public class SQLiteMapReader {

    private File file;
    private Connection connection;

    public SQLiteMapReader(File file) {
        connection = SQLHelper.establishConnection(file);
        this.file = file;
    }

    public File close() {
        try {
            connection.close();
        } catch (SQLException e) {
        }
        return this.file;
    }


    public TileIterator getTiles() {
        String sql = "SELECT * from tile_index;";
        try {
            ResultSet resultSet = SQLHelper.executeQuery(connection, sql);
            return new TileIterator(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SQLiteMapTile getTile(int zoom, int column, int row) {
        String sql = String.format("SELECT data FROM tile_index WHERE zoom = %d AND x = %d AND y = %d", zoom, column, row);

        try {
            ResultSet resultSet = SQLHelper.executeQuery(connection, sql);
            InputStream tileDataInputStream = null;
            tileDataInputStream = resultSet.getBinaryStream("data");

            return new SQLiteMapTile(zoom, column, row, tileDataInputStream);
        } catch (SQLException e) {
           // e.printStackTrace();
        }
        return null;
    }

    public int getMaxZoom() {
        String sql = "SELECT MAX(zoom) FROM tile_index";

        try {
            ResultSet resultSet = SQLHelper.executeQuery(connection, sql);
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getMinZoom() {
        String sql = "SELECT MIN(zoom) FROM tile_index";

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
