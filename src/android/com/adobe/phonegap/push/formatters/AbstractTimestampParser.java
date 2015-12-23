package com.adobe.phonegap.push.formatters;

import static com.adobe.phonegap.push.PushPlugin.LOG_TAG;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public abstract class AbstractTimestampParser {
	protected int style(String style) {
		if ("SHORT".equals(style)) { return DateFormat.SHORT; }
		if ("MEDIUM".equals(style)) { return DateFormat.MEDIUM; }
		if ("LONG".equals(style)) { return DateFormat.LONG; }
		if ("FULL".equals(style)) { return DateFormat.FULL; }
		return DateFormat.DEFAULT;
	}

	protected Date parse(String date) {
		try {
			final DateFormat utcParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
			return utcParser.parse(date);
		} catch (ParseException e) {
			Log.e(LOG_TAG, "AbstractTimestampParser: Couldn't parse date " + date, e);
			return null;
		}
	}
}