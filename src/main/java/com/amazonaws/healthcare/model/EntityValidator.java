/**
 * 
 */
package com.amazonaws.healthcare.model;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;

/**
 * @author yashvant
 *
 */
public class EntityValidator<T> {
	
	public IEntityValidator<T> validate =(T t, Set<String> messages)->{
		Set<ConstraintViolation<T>> violations =   Validation.buildDefaultValidatorFactory().getValidator().validate(t);
		if(violations!=null && !violations.isEmpty()) {
			for (ConstraintViolation<T> violation : violations) {
				messages.add(String.format("%s: %s", violation.getPropertyPath(), violation.getMessage()));
			}
			return false;
		}
		return true;
	};

}
