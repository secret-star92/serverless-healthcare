/**
 * 
 */
package com.amazonaws.healthcare.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.amazonaws.healthcare.util.SensorType;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;

import lombok.Data;
import lombok.ToString;

/**
 * @author yashvant
 *
 */
@Data
@ToString
@DynamoDBTable(tableName = "patient_device")
public class PatientDeviceInfo {
	@NotEmpty
	@DynamoDBHashKey(attributeName = "patient_id")
	String patientId;
	@NotEmpty
	@DynamoDBRangeKey(attributeName = "device_id")
	String deviceId;
	@NotNull
	@DynamoDBTypeConvertedEnum
	@DynamoDBAttribute(attributeName = "sensor_type")
	SensorType sensorType;
	@NotNull
	@DynamoDBAttribute(attributeName = "device_status")
	@DynamoDBTypeConvertedEnum
	DeviceStatus deviceStatus;
}
