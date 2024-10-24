package ru.whbex.lib.sql.impl;

import ru.whbex.lib.sql.ConnectionData;
import ru.whbex.lib.sql.SQLAdapter;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2SQLAdapter extends SQLAdapter {
    private final String path;
    public H2SQLAdapter(ConnectionData data) throws ClassNotFoundException, NoClassDefFoundError, IOException {
        super(org.h2.Driver.class.getName());
        File f = new File(data.dbAddress(), data.dbName());
        if(!f.exists())
            f.createNewFile();
        this.path = SQLAdapter.JDBC_PREFIX + ":h2:" + f.getAbsolutePath();
    }

    @Override
    public Connection getConnection() throws SQLException {
        DriverManager.setLoginTimeout(SQLAdapter.LOGIN_TIMEOUT);
        return DriverManager.getConnection(path);
    }
}
