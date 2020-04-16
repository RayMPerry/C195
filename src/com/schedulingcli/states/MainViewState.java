package com.schedulingcli.states;

import com.schedulingcli.enums.*;
import com.schedulingcli.utils.*;

public class MainViewState implements BasicState {
	public static void setup() {
	}

	public static void run() {
		String customerId = String.valueOf(DbManager.createCustomer("The First Test", "2", "1"));
		DbManager.updateCustomer(customerId, "The Last Test", "2", "0");
		DbManager.deleteCustomer(customerId);
		teardown();
	}
	
	public static void draw() {

	}
	
	public static void teardown() {
		StateManager.setCurrentScreen(ScreenCode.EXIT);
	}
}
