package com.mikedll.headshot.db;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class TransactionStatement<T> {
    
    private StatementType type;

    private String sql;

    private List<Class<?>> argTypes;

    private List<List<Object>> argsList;
    
    private Function<QuietResultSet,T> rsToResult;

    private Integer expectedRows;

    private T result;
    
    public TransactionStatement(StatementType type, String sql, List<Class<?>> argTypes, List<List<Object>> argsList, Function<QuietResultSet,T> rsToResult, Integer expectedRows) {
        if(argsList.size() == 0) {
            throw new IllegalArgumentException("can't provide args of size 0");
        }
        argsList.forEach(args -> {
                if(args.size() != argTypes.size()) {
                    throw new IllegalArgumentException("can't give an args of size " + args.size() + " with arg types of size + " + argTypes.size());
                }
            });
        if(expectedRows != null && type != StatementType.UPDATE) {
            throw new IllegalArgumentException("Given expectedRows with non Update statement type");
        }
        this.type = type;
        this.sql = sql;
        this.argTypes = argTypes;
        this.argsList = argsList;
        this.rsToResult = rsToResult;
        this.expectedRows = expectedRows;
    }

    /*
     * Returns null on success, error on error.
     */
    public String execute(Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for(int i = 0; i < argsList.size(); i++) {
                int offset = i * argTypes.size();
                for(int j = 0; j < argTypes.size(); j++) {
                    Class<?> clazz = argTypes.get(j);
                    Object arg = argsList.get(i).get(j);
                    if(clazz == Integer.class) {
                        stmt.setInt(offset+j+1, (Integer)arg);
                    } else if(clazz == Long.class) {
                        stmt.setLong(offset+j+1, (Long)arg);
                    } else if(clazz == String.class) {
                        stmt.setString(offset+j+1, (String)arg);
                    } else if(clazz == Instant.class) {
                        stmt.setTimestamp(offset+j+1, new Timestamp(((Instant)arg).toEpochMilli()));
                    } else if(clazz == Boolean.class) {
                        stmt.setBoolean(offset+j+1, (Boolean)arg);
                    } else {
                        return "Unhandled sql arg type: " + clazz;
                    }
                }
            }
            System.out.println(sql);
            if(this.type == StatementType.UPDATE) {
                int rows = stmt.executeUpdate();
                if(rows != this.expectedRows) {
                    return "Expected " + this.expectedRows + " updated row(s) but updated " + rows;
                }
            } else if(this.type == StatementType.QUERY) {
                this.result = this.rsToResult.apply(new QuietResultSet(stmt.executeQuery()));
            } else if(this.type == StatementType.EXECUTE) {
                stmt.execute();
            }
        } catch(SQLException ex) {
            return "Failed to execute SQL: " + ex.getMessage();
        }

        return null;
    }

    public T getResult() {
        return this.result;
    }

    public static TransactionStatement buildUpdate(String sql, List<Class<?>> argTypes, List<List<Object>> argsList, Integer expectedRows) {
        return new TransactionStatement<String>(StatementType.UPDATE, sql, argTypes, argsList, null, expectedRows);
    }

    public static TransactionStatement buildUpdateWithArgs(String sql, List<Class<?>> argTypes, List<Object> args, Integer expectedRows) {
        List<List<Object>> argsList = new ArrayList<>(1);
        argsList.add(args);
        return new TransactionStatement<String>(StatementType.UPDATE, sql, argTypes, argsList, null, expectedRows);
    }
    
    public static TransactionStatement build(String sql, List<Class<?>> argTypes, List<List<Object>> argsList) {
        return new TransactionStatement<String>(StatementType.UPDATE, sql, argTypes, argsList, null, null);
    }
    
    public static <T> TransactionStatement<T> build(String sql, List<Class<?>> argTypes, List<List<Object>> argsList, Function<QuietResultSet,T> rsToResult) {
        return new TransactionStatement<T>(StatementType.QUERY, sql, argTypes, argsList, rsToResult, null);
    }

    public static <T> TransactionStatement<T> buildWithArgs(String sql, List<Class<?>> argTypes, List<Object> args, Function<QuietResultSet,T> rsToResult) {
        List<List<Object>> argsList = new ArrayList<>(1);
        argsList.add(args);
        return new TransactionStatement<T>(StatementType.QUERY, sql, argTypes, argsList, rsToResult, null);
    }
    
    public static TransactionStatement buildExecute(String sql, List<Class<?>> argTypes, List<List<Object>> argsList) {
        return new TransactionStatement<String>(StatementType.EXECUTE, sql, argTypes, argsList, null, null);
    }
}
