package com.schedulingcli.enums;

public enum SchemaName {
	Address("address"),
	Appointment("appointment"),
	City("city"),
	Country("country"),
	Customer("customer"),
	User("user");

	public final String tableName;

	SchemaName(String tableName) {
		this.tableName = tableName;
	}
}
