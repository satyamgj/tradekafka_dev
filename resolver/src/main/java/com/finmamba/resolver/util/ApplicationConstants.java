package com.finmamba.resolver.util;

public class ApplicationConstants {
	private ApplicationConstants() {

	}

	// To be injected as a command line parameter
	public static final String BROKER = "KANJI";
	public static final String TOPIC_DELIMITER = "_";

	public static final String BROKER_TO_COUNTERPARTY_PREFIX = BROKER + TOPIC_DELIMITER;
	public static final String COUNTERPARTY_TO_BROKER_PREFIX = TOPIC_DELIMITER + BROKER;
	public static final String BROKER_REQUESTS_TOPIC = TOPIC_DELIMITER + "REQUESTS";

	public static final String REQUEST_SAVED = "Request Saved Successfully!";
	public static final String REQUEST_NOT_SAVED = "Error Occured while saving your request";

	public static final String REQUEST_NOT_PUBLISHED = "Request could not be published on the topic";
	public static final String REQUEST_PUBLISHED = "Request published on the topic";

	public static final String PAYLOAD_TAG = "payload";
	public static final String METHOD_EXECUTION_START = " - START";
	public static final String METHOD_EXECUTION_END = " - END";

	public enum HeaderKeys {
		MESSAGE_TYPE
	}

	public enum HeaderValues {
		REQUEST, RESPONSE
	}

	public enum ResponseTypes {
		DEAL("Deal", 1), NEGOTIATION("NEGOTIATION", 2);

		private final String key;
		private final Integer value;

		ResponseTypes(String key, Integer value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public Integer getValue() {
			return value;
		}

	}

	public enum RecordStatus {
		// This will call enum constructor with one
		// String argument
		ACTIVE(1), INTRANSIT(2), CONFIRMED(3), EXPIRED(4);

		// declaring private variable for getting values
		private long action;

		// getter method
		public long getAction() {
			return this.action;
		}

		// enum constructor - cannot be public or protected
		private RecordStatus(long action) {
			this.action = action;
		}
	}
	
	public enum Event {
		// This will call enum constructor with one
		// String argument
		IN_REQUEST(1), 
		OUT_REQUEST(2), 
		IN_RESPONSE(3), 
		OUT_RESPONSE(4),
		IN_NEGOTIATION(5),
		OUT_NEGOTIATION(6),
		IN_REJECT(7),
		OUT_REJECT(8),
		IN_AWAITING(9),
		OUT_AWAITING(10);

		// declaring private variable for getting values
		private long eventId;

		// getter method
		public long getEventId() {
			return this.eventId;
		}

		// enum constructor - cannot be public or protected
		private Event(long eventId) {
			this.eventId = eventId;
		}
	}
}
