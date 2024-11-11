# WholesomeLib

Description will come eventually...

## Install
```bash
git clone https://github.com/whitebelyash/WholesomeLib
cd WholesomeLib
mvn install
```
This should be enough...

## Examples
### SQLAdapter (WIP)

```java
public class Main {
    private static final ConnectionConfig config = new ConnectionConfig("db", ".", null, null);
    private static final Logger l = Logger.getLogger("Example");

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        long s = System.currentTimeMillis();
        ConnectionProvider provider = new H2Provider(config);
        SQLAdapter.executor(provider, SQLAdapter::update)
                .sql("CREATE TABLE IF NOT EXISTS test(id INT, name varchar(12));")
                .updateCallback(i -> { l.info("Affected rows " + i.updateResult()); return null; })
                .exceptionally(e -> { throw new RuntimeException(e); })
                .execute();
        SQLAdapter.executor(provider, SQLAdapter::preparedUpdate)
                .sql("INSERT INTO test VALUES (?, ?);")
                .setPrepared(ps -> { ps.setInt(1, 1); ps.setString(2, "bhai"); return null; })
                .updateCallback(i -> { l.info("aff rows upd: %d\n", i.updateResult()); return null;})
                .exceptionally(e -> { throw new RuntimeException(e); })
                .execute();
        String ret = SQLAdapter.executor(String.class, provider, SQLAdapter::query)
                .sql("SELECT * FROM test;")
                .queryCallback(resp -> {
                    if(resp.resultSet().next())
                        return resp.resultSet().getString("name");
                    return null;
                })
                .execute();
        SQLAdapter.update(provider, "DROP TABLE test;", resp -> null,  true);
        // Completed in n ms, val: bhai
        System.out.printf("Completed in %d, val: %s\n", System.currentTimeMillis() - s, ret);
    }
}

```