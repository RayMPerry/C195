package com.rperry.utils;

import com.rperry.enums.*;

import java.util.Map;
import java.util.HashMap;

public class LocaleManager {
	private Map<MessageCode, String> englishMessages = new HashMap<>();
	private Map<MessageCode, String> spanishMessages = new HashMap<>();

	private Locale currentLocale = Locale.EN_US;
	private Map<MessageCode, String> currentMessages;
	
	public LocaleManager(Locale locale) {
		this.reinitializeMessages(locale);
	}

	private void reinitializeMessages(Locale locale) {
		// English messages
		englishMessages.put(MessageCode.USER_PASS_INVALID, "The username and password did not match.%n");
		englishMessages.put(MessageCode.USERNAME, "Username");
		englishMessages.put(MessageCode.PASSWORD, "Password");
		
		// Spanish messages
		spanishMessages.put(MessageCode.USER_PASS_INVALID, "El nombre de usuario y la contraseña no coincidían.%n");
		spanishMessages.put(MessageCode.USERNAME, "Nombre de usuario");
		spanishMessages.put(MessageCode.PASSWORD, "Contraseña");

		this.currentMessages = locale == Locale.ES_ME ? this.spanishMessages : this.englishMessages;

		System.out.format("Loaded %s locale.%n", locale.dialect);
		this.currentLocale = locale;
	}
	
	public String getMessage(MessageCode shortCode) {
		String message;
		
		try {
			message = this.currentMessages.get(shortCode);
		} catch (Exception err) {
			message = "Code " + shortCode + " does not exist.%n";
		}

		return message;
	}
}
