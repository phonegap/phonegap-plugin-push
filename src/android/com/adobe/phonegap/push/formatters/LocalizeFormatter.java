package com.adobe.phonegap.push.formatters;

import java.util.ArrayList;
import java.util.List;

public class LocalizeFormatter implements Localize {
	private static final List<Localize> LOCALIZERS = new ArrayList<Localize>();

	static {
		LOCALIZERS.add(new LocalizeDate());
		LOCALIZERS.add(new LocalizeTime());
		LOCALIZERS.add(new LocalizeDateTime());
		LOCALIZERS.add(new LocalizeCurrency());
	}

	public String localize(String raw) {
		String message = raw;
		for (Localize localize : LOCALIZERS) {
			message = localize.localize(message);
		}
		return message;
	}
}