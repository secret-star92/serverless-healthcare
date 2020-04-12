package com.amazonaws.healthcare.function;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.amazonaws.healthcare.model.DeviceStatus;
import com.amazonaws.healthcare.model.HeartRate;
import com.amazonaws.healthcare.model.PatientDeviceInfo;
import com.amazonaws.healthcare.model.ServerlessInput;
import com.amazonaws.healthcare.model.ServerlessOutput;
import com.amazonaws.healthcare.util.JsonUtil;
import com.amazonaws.healthcare.util.SensorType;
import com.amazonaws.healthcare.util.StatusCode;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class GetHeartRateHandler implements RequestStreamHandler, DynamodbHandler {

	private static final String PATIENT_ID = "patientId";
	LambdaLogger logger;

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		logger = context.getLogger();

		Set<String> errorMessages = new LinkedHashSet<>();

		logger.log(String.format("Heart rate table name from system environment %s", Constants.HEART_RATE_TABLE_NAME));
		ServerlessOutput serverlessOutput = new ServerlessOutput();
		try {
			Boolean todayData = null;
			ServerlessInput serverlessInput = JsonUtil.parseObjectFromStream(input, ServerlessInput.class);

			String patientId = (String) serverlessInput.getPathParameters().get(PATIENT_ID);
			if (serverlessInput.getQueryStringParameters() != null
					&& serverlessInput.getQueryStringParameters().get("todayData") != null) {
				todayData = Boolean.parseBoolean((String) serverlessInput.getQueryStringParameters().get("todayData"));
			}

			logger.log(String.format("Heart rate patientId %s, todayData= %s ", patientId, todayData));

			boolean isValid = StringUtils.isNullOrEmpty(patientId);

			if (!isValid) {

				List<PatientDeviceInfo> patientDeviceInfos = getActiveHeartRateSensor(patientId);

				patientDeviceInfos.stream().forEach(d -> System.out.println(String.format("Patient DeviceInfo %s", d)));

				for (PatientDeviceInfo patientDeviceInfo : patientDeviceInfos) {
					if (todayData != null && todayData) {
						serverlessOutput.setBody(getTodayHeartRateData(patientDeviceInfo.getDeviceId()));
					} else {
						serverlessOutput.setBody(getLast10MinutesHeartRateData(patientDeviceInfo.getDeviceId()));
					}
				}

				serverlessOutput.setStatusCode(StatusCode.SUCCESS.getCode());

			} else {
				serverlessOutput.setStatusCode(StatusCode.VALICATION_FAILED.getCode());
				serverlessOutput.setBody(JsonUtil.convertToString(errorMessages));
				logger.log(String.format("Validation failed %s", errorMessages));
			}

		} catch (Exception exe) {
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

	public List<PatientDeviceInfo> getActiveHeartRateSensor(String patinetId) {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

		Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
		eav.put(":val1", new AttributeValue().withS(patinetId));

		DynamoDBQueryExpression<PatientDeviceInfo> queryExpression = new DynamoDBQueryExpression<PatientDeviceInfo>()
				.withKeyConditionExpression("patient_id = :val1").withExpressionAttributeValues(eav);
		DynamoDBMapper mapper = new DynamoDBMapper(client);
		return mapper.query(PatientDeviceInfo.class, queryExpression, getConfig(Constants.PATIENT_DEVICE_TABLE_NAME))
				.stream().filter(d -> SensorType.HEART_RATE.equals(d.getSensorType())
						&& DeviceStatus.ACTIVE.equals(d.getDeviceStatus()))
				.collect(Collectors.toList());

	}

	public String getTodayHeartRateData(String deviceId) throws JsonParseException, JsonMappingException, IOException {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		DynamoDB dynamoDB = new DynamoDB(client);

		Table table = dynamoDB.getTable(Constants.HEART_RATE_TABLE_NAME);
		QuerySpec spec = new QuerySpec().withAttributesToGet("payload")
				.withHashKey(new KeyAttribute("deviceId", deviceId))
				.withRangeKeyCondition(new RangeKeyCondition("timestamp")
						.ge(Timestamp.valueOf(LocalDate.now().atStartOfDay()).getTime()));

		logger.log(String.format("** HEART_RATE_TABLE_NAME spec  %s", spec.toString()));
		ItemCollection<QueryOutcome> items = table.query(spec);

		Iterator<Item> iterator = items.iterator();
		Item item = null;
		List<HeartRate> hRList = new ArrayList<>();
		while (iterator.hasNext()) {
			item = iterator.next();
			logger.log("PAYLOAD : %s" + item.get("payload"));

			HeartRate heartRate = JsonUtil.parseObjectFromBytes(item.getJSON("payload").getBytes(), HeartRate.class);

			logger.log("HeartRate PAYLOAD : " + heartRate);

			hRList.add(heartRate);

		}
		return JsonUtil.convertToString(hRList);
	}

	public String getLast10MinutesHeartRateData(String deviceId)
			throws JsonParseException, JsonMappingException, IOException {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		DynamoDB dynamoDB = new DynamoDB(client);

		Table table = dynamoDB.getTable(Constants.HEART_RATE_TABLE_NAME);
		QuerySpec spec = new QuerySpec().withAttributesToGet("payload")
				.withHashKey(new KeyAttribute("deviceId", deviceId))
				.withRangeKeyCondition(new RangeKeyCondition("timestamp")
						.ge(Timestamp.valueOf(LocalDateTime.now().minusMinutes(10)).getTime()));

		logger.log(String.format("** HEART_RATE_TABLE_NAME spec  %s", spec.toString()));
		ItemCollection<QueryOutcome> items = table.query(spec);

		Iterator<Item> iterator = items.iterator();
		Item item = null;
		List<HeartRate> hRList = new ArrayList<>();
		while (iterator.hasNext()) {
			item = iterator.next();
			logger.log("PAYLOAD : %s" + item.get("payload"));

			HeartRate heartRate = JsonUtil.parseObjectFromBytes(item.getJSON("payload").getBytes(), HeartRate.class);

			logger.log("HeartRate PAYLOAD : " + heartRate);

			hRList.add(heartRate);

		}
		return JsonUtil.convertToString(hRList);
	}
}
