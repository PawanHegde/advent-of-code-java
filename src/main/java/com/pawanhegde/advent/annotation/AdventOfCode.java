package com.pawanhegde.advent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to mark a method as an Advent of Code solution.
 * The method must take a single String argument and return a response that can be converted to a String and should
 * be public.
 * By default, the method is only run and the result is printed to the console.
 *
 * @see com.pawanhegde.advent.annotation.AutoSubmit
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AdventOfCode {
	int year();

	int day();

	int part();
}
