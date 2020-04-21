package com.schedulingcli.enums;

public enum Locale {
	EN_US("American English"),
	ES_ES("Español (Mexico)");
	
	public final String dialect;

	Locale(String _dialect) {
		this.dialect = _dialect;
	}
}
