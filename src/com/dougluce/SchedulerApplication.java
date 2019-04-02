/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dougluce;

import com.dougluce.controller.*;
import com.dougluce.model.Appointment;
import com.dougluce.model.User;
import com.dougluce.utility.AppointmentAlert;
import com.dougluce.utility.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author douglasluce
 */
public class SchedulerApplication extends Application {

  private BorderPane mainWindow;
  private User currentUser;

  Locale locale = Locale.getDefault();

  @Override
  public void start(Stage primaryStage) throws Exception {
    showLogin();
  }

  public void showLogin() throws IOException {
    ResourceBundle languageBundle = ResourceBundle.getBundle("bundles/Language");
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/login.fxml"), languageBundle);
    Parent loginWindow = loader.load();
    LoginController loginController = loader.getController();
    loginController.setSchedulerApplication(this);
    Scene loginScene = new Scene(loginWindow);
    Stage stage = new Stage();
    stage.setScene(loginScene);
    stage.setTitle(languageBundle.getString("LOGIN_TITLE"));
    stage.show();
  }


  public void showMain() throws IOException {
    AppointmentAlert appointmentAlert = new AppointmentAlert(currentUser);
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/main.fxml"));
    mainWindow = loader.load();

    // Loads MainController to call setSchedulerApplication() method
    // Allowing passing schedulerApplication reference
    MainController mainController = loader.getController();
    mainController.setSchedulerApplication(this);

    // Show the main scene
    Scene mainScene = new Scene(mainWindow);
    Stage mainStage = new Stage();
    mainStage.setScene(mainScene);
    mainStage.setTitle("Main");
    mainStage.show();

    // Show the customers pane on login
    showCustomersPane();
    appointmentAlert.checkAppointmentTime();
  }

  public void showCustomersPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/customers.fxml"));
    GridPane customersPane = loader.load();
    CustomersController customersController = loader.getController();
    customersController.setSchedulerApplication(this);
    mainWindow.setCenter(customersPane);
  }

  public void showAppointmentsPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/appointments.fxml"));
    GridPane appointmentsPane = loader.load();
    AppointmentsController appointmentsController = loader.getController();
    appointmentsController.setSchedulerApplication(this);
    mainWindow.setCenter(appointmentsPane);
  }

  public void showAppointmentsEditPane(Appointment appointment) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/appointmentEdit.fxml"));
    GridPane appointmentsEditPane = loader.load();
    AppointmentEditController appointmentEditController = loader.getController();
    appointmentEditController.setAppointment(appointment);
    appointmentEditController.setSchedulerApplication(this);
    mainWindow.setCenter(appointmentsEditPane);
  }

  public void showAppointmentsAddPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/appointmentAdd.fxml"));
    GridPane appointmentsAddPane = loader.load();
    AppointmentAddController appointmentAddController = loader.getController();
    appointmentAddController.setSchedulerApplication(this);
    mainWindow.setCenter(appointmentsAddPane);
  }

  public void showSchedulePane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/schedule.fxml"));
    GridPane schedulePane = loader.load();
    ScheduleController scheduleController = loader.getController();
    scheduleController.setSchedulerApplication(this);
    mainWindow.setCenter(schedulePane);
  }

  public void showReportsByMonthPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/reportsByMonth.fxml"));
    GridPane reportsByMonthPane = loader.load();
    mainWindow.setCenter(reportsByMonthPane);
  }

  public void showReportsByWeekPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/reportsByWeek.fxml"));
    GridPane reportsByWeekPane = loader.load();
    mainWindow.setCenter(reportsByWeekPane);
  }

  public void setCurrentUser(User currentUser) {
    this.currentUser = currentUser;
  }

  public User getCurrentUser() {
    return currentUser;
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    // Remove comment to test locale.
    //Locale.setDefault(new Locale("es", "ES"));
    DatabaseManager.connect();
    launch(args);
    DatabaseManager.closeDatabaseConnection();
  }

}
