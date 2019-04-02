package com.dougluce.controller;

import com.dougluce.SchedulerApplication;
import com.dougluce.model.Appointment;
import com.dougluce.model.Customer;
import com.dougluce.utility.DatabaseManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ResourceBundle;

public class ScheduleController implements Initializable {

  private SchedulerApplication schedulerApplication;

  @FXML
  private Label userLabel;

  @FXML
  private TableView<Appointment> tableView;

  @FXML
  private TableColumn<Appointment, String> customerColumn;

  @FXML
  private TableColumn<Appointment, String> titleColumn;

  @FXML
  private TableColumn<Appointment, String> descriptionColumn;

  @FXML
  private TableColumn<Appointment, String> startColumn;

  @FXML
  private TableColumn<Appointment, String> endColumn;
  private String username;
  private final String getAppointmentsStatement = "SELECT appointment.title, appointment.description, appointment.start, appointment.end, appointment.appointmentId, appointment.customerId, customer.customerId, customer.customerName, appointment.createdBy, appointment.contact  FROM appointment, customer WHERE appointment.customerId = customer.customerId AND contact = ? ORDER BY start";
  private final ZoneId zoneId = ZoneId.systemDefault();
  private final DateTimeFormatter timeDTF = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Platform.runLater(() -> {
      username = schedulerApplication.getCurrentUser().getUserName();
      userLabel.setText(username.substring(0, 1).toUpperCase() + username.substring(1) + "'s " + " Schedule");
      getAndPopulateTableView(getAppointmentsStatement);
    });
  }

  public void setSchedulerApplication(SchedulerApplication schedulerApplication) {
    this.schedulerApplication = schedulerApplication;
  }

  private void getAndPopulateTableView(String sqlStatement) {
    ObservableList<Appointment> appointmentData = FXCollections.observableArrayList();
    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement(sqlStatement);
      statement.setString(1, username);
      ResultSet set = statement.executeQuery();
      while(set.next()) {
        Customer customer = new Customer(set.getString("customer.customerName"), set.getString("appointment.customerId"));

        // Get the start time as a Timestamp
        Timestamp timestampStart = set.getTimestamp("appointment.start");

        // Convert the start timestamp into ZonedDateTime
        ZonedDateTime zoneDateStart = timestampStart.toLocalDateTime().atZone(ZoneId.of("UTC"));
        ZonedDateTime localStartTime = zoneDateStart.withZoneSameInstant(zoneId);

        // Build correct local end time
        Timestamp timestampEnd = set.getTimestamp("appointment.end");
        ZonedDateTime zoneDateEnd = timestampEnd.toLocalDateTime().atZone(ZoneId.of("UTC"));

        ZonedDateTime localEndTime = zoneDateEnd.withZoneSameInstant(zoneId);

        appointmentData.add(new Appointment(set.getString("appointment.title"), set.getString("appointment.description"),
            localStartTime.format(timeDTF), localEndTime.format(timeDTF), set.getString("appointment.contact"),
            customer, set.getString("appointment.appointmentId")));
      }

      // Using a lambda for efficiency
      // This will get the customer's full name from the Customer object
      customerColumn.setCellValueFactory(
          Customer -> {
            SimpleObjectProperty property = new SimpleObjectProperty();
            property.setValue(Customer.getValue().getCustomer().getFullName());
            return property;
          }
      );
      // Set cell factories to populate table
      titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
      descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
      startColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
      endColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));

      // Populate tableview with appointments
      tableView.setItems(appointmentData);

    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }
}
