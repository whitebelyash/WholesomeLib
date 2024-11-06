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
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        long s = System.currentTimeMillis();
        ConnectionProvider provider = new H2ConnProvider(config);
        SQLAdapterWIP.executor(provider, SQLAdapterWIP::update)
                .sql("CREATE TABLE IF NOT EXISTS test(id INT, name varchar(12));")
                .updateCallback(i -> {System.out.printf("aff rows: %d\n", i.updateResult()); return true;})
                .exceptionally(e -> {throw new RuntimeException(e);})
                .execute();
        SQLAdapterWIP.executor(provider, SQLAdapterWIP::preparedUpdate)
                .sql("INSERT INTO test VALUES (?, ?);")
                .setPrepared(ps -> {
                    ps.setInt(1, 1);
                    ps.setString(2, "bhai");
                    return true;
                })
                .updateCallback(i -> {System.out.printf("aff rows upd: %d\n", i.updateResult()); return true;})
                .exceptionally(e -> {throw new RuntimeException(e);})
                .execute();
        SQLAdapterWIP.executor(provider, SQLAdapterWIP::query)
                .sql("SELECT * FROM test")
                .queryCallback(rs -> {
                    // do something
                    return true;
                })
                .execute();
        System.out.printf("Completed in %d\n", System.currentTimeMillis() -s);
    }
}

```