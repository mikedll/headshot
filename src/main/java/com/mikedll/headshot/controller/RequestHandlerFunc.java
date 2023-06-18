
package com.mikedll.headshot.controller;

import java.util.Map;
import java.util.function.Function;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.javatuples.Triplet;

/*
 * Applies a request/response pair, and returns an error,
 * which is null on success.
 */
public interface RequestHandlerFunc extends Function<Triplet<HttpServletRequest,HttpServletResponse,Map<String,String>>,String> {
}

