package com.schedulingcli.entities;

import com.schedulingcli.enums.Schema;
import com.schedulingcli.utils.StateManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Country {
    private int countryId = 0;
    private String country = "";
    private Timestamp createDate = new java.sql.Timestamp(System.currentTimeMillis());
    private String createdBy = StateManager.getValue("loggedInUser");
    private Timestamp lastUpdate = createDate;
    private String lastUpdateBy = createdBy;

    public Country() {}

    public Country(ResultSet results) throws SQLException {
        this.setCountryId(results.getInt(Schema.Country.primaryKeyName));
        this.setCountry(results.getString("country"));
        this.setCreateDate(results.getTimestamp("createDate"));
        this.setCreatedBy(results.getString("createdBy"));
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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
