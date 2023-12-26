package com.pawanhegde.advent;

public final class Constants {
	public static final String CORRECT = "That's the right answer!";
	public static final String INCORRECT = "That's not the right answer";
	public static final String TOO_LOW = "your answer is too low";
	public static final String TOO_HIGH = "your answer is too high";
	public static final String ALREADY_SOLVED = "Did you already complete it";
	public static final String NOT_OPEN_YET = "Please don't repeatedly request this endpoint before it unlocks! " +
			"The calendar countdown is synchronized with the server time; " +
			"the link will be enabled on the calendar the instant this puzzle becomes available.";
	private Constants() {
	}
}
