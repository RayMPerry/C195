package com.schedulingcli.states;

import com.schedulingcli.enums.*;
import com.schedulingcli.utils.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainViewState implements BasicState {
    public static void setup(ReportingMode reportingMode) {
        ReportingState.reportUpcomingAppointments(reportingMode);
    }

    public static void run() {
        String response = "";
        InputManager.setValidResponsesWithArguments("1", "2", "3", "4", "5", "6", "7", "8", "9", "0");
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
