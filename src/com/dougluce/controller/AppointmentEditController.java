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


public class AppointmentEditController implements Initializable {
  private Appointment appointment;
  private SchedulerApplication schedulerApplication;
  private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
  private DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
  private String username;

  @FXML
  private TextField appointmentTitle;

  @FXML
  private TextField contact;

  @FXML
  private ComboBox startTimeComboBox;

  @FXML
  private ComboBox endTimeComboBox;

  @FXML
  private ComboBox typeComboBox;

  @FXML
  private DatePicker datePicker;

  @FXML
  private Label customerNameLabel;

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    // Rubric G: Show the use of a Lambda
    // Using a Lambda to increase efficiency and reduce verbosity
    Platform.runLater(() -> {
      System.out.println(appointment.toString());
      populateAppointmentForm(appointment);
      username = schedulerApplication.getCurrentUser().getUserName();
      contact.setText(username);
      contact.setEditable(false);
    });
  }

  @FXML
  private void handleCancel() throws IOException {
    showAppointmentsPane();
  }

  private void showAppointmentsPane() throws IOException {
    schedulerApplication.showAppointmentsPane();
  }

  @FXML
  private void handleSave() throws IOException{
    if (!validateAppointmentForm()) {
      return;
    }
    updateAppointment(getAppointmentFromForm());
    showAppointmentsPane();
  }

  private void updateAppointment(Appointment appointment) {
    String updateAppointmentStatement = "UPDATE appointment SET customerId = ?, title = ?, description = ?, contact = ?, start = ?, end = ?, lastUpdate = CURRENT_TIMESTAMP WHERE appointmentId = ?";
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
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement(updateAppointmentStatement);
      statement.setString(1, appointment.getCustomer().getID());
      statement.setString(2, appointment.getTitle());
      statement.setString(3, appointment.getDescription());
      statement.setString(4, appointment.getContact());
      statement.setTimestamp(5, startTimeStamp);
      statement.setTimestamp(6, endTimeStamp);
      statement.setString(7, appointment.getAppointmentId());

      int rowsUpdated = statement.executeUpdate();
      if (rowsUpdated == 0 || rowsUpdated > 1) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Error Updating Appointment");
        errorAlert.setContentText("Couldn't update appointment");
        errorAlert.showAndWait();
      }
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }

  private void populateAppointmentForm(Appointment appointment) {
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

    // Convert SQL datetime to LocalDateTime
    LocalDateTime startLocalDateTime = LocalDateTime.parse(appointment.getStartTime(), dateTimeFormatter);
    LocalDateTime endLocalDateTime = LocalDateTime.parse(appointment.getEndTime(), dateTimeFormatter);

    appointmentTitle.setText(appointment.getTitle());
    typeComboBox.setItems(types);
    typeComboBox.setValue(appointment.getDescription());
    startTimeComboBox.setItems(startTimes);
    startTimeComboBox.getSelectionModel().select(startLocalDateTime.toLocalTime().format(timeFormatter));
    endTimeComboBox.setItems(endTimes);
    endTimeComboBox.getSelectionModel().select(endLocalDateTime.toLocalTime().format(timeFormatter));
    datePicker.setValue(LocalDate.parse(appointment.getStartTime(), dateTimeFormatter));
    customerNameLabel.setText(appointment.getCustomer().getFullName());
  }

  private boolean validateAppointmentForm() {
    String _appointmentTitle = appointmentTitle.getText();
    Object _type = typeComboBox.getValue();
    String _contact = contact.getText();
    String _startTime = startTimeComboBox.getValue().toString();
    String _endTime = endTimeComboBox.getValue().toString();
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
    String findAppointmentStatement = "SELECT * FROM appointment WHERE (? BETWEEN start AND end OR ? BETWEEN start AND end OR ? < start AND ? > end) AND (createdBy = ? AND appointmentId != ?)";
    String _appointmentId = appointment.getAppointmentId();

    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement(findAppointmentStatement);
      statement.setTimestamp(1, Timestamp.valueOf(startTime.toLocalDateTime()));
      statement.setTimestamp(2, Timestamp.valueOf(endTime.toLocalDateTime()));
      statement.setTimestamp(3, Timestamp.valueOf(startTime.toLocalDateTime()));
      statement.setTimestamp(4, Timestamp.valueOf(endTime.toLocalDateTime()));
      statement.setString(5, username);
      statement.setString(6, _appointmentId);
      ResultSet set = statement.executeQuery();

      if (set.next()) {
        return true;
      }
    } catch (SQLException exception) {
      exception.printStackTrace();
    }

    return false;
  }

  private Appointment getAppointmentFromForm() {
    return new Appointment(appointmentTitle.getText(),typeComboBox.getValue().toString(),
            startTimeComboBox.getValue().toString(), endTimeComboBox.getValue().toString(), contact.getText(),
            appointment.getCustomer(), appointment.getAppointmentId());
  }

  public void setAppointment(Appointment appointment) {
    this.appointment = appointment;
  }

  public void setSchedulerApplication(SchedulerApplication schedulerApplication) {
    this.schedulerApplication = schedulerApplication;
  }
}
