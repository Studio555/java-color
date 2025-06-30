
package com.esotericsoftware.color;

import static com.esotericsoftware.color.Tests.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.Okhsl;
import com.esotericsoftware.color.space.Okhsv;
import com.esotericsoftware.color.space.Oklab;
import com.esotericsoftware.color.space.Oklch;
import com.esotericsoftware.color.space.RGB;

public class OklabTests extends Tests {
	@Test
	public void testEdgeCases () {
		// Test near-zero values
		roundTripOklab(new RGB(0.001f, 0.001f, 0.001f), "Near black");
		roundTripOklab(new RGB(0.999f, 0.999f, 0.999f), "Near white");

		// Test single channel
		roundTripOklab(new RGB(0.5f, 0, 0), "Half red");
		roundTripOklab(new RGB(0, 0.5f, 0), "Half green");
		roundTripOklab(new RGB(0, 0, 0.5f), "Half blue");

		// Test very saturated colors
		roundTripOklab(new RGB(1, 0, 0.001f), "Almost pure red");
		roundTripOklab(new RGB(0.001f, 1, 0), "Almost pure green");
	}

	@Test
	public void testKnownOklabValues () {
		// Test white
		Oklab white = new RGB(1, 1, 1).Oklab();
		assertClose(white.L(), 1.0f, "White L");
		assertClose(white.a(), 0.0f, "White a");
		assertClose(white.b(), 0.0f, "White b");

		// Test black
		Oklab black = new RGB(0, 0, 0).Oklab();
		assertClose(black.L(), 0.0f, "Black L");
		assertClose(black.a(), 0.0f, "Black a");
		assertClose(black.b(), 0.0f, "Black b");

		// Test gray (should have a=0, b=0)
		Oklab gray = new RGB(0.5f, 0.5f, 0.5f).Oklab();
		assertClose(gray.a(), 0.0f, "Gray a");
		assertClose(gray.b(), 0.0f, "Gray b");
	}

	@Test
	public void testOkhslConversions () {
		// Test primary colors
		roundTripOkhsl(new RGB(1, 0, 0), "Red");
		roundTripOkhsl(new RGB(0, 1, 0), "Green");
		roundTripOkhsl(new RGB(0, 0, 1), "Blue");
		roundTripOkhsl(new RGB(1, 1, 0), "Yellow");
		roundTripOkhsl(new RGB(0, 1, 1), "Cyan");
		roundTripOkhsl(new RGB(1, 0, 1), "Magenta");
		roundTripOkhsl(new RGB(1, 1, 1), "White");
		roundTripOkhsl(new RGB(0, 0, 0), "Black");
		roundTripOkhsl(new RGB(0.5f, 0.5f, 0.5f), "Gray");

		// Test that white has l=1
		Okhsl white = new RGB(1, 1, 1).Okhsl();
		assertClose(1.0f, white.l(), "White Okhsl lightness");
		assertEquals(0.0f, white.s(), 0.01f, "White Okhsl saturation");

		// Test that black has l=0
		Okhsl black = new RGB(0, 0, 0).Okhsl();
		assertClose(0.0f, black.l(), "Black Okhsl lightness");

		// Test that grays have s=0
		Okhsl gray = new RGB(0.5f, 0.5f, 0.5f).Okhsl();
		assertEquals(0.0f, gray.s(), 0.01f, "Gray Okhsl saturation");

		// Test hue angles for primary colors
		Okhsl red = new RGB(1, 0, 0).Okhsl();
		Okhsl green = new RGB(0, 1, 0).Okhsl();
		Okhsl blue = new RGB(0, 0, 1).Okhsl();

		// Verify hue differences
		float hueGreenRed = Math.abs(green.h() - red.h());
		float hueBlueGreen = Math.abs(blue.h() - green.h());
		assertTrue(hueGreenRed > 90 && hueGreenRed < 180, "Green-Red hue difference");
		assertTrue(hueBlueGreen > 90 && hueBlueGreen < 180, "Blue-Green hue difference");

		// Test saturation range
		Okhsl[] testColors = {red, green, blue};
		for (Okhsl color : testColors) {
			assertTrue(color.s() >= 0 && color.s() <= 1, "Saturation in range [0,1]");
		}

		// Test edge cases with very dark colors
		roundTripOkhsl(new RGB(0.01f, 0.01f, 0.01f), "Very dark gray");
		roundTripOkhsl(new RGB(0.1f, 0, 0), "Very dark red");
	}

	@Test
	public void testOkhsvConversions () {
		// Test primary colors
		roundTripOkhsv(new RGB(1, 0, 0), "Red");
		roundTripOkhsv(new RGB(0, 1, 0), "Green");
		roundTripOkhsv(new RGB(0, 0, 1), "Blue");
		roundTripOkhsv(new RGB(1, 1, 0), "Yellow");
		roundTripOkhsv(new RGB(0, 1, 1), "Cyan");
		roundTripOkhsv(new RGB(1, 0, 1), "Magenta");
		roundTripOkhsv(new RGB(1, 1, 1), "White");
		roundTripOkhsv(new RGB(0, 0, 0), "Black");
		roundTripOkhsv(new RGB(0.5f, 0.5f, 0.5f), "Gray");

		// Test that white has v=1
		Okhsv white = new RGB(1, 1, 1).Okhsv();
		assertEquals(1.0f, white.v(), 0.1f, "White Okhsv value");
		assertEquals(0.0f, white.s(), 0.01f, "White Okhsv saturation");

		// Test that black has v=0
		Okhsv black = new RGB(0, 0, 0).Okhsv();
		assertEquals(0.0f, black.v(), 0.01f, "Black Okhsv value");

		// Test that grays have s=0
		Okhsv gray = new RGB(0.5f, 0.5f, 0.5f).Okhsv();
		assertEquals(0.0f, gray.s(), 0.01f, "Gray Okhsv saturation");

		// Test hue angles for primary colors
		Okhsv red = new RGB(1, 0, 0).Okhsv();
		Okhsv green = new RGB(0, 1, 0).Okhsv();
		Okhsv blue = new RGB(0, 0, 1).Okhsv();

		// Verify hue differences
		float hueGreenRed = Math.abs(green.h() - red.h());
		float hueBlueGreen = Math.abs(blue.h() - green.h());
		assertTrue(hueGreenRed > 90 && hueGreenRed < 180, "Green-Red hue difference");
		assertTrue(hueBlueGreen > 90 && hueBlueGreen < 180, "Blue-Green hue difference");

		// Test value and saturation range
		Okhsv[] testColors = {red, green, blue, white, black, gray};
		String[] names = {"red", "green", "blue", "white", "black", "gray"};
		for (int i = 0; i < testColors.length; i++) {
			Okhsv color = testColors[i];
			// System.out.println(names[i] + " Okhsv: h=" + color.h() + " s=" + color.s() + " v=" + color.v());
			assertTrue(color.s() >= 0 && color.s() <= 1, names[i] + " saturation in range [0,1]");
			assertTrue(color.v() >= 0 && color.v() <= 1, names[i] + " value in range [0,1]");
		}

		// Test edge cases
		roundTripOkhsv(new RGB(0.01f, 0.01f, 0.01f), "Very dark gray");
		roundTripOkhsv(new RGB(0.99f, 0.99f, 0.99f), "Very light gray");
	}

	@Test
	public void testOklabConversions () {
		// Test Oklab to Oklch conversions, gray (should have C=0)
		Oklab[] testLabs = {new Oklab(0.5f, 0.1f, 0.1f), new Oklab(0.8f, -0.05f, 0.05f), new Oklab(0.3f, 0.0f, 0.0f)};
		for (Oklab lab : testLabs) {
			Oklch lch = lab.Oklch();
			Oklab labBack = lch.Oklab();
			assertClose(lab, labBack, EPSILON_F, "Oklab <-> Oklch round trip");

			// Verify cylindrical coordinate conversion
			float expectedC = (float)Math.sqrt(lab.a() * lab.a() + lab.b() * lab.b());
			assertEquals(expectedC, lch.C(), EPSILON_F, "Oklch chroma calculation");

			// For gray colors, chroma should be 0
			if (Math.abs(lab.a()) < EPSILON_F && Math.abs(lab.b()) < EPSILON_F) {
				assertEquals(0, lch.C(), EPSILON_F, "Gray Oklch chroma");
			}
		}

		// Test RGB to Oklch direct conversion
		RGB rgb = new RGB(0.6f, 0.4f, 0.2f);
		Oklch oklch = rgb.Oklch();
		RGB rgbBack = oklch.RGB();
		assertClose(rgb, rgbBack, EPSILON_F, "RGB <-> Oklch round trip");
	}

	@Test
	public void testOklabLerp () {
		// Test lerp with t=0 returns first color
		Oklab color1 = new Oklab(0.3f, 0.1f, -0.1f);
		Oklab color2 = new Oklab(0.8f, -0.05f, 0.15f);
		Oklab result = color1.lerp(color2, 0);
		assertClose(color1, result, "Oklab lerp t=0");

		// Test lerp with t=1 returns second color
		result = color1.lerp(color2, 1);
		assertClose(color2, result, "Oklab lerp t=1");

		// Test lerp with t=0.5 returns midpoint
		result = color1.lerp(color2, 0.5f);
		Oklab expected = new Oklab((color1.L() + color2.L()) / 2, (color1.a() + color2.a()) / 2, (color1.b() + color2.b()) / 2);
		assertClose(expected, result, "Oklab lerp t=0.5");

		// Test lerp between black and white
		Oklab black = new RGB(0, 0, 0).Oklab();
		Oklab white = new RGB(1, 1, 1).Oklab();
		Oklab gray = black.lerp(white, 0.5f);
		// Gray should have L around 0.5, a and b near 0
		assertEquals(0.5f, gray.L(), 0.1f, "Lerp black-white L");
		assertEquals(0, gray.a(), 0.01f, "Lerp black-white a");
		assertEquals(0, gray.b(), 0.01f, "Lerp black-white b");

		// Test lerp between complementary colors
		Oklab red = new RGB(1, 0, 0).Oklab();
		Oklab cyan = new RGB(0, 1, 1).Oklab();
		Oklab mid = red.lerp(cyan, 0.5f);
		// Midpoint should be grayish
		RGB midRGB = mid.RGB();
		// Verify it's roughly gray (all channels similar)
		float avgChannel = (midRGB.r() + midRGB.g() + midRGB.b()) / 3;
		assertEquals(avgChannel, midRGB.r(), 0.15f, "Lerp complementary R");
		assertEquals(avgChannel, midRGB.g(), 0.15f, "Lerp complementary G");
		assertEquals(avgChannel, midRGB.b(), 0.15f, "Lerp complementary B");

		// Test lerp produces smooth gradient
		float[] factors = {0, 0.25f, 0.5f, 0.75f, 1};
		Oklab prevColor = null;
		for (float t : factors) {
			Oklab color = color1.lerp(color2, t);
			if (prevColor != null) {
				// Verify smooth progression
				float deltaL = Math.abs(color.L() - prevColor.L());
				float deltaA = Math.abs(color.a() - prevColor.a());
				float deltaB = Math.abs(color.b() - prevColor.b());
				// Each step should have similar deltas
				assertEquals(0.125f, deltaL, 0.01f, "Smooth L progression");
				assertEquals(0.0375f, deltaA, 0.01f, "Smooth a progression");
				assertEquals(0.0625f, deltaB, 0.01f, "Smooth b progression");
			}
			prevColor = color;
		}

		// Test lerp preserves perceptual uniformity
		// Colors at equal t intervals should appear equally spaced
		Oklab blue = new RGB(0, 0, 1).Oklab();
		Oklab yellow = new RGB(1, 1, 0).Oklab();
		Oklab q1 = blue.lerp(yellow, 0.25f);
		Oklab q2 = blue.lerp(yellow, 0.5f);
		Oklab q3 = blue.lerp(yellow, 0.75f);

		// Convert to RGB to verify colors look reasonable
		RGB rgbQ1 = q1.RGB();
		RGB rgbQ2 = q2.RGB();
		RGB rgbQ3 = q3.RGB();

		// All should be valid RGB values
		assertTrue(rgbQ1.r() >= 0 && rgbQ1.r() <= 1, "Q1 R in range");
		assertTrue(rgbQ1.g() >= 0 && rgbQ1.g() <= 1, "Q1 G in range");
		assertTrue(rgbQ1.b() >= 0 && rgbQ1.b() <= 1, "Q1 B in range");
		assertTrue(rgbQ2.r() >= 0 && rgbQ2.r() <= 1, "Q2 R in range");
		assertTrue(rgbQ2.g() >= 0 && rgbQ2.g() <= 1, "Q2 G in range");
		assertTrue(rgbQ2.b() >= 0 && rgbQ2.b() <= 1, "Q2 B in range");
		assertTrue(rgbQ3.r() >= 0 && rgbQ3.r() <= 1, "Q3 R in range");
		assertTrue(rgbQ3.g() >= 0 && rgbQ3.g() <= 1, "Q3 G in range");
		assertTrue(rgbQ3.b() >= 0 && rgbQ3.b() <= 1, "Q3 B in range");

		// Test edge cases
		// Lerp with same color should return that color
		Oklab sameResult = color1.lerp(color1, 0.5f);
		assertClose(color1, sameResult, "Lerp same color");

		// Test lerp with t outside [0,1] (extrapolation)
		Oklab extrapolated = color1.lerp(color2, 1.5f);
		// Should continue the line beyond color2
		float expectedL = color1.L() + 1.5f * (color2.L() - color1.L());
		float expectedA = color1.a() + 1.5f * (color2.a() - color1.a());
		float expectedB = color1.b() + 1.5f * (color2.b() - color1.b());
		assertClose(expectedL, extrapolated.L(), "Extrapolated L");
		assertClose(expectedA, extrapolated.a(), "Extrapolated a");
		assertClose(expectedB, extrapolated.b(), "Extrapolated b");

		// Test lerp with negative t
		Oklab negativeT = color1.lerp(color2, -0.5f);
		expectedL = color1.L() - 0.5f * (color2.L() - color1.L());
		expectedA = color1.a() - 0.5f * (color2.a() - color1.a());
		expectedB = color1.b() - 0.5f * (color2.b() - color1.b());
		assertClose(expectedL, negativeT.L(), "Negative t L");
		assertClose(expectedA, negativeT.a(), "Negative t a");
		assertClose(expectedB, negativeT.b(), "Negative t b");
	}

	@Test
	public void testOklchConversions () {
		// Test RGB -> Oklch -> RGB
		RGB original = new RGB(0.7f, 0.3f, 0.5f);
		Oklch lch = original.Oklch();
		RGB result = lch.RGB();
		assertClose(original, result, "Oklch round trip");

		// Test Oklab <-> Oklch
		Oklab lab = new RGB(0.6f, 0.4f, 0.2f).Oklab();
		Oklch lch2 = lab.Oklch();
		Oklab labBack = lch2.Oklab();
		assertClose(lab.L(), labBack.L(), "L channel");
		assertClose(lab.a(), labBack.a(), "a channel");
		assertClose(lab.b(), labBack.b(), "b channel");

		// Test hue angle wrapping
		Oklab labResult = new Oklch(0.5f, 0.1f, 370f).Oklab(); // 370° = 10°
		Oklab labExpected = new Oklch(0.5f, 0.1f, 10f).Oklab();
		assertClose(labResult.a(), labExpected.a(), "Hue wrap a");
		assertClose(labResult.b(), labExpected.b(), "Hue wrap b");
	}

	@Test
	public void testRgbToOklabAndBack () {
		// Test primary colors
		roundTripOklab(new RGB(1, 0, 0), "Red");
		roundTripOklab(new RGB(0, 1, 0), "Green");
		roundTripOklab(new RGB(0, 0, 1), "Blue");
		roundTripOklab(new RGB(1, 1, 0), "Yellow");
		roundTripOklab(new RGB(0, 1, 1), "Cyan");
		roundTripOklab(new RGB(1, 0, 1), "Magenta");
		roundTripOklab(new RGB(1, 1, 1), "White");
		roundTripOklab(new RGB(0, 0, 0), "Black");
		roundTripOklab(new RGB(0.5f, 0.5f, 0.5f), "Gray");

		// Test some random colors
		roundTripOklab(new RGB(0.8f, 0.2f, 0.4f), "Pink");
		roundTripOklab(new RGB(0.1f, 0.6f, 0.3f), "Teal");
		roundTripOklab(new RGB(0.9f, 0.7f, 0.1f), "Gold");
	}

	static void roundTripOkhsl (RGB original, String name) {
		Okhsl hsl = original.Okhsl();
		RGB result = hsl.RGB();
		assertClose(original, result, 0.01f, name + " Okhsl round trip");
	}

	static void roundTripOkhsv (RGB original, String name) {
		Okhsv hsv = original.Okhsv();
		RGB result = hsv.RGB();
		assertClose(original, result, 0.02f, name + " Okhsv round trip");
	}

	static void roundTripOklab (RGB original, String name) {
		Oklab lab = original.Oklab();
		RGB result = lab.RGB();
		assertClose(original, result, name);
	}
}
