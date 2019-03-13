package com.gillsoft.client;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CostMapDeserializer extends JsonDeserializer<Map<String, Cost>> {

	@Override
	public Map<String, Cost> deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		ObjectCodec codec = jp.getCodec();
	    JsonNode node = codec.readTree(jp);
	    for (Iterator<JsonNode> iterator = node.elements(); iterator.hasNext();) {
	    	JsonNode child = iterator.next();
	    	String value = child.asText();
			if (value != null
					&& !value.isEmpty()) {
				iterator.remove();
			}
		}
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(node.toString(), new TypeReference<Map<String, Cost>>() {});
	}
	
}
