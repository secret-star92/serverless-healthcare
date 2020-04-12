/**
 * 
 */
package com.amazonaws.healthcare.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

/**
 * @author yashvant
 *
 */
@Data
public class User {

	@NotEmpty
	String username;
	@NotEmpty
	String password;
	@Email
	@NotEmpty
	String email;

}
