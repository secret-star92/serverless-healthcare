/**
 * 
 */
package com.amazonaws.healthcare.function;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author yashvant
 *
 */
public interface DynamodbHandler {

	public default <T> T getItemById(String tableName, Map<String, AttributeValue> key, Class<T> t) {
		AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder.standard().build();
		final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
		return mapper.convertValue(convertToMap(dynamoDb.getItem(tableName, key).getItem()), t);
	}

	public default void addAttributes(String tableName, Map<String, AttributeValue> attributes) {
		AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder.standard().build();
		dynamoDb.putItem(new PutItemRequest().withTableName(tableName).withItem(attributes));
	}

	public static Map<String, String> convertToMap(Map<String, AttributeValue> attributesMap) {
		return Optional.ofNullable(attributesMap).orElseGet(HashMap::new).entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> ((AttributeValue) (e.getValue())).getS()));
	}
}
