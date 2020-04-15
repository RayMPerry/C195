package com.rperry.utils;

import java.util.Map;
import java.util.HashMap;

import com.rperry.enums.ScreenCode;

public class ScreenManager {
	private static Map<ScreenCode, String> screens = new HashMap<>();
	private static boolean isReady = false;

	public static ScreenCode currentScreen;

	private static void initialize() {
		screens.put(ScreenCode.CHOOSE_LOCALE, "%n1) %s%n2) %s%nPlease choose your language:%nPor favor elige tu idioma:%n");
		screens.put(ScreenCode.LOG_IN_USER, "%s: ");
		screens.put(ScreenCode.LOG_IN_PASS, "%s: ");
		
		isReady = true;
	}

	public static String changeScreen(ScreenCode screenCode) {
		currentScreen = screenCode;
		return getScreen(currentScreen);
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
