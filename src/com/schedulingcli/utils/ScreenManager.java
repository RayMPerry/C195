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
