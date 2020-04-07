package com.amazonaws.healthcare.function;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import com.amazonaws.healthcare.model.EntityValidator;
import com.amazonaws.healthcare.model.PatientDeviceInfo;
import com.amazonaws.healthcare.model.ServerlessInput;
import com.amazonaws.healthcare.model.ServerlessOutput;
import com.amazonaws.healthcare.util.JsonUtil;
import com.amazonaws.healthcare.util.StatusCode;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class PostPatientDeviceHandler implements RequestStreamHandler, DynamodbHandler {
	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();

		Set<String> errorMessages = new LinkedHashSet<>();

		logger.log(String.format("Patient table name from system environment %s", Constants.PATIENT_DEVICE_TABLE_NAME));
		ServerlessOutput serverlessOutput = new ServerlessOutput();
		try {
			ServerlessInput serverlessInput = JsonUtil.parseObjectFromStream(input, ServerlessInput.class);

			String patientDeviceInfoStr = serverlessInput.getBody();
			logger.log(String.format("Patient Device InfoStr payload  %s", patientDeviceInfoStr));
			PatientDeviceInfo patientDeviceInfo = JsonUtil.parseObjectFromBytes(patientDeviceInfoStr.getBytes(),
					PatientDeviceInfo.class);

			boolean isValid = new EntityValidator<>().validate.isValid(patientDeviceInfo, errorMessages);

			if (isValid) {

				/*
				 * Map<String, AttributeValue> attributes =
				 * InternalUtils.toAttributeValues(Item.fromJSON(patientDeviceInfoStr));
				 * attributes.putIfAbsent(PATIENT_DEVICE_TABLE_ID_NAME, new
				 * AttributeValue().withS(patientDeviceInfo.getPatientId()));
				 * attributes.put(PATIENT_DEVICE__TABLE_KEY_NAME, new
				 * AttributeValue().withS(patientDeviceInfo.getDeviceId()));
				 * 
				 * addAttributes(Constants.PATIENT_DEVICE_TABLE_NAME, attributes);
				 */

				save(patientDeviceInfo);
				serverlessOutput.setStatusCode(StatusCode.SUCCESS.getCode());
				serverlessOutput.setBody(JsonUtil.convertToString(patientDeviceInfo));
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

	/*
	 * public void addAttributes(Map<String, AttributeValue> attributes) {
	 * AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder.standard().build();
	 * dynamoDb.putItem(new
	 * PutItemRequest().withTableName(Constants.PATIENT_TABLE_NAME).withItem(
	 * attributes)); }
	 */

}
