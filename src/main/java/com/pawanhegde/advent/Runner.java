package com.pawanhegde.advent;

import com.pawanhegde.advent.helper.SolutionSupplier;
import com.pawanhegde.advent.helper.SubmissionHelper;

/**
 * Entry-point for running Advent of Code solutions.
 */
@SuppressWarnings("unused")
public class Runner {
	private Runner() {
	}

	public static void run(Class<?>... classes) {
		for (Class<?> aClass : classes) {
			SolutionSupplier.getAocSolutions(aClass).forEach(SubmissionHelper::attempt);
		}
	}
}
