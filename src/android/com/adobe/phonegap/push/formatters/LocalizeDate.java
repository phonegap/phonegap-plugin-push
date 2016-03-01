package com.adobe.phonegap.push.formatters;

import static com.adobe.phonegap.push.PushPlugin.LOG_TAG;
import static java.lang.String.format;

import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class LocalizeDate extends AbstractTimestampParser implements Localize {
	private final Pattern pattern = //
	Pattern.compile("format\\('(\\d{4}-\\d{2}-\\d{2})T(\\d{2}:\\d{2}:\\d{2})\\.?\\d*Z\\',\\s*'(DATE):*(SHORT|MEDIUM|LONG|FULL|DEFAULT)*'\\)");

	public String localize(String raw) {
		final Matcher matcher = pattern.matcher(raw);
		if (matcher.find()) {
			final Date localdate = parse(matcher.group(1) + "T" + matcher.group(2) + "GMT-00:00");
			if (localdate == null) {
				Log.e(LOG_TAG, "LocalizeDate: Couldn't format message.");
				return raw;
			}
			final int style = matcher.groupCount() >= 4 ? style(matcher.group(4)) : DateFormat.DEFAULT;
			final DateFormat formatter = DateFormat.getDateInstance(style);
			final String newMessage = raw.replaceAll(pattern.pattern(), formatter.format(localdate));
			Log.v(LOG_TAG, format("LocalizeDate: Message formatted, new message is %s.", newMessage));
			return newMessage;
		}

		return raw;
	}
}