package com.dougluce.controller;

import com.dougluce.SchedulerApplication;
import com.dougluce.model.Appointment;
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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
    System.out.println("Go to Appointment Screen");
    showAppointmentsPane();
  }

  private void showAppointmentsPane() throws IOException {
    schedulerApplication.showAppointmentsPane();
  }

  @FXML
  private void handleSave() throws IOException{
    System.out.println(getAppointmentFromForm());
    updateAppointment(getAppointmentFromForm());
    showAppointmentsPane();
  }

  private void updateAppointment(Appointment appointment) {
    System.out.println(appointment);
    String updateAppointmentStatement = "UPDATE appointment SET customerId = ?, title = ?, description = ?, contact = ?, start = ?, end = ?, lastUpdate = CURRENT_TIMESTAMP WHERE appointmentId = ?";
    LocalDate localDate = datePicker.getValue();
    LocalTime startTime = LocalTime.parse(appointment.getStartTime(), timeFormatter);
    LocalTime endTime = LocalTime.parse(appointment.getEndTime(), timeFormatter);
    LocalDateTime startDateTime = LocalDateTime.of(localDate, startTime);
    LocalDateTime endDateTime = LocalDateTime.of(localDate, endTime);

    ZonedDateTime startUTC = startDateTime.atZone(ZoneId.systemDefault());
    ZonedDateTime endUTC = endDateTime.atZone(ZoneId.systemDefault());

    Timestamp startTimeStamp = Timestamp.valueOf(startUTC.toLocalDateTime());
    Timestamp endTimeStamp = Timestamp.valueOf(endUTC.toLocalDateTime());


    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement(updateAppointmentStatement);
      statement.setString(1, appointment.getCustomer().getID());
      statement.setString(2, appointment.getTitle());
      statement.setString(3, appointment.getDescription());
      statement.setString(4, appointment.getContact());
      statement.setString(5, startTimeStamp.toString());
      statement.setString(6, endTimeStamp.toString());
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
    System.out.println(appointment);

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
