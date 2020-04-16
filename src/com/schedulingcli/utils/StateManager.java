package com.schedulingcli.utils;

import java.util.Map;
import java.util.HashMap;

import com.schedulingcli.enums.*;

public class StateManager {
	private static Map<String, String> globals = new HashMap<>();
	private static ScreenCode currentScreen;

	public static final int MAXIMUM_LOGIN_ATTEMPTS = 3;
	public static final String MAGIC_LOGIN = "test";
	public static final String LOG_FILE_PATH = "logs/logins.txt";

	public static String getValue(String key) {
		return globals.get(key);
	}

	public static void setValue(String key, String value) {
		globals.put(key, value);
	}

	public static ScreenCode getCurrentScreen() {
		return currentScreen;
	}
	
	public static void setCurrentScreen(ScreenCode screenCode) {
		currentScreen = screenCode;
	}
}
