package com.mikedll.headshot.db;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.sql.DataSource;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.javatuples.Pair;

import com.mikedll.headshot.db.Transaction;
import com.mikedll.headshot.model.User;
import com.mikedll.headshot.DbSuite;
import com.mikedll.headshot.TestSuite;
import com.mikedll.headshot.DbTest;

public class TransactionTests extends DbTest {

    DatabaseConfiguration dbConf;
    
    @BeforeEach
    public void setUp() {
        this.dbConf = TestSuite.getSuite(DbSuite.class).getApp().dbConf;        
    }
    
    @Test
    public void testTxUpdate() {
        Transaction tx = new Transaction(this.dbConf);

        String insertSql = "INSERT INTO users (name, github_id, github_login, url, html_url, repos_url, access_token) VALUES (?,?,?,?,?,?,?)";

        List<Class<?>> argTypes = Arrays.asList(new Class<?>[] {String.class, Long.class, String.class, String.class,
                    String.class, String.class, String.class});
        
        List<Object> args1 = Arrays.asList(new Object[] { "Mike", 10L, "mikelogin", "http://url",
                                                         "http://html_url", "http://repos_url", "accessToken" });

        List<Object> args2 = Arrays.asList(new Object[] { "Sally", 11L, "sallylogin", "http://url",
                                                         "http://html_url", "http://repos_url", "accessToken" });

        tx.add(TransactionStatement.buildUpdateWithArgs(insertSql, argTypes, args1, 1));
        tx.add(TransactionStatement.buildUpdateWithArgs(insertSql, argTypes, args2, 1));
        
        String error = tx.execute();
        Assertions.assertNull(error, "inserts");

        Pair<Long,String> countResult = SimpleSql.executeQuery(this.dbConf, "SELECT COUNT(*) from users", (rs) -> {
                if(!rs.next()) {
                    return null;
                }
                return rs.getLong("count");
            });

        Assertions.assertNull(countResult.getValue1(), "count");
        Assertions.assertEquals(2, countResult.getValue0(), "correct count");

        Pair<User,String> userResult = SimpleSql.executeQuery(this.dbConf, "SELECT * from users where github_id = ?", (rs) -> {
                if(rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setName(rs.getString("name"));
                    return user;
                }
                return null;
            },
            new SqlArg(Long.class, 10L));
        Assertions.assertEquals("Mike", userResult.getValue0().getName());
    }

    @Test
    public void testTxQuery() {
        Transaction tx = new Transaction(this.dbConf);

        String insertSql = "INSERT INTO users (name, github_id, github_login, url, html_url, repos_url, access_token)"
            + " VALUES (?,?,?,?,?,?,?) RETURNING id";

        List<TransactionStatement<Long>> inserts = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        Function<QuietResultSet,Long> rsToResult = (rs) -> {
            if(rs.next()) {
                return rs.getLong("id");
            }
            return null;
        };

        List<Class<?>> argTypes = Arrays.asList(new Class<?>[] {String.class, Long.class, String.class, String.class,
                    String.class, String.class, String.class});
        
        List<Object> args1 = Arrays.asList(new Object[] { "Mike", 10L, "mikelogin", "http://url",
                                                         "http://html_url", "http://repos_url", "accessToken" });
        
        List<Object> args2 = Arrays.asList(new Object[] { "Sally", 11L, "sallylogin", "http://url",
                                                         "http://html_url", "http://repos_url", "accessToken" });

        inserts.add(TransactionStatement.buildWithArgs(insertSql, argTypes, args1, rsToResult));
        inserts.add(TransactionStatement.buildWithArgs(insertSql, argTypes, args2, rsToResult));
        inserts.forEach(s -> tx.add(s));
        String error = tx.execute();
        Assertions.assertNull(error, "inserts");
        inserts.forEach(s -> ids.add(s.getResult()));

        Pair<Long,String> countResult = SimpleSql.executeQuery(this.dbConf, "SELECT COUNT(*) from users", (rs) -> {
                if(!rs.next()) {
                    return null;
                }
                return rs.getLong("count");
            });

        Assertions.assertNull(countResult.getValue1(), "count");
        Assertions.assertEquals(2, countResult.getValue0(), "correct count");

        Pair<String, String> found = SimpleSql.executeQuery(this.dbConf, "SELECT name FROM users WHERE id = ?", (rs) -> {
                if(rs.next()) {
                    return rs.getString("name");
                }
                return null;
            }, new SqlArg(Long.class, ids.get(0)));
        Assertions.assertEquals("Mike", found.getValue0());

        found = SimpleSql.executeQuery(this.dbConf, "SELECT name FROM users WHERE id = ?", (rs) -> {
                if(rs.next()) {
                    return rs.getString("name");
                }
                return null;
            }, new SqlArg(Long.class, ids.get(1)));
        Assertions.assertEquals("Sally", found.getValue0());
    }
    
    @Test
    public void testTxRollback() {
        Transaction tx = new Transaction(this.dbConf);

        String insertSql = "INSERT INTO users (name, github_id, github_login, url, html_url, repos_url, access_token) VALUES (?,?,?,?,?,?,?) " +
            "RETURNING (id)";

        // This is kind of just to give lip service to the fact that you'd need
        // to track your inserts apart from the transaction object, so that you
        // can get the IDs out later.
        List<TransactionStatement<Long>> inserts = new ArrayList<>();
        Function<QuietResultSet,Long> toResult = (rs) -> {
            if(rs.next()) {
                return rs.getLong("id");
            }
            return null;
        };

        List<Class<?>> argTypes = Arrays.asList(new Class<?>[] {String.class, Long.class, String.class, String.class,
                    String.class, String.class, String.class});
        
        List<Object> args1 = Arrays.asList(new Object[] { "Mike", 10L, "mikelogin", "http://url",
                                                         "http://html_url", "http://repos_url", "accessToken" });
        
        List<Object> args2 = Arrays.asList(new Object[] { "Sally", 10L, "sallylogin", "http://url",
                                                         "http://html_url", "http://repos_url", "accessToken" });
        inserts.add(TransactionStatement.buildWithArgs(insertSql, argTypes, args1, toResult));
        inserts.add(TransactionStatement.buildWithArgs(insertSql, argTypes, args2, toResult));

        inserts.forEach(s -> tx.add(s));
        String error = tx.execute();
        Assertions.assertTrue(error.contains("duplicate key value violates unique constraint \"users_github_id\""), "error on inserts");

        Pair<Long,String> countResult = SimpleSql.executeQuery(this.dbConf, "SELECT COUNT(*) from users", (rs) -> {
                if(!rs.next()) {
                    return null;
                }
                return rs.getLong("count");
            });

        Assertions.assertNull(countResult.getValue1(), "count");
        Assertions.assertEquals(0, countResult.getValue0(), "correct count");
    }

    @Test
    public void testInsertWithManyValues() {
        List<Class<?>> argTypes = Arrays.asList(new Class<?>[] {String.class, Long.class, String.class, String.class,
                    String.class, String.class, String.class});
        
        List<List<Object>> data = new ArrayList<>();
        data.add(Arrays.asList(new Object[] { "Mike", 10L, "mikename", "http://url",
                                             "http://html_url", "http://repos_url", "accessToken" }));
        data.add(Arrays.asList(new Object[] { "Sally", 11L, "sallyname", "http://url",
                                             "http://html_url", "http://repos_url", "accessToken" }));
        data.add(Arrays.asList(new Object[] { "Greg", 12L, "gregname", "http://url",
                                             "http://html_url", "http://repos_url", "accessToken" }));
        data.add(Arrays.asList(new Object[] { "Dup Greg", 12L, "gregname", "http://url",
                                             "http://html_url", "http://repos_url", "accessToken" }));

        String valuesSql = String.join(",", Collections.nCopies(data.size(), "(?,?,?,?,?,?,?)").toArray(new String[0]));
        String insertSql = "INSERT INTO users (name, github_id, github_login, url, html_url, repos_url, access_token) VALUES  " +
            valuesSql + " ON CONFLICT (github_id) DO NOTHING RETURNING (id)";
        
        TransactionStatement<List<Long>> statement = TransactionStatement.build(insertSql, argTypes, data, (rs) -> {
                List<Long> ids = new ArrayList<>();
                while(rs.next()) {
                    ids.add(rs.getLong("id"));
                }
                return ids;
            });

        Transaction tx = new Transaction(this.dbConf);
        tx.add(statement);
        String error = tx.execute();
        Assertions.assertNull(error, "bulk insert okay");

        Pair<Long,String> countResult = SimpleSql.executeQuery(this.dbConf, "SELECT COUNT(*) from users", (rs) -> {
                if(!rs.next()) {
                    return null;
                }
                return rs.getLong("count");
            });

        Assertions.assertNull(countResult.getValue1(), "count");
        Assertions.assertEquals(3L, countResult.getValue0(), "correct count");
        statement.getResult().forEach(id -> Assertions.assertNotNull(id));
    }
    
}
