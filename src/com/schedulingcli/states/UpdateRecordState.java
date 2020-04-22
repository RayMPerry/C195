package com.schedulingcli.states;

import com.schedulingcli.entities.Appointment;
import com.schedulingcli.entities.Customer;
import com.schedulingcli.enums.Schema;
import com.schedulingcli.enums.ScreenCode;
import com.schedulingcli.utils.DBManager;
import com.schedulingcli.utils.InputManager;
import com.schedulingcli.utils.ScreenManager;
import com.schedulingcli.utils.StateManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

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
    private static String itemName = "";
    private static Appointment currentAppointment = new Appointment();
    private static Customer currentCustomer = new Customer();

    private static boolean isEditingRecord = false;

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

    public static String[] getStartAndEndDates() {
        String start = "";
        String end = "";
        boolean allDatesValid = false;
        while (!allDatesValid) {
            System.out.print("Appointment start: ");
            start = InputManager.waitForValidDateInput(DBManager.getDateFormat());
            System.out.print("Appointment end: ");
            end = InputManager.waitForValidDateInput(DBManager.getDateFormat());
            if (start.equals(InputManager.cancelCommand) || end.equals(InputManager.cancelCommand)) {
                break;
            }

            try {
                boolean isValidDateRange = Timestamp.valueOf(end).after(Timestamp.valueOf(start));
                if (!isValidDateRange) throw new Exception("End date must be in the future. Resetting.");

                ResultSet results = DBManager.retrieveAllBetweenDates(
                        String.valueOf(currentAppointment.getAppointmentId()),
                        StateManager.getValue("loggedInUserId"),
                        start,
                        end);

                if (results != null && results.next()) throw new Exception("Appointments would overlap. Resetting.");

                if (!InputManager.areDatesDuringBusinessHours(start, end)) {
                    throw new Exception(String.format("Appointment is outside of business hours (%s - %s).",
                            StateManager.BUSINESS_OPEN,
                            StateManager.BUSINESS_CLOSE));
                }

                allDatesValid = true;
            } catch (Exception err) {
                start = "";
                end = "";
                System.out.println(err.getMessage());
            }
        }

        return new String[]{start, end};
    }

    public static String promptForForeignKey(String foreignTableName) {
        String[] validIds = DBManager.getEntityIds(foreignTableName).toArray(String[]::new);
        InputManager.setValidResponsesWithArray(validIds);
        InputManager.addToValidResponses("0");
        System.out.format(ScreenManager.getScreen(ScreenCode.SPECIFY_RECORD), foreignTableName, "use (or \"0\" to create a " + foreignTableName + ")");
        String response = InputManager.waitForValidInput();
        String foreignId = response;
        if (response.equals("0")) {
            if (foreignTableName.equals("customer")) foreignId = createCustomer();
            if (foreignTableName.equals("address")) foreignId = createAddress();
        }

        return foreignId;
    }

    public static String createAppointment() {
        String appointmentId = "-1";

        try {
            String userId = StateManager.getValue("loggedInUserId");
            String customerId = promptForForeignKey(Schema.Customer.tableName);
            String[] values = InputManager.aggregateResponses(formatListOfPrompts(
                    "title",
                    "description",
                    "location",
                    "contact",
                    "type",
                    "url"));

            String[] dates = getStartAndEndDates();

            if (isEditingRecord) {
                appointmentId = String.valueOf(DBManager.updateAppointment(
                        String.valueOf(currentAppointment.getAppointmentId()),
                        customerId,
                        userId,
                        values[0],
                        values[1],
                        values[2],
                        values[3],
                        values[4],
                        values[5],
                        dates[0],
                        dates[1]));
            } else {
                appointmentId = String.valueOf(DBManager.createAppointment(
                        customerId,
                        userId,
                        values[0],
                        values[1],
                        values[2],
                        values[3],
                        values[4],
                        values[5],
                        dates[0],
                        dates[1]));
            }


            System.out.format("%nFinished. ID: %s.%n%n", appointmentId);
        } catch (Exception err) {
            err.printStackTrace();
            System.out.println("Could not create or update appointment. Aborting.");
        }

        return appointmentId;
    }

    public static String createCustomer() {
        String[] values;
        String customerId = "-1";
        try {
            String addressId = promptForForeignKey(Schema.Address.tableName);
            values = InputManager.aggregateResponses(formatListOfPrompts("customerName"), true);
            if (isEditingRecord) {
                customerId = String.valueOf(DBManager.updateCustomer(
                        String.valueOf(currentCustomer.getCustomerId()),
                        values[0],
                        addressId,
                        "1",
                        DBManager.getDateFormat().format(currentCustomer.getCreateDate()),
                        currentCustomer.getCreatedBy(),
                        DBManager.getDateFormat().format(currentCustomer.getLastUpdate()),
                        currentCustomer.getLastUpdateBy()));
            } else {
                customerId = String.valueOf(DBManager.createCustomer(values[0],
                        addressId,
                        "1",
                        DBManager.getDateFormat().format(currentCustomer.getCreateDate()),
                        currentCustomer.getCreatedBy(),
                        DBManager.getDateFormat().format(currentCustomer.getLastUpdate()),
                        currentCustomer.getLastUpdateBy()));
            }

            System.out.format("%nFinished. ID: %s.%n%n", customerId);
        } catch (Exception err) {
            err.printStackTrace();
            System.out.println("Could not create or update customer. Aborting.");
        }

        return customerId;
    }

    public static String createAddress() {
        String[] values;
        String addressId = "-1";
        try {
            values = InputManager.aggregateResponses(formatListOfPrompts("country"), true);
            String countryId = String.valueOf(DBManager.createCountry(values[0]));

            values = InputManager.aggregateResponses(formatListOfPrompts("city"), true);
            String cityId = String.valueOf(DBManager.createCity(values[0], countryId));

            values = InputManager.aggregateResponses(formatListOfPrompts("address", "address2", "postalCode", "phone"), true);
            addressId = String.valueOf(DBManager.createAddress(values[0], values[1], cityId, values[2], values[3]));

            System.out.format("%nFinished. ID: %s.%n%n", addressId);
        } catch (Exception err) {
            err.printStackTrace();
            System.out.println("Could not create or update address. Aborting.");
        }

        return addressId;
    }

    public static void setup() {
        isEditingRecord = StateManager.getCurrentScreen() == ScreenCode.EDIT_RECORD;
        itemName = StateManager.getValue("itemName");
        if (!isEditingRecord) return;

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

    public static void run() {
        switch (itemName) {
            case "appointment":
                createAppointment();
                break;
            case "customer":
                createCustomer();
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
