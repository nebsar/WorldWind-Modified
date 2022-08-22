package gov.nasa.worldwind.layers.mercator.sqlitemap;

import gov.nasa.worldwind.layers.mercator.mbtiles.*;
import java.io.File;
import java.sql.*;

public class SQLHelper {
    public static Connection establishConnection(File file) throws SQLiteMapException {
        Connection c;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch (ClassNotFoundException | SQLException e) {
            throw new SQLiteMapException("Establish Connection failed.", e);
        }
        return c;
    }

    public static ResultSet executeQuery(Connection connection, String sql) throws SQLiteMapException {
        Statement stmt = createStatement(connection);
        try {
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            throw new SQLiteMapException("Execute statement on connection failed. (" + sql + ")", e);
        }
    }

    private static Statement createStatement(Connection connection) throws SQLiteMapException {
        try {
            return connection.createStatement();
        } catch (SQLException e) {
            throw new SQLiteMapException("Create a statement on connection failed.", e);
        }
    }

    public static void execute(Connection connection, String sql) throws SQLiteMapException {
        Statement stmt = createStatement(connection);
        try {
            stmt.execute(sql);
            stmt.close();
        } catch (SQLException e) {
            throw new SQLiteMapException("Execute statement on connection failed. (" + sql + ")", e);
        }
    }
}
