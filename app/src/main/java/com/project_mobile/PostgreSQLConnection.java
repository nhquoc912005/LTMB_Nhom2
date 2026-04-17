package com.project_mobile;

import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLConnection {
    private static final String TAG = "PostgreSQLConnection";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD
            );
            Log.d(TAG, "Connection successful!");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "PostgreSQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            Log.e(TAG, "Connection failed: " + e.getMessage());
        }
        return connection;
    }
}
