package com.dougluce.controller;

import com.dougluce.SchedulerApplication;
import com.dougluce.model.Appointment;
import com.dougluce.model.Customer;
import com.dougluce.utility.DatabaseManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javax.sound.midi.SysexMessage;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AppointmentAddController implements Initializable {
  private SchedulerApplication schedulerApplication;
  private final String getCustomerNamesStatement = "SELECT customerName, customerId FROM customer ORDER BY customerName";
  private DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

  @FXML
  private TableView<Customer> tableView;

  @FXML
  private TableColumn<Customer, String> fullName;

  @FXML
  private ComboBox typeComboBox;

  @FXML
  private ComboBox startTimeComboBox;

  @FXML
  private ComboBox endTimeComboBox;

  @FXML
  private DatePicker datePicker;

  @FXML
  private TextField contact;

  @FXML
  private TextField appointmentTitle;
  private String username;

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    getAndPopulateTableView(getCustomerNamesStatement);
    populateAppointmentForm();
    // Rubric G: Demonstrating the use of a Lambda with justification
    // Using a lambda to reduce verbosity
    Platform.runLater(() -> {
      username = schedulerApplication.getCurrentUser().getUserName();
      contact.setText(username);
      contact.setEditable(false);
    });

  }

  public void setSchedulerApplication(SchedulerApplication schedulerApplication) {
    this.schedulerApplication = schedulerApplication;
  }

  @FXML
  private void handleSave() throws IOException {
    if (!validateAppointmentForm()) {
      return;
    }
    saveAppointment(getAppointmentFromForm());
  }

  @FXML
  private void handleCancel() throws IOException {
    schedulerApplication.showAppointmentsPane();
  }

  private void getAndPopulateTableView(String sqlStatement) {
    ObservableList<Customer> customers = FXCollections.observableArrayList();

    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement(sqlStatement);
      ResultSet set = statement.executeQuery();

      // For every item that set returns add the customer to the ObservableList
      while(set.next()) {
        customers.add(new Customer(set.getString("customerName"), set.getString("customerId")));
      }

      fullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
      tableView.setItems(customers);

    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }

  private void populateAppointmentForm() {
    ObservableList<String> types = FXCollections.observableArrayList();
    ObservableList<String> startTimes = FXCollections.observableArrayList();
    ObservableList<String> endTimes = FXCollections.observableArrayList();

    types.addAll("Introduction", "Consultation", "Appointment", "Pay Bill", "Meeting");

    // Set for 15 minutes before open to get the appropriate time
    LocalTime openTime = LocalTime.of(8, 45);

    // Set for 15 minutes before close to get the appropriate time
    LocalTime closeTime = LocalTime.of(16, 45);

    // Build all start and end times for ComboBoxes
    while(openTime.isBefore(closeTime)) {
      openTime = openTime.plusMinutes(15);
      startTimes.add(openTime.format(timeFormatter));
      endTimes.add(openTime.format(timeFormatter));
    }
    // Add 15 more minutes for business close time
    openTime = openTime.plusMinutes(15);
    endTimes.add(openTime.format(timeFormatter));

    typeComboBox.setItems(types);
    startTimeComboBox.setItems(startTimes);
    endTimeComboBox.setItems(endTimes);
    datePicker.setValue(LocalDate.now());
    startTimeComboBox.getSelectionModel().select(LocalTime.of(9, 0).format(timeFormatter));
    endTimeComboBox.getSelectionModel().select(LocalTime.of(9, 15).format(timeFormatter));
  }

  private void saveAppointment(Appointment appointment) throws IOException {
    // If there is no customer selected in the table, throw an alert telling the user to select a customer
    String insertAppointmentStatement = "INSERT INTO appointment (customerId, title, description, contact, start, end, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";
    LocalDate localDate = datePicker.getValue();
    LocalTime startTime = LocalTime.parse(appointment.getStartTime(), timeFormatter);
    LocalTime endTime = LocalTime.parse(appointment.getEndTime(), timeFormatter);

    LocalDateTime startDateTime = LocalDateTime.of(localDate, startTime);
    LocalDateTime endDateTime = LocalDateTime.of(localDate, endTime);

    ZonedDateTime startUTC = startDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
    ZonedDateTime endUTC = endDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));

    Timestamp startTimeStamp = Timestamp.valueOf(startUTC.toLocalDateTime());
    Timestamp endTimeStamp = Timestamp.valueOf(endUTC.toLocalDateTime());

    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement(insertAppointmentStatement);
      statement.setString(1, appointment.getCustomer().getID());
      statement.setString(2, appointment.getTitle());
      statement.setString(3, appointment.getDescription());
      statement.setString(4, appointment.getContact());
      statement.setTimestamp(5, startTimeStamp);
      statement.setTimestamp(6, endTimeStamp);
      statement.setString(7, username);
      statement.setString(8, username);

      int rowsUpdated = statement.executeUpdate();
      if (rowsUpdated == 0 || rowsUpdated > 1) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Error Creating New Appointment");
        errorAlert.setContentText("Couldn't add appointment");
        errorAlert.showAndWait();
      }
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
    schedulerApplication.showAppointmentsPane();
  }

  private boolean validateAppointmentForm() {
    String _appointmentTitle = appointmentTitle.getText();
    Object _type = typeComboBox.getValue();
    String _contact = contact.getText();
    String _startTime = startTimeComboBox.getValue().toString();
    String _endTime = endTimeComboBox.getValue().toString();
    Customer _customer = tableView.getSelectionModel().getSelectedItem();
    LocalDate _localDate = datePicker.getValue();

    List<String> errorMessages = new ArrayList<>();

    if (_appointmentTitle == null || _appointmentTitle.length() < 3) {
      errorMessages.add("Please set an appointment title that is at least 3 characters");
    }

    if (_type == null) {
      errorMessages.add("Please choose a description");
    }

    if (_contact == null) {
      errorMessages.add("Error loading username");
    }

    if (_customer == null) {
      errorMessages.add("Customer must be selected in the table");
    }

    if (_localDate == null) {
      errorMessages.add("Please select a date");
    }

    LocalTime startTime = LocalTime.parse(_startTime, timeFormatter);
    LocalTime endTime = LocalTime.parse(_endTime, timeFormatter);

    LocalDateTime startDateTime = LocalDateTime.of(_localDate, startTime);
    LocalDateTime endDateTime = LocalDateTime.of(_localDate, endTime);

    ZonedDateTime startUTC = startDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
    ZonedDateTime endUTC = endDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));

    if (startUTC == null) {
      errorMessages.add("Please select a start time");
    }

    if (endUTC == null) {
      errorMessages.add("Please select an end time");
    }

    if (endUTC.isBefore(startUTC) || endUTC.equals(startUTC)) {
      errorMessages.add("Please select an end time that is before the start time");
    }

    if (checkAppointmentForConflict(startUTC, endUTC)) {
      errorMessages.add("Selected appointment times conflict with an exiting appointment. Please select a new time");
    }

    if (errorMessages.size() > 0) {
      errorMessages.forEach((errorMessage) -> {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Please Check Customer Form ");
        errorAlert.setContentText(errorMessage);
        errorAlert.showAndWait();
      });
      return false;
    }

    return true;
  }

  private boolean checkAppointmentForConflict(ZonedDateTime startTime, ZonedDateTime endTime) {
    String findAppointmentStatment = "SELECT * FROM appointment WHERE (? BETWEEN start AND end OR ? BETWEEN start AND end OR ? < start AND ? > end) AND createdBy = ?";

    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement(findAppointmentStatment);
      statement.setTimestamp(1, Timestamp.valueOf(startTime.toLocalDateTime()));
      statement.setTimestamp(2, Timestamp.valueOf(endTime.toLocalDateTime()));
      statement.setTimestamp(3, Timestamp.valueOf(startTime.toLocalDateTime()));
      statement.setTimestamp(4, Timestamp.valueOf(endTime.toLocalDateTime()));
      statement.setString(5, username);
      ResultSet set = statement.executeQuery();
      System.out.println(set);
      if (set.next()) {
        return true;
      }
    } catch (SQLException exception) {
      exception.printStackTrace();
    }

    return false;
  }

  public Appointment getAppointmentFromForm() {
    return new Appointment(appointmentTitle.getText(), typeComboBox.getValue().toString(),
        contact.getText(), startTimeComboBox.getValue().toString(), endTimeComboBox.getValue().toString(),
            tableView.getSelectionModel().getSelectedItem());
  }
}
