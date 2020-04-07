/**
 * 
 */
package com.amazonaws.healthcare.model;

import java.util.Base64;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.amazonaws.healthcare.util.ProviderStatus;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;

import lombok.Data;

/**
 * @author yashvant
 *
 */
@Data
@DynamoDBTable(tableName="provider")
public class Provider {
	@DynamoDBHashKey(attributeName="id")
	public String getId() {
		if (email != null)
			return Base64.getEncoder().encodeToString(email.getBytes());
		return null;
	}
	@NotEmpty
	@DynamoDBRangeKey(attributeName="mobile_number")	
	String mobileNumber;	
	@Email
	@NotEmpty
	@DynamoDBAttribute(attributeName="email")
	String email;
	@NotEmpty
	@DynamoDBAttribute(attributeName="firstname")
	String firstname;
	@NotEmpty
	@DynamoDBAttribute(attributeName="lastname")
	String lastname;
	@NotEmpty
	@DynamoDBAttribute(attributeName="specilization")
	String specilization;
	@NotEmpty
	@DynamoDBAttribute(attributeName="address")
	String address;
	@NotNull
	@DynamoDBAttribute(attributeName="status")
	@DynamoDBTypeConvertedEnum
	ProviderStatus status;

}
