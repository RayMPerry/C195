package com.schedulingcli.utils;

import com.schedulingcli.enums.ApplicationStatus;
import com.schedulingcli.enums.ScreenCode;

import java.util.HashMap;
import java.util.Map;

public class StateManager {
	private static ApplicationStatus applicationStatus = ApplicationStatus.STOPPED;
	private static Map<String, String> globals = new HashMap<>();
	private static ScreenCode currentScreen;

	public static final int MAXIMUM_LOGIN_ATTEMPTS = 3;
	public static final String MAGIC_LOGIN = "test";
	public static final String LOG_FILE_PATH = "logs/logins.txt";
	public static final String BUSINESS_OPEN = "09:00:00";
	public static final String BUSINESS_CLOSE = "17:00:00";

	public static ApplicationStatus getApplicationStatus() {
		return applicationStatus;
	}

	public static boolean isApplicationRunning() {
		return applicationStatus == ApplicationStatus.STARTED;
	}

	public static void startApplication() {
		applicationStatus = ApplicationStatus.STARTED;
	}

	public static void stopApplication() {
		applicationStatus = ApplicationStatus.STOPPED;
	}

	public static String getValue(String key) {
		return globals.getOrDefault(key, "");
	}

	public static void setValue(String key, String value) {
		globals.put(key, value);
	}

	public static void clearValues(String... keys) {
		for (String key : keys) {
			globals.put(key, "");
		}
	}

	public static ScreenCode getCurrentScreen() {
		return currentScreen;
	}
	
	public static void setCurrentScreen(ScreenCode screenCode) {
		currentScreen = screenCode;
	}
}
