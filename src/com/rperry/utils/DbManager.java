package com.rperry.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbManager {
    public static Connection connect() {
		Connection _conn = null;
		try {
			String url = "jdbc:mysql://3.227.166.251:3306/U05NJc";
			String user = "U05NJc";
			String password = "53688551183";
	
			_conn = DriverManager.getConnection(url, user, password);
		} catch (SQLException err) {
			System.out.println(err.getMessage());
		} finally {
			String statusMessage = _conn != null ? "Connected to" : "Failed to connect to";
			System.out.format("%s the database.%n", statusMessage);
			return _conn;
		}
    }
}
