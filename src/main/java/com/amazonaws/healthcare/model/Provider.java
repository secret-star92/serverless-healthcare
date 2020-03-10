/**
 * 
 */
package com.amazonaws.healthcare.model;

import java.util.Base64;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.amazonaws.healthcare.util.ProviderStatus;

import lombok.Data;

/**
 * @author yashvant
 *
 */
@Data
public class Provider {
	public String getId() {
		if (email != null)
			return Base64.getEncoder().encodeToString(email.getBytes());
		return null;
	}
	@Email
	@NotEmpty
	String email;
	@NotEmpty
	String firstname;
	@NotEmpty
	String lastname;
	@NotEmpty
	String specilization;
	@NotEmpty
	String mobileNumber;
	@NotEmpty
	String address;
	@NotNull
	ProviderStatus status;

}
