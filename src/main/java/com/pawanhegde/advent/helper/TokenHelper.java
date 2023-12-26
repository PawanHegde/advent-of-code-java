package com.pawanhegde.advent.helper;

import com.pawanhegde.advent.AocException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TokenHelper {
	private TokenHelper() {
	}

	public static String fetchToken() {
		try {
			return Files.readString(Path.of(System.getProperty("user.home"), "/.config/aocd/token"));
		} catch (IOException e) {
			throw new AocException("Could not read the token", e);
		}
	}
}
