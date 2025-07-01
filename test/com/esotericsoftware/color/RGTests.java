
package com.esotericsoftware.color;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.RGB;
import com.esotericsoftware.color.space.rg;

public class RGTests extends Tests {
	@Test
	void testFromRGB () {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		rg rgRed = red.rg();
		assertEquals(1.0f, rgRed.r(), 0.001f);
		assertEquals(0.0f, rgRed.g(), 0.001f);
		assertEquals(0.0f, rgRed.b(), 0.001f);

		RGB green = new RGB(0, 1, 0);
		rg rgGreen = green.rg();
		assertEquals(0.0f, rgGreen.r(), 0.001f);
		assertEquals(1.0f, rgGreen.g(), 0.001f);
		assertEquals(0.0f, rgGreen.b(), 0.001f);

		RGB blue = new RGB(0, 0, 1);
		rg rgBlue = blue.rg();
		assertEquals(0.0f, rgBlue.r(), 0.001f);
		assertEquals(0.0f, rgBlue.g(), 0.001f);
		assertEquals(1.0f, rgBlue.b(), 0.001f);

		// Test white
		RGB white = new RGB(1, 1, 1);
		rg rgWhite = white.rg();
		assertEquals(0.333f, rgWhite.r(), 0.001f);
		assertEquals(0.333f, rgWhite.g(), 0.001f);
		assertEquals(0.333f, rgWhite.b(), 0.001f);
		assertEquals(0.0f, rgWhite.s(), 0.001f); // White has no saturation

		// Test black (should return NaN)
		RGB black = new RGB(0, 0, 0);
		rg rgBlack = black.rg();
		assertTrue(Float.isNaN(rgBlack.r()));
		assertTrue(Float.isNaN(rgBlack.g()));
		assertTrue(Float.isNaN(rgBlack.b()));
	}

	@Test
	void testToRGB () {
		// Test conversion back from chromaticity
		rg rgRed = new rg(1.0f, 0.0f, 0.0f, 0.667f, 0);
		RGB red = rgRed.RGB(1);
		assertEquals(1.0f, red.r(), 0.001f);
		assertEquals(0.0f, red.g(), 0.001f);
		assertEquals(0.0f, red.b(), 0.001f);

		rg rgWhite = new rg(0.333f, 0.333f, 0.334f, 0.0f, 0);
		RGB white = rgWhite.RGB(2);
		assertEquals(0.666f, white.r(), 0.001f);
		assertEquals(0.666f, white.g(), 0.001f);
		assertEquals(0.668f, white.b(), 0.001f);

		// Test NaN handling
		rg rgNaN = new rg(Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
		assertTrue(Float.isNaN(rgNaN.r()), "NaN propagation in rg");
		RGB rgbNaN = rgNaN.RGB(1);
		assertTrue(Float.isNaN(rgbNaN.r()), "NaN propagation in RGB");
	}

	@Test
	void testRoundTrip () {
		// Test round trip conversion (note: luminance information is lost)
		RGB[] testColors = {new RGB(0.5f, 0.3f, 0.2f), new RGB(0.8f, 0.6f, 0.4f), new RGB(0.1f, 0.9f, 0.5f),
			new RGB(0.7f, 0.7f, 0.7f)};
		for (RGB original : testColors) {
			rg rg = original.rg();
			RGB converted = rg.RGB(1);

			// Calculate the original sum to normalize
			float originalSum = original.r() + original.g() + original.b();

			// The converted values should maintain the same ratios
			float convertedSum = converted.r() + converted.g() + converted.b();

			// Check ratios are preserved
			if (originalSum > 0 && convertedSum > 0) {
				assertEquals(original.r() / originalSum, converted.r() / convertedSum, 0.001f);
				assertEquals(original.g() / originalSum, converted.g() / convertedSum, 0.001f);
				assertEquals(original.b() / originalSum, converted.b() / convertedSum, 0.001f);
			}
		}
	}

	@Test
	void testSaturationAndHue () {
		// Test saturation calculation
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		rg rgGray = gray.rg();
		assertEquals(0.0f, rgGray.s(), 0.001f); // Gray has no saturation

		// Test a saturated color
		RGB orange = new RGB(1.0f, 0.5f, 0.0f);
		rg rgOrange = orange.rg();
		assertTrue(rgOrange.s() > 0); // Orange should have saturation

		// Verify chromaticity values sum to 1
		assertEquals(1.0f, rgOrange.r() + rgOrange.g() + rgOrange.b(), 0.001f);
	}
}
