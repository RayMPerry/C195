package com.schedulingcli.states;

import com.schedulingcli.enums.*;
import com.schedulingcli.utils.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainViewState implements BasicState {
    public static void setup(ReportingMode reportingMode) {
        reportUpcomingAppointments(reportingMode);
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

        System.out.format(ScreenManager.getScreen(ScreenCode.UPCOMING_APPOINTMENTS),
                listOfAppointments.size(),
                viewFooter,
                String.join("", listOfAppointments));

        run();
    }

    public static void run() {
        String response = "";
        InputManager.setValidResponsesWithArguments("1", "2", "3", "4", "5", "6", "7", "8", "9");
        draw();
        response = InputManager.waitForValidInput();
        switch (response) {
            case "1":
                setup(ReportingMode.WEEK);
                break;
            case "2":
                setup(ReportingMode.MONTH);
                break;
            case "3":
            case "6":
                StateManager.setCurrentScreen(ScreenCode.CREATE_RECORD);
                break;
            case "4":
            case "7":
                StateManager.setCurrentScreen(ScreenCode.EDIT_RECORD);
                break;
            case "5":
            case "8":
                StateManager.setValue("itemName", response.equals("5") ? "appointment" : "customer");
                StateManager.setCurrentScreen(ScreenCode.DELETE_RECORD);
                break;
            case "9":
			default:
				StateManager.setCurrentScreen(ScreenCode.EXIT);
                break;
        }
    }

    public static void draw() {
        System.out.format(ScreenManager.getCurrentScreen());
    }

    public static void teardown() {

    }
}
