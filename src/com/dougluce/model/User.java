package com.dougluce.model;

public class User {
  private int userId;
  private String userName;
  private String password;

  public User(String userName) {
    this.userName = userName;
  }

  public int getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
