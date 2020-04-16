package com.schedulingcli.states;

import com.schedulingcli.utils.*;
import com.schedulingcli.enums.*;

public class LoginState implements BasicState {
	public static void run(String username, String password) {
		boolean isLoginValid = false;
		int numberOfLoginAttempts = 0;
		while (!isLoginValid) {
			if (numberOfLoginAttempts >= StateManager.MAXIMUM_LOGIN_ATTEMPTS) System.exit(1);

			if (username == "") {
				draw(LocaleManager.getMessage(MessageCode.USERNAME));
				username = InputManager.waitForAnyInput().trim();
			}

			if (password == "") {
				draw(LocaleManager.getMessage(MessageCode.PASSWORD));
				password = InputManager.waitForPasswordInput().trim();
			}

			numberOfLoginAttempts++;
			
			try {
				if (!username.equals(password)) {
					System.out.format(LocaleManager.getMessage(MessageCode.USER_PASS_INVALID));
					throw new Exception("Invalid credentials");
				}

				if (!username.equals(StateManager.MAGIC_LOGIN) || !password.equals(StateManager.MAGIC_LOGIN)) {
					System.out.format(LocaleManager.getMessage(MessageCode.USER_PASS_INVALID));
					throw new Exception("Invalid credentials");
				}
			} catch (Exception err) {
				username = "";
				password = "";
				continue;
			}

			isLoginValid = true;
		}
		
		teardown(username);
	}
	
	public static void draw(String loginScreenArg) {
		System.out.format(ScreenManager.getCurrentScreen(), loginScreenArg);
	}

	public static void teardown(String username) {
		StateManager.setValue("loggedInUser", username);
		StateManager.setCurrentScreen(ScreenCode.MAIN_VIEW);
	}

	public static void setup() {}
}
