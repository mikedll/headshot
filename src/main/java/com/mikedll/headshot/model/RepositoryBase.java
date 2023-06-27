package com.mikedll.headshot.model;

import org.javatuples.Pair;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.db.SimpleSql;

public abstract class RepositoryBase {

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
    
}
