package com.mikedll.headshot.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
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

    /*
     * Returns T, error where where error is null on success.
     */
    public static <T> Pair<T,String> executeQuery(DataSource dataSource, String sql, Function<SimpleSql,T> func) {
        try(Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet resultSet = stmt.executeQuery(sql);
                return Pair.with(func.apply(new SimpleSql(resultSet)), null);
            } catch(SQLException ex) {
                return Pair.with(null, "Failed to execute SQL: " + ex.getMessage());
            }
        } catch(SQLException ex) {
            return Pair.with(null, "SQL Connection exception: " + ex.getMessage());
        }
    }

    /*
     * Returns error on failure, null on success.
     */
    public static String execute(DataSource dataSource, String sql) {
        try(Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch(SQLException ex) {
                return "Failed to execute SQL: " + ex.getMessage();
            }
        } catch(SQLException ex) {
            return "SQL Connection exception: " + ex.getMessage();
        }

        return null;
    }

    /*
     * Only works with string args :/
     * 
     * Returns error on failure, null on success.
     */
    public static String executeUpdate(DataSource dataSource, String sql, String... sqlArgs) {
        try(Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for(int i = 0; i < sqlArgs.length; i++) {
                    stmt.setString(i+1, sqlArgs[i]);
                }
                stmt.executeUpdate();
            } catch(SQLException ex) {
                return "Failed to execute SQL: " + ex.getMessage();
            }
        } catch(SQLException ex) {
            return "SQL Connection exception: " + ex.getMessage();
        }

        return null;
    }
}
