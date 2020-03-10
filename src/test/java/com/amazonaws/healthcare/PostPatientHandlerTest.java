package com.amazonaws.healthcare;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.amazonaws.healthcare.function.PatientHandler;
import com.amazonaws.healthcare.model.EntityValidator;
import com.amazonaws.healthcare.model.Patient;
import com.amazonaws.healthcare.model.ServerlessInput;
import com.amazonaws.healthcare.util.JsonUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class PostPatientHandlerTest {

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
    	
    	String body= "{\"id\":\"abc@gmail.com\",\"firstname\":\"Amit\",\"lastname\":\"Gupta\",\"specilization\":\"General Physician\",\"email\":\"abc@gmail.com\",\"mobileNumber\":\"891737283\",\"address\":\"ABC hospital\"}";
    	
    	
    	Map attributes=JsonUtil.parseObjectFromBytes(body.getBytes(), Map.class);
    	System.out.println(attributes);
    	
    	
    	
    }
    
    
    @Test
    public void testRequestToServerlessInput() throws JsonParseException, JsonMappingException, IOException {
    	
    	InputStream inputStream = getClass()
    			.getClassLoader().getResourceAsStream("patientPayload.json");

    	Set<String> errorMessages=new LinkedHashSet<>();
    	/*ServerlessInput<String> input=JsonUtil.parseObjectFromStream(inputStream, new TypeReference<ServerlessInput<String>>() {
		});*/
    	
    	ServerlessInput input=JsonUtil.parseObjectFromStream(inputStream, ServerlessInput.class);
    	
    	
    	String body= input.getBody();
    	
    	System.out.println(body);
    	Patient patient=JsonUtil.parseObjectFromBytes(body.getBytes(), Patient.class);
    	
		
    	 boolean isValid=new EntityValidator<>().validate.isValid(patient, errorMessages);
    	 
    	 
    	 System.out.println(JsonUtil.convertToString(errorMessages) );
 		
    	
    	System.out.println(patient.getId());
    	
    	
    }
}
