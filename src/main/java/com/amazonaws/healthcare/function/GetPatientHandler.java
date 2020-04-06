package com.amazonaws.healthcare.function;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.amazonaws.healthcare.model.EntityValidator;
import com.amazonaws.healthcare.model.Patient;
import com.amazonaws.healthcare.model.ServerlessInput;
import com.amazonaws.healthcare.model.ServerlessOutput;
import com.amazonaws.healthcare.util.JsonUtil;
import com.amazonaws.healthcare.util.StatusCode;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class GetPatientHandler implements RequestStreamHandler {
	// DynamoDB table name for storing Patient metadata.
	private static final String PATIENT_TABLE_NAME = System.getenv("PATIENT_TABLE_NAME");
	// DynamoDB table attribute name for storing patient id.
	private static final String PATIENT_TABLE_ID_NAME = "id";
	// DynamoDB table attribute name for sort key
	private static final String PATIENT_TABLE_KEY_NAME = "provider_id";

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();

		Set<String> errorMessages = new LinkedHashSet<>();

		logger.log(String.format("Patient table name from system environment %s", PATIENT_TABLE_NAME));
		ServerlessOutput serverlessOutput = new ServerlessOutput();
		try {
			ServerlessInput serverlessInput = JsonUtil.parseObjectFromStream(input, ServerlessInput.class);
			
			String patientStr = serverlessInput.getBody();
			logger.log(String.format("patient payload  %s", patientStr));
			Patient patient = JsonUtil.parseObjectFromBytes(patientStr.getBytes(), Patient.class);

			boolean isValid = new EntityValidator<>().validate.isValid(patient, errorMessages);

			if (isValid) {

				Map<String, AttributeValue> attributes = InternalUtils.toAttributeValues(Item.fromJSON(patientStr));
				attributes.putIfAbsent(PATIENT_TABLE_ID_NAME, new AttributeValue().withS(patient.getId()));
				attributes.put(PATIENT_TABLE_KEY_NAME, new AttributeValue().withS(patient.getProviderId()));

				addAttributes(attributes);

				serverlessOutput.setStatusCode(StatusCode.SUCCESS.getCode());
				serverlessOutput.setBody(JsonUtil.convertToString(patient));
			} else {
				serverlessOutput.setStatusCode(StatusCode.VALICATION_FAILED.getCode());
				serverlessOutput.setBody(JsonUtil.convertToString(errorMessages));
				logger.log(String.format("Validation failed %s", errorMessages));
			}

		} catch (IOException exe) {
			serverlessOutput.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR.getCode());
			serverlessOutput.setBody(exe.getMessage());
			logger.log(String.format("internal error occurred  %s", exe.toString()));
			throw exe;

		} finally {
			try {
				JsonUtil.writeOutput(output, serverlessOutput);
			} catch (IOException exe) {
				serverlessOutput.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR.getCode());
				serverlessOutput.setBody(exe.getMessage());
				logger.log(String.format("internal error occurred  %s", exe.toString()));
				throw exe;
			}
		}
	}

	public void addAttributes(Map<String, AttributeValue> attributes) {
		AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder.standard().build();
		dynamoDb.putItem(new PutItemRequest().withTableName(PATIENT_TABLE_NAME).withItem(attributes));
	}

}
