/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dougluce.controller;

import com.dougluce.SchedulerApplication;
import com.dougluce.model.User;
import com.dougluce.utility.DatabaseManager;
import com.dougluce.utility.LoggerUtility;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author douglasluce
 */
public class LoginController implements Initializable {

  /**
   * Initializes the controller class.
   */

  @FXML
  Button loginButton;

  @FXML
  public TextField usernameField;

  @FXML
  public PasswordField passwordField;

  private String credentialError;
  private String credentialDirection;

  SchedulerApplication schedulerApplication;
  LoggerUtility loggerUtility = new LoggerUtility();

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    credentialError = rb.getString("CREDENTIAL_ERROR");
    credentialDirection = rb.getString("CREDENTIAL_DIRECTION");

    /**
     * Adds a event handler to the passwordField input and fires the
     * login button when the enter key is pressed.
     */
    // Rubric G: Demonstrating the use of a Lambda with justification
    // Using a lambda to fire the login button and make increase application efficiency
    passwordField.setOnKeyPressed((event -> {
      if (event.getCode().equals(KeyCode.ENTER)) {
        loginButton.fire();
      }
    }));
  }

  public void loginButtonHandler(ActionEvent event) throws IOException {
    String userName = usernameField.getText();
    String password = passwordField.getText();

    // Requirements Rubric F: Exception handling for incorrect username and password
    if (validateUserLogin(userName, password)) {
      // Rubric J: Providing a utility to track the users login
      loggerUtility.log(userName + " logged in");
      Stage loginWindow = (Stage)((Node)event.getSource()).getScene().getWindow();
      loginWindow.hide();
      schedulerApplication.showMain();
    } else {
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setHeaderText(credentialError);
      errorAlert.setContentText(credentialDirection);
      errorAlert.showAndWait();
    }
  }

  public void exitButtonHandler() {
    Platform.exit();
  }

  private boolean validateUserLogin(String username, String password) {
    try {
      // Get all users that match the input login and password
      PreparedStatement preparedStatement = DatabaseManager.getDatabaseConnection().prepareStatement("Select * from user where userName=? AND password=?");
      preparedStatement.setString(1, username);
      preparedStatement.setString(2, password);
      ResultSet set = preparedStatement.executeQuery();

      if(set.next()) {
        schedulerApplication.setCurrentUser(new User(username));
        System.out.println("Login Found");
        return true;
      } else {
        System.out.println("Login Not Found");
        return false;
      }
    } catch (SQLException exception) {
      exception.printStackTrace();
      return false;
    }
  }

  public void setSchedulerApplication(SchedulerApplication schedulerApplication) {
    this.schedulerApplication = schedulerApplication;
  }
}
