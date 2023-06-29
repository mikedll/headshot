package com.mikedll.headshot.util;

import java.util.List;

import org.javatuples.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.CollectionType;
    
public class JsonMarshal {

    public static <T> Pair<String,String> marshal(ObjectMapper mapper, T input) {
        String marshalled = null;
        try {
            marshalled = mapper.writeValueAsString(input);
        } catch (JsonProcessingException ex) {
            return Pair.with(null, "Error marshalling json: " + ex.getMessage());
        }
        return Pair.with(marshalled, null);
    }

    public static <T> Pair<T, String> unmarshal(ObjectMapper mapper, String input, TypeReference<T> typeRef) {
        T unmarshalled = null;
        try {
            unmarshalled = mapper.readValue(input, typeRef);
        } catch (JsonProcessingException ex) {
            return Pair.with(null, "Error unmarshalling json: " + ex.getMessage());
        }
        return Pair.with(unmarshalled, null);        
    }

    public static <T> Pair<T, String> convert(ObjectMapper mapper, JsonNode node, TypeReference<T> typeRef) {
        return Pair.with(mapper.convertValue(node, typeRef), null);        
    }

    public static Pair<JsonNode, String> getJsonNode(ObjectMapper mapper, String input) {
        JsonNode ret = null;
        try {
            ret = mapper.readTree(input);
        } catch (JsonProcessingException ex) {
            return Pair.with(null, "Error reading json: " + ex.getMessage());
        }
        return Pair.with(ret, null);        
    }
    
}
