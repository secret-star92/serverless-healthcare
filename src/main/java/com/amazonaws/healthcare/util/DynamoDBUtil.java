package com.amazonaws.healthcare.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class DynamoDBUtil {

	
    public static Map<String, AttributeValue> convert(Map<String, String> map) {
        return Optional.ofNullable(map).orElseGet(HashMap::new).entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, e -> new AttributeValue().withS(e.getValue())));
    }
    
    
    public static  Map<String, AttributeValue> convert(Item item){
    	return InternalUtils.toAttributeValues(item);
    }
    
}
