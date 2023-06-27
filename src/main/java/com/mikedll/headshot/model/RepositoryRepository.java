package com.mikedll.headshot.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.time.Instant;
import java.util.Optional;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.sql.ResultSetMetaData;
import java.util.stream.IntStream;

import org.javatuples.Pair;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.db.SimpleSql;
import com.mikedll.headshot.db.QuietResultSet;
import com.mikedll.headshot.db.SqlArg;
import com.mikedll.headshot.db.Transaction;
import com.mikedll.headshot.db.TransactionStatement;
import com.mikedll.headshot.controller.Controller;

public class RepositoryRepository extends RepositoryBase {

    public RepositoryRepository(DatabaseConfiguration dbConf) {
        super(dbConf);
    }

    @Override
    public String getTable() {
        return "repositories";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Repository> rsToEntity(QuietResultSet rs) {
        Repository repository = null;
        if(rs.next()) {
            repository = new Repository();
            repository.copyFromRs(rs);
        }
        return Optional.ofNullable(repository);
    }
    
    public Pair<List<Repository>, String> forUser(User user) {
        List<SqlArg> params = new ArrayList<>();
        params.add(new SqlArg(Long.class, user.getId()));
        Pair<List<Repository>, String> result = SimpleSql.executeQuery(dbConf, "SELECT * FROM repositories WHERE user_id = ?", (rs) -> {
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
        Pair<Optional<Repository>, String> result = SimpleSql.executeQuery(dbConf, query, (rs) -> {
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
        Repository alreadyExists = input.stream().filter(r -> r.getId() != null).findAny().orElse(null);
        if(alreadyExists != null) {
            return "can't save Repository that has id (found id " + alreadyExists.getId() + ")";
        }        
        
        String githubIdPlaceholders = String.join(",", input.stream().map(i -> "?").collect(Collectors.toList()));
        String sql = "SELECT github_id FROM repositories WHERE user_id = ? AND github_id in (" + githubIdPlaceholders + ")";

        List<SqlArg> params = new ArrayList<>(input.size() + 1);
        params.add(new SqlArg(Long.class, user.getId()));
        for(int i = 0; i < input.size(); i++) {
            params.add(new SqlArg(Long.class, input.get(i).getGithubId()));
        }
        
        Pair<Set<Long>, String> result = SimpleSql.executeQuery(dbConf, sql, (rs) -> {
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
        Transaction tx = new Transaction(dbConf);

        List<TransactionStatement<Long>> inserts = new ArrayList<>();
        List<Class<?>> argTypes = Arrays.asList(new Class<?>[] { Long.class, Long.class, String.class,
                    Boolean.class, String.class, Instant.class });
        List<Repository> toInsert = input.stream().filter(i -> !result.getValue0().contains(i.getGithubId()))
            .collect(Collectors.toList());
        for(Repository repository : toInsert) {
            repository.setUserId(user.getId());
            List<Object> args = Arrays.asList(new Object[] { repository.getUserId(), repository.getGithubId(),
                                                            repository.getName(),
                                                            repository.getIsPrivate(), repository.getDescription(),
                                                            repository.getGithubCreatedAt() });

            inserts.add(TransactionStatement.buildWithArgs(insertSql, argTypes, args, (rs) -> {
                        if(rs.next()) {
                            return rs.getLong("id");
                        }
                        return null;
                    }));
        }

        inserts.forEach(s -> tx.add(s));
        String error = tx.execute();
        if(error != null) {
            return error;
        }

        IntStream.range(0, inserts.size()).forEach(idx -> {
                toInsert.get(0).setId(inserts.get(idx).getResult());
            });

        return null;
    }
    
}
