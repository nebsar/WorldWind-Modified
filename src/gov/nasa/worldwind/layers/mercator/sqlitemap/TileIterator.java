package gov.nasa.worldwind.layers.mercator.sqlitemap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A streaming iterator to extract tiles
 */
public class TileIterator {

    private ResultSet rs;

    public TileIterator(ResultSet s) {
        rs = s;
    }

    public boolean hasNext() {
        try {
            boolean hasNext = rs.next();
            if (!hasNext) {
                close();
            }
            return hasNext;
        } catch (SQLException e) {
            return false;
        }
    }

    public SQLiteMapTile next() {
        try {
            int zoom = rs.getInt("zoom");
            int column = rs.getInt("x");
            int row = rs.getInt("y");
            InputStream tile_data;
            if (rs.getBytes(4) != null) {
                tile_data = new ByteArrayInputStream(rs.getBytes(4));
            } else {
                tile_data = new ByteArrayInputStream(new byte[]{});
            }
            return new SQLiteMapTile(zoom, column, row, tile_data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            rs.close();
        } catch (SQLException e) {
        }
    }
}
