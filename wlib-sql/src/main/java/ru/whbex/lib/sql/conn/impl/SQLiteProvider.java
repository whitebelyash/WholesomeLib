package ru.whbex.lib.sql.conn.impl;

import org.slf4j.event.Level;
import ru.whbex.lib.log.LogContext;
import ru.whbex.lib.sql.conn.ConnectionConfig;
import ru.whbex.lib.sql.conn.ConnectionProvider;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// copy of H2ConnProvider
public class SQLiteProvider implements ConnectionProvider {
    private final ConnectionConfig conf;
    private Connection conn;
    private final File db;
    public SQLiteProvider(ConnectionConfig config) throws ClassNotFoundException, IOException {
        this.conf = config;
        // Do not initialize if H2 not present in the classpath
        Class.forName("org.sqlite.JDBC");
        // Initialize database file
        db = new File(config.dbAddress(), config.dbName());
        if(!db.exists())
            db.createNewFile();

    }
    @Override
    public Connection getConnection() throws SQLException {
        if(conn != null && !conn.isClosed())
            return conn;
        conn = DriverManager.getConnection(ConnectionProvider.JDBC_PREFIX + "sqlite:" + db.getAbsolutePath());
        return conn;
    }

    @Override
    public Connection newConnection() throws SQLException {
        breakConnection();
        conn = DriverManager.getConnection(ConnectionProvider.JDBC_PREFIX + "sqlite:" + db.getAbsolutePath());
        return conn;
    }

    @Override
    public void breakConnection() throws SQLException {
        if(conn != null && !conn.isClosed()) {
            try {
                conn.close();
            } catch (SQLException e){
                LogContext.log(Level.ERROR, "Failed to close database connection (provider: {0})", this.getClass().getName());
            } finally {
                // Don't need this anymore
                conn = null;
            }
        }
        // Do nothing if already broken
    }
}
