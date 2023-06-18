package com.mikedll.headshot.controller;

import java.util.Map;

public record PathMatch(String matched, Map<String,String> extractedParams) {}
