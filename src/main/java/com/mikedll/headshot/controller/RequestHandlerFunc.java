
package com.mikedll.headshot.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.function.Function;

import org.javatuples.Pair;

/*
 * Applies a request/response pair, and returns an error,
 * which is null on success.
 */
public interface RequestHandlerFunc extends Function<Pair<HttpServletRequest,HttpServletResponse>,String> {
}

