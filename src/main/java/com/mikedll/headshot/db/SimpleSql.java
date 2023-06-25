package com.mikedll.headshot.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.sql.ResultSetMetaData;
import java.time.Instant;
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

    public ResultSetMetaData getMetaData() {
        try {
            return this.resultSet.getMetaData();
        } catch (SQLException ex) {
            throw new RuntimeException("getMetaData() failed", ex);
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

    public Long getLong(int col) {
        try {
            return this.resultSet.getLong(col);
        } catch (SQLException ex) {
            throw new RuntimeException("getLong() failed", ex);
        }
    }
    
    public Boolean getBoolean(String col) {
        try {
            return this.resultSet.getBoolean(col);
        } catch (SQLException ex) {
            throw new RuntimeException("getBoolean() failed", ex);
        }
    }
    
    public Timestamp getTimestamp(String col) {
        try {
            return this.resultSet.getTimestamp(col);
        } catch (SQLException ex) {
            throw new RuntimeException("getTimestamp() failed", ex);
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
     * Returns error on failure, null on success.
     */
    public static String executeUpdate(DataSource dataSource, String sql, SqlArg... sqlArgs) {
        try(Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for(int i = 0; i < sqlArgs.length; i++) {
                    Class<?> clazz = sqlArgs[i].clazz();
                    Object arg = sqlArgs[i].val();
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
        } catch(SQLException ex) {
            return "SQL Connection exception: " + ex.getMessage();
        }

        return null;
    }

    /*
     * Returns (T, error) where error is null on success, an error string on failure.
     */
    public static <T> Pair<T, String> executeQuery(DataSource dataSource, String sql, Function<SimpleSql,T> func,
                                                   SqlArg... sqlArgs) {
        try(Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for(int i = 0; i < sqlArgs.length; i++) {
                    Class<?> clazz = sqlArgs[i].clazz();
                    Object arg = sqlArgs[i].val();
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
                        return Pair.with(null, "Unhandled sql arg type: " + clazz);
                    }
                }
                System.out.println(sql);
                ResultSet resultSet = stmt.executeQuery();
                return Pair.with(func.apply(new SimpleSql(resultSet)), null);
            } catch(SQLException ex) {
                return Pair.with(null, "Failed to execute SQL: " + ex.getMessage());
            }
        } catch(SQLException ex) {
            return Pair.with(null, "SQL Connection exception: " + ex.getMessage());
        }
    }
}
