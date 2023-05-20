package com.finmamba.models.bus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Swanand
 * @category : Data contract
 * @version : 1.0
 * @implNote : Model to wrap data on the communication layer(Serial Bus)
 *
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SerialDataBusModel {
	/**
	 * @implNote : System user actions Supported actions are - CREATE, READ, UPDATE,
	 *           DELETE
	 * TODO : Should be validated before consuming. Validation values should come as an argument to the constraint.
	 */
	private EventType eventType;

	/**
	 * @implNote : Timestamp when the message was delivered
	 */
	private String messageTimestamp;

	/**
	 * @implNote : JSON of user details relevant to the type of action
	 * OR
	 * Response String
	 */
	 //Resolved typo error
	private Object payload;
}
