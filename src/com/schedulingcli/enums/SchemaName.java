package com.schedulingcli.enums;

public enum SchemaName {
	Address("address", "addressId"),
	Appointment("appointment", "appointmentId"),
	City("city", "cityId"),
	Country("country", "countryId"),
	Customer("customer", "customerId"),
	User("user", "userId");

	public final String tableName;
	public final String primaryKeyName;

	SchemaName(String tableName, String primaryKeyName) {
		this.tableName = tableName;
		this.primaryKeyName = primaryKeyName;
	}
}
