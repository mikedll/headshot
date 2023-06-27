package com.mikedll.headshot.model;

import com.mikedll.headshot.db.DatabaseConfiguration;

public class PageRepository extends RepositoryBase {

    public PageRepository(DatabaseConfiguration dbConf) {
        super(dbConf);
    }

    public String getTable() {
        return "pages";
    }

    public String save(Page page) {
        throw new RuntimeException("save no implemented yet");
    }
}
