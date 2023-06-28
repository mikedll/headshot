package com.mikedll.headshot.model;

import java.util.Arrays;
import java.util.List;
import java.time.Instant;
import java.util.Optional;

import org.javatuples.Pair;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.db.SimpleSql;
import com.mikedll.headshot.db.QuietResultSet;

public class PageRepository extends RepositoryBase<Page> {

    public PageRepository(DatabaseConfiguration dbConf) {
        super(dbConf);
    }

    public String getTable() {
        return "pages";
    }

    @Override
    public Optional<Page> rsToEntity(QuietResultSet rs) {
        Page page = null;
        if(rs.next()) {
            page = new Page();
            page.setId(rs.getLong("id"));
            page.setTourId(rs.getLong("tour_id"));
            page.setCreatedAt(Instant.ofEpochMilli(rs.getTimestamp("created_at").getTime()));
            page.setFilename(rs.getString("filename"));
            page.setLineNumber(rs.getInt("line_number"));
            page.setLanguage(rs.getString("language"));
            page.setContent(rs.getString("content"));
            page.setNarration(rs.getString("narration"));
        }
        return Optional.ofNullable(page);
    }
    
    public String save(Page page) {
        if(page.getId() != null) {
            throw new RuntimeException("page updates not supported yet");
        } else {
            if(page.getCreatedAt() == null) {
                page.setCreatedAt(Instant.now());
            }
            List<Class<?>> argTypes = Arrays.asList(new Class<?>[] { Long.class, Instant.class, String.class,
                        Integer.class, String.class, String.class, String.class });
            List<Object> args = Arrays.asList(new Object[] { page.getTourId(), page.getCreatedAt(), page.getFilename(),
                                                            page.getLineNumber(), page.getLanguage(), page.getContent(),
                                                            page.getNarration() });
            String insertSql = "INSERT INTO pages "
                + "(tour_id, created_at, filename, line_number, language, content, narration) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING (id);";
            Pair<Long,String> insertResult = SimpleSql.executeQuery(dbConf, insertSql, argTypes, args, (rs) -> {
                    if(rs.next()) {
                        return rs.getLong("id");
                    }
                    return null;
                });

            if(insertResult.getValue1() != null) {
                return insertResult.getValue1();
            }

            page.setId(insertResult.getValue0());

            return null;
        }
    }
}
