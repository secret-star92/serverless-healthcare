/**
 * 
 */
package com.amazonaws.healthcare.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

	public void setTimestamp(long _timestamp) {
		timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(_timestamp), ZoneId.systemDefault());
	}

}
