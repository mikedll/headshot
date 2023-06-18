package com.mikedll.headshot.controller;

import java.util.Optional;
import java.util.function.Function;

import org.javatuples.Pair;

public interface PathMatchFunc extends Function<Pair<String, HttpMethod>,Optional<PathMatch>> {}
