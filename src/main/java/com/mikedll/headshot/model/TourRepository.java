package com.mikedll.headshot.model;

import java.util.List;
import java.util.Arrays;
import java.time.Instant;

import org.javatuples.Pair;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.db.SimpleSql;

public class TourRepository extends RepositoryBase {
    
    public TourRepository(DatabaseConfiguration dbConf) {
        super(dbConf);
    }

    @Override
    public String getTable() {
        return "tours";
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
            Pair<Long,String> insertResult = SimpleSql.executeQuery(dbConf, "INSERT INTO tours (user_id, created_at, name) VALUES "
                                                                    + "(?,?,?) RETURNING (id)", argTypes, args, (rs) -> {
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
