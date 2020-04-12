/**
 * 
 */
package com.amazonaws.healthcare.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.validation.constraints.NotEmpty;

import com.amazonaws.healthcare.util.LocalDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * @author yashvant
 *
 */
@Data
public class HeartRate {
	@NotEmpty
	private String deviceId;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime timestamp;
	@NotEmpty
	private String patientId;
	private int value;

	public void setTimestamp(Object _timestamp) {
		if(_timestamp instanceof Long) {
			timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli((Long)_timestamp), ZoneId.systemDefault());
		}else if(_timestamp instanceof String) {
			timestamp = LocalDateTime.parse((String)_timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
		}
		
	}

	
	
}
