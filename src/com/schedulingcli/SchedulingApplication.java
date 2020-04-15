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
		
		Connection db = DbManager.connect();
		if (db == null) System.exit(1);

		LocaleManager l8n;
		if (locale.equals("")) {
			System.out.format(ScreenManager.getScreen(ScreenCode.CHOOSE_LOCALE), Locale.EN_US.dialect, Locale.ES_ME.dialect);
			InputManager.setValidResponses("1", "2");
			String response = InputManager.waitForValidInput();
			locale = response.equals("2") ? "ES_ME" : "EN_US";
		}

		LocaleManager.loadLocale(locale.equals("ES_ME") ? Locale.ES_ME : Locale.EN_US);
		ScreenManager.changeCurrentScreen(ScreenCode.LOG_IN);
		
		while (isRunning) {
			switch (ScreenManager.currentScreen) {
				case LOG_IN:
					LoginState.setup();
					LoginState.draw();
					LoginState.run(username, password);
					break;
				default:
					isRunning = false;
					break;
			}
		}
    }
}
