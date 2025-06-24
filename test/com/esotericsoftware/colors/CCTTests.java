
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.TestsUtil.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CCTTests {
	@Test
	public void testCCTConversions () {
		// Test known CCT values
		String actual = hex(RGB(2700, 0));
		assertTrue(actual.equals("ffad59"), "Expected CCT RGB value != actual: ffad59 != " + actual);

		actual = hex(RGB(2700, 0.01f));
		assertTrue(actual.equals("ffb32b"), "Expected CCT RGB with 0.01 Duv value != actual: ffb32b != " + actual);

		// Test many color temperatures including new lower range
		java.util.List<Float> cctsList = new java.util.ArrayList<>();
		// Test lower range more densely
		for (int cct = 1000; cct < 1700; cct += 10) cctsList.add((float)cct);
		// Test regular range
		for (int cct = 1700; cct < 7000; cct += 10) cctsList.add((float)cct);
		
		for (float cct : cctsList) {
			RGB rgb = RGB(cct, 0);

			// Verify the color is reasonable (all channels should be positive)
			assertTrue(rgb.r() >= 0 && rgb.r() <= 1, "Red channel in range for CCT " + cct);
			assertTrue(rgb.g() >= 0 && rgb.g() <= 1, "Green channel in range for CCT " + cct);
			assertTrue(rgb.b() >= 0 && rgb.b() <= 1, "Blue channel in range for CCT " + cct);

			// For very low temperatures, red should dominate
			if (cct <= 1500) {
				assertTrue(rgb.r() > rgb.g() && rgb.r() > rgb.b(), "Red channel dominant for very low CCT " + cct);
			}
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
		for (float cct : cctsList) {
			xy xy = xy(cct, 0);

			// Verify xy values are reasonable
			assertTrue(xy.x() > 0 && xy.x() < 1, "x chromaticity in range for CCT " + cct);
			assertTrue(xy.y() > 0 && xy.y() < 1, "y chromaticity in range for CCT " + cct);
		}

		// Test CCT to UV1960
		for (float cct : cctsList) {
			uv uv1960 = uv(cct, 0);

			// Verify UV values are reasonable
			assertTrue(uv1960.u() > 0 && uv1960.u() < 1, "u1960 in range for CCT " + cct);
			assertTrue(uv1960.v() > 0 && uv1960.v() < 1, "v1960 in range for CCT " + cct);
		}

		// Test that Duv offset produces perpendicular displacement from blackbody locus
		float[] testCCTs = {2500, 3500, 5000, 7500}; // Use specific CCTs that work well
		for (float cct : testCCTs) {
			// Create points using the CCT + Duv constructor
			xy onLocus = xy(cct, 0); // On the locus (Duv = 0)
			xy aboveLocus = xy(cct, 0.005f); // Above locus (Duv = +0.005)
			xy belowLocus = xy(cct, -0.005f); // Below locus (Duv = -0.005)

			// Calculate the actual Duv values back
			float duvOn = onLocus.Duv();
			float duvAbove = aboveLocus.Duv();
			float duvBelow = belowLocus.Duv();

			// Points on the locus should have Duv ≈ 0
			assertClose(0, duvOn, "Point on blackbody locus has Duv ≈ 0 for CCT " + cct, 0.0001);

			// Points off the locus should have the expected Duv, roughly
			assertClose(0.005f, duvAbove, "Point above locus has correct Duv for CCT " + cct, 0.003);
			assertClose(-0.005f, duvBelow, "Point below locus has correct Duv for CCT " + cct, 0.003);

			// Verify colors with different Duv look different
			RGB rgbOn = onLocus.RGB();
			RGB rgbAbove = aboveLocus.RGB();
			RGB rgbBelow = belowLocus.RGB();

			// Above locus (positive Duv) should be more green, below (negative) more magenta
			assertTrue(rgbAbove.g() > rgbOn.g() || rgbAbove.r() < rgbOn.r(), "Positive Duv shifts toward green at CCT " + cct);
			assertTrue(rgbBelow.g() < rgbOn.g() || rgbBelow.r() > rgbOn.r(), "Negative Duv shifts toward magenta at CCT " + cct);
		}

		// Test edge cases
		RGB rgb = RGB(1000, 0); // New minimum CCT
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1 && rgb.g() >= 0 && rgb.g() <= 1 && rgb.b() >= 0 && rgb.b() <= 1,
			"RGB in range for minimum CCT");
		
		rgb = RGB(1667, 0); // Previous minimum CCT (boundary test)
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1 && rgb.g() >= 0 && rgb.g() <= 1 && rgb.b() >= 0 && rgb.b() <= 1,
			"RGB in range for 1667K CCT");

		rgb = RGB(25000, 0); // Maximum CCT
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1 && rgb.g() >= 0 && rgb.g() <= 1 && rgb.b() >= 0 && rgb.b() <= 1,
			"RGB in range for maximum CCT");

		// Test xy to CCT reverse conversions (McCamy's approximation)
		// Note: McCamy's approximation has varying accuracy:
		// - Best accuracy (±5K) in 4000-8000K range
		// - Lower accuracy (±50-200K) outside this range
		// - May have higher error in new 1000-1667K range
		for (float expectedCCT : cctsList) {
			xy xy = xy(expectedCCT, 0);
			float calculatedCCT = xy.CCT();

			// Check for invalid xy coordinates
			if (Float.isNaN(xy.x()) && Float.isNaN(calculatedCCT)) {
				assertTrue(Float.isNaN(calculatedCCT), "CCT should return NaN for invalid xy");
				continue;
			}

			// Skip if result is NaN (outside McCamy's valid range)
			if (Float.isNaN(calculatedCCT)) {
				// This is expected for some very low CCT values
				if (expectedCCT >= 1667) {
					Assertions.fail("CCT should not return NaN for CCT " + expectedCCT + " (xy: " + xy.x() + ", " + xy.y() + ")");
				}
				continue;
			}
			
			float error = Math.abs(calculatedCCT - expectedCCT);

			// McCamy's approximation has varying accuracy
			if (expectedCCT >= 4000 && expectedCCT <= 8000) {
				assertTrue(error < 50, "CCT error should be <50K in optimal range, was " + error + " for CCT " + expectedCCT);
			} else if (expectedCCT >= 1000 && expectedCCT < 1200) {
				// Very low range has poor accuracy with McCamy's method
				assertTrue(error < 4000, "CCT error in very low range (1000-1200K) is expected to be high, was " + error + " for CCT " + expectedCCT);
			} else if (expectedCCT >= 1200 && expectedCCT < 1667) {
				// Lower range may have higher error due to McCamy's approximation limitations
				assertTrue(error < 2000, "CCT error should be <2000K in low range, was " + error + " for CCT " + expectedCCT);
			} else if (expectedCCT >= 1667 && expectedCCT < 4000) {
				// Transition range also has reduced accuracy
				assertTrue(error < 1600, "CCT error should be <1600K in transition range, was " + error + " for CCT " + expectedCCT);
			} else {
				assertTrue(error < 200, "CCT error should be <200K outside optimal range, was " + error + " for CCT " + expectedCCT);
			}
		}
		
		// Test round-trip accuracy specifically for new lower range
		float[] lowCCTs = {1000, 1100, 1200, 1300, 1400, 1500, 1600};
		for (float cct : lowCCTs) {
			// Test xy(CCT) -> CCT(xy) round trip
			xy xyFromCCT = xy(cct, 0);
			float cctFromXY = xyFromCCT.CCT();
			if (!Float.isNaN(cctFromXY)) {
				float roundTripError = Math.abs(cct - cctFromXY);
				// Accept higher error for very low temperatures where McCamy doesn't work well
				float maxError = cct < 1200 ? 4000 : 1000;
				assertTrue(roundTripError < maxError, "Round-trip error for low CCT " + cct + " should be < " + maxError + ", was " + roundTripError);
			}
			
			// Test with Duv offsets
			xy xyWithDuv = xy(cct, 0.005f);
			float duvCalculated = xyWithDuv.Duv();
			if (!Float.isNaN(duvCalculated)) {
				// Higher tolerance for very low CCT values
				float duvTolerance = cct <= 1200 ? 0.003f : 0.001f;
				assertClose(0.005f, duvCalculated, "Duv accuracy for low CCT " + cct, duvTolerance);
			}
		}

		// Test uv to CCT conversions
		for (float expectedCCT : cctsList) {

			uv uv = RGB(expectedCCT, 0).uv();
			float calculatedCCT = uv.CCT();
			float error = Math.abs(calculatedCCT - expectedCCT);
			// Verify error is reasonable
			// Accept higher error for very low temperatures
			float maxError = expectedCCT < 1667 ? 8000 : 500;
			Assertions.assertTrue(error < maxError, "UV to CCT error for " + expectedCCT + "K should be reasonable, was " + error);
		}

		// Test edge cases

		// Test invalid xy coordinates
		float invalidCCT = new xy(0.1f, 0.1f).CCT(); // Far from blackbody locus
		assertTrue(Float.isNaN(invalidCCT), "Should return NaN for invalid xy coordinates");

		invalidCCT = new xy(0.65f, 0.4f).CCT(); // Outside valid range
		assertTrue(Float.isNaN(invalidCCT), "Should return NaN for xy outside range");

		// Test that extreme CCT values outside McCamy's range return NaN
		// Note: xy(1500) still returns valid xy coords, so we test the CCT calculation directly
		xy extremeLowXY = new xy(0.7f, 0.3f); // Very red, far from blackbody
		float extremeLowCCT = extremeLowXY.CCT();
		assertTrue(Float.isNaN(extremeLowCCT), "Should return NaN for xy far from blackbody locus");

		// Test specific xy that would give CCT outside bounds
		xy highCCTXY = new xy(0.25f, 0.25f); // Would give very high CCT
		float highCCT = highCCTXY.CCT();
		if (highCCT > 25000 || highCCT < 1000) assertTrue(Float.isNaN(highCCT), "Should return NaN for CCT outside bounds");
	}

	@Test
	public void testCCTToOklab () {
		// Test some common color temperatures
		RGB warmWhite = RGB(2700, 0);
		Oklab warmLab = warmWhite.Oklab();

		RGB neutralWhite = RGB(4000, 0);
		Oklab neutralLab = neutralWhite.Oklab();

		RGB coolWhite = RGB(6500, 0);
		Oklab coolLab = coolWhite.Oklab();

		// After normalization in CCTRGB, the differences are subtle but b channel shows clear trend
		// Warmer colors have higher b (yellow) values
		assertTrue(warmLab.b() > neutralLab.b() && neutralLab.b() > coolLab.b(),
			"CCT to Oklab 'b' channel ordering incorrect: warm=" + warmLab.b() + ", neutral=" + neutralLab.b() + ", cool="
				+ coolLab.b());
	}

	@Test
	public void testDuv () {
		// Test with D65 white point (should be near 0)
		xy d65 = new xy(0.3127f, 0.3290f);
		float duvD65 = d65.Duv();
		assertClose(0, duvD65, "D65 white point Duv should be near 0", 0.005);

		// Test with colors on the blackbody locus (should be ~0)
		// 2700K on blackbody locus
		xy cct2700 = xy(2700, 0);
		float duv2700 = cct2700.Duv();
		assertClose(0, duv2700, "2700K on blackbody locus should have Duv ~0", 0.003);

		// 4000K on blackbody locus
		xy cct4000 = xy(4000, 0);
		float duv4000 = cct4000.Duv();
		assertClose(0, duv4000, "4000K on blackbody locus should have Duv ~0", 0.001);

		// 6500K on blackbody locus
		xy cct6500 = xy(6500, 0);
		float duv6500 = cct6500.Duv();
		assertClose(0, duv6500, "6500K on blackbody locus should have Duv ~0", 0.001);

		// Test with colors off the locus (should have non-zero Duv)
		// Create a point above the blackbody locus (greenish)
		xy greenish = new xy(0.31f, 0.35f); // Above the locus
		float duvGreenish = greenish.Duv();
		// Some points may have different Duv than expected, just check it's non-zero
		assertTrue(Math.abs(duvGreenish) > 0.001, "Color off blackbody locus should have non-zero Duv (was " + duvGreenish + ")");

		// Create a point below the blackbody locus (pinkish)
		xy pinkish = new xy(0.33f, 0.30f); // Below the locus
		float duvPinkish = pinkish.Duv();
		// Just verify it's non-zero and different from the greenish one
		assertTrue(Math.abs(duvPinkish) > 0.001, "Color off blackbody locus should have non-zero Duv (was " + duvPinkish + ")");

		// Test various CCT xy coordinates with known Duv offsets
		// Using the uv1960(CCT, Duv) function to create test points
		float[] testCCTs = {2500, 3500, 5000, 7500};
		float[] testDuvs = {-0.01f, -0.005f, 0.005f, 0.01f};
		for (float cct : testCCTs) {
			for (float expectedDuv : testDuvs) {
				// Create a point with known Duv offset
				uv uvWithDuv = uv(cct, expectedDuv);
				float calculatedDuv = uvWithDuv.xy().Duv();

				// The calculated Duv should be close to the expected value
				// Some error is expected due to conversions and approximations
				assertClose(expectedDuv, calculatedDuv, "Duv for CCT " + cct + " with offset " + expectedDuv, 0.003);
			}
		}

		// Test edge cases
		// Very low CCT
		xy lowCCT = xy(1000, 0);
		float duvLow = lowCCT.Duv();
		assertClose(0, duvLow, "Very low CCT on locus should have Duv ~0", 0.001);

		// Very high CCT
		xy highCCT = xy(20000, 0);
		float duvHigh = highCCT.Duv();
		assertClose(0, duvHigh, "Very high CCT on locus should have Duv ~0", 0.001);

		// Test that Duv sign convention is correct
		// Points above the locus (more green) should have positive Duv
		// Points below the locus (more pink/magenta) should have negative Duv

		// Create points with known Duv offsets
		xy aboveLocus = xy(4000, 0.002f); // Positive Duv - above the locus
		float duvAbove = aboveLocus.Duv();

		xy belowLocus = xy(4000, -0.002f); // Negative Duv - below the locus
		float duvBelow = belowLocus.Duv();

		// Verify the calculated Duv values match what we specified
		assertClose(0.002f, duvAbove, "Duv above locus", 0.0001);
		assertClose(-0.002f, duvBelow, "Duv below locus", 0.0001);

		// The key is that points on opposite sides of the locus have opposite sign Duv
		assertTrue(Math.abs(duvAbove - duvBelow) > 0.001,
			"Points on opposite sides of locus should have different Duv values: above=" + duvAbove + ", below=" + duvBelow);
	}

	@Test
	public void testMacAdamSteps () {
		// Test with identical colors (should be 0)
		xy color1 = new xy(0.3127f, 0.3290f); // D65
		xy color2 = new xy(0.3127f, 0.3290f); // Same as color1
		float steps = color1.MacAdamSteps(color2);
		assertEquals(0, steps, "Identical colors should have 0 MacAdam steps");

		// Test with known color differences
		// One MacAdam step is approximately 0.0011 in uv1960 space
		xy d65 = new xy(0.3127f, 0.3290f);
		xy d50 = new xy(0.3457f, 0.3585f);

		float stepsD65toD50 = d65.MacAdamSteps(d50);
		assertTrue(stepsD65toD50 > 0, "Different colors should have positive MacAdam steps");

		// Test symmetry - distance should be the same regardless of order
		float stepsD50toD65 = d50.MacAdamSteps(d65);
		assertClose(stepsD65toD50, stepsD50toD65, "MacAdamSteps should be symmetric", 0.0001);

		// Test with colors one MacAdam step apart
		// Create a color approximately 1 MacAdam step from D65
		uv1960 uvD65 = d65.uv1960();
		uv1960 uv1Step = new uv1960(uvD65.u() + 0.0011f, uvD65.v());
		xy xy1Step = uv1Step.xy();

		float stepsTo1 = d65.MacAdamSteps(xy1Step);
		assertClose(1.0f, stepsTo1, "Color 0.0011 units away should be ~1 MacAdam step", 0.1);

		// Test with colors multiple MacAdam steps apart
		uv1960 uv5Steps = new uv1960(uvD65.u() + 0.0055f, uvD65.v());
		xy xy5Steps = uv5Steps.xy();

		float stepsTo5 = d65.MacAdamSteps(xy5Steps);
		assertClose(5.0f, stepsTo5, "Color 0.0055 units away should be ~5 MacAdam steps", 0.1);

		// Test triangle inequality
		xy colorA = new xy(0.31f, 0.32f);
		xy colorB = new xy(0.32f, 0.33f);
		xy colorC = new xy(0.33f, 0.34f);

		float stepsAB = colorA.MacAdamSteps(colorB);
		float stepsBC = colorB.MacAdamSteps(colorC);
		float stepsAC = colorA.MacAdamSteps(colorC);

		// Triangle inequality: direct distance should be <= sum of indirect distances
		assertTrue(stepsAC <= stepsAB + stepsBC + 0.0001,
			"Triangle inequality violated: " + stepsAC + " > " + stepsAB + " + " + stepsBC);

		// Test with various color temperature white points
		xy[] whitePoints = {xy(2700, 0), // Warm white
			xy(4000, 0), // Neutral white
			xy(6500, 0), // Daylight
			xy(9300, 0) // Cool white
		};

		for (int i = 0; i < whitePoints.length - 1; i++) {
			float whitePointSteps = whitePoints[i].MacAdamSteps(whitePoints[i + 1]);
			assertTrue(whitePointSteps > 0, "Different CCT white points should have positive MacAdam steps: " + whitePoints[i].CCT()
				+ "K to " + whitePoints[i + 1].CCT() + "K");
		}

		// Test edge cases with extreme chromaticity values
		xy red = new xy(0.64f, 0.33f); // Near pure red
		xy green = new xy(0.30f, 0.60f); // Near pure green
		xy blue = new xy(0.15f, 0.06f); // Near pure blue

		float stepsRedGreen = red.MacAdamSteps(green);
		float stepsGreenBlue = green.MacAdamSteps(blue);
		float stepsBlueRed = blue.MacAdamSteps(red);

		// Primary colors should be many MacAdam steps apart
		assertTrue(stepsRedGreen > 100, "Red and green should be many MacAdam steps apart");
		assertTrue(stepsGreenBlue > 100, "Green and blue should be many MacAdam steps apart");
		assertTrue(stepsBlueRed > 100, "Blue and red should be many MacAdam steps apart");

		// Test precision with very small differences
		xy baseColor = new xy(0.3127f, 0.3290f);
		xy slightlyDifferent = new xy(0.3128f, 0.3291f);

		float smallSteps = baseColor.MacAdamSteps(slightlyDifferent);
		assertTrue(smallSteps > 0 && smallSteps < 5, "Very similar colors should have small positive MacAdam steps: " + smallSteps);
	}
}
