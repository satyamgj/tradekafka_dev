package com.finmamba.models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicFormat {
	private String realm;
	private String client;
	private String clientRole;
	private String group;
}
