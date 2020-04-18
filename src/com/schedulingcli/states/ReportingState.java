package com.schedulingcli.states;

import com.schedulingcli.enums.ReportingMode;
import com.schedulingcli.enums.ScreenCode;
import com.schedulingcli.utils.DBManager;
import com.schedulingcli.utils.InputManager;
import com.schedulingcli.utils.ScreenManager;
import com.schedulingcli.utils.StateManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReportingState implements BasicState {
    private static void drawTwoColumnReport(String title, ResultSet results) {
        if (results == null) return;
        System.out.println(title);
        System.out.println("=".repeat(title.length()));
        try {
            while (results.next()) {
                System.out.format("%s: %s%n", results.getString(1), results.getInt(2));
            }
        } catch (SQLException err) {
            err.printStackTrace();
        }
        System.out.println();
    }

    private static ArrayList<String> formatAppointments(ResultSet results) {
        ArrayList<String> appointments = new ArrayList<>();

        try {
            while (results != null && results.next()) {
                String appointment = String.format("%d - [%s - %s] [%s] \"%s\" %s (%s) - %s/%s - %s %s%n",
                        results.getInt("appointmentId"),
                        results.getTimestamp("start"),
                        results.getTimestamp("end"),
                        results.getString("type"),
                        results.getString("title"),
                        results.getString("description"),
                        results.getString("location"),
                        results.getString(2),
                        results.getString(3),
                        results.getString("contact"),
                        results.getString("url"));

                appointments.add(appointment);
            }
        } catch (SQLException err) {
            err.printStackTrace();
        }

        return appointments;
    }

    public static void reportAppointmentTypes() {
        drawTwoColumnReport("Number of Appointment Types", DBManager.getNumberOfAppointmentTypes());
    }

    public static void reportTopThreeBookedLocations() {
        drawTwoColumnReport("Top 3 Booked Locations", DBManager.getTopThreeBookedLocations());
    }

    public static void reportAllAppointmentsByUser() {
        System.out.println("All Appointments By User");
        System.out.println("========================");
        formatAppointments(DBManager.getAllAppointmentsByUser())
                .stream()
                // Makes it easier to print instead of a loop.
                .forEachOrdered(System.out::print);
        System.out.println();
    }

    public static void reportUpcomingAppointments(ReportingMode reportingMode) {
        String viewFooter = "";
        switch (reportingMode) {
            case WEEK:
                viewFooter = " this week";
                break;
            case MONTH:
                viewFooter = " this month";
                break;
        }

        ArrayList<String> listOfAppointments = formatAppointments(DBManager.getUpcomingAppointments(reportingMode, StateManager.getValue("loggedInUserId")));

        System.out.format(ScreenManager.getScreen(ScreenCode.UPCOMING_APPOINTMENTS),
                listOfAppointments.size(),
                viewFooter,
                String.join("", listOfAppointments));
    }


    public static void setup() {
    }

    public static void run() {
        InputManager.setValidResponsesWithArguments("1", "2", "3", "4");
        draw();
        String response = InputManager.waitForValidInput();
        switch (response) {
            case "1":
                reportAppointmentTypes();
                break;
            case "2":
                reportAllAppointmentsByUser();
                break;
            case "3":
                reportTopThreeBookedLocations();
                break;
            case "4":
            default:
                teardown();
        }
    }

    public static void draw() {
        System.out.format(ScreenManager.getCurrentScreen());
    }

    public static void teardown() {
        StateManager.setCurrentScreen(ScreenCode.MAIN_VIEW);
    }
}
