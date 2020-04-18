package com.schedulingcli.enums;

import java.util.stream.Stream;

public enum Schema {
	Address("address",
			"addressId",
			"address",
			"address2",
			"cityId",
			"postalCode",
			"phone",
			"createDate",
			"createdBy",
			"lastUpdate",
			"lastUpdateBy"),
	Appointment("appointment",
			"appointmentId",
			"customerId",
			"userId",
			"title",
			"description",
			"location",
			"contact",
			"type",
			"url",
			"start",
			"end",
			"createDate",
			"createdBy",
			"lastUpdate",
			"lastUpdateBy"),
	City("city",
			"cityId",
			"city",
			"countryId",
			"createDate",
			"createdBy",
			"lastUpdate",
			"lastUpdateBy"),
	Country("country",
			"countryId",
			"country",
			"createDate",
			"createdBy",
			"lastUpdate",
			"lastUpdateBy"),
	Customer("customer",
			"customerId",
			"customerName",
			"addressId",
			"active",
			"createDate",
			"createdBy",
			"lastUpdate",
			"lastUpdateBy"),
	User("user",
			"userId",
			"userName",
			"password",
			"active",
			"createDate",
			"createdBy",
			"lastUpdate",
			"lastUpdateBy");

	public final String tableName;
	public final String primaryKeyName;
	public final String[] columnNames;

	Schema(String tableName, String primaryKeyName, String... columnNames) {
		this.tableName = tableName;
		this.primaryKeyName = primaryKeyName;
		this.columnNames = Stream.of(new String[] { primaryKeyName }, columnNames)
				.flatMap(Stream::of)
				.toArray(String[]::new);
	}
}
