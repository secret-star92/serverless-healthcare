/**
 * 
 */
package com.amazonaws.healthcare.model;

import java.time.LocalDate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.amazonaws.healthcare.util.LocalDateConverter;
import com.amazonaws.healthcare.util.LocalDateDeserializer;
import com.amazonaws.healthcare.util.LocalDateSerializer;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * @author yashvant
 *
 */
@Data
@DynamoDBTable(tableName = "patient")
public class Patient {

	@DynamoDBHashKey(attributeName = "id")
	String id;
	@NotEmpty
	@DynamoDBRangeKey(attributeName = "provider_id")
	String providerId;
	@NotEmpty
	@DynamoDBAttribute(attributeName = "firstname")
	String firstname;
	@NotEmpty
	@DynamoDBAttribute(attributeName = "lastname")
	String lastname;
	@Email
	@NotEmpty
	@DynamoDBAttribute(attributeName = "email")
	String email;
	@NotEmpty
	@DynamoDBAttribute(attributeName = "mobile_number")
	String mobileNumber;
	@NotEmpty
	@DynamoDBAttribute(attributeName = "address")
	String address;
	@NotNull
	@DynamoDBAttribute(attributeName = "date_of_birth")
	@DynamoDBTypeConverted(converter=LocalDateConverter.class )
	@JsonDeserialize(using = LocalDateDeserializer.class )
	@JsonSerialize(using= LocalDateSerializer.class)
	LocalDate dateOfBirth;

}
