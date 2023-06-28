package com.mikedll.headshot.model;

import java.util.Optional;

import org.javatuples.Pair;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.db.SimpleSql;
import com.mikedll.headshot.db.QuietResultSet;
import com.mikedll.headshot.db.SqlArg;

public abstract class RepositoryBase<T> {

    protected DatabaseConfiguration dbConf;
    
    public RepositoryBase(DatabaseConfiguration dbConf) {
        this.dbConf = dbConf;
    }

    abstract protected String getTable();
    
    public Pair<Long,String> count() {
        Pair<Long, String> result = SimpleSql.executeQuery(dbConf, "SELECT COUNT(*) FROM " + getTable(), (rs) -> {
                if(!rs.next()) {
                    return null;
                }
                return rs.getLong("count");
            });

        if(result.getValue1() != null) {
            return Pair.with(null, "Failed to get count: " + result.getValue1());
        }
        return Pair.with(result.getValue0(), null);
    }

    protected Optional<T> rsToEntity(QuietResultSet rs) {
        throw new RuntimeException("sublcass must implement rsToEntity(rs)");
    }

    public Pair<Optional<T>,String> findById(Long id) {
        Pair<Optional<T>, String> result = SimpleSql.executeQuery(dbConf, "SELECT * FROM " + getTable() + " WHERE id = ?", (rs) -> {
                return rsToEntity(rs);
            }, new SqlArg(Long.class, id));

        if(result.getValue1() != null) {
            return Pair.with(null, result.getValue1());
        }

        return Pair.with(result.getValue0(), null);
    }
    
    
}
