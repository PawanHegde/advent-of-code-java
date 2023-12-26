package com.pawanhegde.advent;

public class AocException extends RuntimeException {
	public AocException(String message) {
		super(message);
	}

	public AocException(String message, Throwable cause) {
		super(message, cause);
	}
}
