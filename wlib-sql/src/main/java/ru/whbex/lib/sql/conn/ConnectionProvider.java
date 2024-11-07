package ru.whbex.lib.sql.conn;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider {
    String JDBC_PREFIX = "jdbc:";
    /**
     * Get connection to a DB. Returns new connection if old connection is not present or closed
     * @return connection object
     */
    Connection getConnection() throws SQLException;

    /**
     * Get connection to a DB. Returns new connection even if old still exists (it will be destroyed)
     * @return connection object
     */
    Connection newConnection() throws SQLException;

    /**
     * Break connection to a DB.
     * @throws SQLException if connection close throws SQLException
     */
    void breakConnection() throws SQLException;
}
