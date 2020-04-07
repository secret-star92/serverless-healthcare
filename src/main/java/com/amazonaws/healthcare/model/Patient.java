/**
 * 
 */
package com.amazonaws.healthcare.model;

import java.sql.Date;
import java.util.Base64;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.Data;

/**
 * @author yashvant
 *
 */
@Data
@DynamoDBTable(tableName = "patient")
public class Patient {

	@DynamoDBHashKey(attributeName = "id")
	public String getId() {
		if (email != null) {
			return Base64.getEncoder().encodeToString(email.getBytes());
		}
		return null;
	}

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
	Date dateOfBirth;

}
