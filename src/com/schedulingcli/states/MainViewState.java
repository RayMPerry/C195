package com.schedulingcli.states;

import com.schedulingcli.enums.*;
import com.schedulingcli.utils.*;

public class MainViewState implements BasicState {
	public static void setup() {
	}

	public static void run() {
		DbManager.createCustomerRecord("testUser", "2", "1");
		teardown();
	}
	
	public static void draw() {

	}
	
	public static void teardown() {
		StateManager.setCurrentScreen(ScreenCode.EXIT);
	}
}
