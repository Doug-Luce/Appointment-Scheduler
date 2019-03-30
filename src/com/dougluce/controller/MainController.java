/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dougluce.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.dougluce.SchedulerApplication;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.control.MenuBar;

/**
 * FXML Controller class
 *
 * @author douglasluce
 */
public class MainController implements Initializable {

  /**
   * Initializes the controller class.
   */

  @FXML
  MenuBar menuBar;

  SchedulerApplication schedulerApplication;

  @Override
  public void initialize(URL url, ResourceBundle rb) {
  }

  public void setSchedulerApplication(SchedulerApplication schedulerApplication) {
    this.schedulerApplication = schedulerApplication;
  }

  public void logoutHandler() throws IOException {
    Stage mainWindow = (Stage) menuBar.getScene().getWindow();
    mainWindow.hide();
    schedulerApplication.showLogin();
  }

  public void showCustomersPane() throws IOException {
    schedulerApplication.showCustomersPane();
  }

  public void showAppointmentsPane() throws IOException {
    schedulerApplication.showAppointmentsPane();
  }

  public void showReportsPane() throws IOException {
    schedulerApplication.showReportsPane();
  }

  public void exitMenuHandler(ActionEvent e) {
    Platform.exit();
  }

}
