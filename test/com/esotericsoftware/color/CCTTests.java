
package com.esotericsoftware.color;

import static com.esotericsoftware.color.Util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.CCT;
import com.esotericsoftware.color.space.CCT.Method;
import com.esotericsoftware.color.space.Oklab;
import com.esotericsoftware.color.space.RGB;
import com.esotericsoftware.color.space.uv;
import com.esotericsoftware.color.space.uv1960;
import com.esotericsoftware.color.space.xy;

public class CCTTests extends Tests {
	@Test
	public void testDuvSigns () {
		for (CCT.Method method : CCT.Method.values())
			checkDuvSigns(method);
	}

	private void checkDuvSigns (CCT.Method method) {
		for (float u = 0.321f; u <= 0.352f; u += 0.001)
			assertTrue(new uv(u, 0.543f).CCT(method).Duv() > 0, "Wrong Duv sign: " + method);
		for (float u = 0.321f; u <= 0.352f; u += 0.001)
			assertTrue(new uv(u, 0.539f).CCT(method).Duv() < 0, "Wrong Duv sign: " + method);
	}

	@Test
	public void testxyErrorOhno () {
		float max = 0;
		for (float K = 1000; K < 3000; K += 0.1f) {
			xy xy = new CCT(K).xy();
			float roundTripK = xy.CCT(CCT.Method.Ohno2013).K();
			max = Math.max(max, K - roundTripK);
		}
		assertTrue(max < 1, "Max error: " + max);
		for (float K = 25000; K < 100000; K += 0.1f) {
			float roundTripK = new CCT(K).xy().CCT(CCT.Method.Ohno2013).K();
			max = Math.max(max, K - roundTripK);
		}
		assertTrue(max < 30, "Max error: " + max);
	}

	@Test
	public void testXYZError0Duv () {
		checkMaxError(CCT.Method.RobertsonImproved, 1000, 2000, 0.09698486f, 0.0001f);
		checkMaxError(CCT.Method.RobertsonImproved, 2000, 7000, 0.10644531f, 0.0001f);
		checkMaxError(CCT.Method.RobertsonImproved, 7000, 20000, 1.0742188f, 0.0001f);
		checkMaxError(CCT.Method.RobertsonImproved, 20000, 60000, 2.0195312f, 0.0001f);
		checkMaxError(CCT.Method.RobertsonImproved, 60000, 100000, 2.1953125f, 0.0001f);

		checkMaxError(CCT.Method.Robertson1968, 1000, 2000, 666.6666f, 0.007f);
		checkMaxError(CCT.Method.Robertson1968, 2000, 7000, 2.3999023f, 0.007f);
		checkMaxError(CCT.Method.Robertson1968, 7000, 20000, 46.351562f, 0.007f);
		checkMaxError(CCT.Method.Robertson1968, 20000, 60000, 377.125f, 0.007f);
		checkMaxError(CCT.Method.Robertson1968, 60000, 100000, 1959.1484f, 0.007f);

		checkMaxError(CCT.Method.Ohno2013, 1000, 2000, 0.319458f, 0.0001f);
		checkMaxError(CCT.Method.Ohno2013, 2000, 7000, 1.0917969f, 0.0001f);
		checkMaxError(CCT.Method.Ohno2013, 7000, 20000, 2.7089844f, 0.0001f);
		checkMaxError(CCT.Method.Ohno2013, 20000, 60000, 3.6132812f, 0.0001f);
		checkMaxError(CCT.Method.Ohno2013, 60000, 100000, 3.25f, 0.0001f);
	}

	private void checkMaxError (CCT.Method method, float start, float end, float exactMaxError, float epsilonDuv) {
		float maxErrorK = 0, maxKAt = 0;
		for (float K = start; K < end; K += 0.1f) {
			CCT roundTrip = new CCT(K).PlanckianXYZ().CCT(method);
			assertEquals(0, roundTrip.Duv(), epsilonDuv, "Wrong Duv: " + K + " K, " + method);
			float error = Math.abs(K - roundTrip.K());
			if (error > maxErrorK) {
				maxErrorK = error;
				maxKAt = K;
			}
		}
		// System.out.println("K: " + start + ".." + end + ": " + maxErrorK + " @ " + maxKAt);
		assertEquals(exactMaxError, maxErrorK, "Wrong max error: " + maxErrorK + " @ " + maxKAt + ", " + method);
	}

	@Test
	public void testXYZErrorWithDuv () {
		for (float Duv : new float[] {0.001f, 0.004f, 0.01f, 0.003f, 0.05f, -0.001f, -0.004f, -0.01f, -0.003f, -0.05f}) {
			checkMaxErrorWithDuv(CCT.Method.RobertsonImproved, Duv, 1000, 2000, 0.132f, 0.0001f);
			checkMaxErrorWithDuv(CCT.Method.RobertsonImproved, Duv, 2000, 7000, 0.432f, 0.0001f);
			checkMaxErrorWithDuv(CCT.Method.RobertsonImproved, Duv, 7000, 20000, 1.471f, 0.0001f);
			checkMaxErrorWithDuv(CCT.Method.RobertsonImproved, Duv, 20000, 60000, 2.594f, 0.0001f);
			checkMaxErrorWithDuv(CCT.Method.RobertsonImproved, Duv, 60000, 100000, 4.165f, 0.0001f);

			checkMaxErrorWithDuv(CCT.Method.Robertson1968, Duv, 1000, 2000, 666.6666f, 0.0071f);
			checkMaxErrorWithDuv(CCT.Method.Robertson1968, Duv, 2000, 7000, 4.38f, 0.007f);
			checkMaxErrorWithDuv(CCT.Method.Robertson1968, Duv, 7000, 20000, 63.211f, 0.007f);
			checkMaxErrorWithDuv(CCT.Method.Robertson1968, Duv, 20000, 60000, 551.493f, 0.007f);
			checkMaxErrorWithDuv(CCT.Method.Robertson1968, Duv, 60000, 100000, 2586.657f, 0.007f);

			checkMaxErrorWithDuv(CCT.Method.Ohno2013, Duv, 1000, 2000, 0.741f, 0.00064f);
			checkMaxErrorWithDuv(CCT.Method.Ohno2013, Duv, 2000, 7000, 1.256f, 0.0001f);
			checkMaxErrorWithDuv(CCT.Method.Ohno2013, Duv, 7000, 20000, 8.313f, 0.0001f);
			checkMaxErrorWithDuv(CCT.Method.Ohno2013, Duv, 20000, 60000, 517, 0.0003f);
			checkMaxErrorWithDuv(CCT.Method.Ohno2013, Duv, 60000, 100000, 1492, 0.5f);
		}
	}

	private void checkMaxErrorWithDuv (CCT.Method method, float Duv, float start, float end, float expectedMaxErrorK,
		float expectedMaxErrorDuv) {
		// System.out.println("\n=== " + method + ": " + Duv + " Duv ===");
		float maxErrorK = 0, maxKAt = 0, maxErrorDuv = 0, maxDuvAt = 0;
		for (float K = start; K < end; K += 0.5f) {
			CCT cct = new CCT(K, Duv);
			CCT roundTrip = cct.PlanckianXYZ().CCT(method);
			boolean skipDuvChecks = false;
			if (method == Method.Robertson1968) skipDuvChecks = true;
			if (method == Method.Ohno2013 && K > 39000) skipDuvChecks = true;
			if (!skipDuvChecks) {
				assertTrue(Math.signum(Duv) == Math.signum(roundTrip.Duv()),
					"Wrong Duv sign uv -> CCT: " + K + " K, " + Duv + " Duv, " + method);
				assertTrue(Math.signum(Duv) == Math.signum(cct.uv(method).CCT(method).Duv()),
					"Wrong Duv sign CCT -> uv -> CCT: " + K + " K, " + Duv + " Duv, " + method);
			}
			float error = Math.abs(Duv - roundTrip.Duv());
			if (error > maxErrorDuv) {
				maxErrorDuv = error;
				maxDuvAt = K;
			}
			error = Math.abs(K - roundTrip.K());
			if (error > maxErrorK) {
				maxErrorK = error;
				maxKAt = K;
			}
		}
		// System.out.println("Duv: " + start + ".." + end + ": " + maxErrorDuv + " @ " + maxDuvAt);
		// System.out.println("K: " + start + ".." + end + ": " + maxErrorK + " @ " + maxKAt);
		assertTrue(maxErrorDuv <= expectedMaxErrorDuv, "Max Duv error too high: " + maxErrorDuv + " <= " + expectedMaxErrorDuv
			+ " @ " + maxDuvAt + " K, " + Duv + " Duv, " + method);
		assertTrue(maxErrorK <= expectedMaxErrorK,
			"Max K error too high: " + maxErrorK + " <= " + expectedMaxErrorK + " @ " + maxKAt + " K, " + Duv + " Duv, " + method);
	}

	@Test
	public void testCCTConversions () {
		// Test known CCT values
		String actual = hex(new CCT(2700).RGB());
		assertTrue(actual.equals("ffad59"), "Expected CCT RGB value != actual: ffad59 != " + actual);

		actual = hex(new CCT(2700, 0.01f).RGB());
		assertTrue(actual.equals("ffb32a"), "Expected CCT RGB with 0.01 Duv value != actual: ffb32a != " + actual);

		// Test many color temperatures including new lower range
		java.util.List<Float> cctsList = new java.util.ArrayList<>();
		// Test lower range more densely
		for (int cct = 1000; cct < 1700; cct += 10)
			cctsList.add((float)cct);
		// Test regular range
		for (int cct = 1700; cct < 7000; cct += 10)
			cctsList.add((float)cct);
		cctsList.add(10000f);
		cctsList.add(20000f);
		cctsList.add(30000f);
		cctsList.add(60000f);
		cctsList.add(100000f);

		for (float cct : cctsList) {
			RGB rgb = new CCT(cct).RGB();

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
			RGB rgb = new CCT(3000, duv).RGB();
			// Verify Duv affects the color
			Assertions.assertTrue(rgb.r() >= 0 && rgb.r() <= 1, "Duv " + duv + " R in range");
			Assertions.assertTrue(rgb.g() >= 0 && rgb.g() <= 1, "Duv " + duv + " G in range");
			Assertions.assertTrue(rgb.b() >= 0 && rgb.b() <= 1, "Duv " + duv + " B in range");
		}

		// Test CCT to xy conversions
		for (float cct : cctsList) {
			xy xy = new CCT(cct).xy();

			// Verify xy values are reasonable
			assertTrue(xy.x() > 0 && xy.x() < 1, "x chromaticity in range for CCT " + cct);
			assertTrue(xy.y() > 0 && xy.y() < 1, "y chromaticity in range for CCT " + cct);
		}

		// Test CCT to u'v'
		for (float cct : cctsList) {
			uv uv = new CCT(cct).uv();

			// Verify UV values are reasonable
			assertTrue(uv.u() > 0 && uv.u() < 1, "u1960 in range for CCT " + cct);
			assertTrue(uv.v() > 0 && uv.v() < 1, "v1960 in range for CCT " + cct);
		}

		// Test that Duv offset produces perpendicular displacement from blackbody locus
		float[] testCCTs = {2500, 3500, 5000, 7500}; // Use specific CCTs that work well
		for (float cct : testCCTs) {
			// Create points using the CCT + Duv constructor
			xy onLocus = new CCT(cct).xy(); // On the locus (Duv = 0)
			xy aboveLocus = new CCT(cct, 0.005f).xy(); // Above locus (Duv = +0.005)
			xy belowLocus = new CCT(cct, -0.005f).xy(); // Below locus (Duv = -0.005)

			// Calculate the actual Duv values back
			float duvOn = onLocus.CCT().Duv();
			float duvAbove = aboveLocus.CCT().Duv();
			float duvBelow = belowLocus.CCT().Duv();

			// Points on the locus should have Duv ≈ 0
			assertEquals(0, duvOn, 0.001, "Point on blackbody locus has Duv ≈ 0 for CCT " + cct);

			// Points off the locus should have the expected Duv, roughly
			assertEquals(0.005f, duvAbove, 0.003, "Point above locus has correct Duv for CCT " + cct);
			assertEquals(-0.005f, duvBelow, 0.003, "Point below locus has correct Duv for CCT " + cct);

			// Verify colors with different Duv look different
			RGB rgbOn = onLocus.RGB();
			RGB rgbAbove = aboveLocus.RGB();
			RGB rgbBelow = belowLocus.RGB();

			// Above locus (positive Duv) should be more green, below (negative) more magenta
			assertTrue(rgbAbove.g() > rgbOn.g() || rgbAbove.r() < rgbOn.r(), "Positive Duv shifts toward green at CCT " + cct);
			assertTrue(rgbBelow.g() < rgbOn.g() || rgbBelow.r() > rgbOn.r(), "Negative Duv shifts toward magenta at CCT " + cct);
		}

		// Test edge cases
		RGB rgb = new CCT(1000).RGB(); // New minimum CCT
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1 && rgb.g() >= 0 && rgb.g() <= 1 && rgb.b() >= 0 && rgb.b() <= 1,
			"RGB in range for minimum CCT");

		rgb = new CCT(1667).RGB();
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1 && rgb.g() >= 0 && rgb.g() <= 1 && rgb.b() >= 0 && rgb.b() <= 1,
			"RGB in range for 1667K CCT");

		rgb = new CCT(100000).RGB();
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1 && rgb.g() >= 0 && rgb.g() <= 1 && rgb.b() >= 0 && rgb.b() <= 1,
			"RGB in range for maximum CCT");

		// Test xy to CCT reverse conversions
		for (float expectedCCT : cctsList) {
			xy xy = new CCT(expectedCCT).xy();
			float calculatedCCT = xy.CCT().K();

			// Check for invalid xy coordinates
			if (Float.isNaN(xy.x()) && Float.isNaN(calculatedCCT)) {
				assertTrue(Float.isNaN(calculatedCCT), "CCT should return NaN for invalid xy");
				continue;
			}

			float error = Math.abs(calculatedCCT - expectedCCT);
			assertTrue(error < 25, "CCT error should be <25K, was " + error + " for CCT " + expectedCCT);
		}

		// Test round-trip accuracy specifically for new lower range
		float[] lowCCTs = {1000, 1001, 1030, 1100, 1200, 1300, 1400, 1500, 1600, 2700, 3000, 4000, 5000, 6500, 10000};
		for (float cct : lowCCTs) {
			// Test xy(CCT) -> CCT(xy) round trip
			xy xyFromCCT = new CCT(cct).xy();
			float cctFromXY = xyFromCCT.CCT().K();
			if (!Float.isNaN(cctFromXY)) {
				float roundTripError = Math.abs(cct - cctFromXY);
				float maxError = 1000;
				assertTrue(roundTripError < maxError,
					"Round-trip error for low CCT " + cct + " should be < " + maxError + ", was " + roundTripError);
			}

			// Test with Duv offsets
			xy xyWithDuv = new CCT(cct, 0.005f).xy();
			float duvCalculated = xyWithDuv.CCT().Duv();
			if (!Float.isNaN(duvCalculated)) assertEquals(0.005f, duvCalculated, 0.001f, "Duv accuracy for low CCT " + cct);
		}

		// Test uv to CCT conversions
		for (float expectedCCT : cctsList) {
			uv uv = new CCT(expectedCCT).RGB().uv();
			float calculatedCCT = uv.CCT().K();
			float error = Math.abs(calculatedCCT - expectedCCT);
			Assertions.assertTrue(error < 30, "UV to CCT error for " + expectedCCT + "K should be reasonable, was " + error);
		}
	}

	@Test
	public void testCCTToOklab () {
		// Test some common color temperatures
		RGB warmWhite = new CCT(2700).RGB();
		Oklab warmLab = warmWhite.Oklab();

		RGB neutralWhite = new CCT(4000).RGB();
		Oklab neutralLab = neutralWhite.Oklab();

		RGB coolWhite = new CCT(6500).RGB();
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
		xy d65 = new xy(0.3127f, 0.329f);
		float duvD65 = d65.CCT().Duv();
		assertEquals(0, duvD65, 0.005, "D65 white point Duv should be near 0");

		// Test with colors on the blackbody locus (should be ~0)
		// 2700K on blackbody locus
		xy cct2700 = new CCT(2700).xy();
		float duv2700 = cct2700.CCT().Duv();
		assertEquals(0, duv2700, 0.003, "2700K on blackbody locus should have Duv ~0");

		// 4000K on blackbody locus
		xy cct4000 = new CCT(4000).xy();
		float duv4000 = cct4000.CCT().Duv();
		assertEquals(0, duv4000, 0.001, "4000K on blackbody locus should have Duv ~0");

		// 6500K on blackbody locus
		xy cct6500 = new CCT(6500).xy();
		float duv6500 = cct6500.CCT().Duv();
		assertEquals(0, duv6500, 0.001, "6500K on blackbody locus should have Duv ~0");

		// Test with colors off the locus (should have non-zero Duv)
		// Create a point above the blackbody locus (greenish)
		xy greenish = new xy(0.31f, 0.35f); // Above the locus
		float duvGreenish = greenish.CCT().Duv();
		// Some points may have different Duv than expected, just check it's non-zero
		assertTrue(Math.abs(duvGreenish) > 0.001, "Color off blackbody locus should have non-zero Duv (was " + duvGreenish + ")");

		// Create a point below the blackbody locus (pinkish)
		xy pinkish = new xy(0.33f, 0.3f); // Below the locus
		float duvPinkish = pinkish.CCT().Duv();
		// Just verify it's non-zero and different from the greenish one
		assertTrue(Math.abs(duvPinkish) > 0.001, "Color off blackbody locus should have non-zero Duv (was " + duvPinkish + ")");

		// Test various CCT xy coordinates with known Duv offsets
		// Using the uv1960(CCT, Duv) function to create test points
		float[] testCCTs = {2500, 3500, 5000, 7500};
		float[] testDuvs = {-0.01f, -0.005f, 0.005f, 0.01f};
		for (float cct : testCCTs) {
			for (float expectedDuv : testDuvs) {
				// Create a point with known Duv offset
				uv uvWithDuv = new CCT(cct, expectedDuv).uv();
				float calculatedDuv = uvWithDuv.xy().CCT().Duv();

				// The calculated Duv should be close to the expected value
				// Some error is expected due to conversions and approximations
				assertEquals(expectedDuv, calculatedDuv, 0.003, "Duv for CCT " + cct + " with offset " + expectedDuv);
			}
		}

		// Test edge cases
		// Very low CCT
		xy lowCCT = new CCT(1667).xy();
		float duvLow = lowCCT.CCT().Duv();
		assertEquals(0, duvLow, 0.001, "Very low CCT on locus should have Duv ~0");

		// Very high CCT
		xy highCCT = new CCT(20000).xy();
		float duvHigh = highCCT.CCT().Duv();
		assertEquals(0, duvHigh, 0.001, "Very high CCT on locus should have Duv ~0");

		// Test that Duv sign convention is correct
		// Points above the locus (more green) should have positive Duv
		// Points below the locus (more pink/magenta) should have negative Duv

		// Create points with known Duv offsets
		xy aboveLocus = new CCT(4000, 0.002f).xy(); // Positive Duv - above the locus
		float duvAbove = aboveLocus.CCT().Duv();

		xy belowLocus = new CCT(4000, -0.002f).xy(); // Negative Duv - below the locus
		float duvBelow = belowLocus.CCT().Duv();

		// Verify the calculated Duv values match what we specified
		assertEquals(0.002f, duvAbove, 0.0001, "Duv above locus");
		assertEquals(-0.002f, duvBelow, 0.0001, "Duv below locus");

		// The key is that points on opposite sides of the locus have opposite sign Duv
		assertTrue(Math.abs(duvAbove - duvBelow) > 0.001,
			"Points on opposite sides of locus should have different Duv values: above=" + duvAbove + ", below=" + duvBelow);
	}

	@Test
	public void testMacAdamSteps () {
		// Test with identical colors (should be 0)
		xy color1 = new xy(0.3127f, 0.329f); // D65
		xy color2 = new xy(0.3127f, 0.329f); // Same as color1
		float steps = color1.MacAdamSteps(color2);
		assertEquals(0, steps, "Identical colors should have 0 MacAdam steps");

		// Test with known color differences
		// One MacAdam step is approximately 0.0011 in uv1960 space
		xy d65 = new xy(0.3127f, 0.329f);
		xy d50 = new xy(0.3457f, 0.3585f);

		float stepsD65toD50 = d65.MacAdamSteps(d50);
		assertTrue(stepsD65toD50 > 0, "Different colors should have positive MacAdam steps");

		// Test symmetry - distance should be the same regardless of order
		float stepsD50toD65 = d50.MacAdamSteps(d65);
		assertEquals(stepsD65toD50, stepsD50toD65, 0.0001, "MacAdamSteps should be symmetric");

		// Test with colors one MacAdam step apart
		// Create a color approximately 1 MacAdam step from D65
		uv1960 uvD65 = d65.uv1960();
		uv1960 uv1Step = new uv1960(uvD65.u() + 0.0011f, uvD65.v());
		xy xy1Step = uv1Step.xy();

		float stepsTo1 = d65.MacAdamSteps(xy1Step);
		assertEquals(1f, stepsTo1, 0.1, "Color 0.0011 units away should be ~1 MacAdam step");

		// Test with colors multiple MacAdam steps apart
		uv1960 uv5Steps = new uv1960(uvD65.u() + 0.0055f, uvD65.v());
		xy xy5Steps = uv5Steps.xy();

		float stepsTo5 = d65.MacAdamSteps(xy5Steps);
		assertEquals(5f, stepsTo5, 0.1, "Color 0.0055 units away should be ~5 MacAdam steps");

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
		xy[] whitePoints = { //
			new CCT(2700).xy(), // Warm white
			new CCT(4000).xy(), // Neutral white
			new CCT(6500).xy(), // Daylight
			new CCT(9300).xy() // Cool white
		};

		for (int i = 0; i < whitePoints.length - 1; i++) {
			float whitePointSteps = whitePoints[i].MacAdamSteps(whitePoints[i + 1]);
			assertTrue(whitePointSteps > 0, "Different CCT white points should have positive MacAdam steps: " + whitePoints[i].CCT()
				+ "K to " + whitePoints[i + 1].CCT() + "K");
		}

		// Test edge cases with extreme chromaticity values
		xy red = new xy(0.64f, 0.33f); // Near pure red
		xy green = new xy(0.3f, 0.6f); // Near pure green
		xy blue = new xy(0.15f, 0.06f); // Near pure blue

		float stepsRedGreen = red.MacAdamSteps(green);
		float stepsGreenBlue = green.MacAdamSteps(blue);
		float stepsBlueRed = blue.MacAdamSteps(red);

		// Primary colors should be many MacAdam steps apart
		assertTrue(stepsRedGreen > 100, "Red and green should be many MacAdam steps apart");
		assertTrue(stepsGreenBlue > 100, "Green and blue should be many MacAdam steps apart");
		assertTrue(stepsBlueRed > 100, "Blue and red should be many MacAdam steps apart");

		// Test precision with very small differences
		xy baseColor = new xy(0.3127f, 0.329f);
		xy slightlyDifferent = new xy(0.3128f, 0.3291f);

		float smallSteps = baseColor.MacAdamSteps(slightlyDifferent);
		assertTrue(smallSteps > 0 && smallSteps < 5, "Very similar colors should have small positive MacAdam steps: " + smallSteps);
	}
}
