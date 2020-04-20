package com.schedulingcli.states;

import com.schedulingcli.entities.Appointment;
import com.schedulingcli.entities.Customer;
import com.schedulingcli.enums.Schema;
import com.schedulingcli.enums.ScreenCode;
import com.schedulingcli.utils.DBManager;
import com.schedulingcli.utils.InputManager;
import com.schedulingcli.utils.ScreenManager;
import com.schedulingcli.utils.StateManager;

import javax.swing.plaf.nimbus.State;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*
appointmentId | int(10) AI PK
customerId | int(10)
userId | int(11)
title | varchar(255)
description | text
location | text
contact | text
type | text
url | varchar(255)
start | datetime
end | datetime
createDate | datetime
createdBy | varchar(40)
lastUpdate | timestamp
lastUpdateBy | varchar(40)

customerId | int(10) AI PK
customerName | varchar(45)
addressId | int(10) FK
active | tinyint(1)
createDate | datetime
createdBy | varchar(40)
lastUpdate | timestamp
lastUpdateBy | varchar(40)
 */

public class UpdateRecordState implements BasicState {
    private static String itemName = StateManager.getValue("itemName");
    private static Appointment currentAppointment = new Appointment();
    private static Customer currentCustomer = new Customer();

    private static boolean isEditingRecord = StateManager.getCurrentScreen() == ScreenCode.EDIT_RECORD;
    private static List<String> editOrder;

    public static String getFieldValue(String fieldName) {
        String fieldValue = "";

        switch (fieldName) {
            case "title":
                fieldValue = currentAppointment.getTitle();
                break;
            case "description":
                fieldValue = currentAppointment.getDescription();
                break;
            case "contact":
                fieldValue = currentAppointment.getContact();
                break;
            case "location":
                fieldValue = currentAppointment.getLocation();
                break;
            case "url":
                fieldValue = currentAppointment.getUrl();
                break;
            case "type":
                fieldValue = currentAppointment.getType();
                break;
            case "start":
                fieldValue = DBManager.getDateFormat().format(currentAppointment.getStart());
                break;
            case "end":
                fieldValue = DBManager.getDateFormat().format(currentAppointment.getEnd());
                break;
            case "customerName":
                fieldValue = currentCustomer.getCustomerName();
                break;
            case "address":
                break;
            case "address2":
                break;
            case "city":
                break;
            case "phone":
                break;
            case "postalCode":
                break;
            case "country":
                break;
        }

        return fieldValue;
    }

    public static String[] formatListOfPrompts(String... fieldNames) {
        ArrayList<String> prompts = new ArrayList<>();
        for (String fieldName : fieldNames) {
            String prompt = String.format(ScreenManager.getScreen(ScreenCode.CREATE_RECORD), fieldName);
            if (isEditingRecord) {
                String fieldValue = getFieldValue(fieldName);
                prompt = String.format(ScreenManager.getScreen(ScreenCode.EDIT_RECORD), fieldName, fieldValue) + prompt;
            }
            prompts.add(prompt);
        }
        return prompts.toArray(String[]::new);
    }

    public static void setup() {
        if (isEditingRecord) {
            String[] validIds = DBManager.getEntityIds(itemName).toArray(String[]::new);
            InputManager.setValidResponsesWithArray(validIds);
            System.out.format(ScreenManager.getScreen(ScreenCode.SPECIFY_RECORD), itemName, "edit");
            String response = InputManager.waitForValidInput();

            try {
                ResultSet results;
                if (itemName.equals(Schema.Appointment.tableName)) {
                    results = DBManager.retrieveWithCondition(itemName, Schema.Appointment.primaryKeyName, response);
                    if (results != null && results.next()) currentAppointment = new Appointment(results);
                }

                if (itemName.equals(Schema.Customer.tableName)) {
                    results = DBManager.retrieveWithCondition(itemName, Schema.Customer.primaryKeyName, response);
                    if (results != null && results.next()) currentCustomer = new Customer(results);
                }
            } catch (SQLException err) {
                err.printStackTrace();
            }
        }
    }

    public static void run() {
        String[] values;
        switch (itemName) {
            case "customer":
                try {
                    values = InputManager.aggregateResponses(formatListOfPrompts("country"));
                    String countryId = String.valueOf(DBManager.createCountry(values[0]));

                    values = InputManager.aggregateResponses(formatListOfPrompts("city"));
                    String cityId = String.valueOf(DBManager.createCity(values[0], countryId));

                    values = InputManager.aggregateResponses(formatListOfPrompts("address", "address2", "postalCode", "phone"));
                    String addressId = String.valueOf(DBManager.createAddress(values[0], values[1], cityId, values[2], values[3]));

                    values = InputManager.aggregateResponses(formatListOfPrompts("customerName"));
                    String customerId = String.valueOf(DBManager.createCustomer(values[0], addressId, "1"));

                    System.out.format("%nFinished. ID: %s.%n%n", customerId);
                } catch (Exception err) {
                    System.out.println("Returning to main view.");
                }
                break;
            default:
                System.out.println("Not yet.");
                break;
        }

        teardown();
    }

    public static void draw() {

    }

    public static void teardown() {
        StateManager.setCurrentScreen(ScreenCode.MAIN_VIEW);
    }
}
