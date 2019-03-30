package com.dougluce.model;

public class City {

  private int cityID;
  private int countryID;
  private String city;

  public City() {
  }

  public City(String city, int cityID, int countryID) {
    this.cityID = cityID;
    this.countryID = countryID;
    this.city = city;
  }

  public int getCityID() {
    return cityID;
  }

  public void setCityID(int cityID) {
    this.cityID = cityID;
  }

  public int getCountryID() {
    return countryID;
  }

  public void setCountryID(int countryID) {
    this.countryID = countryID;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }


}
