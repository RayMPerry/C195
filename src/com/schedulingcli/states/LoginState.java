package com.schedulingcli.states;

import com.schedulingcli.utils.*;
import com.schedulingcli.enums.*;

public class LoginState implements BasicState {
	private static LocaleManager localeManager;

	private static final int MAXIMUM_LOGIN_ATTEMPTS = 3;
	private static final String MAGIC_LOGIN = "test";

	public static void run(String username, String password) {
		boolean isLoginValid = false;
		int numberOfLoginAttempts = 0;
		while (!isLoginValid) {
			if (numberOfLoginAttempts >= MAXIMUM_LOGIN_ATTEMPTS) System.exit(1);

			if (username == "") {
				System.out.format(ScreenManager.getScreen(ScreenCode.LOG_IN), localeManager.getMessage(MessageCode.USERNAME));
				username = InputManager.waitForAnyInput().trim();
			}

			if (password == "") {
				System.out.format(ScreenManager.getScreen(ScreenCode.LOG_IN), localeManager.getMessage(MessageCode.PASSWORD));
				password = InputManager.waitForPasswordInput().trim();
			}

			numberOfLoginAttempts++;
			
			try {
				if (!username.equals(password)) {
					System.out.format(localeManager.getMessage(MessageCode.USER_PASS_INVALID));
					throw new Exception("Invalid credentials");
				}

				if (!username.equals(MAGIC_LOGIN) || !password.equals(MAGIC_LOGIN)) {
					System.out.format(localeManager.getMessage(MessageCode.USER_PASS_INVALID));
					throw new Exception("Invalid credentials");
				}
			} catch (Exception err) {
				username = "";
				password = "";
				continue;
			}

			isLoginValid = true;
		}
		
		teardown();
	}
	
	public static void setup() {}
	public static void draw() {}
	public static void teardown() {
		ScreenManager.changeCurrentScreen(ScreenCode.MAIN_VIEW);
	}
}
