package com.adobe.phonegap.push.formatters;

import static com.adobe.phonegap.push.PushPlugin.LOG_TAG;
import static java.lang.String.format;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class LocalizeCurrency extends AbstractTimestampParser implements Localize {
	private final Pattern pattern = //
	Pattern.compile("format\\('([+-]?[0-9]{1,3}(?:,?[0-9]{3})*\\.[0-9]{2})', '(CURRENCY)'\\)");

	public String localize(String raw) {
		final Matcher matcher = pattern.matcher(raw);
		if (matcher.find()) {
			final BigDecimal value = new BigDecimal(matcher.group(1));
			String currency = NumberFormat.getCurrencyInstance().format(value);
			final String newMessage = raw.replaceAll(pattern.pattern(), Matcher.quoteReplacement(currency));
			Log.v(LOG_TAG, format("LocalizeCurrency: Message formatted, new message is %s.", newMessage));
			return newMessage;
		}
		return raw;
	}
}