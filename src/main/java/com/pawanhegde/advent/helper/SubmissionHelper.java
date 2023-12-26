package com.pawanhegde.advent.helper;

import com.pawanhegde.advent.AocException;
import com.pawanhegde.advent.Constants;
import com.pawanhegde.advent.annotation.AutoSubmit;
import com.pawanhegde.advent.model.ProblemId;
import com.pawanhegde.advent.model.Submission;
import com.pawanhegde.advent.model.SubmissionStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.pawanhegde.advent.helper.InputHelper.getSample;
import static com.pawanhegde.advent.model.SubmissionStatus.UNKNOWN;
import static com.pawanhegde.advent.util.Log.info;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class SubmissionHelper {
	private SubmissionHelper() {
	}

	public static void attempt(ProblemId problemId, Method method) {
		if (CacheManager.isSubmittedSuccessfully(problemId)) {
			info(problemId + " has already been submitted. Skipping...");
			return;
		}
		attemptOnSampleInput(problemId, method);
		attemptOnActualInput(problemId, method);
	}

	private static void attemptOnSampleInput(ProblemId problemId, Method method) {
		getSample(problemId).map(s -> runMethod(method, s))
				.ifPresent(output -> info(problemId + " (on sample input): " + output));
	}

	private static void attemptOnActualInput(ProblemId problemId, Method method) {
		String input = InputHelper.getInput(problemId);
		String answer = runMethod(method, input);
		info(problemId + ": " + answer);

		if (isAutoSubmit(method)) {
			SubmissionStatus status = CacheManager.judgeBasedOnPreviousSubmissions(problemId, answer);
			if (UNKNOWN == status) {
				Submission submission = submit(problemId, answer);
				info(String.valueOf(submission));
			} else {
				info(problemId + "(based on cache of old attempts): " + status);
			}
		}
	}

	private static String runMethod(Method method, String input) {
		try {
			Class<?> aClass = method.getDeclaringClass();
			Constructor<?> constructor = aClass.getDeclaredConstructor();
			Object instance = constructor.newInstance();
			return String.valueOf(method.invoke(instance, method, input));
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
				 InstantiationException e) {
			String className = method.getDeclaringClass().getName();
			String methodName = method.getName();
			String methodFullName = className + "#" + methodName;
			throw new AocException("Failed to run " + methodFullName + ". Ensure that " + className + " has a default " +
					"constructor and " + methodName + " only accepts one of the following as the input: String, " +
					"List<String>, int[][], String[][]", e);
		}
	}

	private static boolean isAutoSubmit(Method method) {
		return method.isAnnotationPresent(AutoSubmit.class) && method.getAnnotation(AutoSubmit.class).value();
	}

	static Submission submit(ProblemId problemId, String answer) {
		HttpRequest request = createRequest(problemId, answer);
		HttpResponse<String> response = sendRequest(request);
		return createSubmission(problemId, answer, response);
	}

	private static HttpRequest createRequest(ProblemId problemId, String answer) {
		String url = "https://adventofcode.com/" + problemId.year() + "/day/" + problemId.day() + "/answer";
		String token = TokenHelper.fetchToken();

		return HttpRequest.newBuilder()
				.uri(URI.create(url))
				.setHeader("Cookie", "session=" + token)
				.setHeader("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString("level=" + problemId.part() + "&answer=" + answer))
				.build();
	}

	private static HttpResponse<String> sendRequest(HttpRequest request) {
		try {
			return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			throw new AocException("Could not get a response from the server", e);
		}
	}

	private static Submission createSubmission(ProblemId problemId, String answer, HttpResponse<String> response) {
		String message = getMessage(response.body());
		Submission submission = new Submission(problemId, answer, Instant.now().toEpochMilli(), message);
		if (UNKNOWN.equals(submission.status())) {
			submission = tryToDecipherTheResponse(submission);
		} else {
			CacheManager.cache(singletonList(submission));
		}
		return submission;
	}

	static String getMessage(String responseBody) {
		Document document = Jsoup.parse(responseBody);
		return document.selectXpath("//article/p").text();
	}

	private static Submission tryToDecipherTheResponse(Submission submission) {
		// If you submit an answer for a problem that you have already solved, the server sends back a
		// not-so-helpful message. We use this opportunity to grab the solution from the problem page and
		// cache it so that we don't retry sending the same answer again. This can happen if you have
		// submitted the answer manually on the website, or the cache got deleted somehow.
		ProblemId problemId = submission.problemId();
		String answer = submission.answer();
		List<Submission> submissions = tryToGetCorrectAnswersFromTheProblemPage(problemId);
		CacheManager.cache(submissions);
		SubmissionStatus submissionStatus = CacheManager.judgeBasedOnPreviousSubmissions(problemId, answer);
		String fakedMessage = createFakedMessage(submissionStatus);
		return new Submission(problemId, answer, Instant.now().toEpochMilli(), fakedMessage);
	}

	private static List<Submission> tryToGetCorrectAnswersFromTheProblemPage(ProblemId problemId) {
		String problemText = getProblemText(problemId);
		List<String> correctAnswers = getCorrectAnswers(problemText);
		return createSubmissions(problemId, correctAnswers);
	}

	private static List<Submission> createSubmissions(ProblemId problemId, List<String> correctAnswers) {
		long currentTimeEpochMilli = Instant.now().toEpochMilli();
		ProblemId partOne = new ProblemId(problemId.year(), problemId.day(), 1);
		ProblemId partTwo = new ProblemId(problemId.year(), problemId.day(), 2);

		return switch (correctAnswers.size()) {
			case 1 ->
					singletonList(new Submission(partOne, correctAnswers.get(0), currentTimeEpochMilli, Constants.CORRECT));
			case 2 -> List.of(
					new Submission(partOne, correctAnswers.get(0), currentTimeEpochMilli, Constants.CORRECT),
					new Submission(partTwo, correctAnswers.get(1), currentTimeEpochMilli, Constants.CORRECT)
			);
			default -> emptyList();
		};
	}

	private static String createFakedMessage(SubmissionStatus submissionStatus) {
		return switch (submissionStatus) {
			case CORRECT -> Constants.CORRECT;
			case TOO_HIGH -> Constants.TOO_HIGH;
			case TOO_LOW -> Constants.TOO_LOW;
			default -> Constants.INCORRECT;
		};
	}

	private static String getProblemText(ProblemId problemId) {
		String url = "https://adventofcode.com/" + problemId.year() + "/day/" + problemId.day();
		String token = TokenHelper.fetchToken();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.setHeader("Cookie", "session=" + token)
				.build();

		try {
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			return response.body();
		} catch (IOException | InterruptedException e) {
			throw new AocException("Could not get a response from the server", e);
		}
	}

	private static List<String> getCorrectAnswers(String problemText) {
		String regex = "<p>Your puzzle answer was <code>(.+)</code>.</p>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(problemText);

		List<String> correctAnswers = new ArrayList<>();
		while (matcher.find()) {
			String answer = matcher.group(1);
			correctAnswers.add(answer);
		}
		return correctAnswers;
	}
}
