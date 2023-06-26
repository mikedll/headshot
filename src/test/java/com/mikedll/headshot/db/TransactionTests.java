package com.mikedll.headshot.db;

import java.util.List;
import java.util.ArrayList;
import javax.sql.DataSource;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.javatuples.Pair;

import com.mikedll.headshot.db.Transaction;
import com.mikedll.headshot.DbSuite;
import com.mikedll.headshot.TestSuite;
import com.mikedll.headshot.DbTest;

public class TransactionTests extends DbTest {

    DataSource dataSource;
    
    @BeforeEach
    public void setUp() {
        this.dataSource = TestSuite.getSuite(DbSuite.class).getApp().dbConf.getDataSource();        
    }
    
    @Test
    public void testTxUpdate() {
        Transaction tx = new Transaction(this.dataSource);

        String insertSql = "INSERT INTO users (name, github_id, github_login, url, html_url, repos_url, access_token) VALUES (?,?,?,?,?,?,?)";

        List<SqlArg> insertParams = new ArrayList<>();
        insertParams.add(new SqlArg(String.class, "Mike"));
        insertParams.add(new SqlArg(Long.class, 10L));
        insertParams.add(new SqlArg(String.class, "stylishlogin"));
        insertParams.add(new SqlArg(String.class, "http://url"));
        insertParams.add(new SqlArg(String.class, "http://html_url"));
        insertParams.add(new SqlArg(String.class, "http://repos_url"));
        insertParams.add(new SqlArg(String.class, "accessToken"));        
        tx.add(TransactionStatement.buildUpdate(insertSql, insertParams, 1));
        
        insertParams = new ArrayList<>();
        insertParams.add(new SqlArg(String.class, "Sally"));
        insertParams.add(new SqlArg(Long.class, 11L));
        insertParams.add(new SqlArg(String.class, "stylishlogin2"));
        insertParams.add(new SqlArg(String.class, "http://url"));
        insertParams.add(new SqlArg(String.class, "http://html_url"));
        insertParams.add(new SqlArg(String.class, "http://repos_url"));
        insertParams.add(new SqlArg(String.class, "accessToken"));        
        tx.add(TransactionStatement.buildUpdate(insertSql, insertParams, 1));
        
        String error = tx.execute();
        Assertions.assertNull(error, "inserts");

        Pair<Long,String> countResult = SimpleSql.executeQuery(this.dataSource, "SELECT COUNT(*) from users", (rs) -> {
                if(!rs.next()) {
                    return null;
                }
                return rs.getLong("count");
            });

        Assertions.assertNull(countResult.getValue1(), "count");
        Assertions.assertEquals(2, countResult.getValue0(), "correct count");
    }

    @Test
    public void testTxQuery() {
        Transaction tx = new Transaction(this.dataSource);

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
        
        List<SqlArg> insertParams = new ArrayList<>();
        insertParams.add(new SqlArg(String.class, "Mike"));
        insertParams.add(new SqlArg(Long.class, 10L));
        insertParams.add(new SqlArg(String.class, "stylishlogin"));
        insertParams.add(new SqlArg(String.class, "http://url"));
        insertParams.add(new SqlArg(String.class, "http://html_url"));
        insertParams.add(new SqlArg(String.class, "http://repos_url"));
        insertParams.add(new SqlArg(String.class, "accessToken"));        
        inserts.add(TransactionStatement.build(insertSql, insertParams, rsToResult));

        insertParams = new ArrayList<>();
        insertParams.add(new SqlArg(String.class, "Sally"));
        insertParams.add(new SqlArg(Long.class, 11L));
        insertParams.add(new SqlArg(String.class, "stylishlogin2"));
        insertParams.add(new SqlArg(String.class, "http://url"));
        insertParams.add(new SqlArg(String.class, "http://html_url"));
        insertParams.add(new SqlArg(String.class, "http://repos_url"));
        insertParams.add(new SqlArg(String.class, "accessToken"));        
        inserts.add(TransactionStatement.build(insertSql, insertParams, rsToResult));

        inserts.forEach(s -> tx.add(s));
        String error = tx.execute();
        Assertions.assertNull(error, "inserts");
        inserts.forEach(s -> ids.add(s.getResult()));

        Pair<Long,String> countResult = SimpleSql.executeQuery(this.dataSource, "SELECT COUNT(*) from users", (rs) -> {
                if(!rs.next()) {
                    return null;
                }
                return rs.getLong("count");
            });

        Assertions.assertNull(countResult.getValue1(), "count");
        Assertions.assertEquals(2, countResult.getValue0(), "correct count");

        Pair<String, String> found = SimpleSql.executeQuery(this.dataSource, "SELECT name FROM users WHERE id = ?", (rs) -> {
                if(rs.next()) {
                    return rs.getString("name");
                }
                return null;
            }, new SqlArg(Long.class, ids.get(0)));
        Assertions.assertEquals("Mike", found.getValue0());

        found = SimpleSql.executeQuery(this.dataSource, "SELECT name FROM users WHERE id = ?", (rs) -> {
                if(rs.next()) {
                    return rs.getString("name");
                }
                return null;
            }, new SqlArg(Long.class, ids.get(1)));
        Assertions.assertEquals("Sally", found.getValue0());
    }
    
    @Test
    public void testTxRollback() {
        Transaction tx = new Transaction(this.dataSource);

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
        
        List<SqlArg> insertParams = new ArrayList<>();
        insertParams.add(new SqlArg(String.class, "Mike"));
        insertParams.add(new SqlArg(Long.class, 10L));
        insertParams.add(new SqlArg(String.class, "stylishlogin"));
        insertParams.add(new SqlArg(String.class, "http://url"));
        insertParams.add(new SqlArg(String.class, "http://html_url"));
        insertParams.add(new SqlArg(String.class, "http://repos_url"));
        insertParams.add(new SqlArg(String.class, "accessToken"));        
        inserts.add(TransactionStatement.build(insertSql, insertParams, toResult));
        
        insertParams = new ArrayList<>();
        insertParams.add(new SqlArg(String.class, "Sally"));
        insertParams.add(new SqlArg(Long.class, 10L));
        insertParams.add(new SqlArg(String.class, "stylishlogin2"));
        insertParams.add(new SqlArg(String.class, "http://url"));
        insertParams.add(new SqlArg(String.class, "http://html_url"));
        insertParams.add(new SqlArg(String.class, "http://repos_url"));
        insertParams.add(new SqlArg(String.class, "accessToken"));        
        inserts.add(TransactionStatement.build(insertSql, insertParams, toResult));

        inserts.forEach(s -> tx.add(s));
        String error = tx.execute();
        Assertions.assertTrue(error.contains("duplicate key value violates unique constraint \"users_github_id\""), "error on inserts");

        Pair<Long,String> countResult = SimpleSql.executeQuery(this.dataSource, "SELECT COUNT(*) from users", (rs) -> {
                if(!rs.next()) {
                    return null;
                }
                return rs.getLong("count");
            });

        Assertions.assertNull(countResult.getValue1(), "count");
        Assertions.assertEquals(0, countResult.getValue0(), "correct count");
    }
    
}
