package com.mikedll.headshot.db;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class TransactionStatement {

    private String sql;

    private SqlArg[] args;
    
    public TransactionStatement(String sql, SqlArg[] args) {
        this.sql = sql;
        this.args = args;
    }

    /*
     * Returns null on success, error on error.
     */
    public String execute(Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for(int i = 0; i < args.length; i++) {
                Class<?> clazz = args[i].clazz();
                Object arg = args[i].val();
                if(clazz == Integer.class) {
                    stmt.setInt(i+1, (Integer)arg);
                } else if(clazz == Long.class) {
                    stmt.setLong(i+1, (Long)arg);
                } else if(clazz == String.class) {
                    stmt.setString(i+1, (String)arg);
                } else if(clazz == Instant.class) {
                    stmt.setTimestamp(i+1, new Timestamp(((Instant)arg).toEpochMilli()));
                } else if(clazz == Boolean.class) {
                    stmt.setBoolean(i+1, (Boolean)arg);
                } else {
                    return "Unhandled sql arg type: " + clazz;
                }
            }
            System.out.println(sql);
            stmt.executeUpdate();
        } catch(SQLException ex) {
            return "Failed to execute SQL: " + ex.getMessage();
        }

        return null;
    }

    public static TransactionStatement build(String sql, SqlArg...args) {
        return new TransactionStatement(sql, args);
    }
}
