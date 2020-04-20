package com.schedulingcli.utils;

import java.util.Map;
import java.util.HashMap;

import com.schedulingcli.enums.ReportingMode;
import com.schedulingcli.enums.ScreenCode;
import com.schedulingcli.states.*;

public class ScreenManager {
    private static Map<ScreenCode, String> screens = new HashMap<>();
    private static boolean isReady = false;

    private static void initialize() {
        screens.put(ScreenCode.CHOOSE_LOCALE, "%n1) %s%n2) %s%nPlease choose your language:%nPor favor elige tu idioma:%n");
        screens.put(ScreenCode.LOG_IN, "%s: ");
        screens.put(ScreenCode.UPCOMING_APPOINTMENTS, "You have %d upcoming appointments%s.%n%s%n");
        screens.put(ScreenCode.EDIT_RECORD, "Previous %s: %s%n");
        screens.put(ScreenCode.CREATE_RECORD, "New %s (\"_q\" to cancel): ");
        screens.put(ScreenCode.SPECIFY_RECORD, "Specify the ID of the %s you wish to %s (\"_q\" to cancel): %n");
        screens.put(ScreenCode.MAIN_VIEW,
                "Choose an option:%n" +
                        "1) View appointments by week%n" +
                        "2) View appointments by month%n" +
                        "3) Create a new appointment%n" +
                        "4) Edit an existing appointment%n" +
                        "5) Delete an existing appointment%n" +
                        "6) Create a new customer%n" +
                        "7) Edit an existing customer%n" +
                        "8) Delete an existing customer%n" +
                        "9) View reports%n" +
                        "0) Exit%n");
        screens.put(ScreenCode.REPORTS,
                "Choose an report:%n" +
                        "1) Number of appointment types%n" +
                        "2) Schedule of users for the next week%n" +
                        "3) Top 3 booked locations%n" +
                        "4) Exit%n");

        isReady = true;
    }

    public static String getCurrentScreen() {
        return getScreen(StateManager.getCurrentScreen());
    }

    public static String getScreen(ScreenCode screenCode) {
        String screen;

        if (!isReady) initialize();

        try {
            screen = screens.get(screenCode);
        } catch (Exception err) {
            screen = "Code " + screenCode + " does not exist.%n";
        }

        return screen;
    }

    public static void showScreens() {
        switch (StateManager.getCurrentScreen()) {
            case CHOOSE_LOCALE:
                ChooseLocaleState.setup();
                ChooseLocaleState.run();
                break;
            case LOG_IN:
                LoginState.setup();
                LoginState.run();
                MainViewState.setup(ReportingMode.IMMINENT);
                break;
            case MAIN_VIEW:
                MainViewState.run();
                break;
            case CREATE_RECORD:
            case EDIT_RECORD:
                UpdateRecordState.setup();
                UpdateRecordState.run();
                break;
            case DELETE_RECORD:
                DeleteRecordState.setup();
                DeleteRecordState.run();
                break;
            case REPORTS:
                ReportingState.setup();
                ReportingState.run();
                break;
            case EXIT:
                DBManager.closeConnection();
                StateManager.stopApplication();
                break;
        }
    }
}
