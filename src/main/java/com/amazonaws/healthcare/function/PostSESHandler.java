package com.amazonaws.healthcare.function;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.amazonaws.healthcare.model.EntityValidator;
import com.amazonaws.healthcare.model.HeartRate;
import com.amazonaws.healthcare.model.Patient;
import com.amazonaws.healthcare.model.Provider;
import com.amazonaws.healthcare.model.ServerlessInput;
import com.amazonaws.healthcare.model.ServerlessOutput;
import com.amazonaws.healthcare.util.JsonUtil;
import com.amazonaws.healthcare.util.StatusCode;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class PostSESHandler implements RequestStreamHandler, DynamodbHandler {


	private static final String EMAIL_FROM = System.getenv("EMAIL_FROM");

	// The subject line for the email.
	static final String SUBJECT = "Amazon SES test (AWS SDK for Java)";

	// The HTML body for the email.
	static final String HTMLBODY = "<h1>Amazon SES test (AWS SDK for Java)</h1>"
			+ "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
			+ "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>" + "AWS SDK for Java</a>";

	// The email body for recipients with non-HTML email clients.
	static final String TEXTBODY = "This email was sent through Amazon SES " + "using the AWS SDK for Java.";

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();

		Set<String> errorMessages = new LinkedHashSet<>();

		logger.log(String.format("Patient table name from system environment %s", Constants.PATIENT_TABLE_NAME));
		ServerlessOutput serverlessOutput = new ServerlessOutput();
		try {
			ServerlessInput serverlessInput = JsonUtil.parseObjectFromStream(input, ServerlessInput.class);
			
			String heartRateStr = serverlessInput.getBody();
			logger.log(String.format("Heartrate payload  %s", heartRateStr));
			HeartRate heartRate = JsonUtil.parseObjectFromBytes(heartRateStr.getBytes(), HeartRate.class);

			boolean isValid = new EntityValidator<>().validate.isValid(heartRate, errorMessages);

			if (isValid) {
				String providerEmail=getProviderEmail(getPatinetByKey(heartRate.getPatientId()).getProviderId()) ;
				
				AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
						// Replace US_WEST_2 with the AWS Region you're using for
						// Amazon SES.
						.withRegion(Constants.REGION).build();
				SendEmailRequest request = new SendEmailRequest().withDestination(new Destination().withToAddresses(providerEmail))
						.withMessage(new Message()
								.withBody(new Body()//.withHtml(new Content().withCharset("UTF-8").withData(HTMLBODY))
										.withText(new Content().withCharset("UTF-8").withData("Abnormal heartrate detected details as:  "+JsonUtil.convertToString(heartRate))))
								.withSubject(new Content().withCharset("UTF-8").withData(SUBJECT)))
						.withSource(EMAIL_FROM);
						// Comment or remove the next line if you are not using a
						// configuration set
						//.withConfigurationSetName(CONFIGSET);
				client.sendEmail(request);
				System.out.println("Email sent!"+providerEmail);
				serverlessOutput.setBody("Email sent to "+providerEmail);
				serverlessOutput.setStatusCode(StatusCode.SUCCESS.getCode());
				
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
	

	public String getProviderEmail(String providerId) throws JsonParseException, JsonMappingException, IOException {
		String providerEmail = null;
		
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

		Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
		eav.put(":val1", new AttributeValue().withS(providerId));

		DynamoDBQueryExpression<Provider> queryExpression = new DynamoDBQueryExpression<Provider>()
				.withKeyConditionExpression("id = :val1").withExpressionAttributeValues(eav);
		DynamoDBMapper mapper = new DynamoDBMapper(client);
		Provider provider =mapper.query(Provider.class, queryExpression, getConfig(Constants.PROVIDER_TABLE_NAME)).get(0);
		
		providerEmail = provider.getEmail();

		System.out.println(String.format("providerEmail %s", providerEmail));
		return providerEmail;
	}

	public Patient getPatinetByKey(String patientId) throws JsonParseException, JsonMappingException, IOException {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

		Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
		eav.put(":val1", new AttributeValue().withS(patientId));

		DynamoDBQueryExpression<Patient> queryExpression = new DynamoDBQueryExpression<Patient>()
				.withKeyConditionExpression("id = :val1").withExpressionAttributeValues(eav);
		DynamoDBMapper mapper = new DynamoDBMapper(client);
		Patient patient=mapper.query(Patient.class, queryExpression, getConfig(Constants.PATIENT_TABLE_NAME)).get(0);
		
		System.out.println(String.format("patient %s", patient));
		return patient;
	}
	
}