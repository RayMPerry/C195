package com.schedulingcli.states;

import com.schedulingcli.enums.*;
import com.schedulingcli.utils.*;

public class ChooseLocaleState implements BasicState {
	public static void setup() {
		draw();
	}

	public static void run() {
		InputManager.setValidResponsesWithArguments("1", "2");
		String response = InputManager.waitForValidInput();
		LocaleManager.loadLocale(response.equals("2") ? Locale.ES_ES : Locale.EN_US);
		teardown();
	}
	
	public static void draw() {
		System.out.format(ScreenManager.getCurrentScreen(), Locale.EN_US.dialect, Locale.ES_ES.dialect);
	}
	
	public static void teardown() {
		StateManager.setCurrentScreen(ScreenCode.LOG_IN);
	}
}
