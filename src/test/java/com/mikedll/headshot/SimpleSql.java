package com.mikedll.headshot;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import java.util.function.Function;

import org.javatuples.Pair;

public class SimpleSql {

    private ResultSet resultSet;
    
    public SimpleSql(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public boolean next() {
        try {
            return this.resultSet.next();
        } catch (SQLException ex) {
            throw new RuntimeException("next() failed", ex);
        }
    }

    public String getString(String col) {
        try {
            return this.resultSet.getString(col);
        } catch (SQLException ex) {
            throw new RuntimeException("getString() failed", ex);
        }
    }

    public Long getLong(String col) {
        try {
            return this.resultSet.getLong(col);
        } catch (SQLException ex) {
            throw new RuntimeException("getLong() failed", ex);
        }
    }
    
    public static <T> Pair<T,String> executeQuery(DataSource dataSource, String sql, Function<SimpleSql,T> func) {
        try(Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet resultSet = stmt.executeQuery(sql);
                return Pair.with(func.apply(new SimpleSql(resultSet)), null);
            } catch(SQLException ex) {
                return Pair.with(null, "Failed to execute sql: " + ex.getMessage());
            }
        } catch(SQLException ex) {
            return Pair.with(null, "SQL Connection exception: " + ex.getMessage());
        }
    }
    
}
