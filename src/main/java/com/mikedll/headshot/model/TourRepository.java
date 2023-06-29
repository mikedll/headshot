package com.mikedll.headshot.model;

import java.time.Instant;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Optional;

import org.javatuples.Pair;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.db.SimpleSql;
import com.mikedll.headshot.db.SqlArg;
import com.mikedll.headshot.db.QuietResultSet;

public class TourRepository extends RepositoryBase<Tour> {
    
    public TourRepository(DatabaseConfiguration dbConf) {
        super(dbConf);
    }

    @Override
    public String getTable() {
        return "tours";
    }

    @Override
    protected Optional<Tour> rsToEntity(QuietResultSet rs) {
        Tour tour = null;
        if(rs.next()) {
            tour = new Tour();
            tour.setId(rs.getLong("id"));
            tour.setUserId(rs.getLong("user_id"));
            tour.setCreatedAt(Instant.ofEpochMilli(rs.getTimestamp("created_at").getTime()));
            tour.setName(rs.getString("name"));
        }
        return Optional.ofNullable(tour);
    }

    public Pair<List<Tour>,String> forUser(User user) {
        return SimpleSql.executeQuery(dbConf, "SELECT * FROM tours WHERE user_id = ?", (rs) -> {
                List<Tour> ret = new ArrayList<>();
                Tour tour = rsToEntity(rs).orElse(null);
                while(tour != null) {
                    ret.add(tour);
                    tour = rsToEntity(rs).orElse(null);
                }
                return ret;
            }, new SqlArg(Long.class, user.getId()));
    }

    public Pair<Optional<Tour>,String> forUserAndId(User user, Long id) {
        return SimpleSql.executeQuery(dbConf, "SELECT * FROM tours WHERE user_id = ? AND id = ?", (rs) -> {
                return rsToEntity(rs);
            }, new SqlArg(Long.class, user.getId()), new SqlArg(Long.class, id));
    }
    
    public String save(Tour tour) {
        if(tour.getId() != null) {
            throw new RuntimeException("tour update not implemented yet");
        } else {
            if(tour.getCreatedAt() == null) {
                tour.setCreatedAt(Instant.now());
            }
            List<Class<?>> argTypes = Arrays.asList(new Class<?>[] { Long.class, Instant.class, String.class });
            List<Object> args = Arrays.asList(new Object[] { tour.getUserId(), tour.getCreatedAt(),
                                                            tour.getName() });
            String insertSql = "INSERT INTO tours (user_id, created_at, name) VALUES "
                + "(?,?,?) RETURNING (id)";
            Pair<Long,String> insertResult = SimpleSql.executeQuery(dbConf, insertSql, argTypes, args, (rs) -> {
                    if(rs.next()) {
                        return rs.getLong("id");
                    }
                    return null;
                });
            if(insertResult.getValue1() != null) {
                return insertResult.getValue1();
            }

            tour.setId(insertResult.getValue0());
            return null;
        }
    }
}
