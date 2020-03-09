/**
 * 
 */
package com.amazonaws.healthcare.model;

import java.util.Set;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * @author yashvant
 *
 */
@FunctionalInterface
public interface IEntityValidator<T> {

	public boolean isValid(T t, Set<String> messages);
	
	public static Validator getValidator() {
		return Validation.buildDefaultValidatorFactory().getValidator();
	}
	
	
}
