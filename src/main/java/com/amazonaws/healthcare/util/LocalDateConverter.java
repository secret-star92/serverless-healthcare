package com.amazonaws.healthcare.util;

import java.time.LocalDate;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class LocalDateConverter implements DynamoDBTypeConverter<String, LocalDate> {

	@Override
	public LocalDate unconvert(final String stringValue) {
		return LocalDate.parse(stringValue);
	}

	@Override
	public String convert(LocalDate date) {
		return date.toString();
	}

}