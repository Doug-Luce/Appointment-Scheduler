package com.dougluce.controller;

import com.dougluce.SchedulerApplication;
import com.dougluce.model.City;
import com.dougluce.model.Customer;
import com.dougluce.utility.CustomerViewState;
import com.dougluce.utility.DatabaseManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomersController implements Initializable {

  @FXML
  private TableView<Customer> tableView;

  @FXML
  private TableColumn<Customer, String> fullName;

  @FXML
  private TableColumn<Customer, String> phoneNumber;

  @FXML
  private ComboBox cityComboBox;

  @FXML
  private TextField name;

  @FXML
  private TextField address1;

  @FXML
  private TextField address2;

  @FXML
  private TextField country;

  @FXML
  private TextField zipCode;

  @FXML
  private TextField pNumber;

  @FXML
  private TextField customerId;

  @FXML
  private Button add;

  @FXML
  private Button edit;

  @FXML
  private Button delete;

  @FXML
  private Button save;

  @FXML
  private Button cancel;

  private CustomerViewState currentState = CustomerViewState.DEFAULT;
  private String username;
  private SchedulerApplication schedulerApplication;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Set up stringConverter to display name of city objects in combo box
    cityComboBox.setConverter(new StringConverter<City>() {

      @Override
      public String toString(City object) {
        return object.getCity();
      }

      @Override
      public City fromString(String string) {
        return (City) cityComboBox.getValue();
      }
    });

    // Event handler for combo box, automatically set country id
    cityComboBox.setOnAction((event -> {
      City selectedCity = (City) cityComboBox.getSelectionModel().getSelectedItem();
      int countryId = selectedCity.getCountryID();

      String countryText;
      if (countryId == 1) {
        countryText = "United States";
      } else {
        countryText = "England";
      }
      country.setText(countryText);
    }));

    getAndPopulateCityComboBox();
    getAndPopulateTableView();

    // Event handler when tableview is clicked
    handleTableviewClick();

    // Disable save and cancel button
    cancel.setDisable(true);
    save.setDisable(true);

    // Disable form input until edit is clicked
    isFormEditable(false);

    // Disable ID input
    customerId.setEditable(false);

    // Disable Country input
    country.setEditable(false);
    // Set username
    Platform.runLater(() -> {
      username = schedulerApplication.getCurrentUser().getUserName();
    });

  }

  private void handleTableviewClick() {
    // Using a lambda here to decrease verbose click handler
    tableView.setOnMousePressed(event -> {
      if (event.isPrimaryButtonDown()) {
        System.out.println(tableView.getSelectionModel().getSelectedItem());
        populateCustomerForm(tableView.getSelectionModel().getSelectedItem());
      }
    });
  }

  private void clearForm() {
    name.clear();
    address1.clear();
    address2.clear();
    country.clear();
    zipCode.clear();
    pNumber.clear();
    customerId.clear();
    cityComboBox.getSelectionModel().selectFirst();
  }

  public void getAndPopulateCityComboBox() {
    ObservableList<City> cities = FXCollections.observableArrayList();

    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement("SELECT city, cityId, countryId from city");
      ResultSet set = statement.executeQuery();

      while (set.next()) {
        cities.add(new City(set.getString("city.city"), set.getInt("city.cityId"), set.getInt("city.countryId")));
      }
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
    cityComboBox.setItems(cities);
  }

  public void getAndPopulateTableView() {
    ObservableList<Customer> customerData = FXCollections.observableArrayList();

    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement("SELECT customer.customerId, customer.customerName, address.address, address.address2, address.postalCode, city.cityId, city.city, country.country, country.countryId, address.phone FROM customer, address, city, country WHERE customer.addressId = address.addressId AND address.cityId = city.cityId AND city.countryId = country.countryId ORDER BY customer.customerName");
      ResultSet set = statement.executeQuery();

      // For every item that set returns from the database add a new Customer
      while(set.next()) {
        customerData.add(new Customer(set.getString("customerName"), set.getString("address"),
            set.getString("address2"), set.getString("country"), new City(set.getString("city"),
            set.getInt("cityId"), set.getInt("countryId")),
            set.getString("postalCode"), set.getString("phone"), set.getString("customerId")));
      }

      // Set values of fullName and phoneNumber columns
      fullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
      phoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

      // Populate the tableView with customers
      tableView.setItems(customerData);

    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }

  @FXML
  public void handleDelete() {
    Customer selectedCustomer = tableView.getSelectionModel().getSelectedItem();
    if (selectedCustomer != null) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Delete Confirmation");
      alert.setContentText("Are you sure you wish to delete " + selectedCustomer.getFullName() + "?");
      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == ButtonType.OK) {
        deleteCustomer(selectedCustomer);
        getAndPopulateTableView();
      }
    } else {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("No Customer Selected");
      alert.setContentText("Please select a user in the table to delete");
      alert.showAndWait();
    }
    clearForm();
    currentState = CustomerViewState.DEFAULT;
  }

  @FXML
  private void handleAdd() {
    currentState = CustomerViewState.ADD;
    clearForm();
    customerId.setText("Auto Generated");
    setEditMode(true);
  }

  @FXML
  private void handleEdit() {
    // TODO Show user alert when customer is not selected
    if(tableView.getSelectionModel().getSelectedItem() == null) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("No Customer Selected");
      alert.setContentText("Select a customer from the table to edit.");
      alert.showAndWait();
    } else {
      currentState = CustomerViewState.EDIT;
      setEditMode(true);
    }
  }

  @FXML
  private void handleCancel() {
    setEditMode(false);
    isFormEditable(false);
    currentState = CustomerViewState.DEFAULT;
  }

  @FXML
  private void handleSave() {
    System.out.println(currentState);
    if (currentState == CustomerViewState.EDIT) {
      updateCustomer(getCustomerFromForm());
      currentState = CustomerViewState.DEFAULT;
    }
    if (currentState == CustomerViewState.ADD) {
      addCustomer(getCustomerFromForm());
      clearForm();
      setEditMode(false);
      currentState = CustomerViewState.DEFAULT;
    }
  }

  private Customer getCustomerFromForm() {
    City cityFromForm = (City) cityComboBox.getSelectionModel().getSelectedItem();
    Customer customerFromForm = new Customer();
    if (currentState == CustomerViewState.ADD) {
      customerFromForm = new Customer(name.getText(), address1.getText(), address2.getText(), country.getText(),
          cityFromForm, zipCode.getText(), pNumber.getText());
    }

    if (currentState == CustomerViewState.EDIT) {
      customerFromForm = new Customer(name.getText(), address1.getText(), address2.getText(), country.getText(),
          cityFromForm, zipCode.getText(), pNumber.getText(), customerId.getText());
    }

    return customerFromForm;
  }

  private void setEditMode(Boolean bool) {
    isFormEditable(true);
    tableView.setDisable(bool);
    add.setDisable(bool);
    edit.setDisable(bool);
    delete.setDisable(bool);
    save.setDisable(!bool);
    cancel.setDisable(!bool);
  }

  private void isFormEditable(Boolean bool) {
    name.setEditable(bool);
    address1.setEditable(bool);
    address2.setEditable(bool);
    zipCode.setEditable(bool);
    pNumber.setEditable(bool);
  }

  private void addCustomer(Customer customer) {
    try {
      PreparedStatement addAddressStatement = DatabaseManager.getDatabaseConnection().prepareStatement("INSERT INTO address (address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)", Statement.RETURN_GENERATED_KEYS);
      addAddressStatement.setString(1, customer.getAddress1());
      addAddressStatement.setString(2, customer.getAddress2());
      addAddressStatement.setInt(3, customer.getCity().getCityID());
      addAddressStatement.setString(4, customer.getZipCode());
      addAddressStatement.setString(5, customer.getPhoneNumber());
      addAddressStatement.setString(6, username);
      addAddressStatement.setString(7, username);

      int affectedAddressRows = addAddressStatement.executeUpdate();
      int generatedAddressId = -100;
      if (affectedAddressRows == 0) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Error Adding Customer " + customer.getID());
        errorAlert.setContentText("Couldn't add customer name: " + customer.getFullName());
        errorAlert.showAndWait();
      }

      try {
        ResultSet generatedKeys = addAddressStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
          generatedAddressId = generatedKeys.getInt(1);
          System.out.println(" Generated Address ID is: " + generatedAddressId);
        }
      } catch (SQLException exception) {
        exception.printStackTrace();
      }

      if (generatedAddressId < 0) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Error Adding Customer");
        errorAlert.setContentText("Couldn't set ID");
        errorAlert.showAndWait();
        return;
      }

      PreparedStatement addCustomerStatement = DatabaseManager.getDatabaseConnection().prepareStatement("INSERT INTO customer (customerName, addressId, active, createDate, createdBy, lastUpdate, lastUpdateBy)" + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)");
      addCustomerStatement.setString(1,customer.getFullName());
      addCustomerStatement.setInt(2, generatedAddressId);
      addCustomerStatement.setInt(3,1);
      addCustomerStatement.setString(4, username);
      addCustomerStatement.setString(5, username);

      int affectedCustomerRows = addCustomerStatement.executeUpdate();
      if (affectedCustomerRows == 0) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Error Adding Customer");
        errorAlert.setContentText("Couldn't add Customer");
        errorAlert.showAndWait();
        return;
      }
      getAndPopulateTableView();
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }

  private void updateCustomer(Customer customer) {

    System.out.println(customer);
    try {

      PreparedStatement updateAddressStatement = DatabaseManager.getDatabaseConnection().prepareStatement("UPDATE customer, address, city,country SET address.address = ?, address.address2 = ?, address.postalCode = ?, address.phone = ?, address.cityId = ?, address.lastUpdate = CURRENT_TIMESTAMP, address.lastUpdateBy = ? WHERE customer.customerId = ? AND customer.addressId = address.addressId AND address.cityId = city.cityId AND city.countryId = country.countryId");
      int cityId = customer.getCity().getCityID();
      int customerId = Integer.parseInt(customer.getID());
      updateAddressStatement.setString(1, customer.getAddress1());
      updateAddressStatement.setString(2, customer.getAddress2());
      updateAddressStatement.setString(3, customer.getZipCode());
      updateAddressStatement.setString(4, customer.getPhoneNumber());
      updateAddressStatement.setInt(5, cityId);
      updateAddressStatement.setString(6, username);
      updateAddressStatement.setInt(7, customerId);

      int i = updateAddressStatement.executeUpdate();
      System.out.println("updateAddressStatement updated " + i + " records");

      PreparedStatement updateCustomerStatement = DatabaseManager.getDatabaseConnection().prepareStatement("UPDATE customer, city, address SET customerName = ?, customer.lastUpdateBy = ?, customer.lastUpdate = CURRENT_TIMESTAMP WHERE customer.customerId = ? AND address.cityId = city.cityId AND address.addressId = customer.addressId");
      
      updateCustomerStatement.setString(1, customer.getFullName());
      updateCustomerStatement.setString(2, username);
      updateCustomerStatement.setString(3, customer.getID());

      int x = updateCustomerStatement.executeUpdate();
      System.out.println("updateCustomerStatement updated " + x + " records");

      getAndPopulateTableView();
      populateCustomerForm(customer);
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
    setEditMode(false);
    isFormEditable(false);
  }

  private void deleteCustomer(Customer customer) {
    try {
      PreparedStatement statement = DatabaseManager.getDatabaseConnection().prepareStatement("DELETE customer.*, address.* from customer, address WHERE customer.customerId = ? AND customer.addressId = address.addressId");
      statement.setString(1, customer.getID());
      statement.executeUpdate();
    } catch (SQLException exception) {
      exception.printStackTrace();
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setHeaderText("Error Deleting Customer ID " + customer.getID());
      errorAlert.setContentText("Couldn't delete customer " + customer.getFullName());
      errorAlert.showAndWait();
    }
  }

  private void populateCustomerForm(Customer customer) {
    name.setText(customer.getFullName());
    address1.setText(customer.getAddress1());
    address2.setText(customer.getAddress2());
    country.setText(customer.getCountry());
    zipCode.setText(customer.getZipCode());
    pNumber.setText(customer.getPhoneNumber());
    customerId.setText(customer.getID());
    cityComboBox.getSelectionModel().select(customer.getCity().getCityID() - 1);
  }

  public void setSchedulerApplication(SchedulerApplication schedulerApplication) {
    this.schedulerApplication = schedulerApplication;
  }
}
