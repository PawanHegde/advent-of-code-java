package com.pawanhegde.advent.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates that the solution should be submitted automatically.
 * The method should be annotated with {@link AdventOfCode} as well.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
public @interface AutoSubmit {
	boolean value() default true;
}
