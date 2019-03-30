package com.dougluce.model;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Appointment {
  private String title;
  private String description;
  private String contact;
  private String startTime;
  private String endTime;
  private Customer customer;
  private String appointmentId;

  public Appointment() {
  }

  public Appointment(String title, String description, String startTime, String endTime, String contact, Customer customer, String appointmentId) {
    this.title = title;
    this.description = description;
    this.contact = contact;
    this.startTime = startTime;
    this.endTime = endTime;
    this.customer = customer;
    this.appointmentId = appointmentId;
  }

  public Appointment(String title, String description, String contact, String startTime, String endTime, Customer customer) {
    this.title = title;
    this.description = description;
    this.contact = contact;
    this.startTime = startTime;
    this.endTime = endTime;
    this.customer = customer;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getContact() {
    return contact;
  }

  public void setContact(String contact) {
    this.contact = contact;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  public String getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(String appointmentId) {
    this.appointmentId = appointmentId;
  }

  @Override
  public String toString() {
    return "Appointment{" +
        "title='" + title + '\'' +
        ", description='" + description + '\'' +
        ", contact='" + contact + '\'' +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", customer=" + customer +
        ", appointmentId='" + appointmentId + '\'' +
        '}';
  }
}

