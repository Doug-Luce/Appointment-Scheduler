package com.dougluce.model;


public class Customer {
  private  String fullName;
  private  String address1;
  private  String address2;
  private  String country;
  private  City city;
  private  String zipCode;
  private  String phoneNumber;
  private  String ID;

  public Customer() {
  }

  public Customer(String fullName, String ID) {
    this.fullName = fullName;
    this.ID = ID;
  }

  public Customer(String fullName, String address1, String address2, String country, City city, String zipCode, String phoneNumber, String ID) {
    this.fullName = fullName;
    this.address1 = address1;
    this.address2 = address2;
    this.country = country;
    this.city = city;
    this.zipCode = zipCode;
    this.phoneNumber = phoneNumber;
    this.ID = ID;
  }

  public Customer(String fullName, String address1, String address2, String country, City city, String zipCode, String phoneNumber) {
    this.fullName = fullName;
    this.address1 = address1;
    this.address2 = address2;
    this.country = country;
    this.city = city;
    this.zipCode = zipCode;
    this.phoneNumber = phoneNumber;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getAddress1() {
    return address1;
  }

  public void setAddress1(String address1) {
    this.address1 = address1;
  }

  public String getAddress2() {
    return address2;
  }

  public void setAddress2(String address2) {
    this.address2 = address2;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }


  public City getCity() {
    return city;
  }

  public void setCity(City city) {
    this.city = city;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getID() {
    return ID;
  }

  public void setID(String ID) {
    this.ID = ID;
  }

  @Override
  public String toString() {
    return "Customer{" +
        "fullName='" + fullName + '\'' +
        ", address1='" + address1 + '\'' +
        ", address2='" + address2 + '\'' +
        ", country='" + country + '\'' +
        ", city=" + city +
        ", zipCode='" + zipCode + '\'' +
        ", phoneNumber='" + phoneNumber + '\'' +
        ", ID=" + ID +
        '}';
  }
}

