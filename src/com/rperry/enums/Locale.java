package com.rperry.enums;

public enum Locale {
	EN_US("American English"),
	ES_ME("Español (Mexico)");
	
	public final String dialect;

	Locale(String _dialect) {
		this.dialect = _dialect;
	}
}
