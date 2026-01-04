package ru.whbex.lib.sql.conn;

// ConnectionData

/**
 * Database connection config
 * @param dbName database name. Defines file name for file-backed DBs
 * @param dbAddress database address. Defines directory path containing DB file for file-backed DBs or SQL server address
 * @param dbUser database user
 * @param dbPassword database password
 */
public record ConnectionConfig(String dbName, String dbAddress, String dbUser, String dbPassword) {
    @Override
    public String toString() {
        return "ConnectionConfig{" +
                "dbName='" + dbName + '\'' +
                ", dbAddress='" + dbAddress + '\'' +
                ", dbUser='" + dbUser + '\'' +
                ", dbPassword='" + "<stripped>" + '\'' +
                '}';
    }
}
