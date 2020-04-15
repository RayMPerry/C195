package com.rperry.states;

import com.rperry.utils.*;
import com.rperry.enums.*;
import com.rperry.states.*;

public class LoginState implements BasicState {
	private static LocaleManager localeManager;

	private static final int MAXIMUM_LOGIN_ATTEMPTS = 3;
	private static final String MAGIC_LOGIN = "test";

	public static void setup(LocaleManager _localeManager) {
		localeManager = _localeManager;
	}
	
	public static void run(String username, String password) {
		boolean isLoginValid = false;
		int numberOfLoginAttempts = 0;
		while (!isLoginValid) {
			if (numberOfLoginAttempts >= MAXIMUM_LOGIN_ATTEMPTS) System.exit(1);

			if (username == "") {
				System.out.format(ScreenManager.getScreen(ScreenCode.LOG_IN_USER), localeManager.getMessage(MessageCode.USERNAME));
				username = InputManager.waitForAnyInput().trim();
			}

			if (password == "") {
				System.out.format(ScreenManager.getScreen(ScreenCode.LOG_IN_PASS), localeManager.getMessage(MessageCode.PASSWORD));
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
	}
	
	public static void draw() {
		
	}
}
