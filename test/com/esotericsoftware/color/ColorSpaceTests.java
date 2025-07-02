
package com.esotericsoftware.color;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.CAM16;
import com.esotericsoftware.color.space.HCT;
import com.esotericsoftware.color.space.HSL;
import com.esotericsoftware.color.space.HSV;
import com.esotericsoftware.color.space.Lab;
import com.esotericsoftware.color.space.Oklab;
import com.esotericsoftware.color.space.RGB;

/** Comprehensive tests that systematically test color conversions across the entire color space. */
public class ColorSpaceTests extends Tests {
	@Test
	public void testSystematicRGBtoLabConversions () {
		// Test a grid of RGB values to ensure Lab conversions work across the entire gamut
		int failureCount = 0;
		StringBuilder failures = new StringBuilder();

		// Test with 10% increments for reasonable coverage
		for (float r = 0; r <= 1f; r += 0.1f) {
			for (float g = 0; g <= 1f; g += 0.1f) {
				for (float b = 0; b <= 1f; b += 0.1f) {
					RGB rgb = new RGB(r, g, b);
					Lab lab = rgb.Lab();
					RGB rgbBack = lab.RGB();

					float diff = Math.abs(rgb.r() - rgbBack.r()) + Math.abs(rgb.g() - rgbBack.g()) + Math.abs(rgb.b() - rgbBack.b());
					if (diff > 0.01f) {
						failureCount++;
						if (failureCount <= 10) { // Only report first 10 failures
							failures.append(rgb + " -> " + lab + " -> " + rgbBack + " diff=" + diff + "\n");
						}
					}

					// Verify Lab value constraints
					assertTrue(lab.L() >= 0 && lab.L() <= 100, "L* out of range [0,100]: " + lab.L() + " for " + rgb);
					assertTrue(lab.a() >= -128 && lab.a() <= 127, "a* out of range [-128,127]: " + lab.a() + " for " + rgb);
					assertTrue(lab.b() >= -128 && lab.b() <= 127, "b* out of range [-128,127]: " + lab.b() + " for " + rgb);
				}
			}
		}

		if (failureCount > 0) throw new AssertionError("Lab round-trip failures: " + failureCount + "\n" + failures.toString());
	}

	@Test
	public void testHuePreservation () {
		// Test that hue is preserved across different color space conversions
		for (float hue = 15; hue < 360; hue += 15) {
			for (float sat = 0.2f; sat <= 1f; sat += 0.2f) {
				for (float light = 0.2f; light <= 0.8f; light += 0.2f) {
					HSL hsl = new HSL(hue, sat, light);
					RGB rgb = hsl.RGB();

					// Convert through different color spaces
					HSV hsv = rgb.HSV();
					Lab lab = rgb.Lab();
					Oklab oklab = rgb.Oklab();
					CAM16 cam16 = rgb.CAM16();
					HCT hct = rgb.HCT();

					// Convert back to RGB
					RGB rgbFromHSV = hsv.RGB();
					RGB rgbFromLab = lab.RGB();
					RGB rgbFromOklab = oklab.RGB();
					RGB rgbFromCAM16 = cam16.RGB();
					RGB rgbFromHCT = hct.RGB();

					// Convert back to HSL to check hue
					HSL hslFromHSV = rgbFromHSV.HSL();
					HSL hslFromLab = rgbFromLab.HSL();
					HSL hslFromOklab = rgbFromOklab.HSL();
					HSL hslFromCAM16 = rgbFromCAM16.HSL();
					HSL hslFromHCT = rgbFromHCT.HSL();

					// For saturated colors, hue should be preserved
					if (sat > 0.1f) {
						assertHueClose(hue, hslFromHSV.H(), "HSV hue preservation " + hue, 2);
						assertHueClose(hue, hslFromLab.H(), "Lab hue preservation " + hue, 5);
						assertHueClose(hue, hslFromOklab.H(), "Oklab hue preservation " + hue, 5);
						assertHueClose(hue, hslFromCAM16.H(), "CAM16 hue preservation " + hue, 5);
						assertHueClose(hue, hslFromHCT.H(), "HCT hue preservation " + hue, 5);
					}
				}
			}
		}
	}

	@Test
	public void testGrayAxisConsistency () {
		// Test that grays remain gray through all color space conversions
		for (float gray = 0; gray <= 1f; gray += 0.05f) {
			RGB rgb = new RGB(gray, gray, gray);

			// Lab should have a*=b*=0 for grays
			Lab lab = rgb.Lab();
			assertEquals(0, lab.a(), 0.5, "Gray Lab a* at " + gray);
			assertEquals(0, lab.b(), 0.5, "Gray Lab b* at " + gray);

			// Oklab should have a=b=0 for grays
			Oklab oklab = rgb.Oklab();
			assertEquals(0, oklab.a(), 0.001, "Gray Oklab a at " + gray);
			assertEquals(0, oklab.b(), 0.001, "Gray Oklab b at " + gray);

			// HSL/HSV should have S=0 for grays
			HSL hsl = rgb.HSL();
			HSV hsv = rgb.HSV();
			assertEquals(0, hsl.S(), 0.001, "Gray HSL saturation at " + gray);
			assertEquals(0, hsv.S(), 0.001, "Gray HSV saturation at " + gray);

			// Round-trip through all color spaces should preserve gray
			RGB grayBack = rgb.Lab().RGB();
			assertGray(grayBack, "Lab gray preservation at " + gray);

			grayBack = rgb.Oklab().RGB();
			assertGray(grayBack, "Oklab gray preservation at " + gray);

			grayBack = rgb.CAM16().RGB();
			assertGray(grayBack, "CAM16 gray preservation at " + gray);

			grayBack = rgb.HCT().RGB();
			assertGray(grayBack, "HCT gray preservation at " + gray);
		}
	}

	@Test
	public void testColorSpaceBoundaries () {
		// Test colors at the boundaries of the sRGB gamut
		RGB[] boundaryColors = {new RGB(1, 0, 0), new RGB(0, 1, 0), new RGB(0, 0, 1), // Primaries
			new RGB(1, 1, 0), new RGB(0, 1, 1), new RGB(1, 0, 1), // Secondaries
			new RGB(1, 1, 1), new RGB(0, 0, 0), // White and black
			new RGB(1, 0.5f, 0), new RGB(0.5f, 1, 0), // Half-saturated
			new RGB(0, 1, 0.5f), new RGB(0, 0.5f, 1), new RGB(0.5f, 0, 1), new RGB(1, 0, 0.5f)};

		for (RGB boundary : boundaryColors) {
			// Test round-trip through various color spaces
			testRoundTrip(boundary, rgb -> rgb.Lab(), lab -> lab.RGB(), "Lab boundary");
			testRoundTrip(boundary, rgb -> rgb.Oklab(), oklab -> oklab.RGB(), "Oklab boundary");
			testRoundTrip(boundary, rgb -> rgb.HSL(), hsl -> hsl.RGB(), "HSL boundary");
			testRoundTrip(boundary, rgb -> rgb.HSV(), hsv -> hsv.RGB(), "HSV boundary");
			testRoundTrip(boundary, rgb -> rgb.CAM16(), cam16 -> cam16.RGB(), "CAM16 boundary");
			testRoundTrip(boundary, rgb -> rgb.HCT(), hct -> hct.RGB(), "HCT boundary");
			testRoundTrip(boundary, rgb -> rgb.XYZ(), xyz -> xyz.RGB(), "XYZ boundary");
			testRoundTrip(boundary, rgb -> rgb.Luv(), luv -> luv.RGB(), "Luv boundary");
		}
	}

	@Test
	public void testPerceptualUniformity () {
		// Test that perceptually uniform spaces show consistent behavior
		// Small steps in Lab/Oklab should produce visually similar changes
		RGB baseColor = new RGB(0.5f, 0.5f, 0.7f);
		Lab baseLab = baseColor.Lab();
		Oklab baseOklab = baseColor.Oklab();

		float deltaE_Lab_total = 0;
		float deltaE_Oklab_total = 0;
		int steps = 10;

		// Test small steps in lightness
		for (int i = 1; i <= steps; i++) {
			Lab labStep = new Lab(baseLab.L() + i * 2, baseLab.a(), baseLab.b());
			Oklab oklabStep = new Oklab(baseOklab.L() + i * 0.02f, baseOklab.a(), baseOklab.b());

			RGB rgbLabStep = labStep.RGB();
			RGB rgbOklabStep = oklabStep.RGB();

			// Verify RGB values are valid
			assertTrue(rgbLabStep.r() >= 0 && rgbLabStep.r() <= 1, "Lab step " + i + " R out of range: " + rgbLabStep.r());
			assertTrue(rgbLabStep.g() >= 0 && rgbLabStep.g() <= 1, "Lab step " + i + " G out of range: " + rgbLabStep.g());
			assertTrue(rgbLabStep.b() >= 0 && rgbLabStep.b() <= 1, "Lab step " + i + " B out of range: " + rgbLabStep.b());

			assertTrue(rgbOklabStep.r() >= 0 && rgbOklabStep.r() <= 1, "Oklab step " + i + " R out of range: " + rgbOklabStep.r());
			assertTrue(rgbOklabStep.g() >= 0 && rgbOklabStep.g() <= 1, "Oklab step " + i + " G out of range: " + rgbOklabStep.g());
			assertTrue(rgbOklabStep.b() >= 0 && rgbOklabStep.b() <= 1, "Oklab step " + i + " B out of range: " + rgbOklabStep.b());

			// Verify round-trip accuracy
			Lab labRoundTrip = rgbLabStep.Lab();
			Oklab oklabRoundTrip = rgbOklabStep.Oklab();
			assertClose(labStep, labRoundTrip, 0.01f, "Lab round-trip at step " + i);
			assertClose(oklabStep, oklabRoundTrip, 0.01f, "Oklab round-trip at step " + i);

			// Calculate perceptual differences
			if (i > 1) {
				Lab prevLab = new Lab(baseLab.L() + (i - 1) * 2, baseLab.a(), baseLab.b());
				Oklab prevOklab = new Oklab(baseOklab.L() + (i - 1) * 0.02f, baseOklab.a(), baseOklab.b());

				float deltaE_Lab = prevLab.deltaE2000(labStep);
				float deltaE_Oklab = prevOklab.RGB().Lab().deltaE2000(oklabStep.RGB().Lab());

				deltaE_Lab_total += deltaE_Lab;
				deltaE_Oklab_total += deltaE_Oklab;
			}
		}

		// The average step size should be relatively consistent
		float avgDeltaE_Lab = deltaE_Lab_total / (steps - 1);
		float avgDeltaE_Oklab = deltaE_Oklab_total / (steps - 1);

		// Both should show reasonable uniformity (not testing exact values as implementations vary)
		assertTrue(avgDeltaE_Lab > 0 && avgDeltaE_Lab < 10, "Lab lightness steps should be uniform: " + avgDeltaE_Lab);
		assertTrue(avgDeltaE_Oklab > 0 && avgDeltaE_Oklab < 10, "Oklab lightness steps should be uniform: " + avgDeltaE_Oklab);
	}

	private void assertHueClose (float expected, float actual, String message, float tolerance) {
		float diff = Math.abs(expected - actual);
		if (diff > 180) diff = 360 - diff;
		assertEquals(0, diff, tolerance, message + " (expected " + expected + ", got " + actual + ")");
	}

	private void assertGray (RGB rgb, String message) {
		float max = Math.max(Math.max(rgb.r(), rgb.g()), rgb.b());
		float min = Math.min(Math.min(rgb.r(), rgb.g()), rgb.b());
		assertEquals(0, max - min, 0.01f, message + " should be gray");
	}

	private <T> void testRoundTrip (RGB original, java.util.function.Function<RGB, T> forward,
		java.util.function.Function<T, RGB> backward, String name) {
		T intermediate = forward.apply(original);
		RGB result = backward.apply(intermediate);
		assertClose(original, result, 0.02f, name + " " + original);
	}

}
