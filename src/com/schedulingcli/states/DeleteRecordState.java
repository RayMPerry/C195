package com.schedulingcli.states;

import com.schedulingcli.enums.ScreenCode;
import com.schedulingcli.utils.DBManager;
import com.schedulingcli.utils.InputManager;
import com.schedulingcli.utils.ScreenManager;
import com.schedulingcli.utils.StateManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DeleteRecordState implements BasicState {
    private static String itemName;
    private static ArrayList<String> validIds = new ArrayList<>();

    public static void setup() {
        itemName = StateManager.getValue("itemName");
        ResultSet results = null;
        try {
            if (itemName.equals("appointment")) {
                results = DBManager.retrieveWithCondition(itemName, "userId", StateManager.getValue("loggedInUserId"));
            } else {
                results = DBManager.retrieveAll(itemName);
            }

            while (results != null && results.next()) {
                validIds.add(String.valueOf(results.getInt(itemName + "Id")));
            }
        } catch (SQLException err) {
            err.printStackTrace();
            System.out.format("Could not retrieve %ss.%n", itemName);
        }
    }

    public static void run() {
        InputManager.setValidResponsesWithArray(validIds.toArray(String[]::new));
        draw();
        String response = InputManager.waitForValidInput();
        DBManager.cleanUpPastAppointments();
        DBManager.deleteWithCondition(itemName, itemName + "Id", response);
        teardown();
    }

    public static void draw() {
        System.out.format(ScreenManager.getCurrentScreen(), StateManager.getValue("itemName"));

    }

    public static void teardown() {
        StateManager.setCurrentScreen(ScreenCode.MAIN_VIEW);
    }
}
