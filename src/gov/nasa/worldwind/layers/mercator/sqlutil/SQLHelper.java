package gov.nasa.worldwind.layers.mercator.sqlutil;

import gov.nasa.worldwind.layers.mercator.sqlitemap.*;
import java.io.File;
import java.sql.*;

public class SQLHelper {

    public static Connection establishConnection(File file) {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return c;
    }

    public static ResultSet executeQuery(Connection connection, String sql){
        Statement stmt = createStatement(connection);
        try {
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            
        }
        return null;
    }

    private static Statement createStatement(Connection connection) {
        try {
            return connection.createStatement();
        } catch (SQLException e) {
            
        }
        return null;
    }

    public static void execute(Connection connection, String sql) {
        Statement stmt = createStatement(connection);
        try {
            stmt.execute(sql);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
