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

/**
 * SQLAdapter. Simplifies access to SQL databases
 */
public final class SQLAdapterWIP {
    public final class Executor {
        private final SQLAdapterWIP inst = SQLAdapterWIP.this;
        private Consumer<SQLAdapterWIP> task;
        private ExecutorService eserv;

        /**
         * Task to execute on SQLAdapter
         * @param task Task. See available method references
         * @return Executor instance (chain)
         */
        private Executor task(Consumer<SQLAdapterWIP> task){
            this.task = task;
            return this;
        }

        /**
         * SQL Statement to execute on SQLAdapter
         * @param sql SQL Statement string
         * @return Executor instance (chain)
         */
        public Executor sql(String sql){
            inst.sql = sql;
            return this;
        }

        /**
         * Task to execute on SQLAdapter exception
         * @param except Task
         * @return Executor instance (chain)
         */
        public Executor exceptionally(Consumer<SQLException> except){
            inst.except = except;
            return this;
        }

        /**
         * Query callback. Will be executed on query complete, ResultSet is managed on SQLAdapter side, don't close
         * @param callback callback
         * @return Executor instance (chain)
         */
        public Executor queryCallback(SQLCallback<ResultSet> callback){
            inst.queryCallback = callback;
            return this;
        }
        /**
         * Update callback. Will be executed on update complete
         * @param callback callback
         * @return Executor instance (chain)
         */
        public Executor updateCallback(SQLCallback<Integer> callback){
            inst.updateCallback = callback;
            return this;
        }
        /**
         * PreparedStatement value setter
         * @param ps PreparedStatement callback
         * @return Executor instance (chain)
         */
        public Executor setPrepared(SQLCallback<PreparedStatement> ps){
            inst.valueSetter = ps;
            return this;
        }
        /**
         * Executor service for executeAsync()
         * @param es executor service instance
         * @return Executor instance (chain)
         */
        public Executor executorService(ExecutorService es){
            this.eserv = es;
            return this;
        }

        /**
         * Execute task on SQLAdapter
         */
        public void execute(){
            task.accept(inst);
        }

        /**
         * Execute task on SQLAdapter asynchronously
         * @return Future object of task or null if ExecutorService isn't provided.
         */
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

    /**
     * Get executor chain.
     * @param provider Connection provider
     * @param task Task to execute on SQLAdapter. See available method references
     * @return Executor instance
     */
    public static Executor executor(ConnectionProvider provider, Consumer<SQLAdapterWIP> task){
        return new SQLAdapterWIP(provider)
                .newExecutor()
                .task(task);

    }
    private Executor newExecutor(){
        return new Executor();

    }

    /**
     * Execute query on SQLAdapter
     */
    public void query() {
        try {
            query(prov, sql, queryCallback);
        } catch (SQLException e) {
            if(except != null)
                except.accept(e);
        }
    }

    /**
     * Execute query with PreparedStatement on SQLAdapter
     */
    public void preparedQuery() {
        try {
            preparedQuery(prov, sql, valueSetter, queryCallback);
        } catch (SQLException e) {
            if(except != null)
                except.accept(e);
        }
    }

    /**
     * Execute update on SQLAdapter
     */
    public void update() {
        try {
            update(prov, sql, updateCallback);
        } catch (SQLException e) {
            if(except != null)
                except.accept(e);
        }
    }

    /**
     * Execute update with PreparedStatement on SQLAdapter
     */
    public void preparedUpdate(){
        try {
            preparedUpdate(prov, sql, valueSetter, updateCallback);
        } catch (SQLException e) {
            if(except != null)
                except.accept(e);
        }
    }

    /**
     * Execute query on SQLAdapter.
     * @param provider Connection provider
     * @param sql SQL Statement string
     * @param callback Query callback. Will be executed on query complete, ResultSet is managed on SQLAdapter side, don't close
     */
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
    /**
     * Execute query with PreparedStatement on SQLAdapter.
     * @param provider Connection provider
     * @param sql SQL Statement string
     * @param callback Query callback. Will be executed on query complete, ResultSet is managed on SQLAdapter side, don't close
     * @param setter Value setter callback
     */
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
    /**
     * Execute update on SQLAdapter.
     * @param provider Connection provider
     * @param sql SQL Statement string
     * @param callback Update callback. Will be executed on query complete, ResultSet is managed on SQLAdapter side, don't close
     */
    public static void update(ConnectionProvider provider, String sql, SQLCallback<Integer> callback) throws SQLException {
        Connection conn = provider.getConnection();
        try(Statement s = conn.createStatement()){
            callback.execute(s.executeUpdate(sql));
        } catch (SQLException e){
            handleException(e);
            throw new SQLException(e);
        }
    }
    /**
     * Execute update with PreparedStatement on SQLAdapter.
     * @param provider Connection provider
     * @param sql SQL Statement string
     * @param callback Update callback. Will be executed on query complete, ResultSet is managed on SQLAdapter side, don't close
     * @param setter Value setter callback
     */
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
