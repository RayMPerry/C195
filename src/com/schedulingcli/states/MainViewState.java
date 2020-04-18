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
				String appointment = String.format("ID: %d - [%s] [%s] \"%s\" %s (%s) - %s %s%n",
						upcomingAppointments.getInt("appointmentId"),
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
		String response = "";
		boolean hasExited = false;
    	while (!hasExited) {
			InputManager.setValidResponses("1", "2", "3", "4", "5", "6", "7");
			draw();
			response = InputManager.waitForValidInput();
			if (response.equals("1")) {
				reportUpcomingAppointments(ReportingMode.WEEK);
			} else if (response.equals("2")) {
				reportUpcomingAppointments(ReportingMode.MONTH);
			} else {
				hasExited = true;
			}
		}

    	if (response.equals("3") || response.equals("5")) {
    		StateManager.setCurrentScreen(ScreenCode.CREATE_RECORD);
		} else if (response.equals("4") || response.equals("6")) {
			StateManager.setCurrentScreen(ScreenCode.EDIT_RECORD);
		} else {
			StateManager.setCurrentScreen(ScreenCode.EXIT);
		}
    }

    public static void draw() {
		System.out.format(ScreenManager.getCurrentScreen());
    }

    public static void teardown() {

    }
}
