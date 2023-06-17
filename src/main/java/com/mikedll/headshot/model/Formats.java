package com.mikedll.headshot.model;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class Formats {

    public static final String PRETTY_TIME = "MMM d, uuuu hh:mm:ssa";

    public static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Los_Angeles");

    public static final DateTimeFormatter PRETTY_TIME_FORMATTER = DateTimeFormatter.ofPattern(Formats.PRETTY_TIME)
        .withZone(ZoneId.systemDefault());
        
}
