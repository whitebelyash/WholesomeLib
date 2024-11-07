package ru.whbex.lib.sql.v2.conn;

// ConnectionData

/**
 * Database connection config
 * @param dbName database name. Defines file name with file-backed DBs
 * @param dbAddress database address. Defines directory path containing DB file with file-backed DBs
 * @param dbUser database user
 * @param dbPassword database password
 */
public record ConnectionConfig(String dbName, String dbAddress, String dbUser, String dbPassword) {}
