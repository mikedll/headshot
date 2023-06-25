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

    DataSource dataSource;

    List<TransactionStatement> statements;
    
    public Transaction(DataSource dataSource) {
        this.dataSource = dataSource;
        this.statements = new ArrayList<>(10);
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
                stmt.execute("BEGIN;");
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
                    stmt.execute("ROLLBACK;");
                } catch (SQLException ex) {
                    return "SQLException when rolling back transaction: " + ex.getMessage();
                }
                return error;
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("COMMIT;");
            } catch (SQLException ex) {
                return "SQLException when commiting transaction: " + ex.getMessage();
            }
            
            return null;
        } catch (SQLException ex) {
            return "SQLException in transaction: " + ex.getMessage();
        }
    }
}
