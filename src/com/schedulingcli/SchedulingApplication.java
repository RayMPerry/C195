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
		String username = "";
		String password = "";

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
					if (isValidValue) username = nextArgument;
					break;
				case "--password":
					if (isValidValue) password = nextArgument;
					break;
			}
		}
		
		DBManager.connect();
		if (DBManager.getConnection() == null) System.exit(1);
		if (!Files.exists(Paths.get(StateManager.LOG_FILE_PATH))) {
			DBManager.createUser(StateManager.MAGIC_LOGIN, StateManager.MAGIC_LOGIN, "1");
		}

		if (locale.equals("")) {
			StateManager.setCurrentScreen(ScreenCode.CHOOSE_LOCALE);
		} else {
			StateManager.setCurrentScreen(ScreenCode.LOG_IN);
		}

		while (isRunning) {
			switch (StateManager.getCurrentScreen()) {
				case CHOOSE_LOCALE:
					ChooseLocaleState.setup();
					ChooseLocaleState.run();
					break;
				case LOG_IN:
					LoginState.setup();
					LoginState.run(username, password);
					break;
				case MAIN_VIEW:
					MainViewState.setup();
					MainViewState.run();
					break;
				case EXIT:
					DBManager.closeConnection();
					isRunning = false;
					break;
			}
		}
    }
}
