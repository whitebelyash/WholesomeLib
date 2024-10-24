package ru.whbex.lib.sql;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLCallback<T> {

    boolean execute(T t) throws SQLException;
}
