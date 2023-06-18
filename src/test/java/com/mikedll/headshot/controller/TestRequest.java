package com.mikedll.headshot.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public record TestRequest(HttpServletRequest req, HttpServletResponse res, PrintWriter printWriter, StringWriter stringWriter) {}
