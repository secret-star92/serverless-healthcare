package com.amazonaws.healthcare;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.amazonaws.healthcare.function.PatientHandler;
import com.amazonaws.healthcare.model.EntityValidator;
import com.amazonaws.healthcare.model.Provider;
import com.amazonaws.healthcare.model.ServerlessInput;
import com.amazonaws.healthcare.util.JsonUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class PostProviderHandlerTest {

    private static final String SAMPLE_INPUT_STRING = "{\"foo\": \"bar\"}";
    private static final String EXPECTED_OUTPUT_STRING = "{\"FOO\": \"BAR\"}";

    @Test
    public void testLambdaFunctionHandler() throws IOException {
        PatientHandler handler = new PatientHandler();

        InputStream input = new ByteArrayInputStream(SAMPLE_INPUT_STRING.getBytes());;
        OutputStream output = new ByteArrayOutputStream();

        handler.handleRequest(input, output, null);

        // TODO: validate output here if needed.
        String sampleOutputString = output.toString();
        System.out.println(sampleOutputString);
        Assert.assertEquals(EXPECTED_OUTPUT_STRING, sampleOutputString);
    }
    
    
    @Test
    public void testGenrateJson() throws IOException {

    	//Provider provider=Provider.builder().name("Amit Gupta").address("ABC hospital").email("abc@gmail.com").mobile("891737283").specilization("General Physician").build();
    	
    	//System.out.println(JsonUtil.convertToString(provider));
    }
    
    
    @Test
    public void testBodyToMap() throws JsonParseException, JsonMappingException, IOException {
    	
    	String body= "{\"id\":\"abc@gmail.com\",\"name\":\"Amit Gupta\",\"specilization\":\"General Physician\",\"email\":\"abc@gmail.com\",\"mobile\":\"891737283\",\"address\":\"ABC hospital\"}";
    	
    	
    	Map attributes=JsonUtil.parseObjectFromBytes(body.getBytes(), Map.class);
    	System.out.println(attributes);
    	
    	
    	
    }
    
    
    @Test
    public void testRequestToServerlessInput() throws JsonParseException, JsonMappingException, IOException {
    	
    	InputStream inputStream = getClass()
    			.getClassLoader().getResourceAsStream("providerPayload.json");

    	Set<String> errorMessages=new LinkedHashSet<>();
    	/*ServerlessInput<String> input=JsonUtil.parseObjectFromStream(inputStream, new TypeReference<ServerlessInput<String>>() {
		});*/
    	
    	ServerlessInput input=JsonUtil.parseObjectFromStream(inputStream, ServerlessInput.class);
    	
    	
    	String body= (String) input.getBody();
    	Provider provider=JsonUtil.parseObjectFromBytes(body.getBytes(), Provider.class);
    	
		
    	 boolean isValid=new EntityValidator<>().validate.isValid(provider, errorMessages);
    	 
    	 
    	 System.out.println(JsonUtil.convertToString(errorMessages) );
 		
    	
    	System.out.println(provider.getId());
    	
    	
    }
}
