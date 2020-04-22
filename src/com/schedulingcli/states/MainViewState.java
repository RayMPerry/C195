package com.schedulingcli.states;

import com.schedulingcli.enums.ReportingMode;
import com.schedulingcli.enums.Schema;
import com.schedulingcli.enums.ScreenCode;
import com.schedulingcli.utils.InputManager;
import com.schedulingcli.utils.ScreenManager;
import com.schedulingcli.utils.StateManager;

import java.util.Arrays;
import java.util.List;

public class MainViewState implements BasicState {
    public static void setup(ReportingMode reportingMode) {
        ReportingState.reportUpcomingAppointments(reportingMode);
    }

    public static void run() {
        String response = "";
        List<String> appointmentResponses = Arrays.asList("3", "4", "5");
        List<String> customerResponses = Arrays.asList("6", "7", "8");

        InputManager.setValidResponsesWithArguments("1", "2", "9", "0");
        InputManager.addToValidResponses(appointmentResponses.toArray(String[]::new));
        InputManager.addToValidResponses(customerResponses.toArray(String[]::new));

        draw();

        response = InputManager.waitForValidInput();
        if (appointmentResponses.contains(response)) StateManager.setValue("itemName", Schema.Appointment.tableName);
        if (customerResponses.contains(response)) StateManager.setValue("itemName", Schema.Customer.tableName);

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
                StateManager.setCurrentScreen(ScreenCode.DELETE_RECORD);
                break;
            case "9":
                StateManager.setCurrentScreen(ScreenCode.REPORTS);
                break;
            case "0":
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
