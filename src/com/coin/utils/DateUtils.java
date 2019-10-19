package com.coin.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
	public static String formatDate(Date date,  String format) throws ParseException {
		DateFormat df = new SimpleDateFormat(format);
		df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		return df.format(date);
	}
	public static Date parseDate(String date,  String format) {
		DateFormat df = new SimpleDateFormat(format);
		df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		try {
			return df.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
