package com.pawanhegde.advent.helper;

import com.pawanhegde.advent.AocException;
import com.pawanhegde.advent.model.ProblemId;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static com.pawanhegde.advent.util.Log.info;

public class InputHelper {
	private static final String INPUT_FILE = "input.txt";
	private static final String SAMPLE_FILE = "sample.txt";
	private static final Path TEST_RESOURCES_FOLDER = Path.of("src/test/resources/");
	private InputHelper() {
	}

	public static Optional<String> getSample(ProblemId problemId) {
		Optional<String> sample = fetchFromLocal(SAMPLE_FILE, problemId);
		if (sample.isEmpty()) {
			info("If you want to run the solutions on the sample input, add a file called " + SAMPLE_FILE
					+ " in src/test/resources/" + problemId.day() + "/" + SAMPLE_FILE);
		}
		return sample;
	}

	public static String getInput(ProblemId problemId) {
		Path pathToInputFile = pathTo(problemId, INPUT_FILE);
		if (!Files.exists(pathToInputFile)) {
			try {
				downloadInput(problemId);
			} catch (IOException | InterruptedException e) {
				throw new AocException("Could not download input for " + problemId + ". " +
						"You can either retry, or add a file called " + INPUT_FILE, e);
			}
		}

		return fetchFromLocal(INPUT_FILE, problemId).orElseThrow();
	}

	private static Path pathTo(ProblemId problemId, String fileName) {
		return TEST_RESOURCES_FOLDER.resolve(String.valueOf(problemId.day())).resolve(fileName);
	}

	private static void downloadInput(ProblemId problemId) throws IOException, InterruptedException {
		String input = fetchFromServer(problemId);
		storeInputLocally(input, problemId);
	}

	private static Optional<String> fetchFromLocal(String fileName, ProblemId problemId) {
		Path pathToSample = pathTo(problemId, fileName);
		try {
			return Optional.of(Files.readString(pathToSample));
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	private static String fetchFromServer(ProblemId problemId)
			throws IOException, InterruptedException {
		String url = "https://adventofcode.com/" + problemId.year() + "/day/" + problemId.day() + "/input";
		String token = TokenHelper.fetchToken();

		HttpRequest request =
				HttpRequest.newBuilder().uri(URI.create(url)).setHeader("Cookie", "session=" + token).GET().build();

		HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			throw new AocException("Could not fetch input from server.");
		}
		return response.body();
	}

	private static void storeInputLocally(String input, ProblemId problemId) {
		Path pathToInput = pathTo(problemId, INPUT_FILE);
		try {
			Files.writeString(pathToInput, input);
		} catch (IOException e) {
			throw new AocException("Failed to store the input for " + problemId, e);
		}
	}
}
