package com.schedulingcli.states;

import com.schedulingcli.utils.*;
import com.schedulingcli.enums.*;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class LoginState implements BasicState {
	public static void run() {
		String userName = StateManager.getValue("userName");
		String password = StateManager.getValue("password");
		boolean isLoginValid = false;
		int numberOfLoginAttempts = 0;
		while (!isLoginValid) {
			if (numberOfLoginAttempts >= StateManager.MAXIMUM_LOGIN_ATTEMPTS) System.exit(1);

			if (userName == null || userName.isEmpty()) {
				draw(LocaleManager.getMessage(MessageCode.USERNAME));
				userName = InputManager.waitForAnyInput().trim();
			}

			if (password == null || password.isEmpty()) {
				draw(LocaleManager.getMessage(MessageCode.PASSWORD));
				password = InputManager.waitForPasswordInput().trim();
			}

			numberOfLoginAttempts++;
			
			try {
				if (!DBManager.areCredentialsValid(userName, password)) {
					System.out.format(LocaleManager.getMessage(MessageCode.USER_PASS_INVALID));
					throw new Exception("Invalid credentials");
				}
			} catch (Exception err) {
				userName = "";
				password = "";
				continue;
			}

			isLoginValid = true;
		}
		
		teardown();
	}
	
	public static void draw(String loginScreenArg) {
		System.out.format(ScreenManager.getCurrentScreen(), loginScreenArg);
	}

	public static void teardown() {
		try {
			Path logFilePath = Paths.get(StateManager.LOG_FILE_PATH);

			if (!Files.exists(logFilePath)) {
				Files.createDirectories(logFilePath.getParent());
				Files.createFile(logFilePath);
			}

			String logLine = String.format("[%s] %s logged in.", new java.sql.Timestamp(System.currentTimeMillis()), StateManager.getValue("loggedInUser"));
			Files.write(logFilePath, Arrays.asList(logLine), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
		} catch (IOException err) {
			err.printStackTrace();
			System.out.println("Could not add user to log file.");
		}

		System.out.format("Welcome back, %s.%n", StateManager.getValue("loggedInUser"));
		StateManager.clearValues("userName", "password");
		StateManager.setCurrentScreen(ScreenCode.MAIN_VIEW);
	}

	public static void setup() {}
}
