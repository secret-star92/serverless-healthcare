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
public enum StatusCode {

	SUCCESS(200), VALICATION_FAILED(400), INTERNAL_SERVER_ERROR(500);

	private int code;

}
