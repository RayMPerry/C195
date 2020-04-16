package com.schedulingcli.utils;

import java.sql.*;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import com.schedulingcli.enums.*;

public class DBManager {
	private static Connection dbConnection = null;

	public static void closeConnection() {
		try {
			dbConnection.close();
		} catch (SQLException err) {
			err.printStackTrace();
		}
	}
	
    public static void connect() {
		try {
			String url = "jdbc:mysql://3.227.166.251:3306/U05NJc";
			String user = "U05NJc";
			String password = "53688551183";
	
			dbConnection = DriverManager.getConnection(url, user, password);
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

	private static ResultSet retrieve(String whereCondition, String tableName, String keyName, String keyValue) {
		ResultSet results = null;
		String sqlQuery;

		try {
			if (whereCondition.equals("")) {
				sqlQuery = String.format("SELECT * FROM %s", tableName);
			} else {
				sqlQuery = String.format("SELECT * FROM %s " + whereCondition, tableName, keyName, keyValue);
			}

			Statement retrievalStatement = dbConnection.createStatement();
			results = retrievalStatement.executeQuery(sqlQuery);
		} catch (SQLException err) {
			err.printStackTrace();
		}

		return results;
	}

	public static boolean areCredentialsValid(String userName, String password) {
		boolean isValidLogin = false;
		ResultSet user = retrieve("WHERE %s = '%s'", "user", "userName", userName);
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

	public static ResultSet retrieveOne(String tableName, String primaryKeyName, String primaryKeyValue) {
		return retrieve("WHERE %s = %s", tableName, primaryKeyName, primaryKeyValue);
	}

	public static ResultSet retrieveAll(String tableName, String primaryKeyName, String primaryKeyValue) {
		return retrieve("", tableName, primaryKeyName, primaryKeyValue);
	}

	public static int deleteOne(String tableName, String primaryKeyName, String primaryKeyValue) {
		int primaryKeyOfAffectedRecord = -1;

		try {
			String sqlQuery = "DELETE FROM %s WHERE %s = %s";
			Statement retrievalStatement = dbConnection.createStatement();
			retrievalStatement.execute(String.format(sqlQuery, tableName, primaryKeyName, primaryKeyValue));
			primaryKeyOfAffectedRecord = Integer.parseInt(primaryKeyValue);
		} catch (SQLException err) {
			err.printStackTrace();
		}

		return primaryKeyOfAffectedRecord;
	}

	private static int createOrUpdateCustomer(CreationMode creationMode, Schema schema, String[] values) {
		int primaryKeyOfAffectedRecord = -1;

		String[] columnNames = new String[] {
				schema.primaryKeyName,
				"customerName",
				"addressId",
				"active",
				"createDate",
				"createdBy",
				"lastUpdate",
				"lastUpdateBy"
		};

		String fullStatement = buildStatement(creationMode, schema, getSlotsString(creationMode, columnNames));

		int customerId = 0;
		String customerName = "";
		int addressId = 1;
		int active = 0;
		Timestamp lastUpdate = new java.sql.Timestamp(System.currentTimeMillis());
		String lastUpdateBy = StateManager.getValue("loggedInUser");
		Timestamp createDate = lastUpdate;
		String createdBy = lastUpdateBy;

		if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
			values = Stream
					.of(new String[] { String.valueOf(customerId) }, values)
					// While it doesn't look like a lambda, this would be a place you could put a lambda.
					// Not using the long form because I can use the shorthand to be more "efficient".
					// The corresponding lambda would be: (value) -> Stream.of(value)
					.flatMap(Stream::of)
					.toArray(String[]::new);
		}

		if (creationMode == CreationMode.Update) {
			try {
				ResultSet results = retrieveOne(schema.tableName, schema.primaryKeyName, values[0]);

				while (results.next()) {
					customerId = results.getInt(schema.primaryKeyName);
					customerName = results.getString("customerName");
					addressId = results.getInt("addressId");
					active = results.getInt("active");
					createDate = results.getTimestamp("createDate");
					createdBy = results.getString("createdBy");
				}

				results.close();
			} catch (SQLException err) {
				err.printStackTrace();
			}
		}

		try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement, Statement.RETURN_GENERATED_KEYS)) {
			for (int index = 0; index < values.length; index++) {
				if (index == 0) customerId = Integer.parseInt(values[0]);
				if (index == 1) customerName = values[1];
				if (index == 2) addressId = Integer.parseInt(values[2]);
				if (index == 3) active = Integer.parseInt(values[3]);
			}

			updateStatement.setInt(1, customerId);
			updateStatement.setString(2, customerName);
			updateStatement.setInt(3, addressId);
			updateStatement.setInt(4, active);
			updateStatement.setTimestamp(5, createDate);
			updateStatement.setString(6, createdBy);
			updateStatement.setTimestamp(7, lastUpdate);
			updateStatement.setString(8, lastUpdateBy);

			if (creationMode == CreationMode.Update) {
				updateStatement.setInt(9, customerId);
			}

			updateStatement.executeUpdate();
			ResultSet results = updateStatement.getGeneratedKeys();
			if (results.next()) primaryKeyOfAffectedRecord = results.getInt(1);
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

		String[] columnNames = new String[] {
				schema.primaryKeyName,
				"address",
				"address2",
				"cityId",
				"postalCode",
				"phone",
				"createDate",
				"createdBy",
				"lastUpdate",
				"lastUpdateBy"
		};

		String fullStatement = buildStatement(creationMode, schema, getSlotsString(creationMode, columnNames));

		int addressId = 0;
		String address = "";
		String address2 = "";
		int cityId = 0;
		String postalCode = "";
		String phone = "";
		Timestamp lastUpdate = new java.sql.Timestamp(System.currentTimeMillis());
		String lastUpdateBy = StateManager.getValue("loggedInUser");
		Timestamp createDate = lastUpdate;
		String createdBy = lastUpdateBy;

		if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
			values = Stream
					.of(new String[] { String.valueOf(addressId) }, values)
					// While it doesn't look like a lambda, this would be a place you could put a lambda.
					// Not using the long form because I can use the shorthand to be more "efficient".
					// The corresponding lambda would be: (value) -> Stream.of(value)
					.flatMap(Stream::of)
					.toArray(String[]::new);
		}

		if (creationMode == CreationMode.Update) {
			try {
				ResultSet results = retrieveOne(schema.tableName, schema.primaryKeyName, values[0]);

				while (results.next()) {
					addressId = results.getInt(schema.primaryKeyName);
					address = results.getString("address");
					address2 = results.getString("address2");
					cityId = results.getInt("cityId");
					postalCode = results.getString("postalCode");
					phone = results.getString("phone");
					createDate = results.getTimestamp("createDate");
					createdBy = results.getString("createdBy");
				}

				results.close();
			} catch (SQLException err) {
				err.printStackTrace();
			}
		}

		try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement, Statement.RETURN_GENERATED_KEYS)) {
			for (int index = 0; index < values.length; index++) {
				if (index == 0) addressId = Integer.parseInt(values[0]);
				if (index == 1) address = values[1];
				if (index == 2) address2 = values[2];
				if (index == 3) cityId = Integer.parseInt(values[3]);
				if (index == 4) postalCode = values[4];
				if (index == 5) phone = values[5];
			}

			updateStatement.setInt(1, addressId);
			updateStatement.setString(2, address);
			updateStatement.setString(3, address2);
			updateStatement.setInt(4, cityId);
			updateStatement.setString(5, postalCode);
			updateStatement.setString(6, phone);
			updateStatement.setTimestamp(7, createDate);
			updateStatement.setString(8, createdBy);
			updateStatement.setTimestamp(9, lastUpdate);
			updateStatement.setString(10, lastUpdateBy);

			if (creationMode == CreationMode.Update) {
				updateStatement.setInt(11, addressId);
			}

			updateStatement.executeUpdate();
			ResultSet results = updateStatement.getGeneratedKeys();
			if (results.next()) primaryKeyOfAffectedRecord = results.getInt(1);
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

		String[] columnNames = new String[] {
				schema.primaryKeyName,
				"country",
				"createDate",
				"createdBy",
				"lastUpdate",
				"lastUpdateBy"
		};

		String fullStatement = buildStatement(creationMode, schema, getSlotsString(creationMode, columnNames));

		int countryId = 0;
		String country = "";
		Timestamp lastUpdate = new java.sql.Timestamp(System.currentTimeMillis());
		String lastUpdateBy = StateManager.getValue("loggedInUser");
		Timestamp createDate = lastUpdate;
		String createdBy = lastUpdateBy;

		if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
			values = Stream
					.of(new String[] { String.valueOf(countryId) }, values)
					// While it doesn't look like a lambda, this would be a place you could put a lambda.
					// Not using the long form because I can use the shorthand to be more "efficient".
					// The corresponding lambda would be: (value) -> Stream.of(value)
					.flatMap(Stream::of)
					.toArray(String[]::new);
		}

		if (creationMode == CreationMode.Update) {
			try {
				ResultSet results = retrieveOne(schema.tableName, schema.primaryKeyName, values[0]);

				while (results.next()) {
					countryId = results.getInt(schema.primaryKeyName);
					country = results.getString("country");
					createDate = results.getTimestamp("createDate");
					createdBy = results.getString("createdBy");
				}

				results.close();
			} catch (SQLException err) {
				err.printStackTrace();
			}
		}

		try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement, Statement.RETURN_GENERATED_KEYS)) {
			for (int index = 0; index < values.length; index++) {
				if (index == 0) countryId = Integer.parseInt(values[0]);
				if (index == 1) country = values[1];
			}

			updateStatement.setInt(1, countryId);
			updateStatement.setString(2, country);
			updateStatement.setTimestamp(3, createDate);
			updateStatement.setString(4, createdBy);
			updateStatement.setTimestamp(5, lastUpdate);
			updateStatement.setString(6, lastUpdateBy);

			if (creationMode == CreationMode.Update) {
				updateStatement.setInt(7, countryId);
			}

			updateStatement.executeUpdate();
			ResultSet results = updateStatement.getGeneratedKeys();
			if (results.next()) primaryKeyOfAffectedRecord = results.getInt(1);
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

		String[] columnNames = new String[] {
				schema.primaryKeyName,
				"city",
				"countryId",
				"createDate",
				"createdBy",
				"lastUpdate",
				"lastUpdateBy"
		};

		String fullStatement = buildStatement(creationMode, schema, getSlotsString(creationMode, columnNames));

		int cityId = 0;
		String city = "";
		int countryId = 0;
		Timestamp lastUpdate = new java.sql.Timestamp(System.currentTimeMillis());
		String lastUpdateBy = StateManager.getValue("loggedInUser");
		Timestamp createDate = lastUpdate;
		String createdBy = lastUpdateBy;

		if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
			values = Stream
					.of(new String[] { String.valueOf(cityId) }, values)
					// While it doesn't look like a lambda, this would be a place you could put a lambda.
					// Not using the long form because I can use the shorthand to be more "efficient".
					// The corresponding lambda would be: (value) -> Stream.of(value)
					.flatMap(Stream::of)
					.toArray(String[]::new);
		}

		if (creationMode == CreationMode.Update) {
			try {
				ResultSet results = retrieveOne(schema.tableName, schema.primaryKeyName, values[0]);

				while (results.next()) {
					cityId = results.getInt(schema.primaryKeyName);
					city = results.getString("city");
					countryId = results.getInt("countryId");
					createDate = results.getTimestamp("createDate");
					createdBy = results.getString("createdBy");
				}

				results.close();
			} catch (SQLException err) {
				err.printStackTrace();
			}
		}

		try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement, Statement.RETURN_GENERATED_KEYS)) {
			for (int index = 0; index < values.length; index++) {
				if (index == 0) cityId = Integer.parseInt(values[0]);
				if (index == 1) city = values[1];
				if (index == 2) countryId = Integer.parseInt(values[2]);
			}

			updateStatement.setInt(1, cityId);
			updateStatement.setString(2, city);
			updateStatement.setInt(3, countryId);
			updateStatement.setTimestamp(4, createDate);
			updateStatement.setString(5, createdBy);
			updateStatement.setTimestamp(6, lastUpdate);
			updateStatement.setString(7, lastUpdateBy);

			if (creationMode == CreationMode.Update) {
				updateStatement.setInt(8, countryId);
			}

			updateStatement.executeUpdate();
			ResultSet results = updateStatement.getGeneratedKeys();
			if (results.next()) primaryKeyOfAffectedRecord = results.getInt(1);
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

		String[] columnNames = new String[] {
				schema.primaryKeyName,
				"userName",
				"password",
				"active",
				"createDate",
				"createdBy",
				"lastUpdate",
				"lastUpdateBy"
		};

		String fullStatement = buildStatement(creationMode, schema, getSlotsString(creationMode, columnNames));

		int userId = 0;
		String userName = "";
		String password = "";
		int active = 0;
		Timestamp lastUpdate = new java.sql.Timestamp(System.currentTimeMillis());
		String lastUpdateBy = StateManager.getValue("loggedInUser");
		Timestamp createDate = lastUpdate;
		String createdBy = lastUpdateBy;

		if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
			values = Stream
					.of(new String[] { String.valueOf(userId) }, values)
					// While it doesn't look like a lambda, this would be a place you could put a lambda.
					// Not using the long form because I can use the shorthand to be more "efficient".
					// The corresponding lambda would be: (value) -> Stream.of(value)
					.flatMap(Stream::of)
					.toArray(String[]::new);
		}

		if (creationMode == CreationMode.Update) {
			try {
				ResultSet results = retrieveOne(schema.tableName, schema.primaryKeyName, values[0]);

				while (results.next()) {
					userId = results.getInt(schema.primaryKeyName);
					userName = results.getString("userName");
					password = results.getString("password");
					active = results.getInt("active");
					createDate = results.getTimestamp("createDate");
					createdBy = results.getString("createdBy");
				}

				results.close();
			} catch (SQLException err) {
				err.printStackTrace();
			}
		}

		try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement, Statement.RETURN_GENERATED_KEYS)) {
			for (int index = 0; index < values.length; index++) {
				if (index == 0) userId = Integer.parseInt(values[0]);
				if (index == 1) userName = values[1];
				if (index == 2) password = values[2];
				if (index == 3) active = Integer.parseInt(values[3]);
			}

			updateStatement.setInt(1, userId);
			updateStatement.setString(2, userName);
			updateStatement.setString(3, password);
			updateStatement.setInt(4, active);
			updateStatement.setTimestamp(5, createDate);
			updateStatement.setString(6, createdBy);
			updateStatement.setTimestamp(7, lastUpdate);
			updateStatement.setString(8, lastUpdateBy);

			if (creationMode == CreationMode.Update) {
				updateStatement.setInt(9, userId);
			}

			updateStatement.executeUpdate();
			ResultSet results = updateStatement.getGeneratedKeys();
			if (results.next()) primaryKeyOfAffectedRecord = results.getInt(1);
		} catch (SQLException err) {
			err.printStackTrace();
			System.err.println("Could not update all values as provided. Refusing to proceed.");
		}

		return primaryKeyOfAffectedRecord;
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

		String[] columnNames = new String[] {
				schema.primaryKeyName,
				"customerId",
				"userId",
				"title",
				"description",
				"location",
				"contact",
				"type",
				"url",
				"start",
				"end",
				"createDate",
				"createdBy",
				"lastUpdate",
				"lastUpdateBy"
		};

		String fullStatement = buildStatement(creationMode, schema, getSlotsString(creationMode, columnNames));

		int appointmentId = 0;
		int customerId = 0;
		int userId = 0;
		String title = "";
		String description = "";
		String location = "";
		String contact = "";
		String type = "";
		String url = "";
		Timestamp start = new java.sql.Timestamp(System.currentTimeMillis());
		Timestamp end = start;
		Timestamp lastUpdate = end;
		String lastUpdateBy = StateManager.getValue("loggedInUser");
		Timestamp createDate = lastUpdate;
		String createdBy = lastUpdateBy;

		if (creationMode == CreationMode.Create || creationMode == CreationMode.Ensure) {
			values = Stream
					.of(new String[] { String.valueOf(appointmentId) }, values)
					// While it doesn't look like a lambda, this would be a place you could put a lambda.
					// Not using the long form because I can use the shorthand to be more "efficient".
					// The corresponding lambda would be: (value) -> Stream.of(value)
					.flatMap(Stream::of)
					.toArray(String[]::new);
		}

		if (creationMode == CreationMode.Update) {
			try {
				ResultSet results = retrieveOne(schema.tableName, schema.primaryKeyName, values[0]);

				while (results.next()) {
					appointmentId = results.getInt(schema.primaryKeyName);
					customerId = results.getInt("customerId");
					userId = results.getInt("userId");
					title = results.getString("title");
					description = results.getString("description");
					location = results.getString("location");
					contact = results.getString("contact");
					type = results.getString("type");
					url = results.getString("url");
					start = results.getTimestamp("start");
					end = results.getTimestamp("end");
					createDate = results.getTimestamp("createDate");
					createdBy = results.getString("createdBy");
				}

				results.close();
			} catch (SQLException err) {
				err.printStackTrace();
			}
		}

		try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement, Statement.RETURN_GENERATED_KEYS)) {
			for (int index = 0; index < values.length; index++) {
				if (index == 0) appointmentId = Integer.parseInt(values[0]);
				if (index == 1) customerId = Integer.parseInt(values[1]);
				if (index == 2) userId = Integer.parseInt(values[2]);
				if (index == 3) title = values[3];
				if (index == 4) description = values[4];
				if (index == 5) location = values[5];
				if (index == 6) contact = values[6];
				if (index == 7) type = values[7];
				if (index == 8) url = values[8];
				if (index == 9) start = java.sql.Timestamp.valueOf(values[9]);
				if (index == 10) end = java.sql.Timestamp.valueOf(values[10]);
			}

			updateStatement.setInt(1, appointmentId);
			updateStatement.setInt(2, customerId);
			updateStatement.setInt(3, userId);
			updateStatement.setString(4, title);
			updateStatement.setString(5, description);
			updateStatement.setString(6, location);
			updateStatement.setString(7, contact);
			updateStatement.setString(8, type);
			updateStatement.setString(9, url);
			updateStatement.setTimestamp(10, start);
			updateStatement.setTimestamp(11, end);
			updateStatement.setTimestamp(12, createDate);
			updateStatement.setString(13, createdBy);
			updateStatement.setTimestamp(14, lastUpdate);
			updateStatement.setString(15, lastUpdateBy);

			if (creationMode == CreationMode.Update) {
				updateStatement.setInt(16, appointmentId);
			}

			updateStatement.executeUpdate();
			ResultSet results = updateStatement.getGeneratedKeys();
			if (results.next()) primaryKeyOfAffectedRecord = results.getInt(1);
		} catch (SQLException err) {
			err.printStackTrace();
			System.err.println("Could not update all values as provided. Refusing to proceed.");
		}

		return primaryKeyOfAffectedRecord;
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
	
	public static int createCustomer(String... values) {
		return createOrUpdate(CreationMode.Create, Schema.Customer, values);
	}

	public static int updateCustomer(String... values) {
		return createOrUpdate(CreationMode.Update, Schema.Customer, values);
	}

	public static int deleteCustomer(String customerId) {
		return deleteOne(Schema.Customer.tableName, Schema.Customer.primaryKeyName, customerId);
	}

	public static int createAddress(String... values) {
		return createOrUpdate(CreationMode.Create, Schema.Address, values);
	}

	public static int updateAddress(String... values) {
		return createOrUpdate(CreationMode.Update, Schema.Address, values);
	}

	public static int deleteAddress(String addressId) {
		return deleteOne(Schema.Address.tableName, Schema.Address.primaryKeyName, addressId);
	}

	public static int createCountry(String... values) {
		return createOrUpdate(CreationMode.Create, Schema.Country, values);
	}

	public static int updateCountry(String... values) {
		return createOrUpdate(CreationMode.Update, Schema.Country, values);
	}

	public static int deleteCountry(String countryId) {
		return deleteOne(Schema.Country.tableName, Schema.Country.primaryKeyName, countryId);
	}

	public static int createCity(String... values) {
		return createOrUpdate(CreationMode.Create, Schema.City, values);
	}

	public static int updateCity(String... values) {
		return createOrUpdate(CreationMode.Update, Schema.City, values);
	}

	public static int deleteCity(String cityId) {
		return deleteOne(Schema.City.tableName, Schema.City.primaryKeyName, cityId);
	}

	public static int createUser(String... values) {
		return createOrUpdate(CreationMode.Create, Schema.User, values);
	}

	public static int updateUser(String... values) {
		return createOrUpdate(CreationMode.Update, Schema.User, values);
	}

	public static int deleteUser(String cityId) {
		return deleteOne(Schema.User.tableName, Schema.User.primaryKeyName, cityId);
	}

	public static int createAppointment(String... values) {
		return createOrUpdate(CreationMode.Create, Schema.Appointment, values);
	}

	public static int updateAppointment(String... values) {
		return createOrUpdate(CreationMode.Update, Schema.Appointment, values);
	}

	public static int deleteAppointment(String appointmentId) {
		return deleteOne(Schema.Appointment.tableName, Schema.Appointment.primaryKeyName, appointmentId);
	}
}