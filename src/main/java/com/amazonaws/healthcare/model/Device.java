/**
 * 
 */
package com.amazonaws.healthcare.model;

import javax.validation.constraints.NotEmpty;

import com.amazonaws.healthcare.util.SensorType;

import lombok.Builder;
import lombok.Data;

/**
 * @author yashvant
 *
 */
@Data
@Builder
public class Device {
	//UUID 
	String id;
	@NotEmpty
	String patiendId;
	@NotEmpty
	String deviceName;
	@NotEmpty
	SensorType type;
	@NotEmpty
	String ipAddress;
	@NotEmpty
	int port;
	@NotEmpty
	String protocal;

}
