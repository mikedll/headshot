
package com.mikedll.headshot.experiment;

import java.util.function.Function;

import org.javatuples.Pair;

interface AnimalHandler extends Function<Pair<String,Integer>,Pair<String,String>> {
}
