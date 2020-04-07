package com.amazonaws.healthcare.function;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.amazonaws.healthcare.model.Device;
import com.amazonaws.healthcare.model.EntityValidator;
import com.amazonaws.healthcare.model.ServerlessInput;
import com.amazonaws.healthcare.model.ServerlessOutput;
import com.amazonaws.healthcare.util.JsonUtil;
import com.amazonaws.healthcare.util.StatusCode;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class PostDeviceHandler implements RequestStreamHandler, DynamodbHandler {

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();

		Set<String> errorMessages = new LinkedHashSet<>();

		logger.log(String.format("Device table name from system environment %s", Constants.DEVICE_TABLE_NAME));
		ServerlessOutput serverlessOutput = new ServerlessOutput();
		try {
			ServerlessInput serverlessInput = JsonUtil.parseObjectFromStream(input, ServerlessInput.class);

			String deviceStr = (String) serverlessInput.getBody();
			Device device = JsonUtil.parseObjectFromBytes(deviceStr.getBytes(), Device.class);

			boolean isValid = new EntityValidator<>().validate.isValid(device, errorMessages);

			if (isValid) {
				device.setId(UUID.randomUUID().toString());
				
				//Map<String, AttributeValue> attributes = InternalUtils.toAttributeValues(Item.fromJSON(deviceStr));
				//attributes.putIfAbsent(DEVICE_TABLE_ID_NAME, new AttributeValue().withS(device.getId()));
				//attributes.put(DEVICE_TABLE_KEY_NAME, new AttributeValue().withS(device.getType().name()));

				//addAttributes(Constants.DEVICE_TABLE_NAME, attributes);
				save(device);

				serverlessOutput.setStatusCode(StatusCode.SUCCESS.getCode());
				serverlessOutput.setBody(JsonUtil.convertToString(device));
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

}
