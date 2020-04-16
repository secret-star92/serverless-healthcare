package com.amazonaws.healthcare.function;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayInputStream;
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

import com.amazonaws.healthcare.function.PostProviderHandler;
import com.amazonaws.healthcare.model.EntityValidator;
import com.amazonaws.healthcare.model.Provider;
import com.amazonaws.healthcare.model.ServerlessInput;
import com.amazonaws.healthcare.model.ServerlessOutput;
import com.amazonaws.healthcare.util.JsonUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(MockitoJUnitRunner.class)
public class PostProviderHandlerTest {


    private static final String EXPECTED_RESPONSE_BODY_STRING = "{\"id\":\"YWJjQGdtYWlsLmNvbQ==\",\"mobileNumber\":\"891737283\",\"email\":\"abc@gmail.com\",\"firstname\":\"Amit\",\"lastname\":\"Gupta\",\"specilization\":\"General Physician\",\"address\":\"ABC hospital\",\"status\":\"ACTIVE\"}";

    @Test
    public void testLambdaFunctionHandler() throws IOException {
    	PostProviderHandler handler = new PostProviderHandler();
    	PostProviderHandler handlerSpy = spy(handler);
    	
    	InputStream input = getClass().getClassLoader().getResourceAsStream("providerPayload.json");
        OutputStream output = new ByteArrayOutputStream();
        
        doThrow(new RuntimeException()).when(handlerSpy).save(null, null);
        handlerSpy.handleRequest(input, output, new TestContext());

		ServerlessOutput response = JsonUtil.parseObjectFromBytes(output.toString().getBytes(), ServerlessOutput.class);
		Assert.assertEquals(Integer.valueOf(200), response.getStatusCode());
		Assert.assertEquals(EXPECTED_RESPONSE_BODY_STRING, response.getBody());
    }
    
        
	@Test
	public void testRequestToServerlessInput() throws JsonParseException, JsonMappingException, IOException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("providerPayload.json");
		Set<String> errorMessages = new LinkedHashSet<>();
		ServerlessInput input = JsonUtil.parseObjectFromStream(inputStream, ServerlessInput.class);
		String body = (String) input.getBody();
		Provider provider = JsonUtil.parseObjectFromBytes(body.getBytes(), Provider.class);
		boolean isValid = new EntityValidator<>().validate.isValid(provider, errorMessages);
		Assert.assertEquals(true, isValid);
		Assert.assertNotNull(provider);
		Assert.assertEquals(0, errorMessages.size());
		Assert.assertNull(provider.getId());
	}
}
