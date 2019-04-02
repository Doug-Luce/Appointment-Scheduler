package com.dougluce.controller;

import com.dougluce.SchedulerApplication;
import com.dougluce.utility.DatabaseManager;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ReportsByMonthController implements Initializable {

  @FXML
  private PieChart pieChart;
  private ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
  private final String getDescriptionCounts = "SELECT appointment.description, appointment.start, COUNT(*) AS \"count\" FROM appointment WHERE appointment.start BETWEEN NOW() and DATE_ADD(NOW(), INTERVAL 1 MONTH) GROUP BY appointment.description;";

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    getPieChartData();
    pieChart.setTitle("Appointment Types by Month");
    pieChart.dataProperty().setValue(pieChartData);
  }

  private void getPieChartData() {
    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement(getDescriptionCounts);
      ResultSet resultSet = statement.executeQuery();
      while(resultSet.next()) {
        System.out.println(resultSet.getString("description"));
        System.out.println(resultSet.getString("count"));
        pieChartData.add(new PieChart.Data(resultSet.getString("description"), resultSet.getInt("count")));
      }

      // Using a lambda to efficiently filter through the list of pieChartData and add value to the legend.
      pieChartData.forEach(data ->
        data.nameProperty().bind(Bindings.concat(data.getName(), " ", data.pieValueProperty().intValue()))
      );


    } catch (SQLException exception) {

    }

  }
}