/**
 * 
 */
package com.amazonaws.healthcare.model;

import java.util.Base64;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

/**
 * @author yashvant
 *
 */
@Data
public class User {

	public String getId() {
		if (email != null) {
			return Base64.getEncoder().encodeToString(email.getBytes());
		}
		return null;
	}
	@NotEmpty
	String username;
	@NotEmpty
	String password;
	@Email
	@NotEmpty
	String email;
	@NotEmpty
	String phoneNumber;


}
