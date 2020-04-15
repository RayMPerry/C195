package com.rperry.utils;

import java.io.Console;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

public class InputManager {
	private static Console console = System.console();
	private static Scanner input = new Scanner(System.in);
	private static ArrayList<String> validResponses = new ArrayList<>();

	public static void setValidResponses(String... newResponses) {
		validResponses = new ArrayList<>(Arrays.asList(newResponses));
	}

	public static void addToValidResponses(String response) {
		validResponses.add(response);
	}
	
	public static boolean checkForValidInput(String response) {
		return validResponses.contains(response);
	}

	public static String waitForValidInput() {
		String response = "";
		boolean complete = false;
		while (!complete) {
			response = input.nextLine();
			complete = checkForValidInput(response);
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
