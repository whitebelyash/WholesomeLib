package ru.whbex.lib.sql;

import org.slf4j.event.Level;
import ru.whbex.lib.log.LogContext;
import ru.whbex.lib.log.Debug;
import ru.whbex.lib.sql.conn.ConnectionProvider;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * SQLAdapter. Single method SQL database access.
 */
public final class SQLAdapter<T> {
    public final class Executor<T> {
        @SuppressWarnings("unchecked")
        private final SQLAdapter<T> inst = (SQLAdapter<T>) SQLAdapter.this;
        private Consumer<SQLAdapter<T>> task;
        private ExecutorService eserv;
        private T val;

        /**
         * Task to execute on SQLAdapter
         * @param task Task. See available method references
         * @return Executor instance (chain)
         */
        private Executor<T> task(Consumer<SQLAdapter<T>> task){
            this.task = task;
            return this;
        }

        /**
         * SQL Statement to execute on SQLAdapter
         * @param sql SQL Statement string
         * @return Executor instance (chain)
         */
        public Executor<T> sql(String sql){
            inst.sql = sql;
            return this;
        }

        /**
         * Set verbosity status. When true - will log failed operations.
         * @param verbose status
         */
        public Executor<T> setVerbose(boolean verbose){
            inst.verbose = verbose;
            return this;
        }

        /**
         * Task to execute on SQLAdapter exception
         * @param except Task
         * @return Executor instance (chain)
         */
        public Executor<T> exceptionally(Consumer<SQLException> except){
            inst.except = except;
            return this;
        }

        /**
         * Query callback. Will be executed on query complete, ResultSet is managed on SQLAdapter side, don't close
         * @param callback callback
         * @return Executor instance (chain)
         */
        public Executor<T> queryCallback(SQLCallback<SQLResponse, T> callback){
            inst.queryCallback = callback;
            return this;
        }
        /**
         * Update callback. Will be executed on update complete
         * @param callback callback
         * @return Executor instance (chain)
         */
        public Executor<T> updateCallback(SQLCallback<SQLResponse, Void> callback){
            inst.updateCallback = callback;
            return this;
        }
        /**
         * Set primary PreparedStatement value setter. Won't affect additional value setters.
         * @param ps PreparedStatement callback
         * @return Executor instance (chain)
         */
        public Executor<T> setPrepared(SQLCallback<PreparedStatement, Void> ps){
            inst.valueSetter = ps;
            inst.valueSetterExists = true;
            return this;
        }

        /**
         * Add PreparedStatement value setter. Multiple value setters will enable batched execute
         * @param ps PreparedStatement callback
         * @return Executor instance (chain)
         */
        public Executor<T> addPrepared(SQLCallback<PreparedStatement, Void> ps){
            if(!inst.valueSetterExists)
                return setPrepared(ps);
            Debug.print("Adding additional prepared statement");
            List<SQLCallback<PreparedStatement, Void>> setters = inst.valueSetters == null ? new LinkedList<>() : inst.valueSetters;
            setters.add(ps);
            inst.valueSetters = setters;
            return this;
        }
        /**
         * Executor service for executeAsync()
         * @param es executor service instance
         * @return Executor instance (chain)
         */
        public Executor<T> executorService(ExecutorService es){
            this.eserv = es;
            return this;
        }

        /**
         * Execute task on SQLAdapter
         */
        public T execute(){
            task.accept(inst);
            return inst.val;
        }

        /**
         * Execute task on SQLAdapter asynchronously
         * @return Future object of task or null if ExecutorService wasn't provided.
         */
        public Future<T> executeAsync(){
            if(eserv != null)
                return eserv.submit(() -> {execute(); return val;});
            return null;
        }
    }
    private final ConnectionProvider prov;
    private final Class<T> type;
    private T val;
    private String sql;
    private boolean verbose = true;
    private SQLCallback<SQLResponse, T> queryCallback = resp -> null;
    private SQLCallback<SQLResponse, Void> updateCallback = resp -> null;
    private SQLCallback<PreparedStatement, Void> valueSetter = ps -> null;
    private boolean valueSetterExists = false;
    private List<SQLCallback<PreparedStatement, Void>> valueSetters;
    private Consumer<SQLException> except;
    private SQLAdapter(ConnectionProvider provider, Class<T> ret){
        this.prov = provider;
        this.type = ret;
    }

    /**
     * Get executor chain.
     * @param returnType return type class
     * @param provider Connection provider
     * @param task Task to execute on SQLAdapter. See available method references
     * @return Executor instance
     */
    public static <T> SQLAdapter<T>.Executor<T> executor(Class<T> returnType, ConnectionProvider provider, Consumer<SQLAdapter<T>> task){
        return new SQLAdapter<>(provider, returnType)
                .newExecutor()
                .task(task);
    }
    /**
     * Get executor chain.
     * @param provider Connection provider
     * @param task Task to execute on SQLAdapter. See available method references
     * @return Executor instance
     */
    public static SQLAdapter<Void>.Executor<Void> executor(ConnectionProvider provider, Consumer<SQLAdapter<Void>> task){
        return new SQLAdapter<>(provider, Void.class)
                .newExecutor()
                .task(task);
    }
    private Executor<T> newExecutor(){
        return new Executor<>();

    }

    /**
     * Execute query on SQLAdapter
     */
    public void query() {
        try {
            val = query(type, prov, sql, queryCallback, verbose);
        } catch (SQLException e) {
            if (except != null)
                except.accept(e);
        }
    }

    /**
     * Execute query with PreparedStatement on SQLAdapter
     */
    public void preparedQuery() {
        try {
            val = preparedQuery(type, prov, sql, valueSetter, queryCallback, verbose);
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
            update(prov, sql, updateCallback, verbose);
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
            boolean batch = valueSetters != null && !valueSetters.isEmpty();
            if(batch)
                Debug.print("Batched update!");
            SQLCallback<PreparedStatement, Void> p = batch ?
                    ps -> {
                        ps.clearParameters();
                        valueSetter.execute(ps);
                        ps.addBatch();
                        for (SQLCallback<PreparedStatement, Void> sc : valueSetters) {
                            sc.execute(ps);
                            Debug.print("added additional batch");
                            ps.addBatch();
                        }
                        Debug.print("ps: " + ps.toString());
                        return null;
                    } :
                    valueSetter;
            preparedUpdate(prov, sql, p, updateCallback, batch, verbose);
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
     * @param verbose log failed database execution (will forward exception anyway)
     */
    public static <T> T query(Class<T> ret, ConnectionProvider provider, String sql, SQLCallback<SQLResponse, T> callback, boolean verbose) throws SQLException {
        Connection conn = provider.getConnection();
        // formatting shit
        // AFAIK closing statement will close resultset automatically
        try(Statement s = conn.createStatement()){
            return callback.execute(new SQLResponse(s.executeQuery(sql), -1, null));
        } catch (SQLException e){
            if(verbose)
                handleException(e, sql, provider);
            throw new SQLException(e);
        }
    }
    /**
     * Execute query with PreparedStatement on SQLAdapter.
     * @param provider Connection provider
     * @param sql SQL Statement string
     * @param callback Query callback. Will be executed on query complete, ResultSet is managed on SQLAdapter side, don't close
     * @param setter Value setter callback
     * @param verbose log failed database execution (will forward exception anyway)
     */
    public static <T> T preparedQuery(Class<T> ret, ConnectionProvider provider, String sql, SQLCallback<PreparedStatement, Void> setter, SQLCallback<SQLResponse, T> callback, boolean verbose) throws SQLException {
        Connection conn = provider.getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            // set all values
            setter.execute(ps);
            // dispatch query
            return callback.execute(new SQLResponse(ps.executeQuery(), -1, null));
        } catch (SQLException e){
            if(verbose)
                handleException(e, sql, provider);
            throw new SQLException(e);
        }
    }
    /**
     * Execute update on SQLAdapter.
     * @param provider Connection provider
     * @param sql SQL Statement string
     * @param callback Update callback. Will be executed on update complete.
     * @param verbose log failed database execution (will forward exception anyway)
     */
    public static void update(ConnectionProvider provider, String sql, SQLCallback<SQLResponse, Void> callback, boolean verbose) throws SQLException {
        Connection conn = provider.getConnection();
        try(Statement s = conn.createStatement()){
            callback.execute(new SQLResponse(null, s.executeUpdate(sql), null));
        } catch (SQLException e){
            if(verbose)
                handleException(e, sql, provider);
            throw new SQLException(e);
        }
    }
    /**
     * Execute update with PreparedStatement on SQLAdapter.
     * @param provider Connection provider
     * @param sql SQL Statement string
     * @param callback Update callback. Will be executed on query complete.
     * @param setter Value setter callback
     * @param batch enable batched update
     * @param verbose log failed database execution (will forward exception anyway)
     */
    public static void preparedUpdate(ConnectionProvider provider, String sql, SQLCallback<PreparedStatement, Void> setter, SQLCallback<SQLResponse, Void> callback, boolean batch, boolean verbose) throws SQLException {
        Connection conn = provider.getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            setter.execute(ps);
            callback.execute(batch ?
                    new SQLResponse(null, -1, ps.executeBatch()) :
                    new SQLResponse(null, ps.executeUpdate(), null));
        } catch (SQLException e){
            if(verbose)
                handleException(e, sql, provider);
            throw new SQLException(e);
        }
    }

    private static void handleException(SQLException e, String sql, ConnectionProvider prov){
        LogContext.log(Level.ERROR, "=== Failed to execute SQL query/update! ===");
        LogContext.log(Level.ERROR, "Message: {0}", e.getLocalizedMessage());
        LogContext.log(Level.ERROR, "SQL Statement: {0}", sql);
        LogContext.log(Level.ERROR, "ConnectionProvider config: {0}", prov.getConfig());
        LogContext.log(Level.ERROR, "=== !!! ===");
    }

}
