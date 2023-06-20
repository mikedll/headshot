package com.mikedll.headshot.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.stream.Collectors;
import java.time.Instant;
import java.util.Optional;

import org.javatuples.Pair;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.db.SimpleSql;
import com.mikedll.headshot.db.SqlArg;
import com.mikedll.headshot.controller.Controller;

public class RepositoryRepository {

    private DatabaseConfiguration dbConf;

    private RepositoryRepository() {
    }
    
    public RepositoryRepository(DatabaseConfiguration dbConf) {
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
            }, params.toArray(new SqlArg[0]));

        if(result.getValue1() != null) {
            return Pair.with(null, result.getValue1());
        }
        
        return Pair.with(result.getValue0(), null);
    }

    public Pair<Optional<Repository>, String> forUserAndId(User user, Long id) {
        List<SqlArg> params = new ArrayList<>();
        params.add(new SqlArg(Long.class, user.getId()));
        params.add(new SqlArg(Long.class, id));

        String query = "SELECT * FROM repositories WHERE user_id = ? AND id = ? LIMIT 1";
        Pair<Optional<Repository>, String> result = SimpleSql.executeQuery(dbConf.getDataSource(), query, (rs) -> {
                Repository ret = null;
                if(rs.next()) {
                    ret = new Repository();
                    ret.copyFromRs(rs);
                }
                return Optional.ofNullable(ret);
            }, params.toArray(new SqlArg[0]));

        if(result.getValue1() != null) {
            return Pair.with(null, result.getValue1());
        }

        return Pair.with(result.getValue0(), null);
    }
    
    public String save(User user, List<Repository> input) {
        DataSource dataSource = dbConf.getDataSource();
        String githubIdPlaceholders = String.join(",", input.stream().map(i -> "?").collect(Collectors.toList()));
        String sql = "SELECT github_id FROM repositories WHERE user_id = ? AND github_id in (" + githubIdPlaceholders + ")";

        List<SqlArg> params = new ArrayList<>(input.size() + 1);
        params.add(new SqlArg(Long.class, user.getId()));
        for(int i = 0; i < input.size(); i++) {
            params.add(new SqlArg(Long.class, input.get(i).getGithubId()));
        }
        
        Pair<Set<Long>, String> result = SimpleSql.executeQuery(dataSource, sql, (rs) -> {
                Set<Long> existing = new HashSet<>();
                while(rs.next()) {
                    existing.add(rs.getLong("github_id"));
                }
                return existing;
            }, params.toArray(new SqlArg[0]));

        if(result.getValue1() != null) {
            return result.getValue1();
        }

        String insertSql = "INSERT INTO repositories (user_id, github_id, name, is_private, description, github_created_at)"
            + " VALUES (?, ?, ?, ?, ?, ?) RETURNING id;";
        for(Repository repository : input.stream().filter(i -> !result.getValue0().contains(i.getGithubId())).collect(Collectors.toList())) {
            List<SqlArg> insertParams = new ArrayList<>(6);
            insertParams.add(new SqlArg(Long.class, user.getId()));
            insertParams.add(new SqlArg(Long.class, repository.getGithubId()));
            insertParams.add(new SqlArg(String.class, repository.getName()));
            insertParams.add(new SqlArg(Boolean.class, repository.getIsPrivate()));
            insertParams.add(new SqlArg(String.class, repository.getDescription()));
            insertParams.add(new SqlArg(Instant.class, repository.getCreatedAt()));

            Pair<Long,String> insertResult = SimpleSql.executeQuery(dataSource, insertSql, (rs) -> {
                    rs.next();
                    return rs.getLong("id");
                }, insertParams.toArray(new SqlArg[0]));
            if(insertResult.getValue1() != null) {
                // Todo: rollback transaction
                return insertResult.getValue1();
            }
            repository.setId(insertResult.getValue0());
        }

        return null;
    }
    
}
