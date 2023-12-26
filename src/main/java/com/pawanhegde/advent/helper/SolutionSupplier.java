package com.pawanhegde.advent.helper;

import com.pawanhegde.advent.AocException;
import com.pawanhegde.advent.annotation.AdventOfCode;
import com.pawanhegde.advent.model.ProblemId;

import java.lang.reflect.Method;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Comparator.comparing;

public class SolutionSupplier {
	private SolutionSupplier() {
	}

	public static SortedMap<ProblemId, Method> getAocSolutions(Object instance) {
		TreeMap<ProblemId, Method> problemsToSolutions = new TreeMap<>(comparing(p -> p.year() + p.day() + p.part()));
		for (Method method : instance.getClass().getMethods()) {
			Optional<ProblemId> problemId = extractProblemIdFromAnnotation(method).or(() -> extractProblemIdFromName(method));

			problemId.ifPresent(problem -> {
				checkIfProblemIdIsValid(problem, method.getName());
				checkIfDuplicateSolutions(problemsToSolutions, problem, method);
				problemsToSolutions.put(problem, method);
			});
		}
		return problemsToSolutions;
	}

	private static Optional<ProblemId> extractProblemIdFromAnnotation(Method method) {
		return Optional.ofNullable(method.getAnnotation(AdventOfCode.class))
				.map(aoc -> new ProblemId(aoc.year(), aoc.day(), aoc.part()));
	}

	/**
	 * Extracts the problem id from the method name. The method name should be of the form adventYYYYDDP where YYYY is the
	 * year, DD is the day and P is the part.
	 * For example, advent2020011 is the solution for the first part of the problem on day 1 of 2020, and
	 * advent2023012 is the solution for the second part of the problem on day 3 of 2023.
	 *
	 * @param method the method to extract the problem id from
	 * @return the problem id if the method name matches the pattern, empty otherwise
	 */
	private static Optional<ProblemId> extractProblemIdFromName(Method method) {
		String methodName = method.getName();
		if (!methodName.startsWith("advent")) {
			return Optional.empty();
		}
		try {
			int year = Integer.parseInt(methodName.substring(6, 10));
			int day = Integer.parseInt(methodName.substring(10, 12));
			int part = Integer.parseInt(methodName.substring(12, 13));
			return Optional.of(new ProblemId(year, day, part));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	private static void checkIfProblemIdIsValid(ProblemId problem, String name) {
		if (problem.year() < 2015) {
			throw new AocException("Advent of code started in 2015. " + name + " does not have a valid year.");
		}
		if (problem.day() < 1 || problem.day() > 25) {
			throw new AocException("There are only 25 days in Advent of Code. " + name + " does not have a valid day.");
		}
		if (problem.part() < 1 || problem.part() > 2) {
			throw new AocException("There are only 2 parts to each problem. " + name + " does not have a valid part id.");
		}

		int year = ZonedDateTime.now(ZoneOffset.UTC).getYear();
		int day = ZonedDateTime.now(ZoneOffset.UTC).getDayOfMonth();
		if (problem.year() > year || (problem.year() == year && problem.day() > day)) {
			throw new AocException("The puzzle for " + problem + " has not been released yet. Please double-check " + name + ".");
		}
	}

	private static void checkIfDuplicateSolutions(TreeMap<ProblemId, Method> problemsToSolutions, ProblemId problem, Method method) {
		Method existingSolution = problemsToSolutions.get(problem);
		if (existingSolution != null && !existingSolution.equals(method)) {
			throw new AocException("Multiple solutions found for " + problem + ". " + getFullMethodName(existingSolution) +
					" and " + getFullMethodName(method) + " both match. Please only keep one of them.");
		}
	}

	private static String getFullMethodName(Method method) {
		return method.getDeclaringClass().getName() + "#" + method.getName();
	}
}
