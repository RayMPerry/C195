package com.schedulingcli.states;

import com.schedulingcli.enums.*;
import com.schedulingcli.utils.*;

public class MainViewState implements BasicState {
	public static void setup() {
	}

	public static void run() {
		String countryId = String.valueOf(DBManager.createCountry("USA"));
		String cityId = String.valueOf(DBManager.createCity("Chicago", countryId));
		String addressId = String.valueOf(DBManager.createAddress("3238 N Drake Ave", "1", cityId, "60618", "6304027433"));
		String customerId = String.valueOf(DBManager.createCustomer("My Test", addressId, "1"));

		DBManager.createAppointment(
				customerId,
				StateManager.getValue("loggedInUserId"),
				"First Appointment",
				"Yeah",
				"Here",
				"Me",
				"Therapy",
				"http://example.com",
				"2020-01-12 10:00:00",
				"2020-01-12 11:00:00");

		teardown();
	}
	
	public static void draw() {

	}
	
	public static void teardown() {
		StateManager.setCurrentScreen(ScreenCode.EXIT);
	}
}
