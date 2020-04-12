package com.amazonaws.healthcare.function;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.healthcare.model.HeartRate;
import com.amazonaws.healthcare.model.Patient;
import com.amazonaws.healthcare.model.Provider;
import com.amazonaws.healthcare.util.JsonUtil;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsOutputDeliveryEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsOutputDeliveryEvent.Record;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class KinesisHeartRateSESHandler
		implements RequestHandler<KinesisAnalyticsOutputDeliveryEvent, Void>, DynamodbHandler {

	// Replace sender@example.com with your "From" address.
	// This address must be verified with Amazon SES.
	// static final String FROM = "sender@example.com";
	private static final String EMAIL_FROM = System.getenv("EMAIL_FROM");

	// The configuration set to use for this email. If you do not want to use a
	// configuration set, comment the following variable and the
	// .withConfigurationSetName(CONFIGSET); argument below.
	static final String CONFIGSET = "ConfigSet";

	// The subject line for the email.
	static final String SUBJECT = "Abnormal Heartrate";

	// The HTML body for the email.
	static final String HTMLBODY = "<h1>Amazon SES test (AWS SDK for Java)</h1>"
			+ "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
			+ "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>" + "AWS SDK for Java</a>";

	// The email body for recipients with non-HTML email clients.
	static final String TEXTBODY = "This email was sent through Amazon SES " + "using the AWS SDK for Java.";

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

	@Override
	public Void handleRequest(KinesisAnalyticsOutputDeliveryEvent input, Context context) {

		LambdaLogger logger = context.getLogger();

		logger.log("KinesisAnalyticsOutputDeliveryEvent " + input);

		List<Record> records = input.getRecords();

		logger.log("Number of records  " + records.size());

		for (Record record : records) {
			
			try {
				logger.log(new String(record.data.array(), "UTF-8"));
				HeartRate heartRate = JsonUtil.parseObjectFromBytes(record.data.array(), HeartRate.class);
				String providerEmail = getProviderEmail(getPatinetByKey(heartRate.getPatientId()).getProviderId());

				AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
						// Replace US_WEST_2 with the AWS Region you're using for
						// Amazon SES.
						.withRegion(Constants.REGION).build();
				SendEmailRequest request = new SendEmailRequest()
						.withDestination(new Destination().withToAddresses(providerEmail))
						.withMessage(new Message()
								.withBody(new Body()
										//.withHtml(new Content().withCharset("UTF-8").withData(HTMLBODY))
										.withText(new Content().withCharset("UTF-8").withData("Abnormal heartrate detected details as:  "+JsonUtil.convertToString(heartRate)))
										)
								.withSubject(new Content().withCharset("UTF-8").withData(SUBJECT)))
						.withSource(EMAIL_FROM);
				client.sendEmail(request);
				System.out.println("Email sent!" + providerEmail);

				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		return null;
	}

}