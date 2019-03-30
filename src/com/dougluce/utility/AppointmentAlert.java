package com.dougluce.utility;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AppointmentAlert {
  public AppointmentAlert() {
  }

  private void getAppointments() {
    String getAppointmentsStatement = "SELECT * FROM appointments";
    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement("");
    } catch (SQLException exception) {
        exception.printStackTrace();
    }
  }

  private void getCurrentTime() {

  }

  private void getCurrentUser() {

  }
}
