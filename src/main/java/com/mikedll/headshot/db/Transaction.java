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

    private DataSource dataSource;

    public List<TransactionStatement> statements = new ArrayList<>(10);

    public Transaction(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void add(TransactionStatement stmt) {
        this.statements.add(stmt);
    }

    /*
     * Returns null on success, error on error.
     */
    public String execute() {
        try(Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String begin = "BEGIN;";
                System.out.println(begin);
                stmt.execute(begin);
            } catch (SQLException ex) {
                return "SQLException when starting transaction: " + ex.getMessage();
            }
            
            String error = null;
            for(TransactionStatement stmt : this.statements) {
                error = stmt.execute(conn);
                if(error != null) {
                    break;
                }
            }
            if(error != null) {
                try (Statement stmt = conn.createStatement()) {
                    String rollback = "ROLLBACK;";
                    System.out.println(rollback);
                    stmt.execute(rollback);
                } catch (SQLException ex) {
                    return "SQLException when rolling back transaction: " + ex.getMessage();
                }
                return error;
            }

            try (Statement stmt = conn.createStatement()) {
                String commit = "COMMIT;";
                System.out.println(commit);
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
