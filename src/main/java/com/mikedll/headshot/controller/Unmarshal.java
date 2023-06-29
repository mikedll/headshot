package com.mikedll.headshot.controller;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import org.javatuples.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.IOUtils;

import com.mikedll.headshot.util.JsonMarshal;

public class Unmarshal {

    public static <T> Pair<T,String> go(ObjectMapper mapper, HttpServletRequest req, TypeReference<T> typeRef) {
        try { 
            return JsonMarshal.unmarshal(mapper, IOUtils.toString(req.getReader()), typeRef);
        } catch (IOException ex) {
            return Pair.with(null, "IOException when unmarshalling JSON body: " + ex.getMessage());
        }
    }
}
