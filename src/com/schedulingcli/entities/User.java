package com.schedulingcli.entities;

import com.schedulingcli.enums.Schema;
import com.schedulingcli.utils.StateManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class User {
    private int userId = 0;
    private String userName = "";
    private String password = "";
    private int active = 0;
    private Timestamp createDate = new java.sql.Timestamp(System.currentTimeMillis());
    private String createdBy = StateManager.getValue("loggedInUser");
    private Timestamp lastUpdate = createDate;
    private String lastUpdateBy = createdBy;

    public User() {}

    public User(ResultSet results) throws SQLException {
        this.setUserId(results.getInt(Schema.User.primaryKeyName));
        this.setUserName(results.getString("userName"));
        this.setPassword(results.getString("password"));
        this.setActive(results.getInt("active"));
        this.setCreateDate(results.getTimestamp("createDate"));
        this.setCreatedBy(results.getString("createdBy"));
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
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
