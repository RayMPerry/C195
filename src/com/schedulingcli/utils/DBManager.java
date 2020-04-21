package com.schedulingcli.utils;

import java.sql.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import com.schedulingcli.entities.*;
import com.schedulingcli.enums.*;

public class DBManager {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static Connection dbConnection = null;

    public static void connect() {
        try {
            String url = "jdbc:mysql://3.227.166.251:3306/U05NJc";
            String user = "U05NJc";
            String password = "53688551183";

            dbConnection = DriverManager.getConnection(url, user, password);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        } catch (SQLException err) {
            System.out.println(err.getMessage());
        } finally {
            String statusMessage = dbConnection != null ? "Connected to" : "Failed to connect to";
            System.out.format("%s the database.%n", statusMessage);
        }
    }

    public static Connection getConnection() {
        return dbConnection;
    }

    public static SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public static void closeConnection() {
        try {
            dbConnection.close();
        } catch (SQLException err) {
            err.printStackTrace();
        }
    }

    private static String getSlotsString(CreationMode creationMode, String[] columnNames) {
        return Arrays
                .asList(columnNames)
                .stream()
                // The following lambda is simpler than declaring a method on class for the sole purpose using it once.
                // This returns a number of "slots" for the SQL statement we're creating.
                .map(columnName -> creationMode == CreationMode.Update ? String.format("%s = ?", columnName) : "?")
                .collect(Collectors.joining(", "));
    }

    private static String buildStatement(CreationMode creationMode, Schema schema, String slotsString) {
        String primaryOperator;

        switch (creationMode) {
            case Update:
                primaryOperator = "UPDATE";
                break;
            case Ensure:
                primaryOperator = "INSERT IF NOT EXISTS INTO";
                break;
            default:
                primaryOperator = "INSERT INTO";
                break;
        }

        String tableName = schema.tableName;
        String valueKeyword = creationMode == CreationMode.Update ? "SET" : "VALUES";
        String statementPreamble = String.format("%s %s %s", primaryOperator, tableName, valueKeyword);
        String statementConclusion = "";

        if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
            slotsString = "(" + slotsString + ")";
        }

        if (creationMode == CreationMode.Update) {
            statementConclusion = String.format("WHERE %s = ?", schema.primaryKeyName);
        }

        return String.format("%s %s %s", statementPreamble, slotsString, statementConclusion);
    }

    public static String quote(String input) {
        return "\'" + input + "\'";
    }

    private static ResultSet retrieve(String tableName, String keyName, String keyValue) {
        ResultSet results = null;
        String sqlQuery;

        try {
            if (keyName != null) {
                sqlQuery = String.format("SELECT * FROM %s WHERE %s = %s", tableName, keyName, keyValue);
            } else {
                sqlQuery = String.format("SELECT * FROM %s", tableName);
            }

            Statement retrievalStatement = dbConnection.createStatement();
            results = retrievalStatement.executeQuery(sqlQuery);
        } catch (SQLException err) {
            err.printStackTrace();
        }

        return results;
    }

    public static ResultSet retrieveWithCondition(String tableName, String primaryKeyName, String primaryKeyValue) {
        return retrieve(tableName, primaryKeyName, primaryKeyValue);
    }

    public static ResultSet retrieveAll(String tableName) {
        return retrieve(tableName, null, null);
    }

    public static ResultSet retrieveAllBetweenDates(String appointmentId, String userId, String start, String end) {
        ResultSet results = null;

        try {
            String inclusiveAppointmentClause = "OR end BETWEEN STR_TO_DATE('%s', '%%Y-%%m-%%d %%H:%%i:%%s') " +
                    "AND STR_TO_DATE('%s', '%%Y-%%m-%%d %%H:%%i:%%s') ";

            String formattedString = "SELECT * FROM appointment " +
                    "WHERE userId = %s " +
                    (appointmentId != null ? "AND appointmentId != %s " : "/* %s */ ") +
                    "AND start BETWEEN STR_TO_DATE('%s', '%%Y-%%m-%%d %%H:%%i:%%s') " +
                    "AND STR_TO_DATE('%s', '%%Y-%%m-%%d %%H:%%i:%%s') " +
                    (appointmentId != null ? inclusiveAppointmentClause : "") +
                    "ORDER BY start ASC";

            String sqlQuery = String.format(
                    formattedString,
                    userId,
                    appointmentId,
                    dateFormat.format(Timestamp.valueOf(start)),
                    dateFormat.format(Timestamp.valueOf(end)),
                    dateFormat.format(Timestamp.valueOf(start)),
                    dateFormat.format(Timestamp.valueOf(end)));

            Statement retrievalStatement = dbConnection.createStatement();
            results = retrievalStatement.executeQuery(sqlQuery);
        } catch (SQLException err) {
            err.printStackTrace();
        }

        return results;
    }

    public static ResultSet getUpcomingAppointments(ReportingMode reportingMode, String userId) {
        TemporalField usField = WeekFields.of(java.util.Locale.US).dayOfWeek();
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime futureDate = currentDate;
        switch (reportingMode) {
            case IMMINENT:
                futureDate = currentDate.plusMinutes(15);
                break;
            case WEEK:
                currentDate = LocalDateTime.now().with(usField, 1);
                futureDate = LocalDateTime.now().with(usField, 7);
                break;
            case MONTH:
                currentDate = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
                futureDate = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth());
                break;
        }

        SimpleDateFormat dumbDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = dumbDateFormat.format(Timestamp.valueOf(currentDate));
        String end = dumbDateFormat.format(Timestamp.valueOf(futureDate));

        return retrieveAllBetweenDates(null, userId, start, end);
    }

    public static ResultSet getNumberOfAppointmentTypes() {
        ResultSet results = null;
        try {
            String sqlQuery = "SELECT type, COUNT(type) as 'number' FROM appointment " +
                    "GROUP BY type";
            Statement retrievalStatement = dbConnection.createStatement();
            results = retrievalStatement.executeQuery(sqlQuery);
        } catch (SQLException err) {
            err.printStackTrace();
        }
        return results;
    }

    public static ResultSet getTopThreeBookedLocations() {
        ResultSet results = null;
        try {
            String sqlQuery = "SELECT location, COUNT(location) as 'number' FROM appointment " +
                    "GROUP BY location " +
                    "ORDER BY number DESC " +
                    "LIMIT 3";
            Statement retrievalStatement = dbConnection.createStatement();
            results = retrievalStatement.executeQuery(sqlQuery);
        } catch (SQLException err) {
            err.printStackTrace();
        }
        return results;
    }

    public static ResultSet getAllAppointmentsByUser() {
        ResultSet results = null;
        try {
            String sqlQuery = "SELECT appt.appointmentId, cstm.customerName, usr.userName, appt.title, " +
                    "appt.description, appt.location, appt.contact, appt.type, appt.url, appt.start, appt.end " +
                    "FROM appointment appt " +
                    "INNER JOIN user usr ON appt.userId = usr.userId " +
                    "INNER JOIN customer cstm ON appt.customerId = cstm.customerId " +
                    "GROUP BY appt.appointmentId, usr.userName " +
                    "ORDER BY appt.start ASC";
            Statement retrievalStatement = dbConnection.createStatement();
            results = retrievalStatement.executeQuery(sqlQuery);
        } catch (SQLException err) {
            err.printStackTrace();
        }
        return results;
    }

    public static int deleteWithCondition(String tableName, String primaryKeyName, String primaryKeyValue) {
        int primaryKeyOfAffectedRecord = -1;

        try {
            String sqlQuery = "DELETE FROM %s WHERE %s = %s";
            Statement retrievalStatement = dbConnection.createStatement();
            retrievalStatement.execute(String.format(sqlQuery, tableName, primaryKeyName, primaryKeyValue));
            primaryKeyOfAffectedRecord = Integer.parseInt(primaryKeyValue);
        } catch (SQLException err) {
            if (StateManager.getValue("itemName").equals("customer")) {
                System.out.println("Customer has pending appointments. Please delete all of their appointments first.");
            } else {
                err.printStackTrace();
            }
        }

        return primaryKeyOfAffectedRecord;
    }

    public static void cleanUpPastAppointments() {
        try {
            String sqlQuery = "DELETE FROM appointment WHERE start < NOW()";
            Statement retrievalStatement = dbConnection.createStatement();
            retrievalStatement.execute(sqlQuery);
        } catch (SQLException err) {
            err.printStackTrace();
        }
    }

    public static ArrayList<String> getEntityIds(String tableName) {
        ArrayList<String> validIds = new ArrayList<>();

        ResultSet results;
        try {
            if (tableName.equals(Schema.Appointment.tableName)) {
                results = DBManager.retrieveWithCondition(tableName, "userId", StateManager.getValue("loggedInUserId"));
            } else {
                results = DBManager.retrieveAll(tableName);
            }

            while (results != null && results.next()) {
                validIds.add(String.valueOf(results.getInt(tableName + "Id")));
            }
        } catch (SQLException err) {
            err.printStackTrace();
            System.out.format("Could not retrieve %ss.%n", tableName);
        }

        return validIds;
    }

    public static boolean areCredentialsValid(String userName, String password) {
        boolean isValidLogin = false;
        ResultSet user = retrieve("user", "userName", quote(userName));
        try {
            isValidLogin = user.next() && user.getString("password").equals(password);
            if (isValidLogin) {
                StateManager.setValue("loggedInUserId", String.valueOf(user.getInt("userId")));
                StateManager.setValue("loggedInUser", user.getString("userName"));
            }
        } catch (SQLException err) {
            err.printStackTrace();
        }
        return isValidLogin;
    }

    public static void createDummyData() {
        // For debugging/testing purposes only.
        String userId = String.valueOf(createUser(StateManager.MAGIC_LOGIN, StateManager.MAGIC_LOGIN, "1"));

        String countryId = String.valueOf(createCountry("USA"));
        String cityId = String.valueOf(createCity("Chicago", countryId));
        String addressId = String.valueOf(createAddress("3238 N Drake Ave", "1", cityId, "60618", "6304027433"));
        String customerId = String.valueOf(createCustomer("My Test", addressId, "1"));

        for (int index = 0; index < 5; index++) {
            createAppointment(
                    customerId,
                    userId,
                    "Appointment " + (index + 1),
                    "This is a description.",
                    "Meeting Room " + (index + 1),
                    "D. Ummy User",
                    "Fake",
                    "http://example.com",
                    String.valueOf(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5))),
                    String.valueOf(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(6))));
        }
    }

    public static int createCustomer(String... values) {
        return createOrUpdate(CreationMode.Create, Schema.Customer, values);
    }

    public static int updateCustomer(String... values) {
        return createOrUpdate(CreationMode.Update, Schema.Customer, values);
    }

    public static int deleteCustomer(String customerId) {
        return deleteWithCondition(Schema.Customer.tableName, Schema.Customer.primaryKeyName, customerId);
    }

    public static int createAddress(String... values) {
        return createOrUpdate(CreationMode.Create, Schema.Address, values);
    }

    public static int updateAddress(String... values) {
        return createOrUpdate(CreationMode.Update, Schema.Address, values);
    }

    public static int deleteAddress(String addressId) {
        return deleteWithCondition(Schema.Address.tableName, Schema.Address.primaryKeyName, addressId);
    }

    public static int createCountry(String... values) {
        return createOrUpdate(CreationMode.Create, Schema.Country, values);
    }

    public static int updateCountry(String... values) {
        return createOrUpdate(CreationMode.Update, Schema.Country, values);
    }

    public static int deleteCountry(String countryId) {
        return deleteWithCondition(Schema.Country.tableName, Schema.Country.primaryKeyName, countryId);
    }

    public static int createCity(String... values) {
        return createOrUpdate(CreationMode.Create, Schema.City, values);
    }

    public static int updateCity(String... values) {
        return createOrUpdate(CreationMode.Update, Schema.City, values);
    }

    public static int deleteCity(String cityId) {
        return deleteWithCondition(Schema.City.tableName, Schema.City.primaryKeyName, cityId);
    }

    public static int createUser(String... values) {
        return createOrUpdate(CreationMode.Create, Schema.User, values);
    }

    public static int updateUser(String... values) {
        return createOrUpdate(CreationMode.Update, Schema.User, values);
    }

    public static int deleteUser(String cityId) {
        return deleteWithCondition(Schema.User.tableName, Schema.User.primaryKeyName, cityId);
    }

    public static int createAppointment(String... values) {
        return createOrUpdate(CreationMode.Create, Schema.Appointment, values);
    }

    public static int updateAppointment(String... values) {
        return createOrUpdate(CreationMode.Update, Schema.Appointment, values);
    }

    public static int deleteAppointment(String appointmentId) {
        return deleteWithCondition(Schema.Appointment.tableName, Schema.Appointment.primaryKeyName, appointmentId);
    }

    private static int createOrUpdate(CreationMode creationMode, Schema schema, String[] values) {
        int affectedRecordPrimaryId = -1;

        switch (schema) {
            case Customer:
                affectedRecordPrimaryId = createOrUpdateCustomer(creationMode, schema, values);
                break;
            case Address:
                affectedRecordPrimaryId = createOrUpdateAddress(creationMode, schema, values);
                break;
            case Country:
                affectedRecordPrimaryId = createOrUpdateCountry(creationMode, schema, values);
                break;
            case City:
                affectedRecordPrimaryId = createOrUpdateCity(creationMode, schema, values);
                break;
            case User:
                affectedRecordPrimaryId = createOrUpdateUser(creationMode, schema, values);
                break;
            case Appointment:
                affectedRecordPrimaryId = createOrUpdateAppointment(creationMode, schema, values);
                break;
        }

        return affectedRecordPrimaryId;
    }

    private static int createOrUpdateAppointment(CreationMode creationMode, Schema schema, String[] values) {
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
		 */

        int primaryKeyOfAffectedRecord = -1;

        String fullStatement = buildStatement(creationMode, schema, getSlotsString(creationMode, schema.columnNames));

        Appointment newAppointment = new Appointment();

        if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
            values = Stream
                    .of(new String[]{"0"}, values)
                    // While it doesn't look like a lambda, this would be a place you could put a lambda.
                    // Not using the long form because I can use the shorthand to be more "efficient".
                    // The corresponding lambda would be: (value) -> Stream.of(value)
                    .flatMap(Stream::of)
                    .toArray(String[]::new);
        }

        if (creationMode == CreationMode.Update) {
            try {
                ResultSet results = retrieveWithCondition(schema.tableName, schema.primaryKeyName, values[0]);
                if (results != null && results.next()) newAppointment = new Appointment(results);
                results.close();
            } catch (SQLException err) {
                err.printStackTrace();
            }
        }

        try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement, Statement.RETURN_GENERATED_KEYS)) {
            for (int index = 0; index < values.length; index++) {
                if (index == 0) newAppointment.setAppointmentId(Integer.parseInt(values[0]));
                if (index == 1) newAppointment.setCustomerId(Integer.parseInt(values[1]));
                if (index == 2) newAppointment.setUserId(Integer.parseInt(values[2]));
                if (index == 3) newAppointment.setTitle(values[3]);
                if (index == 4) newAppointment.setDescription(values[4]);
                if (index == 5) newAppointment.setLocation(values[5]);
                if (index == 6) newAppointment.setContact(values[6]);
                if (index == 7) newAppointment.setType(values[7]);
                if (index == 8) newAppointment.setUrl(values[8]);
                if (index == 9) newAppointment.setStart(Timestamp.valueOf(values[9]));
                if (index == 10) newAppointment.setEnd(Timestamp.valueOf(values[10]));
            }

            updateStatement.setInt(1, newAppointment.getAppointmentId());
            updateStatement.setInt(2, newAppointment.getCustomerId());
            updateStatement.setInt(3, newAppointment.getUserId());
            updateStatement.setString(4, newAppointment.getTitle());
            updateStatement.setString(5, newAppointment.getDescription());
            updateStatement.setString(6, newAppointment.getLocation());
            updateStatement.setString(7, newAppointment.getContact());
            updateStatement.setString(8, newAppointment.getType());
            updateStatement.setString(9, newAppointment.getUrl());
            updateStatement.setTimestamp(10, newAppointment.getStart());
            updateStatement.setTimestamp(11, newAppointment.getEnd());
            updateStatement.setTimestamp(12, newAppointment.getCreateDate());
            updateStatement.setString(13, newAppointment.getCreatedBy());
            updateStatement.setTimestamp(14, newAppointment.getLastUpdate());
            updateStatement.setString(15, newAppointment.getLastUpdateBy());

            if (creationMode == CreationMode.Update) {
                updateStatement.setInt(16, newAppointment.getAppointmentId());
            }

            updateStatement.executeUpdate();
            ResultSet results = updateStatement.getGeneratedKeys();
            if (results != null && results.next()) {
                primaryKeyOfAffectedRecord = results.getInt(1);
            } else if (results != null) {
                primaryKeyOfAffectedRecord = newAppointment.getAppointmentId();
            }
        } catch (SQLException err) {
            err.printStackTrace();
            System.err.println("Could not update all values as provided. Refusing to proceed.");
        }

        return primaryKeyOfAffectedRecord;
    }

    private static int createOrUpdateCustomer(CreationMode creationMode, Schema schema, String[] values) {
		/*
			customerId | int(10) AI PK
			customerName | varchar(45)
			addressId | int(10)
			active | tinyint(1)
			createDate | datetime
			createdBy | varchar(40)
			lastUpdate | timestamp
			lastUpdateBy | varchar(40)
		*/

        int primaryKeyOfAffectedRecord = -1;

        String fullStatement = buildStatement(creationMode, schema, getSlotsString(creationMode, schema.columnNames));

        Customer newCustomer = new Customer();

        if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
            values = Stream
                    .of(new String[]{"0"}, values)
                    // While it doesn't look like a lambda, this would be a place you could put a lambda.
                    // Not using the long form because I can use the shorthand to be more "efficient".
                    // The corresponding lambda would be: (value) -> Stream.of(value)
                    .flatMap(Stream::of)
                    .toArray(String[]::new);
        }

        if (creationMode == CreationMode.Update) {
            try {
                ResultSet results = retrieveWithCondition(schema.tableName, schema.primaryKeyName, values[0]);
                if (results != null && results.next()) newCustomer = new Customer(results);
                results.close();
            } catch (SQLException err) {
                err.printStackTrace();
            }
        }

        try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement, Statement.RETURN_GENERATED_KEYS)) {
            for (int index = 0; index < values.length; index++) {
                if (index == 0) newCustomer.setCustomerId(Integer.parseInt(values[0]));
                if (index == 1) newCustomer.setCustomerName(values[1]);
                if (index == 2) newCustomer.setAddressId(Integer.parseInt(values[2]));
                if (index == 3) newCustomer.setActive(Integer.parseInt(values[3]));
            }

            updateStatement.setInt(1, newCustomer.getCustomerId());
            updateStatement.setString(2, newCustomer.getCustomerName());
            updateStatement.setInt(3, newCustomer.getAddressId());
            updateStatement.setInt(4, newCustomer.getActive());
            updateStatement.setTimestamp(5, newCustomer.getCreateDate());
            updateStatement.setString(6, newCustomer.getCreatedBy());
            updateStatement.setTimestamp(7, newCustomer.getLastUpdate());
            updateStatement.setString(8, newCustomer.getLastUpdateBy());

            if (creationMode == CreationMode.Update) {
                updateStatement.setInt(9, newCustomer.getCustomerId());
            }

            updateStatement.executeUpdate();
            ResultSet results = updateStatement.getGeneratedKeys();
            if (results != null && results.next()) {
                primaryKeyOfAffectedRecord = results.getInt(1);
            } else if (results != null) {
                primaryKeyOfAffectedRecord = newCustomer.getCustomerId();
            }
        } catch (SQLException err) {
            err.printStackTrace();
            System.err.println("Could not update all values as provided. Refusing to proceed.");
        }

        return primaryKeyOfAffectedRecord;
    }

    private static int createOrUpdateAddress(CreationMode creationMode, Schema schema, String[] values) {
		/*
			addressId | int(10) AI PK
			address | varchar(50)
			address2 | varchar(50)
			cityId | int(10)
			postalCode | varchar(10)
			phone | varchar(20)
			createDate | datetime
			createdBy | varchar(40)
			lastUpdate | timestamp
			lastUpdateBy | varchar(40)
		 */

        int primaryKeyOfAffectedRecord = -1;

        String fullStatement = buildStatement(creationMode, schema, getSlotsString(creationMode, schema.columnNames));

        Address newAddress = new Address();

        if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
            values = Stream
                    .of(new String[]{"0"}, values)
                    // While it doesn't look like a lambda, this would be a place you could put a lambda.
                    // Not using the long form because I can use the shorthand to be more "efficient".
                    // The corresponding lambda would be: (value) -> Stream.of(value)
                    .flatMap(Stream::of)
                    .toArray(String[]::new);
        }

        if (creationMode == CreationMode.Update) {
            try {
                ResultSet results = retrieveWithCondition(schema.tableName, schema.primaryKeyName, values[0]);
                if (results != null && results.next()) newAddress = new Address(results);
                results.close();
            } catch (SQLException err) {
                err.printStackTrace();
            }
        }

        try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement, Statement.RETURN_GENERATED_KEYS)) {
            for (int index = 0; index < values.length; index++) {
                if (index == 0) newAddress.setAddressId(Integer.parseInt(values[0]));
                if (index == 1) newAddress.setAddress(values[1]);
                if (index == 2) newAddress.setAddress2(values[2]);
                if (index == 3) newAddress.setCityId(Integer.parseInt(values[3]));
                if (index == 4) newAddress.setPostalCode(values[4]);
                if (index == 5) newAddress.setPhone(values[5]);
            }

            updateStatement.setInt(1, newAddress.getAddressId());
            updateStatement.setString(2, newAddress.getAddress());
            updateStatement.setString(3, newAddress.getAddress2());
            updateStatement.setInt(4, newAddress.getCityId());
            updateStatement.setString(5, newAddress.getPostalCode());
            updateStatement.setString(6, newAddress.getPhone());
            updateStatement.setTimestamp(7, newAddress.getCreateDate());
            updateStatement.setString(8, newAddress.getCreatedBy());
            updateStatement.setTimestamp(9, newAddress.getLastUpdate());
            updateStatement.setString(10, newAddress.getLastUpdateBy());

            if (creationMode == CreationMode.Update) {
                updateStatement.setInt(11, newAddress.getAddressId());
            }

            updateStatement.executeUpdate();
            ResultSet results = updateStatement.getGeneratedKeys();
            if (results != null && results.next()) {
                primaryKeyOfAffectedRecord = results.getInt(1);
            } else if (results != null) {
                primaryKeyOfAffectedRecord = newAddress.getAddressId();
            }
        } catch (SQLException err) {
            err.printStackTrace();
            System.err.println("Could not update all values as provided. Refusing to proceed.");
        }

        return primaryKeyOfAffectedRecord;
    }

    private static int createOrUpdateCity(CreationMode creationMode, Schema schema, String[] values) {
		/*
			cityId | int(10) AI PK
			city | varchar(50)
			countryId | int(10)
			createDate | datetime
			createdBy | varchar(40)
			lastUpdate | timestamp
			lastUpdateBy | varchar(40)
		 */

        int primaryKeyOfAffectedRecord = -1;

        String fullStatement = buildStatement(creationMode, schema, getSlotsString(creationMode, schema.columnNames));

        City newCity = new City();

        if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
            values = Stream
                    .of(new String[]{"0"}, values)
                    // While it doesn't look like a lambda, this would be a place you could put a lambda.
                    // Not using the long form because I can use the shorthand to be more "efficient".
                    // The corresponding lambda would be: (value) -> Stream.of(value)
                    .flatMap(Stream::of)
                    .toArray(String[]::new);
        }

        if (creationMode == CreationMode.Update) {
            try {
                ResultSet results = retrieveWithCondition(schema.tableName, schema.primaryKeyName, values[0]);
                if (results != null && results.next()) newCity = new City(results);
                results.close();
            } catch (SQLException err) {
                err.printStackTrace();
            }
        }

        try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement, Statement.RETURN_GENERATED_KEYS)) {
            for (int index = 0; index < values.length; index++) {
                if (index == 0) newCity.setCityId(Integer.parseInt(values[0]));
                if (index == 1) newCity.setCity(values[1]);
                if (index == 2) newCity.setCountryId(Integer.parseInt(values[2]));
            }

            updateStatement.setInt(1, newCity.getCityId());
            updateStatement.setString(2, newCity.getCity());
            updateStatement.setInt(3, newCity.getCountryId());
            updateStatement.setTimestamp(4, newCity.getCreateDate());
            updateStatement.setString(5, newCity.getCreatedBy());
            updateStatement.setTimestamp(6, newCity.getLastUpdate());
            updateStatement.setString(7, newCity.getLastUpdateBy());

            if (creationMode == CreationMode.Update) {
                updateStatement.setInt(8, newCity.getCityId());
            }

            updateStatement.executeUpdate();
            ResultSet results = updateStatement.getGeneratedKeys();
            if (results != null && results.next()) {
                primaryKeyOfAffectedRecord = results.getInt(1);
            } else if (results != null) {
                primaryKeyOfAffectedRecord = newCity.getCityId();
            }
        } catch (SQLException err) {
            err.printStackTrace();
            System.err.println("Could not update all values as provided. Refusing to proceed.");
        }

        return primaryKeyOfAffectedRecord;
    }

    private static int createOrUpdateCountry(CreationMode creationMode, Schema schema, String[] values) {
		/*
			countryId | int(10) AI PK
			country | varchar(50)
			createDate | datetime
			createdBy | varchar(40)
			lastUpdate | timestamp
			lastUpdateBy | varchar(40)
		 */

        int primaryKeyOfAffectedRecord = -1;

        String fullStatement = buildStatement(creationMode, schema, getSlotsString(creationMode, schema.columnNames));

        Country newCountry = new Country();

        if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
            values = Stream
                    .of(new String[]{"0"}, values)
                    // While it doesn't look like a lambda, this would be a place you could put a lambda.
                    // Not using the long form because I can use the shorthand to be more "efficient".
                    // The corresponding lambda would be: (value) -> Stream.of(value)
                    .flatMap(Stream::of)
                    .toArray(String[]::new);
        }

        if (creationMode == CreationMode.Update) {
            try {
                ResultSet results = retrieveWithCondition(schema.tableName, schema.primaryKeyName, values[0]);
                if (results != null && results.next()) newCountry = new Country(results);
                results.close();
            } catch (SQLException err) {
                err.printStackTrace();
            }
        }

        try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement, Statement.RETURN_GENERATED_KEYS)) {
            for (int index = 0; index < values.length; index++) {
                if (index == 0) newCountry.setCountryId(Integer.parseInt(values[0]));
                if (index == 1) newCountry.setCountry(values[1]);
            }

            updateStatement.setInt(1, newCountry.getCountryId());
            updateStatement.setString(2, newCountry.getCountry());
            updateStatement.setTimestamp(3, newCountry.getCreateDate());
            updateStatement.setString(4, newCountry.getCreatedBy());
            updateStatement.setTimestamp(5, newCountry.getLastUpdate());
            updateStatement.setString(6, newCountry.getLastUpdateBy());

            if (creationMode == CreationMode.Update) {
                updateStatement.setInt(7, newCountry.getCountryId());
            }

            updateStatement.executeUpdate();
            ResultSet results = updateStatement.getGeneratedKeys();
            if (results != null && results.next()) {
                primaryKeyOfAffectedRecord = results.getInt(1);
            } else if (results != null) {
                primaryKeyOfAffectedRecord = newCountry.getCountryId();
            }
        } catch (SQLException err) {
            err.printStackTrace();
            System.err.println("Could not update all values as provided. Refusing to proceed.");
        }

        return primaryKeyOfAffectedRecord;
    }

    private static int createOrUpdateUser(CreationMode creationMode, Schema schema, String[] values) {
		/*
			userId | int(11) AI PK
			userName | varchar(50)
			password | varchar(50)
			active | tinyint(4)
			createDate | datetime
			createdBy | varchar(40)
			lastUpdate | timestamp
			lastUpdateBy | varchar(40)
		 */

        int primaryKeyOfAffectedRecord = -1;

        String fullStatement = buildStatement(creationMode, schema, getSlotsString(creationMode, schema.columnNames));

        User newUser = new User();

        if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
            values = Stream
                    .of(new String[]{"0"}, values)
                    // While it doesn't look like a lambda, this would be a place you could put a lambda.
                    // Not using the long form because I can use the shorthand to be more "efficient".
                    // The corresponding lambda would be: (value) -> Stream.of(value)
                    .flatMap(Stream::of)
                    .toArray(String[]::new);
        }

        if (creationMode == CreationMode.Update) {
            try {
                ResultSet results = retrieveWithCondition(schema.tableName, schema.primaryKeyName, values[0]);
                if (results != null && results.next()) newUser = new User(results);
                results.close();
            } catch (SQLException err) {
                err.printStackTrace();
            }
        }

        try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement, Statement.RETURN_GENERATED_KEYS)) {
            for (int index = 0; index < values.length; index++) {
                if (index == 0) newUser.setUserId(Integer.parseInt(values[0]));
                if (index == 1) newUser.setUserName(values[1]);
                if (index == 2) newUser.setPassword(values[2]);
                if (index == 3) newUser.setActive(Integer.parseInt(values[3]));
            }

            updateStatement.setInt(1, newUser.getUserId());
            updateStatement.setString(2, newUser.getUserName());
            updateStatement.setString(3, newUser.getPassword());
            updateStatement.setInt(4, newUser.getActive());
            updateStatement.setTimestamp(5, newUser.getCreateDate());
            updateStatement.setString(6, newUser.getCreatedBy());
            updateStatement.setTimestamp(7, newUser.getLastUpdate());
            updateStatement.setString(8, newUser.getLastUpdateBy());

            if (creationMode == CreationMode.Update) {
                updateStatement.setInt(9, newUser.getUserId());
            }

            updateStatement.executeUpdate();
            ResultSet results = updateStatement.getGeneratedKeys();
            if (results != null && results.next()) {
                primaryKeyOfAffectedRecord = results.getInt(1);
            } else if (results != null) {
                primaryKeyOfAffectedRecord = newUser.getUserId();
            }
        } catch (SQLException err) {
            err.printStackTrace();
            System.err.println("Could not update all values as provided. Refusing to proceed.");
        }

        return primaryKeyOfAffectedRecord;
    }
}