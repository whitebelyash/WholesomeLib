package ru.whbex.lib.sql;

import java.sql.SQLException;

/**
 * Checked SQL Callback
 * @param <T> Accept type
 * @param <R> Return type
 */
@FunctionalInterface
public interface SQLCallback<T, R> {

    R execute(T t) throws SQLException;
}
