package ru.whbex.lib.sql;

import ru.whbex.lib.log.LogContext;

import java.sql.*;
import org.slf4j.event.Level;
import ru.whbex.lib.log.LogDebug;

/* JDBC adapter. Supports query/update, preparedstatement query/update, batched update */
public abstract class SQLAdapter {
    private volatile Connection con;

    public static final String JDBC_PREFIX = "jdbc";
    public static final int LOGIN_TIMEOUT = 3;

    public SQLAdapter(String driverClass) throws ClassNotFoundException {
        Class.forName(driverClass);
    }


    public final boolean isClosed() throws SQLException {
        return con == null || con.isClosed();
    }

    public final boolean isValid() throws SQLException {
        return con != null && con.isValid(5000); // TODO: move timeout to constants?
    }

    public abstract Connection getConnection() throws SQLException;

    // TODO: Implement this another way - connect on query/update, not on plugin startup
    public final void connect() throws SQLException {
        if (!isClosed())
            throw new IllegalStateException("Already connected!");
        LogContext.log(Level.INFO, "Connecting to the database...");
        con = getConnection();
        LogContext.log(Level.INFO, "Connected");
    }

    public final void disconnect() throws SQLException {
        if (!isClosed()) {
            LogContext.log(Level.INFO, "Disconnecting from the database...");
            con.close();
            LogContext.log(Level.INFO, "Disconnected");
        }
    }

    public final boolean query(String sql, SQLCallback<ResultSet> callback) throws SQLException {
        boolean ret;
        LogDebug.print("SQLAdapter query: {0}", sql);
        try (
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            ret = callback.execute(rs);
        } catch (SQLException e) {
            handleException(e);
            throw new SQLException(e);
        }
        return ret;
    }

    /**
     * Execute update on database
     *
     * @param sql sql
     * @return affected rows if success, -1 otherwise
     * @throws SQLException
     */
    public final int update(String sql) throws SQLException {
        LogDebug.print("SQLAdapter update: {0}", sql);
        try (Statement st = con.createStatement()) {
            return st.executeUpdate(sql);
        } catch (SQLException e) {
            handleException(e);
            throw new SQLException(e);
        }
    }

    public final boolean queryPrepared(String sql, SQLCallback<PreparedStatement> ps, SQLCallback<ResultSet> callback) throws SQLException {
        LogDebug.print("SQLAdapter prepared query: {0}", sql);
        boolean ret;
        try (
                PreparedStatement s = con.prepareStatement(sql)
        ) {
            ps.execute(s);
            ret = callback.execute(s.executeQuery());
        } catch (SQLException e) {
            handleException(e);
            throw new SQLException(e);
        }
        return ret;
    }

    public final int updatePrepared(String sql, SQLCallback<PreparedStatement> ps) throws SQLException {
        LogDebug.print("SQLAdapter prepared update: {0}", sql);
        try (
                PreparedStatement s = con.prepareStatement(sql)
        ) {
            ps.execute(s);
            return s.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
            throw new SQLException(e);
        }
    }

    public final int[] updateBatched(String sql, SQLCallback<PreparedStatement> ps) throws SQLException {
        LogDebug.print("SQLAdapter batched update: {0}", sql);
        try (
                PreparedStatement s = con.prepareStatement(sql)
        ) {
            ps.execute(s);
            return s.executeBatch();
        } catch (SQLException e) {
            handleException(e);
            throw new SQLException(e);
        }
    }
    private static void handleException(SQLException e){
        LogContext.log(Level.ERROR, "!!! Failed to execute SQL query/update !!!");
        LogContext.log(Level.ERROR, "Message: {0}", e.getLocalizedMessage());
        LogContext.log(Level.ERROR, "SQL State: {0}", e.getSQLState());
        LogContext.log(Level.ERROR, "!!! !!!");
    }
}
