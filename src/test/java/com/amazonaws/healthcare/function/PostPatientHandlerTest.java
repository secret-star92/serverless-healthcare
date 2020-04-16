package com.amazonaws.healthcare.function;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.healthcare.function.PostPatientHandler;
import com.amazonaws.healthcare.model.EntityValidator;
import com.amazonaws.healthcare.model.Patient;
import com.amazonaws.healthcare.model.ServerlessInput;
import com.amazonaws.healthcare.model.ServerlessOutput;
import com.amazonaws.healthcare.util.JsonUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(MockitoJUnitRunner.class)
public class PostPatientHandlerTest {

	private static final String EXPECTED_RESPONSE_BODY_STRING = "{\"id\":\"YWJjQGdtYWlsLmNvbQ==\",\"providerId\":\"abc@gmail.com\",\"firstname\":\"Amit\",\"lastname\":\"Gupta\",\"email\":\"abc@gmail.com\",\"mobileNumber\":\"891737283\",\"address\":\"ABC hospital\",\"dateOfBirth\":\"2012-03-21\"}";

	@Test
	public void testLambdaFunctionHandler() throws IOException {
		PostPatientHandler handler = new PostPatientHandler();
		PostPatientHandler handlerSpy = spy(handler);

		InputStream input = getClass().getClassLoader().getResourceAsStream("patientPayload.json");
		OutputStream output = new ByteArrayOutputStream();

		doThrow(new RuntimeException()).when(handlerSpy).save(null, null);
		handlerSpy.handleRequest(input, output, new TestContext());

		ServerlessOutput response = JsonUtil.parseObjectFromBytes(output.toString().getBytes(), ServerlessOutput.class);
		Assert.assertEquals(Integer.valueOf(200), response.getStatusCode());
		Assert.assertEquals(EXPECTED_RESPONSE_BODY_STRING, response.getBody());
	}

	@Test
	public void testRequestToServerlessInput() throws JsonParseException, JsonMappingException, IOException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("patientPayload.json");
		Set<String> errorMessages = new LinkedHashSet<>();
		ServerlessInput input = JsonUtil.parseObjectFromStream(inputStream, ServerlessInput.class);
		String body = input.getBody();
		Patient patient = JsonUtil.parseObjectFromBytes(body.getBytes(), Patient.class);
		boolean isValid = new EntityValidator<>().validate.isValid(patient, errorMessages);

		Assert.assertEquals(true, isValid);
		Assert.assertNotNull(patient);
		Assert.assertEquals(0, errorMessages.size());
		Assert.assertNull(patient.getId());
	}
}
