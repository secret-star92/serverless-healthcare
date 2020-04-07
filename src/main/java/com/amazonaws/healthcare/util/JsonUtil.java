/**
 * 
 */
package com.amazonaws.healthcare.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author yashvant
 *
 */
public class JsonUtil {
	
	public static <T> void writeOutput(OutputStream output, T t)	throws  IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(output, t);
	}

	
	public static <T> T parseObjectFromStream(InputStream in, TypeReference<T> valueType)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return objectMapper.readValue(in, valueType);

	}
	public static <T> T parseObjectFromStream(InputStream in, Class<T> valueType)
			throws  IOException {

		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return objectMapper.readValue(in, valueType);

	}

	public static <T> T parseObjectFromBytes(byte[] body, Class<T> valueType)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper.readValue(body, valueType);

	}

	public static String convertToString(Object value) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(value);
	}

}
