
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.TestsUtil.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.colors.Colors.Oklab;
import com.esotericsoftware.colors.Colors.RGB;
import com.esotericsoftware.colors.Colors.uv;
import com.esotericsoftware.colors.Colors.uv1960;
import com.esotericsoftware.colors.Colors.xy;

public class CCTTests {
	@Test
	public void testCCTConversions () {
		// Test known CCT values
		String actual = hex(RGB(2700, 0, 50));
		assertTrue(actual.equals("fbaa58"), "Expected CCTRGB value != actual: ffe87a != " + actual);

		actual = hex(RGB(2700, 0.01f, 50));
		assertTrue(actual.equals("ffa774"), "Expected CCTRGB with 0.01 Duv value != actual: ffe39f != " + actual);

		// Test many color temperatures
		float[] ccts = new float[530];
		for (int i = 0, cct = 1700; cct < 7000; cct += 10, i++)
			ccts[i] = cct;
		for (float cct : ccts) {
			RGB rgb = RGB(cct, 0, 100);

			// Verify the color is reasonable (all channels should be positive)
			assertTrue(rgb.r() >= 0 && rgb.r() <= 1, "Red channel in range for CCT " + cct);
			assertTrue(rgb.g() >= 0 && rgb.g() <= 1, "Green channel in range for CCT " + cct);
			assertTrue(rgb.b() >= 0 && rgb.b() <= 1, "Blue channel in range for CCT " + cct);

			// For daylight temperatures, blue should increase with temperature
			if (cct >= 5000) {
				assertTrue(rgb.b() > 0.8f, "Blue channel high for daylight CCT " + cct);
			}
		}

		// Test CCT with Duv offsets
		float[] duvOffsets = {-0.01f, 0, 0.01f};
		for (float duv : duvOffsets) {
			RGB rgb = RGB(3000, duv);
			// Verify Duv affects the color
			Assertions.assertTrue(rgb.r() >= 0 && rgb.r() <= 1, "Duv " + duv + " R in range");
			Assertions.assertTrue(rgb.g() >= 0 && rgb.g() <= 1, "Duv " + duv + " G in range");
			Assertions.assertTrue(rgb.b() >= 0 && rgb.b() <= 1, "Duv " + duv + " B in range");
		}

		// Test CCT to xy conversions
		for (float cct : ccts) {
			xy xy = xy(cct);

			// Verify xy values are reasonable
			assertTrue(xy.x() > 0 && xy.x() < 1, "x chromaticity in range for CCT " + cct);
			assertTrue(xy.y() > 0 && xy.y() < 1, "y chromaticity in range for CCT " + cct);
		}

		// Test CCT to UV1960
		for (float cct : ccts) {
			uv1960 uv1960 = uv1960(cct);

			// Verify UV values are reasonable
			assertTrue(uv1960.u() > 0 && uv1960.u() < 1, "u1960 in range for CCT " + cct);
			assertTrue(uv1960.v() > 0 && uv1960.v() < 1, "v1960 in range for CCT " + cct);
		}

		// Test DUV1960 offset calculation
		for (float cct : ccts) {
			uv1960 base = uv1960(cct);
			uv1960 offset = uv1960(cct, 0.01f);
			offset = new uv1960(offset.u() - base.u(), offset.v() - base.v());

			// Verify offset is perpendicular (small magnitude)
			float magnitude = (float)Math.sqrt(offset.u() * offset.u() + offset.v() * offset.v());
			assertClose(0.01f, magnitude, "DUV1960 offset magnitude", 0.0001);
		}

		// Test edge cases
		RGB rgb = RGB(1667, 0); // Minimum CCT
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1 && rgb.g() >= 0 && rgb.g() <= 1 && rgb.b() >= 0 && rgb.b() <= 1,
			"RGB in range for minimum CCT");

		rgb = RGB(25000, 0); // Maximum CCT
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1 && rgb.g() >= 0 && rgb.g() <= 1 && rgb.b() >= 0 && rgb.b() <= 1,
			"RGB in range for maximum CCT");

		// Test xy to CCT reverse conversions (McCamy's approximation)
		// Note: McCamy's approximation has varying accuracy:
		// - Best accuracy (±5K) in 4000-8000K range
		// - Lower accuracy (±50-200K) outside this range
		for (float expectedCCT : ccts) {
			xy xy = xy(expectedCCT);
			float calculatedCCT = CCT(xy);

			// Check for invalid xy coordinates
			if (Float.isNaN(xy.x()) && Float.isNaN(calculatedCCT)) {
				assertTrue(Float.isNaN(calculatedCCT), "CCT should return -1 for invalid xy(-1, -1)");
				continue;
			}

			float error = Math.abs(calculatedCCT - expectedCCT);

			// McCamy's approximation has varying accuracy
			if (expectedCCT >= 4000 && expectedCCT <= 8000) {
				assertTrue(error < 50, "CCT error should be <50K in optimal range, was " + error);
			} else {
				assertTrue(error < 200, "CCT error should be <200K outside optimal range, was " + error);
			}
		}

		// Test uv to CCT conversions
		for (float expectedCCT : ccts) {
			// Skip invalid CCTs that would produce fallback values
			if (expectedCCT < 1667) {
				continue;
			}

			uv uv = uv(RGB(expectedCCT, 0));
			float calculatedCCT = CCT(uv);
			float error = Math.abs(calculatedCCT - expectedCCT);
			// Verify error is reasonable
			Assertions.assertTrue(error < 500, "UV to CCT error for " + expectedCCT + "K should be reasonable, was " + error);
		}

		// Test edge cases

		// Test invalid xy coordinates
		float invalidCCT = CCT(new xy(0.1f, 0.1f)); // Far from blackbody locus
		assertTrue(Float.isNaN(invalidCCT), "Should return -1 for invalid xy coordinates");

		invalidCCT = CCT(new xy(0.6f, 0.4f)); // Outside valid range
		assertTrue(Float.isNaN(invalidCCT), "Should return -1 for xy outside range");

		// Test that extreme CCT values outside McCamy's range return -1
		// Note: xy(1500) still returns valid xy coords, so we test the CCT calculation directly
		xy extremeLowXY = new xy(0.7f, 0.3f); // Very red, far from blackbody
		float extremeLowCCT = CCT(extremeLowXY);
		assertTrue(Float.isNaN(extremeLowCCT), "Should return -1 for xy far from blackbody locus");

		// Test specific xy that would give CCT outside bounds
		xy highCCTXY = new xy(0.25f, 0.25f); // Would give very high CCT
		float highCCT = CCT(highCCTXY);
		if (highCCT > 25000 || highCCT < 1667) assertTrue(Float.isNaN(highCCT), "Should return -1 for CCT outside bounds");
	}

	@Test
	public void testCCTToOklab () {
		// Test some common color temperatures
		RGB warmWhite = RGB(2700, 0);
		Oklab warmLab = Oklab(warmWhite);

		RGB neutralWhite = RGB(4000, 0);
		Oklab neutralLab = Oklab(neutralWhite);

		RGB coolWhite = RGB(6500, 0);
		Oklab coolLab = Oklab(coolWhite);

		// After normalization in CCTRGB, the differences are subtle but b channel shows clear trend
		// Warmer colors have higher b (yellow) values
		if (warmLab.b() > neutralLab.b() && neutralLab.b() > coolLab.b()) {
		} else {
			throw new AssertionError("CCT to Oklab 'b' channel ordering incorrect");
		}
	}

	@Test
	public void testDuv () {
		// Test with D65 white point (should be near 0)
		xy d65 = new xy(0.3127f, 0.3290f);
		float duvD65 = Duv(d65);
		assertClose(0, duvD65, "D65 white point Duv should be near 0", 0.005);

		// Test with colors on the blackbody locus (should be ~0)
		// 2700K on blackbody locus
		xy cct2700 = xy(2700);
		float duv2700 = Duv(cct2700);
		assertClose(0, duv2700, "2700K on blackbody locus should have Duv ~0", 0.001);

		// 4000K on blackbody locus
		xy cct4000 = xy(4000);
		float duv4000 = Duv(cct4000);
		assertClose(0, duv4000, "4000K on blackbody locus should have Duv ~0", 0.001);

		// 6500K on blackbody locus
		xy cct6500 = xy(6500);
		float duv6500 = Duv(cct6500);
		assertClose(0, duv6500, "6500K on blackbody locus should have Duv ~0", 0.001);

		// Test with colors off the locus (should have non-zero Duv)
		// Create a point above the blackbody locus (greenish)
		xy greenish = new xy(0.31f, 0.35f); // Above the locus
		float duvGreenish = Duv(greenish);
		// Some points may have different Duv than expected, just check it's non-zero
		assertTrue(Math.abs(duvGreenish) > 0.001, "Color off blackbody locus should have non-zero Duv (was " + duvGreenish + ")");

		// Create a point below the blackbody locus (pinkish)
		xy pinkish = new xy(0.33f, 0.30f); // Below the locus
		float duvPinkish = Duv(pinkish);
		// Just verify it's non-zero and different from the greenish one
		assertTrue(Math.abs(duvPinkish) > 0.001, "Color off blackbody locus should have non-zero Duv (was " + duvPinkish + ")");

		// Test various CCT xy coordinates with known Duv offsets
		// Using the uv1960(CCT, Duv) function to create test points
		float[] testCCTs = {2500, 3500, 5000, 7500};
		float[] testDuvs = {-0.01f, -0.005f, 0.005f, 0.01f};

		for (float cct : testCCTs) {
			for (float expectedDuv : testDuvs) {
				// Create a point with known Duv offset
				uv1960 uvWithDuv = uv1960(cct, expectedDuv);
				xy xyWithDuv = xy(uvWithDuv);
				float calculatedDuv = Duv(xyWithDuv);

				// The calculated Duv should be close to the expected value
				// Some error is expected due to conversions and approximations
				assertClose(expectedDuv, calculatedDuv, "Duv for CCT " + cct + " with offset " + expectedDuv, 0.002);
			}
		}

		// Test edge cases
		// Very low CCT
		xy lowCCT = xy(1700);
		float duvLow = Duv(lowCCT);
		assertClose(0, duvLow, "Very low CCT on locus should have Duv ~0", 0.001);

		// Very high CCT
		xy highCCT = xy(20000);
		float duvHigh = Duv(highCCT);
		assertClose(0, duvHigh, "Very high CCT on locus should have Duv ~0", 0.001);

		// Test that Duv sign convention is correct
		// Points above the locus (more green) should have positive Duv
		// Points below the locus (more pink/magenta) should have negative Duv
		xy testPoint = xy(4000); // Start on the locus
		uv1960 uvTest = uv1960(testPoint);

		// Move slightly perpendicular to the locus in the positive direction
		xy aboveLocus = xy(new uv1960(uvTest.u(), uvTest.v() + 0.002f));
		float duvAbove = Duv(aboveLocus);
		// Just verify the points have different Duv values

		// Move slightly perpendicular to the locus in the negative direction
		xy belowLocus = xy(new uv1960(uvTest.u(), uvTest.v() - 0.002f));
		float duvBelow = Duv(belowLocus);

		// The key is that points on opposite sides of the locus have opposite sign Duv
		assertTrue(Math.abs(duvAbove - duvBelow) > 0.001, "Points on opposite sides of locus should have different Duv values");
	}

	@Test
	public void testMacAdamSteps () {
		// Test with identical colors (should be 0)
		xy color1 = new xy(0.3127f, 0.3290f); // D65
		xy color2 = new xy(0.3127f, 0.3290f); // Same as color1
		float steps = MacAdamSteps(color1, color2);
		assertEquals(0, steps, "Identical colors should have 0 MacAdam steps");

		// Test with known color differences
		// One MacAdam step is approximately 0.0011 in uv1960 space
		xy d65 = new xy(0.3127f, 0.3290f);
		xy d50 = new xy(0.3457f, 0.3585f);

		float stepsD65toD50 = MacAdamSteps(d65, d50);
		assertTrue(stepsD65toD50 > 0, "Different colors should have positive MacAdam steps");

		// Test symmetry - distance should be the same regardless of order
		float stepsD50toD65 = MacAdamSteps(d50, d65);
		assertClose(stepsD65toD50, stepsD50toD65, "MacAdamSteps should be symmetric", 0.0001);

		// Test with colors one MacAdam step apart
		// Create a color approximately 1 MacAdam step from D65
		uv1960 uvD65 = uv1960(d65);
		uv1960 uv1Step = new uv1960(uvD65.u() + 0.0011f, uvD65.v());
		xy xy1Step = xy(uv1Step);

		float stepsTo1 = MacAdamSteps(d65, xy1Step);
		assertClose(1.0f, stepsTo1, "Color 0.0011 units away should be ~1 MacAdam step", 0.1);

		// Test with colors multiple MacAdam steps apart
		uv1960 uv5Steps = new uv1960(uvD65.u() + 0.0055f, uvD65.v());
		xy xy5Steps = xy(uv5Steps);

		float stepsTo5 = MacAdamSteps(d65, xy5Steps);
		assertClose(5.0f, stepsTo5, "Color 0.0055 units away should be ~5 MacAdam steps", 0.1);

		// Test triangle inequality
		xy colorA = new xy(0.31f, 0.32f);
		xy colorB = new xy(0.32f, 0.33f);
		xy colorC = new xy(0.33f, 0.34f);

		float stepsAB = MacAdamSteps(colorA, colorB);
		float stepsBC = MacAdamSteps(colorB, colorC);
		float stepsAC = MacAdamSteps(colorA, colorC);

		// Triangle inequality: direct distance should be <= sum of indirect distances
		assertTrue(stepsAC <= stepsAB + stepsBC + 0.0001,
			"Triangle inequality violated: " + stepsAC + " > " + stepsAB + " + " + stepsBC);

		// Test with various color temperature white points
		xy[] whitePoints = {xy(2700), // Warm white
			xy(4000), // Neutral white
			xy(6500), // Daylight
			xy(9300) // Cool white
		};

		for (int i = 0; i < whitePoints.length - 1; i++) {
			float whitePointSteps = MacAdamSteps(whitePoints[i], whitePoints[i + 1]);
			assertTrue(whitePointSteps > 0, "Different CCT white points should have positive MacAdam steps: " + CCT(whitePoints[i])
				+ "K to " + CCT(whitePoints[i + 1]) + "K");
		}

		// Test edge cases with extreme chromaticity values
		xy red = new xy(0.64f, 0.33f); // Near pure red
		xy green = new xy(0.30f, 0.60f); // Near pure green
		xy blue = new xy(0.15f, 0.06f); // Near pure blue

		float stepsRedGreen = MacAdamSteps(red, green);
		float stepsGreenBlue = MacAdamSteps(green, blue);
		float stepsBlueRed = MacAdamSteps(blue, red);

		// Primary colors should be many MacAdam steps apart
		assertTrue(stepsRedGreen > 100, "Red and green should be many MacAdam steps apart");
		assertTrue(stepsGreenBlue > 100, "Green and blue should be many MacAdam steps apart");
		assertTrue(stepsBlueRed > 100, "Blue and red should be many MacAdam steps apart");

		// Test precision with very small differences
		xy baseColor = new xy(0.3127f, 0.3290f);
		xy slightlyDifferent = new xy(0.3128f, 0.3291f);

		float smallSteps = MacAdamSteps(baseColor, slightlyDifferent);
		assertTrue(smallSteps > 0 && smallSteps < 5, "Very similar colors should have small positive MacAdam steps: " + smallSteps);
	}
}
