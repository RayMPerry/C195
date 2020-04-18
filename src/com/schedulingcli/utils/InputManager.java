package com.schedulingcli.utils;

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
	private static String cancelCommand = "_q";

	public static void setValidResponses(String... newResponses) {
		validResponses = new ArrayList<>(Arrays.asList(newResponses));
	}

	public static void addToValidResponses(String response) {
		validResponses.add(response);
	}
	
	public static boolean checkForValidInput(String response) {
		return response.equals(cancelCommand) || validResponses.contains(response);
	}

	public static String waitForValidDateInput(SimpleDateFormat dateFormat) {
		String response = "";
		boolean isComplete = false;
		while (!isComplete) {
			response = input.nextLine();
			if (response.equals(cancelCommand)) isComplete = true;
			try {
				dateFormat.parse(response);
				isComplete = true;
			} catch (ParseException err) {
				System.out.format("Please enter a date in %s format.", dateFormat.toPattern());
			}
		}

		return response;
	}

	public static String waitForValidInput() {
		String response = "";
		boolean isComplete = false;
		while (!isComplete) {
			response = input.nextLine();
			isComplete = checkForValidInput(response);
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
