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

		for (int index = 0; index < args.length; index++) {
			String currentArgument = args[index];
			String nextArgument;
			
			boolean isValidValue = false;
			try {
				isValidValue = index + 1 <= args.length && !args[index + 1].startsWith("--");
			} catch (Exception err) { }

			nextArgument = isValidValue ? args[index + 1] : "";
			
			switch (currentArgument) {
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
		
		DBManager.connect();
		if (DBManager.getConnection() == null) System.exit(1);
		if (!Files.exists(Paths.get(StateManager.LOG_FILE_PATH))) {
			DBManager.createDummyData();
		}

		StateManager.setCurrentScreen(locale.equals("") ? ScreenCode.CHOOSE_LOCALE : ScreenCode.LOG_IN);
		StateManager.startApplication();

		while (StateManager.isApplicationRunning()) {
			ScreenManager.showScreens();
		}
    }
}
