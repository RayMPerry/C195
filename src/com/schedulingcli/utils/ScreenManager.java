package com.schedulingcli.utils;

import java.util.Map;
import java.util.HashMap;

import com.schedulingcli.enums.ScreenCode;

public class ScreenManager {
	private static Map<ScreenCode, String> screens = new HashMap<>();
	private static boolean isReady = false;

	private static void initialize() {
		screens.put(ScreenCode.CHOOSE_LOCALE, "%n1) %s%n2) %s%nPlease choose your language:%nPor favor elige tu idioma:%n");
		screens.put(ScreenCode.LOG_IN, "%s: ");
		screens.put(ScreenCode.UPCOMING_APPTS, "You have %d upcoming appointments%s.%n%s%n");
		screens.put(ScreenCode.CREATE_RECORD, "%s? ");
		screens.put(ScreenCode.EDIT_RECORD, "New %s? ");
		screens.put(ScreenCode.MAIN_VIEW,
				"Choose an option:%n" +
				"1) View appointments by week%n" +
				"2) View appointments by month%n" +
				"3) Create a new appointment%n" +
				"4) Edit an existing appointment%n" +
				"5) Create a new customer%n" +
				"6) Edit an existing customer%n" +
				"7) Exit%n");

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
}
