package com.schedulingcli;

import com.schedulingcli.utils.*;
import com.schedulingcli.enums.*;
import com.schedulingcli.states.*;

import java.nio.file.Files;
import java.nio.file.Paths;

public class SchedulingApplication {
	public static boolean isRunning = true;
	
    public static void main(String[] args) {
		String locale = "";

		StateManager.setValue("loggedInUser", "system");
		StateManager.setValue("logFilePath", "logs/logins.txt");

		DBManager.connect();
		if (DBManager.getConnection() == null) System.exit(1);
		if (!Files.exists(Paths.get(StateManager.LOG_FILE_PATH))) {
			DBManager.createDummyData();
		}

		for (int index = 0; index < args.length; index++) {
			String currentArgument = args[index];
			String nextArgument;
			
			boolean isValidValue = false;
			try {
				isValidValue = index + 1 <= args.length && !args[index + 1].startsWith("--");
			} catch (Exception err) { }

			nextArgument = isValidValue ? args[index + 1] : "";
			
			switch (currentArgument) {
				case "--generate":
					DBManager.createDummyData();
					break;
				case "--locale":
					if (isValidValue) locale = nextArgument;
					break;
				case "--username":
					if (isValidValue) StateManager.setValue("userName", nextArgument);
					break;
				case "--password":
					if (isValidValue) StateManager.setValue("password", nextArgument);
					break;
			}
		}

		if (locale.isEmpty()) locale = java.util.Locale.getDefault().toString();

		switch (locale) {
			case "es_ES":
				LocaleManager.loadLocale(Locale.ES_ES);
				break;
			case "en_US":
				LocaleManager.loadLocale(Locale.EN_US);
				break;
			default:
				System.out.format("No suitable language found for %s.", java.util.Locale.getDefault().getDisplayLanguage());
				StateManager.setCurrentScreen(ScreenCode.CHOOSE_LOCALE);
				break;
		}

		StateManager.setCurrentScreen(ScreenCode.LOG_IN);
		StateManager.startApplication();

		while (StateManager.isApplicationRunning()) {
			ScreenManager.showScreens();
		}
    }
}
