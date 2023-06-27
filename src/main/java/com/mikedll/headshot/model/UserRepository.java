package com.mikedll.headshot.model;

import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import org.javatuples.Pair;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.db.SimpleSql;
import com.mikedll.headshot.db.QuietResultSet;
import com.mikedll.headshot.db.SqlArg;

public class UserRepository extends RepositoryBase {
    
    public UserRepository(DatabaseConfiguration dbConf) {
        super(dbConf);
    }

    @Override
    public String getTable() {
        return "users";
    }
    
    public Pair<Long,String> count() {
        Pair<Long,String> countResult = SimpleSql.executeQuery(dbConf, "SELECT COUNT(*) FROM users", (rs) -> {
                if(rs.next()) {
                    return rs.getLong("count");
                }
                return null;
            });
        if(countResult.getValue1() != null) {
            return Pair.with(null, countResult.getValue1());
        }
        return Pair.with(countResult.getValue0(), null);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<User> rsToEntity(QuietResultSet rs) {
        User user = null;
        if(rs.next()) {
            user = new User();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setGithubId(rs.getLong("github_id"));
            user.setGithubLogin(rs.getString("github_login"));
            user.setUrl(rs.getString("url"));
            user.setHtmlUrl(rs.getString("html_url"));
            user.setReposUrl(rs.getString("repos_url"));
            user.setAccessToken(rs.getString("access_token"));
        }
        return Optional.ofNullable(user);
    }
    
    public Pair<Optional<User>,String> findByGithubId(Long githubId) {
        Pair<Optional<User>, String> result = SimpleSql.executeQuery(dbConf, "SELECT * FROM users WHERE github_id = ?", (rs) -> {
                return rsToEntity(rs);
            }, new SqlArg(Long.class, githubId));

        if(result.getValue1() != null) {
            return Pair.with(null, result.getValue1());
        }

        return Pair.with(result.getValue0(), null);
    }

    public String save(User user) {        
        if(user.getId() != null) {
            List<Class<?>> argTypes = Arrays.asList(new Class<?>[] { String.class, Long.class, String.class,
                        String.class, String.class, String.class, String.class, Long.class });
            
            String updateSql = "UPDATE users SET name = ?, github_id = ?, github_login = ?, url = ?, " +
                "html_url = ?, repos_url = ?, access_token = ? WHERE id = ?;";
            List<Object> args = Arrays.asList(new Object[] { user.getName(), user.getGithubId(), user.getGithubLogin(),
                                                            user.getUrl(),
                                                            user.getHtmlUrl(), user.getReposUrl(), user.getAccessToken(),
                                                            user.getId() });
            return SimpleSql.executeUpdate(dbConf, updateSql, argTypes, args, 1);
        } else {
            List<Class<?>> argTypes = Arrays.asList(new Class<?>[] { String.class, Long.class, String.class, String.class,
                        String.class, String.class, String.class });
            String insertSql = "INSERT INTO users (name, github_id, github_login, url, html_url, repos_url, access_token) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING (id);";
            List<Object> args = Arrays.asList(new Object[] { user.getName(), user.getGithubId(), user.getGithubLogin(),
                                                            user.getUrl(),
                                                            user.getHtmlUrl(), user.getReposUrl(), user.getAccessToken() });

            Pair<Long,String> insertResult = SimpleSql.executeQuery(dbConf, insertSql, argTypes, args, (rs) -> {
                    if(rs.next()) {
                        return rs.getLong("id");
                    }
                    return null;
                });
            if(insertResult.getValue1() != null) {
                return insertResult.getValue1();
            }
            user.setId(insertResult.getValue0());
            return null;
        }
    }

}
