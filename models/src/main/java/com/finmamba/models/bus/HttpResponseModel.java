package com.finmamba.models.bus;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * @author Swanand Limaye
 * @apiNote Data contract to wrap exception details.
 */

@Data
@Builder
public class HttpResponseModel {
	// customizing timestamp serialization format
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	private LocalDateTime timestamp;

	/**
	 * @implSpec : Details of the exception
	 */
	private String message;

	/**
	 * @implSpec : Exception stack trace
	 */
	private StackTraceElement[] stackTrace;

	/**
	 * @implSpec : Additional details if required
	 */
	private Object data;
	
	private int status;
}
