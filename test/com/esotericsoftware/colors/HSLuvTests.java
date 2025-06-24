
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.TestsUtil.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.colors.space.HSLuv;
import com.esotericsoftware.colors.space.RGB;

public class HSLuvTests {

	@Test
	public void testHSLuvReferenceValues () {
		// Test pure colors
		testHSLuvConversion(new RGB(1, 0, 0), 12.177f, 100.0f, 53.237f, "Pure red");
		testHSLuvConversion(new RGB(0, 1, 0), 127.715f, 100.0f, 87.737f, "Pure green");
		testHSLuvConversion(new RGB(0, 0, 1), 265.874f, 100.0f, 32.301f, "Pure blue");
		testHSLuvConversion(new RGB(1, 1, 0), 85.874f, 100.0f, 97.139f, "Pure yellow");
		testHSLuvConversion(new RGB(0, 1, 1), 192.177f, 100.0f, 91.114f, "Pure cyan");
		testHSLuvConversion(new RGB(1, 0, 1), 307.715f, 100.0f, 60.324f, "Pure magenta");

		// Test achromatic colors
		testHSLuvConversion(new RGB(1, 1, 1), 0.0f, 0.0f, 100.0f, "White");
		testHSLuvConversion(new RGB(0, 0, 0), 0.0f, 0.0f, 0.0f, "Black");
		testHSLuvConversion(new RGB(0.5f, 0.5f, 0.5f), 0.0f, 0.0f, 53.389f, "50% gray");

		// Test some specific colors
		testHSLuvConversion(new RGB(0.75f, 0.25f, 0.25f), 12.174f, 63.847f, 45.796f, "75% red");
		testHSLuvConversion(new RGB(0.25f, 0.75f, 0.25f), 127.724f, 84.679f, 68.619f, "75% green");
		testHSLuvConversion(new RGB(0.25f, 0.25f, 0.75f), 265.873f, 66.509f, 34.987f, "75% blue");
	}

	@Test
	public void testHSLuvRoundtrip () {
		// Test perfect roundtrip for various colors
		RGB[] testColors = {new RGB(1, 0, 0), new RGB(0, 1, 0), new RGB(0, 0, 1), new RGB(1, 1, 0), new RGB(0, 1, 1),
			new RGB(1, 0, 1), new RGB(0.25f, 0.5f, 0.75f), new RGB(0.9f, 0.1f, 0.5f), new RGB(0, 0, 0), new RGB(1, 1, 1),
			new RGB(0.5f, 0.5f, 0.5f)};
		for (RGB color : testColors) {
			HSLuv hsluv = color.HSLuv();
			RGB back = hsluv.RGB();

			assertClose(color.r(), back.r(), String.format("Roundtrip R for %s", color), 0.005f);
			assertClose(color.g(), back.g(), String.format("Roundtrip G for %s", color), 0.005f);
			assertClose(color.b(), back.b(), String.format("Roundtrip B for %s", color), 0.005f);
		}
	}

	@Test
	public void testHSLuvEdgeCases () {
		// Test white (H should be defined but S should be 0)
		HSLuv white = new RGB(1, 1, 1).HSLuv();
		assertClose(0.0f, white.S(), "White saturation", 0.1f);
		assertClose(100.0f, white.L(), "White lightness", 0.1f);

		// Test black (H should be defined but S should be 0)
		HSLuv black = new RGB(0, 0, 0).HSLuv();
		assertClose(0.0f, black.S(), "Black saturation", 0.1f);
		assertClose(0.0f, black.L(), "Black lightness", 0.1f);

		// Test that all pure colors have S=100
		RGB[] pureColors = {new RGB(1, 0, 0), new RGB(0, 1, 0), new RGB(0, 0, 1), new RGB(1, 1, 0), new RGB(0, 1, 1),
			new RGB(1, 0, 1)};
		for (RGB color : pureColors) {
			HSLuv hsluv = color.HSLuv();
			assertClose(100.0f, hsluv.S(), String.format("Saturation for pure color %s", color), 0.1f);
		}
	}

	@Test
	public void testHSLuvFromValues () {
		// Test creating HSLuv from values and converting to RGB
		// Reference: HSLuv(180, 50, 50) should be a desaturated cyan
		HSLuv hsluv = new HSLuv(180.0f, 50.0f, 50.0f);
		RGB rgb = hsluv.RGB();

		// Should be roughly equal amounts of green and blue, less red
		assertTrue(rgb.r() < rgb.g(), "Red should be less than green");
		assertTrue(rgb.r() < rgb.b(), "Red should be less than blue");
		assertTrue(Math.abs(rgb.g() - rgb.b()) < 0.1f, "Green and blue should be similar");

		// Test that the values roundtrip
		HSLuv back = rgb.HSLuv();
		assertClose(hsluv.H(), back.H(), "Hue roundtrip", 0.5f);
		assertClose(hsluv.S(), back.S(), "Saturation roundtrip", 0.5f);
		assertClose(hsluv.L(), back.L(), "Lightness roundtrip", 0.5f);
	}

	@Test
	public void testHSLuvSaturationRange () {
		// Test that saturation is correctly bounded
		// For any valid RGB color, HSLuv saturation should be 0-100
		for (float r = 0; r <= 1; r += 0.25f) {
			for (float g = 0; g <= 1; g += 0.25f) {
				for (float b = 0; b <= 1; b += 0.25f) {
					RGB rgb = new RGB(r, g, b);
					HSLuv hsluv = rgb.HSLuv();

					assertTrue(hsluv.S() >= 0.0f, String.format("S >= 0 for %s", rgb));
					assertTrue(hsluv.S() <= 100.0f, String.format("S <= 100 for %s", rgb));
					assertTrue(hsluv.L() >= 0.0f, String.format("L >= 0 for %s", rgb));
					assertTrue(hsluv.L() <= 100.0f, String.format("L <= 100 for %s", rgb));

					if (!Float.isNaN(hsluv.H())) {
						assertTrue(hsluv.H() >= 0.0f, String.format("H >= 0 for %s", rgb));
						assertTrue(hsluv.H() < 360.0f, String.format("H < 360 for %s", rgb));
					}
				}
			}
		}
	}

	private void testHSLuvConversion (RGB rgb, float expectedH, float expectedS, float expectedL, String name) {
		HSLuv hsluv = rgb.HSLuv();

		// For achromatic colors, hue can be any value or NaN
		if (expectedS == 0.0f) {
			assertClose(expectedS, hsluv.S(), name + " saturation", 0.5f);
			assertClose(expectedL, hsluv.L(), name + " lightness", 0.5f);
		} else {
			assertClose(expectedH, hsluv.H(), name + " hue", 0.5f);
			assertClose(expectedS, hsluv.S(), name + " saturation", 0.5f);
			assertClose(expectedL, hsluv.L(), name + " lightness", 0.5f);
		}

		// Test reverse conversion
		RGB back = hsluv.RGB();
		assertClose(rgb.r(), back.r(), name + " roundtrip R", 0.005f);
		assertClose(rgb.g(), back.g(), name + " roundtrip G", 0.005f);
		assertClose(rgb.b(), back.b(), name + " roundtrip B", 0.005f);
	}
}
