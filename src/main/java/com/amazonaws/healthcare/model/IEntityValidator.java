/**
 * 
 */
package com.amazonaws.healthcare.model;

import java.util.Set;

/**
 * @author yashvant
 *
 */
@FunctionalInterface
public interface IEntityValidator<T> {

	public boolean isValid(T t, Set<String> messages);
	
}
