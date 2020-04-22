package com.schedulingcli.utils;

import com.schedulingcli.enums.ScreenCode;

import java.io.Console;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

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

    public static void addToValidResponses(String[] responses) {
        Collections.addAll(validResponses, responses);
    }

    public static boolean checkForValidInput(String response) {
        return response.equals(cancelCommand) || validResponses.contains(response);
    }

    public static boolean checkForValidInput(String response, String errorMessage) {
        boolean isValid = response.equals(cancelCommand) || validResponses.contains(response);
        if (!isValid) System.out.println(errorMessage);
        return isValid;
    }

    public static boolean areDatesDuringBusinessHours(String start, String end) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Timestamp startingTimestamp = Timestamp.valueOf(start);
        Timestamp endingTimestamp = Timestamp.valueOf(end);

        String startDate = dateFormat.format(startingTimestamp);
        boolean onSameDay = startDate.equals(dateFormat.format(endingTimestamp));

        Timestamp openingTimestamp = Timestamp.valueOf(String.format("%s %s", startDate, StateManager.BUSINESS_OPEN));
        Timestamp closingTimestamp = Timestamp.valueOf(String.format("%s %s", startDate, StateManager.BUSINESS_CLOSE));

        long appointmentStartTime = startingTimestamp.toInstant().toEpochMilli();
        long appointmentEndTime = endingTimestamp.toInstant().toEpochMilli();
        long businessOpen = openingTimestamp.toInstant().toEpochMilli();
        long businessClose = closingTimestamp.toInstant().toEpochMilli();

        return onSameDay
                && appointmentStartTime >= businessOpen
                && appointmentStartTime < businessClose
                && appointmentEndTime <= businessClose
                && appointmentEndTime > businessOpen;
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
                System.out.format("Please enter a date in %s format: ", dateFormat.toPattern());
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
        String response = input.nextLine();
        if (response.equals(cancelCommand)) response = "";
        return response;
    }

    public static String waitForNonEmptyInput() {
        String response = "";
        while (response.isEmpty()) {
            response = input.nextLine();
            if (response.isEmpty()) System.out.print("Please enter a value: ");
        }
        return response;
    }

    public static String[] aggregateResponses(String[] listOfPrompts) {
        return aggregateResponses(listOfPrompts, false);
    }

    public static String[] aggregateResponses(String[] listOfPrompts, boolean ensureNonEmptyResponses) {
        ArrayList<String> allResponses = new ArrayList<>();

        String response;
        for (String prompt : listOfPrompts) {
            System.out.format(prompt);
            response = ensureNonEmptyResponses ? InputManager.waitForNonEmptyInput() : InputManager.waitForAnyInput();
            allResponses.add(response);
        }

        return allResponses.toArray(String[]::new);
    }
}
