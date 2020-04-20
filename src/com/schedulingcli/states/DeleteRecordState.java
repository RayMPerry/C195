package com.schedulingcli.states;

import com.schedulingcli.enums.ScreenCode;
import com.schedulingcli.utils.DBManager;
import com.schedulingcli.utils.InputManager;
import com.schedulingcli.utils.ScreenManager;
import com.schedulingcli.utils.StateManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeleteRecordState implements BasicState {
    private static String itemName;
    private static ArrayList<String> validIds = new ArrayList<>();

    public static void setup() {
        itemName = StateManager.getValue("itemName");
        validIds = DBManager.getEntityIds(itemName);
    }

    public static void run() {
        InputManager.setValidResponsesWithArray(validIds.toArray(String[]::new));
        draw();
        String response = InputManager.waitForValidInput();
        DBManager.cleanUpPastAppointments();
        if (!response.equals(InputManager.cancelCommand)) {
            try {
                DBManager.deleteWithCondition(itemName, itemName + "Id", response);
                System.out.format("%nFinished.%n%n");
            } catch (Exception err) {
                System.out.format("Could not delete %s with ID %s. Aborting.", itemName, response);
            }
        }
        teardown();
    }

    public static void draw() {
        System.out.format(ScreenManager.getScreen(ScreenCode.SPECIFY_RECORD), itemName, "delete");
    }

    public static void teardown() {
        StateManager.setCurrentScreen(ScreenCode.MAIN_VIEW);
    }
}
