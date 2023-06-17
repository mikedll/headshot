package com.mikedll.headshot.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.stream.Collectors;
import java.time.Instant;

import org.javatuples.Pair;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.db.SimpleSql;
import com.mikedll.headshot.db.SqlArg;
import com.mikedll.headshot.controller.Controller;

public class RepositoryService {

    private DatabaseConfiguration dbConf;

    private RepositoryService() {
    }
    
    public RepositoryService(Controller controller, DatabaseConfiguration dbConf) {
        if(!controller.canAccessDb()) {
            throw new RuntimeException("Controller canAccessDb() returned false in RepositoriesService");
        }

        this.dbConf = dbConf;
    }

    public Pair<List<Repository>, String> forUser(User user) {
        List<SqlArg> params = new ArrayList<>();
        params.add(new SqlArg(Long.class, user.getId()));
        Pair<List<Repository>, String> result = SimpleSql.executeQuery(dbConf.getDataSource(), "SELECT * FROM repositories WHERE user_id = ?", (rs) -> {
                List<Repository> ret = new ArrayList<>();
                while(rs.next()) {
                    Repository toAdd = new Repository();
                    toAdd.copyFromRs(rs);
                    ret.add(toAdd);
                }
                return ret;
            }, (SqlArg[])params.toArray());

        if(result.getValue1() != null) {
            return Pair.with(null, result.getValue1());
        }
        
        return Pair.with(result.getValue0(), null);
    }
    
    public String save(User user, List<Repository> input) {
        DataSource dataSource = dbConf.getDataSource();
        String sql = "SELECT github_id FROM repositories WHERE user_id = ? AND github_id in (?)";

        List<SqlArg> params = new ArrayList<>(input.size() + 1);
        params.add(new SqlArg(Long.class, user.getId()));
        for(int i = 0; i < input.size(); i++) {
            params.add(new SqlArg(String.class, input.get(i)));
        }
        
        Pair<Set<Long>, String> result = SimpleSql.executeQuery(dataSource, sql, (rs) -> {
                Set<Long> existing = new HashSet<>();
                while(rs.next()) {
                    existing.add(rs.getLong("github_id"));
                }
                return existing;
            }, (SqlArg[])params.toArray());

        if(result.getValue1() != null) {
            return result.getValue1();
        }

        String insertSql = "INSERT INTO repositories (user_id, github_id, name, is_private, description, created_at) VALUES (?, ?, ?, ?, ?, ?);";
        for(Repository repository : input.stream().filter(i -> !result.getValue0().contains(i)).collect(Collectors.toList())) {
            List<SqlArg> insertParams = new ArrayList<>(6);
            insertParams.add(new SqlArg(Long.class, user.getId()));
            insertParams.add(new SqlArg(Long.class, repository.getId()));
            insertParams.add(new SqlArg(String.class, repository.getName()));
            insertParams.add(new SqlArg(Boolean.class, repository.getIsPrivate()));
            insertParams.add(new SqlArg(String.class, repository.getDescription()));
            insertParams.add(new SqlArg(Instant.class, repository.getCreatedAt()));

            String error = SimpleSql.executeUpdate(dataSource, insertSql, (SqlArg[])insertParams.toArray());
            if(error != null) {
                // Todo: rollback transaction
                return error;
            }
        }

        return null;
    }
    
}
