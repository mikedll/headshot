
package com.mikedll.headshot.controller;

import java.util.Map;
import java.util.function.Function;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.javatuples.Quartet;

import com.mikedll.headshot.Application;

/*
 * Applies a request/response pair, and returns an error,
 * which is null on success.
 */
public interface RequestHandlerFunc extends Function<Quartet<Application,HttpServletRequest,HttpServletResponse,PathMatch>,String> {
}

