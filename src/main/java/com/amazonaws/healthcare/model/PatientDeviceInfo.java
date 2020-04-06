/**
 * 
 */
package com.amazonaws.healthcare.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.amazonaws.healthcare.util.SensorType;

import lombok.Data;
import lombok.ToString;

/**
 * @author yashvant
 *
 */
@Data
@ToString
public class PatientDeviceInfo {

	@NotEmpty
	String patientId;
	@NotEmpty
	String deviceId;
	@NotNull
	SensorType sensorType;	
	@NotNull
	DeviceStatus status;
}
