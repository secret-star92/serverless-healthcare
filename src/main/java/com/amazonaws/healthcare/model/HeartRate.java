/**
 * 
 */
package com.amazonaws.healthcare.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.amazonaws.healthcare.util.LocalDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * @author yashvant
 *
 */
@Data
public class HeartRate {
	private String deviceId;
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime timestamp;
	private int value;

	public void setTimestamp(long _timestamp) {
		timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(_timestamp), ZoneId.systemDefault());
	}

}
