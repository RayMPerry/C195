package com.schedulingcli.utils;

import java.sql.*;

import java.util.Date;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import com.schedulingcli.enums.*;

public class DbManager {
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
				.map(columnName -> creationMode == CreationMode.Update ? String.format("%s = ?", columnName) : "?")
				.collect(Collectors.joining(", "));
	}

	private static String buildStatement(CreationMode creationMode, SchemaName schemaName, String slotsString) {
		String primaryOperator = creationMode == CreationMode.Update ? "UPDATE" : "INSERT INTO";
		String tableName = schemaName.tableName;
		String valueKeyword = creationMode == CreationMode.Update ? "SET" : "VALUES";
		String statementPreamble = String.format("%s %s %s", primaryOperator, tableName, valueKeyword);
		String statementConclusion = "";

		if (creationMode == CreationMode.Create) {
			slotsString = "(" + slotsString + ")";
		}
		
		if (creationMode == CreationMode.Update) {
			statementConclusion = String.format("WHERE %s = ?", schemaName.primaryKeyName);
		}
		
		return String.format("%s %s %s", statementPreamble, slotsString, statementConclusion);
	}

	public static ResultSet retrieveRow(String tableName, String primaryKeyName, String primaryKeyValue) {
		ResultSet results = null;

		try {
			String sqlQuery = "SELECT * FROM %s WHERE %s = %s";
			Statement retrievalStatement = dbConnection.createStatement();
			results = retrievalStatement.executeQuery(String.format(sqlQuery, tableName, primaryKeyName, primaryKeyValue));
		} catch (SQLException err) {
			err.printStackTrace();
		}

		return results;
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
	
	private static int createOrUpdate(CreationMode creationMode, SchemaName schemaName, String... values) {
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

		String[] columnNames;
		int primaryKeyOfNewRecord = -1;
		
		switch (schemaName) {
			case Customer:
				columnNames = new String[] {
					schemaName.primaryKeyName,
					"customerName",
					"addressId",
					"active",
					"createDate",
					"createdBy",
					"lastUpdate",
					"lastUpdateBy"
				};

				String fullStatement = buildStatement(creationMode, schemaName, getSlotsString(creationMode, columnNames));

				int customerId = 0;
				String customerName = "";
				int addressId = 1;
				int active = 0;
				Timestamp lastUpdate = new java.sql.Timestamp(System.currentTimeMillis());
				String lastUpdateBy = StateManager.getValue("loggedInUser");
				Timestamp createDate = lastUpdate;
				String createdBy = lastUpdateBy;

				if (creationMode == CreationMode.Create) {
					values = Stream
							.of(new String[] { String.valueOf(customerId) }, values)
							.flatMap(Stream::of)
							.toArray(String[]::new);
				}

				if (creationMode == CreationMode.Update) {
					try {
						ResultSet results = retrieveRow(schemaName.tableName, schemaName.primaryKeyName, values[0]);
						
						while (results.next()) {
							customerId = results.getInt(schemaName.primaryKeyName);
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
					if (results.next()) primaryKeyOfNewRecord = results.getInt(1);
				} catch (SQLException err) {
					err.printStackTrace();
					System.err.println("Could not update all values as provided. Refusing to proceed.");
				}
				break;
		}

		return primaryKeyOfNewRecord;
	}
	
	public static int createCustomer(String... values) {
		return createOrUpdate(CreationMode.Create, SchemaName.Customer, values);
	};

	public static int updateCustomer(String... values) {
		return createOrUpdate(CreationMode.Update, SchemaName.Customer, values);
	};

	public static int deleteCustomer(String customerId) {
		return deleteOne(SchemaName.Customer.tableName, SchemaName.Customer.primaryKeyName, customerId);
	}
}