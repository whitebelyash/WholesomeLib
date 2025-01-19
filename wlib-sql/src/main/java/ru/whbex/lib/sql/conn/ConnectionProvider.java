package ru.whbex.lib.sql.conn;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Single connection pool. Needs testing in a concurrent environment
 */
public interface ConnectionProvider {
    String JDBC_PREFIX = "jdbc:";
    /**
     * Get connection to the DB. Returns new connection if old connection is not present or closed
     * @return connection object
     */
    Connection getConnection() throws SQLException;

    /**
     * Get connection to the DB. Returns new connection even if old still exists (it will be destroyed)
     * @return connection object
     */
    Connection newConnection() throws SQLException;

    /**
     * Break connection to the DB.
     * @throws SQLException if connection close throws SQLException
     */
    void breakConnection() throws SQLException;

    /**
     * Get this provider's connection config
     * @return configuration
     */
    ConnectionConfig getConfig();


}
