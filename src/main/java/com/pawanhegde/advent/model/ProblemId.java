package com.pawanhegde.advent.model;

public record ProblemId(int year, int day, int part) {
	@Override
	public String toString() {
		return "Problem(" + year + ", " + day + ", Part" + part + ')';
	}
}
