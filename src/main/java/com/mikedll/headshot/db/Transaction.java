package com.mikedll.headshot.db;

import java.util.List;
import java.util.ArrayList;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;

/*
 * Assumes we're doing an update.
 */
public class Transaction {

    private DatabaseConfiguration dbConf;

    public List<TransactionStatement<?>> statements = new ArrayList<>(10);

    public Transaction(DatabaseConfiguration dbConf) {
        this.dbConf = dbConf;
    }

    public void add(TransactionStatement<?> stmt) {
        this.statements.add(stmt);
    }

    /*
     * Returns null on success, error on error.
     */
    public String execute() {
        try(Connection conn = dbConf.getDataSource().getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String begin = "BEGIN;";
                dbConf.logger.debug(begin);
                stmt.execute(begin);
            } catch (SQLException ex) {
                return "SQLException when starting transaction: " + ex.getMessage();
            }
            
            String error = null;
            for(TransactionStatement stmt : this.statements) {
                error = stmt.execute(dbConf.logger, conn);
                if(error != null) {
                    break;
                }
            }
            if(error != null) {
                try (Statement stmt = conn.createStatement()) {
                    String rollback = "ROLLBACK;";
                    dbConf.logger.debug(rollback);
                    stmt.execute(rollback);
                } catch (SQLException ex) {
                    return "SQLException when rolling back transaction: " + ex.getMessage();
                }
                return error;
            }

            try (Statement stmt = conn.createStatement()) {
                String commit = "COMMIT;";
                dbConf.logger.debug(commit);
                stmt.execute(commit);
            } catch (SQLException ex) {
                return "SQLException when commiting transaction: " + ex.getMessage();
            }
            
            return null;
        } catch (SQLException ex) {
            return "SQLException in transaction: " + ex.getMessage();
        }
    }
}
