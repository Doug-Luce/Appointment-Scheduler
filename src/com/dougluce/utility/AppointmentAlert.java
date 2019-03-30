package com.dougluce.utility;
import com.dougluce.model.User;
import javafx.scene.control.Alert;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentAlert {
  private User currentUser;

  public AppointmentAlert(User currentUser) {
    this.currentUser = currentUser;
  }

  public void checkAppointmentTime() {
    String getAppointmentsStatement = "SELECT start, customerName FROM appointment, customer WHERE contact = ? AND appointment.customerId = customer.customerId";
    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement(getAppointmentsStatement);
      statement.setString(1, currentUser.getUserName());
      ResultSet resultSet = statement.executeQuery();

      while(resultSet.next()) {
        String customerName = resultSet.getString("customerName");
        LocalDateTime appointmentStartTime = resultSet.getTimestamp("start").toLocalDateTime();
        LocalDateTime currentTime = LocalDateTime.now();
        System.out.println(appointmentStartTime);
        System.out.println(currentTime.plusMinutes(15));
        System.out.println(currentTime.isBefore(appointmentStartTime.plusMinutes(15)));
        if (currentTime.isBefore(appointmentStartTime.plusMinutes(15))) {
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("Upcoming Appointment");
          // TODO update this string to contain the appointment information
          alert.setContentText("You have an upcoming appointment with " + customerName + " at " +
              appointmentStartTime.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a")));
          alert.showAndWait();
        }
      }

    } catch (SQLException exception) {
        exception.printStackTrace();
    }
  }

}
