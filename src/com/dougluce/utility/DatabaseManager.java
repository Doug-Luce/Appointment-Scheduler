package com.dougluce.utility;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseManager {

  private static Connection databaseConnection;
  private static final String DATABASE_URL = "jdbc:mysql://52.206.157.109:3306/U05ABR";
  private static final String USERNAME = "U05ABR";
  private static final String PASSWORD = "53688443692";
  private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";

  public static void connect() {
    try {
      Class.forName(DATABASE_DRIVER);
      databaseConnection = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
      System.out.println("Database Connection Established");
    } catch (ClassNotFoundException classNotFoundException) {
      classNotFoundException.printStackTrace();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public static Connection getDatabaseConnection() {
    return databaseConnection;
  }

  public static void closeDatabaseConnection() {
    try {
      databaseConnection.close();
      System.out.println("Database Connection Closed");
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
