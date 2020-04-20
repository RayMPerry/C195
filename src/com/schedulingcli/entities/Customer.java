package com.schedulingcli.entities;

import com.schedulingcli.enums.Schema;
import com.schedulingcli.utils.StateManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Customer {
    private int customerId = 0;
    private String customerName = "";
    private int addressId = 0;
    private int active = 0;
    private Timestamp createDate = new java.sql.Timestamp(System.currentTimeMillis());
    private String createdBy = StateManager.getValue("loggedInUser");
    private Timestamp lastUpdate = createDate;
    private String lastUpdateBy = createdBy;

    public Customer() {}

    public Customer(ResultSet results) throws SQLException {
        this.setCustomerId(results.getInt(Schema.Customer.primaryKeyName));
        this.setCustomerName(results.getString("customerName"));
        this.setAddressId(results.getInt("addressId"));
        this.setActive(results.getInt("active"));
        this.setCreateDate(results.getTimestamp("createDate"));
        this.setCreatedBy(results.getString("createdBy"));
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
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
