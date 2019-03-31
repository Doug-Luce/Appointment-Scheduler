/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dougluce.controller;


import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;
import java.util.ResourceBundle;

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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;



/**
 * FXML Controller class
 *
 * @author douglasluce
 */
public class AppointmentsController implements Initializable {

  /**
   * Initializes the controller class.
   */

  @FXML
  private TableView<Appointment> tableView;

  @FXML
  private TableColumn<Appointment, String> customerColumn;

  @FXML
  private TableColumn<Appointment, String> titleColumn;

  @FXML
  private TableColumn<Appointment, String> descriptionColumn;

  @FXML
  private TableColumn<Appointment, String> startTimeColumn;

  @FXML
  private TableColumn<Appointment, String> endTimeColumn;

  @FXML
  private TableColumn<Appointment, String> contactColumn;

  private Appointment selectedAppointment;

  private final DateTimeFormatter timeDTF = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
  private final ZoneId zoneId = ZoneId.systemDefault();
  private final String getAppointmentsStatement = "SELECT appointment.title, appointment.description, appointment.start, appointment.end, appointment.contact, appointment.appointmentId, appointment.customerId, customer.customerId, customer.customerName, appointment.createdBy  FROM appointment, customer WHERE appointment.customerId = customer.customerId ORDER BY start";
  SchedulerApplication schedulerApplication;

  public void setSchedulerApplication(SchedulerApplication schedulerApplication) {
    this.schedulerApplication = schedulerApplication;
  }

  public void showEditPane(Appointment appointment) throws IOException {
    schedulerApplication.showAppointmentsEditPane(appointment);
  }

  public void showAddPane() throws IOException {
    schedulerApplication.showAppointmentsAddPane();
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    getAndPopulateTableView(getAppointmentsStatement);
    handleTableviewClick();
  }

  private void handleTableviewClick() {
    // Using a lambda here to decrease verbosity of click handler
    tableView.setOnMousePressed(event -> {
      if (event.isPrimaryButtonDown()) {
        System.out.println(tableView.getSelectionModel().getSelectedItem().toString());
        selectedAppointment = tableView.getSelectionModel().getSelectedItem();
      }
    });
  }

  @FXML
  private void handleDelete() {
    Appointment selectedAppointment = tableView.getSelectionModel().getSelectedItem();
    if (selectedAppointment != null) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Delete Confirmation");
      alert.setContentText("Area you sure you wish to delete this appointment?");
      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == ButtonType.OK) {
        deleteAppointment(selectedAppointment);
        getAndPopulateTableView(getAppointmentsStatement);
      }
      } else {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("No Appointment Selected");
      alert.setContentText("Please select an appointment in the table to delete");
      alert.showAndWait();
    }
  }

  @FXML
  private void handleShowByWeek() {
    String appointmentsByWeekStatement = "SELECT appointment.title, appointment.description, appointment.start, appointment.end, appointment.contact, appointment.appointmentId, appointment.customerId, customer.customerId, customer.customerName, appointment.createdBy  FROM appointment, customer WHERE (appointment.customerId = customer.customerId) AND appointment.start BETWEEN NOW() and DATE_ADD(NOW(), INTERVAL 7 DAY) ORDER BY start";
    getAndPopulateTableView(appointmentsByWeekStatement);
  }

  @FXML
  private void handleShowByMonth() {
    String appointmentsByMonthStatement = "SELECT appointment.title, appointment.description, appointment.start, appointment.end, appointment.contact, appointment.appointmentId, appointment.customerId, customer.customerId, customer.customerName, appointment.createdBy  FROM appointment, customer WHERE (appointment.customerId = customer.customerId) AND appointment.start BETWEEN NOW() and DATE_ADD(NOW(), INTERVAL 1 MONTH) ORDER BY start";
    getAndPopulateTableView(appointmentsByMonthStatement);
  }

  @FXML
  private void handleShowAll() {
    getAndPopulateTableView(getAppointmentsStatement);
  }

  @FXML
  private void handleEdit() {
    try {
      if (selectedAppointment != null) {
        showEditPane(selectedAppointment);
      } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No Appointment Selected");
        alert.setContentText("Please select an appointment to edit.");
        alert.showAndWait();
      }
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  @FXML
  private void handleAdd() {
    try {
      showAddPane();
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  private void deleteAppointment(Appointment appointment) {
    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement("DELETE appointment.* FROM appointment WHERE appointment.appointmentId = ?");
      statement.setString(1, appointment.getAppointmentId());
      statement.executeUpdate();
    } catch (SQLException exception) {
      exception.printStackTrace();
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setHeaderText("Error Deleting Appointment ID " + appointment.getAppointmentId());
      errorAlert.setContentText("Couldn't delete appointment");
      errorAlert.showAndWait();
    }
  }

  private void getAndPopulateTableView(String sqlStatement) {
    ObservableList<Appointment> appointmentData = FXCollections.observableArrayList();
    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement(sqlStatement);
      ResultSet set = statement.executeQuery();
      // TODO verify that the time being displayed in the table is correct. It appears that it's not showing the right times.
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
        System.out.println(set.getTimestamp("appointment.start"));
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
      startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
      endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
      contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));

      // Populate tableview with appointments
      tableView.setItems(appointmentData);

    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }
}
