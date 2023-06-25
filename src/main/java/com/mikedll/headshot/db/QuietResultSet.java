package com.mikedll.headshot.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.time.Instant;

public class QuietResultSet {
    
    private ResultSet resultSet;
    
    public QuietResultSet(ResultSet resultSet) {
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
    
}
