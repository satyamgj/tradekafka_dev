package com.finmamba.simulator.configuration;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimestampDeserializer implements JsonDeserializer<Date> {

	@Override
	public Timestamp deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
		String date = element.getAsString();

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));

		try {
			return Timestamp.from(format.parse(date).toInstant());
		} catch (ParseException exp) {
			log.error("Failed to parse Date:", exp);
			return null;
		}
	}
}
