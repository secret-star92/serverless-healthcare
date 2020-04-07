/**
 * 
 */
package com.amazonaws.healthcare.function;

/**
 * @author yashvant
 *
 */
public class Constants {
	// DynamoDB table name for storing Patient metadata.
	public static final String PATIENT_DEVICE_TABLE_NAME = System.getenv("PATIENT_DEVICE_TABLE_NAME");
	// DynamoDB table name for storing Patient metadata.
	public static final String PATIENT_TABLE_NAME = System.getenv("PATIENT_TABLE_NAME");
	
	public static final String HEART_RATE_TABLE_NAME = System.getenv("HEART_RATE_TABLE_NAME");
	
	// DynamoDB table name for storing Provider metadata.
	public static final String PROVIDER_TABLE_NAME = System.getenv("PROVIDER_TABLE_NAME");
	
	// DynamoDB table name for storing Device metadata.
	public static final String DEVICE_TABLE_NAME = System.getenv("DEVICE_TABLE_NAME");
	
	public static final String REGION = System.getenv("REGION");
}
