package com.finmamba.models.communication.email;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.springframework.lang.NonNull;

import com.google.common.collect.ImmutableMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EmailProperties {

	private String to;

	private String cc;

	private String bcc;

	@NonNull
	private String subject;

	@NonNull
	private String body;

	@Builder.Default
	private String encoding = StandardCharsets.UTF_8.name();

	private Locale locale;

	private Date sentAt;

	// This is to have default values in Lombok constructor
	public static class DefaultEmailBuilder {

		private String cc = "";

		private String bcc = "";

		private String encoding = StandardCharsets.UTF_8.name();

		private Map<String, String> customHeaders = ImmutableMap.of();
	}
}
