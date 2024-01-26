package com.finmamba.models.bus;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * @author Satyam Gijare
 * @apiNote Data contract to wrap exception details.
 */

@Data
@Builder
public class HttpResponseModel implements Serializable{
	
	private static final long serialVersionUID = 1L;

//	// customizing timestamp serialization format
//	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
//	private LocalDateTime timestamp;

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
	
	private int total;
	
	private int status;
}
