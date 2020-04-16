package com.schedulingcli;

import com.schedulingcli.utils.*;
import com.schedulingcli.enums.*;
import com.schedulingcli.states.*;

import java.sql.Connection;

public class SchedulingApplication {
	public static boolean isRunning = true;
	
    public static void main(String[] args) {
		String locale = "";
		String username = "";
		String password = "";

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
		
		DbManager.connect();
		if (DbManager.getConnection() == null) System.exit(1);

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
					isRunning = false;
					break;
			}
		}
    }
}
