package ru.whbex.lib.sql.impl;

import ru.whbex.lib.sql.v2.conn.ConnectionConfig;
import ru.whbex.lib.sql.SQLAdapter;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteAdapter extends SQLAdapter {
    private final String path;
    public SQLiteAdapter(ConnectionConfig data) throws ClassNotFoundException, NoClassDefFoundError, IOException {
        super(org.sqlite.JDBC.class.getName());
        File db = new File(data.dbAddress(), data.dbName());
        if(!db.exists())
            db.createNewFile();
        this.path = SQLAdapter.JDBC_PREFIX + ":sqlite:" + db.getAbsolutePath();
    }

    @Override
    public Connection getConnection() throws SQLException {
        DriverManager.setLoginTimeout(SQLAdapter.LOGIN_TIMEOUT);
        return DriverManager.getConnection(path);
    }
}
