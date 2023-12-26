package com.pawanhegde.advent.util;

public class Log {
	private Log() {
	}

	public static void debug(String message) {
		printWithColour(message, Color.BLUE);
	}

	public static void info(String message) {
		printWithColour(message, Color.WHITE);
	}

	public static void warn(String message) {
		printWithColour(message, Color.YELLOW);
	}

	public static void error(String message) {
		printWithColour(message, Color.RED);
	}

	private static void printWithColour(String message, Color color) {
		var asciiColorReset = "\u001B[0m";
		System.out.println(color.getCode() + message + asciiColorReset);
	}

	private enum Color {
		BLUE("\u001B[34m"),
		WHITE("\033[0;97m"),
		RED("\u001B[31m"),
		YELLOW("\u001B[33m");

		private final String code;

		Color(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}
}
