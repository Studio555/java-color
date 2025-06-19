
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.TestsUtil.*;
import static com.esotericsoftware.colors.Util.OklabUtil.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.colors.Colors.Okhsl;
import com.esotericsoftware.colors.Colors.Okhsv;
import com.esotericsoftware.colors.Colors.Oklab;
import com.esotericsoftware.colors.Colors.Oklch;
import com.esotericsoftware.colors.Colors.RGB;

public class OklabTests {
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
		Oklab white = Oklab(new RGB(1, 1, 1));
		assertClose(white.L(), 1.0f, "White L");
		assertClose(white.a(), 0.0f, "White a");
		assertClose(white.b(), 0.0f, "White b");

		// Test black
		Oklab black = Oklab(new RGB(0, 0, 0));
		assertClose(black.L(), 0.0f, "Black L");
		assertClose(black.a(), 0.0f, "Black a");
		assertClose(black.b(), 0.0f, "Black b");

		// Test gray (should have a=0, b=0)
		Oklab gray = Oklab(new RGB(0.5f, 0.5f, 0.5f));
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
		Okhsl white = Okhsl(new RGB(1, 1, 1));
		assertClose(1.0f, white.l(), "White Okhsl lightness");
		assertClose(0.0f, white.s(), "White Okhsl saturation", 0.01f);

		// Test that black has l=0
		Okhsl black = Okhsl(new RGB(0, 0, 0));
		assertClose(0.0f, black.l(), "Black Okhsl lightness");

		// Test that grays have s=0
		Okhsl gray = Okhsl(new RGB(0.5f, 0.5f, 0.5f));
		assertClose(0.0f, gray.s(), "Gray Okhsl saturation", 0.01f);

		// Test hue angles for primary colors
		Okhsl red = Okhsl(new RGB(1, 0, 0));
		Okhsl green = Okhsl(new RGB(0, 1, 0));
		Okhsl blue = Okhsl(new RGB(0, 0, 1));

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
		Okhsv white = Okhsv(new RGB(1, 1, 1));
		assertClose(1.0f, white.v(), "White Okhsv value", 0.1f);
		assertClose(0.0f, white.s(), "White Okhsv saturation", 0.01f);

		// Test that black has v=0
		Okhsv black = Okhsv(new RGB(0, 0, 0));
		assertClose(0.0f, black.v(), "Black Okhsv value", 0.01f);

		// Test that grays have s=0
		Okhsv gray = Okhsv(new RGB(0.5f, 0.5f, 0.5f));
		assertClose(0.0f, gray.s(), "Gray Okhsv saturation", 0.01f);

		// Test hue angles for primary colors
		Okhsv red = Okhsv(new RGB(1, 0, 0));
		Okhsv green = Okhsv(new RGB(0, 1, 0));
		Okhsv blue = Okhsv(new RGB(0, 0, 1));

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
			Oklch lch = Oklch(lab);
			Oklab labBack = Oklab(lch);
			assertRecordClose(lab, labBack, "Oklab <-> Oklch round trip", EPSILON_F);

			// Verify cylindrical coordinate conversion
			float expectedC = (float)Math.sqrt(lab.a() * lab.a() + lab.b() * lab.b());
			assertClose(expectedC, lch.C(), "Oklch chroma calculation", EPSILON_F);

			// For gray colors, chroma should be 0
			if (Math.abs(lab.a()) < EPSILON_F && Math.abs(lab.b()) < EPSILON_F) {
				assertClose(0, lch.C(), "Gray Oklch chroma", EPSILON_F);
			}
		}

		// Test RGB to Oklch direct conversion
		RGB rgb = new RGB(0.6f, 0.4f, 0.2f);
		Oklch oklch = Oklch(rgb);
		RGB rgbBack = RGB(oklch);
		assertRecordClose(rgb, rgbBack, "RGB <-> Oklch round trip", EPSILON_F);
	}

	@Test
	public void testOklabLerp () {
		// Test lerp with t=0 returns first color
		Oklab color1 = new Oklab(0.3f, 0.1f, -0.1f);
		Oklab color2 = new Oklab(0.8f, -0.05f, 0.15f);
		Oklab result = lerp(color1, color2, 0);
		assertRecordClose(color1, result, "Oklab lerp t=0");

		// Test lerp with t=1 returns second color
		result = lerp(color1, color2, 1);
		assertRecordClose(color2, result, "Oklab lerp t=1");

		// Test lerp with t=0.5 returns midpoint
		result = lerp(color1, color2, 0.5f);
		Oklab expected = new Oklab((color1.L() + color2.L()) / 2, (color1.a() + color2.a()) / 2, (color1.b() + color2.b()) / 2);
		assertRecordClose(expected, result, "Oklab lerp t=0.5");

		// Test lerp between black and white
		Oklab black = Oklab(new RGB(0, 0, 0));
		Oklab white = Oklab(new RGB(1, 1, 1));
		Oklab gray = lerp(black, white, 0.5f);
		// Gray should have L around 0.5, a and b near 0
		assertClose(0.5f, gray.L(), "Lerp black-white L", 0.1f);
		assertClose(0, gray.a(), "Lerp black-white a", 0.01f);
		assertClose(0, gray.b(), "Lerp black-white b", 0.01f);

		// Test lerp between complementary colors
		Oklab red = Oklab(new RGB(1, 0, 0));
		Oklab cyan = Oklab(new RGB(0, 1, 1));
		Oklab mid = lerp(red, cyan, 0.5f);
		// Midpoint should be grayish
		RGB midRGB = RGB(mid);
		// Verify it's roughly gray (all channels similar)
		float avgChannel = (midRGB.r() + midRGB.g() + midRGB.b()) / 3;
		assertClose(avgChannel, midRGB.r(), "Lerp complementary R", 0.15f);
		assertClose(avgChannel, midRGB.g(), "Lerp complementary G", 0.15f);
		assertClose(avgChannel, midRGB.b(), "Lerp complementary B", 0.15f);

		// Test lerp produces smooth gradient
		float[] factors = {0, 0.25f, 0.5f, 0.75f, 1};
		Oklab prevColor = null;
		for (float t : factors) {
			Oklab color = lerp(color1, color2, t);
			if (prevColor != null) {
				// Verify smooth progression
				float deltaL = Math.abs(color.L() - prevColor.L());
				float deltaA = Math.abs(color.a() - prevColor.a());
				float deltaB = Math.abs(color.b() - prevColor.b());
				// Each step should have similar deltas
				assertClose(0.125f, deltaL, "Smooth L progression", 0.01f);
				assertClose(0.0375f, deltaA, "Smooth a progression", 0.01f);
				assertClose(0.0625f, deltaB, "Smooth b progression", 0.01f);
			}
			prevColor = color;
		}

		// Test lerp preserves perceptual uniformity
		// Colors at equal t intervals should appear equally spaced
		Oklab blue = Oklab(new RGB(0, 0, 1));
		Oklab yellow = Oklab(new RGB(1, 1, 0));
		Oklab q1 = lerp(blue, yellow, 0.25f);
		Oklab q2 = lerp(blue, yellow, 0.5f);
		Oklab q3 = lerp(blue, yellow, 0.75f);

		// Convert to RGB to verify colors look reasonable
		RGB rgbQ1 = RGB(q1);
		RGB rgbQ2 = RGB(q2);
		RGB rgbQ3 = RGB(q3);

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
		Oklab sameResult = lerp(color1, color1, 0.5f);
		assertRecordClose(color1, sameResult, "Lerp same color");

		// Test lerp with t outside [0,1] (extrapolation)
		Oklab extrapolated = lerp(color1, color2, 1.5f);
		// Should continue the line beyond color2
		float expectedL = color1.L() + 1.5f * (color2.L() - color1.L());
		float expectedA = color1.a() + 1.5f * (color2.a() - color1.a());
		float expectedB = color1.b() + 1.5f * (color2.b() - color1.b());
		assertClose(expectedL, extrapolated.L(), "Extrapolated L");
		assertClose(expectedA, extrapolated.a(), "Extrapolated a");
		assertClose(expectedB, extrapolated.b(), "Extrapolated b");

		// Test lerp with negative t
		Oklab negativeT = lerp(color1, color2, -0.5f);
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
		Oklch lch = Oklch(original);
		RGB result = RGB(lch);
		assertRecordClose(original, result, "Oklch round trip");

		// Test Oklab <-> Oklch
		Oklab lab = Oklab(new RGB(0.6f, 0.4f, 0.2f));
		Oklch lch2 = Oklch(lab);
		Oklab labBack = Oklab(lch2);
		assertClose(lab.L(), labBack.L(), "L channel");
		assertClose(lab.a(), labBack.a(), "a channel");
		assertClose(lab.b(), labBack.b(), "b channel");

		// Test hue angle wrapping
		Oklab labResult = Oklab(new Oklch(0.5f, 0.1f, 370f)); // 370° = 10°
		Oklab labExpected = Oklab(new Oklch(0.5f, 0.1f, 10f));
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
		Okhsl hsl = Okhsl(original);
		RGB result = RGB(hsl);
		assertRecordClose(original, result, name + " Okhsl round trip", 0.01f);
	}

	static void roundTripOkhsv (RGB original, String name) {
		Okhsv hsv = Okhsv(original);
		RGB result = RGB(hsv);
		assertRecordClose(original, result, name + " Okhsv round trip", 0.02f);
	}

	static void roundTripOklab (RGB original, String name) {
		Oklab lab = Oklab(original);
		RGB result = RGB(lab);
		assertRecordClose(original, result, name);
	}
}
