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

    public static <T> Pair<T, String> unmarshal(String input) {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<T> typeRef = new TypeReference<T>() {};
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
    public static <T> Pair<T, String> convert(JsonNode node) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        TypeReference<T> typeRef = new TypeReference<T>() {};
        System.out.println("typeRef: " + typeRef);
        T unmarshalled = null;

        unmarshalled = mapper.convertValue(node, typeRef);
        return Pair.with(unmarshalled, null);        
    }

    public static <T> Pair<List<T>, String> convertToList(JsonNode node, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        CollectionType javaType = mapper.getTypeFactory()
            .constructCollectionType(List.class, clazz);

        List<T> unmarshalled = null;
        unmarshalled = mapper.convertValue(node, javaType);
        return Pair.with(unmarshalled, null);
    }
    
    
    public static <T> Pair<T, String> convert(JsonNode node, CollectionType collectionType) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        T unmarshalled = null;

        unmarshalled = mapper.convertValue(node, collectionType);
        return Pair.with(unmarshalled, null);        
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
