package com.finmamba.reports.configuration;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TimestampSerializer implements JsonSerializer<Timestamp> {

	/*@Override
	public JsonElement serialize(Date dateField, Type typeOfSrc, JsonSerializationContext context) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		String strDate = format.format(dateField);
		
		return com.google.gson.JsonParser.parseString(strDate);
		
	}*/
	public JsonElement serialize(Timestamp dateTimeField, Type typeOfSrc, JsonSerializationContext context) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		String strDate = format.format(dateTimeField);
		
		return com.google.gson.JsonParser.parseString(strDate);
		
	}
}
