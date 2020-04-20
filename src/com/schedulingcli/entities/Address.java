package com.schedulingcli.entities;

import com.schedulingcli.enums.Schema;
import com.schedulingcli.utils.StateManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Address {
    private int addressId = 0;
    private String address = "";
    private String address2 = "";
    private int cityId = 0;
    private String postalCode = "";
    private String phone = "";
    private Timestamp createDate = new java.sql.Timestamp(System.currentTimeMillis());
    private String createdBy = StateManager.getValue("loggedInUser");
    private Timestamp lastUpdate = createDate;
    private String lastUpdateBy = createdBy;

    public Address() {}

    public Address(ResultSet results) throws SQLException {
        this.setAddressId(results.getInt(Schema.Address.primaryKeyName));
        this.setAddress(results.getString("address"));
        this.setAddress2(results.getString("address2"));
        this.setCityId(results.getInt("cityId"));
        this.setPostalCode(results.getString("postalCode"));
        this.setPhone(results.getString("phone"));
        this.setCreateDate(results.getTimestamp("createDate"));
        this.setCreatedBy(results.getString("createdBy"));
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
