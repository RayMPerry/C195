package com.schedulingcli.entities;

import com.schedulingcli.enums.Schema;
import com.schedulingcli.utils.StateManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Appointment {
    private int appointmentId = 0;
    private int customerId = 0;
    private int userId = 0;
    private String title = "";
    private String description = "";
    private String contact = "";
    private String location = "";
    private String url = "";
    private String type = "";
    private Timestamp start = new java.sql.Timestamp(System.currentTimeMillis());
    private Timestamp end = start;
    private Timestamp createDate = start;
    private String createdBy = StateManager.getValue("loggedInUser");
    private Timestamp lastUpdate = createDate;
    private String lastUpdateBy = createdBy;

    public Appointment() {}

    public Appointment(ResultSet results) throws SQLException {
        this.setAppointmentId(results.getInt(Schema.Appointment.primaryKeyName));
        this.setCustomerId(results.getInt("customerId"));
        this.setUserId(results.getInt("userId"));
        this.setTitle(results.getString("title"));
        this.setDescription(results.getString("description"));
        this.setLocation(results.getString("location"));
        this.setContact(results.getString("contact"));
        this.setType(results.getString("type"));
        this.setUrl(results.getString("url"));
        this.setStart(results.getTimestamp("start"));
        this.setEnd(results.getTimestamp("end"));
        this.setCreateDate(results.getTimestamp("createDate"));
        this.setCreatedBy(results.getString("createdBy"));
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getStart() {
        return start;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public Timestamp getEnd() {
        return end;
    }

    public void setEnd(Timestamp end) {
        this.end = end;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getLastUpdateBy() {
        return lastUpdateBy;
    }

    public void setLastUpdateBy(String lastUpdateBy) {
        this.lastUpdateBy = lastUpdateBy;
    }
}
