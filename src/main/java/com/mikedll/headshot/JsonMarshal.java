package com.mikedll.headshot;

import java.util.List;

import org.javatuples.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.DeserializationFeature;

public class JsonMarshal {

    public static <T> Pair<String,String> marshal(T input) {
        ObjectMapper mapper = new ObjectMapper();
        String marshalled = null;
        try {
            marshalled = mapper.writeValueAsString(input);
        } catch (JsonProcessingException ex) {
            return Pair.with(null, "Error marshalling json: " + ex.getMessage());
        }
        return Pair.with(marshalled, null);
    }

    public static <T> Pair<T, String> unmarshal(String input, TypeReference<T> typeRef) {
        ObjectMapper mapper = new ObjectMapper();
        T unmarshalled = null;
        try {
            unmarshalled = mapper.readValue(input, typeRef);
        } catch (JsonProcessingException ex) {
            return Pair.with(null, "Error unmarshalling json: " + ex.getMessage());
        }
        return Pair.with(unmarshalled, null);        
    }

    /*
     * Doesn't work.
     */
    public static <T> Pair<T, String> convert(JsonNode node, TypeReference<T> typeRef) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return Pair.with(mapper.convertValue(node, typeRef), null);        
    }

    public static Pair<JsonNode, String> getJsonNode(String input) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode ret = null;
        try {
            ret = mapper.readTree(input);
        } catch (JsonProcessingException ex) {
            return Pair.with(null, "Error reading json: " + ex.getMessage());
        }
        return Pair.with(ret, null);        
    }
    
}
