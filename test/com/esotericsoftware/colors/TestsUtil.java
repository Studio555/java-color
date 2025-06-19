
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;

import com.esotericsoftware.colors.Colors.RGB;

public class TestsUtil {
	static final float EPSILON_F = 0.00002f;
	static final double EPSILON_D = 0.000001;

	static void assertArrayClose (float[] expected, float[] actual, String name) {
		assertArrayClose(expected, actual, name, EPSILON_D);
	}

	static void assertArrayClose (float[] expected, float[] actual, String name, double epsilon) {
		if (expected.length != actual.length) {
			throw new AssertionError(name + " array length mismatch");
		}
		for (int i = 0; i < expected.length; i++) {
			if (Math.abs(expected[i] - actual[i]) > epsilon) {
				throw new AssertionError(name + " mismatch at index " + i + ": expected " + expected[i] + ", got " + actual[i]
					+ " (diff: " + Math.abs(expected[i] - actual[i]) + ")");
			}
		}
	}

	static void assertRecordClose (Record expected, Record actual, String name) {
		assertArrayClose(floats(expected), floats(actual), name, EPSILON_F);
	}

	static void assertRecordClose (Record expected, Record actual, String name, double epsilon) {
		assertArrayClose(floats(expected), floats(actual), name, epsilon);
	}

	static void assertClose (float expected, float actual, String name) {
		Assertions.assertEquals(expected, actual, EPSILON_F, name);
	}

	static void assertClose (float expected, float actual, String name, double epsilon) {
		Assertions.assertEquals(expected, actual, epsilon, name);
	}

	static void assertEquals (float expected, float actual, String name) {
		Assertions.assertEquals(expected, actual, 0.0f, name);
	}

	static void assertEquals (int expected, int actual, String name) {
		Assertions.assertEquals(expected, actual, name);
	}

	static void assertEquals (String expected, String actual, String name) {
		Assertions.assertEquals(expected, actual, name);
	}

	static void assertTrue (boolean condition, String message) {
		Assertions.assertTrue(condition, message);
	}

	static boolean colorIsOnBoundary (RGB rgb) {
		float epsilon = 0.001f;
		return rgb.r() <= epsilon || rgb.r() >= 1 - epsilon || rgb.g() <= epsilon || rgb.g() >= 1 - epsilon || rgb.b() <= epsilon
			|| rgb.b() >= 1 - epsilon;
	}

	static <T extends Record, U extends Record> void roundTrip (T original, Function<T, U> forward, Function<U, T> backward,
		String name, double epsilon) {
		U converted = forward.apply(original);
		T back = backward.apply(converted);
		assertRecordClose(original, back, name + " round trip", epsilon);
	}

	static <T extends Record, U extends Record> void roundTripd (T original, Function<T, U> forward, Function<U, T> backward,
		String name) {
		roundTrip(original, forward, backward, name, EPSILON_D);
	}

	static <T extends Record, U extends Record> void roundTripf (T original, Function<T, U> forward, Function<U, T> backward,
		String name) {
		roundTrip(original, forward, backward, name, EPSILON_F);
	}
}
