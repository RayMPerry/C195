package com.schedulingcli.states;

import com.schedulingcli.enums.*;
import com.schedulingcli.utils.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainViewState implements BasicState {
    public static void setup() {
/*		String countryId = String.valueOf(DBManager.createCountry("USA"));
		String cityId = String.valueOf(DBManager.createCity("Chicago", countryId));
		String addressId = String.valueOf(DBManager.createAddress("3238 N Drake Ave", "1", cityId, "60618", "6304027433"));
		String customerId = String.valueOf(DBManager.createCustomer("My Test", addressId, "1"));
*/
		for (int index = 0; index < 5; index++) {
			DBManager.createAppointment(
					"8",
					StateManager.getValue("loggedInUserId"),
					"First Appointment",
					"Yeah",
					"Here",
					"Me",
					"Therapy",
					"http://example.com",
					String.valueOf(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5))),
					String.valueOf(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(6))));
		}

		reportUpcomingAppointments(ReportingMode.IMMINENT);
    }

    private static void reportUpcomingAppointments(ReportingMode reportingMode) {
		String viewFooter = "";
		switch (reportingMode) {
			case WEEK:
				viewFooter = " this week";
				break;
			case MONTH:
				viewFooter = " this month";
				break;
		}

		ArrayList<String> listOfAppointments = new ArrayList<>();
		try {
			ResultSet upcomingAppointments = DBManager.getUpcomingAppointments(reportingMode, StateManager.getValue("loggedInUserId"));
			while (upcomingAppointments.next()) {
				String appointment = String.format("[%s] [%s] \"%s\" %s (%s) - %s %s%n",
						upcomingAppointments.getTimestamp("start"),
						upcomingAppointments.getString("type"),
						upcomingAppointments.getString("title"),
						upcomingAppointments.getString("description"),
						upcomingAppointments.getString("location"),
						upcomingAppointments.getString("contact"),
						upcomingAppointments.getString("url"));

				listOfAppointments.add(appointment);
			}
		} catch (SQLException err) {
			err.printStackTrace();
		}

		System.out.format(ScreenManager.getScreen(ScreenCode.UPCOMING_APPTS),
				listOfAppointments.size(),
				viewFooter,
				String.join("", listOfAppointments));
	}

    public static void run() {
		boolean hasExited = false;
    	while (!hasExited) {
			InputManager.setValidResponses("1", "2", "3");
			draw();
			String response = InputManager.waitForValidInput();
			if (response.equals("1")) reportUpcomingAppointments(ReportingMode.WEEK);
			if (response.equals("2")) reportUpcomingAppointments(ReportingMode.MONTH);
			if (response.equals("3")) hasExited = true;
		}
		StateManager.setCurrentScreen(ScreenCode.EXIT);
    }

    public static void draw() {
		System.out.format(ScreenManager.getCurrentScreen());
    }

    public static void teardown() {

    }
}
