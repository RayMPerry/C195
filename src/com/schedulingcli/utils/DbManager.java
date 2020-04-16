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
			statementConclusion = String.format("WHERE %s = ?", "customerId");
		}
		
		return String.format("%s %s %s", statementPreamble, slotsString, statementConclusion);
	}
	
	private static void createOrUpdate(CreationMode creationMode, SchemaName schemaName, String... values) {
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
		
		switch (schemaName) {
			case Customer:
				columnNames = new String[] {
					"customerId",
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
					values = Stream.of(new String[] { "0" }, values)
							 .flatMap(Stream::of)
							 .toArray(String[]::new);
				}
				
				if (creationMode == CreationMode.Update) {
					String sqlQuery = "SELECT * FROM %s WHERE %s = %s";
					try {
						Statement retrievalStatement = dbConnection.createStatement();
						ResultSet results = retrievalStatement.executeQuery(String.format(sqlQuery, schemaName.tableName, "customerId", values[0]));

						while (results.next()) {
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
				
				try (PreparedStatement updateStatement = dbConnection.prepareStatement(fullStatement)) {
					for (int index = 0; index < values.length; index++) {
						if (index == 0) updateStatement.setInt(1, Integer.parseInt(values[0]));
						if (index == 1) updateStatement.setString(2, values[1]);
						if (index == 2) updateStatement.setInt(3, Integer.parseInt(values[2]));
						if (index == 3) updateStatement.setInt(4, Integer.parseInt(values[3]));
					}
					
					if (creationMode == CreationMode.Create) {
						createDate = lastUpdate;
						createdBy = lastUpdateBy;
					}

					updateStatement.setTimestamp(5, createDate);
					updateStatement.setString(6, createdBy);					
					updateStatement.setTimestamp(7, lastUpdate);
					updateStatement.setString(8, lastUpdateBy);

					updateStatement.execute();
				} catch (SQLException err) {
					err.printStackTrace();
					System.err.println("Could not update all values as provided. Refusing to proceed.");
				}
				break;
		}
	}
	
	public static void createCustomerRecord(String... values) {
		createOrUpdate(CreationMode.Create, SchemaName.Customer, values);
	};
}
