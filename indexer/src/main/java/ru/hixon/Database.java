package ru.hixon;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    private static final Logger logger = Logger.getLogger(Database.class.getName());
    private static final int QUERY_TIMEOUT_IN_SECONDS = 10;

    private final String dbUrl;

    public Database(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    /**
     * Creates tables and indexes, if needed
     */
    public void executeDatabaseMigrations () {
        logger.info("execution database migrations");

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(QUERY_TIMEOUT_IN_SECONDS);

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS emails (url VARCHAR(128) PRIMARY KEY, content TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS month_indexed (month_url VARCHAR(128) PRIMARY KEY)");
        } catch (Exception e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            logger.log(Level.SEVERE, "Got error, when execute migration", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Db connection close failed", e);
            }
        }
    }

    public Set<String> getIndexedMonthUrls() {
        logger.info("execution getIndexedMonthUrls()");

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(QUERY_TIMEOUT_IN_SECONDS);

            ResultSet rs = statement.executeQuery("SELECT month_url FROM month_indexed");
            Set<String> result = new HashSet<>();
            while (rs.next()) {
                result.add(rs.getString("month_url"));
            }
            return result;
        } catch (Exception e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            logger.log(Level.SEVERE, "Got error, when execute getIndexedMonthUrls()", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Db connection close failed", e);
            }
        }
    }
}
