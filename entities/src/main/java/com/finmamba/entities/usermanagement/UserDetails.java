package com.finmamba.entities.usermanagement;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.finmamba.entities.staticdata.Cities;
import com.finmamba.entities.staticdata.Country;
import com.finmamba.entities.staticdata.States;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name="user_details",
        schema = "user_management")
public class UserDetails implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name="first_name")
	private String firstName;
	
	@Column(name="middle_name")
	private String middleName;
	
	@Column(name="last_name")
	private String lastName;
	
	@Column(name="email_id")
	private String emailId;

	@OneToOne
	@JoinColumn(name = "city_id", referencedColumnName = "id")
	private Cities cityId;
	
	@OneToOne
	@JoinColumn(name = "states_id", referencedColumnName = "id")
	private States statesId;
	
	@OneToOne
	@JoinColumn(name = "country_id", referencedColumnName = "id")
	private Country countryId;
	
	@Column(name="mobile_number")
	private String mobile_number;
	
	@Column(name="address")
	private String address;
	
	@Column(name="created_at")
	private Timestamp createdAt;
	
}
