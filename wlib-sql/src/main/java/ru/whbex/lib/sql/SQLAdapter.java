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

/**
 * SQLAdapter. Single method SQL database access.
 */
public final class SQLAdapter {
    public final class Executor {
        private final SQLAdapter inst = SQLAdapter.this;
        private Consumer<SQLAdapter> task;
        private ExecutorService eserv;

        /**
         * Task to execute on SQLAdapter
         * @param task Task. See available method references
         * @return Executor instance (chain)
         */
        private Executor task(Consumer<SQLAdapter> task){
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
         * Set verbosity status. When true - will log failed operations.
         * @param verbose status
         */
        public void setVerbose(boolean verbose){
            inst.verbose = verbose;
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
        public Executor queryCallback(SQLCallback<SQLResponse> callback){
            inst.queryCallback = callback;
            return this;
        }
        /**
         * Update callback. Will be executed on update complete
         * @param callback callback
         * @return Executor instance (chain)
         */
        public Executor updateCallback(SQLCallback<SQLResponse> callback){
            inst.updateCallback = callback;
            return this;
        }
        /**
         * Set primary PreparedStatement value setter
         * @param ps PreparedStatement callback
         * @return Executor instance (chain)
         */
        public Executor setPrepared(SQLCallback<PreparedStatement> ps){
            inst.valueSetter = ps;
            return this;
        }

        /**
         * Add PreparedStatement value setter. Multiple value setters will enable batched execute
         * @param ps PreparedStatement callback
         * @return Executor instance (chain)
         */
        public Executor addPrepared(SQLCallback<PreparedStatement> ps){
            if(inst.valueSetter == null)
                return setPrepared(ps);
            List<SQLCallback<PreparedStatement>> setters = inst.valueSetters == null ? new LinkedList<>() : inst.valueSetters;
            setters.add(ps);
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
         * @return Future object of task or null if ExecutorService wasn't provided.
         */
        public Future<Void> executeAsync(){
            if(eserv != null)
                return eserv.submit(() -> {execute(); return null;});
            return null;
        }
    }
    private final ConnectionProvider prov;
    private String sql;
    private boolean verbose = true;
    private SQLCallback<SQLResponse> queryCallback;
    private SQLCallback<SQLResponse> updateCallback;
    private SQLCallback<PreparedStatement> valueSetter;
    private List<SQLCallback<PreparedStatement>> valueSetters;
    private Consumer<SQLException> except;
    private SQLAdapter(ConnectionProvider provider){
        this.prov = provider;
    }

    /**
     * Get executor chain.
     * @param provider Connection provider
     * @param task Task to execute on SQLAdapter. See available method references
     * @return Executor instance
     */
    public static Executor executor(ConnectionProvider provider, Consumer<SQLAdapter> task){
        return new SQLAdapter(provider)
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
            query(prov, sql, queryCallback, verbose);
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
            preparedQuery(prov, sql, valueSetter, queryCallback, verbose);
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
            Debug.print("update is batched!");
            boolean batch = valueSetters != null && !valueSetters.isEmpty();
            SQLCallback<PreparedStatement> p = batch ?
                    ps -> {
                        valueSetter.execute(ps);
                        ps.addBatch();
                        for (SQLCallback<PreparedStatement> sc : valueSetters) {
                            sc.execute(ps);
                            ps.addBatch();
                        }
                        return true;
                    } :
                    valueSetter;
            preparedUpdate(prov, sql, valueSetter, updateCallback, batch, verbose);
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
    public static void query(ConnectionProvider provider, String sql, SQLCallback<SQLResponse> callback, boolean verbose) throws SQLException {
        Connection conn = provider.getConnection();
        // formatting shit
        // AFAIK closing statement will close resultset automatically
        try(Statement s = conn.createStatement()){
            callback.execute(new SQLResponse(s.executeQuery(sql), -1, null));
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
    public static void preparedQuery(ConnectionProvider provider, String sql, SQLCallback<PreparedStatement> setter, SQLCallback<SQLResponse> callback, boolean verbose) throws SQLException {
        Connection conn = provider.getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            // set all values
            setter.execute(ps);
            // dispatch query
            callback.execute(new SQLResponse(ps.executeQuery(), -1, null));
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
    public static void update(ConnectionProvider provider, String sql, SQLCallback<SQLResponse> callback, boolean verbose) throws SQLException {
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
    public static void preparedUpdate(ConnectionProvider provider, String sql, SQLCallback<PreparedStatement> setter, SQLCallback<SQLResponse> callback, boolean batch, boolean verbose) throws SQLException {
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
