/**
 * 
 */
package com.amazonaws.healthcare.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yashvant
 *
 */
@Getter
@AllArgsConstructor
public enum SensorType {

	HEART_RATE("Heart Rate");

	private String value;

}
