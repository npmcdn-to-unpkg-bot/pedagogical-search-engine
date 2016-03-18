package mysql;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Connection {

    private String m_database;
    private java.sql.Connection m_connection;

    public Connection(String database,
                      String user,
                      String password,
                      String ip,
                      String port) {
        m_database = database;

        // Load jdbc driver
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Connect
        String url = String.format("jdbc:mysql://%s:%s/%s",
                ip,
                port,
                database);
        try {
            m_connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getDatabase() {
        return m_database;
    }

    public ResultSet query(String query) {
        ResultSet rs = null;
        try {
            Statement stmt = m_connection.createStatement();
            rs = stmt.executeQuery(query);
        } catch (Exception e) {
            System.err.println("Cannot connect to database server");
            System.err.println(e.getMessage());
            System.out.println(query);
            e.printStackTrace();
        }
        return rs;
    }

    public void update(String query) {
        try {
            m_connection.createStatement().executeUpdate(query);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.out.println(query);
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            m_connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
