package com.pawanhegde.advent.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawanhegde.advent.AocException;
import com.pawanhegde.advent.model.ProblemId;
import com.pawanhegde.advent.model.Submission;
import com.pawanhegde.advent.model.SubmissionStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pawanhegde.advent.model.SubmissionStatus.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class CacheManager {
	private static final Path CACHE_FILE = Path.of("/tmp/aoc/cache.json");
	private static final Map<ProblemId, List<Submission>> CACHE = new HashMap<>();

	static {
		initialize();
	}

	private CacheManager() {
	}

	public static void cache(List<Submission> submissions) {
		submissions.forEach(s -> CACHE.computeIfAbsent(s.problemId(), k -> new ArrayList<>()).add(s));
	}

	public static boolean isSubmittedSuccessfully(ProblemId problemId) {
		return CACHE.getOrDefault(problemId, emptyList()).stream().anyMatch(s -> CORRECT == s.status());
	}

	public static SubmissionStatus judgeBasedOnPreviousSubmissions(ProblemId problemId, String answer) {
		for (Submission submission : CACHE.getOrDefault(problemId, emptyList())) {
			if (submission.answer().equals(answer)) {
				return submission.status();
			}

			if (isNumeric(submission.answer()) && isNumeric(answer)) {
				return judgeNumericAnswer(submission, answer);
			}
		}
		return SubmissionStatus.UNKNOWN;
	}

	private static void initialize() {
		try {
			createCacheFileIfNotExists();
			populateCacheFromFile();
		} catch (IOException e) {
			throw new AocException("Could not initialize the cache", e);
		}

		addShutdownHook();
	}

	private static void createCacheFileIfNotExists() throws IOException {
		if (Files.notExists(CACHE_FILE)) {
			Files.createDirectories(CACHE_FILE.getParent());
			Files.createFile(CACHE_FILE);
		}
	}

	private static void populateCacheFromFile() throws IOException {
		String jsonContent = Files.readString(CACHE_FILE);
		if (!jsonContent.isBlank()) {
			List<Submission> submissions = new ObjectMapper().readValue(jsonContent, new TypeReference<>() {
			});
			submissions.forEach(s -> CACHE.computeIfAbsent(s.problemId(), k -> new ArrayList<>()).add(s));
		}
	}

	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				writeCacheToFile();
			} catch (IOException e) {
				throw new AocException("Could not update the cache", e);
			}
		}));
	}

	private static void writeCacheToFile() throws IOException {
		List<Submission> submissions = CACHE.values().stream().flatMap(List::stream).toList();
		new ObjectMapper().writeValue(CACHE_FILE.toFile(), submissions);
	}

	private static SubmissionStatus judgeNumericAnswer(Submission submission, String answer) {
		double currentAnswer = Double.parseDouble(answer);
		double previousAnswer = Double.parseDouble(submission.answer());
		SubmissionStatus previousStatus = submission.status();
		if (currentAnswer < previousAnswer && asList(TOO_LOW, CORRECT).contains(previousStatus)) {
			return TOO_LOW;
		}

		if (currentAnswer > previousAnswer && asList(TOO_HIGH, CORRECT).contains(previousStatus)) {
			return TOO_HIGH;
		}

		return SubmissionStatus.UNKNOWN;
	}

	private static boolean isNumeric(String strNum) {
		try {
			Double.parseDouble(strNum);
			return true;
		} catch (NumberFormatException | NullPointerException e) {
			return false;
		}
	}
}
