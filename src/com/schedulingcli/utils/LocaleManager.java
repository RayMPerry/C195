package com.schedulingcli.utils;

import com.schedulingcli.enums.Locale;
import com.schedulingcli.enums.MessageCode;

import java.util.HashMap;
import java.util.Map;

public class LocaleManager {
	private static Map<MessageCode, String> englishMessages = new HashMap<>();
	private static Map<MessageCode, String> spanishMessages = new HashMap<>();
	private static Locale currentLocale = Locale.EN_US;
	private static Map<MessageCode, String> currentMessages;
	
	public static void loadLocale(Locale locale) {
		// English messages
		englishMessages.put(MessageCode.USER_PASS_INVALID, "The username and password did not match.%n");
		englishMessages.put(MessageCode.USERNAME, "Username");
		englishMessages.put(MessageCode.PASSWORD, "Password");
		
		// Spanish messages
		spanishMessages.put(MessageCode.USER_PASS_INVALID, "El nombre de usuario y la contraseña no coincidían.%n");
		spanishMessages.put(MessageCode.USERNAME, "Nombre de usuario");
		spanishMessages.put(MessageCode.PASSWORD, "Contraseña");

		currentMessages = locale == Locale.ES_ES ? spanishMessages : englishMessages;

		System.out.format("Loaded %s locale.%n", locale.dialect);
		currentLocale = locale;
	}
	
	public static String getMessage(MessageCode shortCode) {
		String message;
		
		try {
			message = currentMessages.get(shortCode);
		} catch (Exception err) {
			message = "Code " + shortCode + " does not exist.%n";
		}

		return message;
	}
}
