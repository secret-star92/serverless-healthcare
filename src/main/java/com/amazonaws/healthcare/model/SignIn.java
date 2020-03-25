/**
 * 
 */
package com.amazonaws.healthcare.model;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

/**
 * @author yashvant
 *
 */
@Data
public class SignIn {

	@NotEmpty
	String username;
	@NotEmpty
	String password;

}
