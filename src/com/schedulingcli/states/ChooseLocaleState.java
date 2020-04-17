package com.schedulingcli.states;

import com.schedulingcli.enums.*;
import com.schedulingcli.utils.*;

public class ChooseLocaleState implements BasicState {
	public static void setup() {
		draw();
	}

	public static void run() {
		InputManager.setValidResponses("1", "2");
		String response = InputManager.waitForValidInput();
		LocaleManager.loadLocale(response.equals("2") ? Locale.ES_ME : Locale.EN_US);
		teardown();
	}
	
	public static void draw() {
		System.out.format(ScreenManager.getCurrentScreen(), Locale.EN_US.dialect, Locale.ES_ME.dialect);
	}
	
	public static void teardown() {
		StateManager.setCurrentScreen(ScreenCode.LOG_IN);
	}
}
