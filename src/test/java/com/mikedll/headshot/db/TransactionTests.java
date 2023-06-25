package com.mikedll.headshot.db;

import java.util.List;
import java.util.ArrayList;
import javax.sql.DataSource;

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
    public void testTx() {
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
        tx.add(TransactionStatement.build(insertSql, insertParams.toArray(new SqlArg[0])));
        
        insertParams = new ArrayList<>();
        insertParams.add(new SqlArg(String.class, "Sally"));
        insertParams.add(new SqlArg(Long.class, 11L));
        insertParams.add(new SqlArg(String.class, "stylishlogin2"));
        insertParams.add(new SqlArg(String.class, "http://url"));
        insertParams.add(new SqlArg(String.class, "http://html_url"));
        insertParams.add(new SqlArg(String.class, "http://repos_url"));
        insertParams.add(new SqlArg(String.class, "accessToken"));        
        tx.add(TransactionStatement.build(insertSql, insertParams.toArray(new SqlArg[0])));
        
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
    public void testTxRollback() {
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
        tx.add(TransactionStatement.build(insertSql, insertParams.toArray(new SqlArg[0])));
        
        insertParams = new ArrayList<>();
        insertParams.add(new SqlArg(String.class, "Sally"));
        insertParams.add(new SqlArg(Long.class, 10L));
        insertParams.add(new SqlArg(String.class, "stylishlogin2"));
        insertParams.add(new SqlArg(String.class, "http://url"));
        insertParams.add(new SqlArg(String.class, "http://html_url"));
        insertParams.add(new SqlArg(String.class, "http://repos_url"));
        insertParams.add(new SqlArg(String.class, "accessToken"));        
        tx.add(TransactionStatement.build(insertSql, insertParams.toArray(new SqlArg[0])));

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
