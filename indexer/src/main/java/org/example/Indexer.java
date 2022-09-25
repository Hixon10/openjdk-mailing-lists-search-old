package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;


import static java.time.temporal.ChronoUnit.SECONDS;


public class Indexer {

    public void index() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://mail.openjdk.org/pipermail/amber-dev/2022-September/007456.html"))
                .timeout(Duration.of(5, SECONDS))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        saveToDb(response.body());
    }

    private void saveToDb(String response) {
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);  // set timeout to 10 sec.

            statement.executeUpdate("INSERT INTO myTable (id, name) VALUES (5, '" + UUID.randomUUID().toString() + "')");
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }
}
