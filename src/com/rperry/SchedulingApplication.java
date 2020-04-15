package com.rperry;

import com.rperry.utils.*;
import com.rperry.enums.*;
import com.rperry.states.*;

import java.io.*;
import java.util.Scanner;
import java.sql.Connection;
import java.util.stream.*;

public class SchedulingApplication {
    public static void main(String[] args) {
		String locale = "";
		String username = "";
		String password = "";

		for (int index = 0; index < args.length; index++) {
			String currentArgument = args[index];
			boolean isValidValue = index + 1 <= args.length && !args[index + 1].startsWith("--");
			switch (currentArgument) {
				case "--locale":
					if (isValidValue) locale = args[index + 1];
					break;
				case "--username":
					if (isValidValue) username = args[index + 1];
					break;
				case "--password":
					if (isValidValue) password = args[index + 1];
					break;
			}
		}
		
		Connection db = DbManager.connect();
		if (db == null) System.exit(1);

		LocaleManager l8n;
		if (locale == "") {
			System.out.format(ScreenManager.getScreen(ScreenCode.CHOOSE_LOCALE), Locale.EN_US.dialect, Locale.ES_ME.dialect);
			InputManager.setValidResponses("1", "2");
			String response = InputManager.waitForValidInput();
			locale = response == "2" ? "ES_ME" : "EN_US";
		}

		l8n = new LocaleManager(locale == "ES_ME" ? Locale.ES_ME : Locale.EN_US);

		LoginState.setup(l8n);
		LoginState.draw();
		LoginState.run(username, password);
    }
}
