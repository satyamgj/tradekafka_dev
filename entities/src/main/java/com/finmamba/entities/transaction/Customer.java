package com.finmamba.entities.transaction;

import java.io.Serializable;
import java.time.Instant;

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
import com.finmamba.entities.staticdata.CustomerSegment;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Table(name = "customer_details", schema = "user_management")
@Builder
public class Customer implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
	
	@Column(name="first_name")
	private String firstName;
	
	@Column(name="middle_name")
	private String middleName;
	
	@Column(name="last_name")
	private String lastName;
	
	@Column(name="email_id")
	private String email;
	
	
	@OneToOne
	@JoinColumn(name="city_id")
	private Cities city;
	
	@OneToOne
	@JoinColumn(name="state_id")
	private Cities state;
	
	@OneToOne
	@JoinColumn(name="country_id")
	private Country country;
	
	@Column(name="mobile_number")
	private String phone;
	
	@Column(name="address")
	private String address;
	
	@OneToOne
	@JoinColumn(name="user_id")
	private User user;
	
	@OneToOne
	@JoinColumn(name="customer_segment_id")
	private CustomerSegment customerSegment;
	
	@Column(name="created_at")
	private Instant createdAt; 
	
}
