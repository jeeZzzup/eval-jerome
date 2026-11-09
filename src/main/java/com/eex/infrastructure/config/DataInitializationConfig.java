package com.eex.infrastructure.config;

import com.eex.domain.util.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DataInitializationConfig {

    private static final String sqlite_url = "jdbc:sqlite:sqlite/sqlite.test.db";

    @Bean
    public CommandLineRunner initializeDatabase() {
        return args -> {
            try (var conn = DriverManager.getConnection(sqlite_url)) {
                if (conn != null) {
                    var meta = conn.getMetaData();
                    System.out.println("La base a été créée sur " + meta.getURL());
                }
                createTables();
                insertData();
                System.out.println("La base de données a été initialisée");
                displayData();
            } catch (Exception e) {
                System.err.println("Erreur lors de l'initialisation SQLite : " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    public static void createTables() {
        var createClientTableQuery = "CREATE TABLE IF NOT EXISTS client ("
                + "	id INTEGER PRIMARY KEY,"
                + "	legal_entity BOOLEAN NOT NULL"
                + ");";
        var createUserTableQuery = "CREATE TABLE IF NOT EXISTS user ("
                + "	id INTEGER PRIMARY KEY,"
                + "	username TEXT NOT NULL,"
                + "	password TEXT NOT NULL,"
                + "	client_id INTEGER NOT NULL,"
                + "	FOREIGN KEY (client_id) REFERENCES client(id)"
                + ");";
        var createAccountsTableQuery = "CREATE TABLE IF NOT EXISTS account ("
                + "	id INTEGER PRIMARY KEY,"
                + "	name TEXT NOT NULL,"
                + "	amount DOUBLE NOT NULL,"
                + "	client_id INTEGER NOT NULL,"
                + "	FOREIGN KEY (client_id) REFERENCES client(id)"
                + ");";

        try (var conn = DriverManager.getConnection(sqlite_url);
             var stmt = conn.createStatement()) {
            stmt.execute(createClientTableQuery);
            stmt.execute(createUserTableQuery);
            stmt.execute(createAccountsTableQuery);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertData() {
        var legalEntity = new boolean[] {true, true, true, false, false};
        var usernames = new String[] {"Alice", "Bernard", "Chloé", "David", "Emma"};
        var passwords = new String[] {"aliceSecret", "bernardSecret", "chloeSecret", "davidSecret", "emmaSecret"};
        var accountNames = new String[] {"AMAZON", "BORNEO", "COMORA"};
        var amounts = new double[] {1000.0, 2000.0, 3000.0};

        try (var conn = DriverManager.getConnection(sqlite_url);
             var pstmtClient =  conn.prepareStatement("INSERT OR IGNORE INTO client (id, legal_entity) VALUES(?,?)");
             var pstmtUser =    conn.prepareStatement("INSERT OR IGNORE INTO user (id, client_id, username, password) VALUES(?,?,?,?)");
             var pstmtAccount = conn.prepareStatement("INSERT OR IGNORE INTO account (id, client_id, name, amount) VALUES(?,?,?,?)")) {
            for (int i = 0; i < 5; i++){
                pstmtClient.setInt(1, i + 1);
                pstmtClient.setBoolean(2, legalEntity[i]);
                pstmtClient.executeUpdate();

                pstmtUser.setInt(1, i + 1);
                pstmtUser.setInt(2, i + 1);
                pstmtUser.setString(3, usernames[i]);
                pstmtUser.setString(4, PasswordUtil.hashPassword(passwords[i]));
                pstmtUser.executeUpdate();
            }

            for (int i = 0; i < 3; i++){
                pstmtAccount.setInt(1, i + 1);
                pstmtAccount.setInt(2, i + 1);
                pstmtAccount.setString(3, accountNames[i]);
                pstmtAccount.setDouble(4, amounts[i]);
                pstmtAccount.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void displayData() {
        try (var conn = DriverManager.getConnection(sqlite_url);
             var stmt = conn.createStatement();
             var rsUser = stmt.executeQuery("SELECT * FROM user")) {

            System.out.println("\nTable User");
            System.out.println("id\tclient_id\tusername\tpassword");
            System.out.println("------------------------------------");
            while (rsUser.next()) {
                System.out.println(rsUser.getInt("id") + "\t" +
                        rsUser.getInt("client_id") + "\t\t\t" +
                        rsUser.getString("username") + "\t\t" +
                        rsUser.getString("password")
                );
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        try (var conn = DriverManager.getConnection(sqlite_url);
             var stmt = conn.createStatement();
             var rsClient = stmt.executeQuery("SELECT * FROM client")) {

            System.out.println("\nTable Client");
            System.out.println("id\tlegal_entity");
            System.out.println("----------------");
            while (rsClient.next()) {
                System.out.println(rsClient.getInt("id") + "\t" +
                        rsClient.getBoolean("legal_entity")
                );
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        try (var conn = DriverManager.getConnection(sqlite_url);
             var stmt = conn.createStatement();
             var rsAccount = stmt.executeQuery("SELECT * FROM account")) {

            System.out.println("\nTable Account");
            System.out.println("id\tclient_id\tname\tamount");
            System.out.println("------------------------------");
            while (rsAccount.next()) {
                System.out.println(rsAccount.getInt("id") + "\t" +
                        rsAccount.getInt("client_id") + "\t\t\t" +
                        rsAccount.getString("name") + "\t" +
                        rsAccount.getDouble("amount")
                );
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}

