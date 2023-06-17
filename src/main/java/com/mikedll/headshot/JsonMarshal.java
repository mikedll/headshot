package com.mikedll.headshot;

import org.javatuples.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

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
}
