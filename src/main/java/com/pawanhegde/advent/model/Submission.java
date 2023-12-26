package com.pawanhegde.advent.model;

import static com.pawanhegde.advent.Constants.*;

public record Submission(ProblemId problemId, String answer, long epochMilli, String response) {
	public SubmissionStatus status() {
		if (response.contains(CORRECT)) {
			return SubmissionStatus.CORRECT;
		}
		if (response.contains(INCORRECT)) {
			return SubmissionStatus.INCORRECT;
		}
		if (response.contains(TOO_HIGH)) {
			return SubmissionStatus.TOO_HIGH;
		}
		if (response.contains(TOO_LOW)) {
			return SubmissionStatus.TOO_LOW;
		}

		// You might have already solved it, or you might have submitted an answer for something that isn't open yet.
		// Or perhaps AOC changed their response format.
		return SubmissionStatus.UNKNOWN;
	}

	@Override
	public String toString() {
		return problemId + ": " + status() + " (" + response + ")";
	}
}
