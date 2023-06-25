package com.mikedll.headshot.db;

import java.util.List;
import java.util.function.Consumer;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class TransactionStatement {
    
    private StatementType type;

    private String sql;

    private List<SqlArg> args;

    private Consumer<QuietResultSet> rsConsumer;
    
    public TransactionStatement(StatementType type, String sql, List<SqlArg> args, Consumer<QuietResultSet> rsConsumer) {
        this.type = type;
        this.sql = sql;
        this.args = args;
        this.rsConsumer = rsConsumer;
    }

    /*
     * Returns null on success, error on error.
     */
    public String execute(Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for(int i = 0; i < args.size(); i++) {
                Class<?> clazz = args.get(i).clazz();
                Object arg = args.get(i).val();
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
            if(this.type == StatementType.UPDATE) {
                stmt.executeUpdate();
            } else if(this.type == StatementType.QUERY) {
                this.rsConsumer.accept(new QuietResultSet(stmt.executeQuery()));
            } else if(this.type == StatementType.EXECUTE) {
                stmt.execute();
            }
        } catch(SQLException ex) {
            return "Failed to execute SQL: " + ex.getMessage();
        }

        return null;
    }

    public static TransactionStatement build(String sql, List<SqlArg> args) {
        return new TransactionStatement(StatementType.UPDATE, sql, args, null);
    }

    public static <T> TransactionStatement build(String sql, List<SqlArg> args, Consumer<QuietResultSet> rsConsumer) {
        return new TransactionStatement(StatementType.QUERY, sql, args, rsConsumer);
    }

    public static TransactionStatement buildExecute(String sql, List<SqlArg> args) {
        return new TransactionStatement(StatementType.EXECUTE, sql, args, null);
    }
}
