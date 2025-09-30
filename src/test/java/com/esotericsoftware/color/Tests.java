
package com.esotericsoftware.color;

import static com.esotericsoftware.color.Colors.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;

import com.esotericsoftware.color.space.RGB;

public class Tests extends Assertions {
	static final float EPSILON_F = 0.00002f;
	static final double EPSILON_D = 0.000001;

	static private long startTime;

	@RegisterExtension static TestWatcher watcher = new TestWatcher() {
		public void testFailed (ExtensionContext context, Throwable cause) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			cause.printStackTrace(pw);
			String trimmed = sw.toString()//
				.replace("\t", "   ") //
				.replace("org.opentest4j.AssertionFailedError: ", "") //
				.lines().filter(line -> {
					String stripped = line.stripLeading();
					return stripped.isEmpty() || !stripped.startsWith("at ") || stripped.startsWith("at com.esotericsoftware");
				}).collect(Collectors.joining("\n"));
			System.out.println(
				"--- " + context.getTestClass().get().getSimpleName() + ": " + context.getTestMethod().get().getName() + " ---");
			System.out.println(trimmed);
			System.out.println();
		}
	};

	static public void startTimer () {
		startTime = System.nanoTime();
	}

	static public void stopTimer (String message) {
		System.out.println(message + time((System.nanoTime() - startTime) / 1_000_000));
	}

	static public String time (long millis) {
		int hours = (int)(millis / (60 * 60 * 1000));
		int minutes = (int)(millis % (60 * 60 * 1000) / (60 * 1000));
		if (hours == 0 && minutes == 0) {
			float seconds = (int)(millis / 1000d * 10d) / 10f;
			if (seconds == 0) return millis + "ms";
			if (seconds == (int)seconds) return (int)seconds + "s";
			return seconds + "s";
		}
		int seconds = (int)(millis % (60 * 1000) / 1000);
		return (hours == 0 ? "" : hours + ":") + (minutes < 10 ? "0" : "") + minutes + ':' + (seconds < 10 ? "0" : "") + seconds;
	}

	static boolean colorIsOnBoundary (RGB rgb) {
		float epsilon = 0.001f;
		return rgb.r() <= epsilon || rgb.r() >= 1 - epsilon || rgb.g() <= epsilon || rgb.g() >= 1 - epsilon || rgb.b() <= epsilon
			|| rgb.b() >= 1 - epsilon;
	}

	static void assertCloseD (float[] expected, float[] actual, String name) {
		assertClose(expected, actual, EPSILON_D, name);
	}

	static void assertClose (float[] expected, float[] actual, double epsilon, String name) {
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

	static void assertClose (Record expected, Record actual, String name) {
		assertClose(floats(expected), floats(actual), EPSILON_F, name);
	}

	static void assertClose (Record expected, Record actual, double epsilon, String name) {
		assertClose(floats(expected), floats(actual), epsilon, name);
	}

	static void assertClose (float expected, float actual, String name) {
		Assertions.assertEquals(expected, actual, EPSILON_F, name);
	}

	static <T extends Record, U extends Record> void roundTripD (T original, Function<T, U> forward, Function<U, T> backward,
		String name) {
		roundTrip(original, forward, backward, name, EPSILON_D);
	}

	static <T extends Record, U extends Record> void roundTripF (T original, Function<T, U> forward, Function<U, T> backward,
		String name) {
		roundTrip(original, forward, backward, name, EPSILON_F);
	}

	static private <T extends Record, U extends Record> void roundTrip (T original, Function<T, U> forward,
		Function<U, T> backward, String name, double epsilon) {
		U converted = forward.apply(original);
		T back = backward.apply(converted);
		assertClose(original, back, epsilon, name + " round trip");
	}
}
