package com.schedulingcli.utils;

import com.schedulingcli.enums.ScreenCode;

import java.io.Console;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

public class InputManager {
	private static Console console = System.console();
	private static Scanner input = new Scanner(System.in);
	private static ArrayList<String> validResponses = new ArrayList<>();

	public static String cancelCommand = "_q";

	public static void setValidResponsesWithArray(String[] newResponses) {
		validResponses = new ArrayList<>(Arrays.asList(newResponses));
	}

	public static void setValidResponsesWithArguments(String... newResponses) {
		setValidResponsesWithArray(newResponses);
	}

	public static void addToValidResponses(String response) {
		validResponses.add(response);
	}

	public static boolean checkForValidInput(String response) {
		return response.equals(cancelCommand) || validResponses.contains(response);
	}

	public static boolean checkForValidInput(String response, String errorMessage) {
		boolean isValid = response.equals(cancelCommand) || validResponses.contains(response);
		if (!isValid) System.out.println(errorMessage);
		return isValid;
	}

	public static String waitForValidDateInput(SimpleDateFormat dateFormat) {
		String response = "";
		boolean isComplete = false;
		while (!isComplete) {
			response = input.nextLine();
			if (response.equals(cancelCommand)) {
				StateManager.setCurrentScreen(ScreenCode.MAIN_VIEW);
				break;
			}
			try {
				dateFormat.parse(response);
				isComplete = true;
			} catch (ParseException err) {
				System.out.format("Please enter a date in %s format.", dateFormat.toPattern());
			}
		}

		if (response.equals(cancelCommand)) StateManager.setCurrentScreen(ScreenCode.MAIN_VIEW);
		return response;
	}

	public static String waitForValidInput() {
		return waitForValidInput("");
	}

	public static String waitForValidInput(String errorMessage) {
		String response = "";
		boolean isComplete = false;
		while (!isComplete) {
			response = input.nextLine();
			isComplete = !errorMessage.isEmpty() ? checkForValidInput(response, errorMessage) : checkForValidInput(response);
		}

		return response;
	}

	public static String waitForPasswordInput() {
		return console != null ? new String(console.readPassword()) : waitForAnyInput();
	}

	public static String waitForAnyInput() {
		return input.nextLine();
	}
}
