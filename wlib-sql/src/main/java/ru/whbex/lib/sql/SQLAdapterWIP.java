package ru.whbex.lib.sql;

import org.slf4j.event.Level;
import ru.whbex.lib.log.LogContext;
import ru.whbex.lib.sql.conn.ConnectionProvider;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

// TODO: Rename to SQLAdapter and switch everything to it.
// Keeping old SQLAdapter for compatibility purposes
public final class SQLAdapterWIP {
    public final class Executor {
        private final SQLAdapterWIP inst = SQLAdapterWIP.this;
        private Consumer<SQLAdapterWIP> task;
        private ExecutorService eserv;
        private Executor task(Consumer<SQLAdapterWIP> task){
            this.task = task;
            return this;
        }
        public Executor sql(String sql){
            inst.sql = sql;
            return this;
        }
        public Executor exceptionally(Consumer<SQLException> except){
            inst.except = except;
            return this;
        }
        public Executor queryCallback(SQLCallback<ResultSet> callback){
            inst.queryCallback = callback;
            return this;
        }
        public Executor updateCallback(SQLCallback<Integer> callback){
            inst.updateCallback = callback;
            return this;
        }
        public Executor setPrepared(SQLCallback<PreparedStatement> ps){
            inst.valueSetter = ps;
            return this;
        }
        public Executor executorService(ExecutorService es){
            this.eserv = es;
            return this;
        }

        public void execute(){
            task.accept(inst);
        }
        public Future<Void> executeAsync(){
            if(eserv != null)
                return eserv.submit(() -> {execute(); return null;});
            return null;
        }
    }
    private final ConnectionProvider prov;
    private String sql;
    private SQLCallback<ResultSet> queryCallback;
    private SQLCallback<Integer> updateCallback;
    private SQLCallback<PreparedStatement> valueSetter;
    private Consumer<SQLException> except;
    private SQLAdapterWIP(ConnectionProvider provider){
        this.prov = provider;
    }
    public static Executor executor(ConnectionProvider provider, Consumer<SQLAdapterWIP> task){
        return new SQLAdapterWIP(provider)
                .newExecutor()
                .task(task);

    }
    private Executor newExecutor(){
        return new Executor();

    }

    public void query() {
        try {
            query(prov, sql, queryCallback);
        } catch (SQLException e) {
            if(except != null)
                except.accept(e);
        }
    }
    public void preparedQuery() {
        try {
            preparedQuery(prov, sql, valueSetter, queryCallback);
        } catch (SQLException e) {
            if(except != null)
                except.accept(e);
        }
    }
    public void update() {
        try {
            update(prov, sql, updateCallback);
        } catch (SQLException e) {
            if(except != null)
                except.accept(e);
        }
    }
    public void preparedUpdate(){
        try {
            preparedUpdate(prov, sql, valueSetter, updateCallback);
        } catch (SQLException e) {
            if(except != null)
                except.accept(e);
        }
    }

    public static void query(ConnectionProvider provider, String sql, SQLCallback<ResultSet> callback) throws SQLException {
        Connection conn = provider.getConnection();
        // formatting shit
        // AFAIK closing statement will close resultset automatically
        try(Statement s = conn.createStatement()){
            callback.execute(s.executeQuery(sql));
        } catch (SQLException e){
            handleException(e);
            throw new SQLException(e);
        }
    }
    public static void preparedQuery(ConnectionProvider provider, String sql, SQLCallback<PreparedStatement> setter, SQLCallback<ResultSet> callback) throws SQLException {
        Connection conn = provider.getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            // set all values
            setter.execute(ps);
            // dispatch query
            callback.execute(ps.executeQuery());
        } catch (SQLException e){
            handleException(e);
            throw new SQLException(e);
        }
    }
    public static void update(ConnectionProvider provider, String sql, SQLCallback<Integer> callback) throws SQLException {
        Connection conn = provider.getConnection();
        try(Statement s = conn.createStatement()){
            callback.execute(s.executeUpdate(sql));
        } catch (SQLException e){
            handleException(e);
            throw new SQLException(e);
        }
    }
    public static void preparedUpdate(ConnectionProvider provider, String sql, SQLCallback<PreparedStatement> setter, SQLCallback<Integer> callback) throws SQLException {
        Connection conn = provider.getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            setter.execute(ps);
            callback.execute(ps.executeUpdate());
        } catch (SQLException e){
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
