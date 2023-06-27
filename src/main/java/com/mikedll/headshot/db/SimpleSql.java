package com.mikedll.headshot.db;

import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import java.util.function.Function;
import java.sql.Timestamp;
import java.time.Instant;

import org.javatuples.Pair;

public class SimpleSql {

    private static String setVal(PreparedStatement stmt, int idx, Class<?> clazz, Object arg) {
        try {
            if(clazz == Integer.class) {
                stmt.setInt(idx, (Integer)arg);
            } else if(clazz == Long.class) {
                stmt.setLong(idx, (Long)arg);
            } else if(clazz == String.class) {
                stmt.setString(idx, (String)arg);
            } else if(clazz == Instant.class) {
                stmt.setTimestamp(idx, new Timestamp(((Instant)arg).toEpochMilli()));
            } else if(clazz == Boolean.class) {
                stmt.setBoolean(idx, (Boolean)arg);
            } else {
                return "Unhandled sql arg type: " + clazz;
            }
        } catch (SQLException ex) {
            return "SQLException in setVal: " + ex.getMessage();
        }
        return null;
    }
    
    /*
     * Returns error on failure, null on success.
     */
    public static String execute(DatabaseConfiguration dbConf, String sql) {
        try(Connection conn = dbConf.getDataSource().getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String sqlWithCommit = sql + "; COMMIT;";
                dbConf.logger.debug(sqlWithCommit);
                stmt.execute(sqlWithCommit);
            } catch(SQLException ex) {
                return "Failed to execute SQL: " + ex.getMessage();
            }
        } catch(SQLException ex) {
            return "SQL Connection exception: " + ex.getMessage();
        }

        return null;
    }

    /*
     * Returns error on failure, null on success.
     */
    public static String executeUpdate(DatabaseConfiguration dbConf, String sql, SqlArg... sqlArgs) {
        try(Connection conn = dbConf.getDataSource().getConnection()) {
            String sqlWithCommit = sql + "; COMMIT;";
            try (PreparedStatement stmt = conn.prepareStatement(sqlWithCommit)) {
                for(int i = 0; i < sqlArgs.length; i++) {
                    Class<?> clazz = sqlArgs[i].clazz();
                    Object arg = sqlArgs[i].val();
                    String error = setVal(stmt, i+1, clazz, arg);
                    if(error != null) {
                        return error;
                    }
                }
                dbConf.logger.debug(sqlWithCommit);
                stmt.executeUpdate();
            } catch(SQLException ex) {
                return "Failed to execute SQL: " + ex.getMessage();
            }
        } catch(SQLException ex) {
            return "SQL Connection exception: " + ex.getMessage();
        }

        return null;
    }
    

    public static String executeUpdate(DatabaseConfiguration dbConf, String sql, List<Class<?>> argTypes, List<Object> args, int expected) {
        if(argTypes.size() != args.size()) {
            return "argTypes size (" + argTypes.size() + ") does not match args size (" + args.size() + ")";
        }
        try(Connection conn = dbConf.getDataSource().getConnection()) {
            String sqlWithCommit = sql + "; COMMIT;";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for(int i = 0; i < argTypes.size(); i++) {
                    Class<?> clazz = argTypes.get(i);
                    Object arg = args.get(i);
                    String error = setVal(stmt, i+1, clazz, arg);
                    if(error != null) {
                        return error;
                    }
                }
                dbConf.logger.debug(sqlWithCommit);
                int rows = stmt.executeUpdate();
                if(rows != expected) {
                    return "expected to update " + expected + " rows(s) but updated " + rows;
                }
            } catch(SQLException ex) {
                return "Failed to execute SQL: " + ex.getMessage();
            }

            // Commit
            try(Statement stmt = conn.createStatement()) {
                stmt.execute("COMMIT;");
            } catch (SQLException ex) {
                return "Failed to commit: " + ex.getMessage();
            }
        } catch(SQLException ex) {
            return "SQL Connection exception: " + ex.getMessage();
        }

        return null;        
    }

    /*
     * T is the result of applying the given func to only the first ResultSet.
     * 
     * Returns (T, error) where error is null on success, an error string on failure.
     */
    public static <T> Pair<T, String> executeQuery(DatabaseConfiguration dbConf, String sql, Function<QuietResultSet,T> func,
                                                   SqlArg... sqlArgs) {
        try(Connection conn = dbConf.getDataSource().getConnection()) {
            String sqlWithCommit = sql + "; COMMIT;";
            try (PreparedStatement stmt = conn.prepareStatement(sqlWithCommit)) {
                for(int i = 0; i < sqlArgs.length; i++) {
                    Class<?> clazz = sqlArgs[i].clazz();
                    Object arg = sqlArgs[i].val();
                    String error = setVal(stmt, i+1, clazz, arg);
                    if(error != null) {
                        return Pair.with(null, error);
                    }
                }
                dbConf.logger.debug(sqlWithCommit);
                boolean hasResults = stmt.execute();
                if(!hasResults) {
                    return Pair.with(null, "failed to find results");
                }
                ResultSet resultSet = stmt.getResultSet();
                return Pair.with(func.apply(new QuietResultSet(resultSet)), null);
            } catch(SQLException ex) {
                return Pair.with(null, "Failed to execute SQL: " + ex.getMessage());
            }
        } catch(SQLException ex) {
            return Pair.with(null, "SQL Connection exception: " + ex.getMessage());
        }
    }

    /*
     * T comes from applying the given func to only the first ResultSet.
     * 
     * Returns (T, error) where error is null on success, an error string on failure.
     */
    public static <T> Pair<T, String> executeQuery(DatabaseConfiguration dbConf, String sql, List<Class<?>> argTypes, List<Object> args,
                                                   Function<QuietResultSet,T> func) {
        if(argTypes.size() != args.size()) {
            return Pair.with(null, "argTypes size (" + argTypes.size() + ") does not match args size (" + args.size() + ")");
        }
        try(Connection conn = dbConf.getDataSource().getConnection()) {
            String sqlWithCommit = sql + "; COMMIT;";
            try (PreparedStatement stmt = conn.prepareStatement(sqlWithCommit)) {
                for(int i = 0; i < argTypes.size(); i++) {
                    Class<?> clazz = argTypes.get(i);
                    Object arg = args.get(i);
                    String error = setVal(stmt, i+1, clazz, arg);
                    if(error != null) {
                        return Pair.with(null, error);
                    }
                }
                dbConf.logger.debug(sqlWithCommit);
                boolean hasResults = stmt.execute();
                if(!hasResults) {
                    return Pair.with(null, "failed to find results");
                }
                ResultSet resultSet = stmt.getResultSet();
                return Pair.with(func.apply(new QuietResultSet(resultSet)), null);
            } catch(SQLException ex) {
                return Pair.with(null, "Failed to execute SQL: " + ex.getMessage());
            }
        } catch(SQLException ex) {
            return Pair.with(null, "SQL Connection exception: " + ex.getMessage());
        }
    }    
}
