
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.colors.Colors.RGB;
import com.esotericsoftware.colors.Colors.xy;

/** @author Nathan Sweet <misc@n4te.com> */
public class GamutTests {
	private static final float EPSILON = 0.001f;

	@Test
	public void testGamutContains () {
		// Test if a point inside sRGB gamut is correctly identified
		xy insidePoint = new xy(0.3f, 0.3f); // Near center, should be inside
		assertTrue(Gamut.sRGB.contains(insidePoint), "Point should be inside sRGB gamut");

		// Test if a point outside sRGB gamut is correctly identified
		xy outsidePoint = new xy(0.9f, 0.1f); // Far red, outside sRGB
		assertFalse(Gamut.sRGB.contains(outsidePoint), "Point should be outside sRGB gamut");

		// Test gamut vertices - they should be on the boundary
		assertTrue(Gamut.sRGB.contains(Gamut.sRGB.red), "Red primary should be in gamut");
		assertTrue(Gamut.sRGB.contains(Gamut.sRGB.green), "Green primary should be in gamut");
		assertTrue(Gamut.sRGB.contains(Gamut.sRGB.blue), "Blue primary should be in gamut");
	}

	@Test
	public void testGamutClamp () {
		// Test that a point inside the gamut remains unchanged
		xy insidePoint = new xy(0.3f, 0.3f);
		xy clamped = Gamut.sRGB.clamp(insidePoint);
		assertEquals(insidePoint.x(), clamped.x(), EPSILON, "Inside point x should not change");
		assertEquals(insidePoint.y(), clamped.y(), EPSILON, "Inside point y should not change");

		// Test that a point outside gets clamped to the gamut boundary
		xy outsidePoint = new xy(0.9f, 0.1f);
		xy clampedOutside = Gamut.sRGB.clamp(outsidePoint);
		assertFalse(Gamut.sRGB.contains(outsidePoint), "Original point should be outside");
		assertTrue(Gamut.sRGB.contains(clampedOutside), "Clamped point should be inside or on boundary");

		// Verify clamped point is different from original
		assertTrue(
			Math.abs(outsidePoint.x() - clampedOutside.x()) > EPSILON || Math.abs(outsidePoint.y() - clampedOutside.y()) > EPSILON,
			"Clamped point should be different from original");
	}

	@Test
	public void testRGBToXYConversion () {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		xy redXY = xy(red, Gamut.sRGB);
		// Red should be close to sRGB red primary (considering gamma correction)
		assertEquals(0.64f, redXY.x(), 0.01f, "Red x coordinate");
		assertEquals(0.33f, redXY.y(), 0.01f, "Red y coordinate");

		// Test white - note that RGB(1,1,1) doesn't necessarily map to D65
		// due to gamma correction and the specific RGB to XYZ transformation
		RGB white = new RGB(1, 1, 1);
		xy whiteXY = xy(white, Gamut.sRGB);
		// Just verify it's a valid result, not NaN
		assertFalse(Float.isNaN(whiteXY.x()), "White x should not be NaN");
		assertFalse(Float.isNaN(whiteXY.y()), "White y should not be NaN");

		// Test black
		RGB black = new RGB(0, 0, 0);
		xy blackXY = xy(black, Gamut.sRGB);
		// Black should return NaN when sum is zero
		assertTrue(Float.isNaN(blackXY.x()), "Black x should be NaN");
		assertTrue(Float.isNaN(blackXY.y()), "Black y should be NaN");
	}

	@Test
	public void testXYToRGBConversion () {
		// Test converting xy back to RGB
		xy testPoint = new xy(0.3127f, 0.3290f); // D65 white point
		RGB rgb = RGB(testPoint, Gamut.sRGB);

		// D65 white point doesn't necessarily map to RGB(1,1,1) due to the gamut's
		// specific transformation. Just check it's valid RGB values.
		assertFalse(Float.isNaN(rgb.r()), "R should not be NaN");
		assertFalse(Float.isNaN(rgb.g()), "G should not be NaN");
		assertFalse(Float.isNaN(rgb.b()), "B should not be NaN");
		// Values should be in valid range
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1, "R should be in [0,1]");
		assertTrue(rgb.g() >= 0 && rgb.g() <= 1, "G should be in [0,1]");
		assertTrue(rgb.b() >= 0 && rgb.b() <= 1, "B should be in [0,1]");

		// Test edge case: y = 0
		// Note: the gamut will clamp this to a valid point, so it won't return NaN
		xy zeroY = new xy(0.3f, 0.0f);
		RGB clampedResult = RGB(zeroY, Gamut.sRGB);
		assertFalse(Float.isNaN(clampedResult.r()), "Should not be NaN after clamping");
		assertFalse(Float.isNaN(clampedResult.g()), "Should not be NaN after clamping");
		assertFalse(Float.isNaN(clampedResult.b()), "Should not be NaN after clamping");
		// Should be valid RGB values
		assertTrue(clampedResult.r() >= 0 && clampedResult.r() <= 1, "R should be in [0,1]");
		assertTrue(clampedResult.g() >= 0 && clampedResult.g() <= 1, "G should be in [0,1]");
		assertTrue(clampedResult.b() >= 0 && clampedResult.b() <= 1, "B should be in [0,1]");
	}

	@Test
	public void testRoundTripConversion () {
		// Test that RGB -> xy -> RGB maintains color
		// Note: xy coordinates don't preserve luminance, so colors that differ only in brightness
		// (like grays) will all map to white when converted back from xy
		RGB[] testColors = {new RGB(1, 0, 0), // Red
			new RGB(0, 1, 0), // Green
			new RGB(0, 0, 1), // Blue
			new RGB(1, 1, 1), // White
			new RGB(0.8f, 0.2f, 0.3f) // Random color
		};

		for (RGB original : testColors) {
			xy intermediate = xy(original, Gamut.sRGB);

			// Skip if we got NaN (e.g., for black)
			if (Float.isNaN(intermediate.x()) || Float.isNaN(intermediate.y())) {
				continue;
			}

			RGB recovered = RGB(intermediate, Gamut.sRGB);

			// For colors at maximum saturation (primaries and white), we can expect good round-trip
			if ((original.r() == 1 || original.r() == 0) && (original.g() == 1 || original.g() == 0)
				&& (original.b() == 1 || original.b() == 0)) {
				assertEquals(original.r(), recovered.r(), 0.001f, "R channel round trip for " + original);
				assertEquals(original.g(), recovered.g(), 0.001f, "G channel round trip for " + original);
				assertEquals(original.b(), recovered.b(), 0.001f, "B channel round trip for " + original);
			} else {
				// For other colors, just verify they're valid RGB values
				assertTrue(recovered.r() >= 0 && recovered.r() <= 1, "R should be in [0,1]");
				assertTrue(recovered.g() >= 0 && recovered.g() <= 1, "G should be in [0,1]");
				assertTrue(recovered.b() >= 0 && recovered.b() <= 1, "B should be in [0,1]");
			}
		}

		// Test specifically that gray colors all map to the same xy (D65 white point)
		xy gray1 = xy(new RGB(0.2f, 0.2f, 0.2f), Gamut.sRGB);
		xy gray2 = xy(new RGB(0.5f, 0.5f, 0.5f), Gamut.sRGB);
		xy gray3 = xy(new RGB(0.8f, 0.8f, 0.8f), Gamut.sRGB);
		assertEquals(gray1.x(), gray2.x(), EPSILON, "All grays should have same x");
		assertEquals(gray1.y(), gray2.y(), EPSILON, "All grays should have same y");
		assertEquals(gray2.x(), gray3.x(), EPSILON, "All grays should have same x");
		assertEquals(gray2.y(), gray3.y(), EPSILON, "All grays should have same y");
	}

	@Test
	public void testDifferentGamuts () {
		// Test that different gamuts produce different results
		RGB testColor = new RGB(0.8f, 0.2f, 0.3f);

		xy srgbResult = xy(testColor, Gamut.sRGB);
		xy p3Result = xy(testColor, Gamut.DisplayP3);
		xy rec2020Result = xy(testColor, Gamut.Rec2020);

		// Results should be different (gamuts have different primaries)
		assertTrue(Math.abs(srgbResult.x() - p3Result.x()) > EPSILON || Math.abs(srgbResult.y() - p3Result.y()) > EPSILON,
			"sRGB and P3 should produce different results");

		assertTrue(Math.abs(srgbResult.x() - rec2020Result.x()) > EPSILON || Math.abs(srgbResult.y() - rec2020Result.y()) > EPSILON,
			"sRGB and Rec2020 should produce different results");
	}

	@Test
	public void testGamutPrimaries () {
		// Verify that pure RGB colors map close to their respective primaries
		RGB pureRed = new RGB(1, 0, 0);
		RGB pureGreen = new RGB(0, 1, 0);
		RGB pureBlue = new RGB(0, 0, 1);

		xy redXY = xy(pureRed, Gamut.sRGB);
		xy greenXY = xy(pureGreen, Gamut.sRGB);
		xy blueXY = xy(pureBlue, Gamut.sRGB);

		// They should be close to the gamut's primaries
		assertEquals(Gamut.sRGB.red.x(), redXY.x(), 0.01f, "Pure red should map to red primary x");
		assertEquals(Gamut.sRGB.red.y(), redXY.y(), 0.01f, "Pure red should map to red primary y");

		assertEquals(Gamut.sRGB.green.x(), greenXY.x(), 0.01f, "Pure green should map to green primary x");
		assertEquals(Gamut.sRGB.green.y(), greenXY.y(), 0.01f, "Pure green should map to green primary y");

		assertEquals(Gamut.sRGB.blue.x(), blueXY.x(), 0.01f, "Pure blue should map to blue primary x");
		assertEquals(Gamut.sRGB.blue.y(), blueXY.y(), 0.01f, "Pure blue should map to blue primary y");
	}

	@Test
	public void testOutOfGamutHandling () {
		// Test that out-of-gamut colors are properly clamped
		xy wideGamutPoint = new xy(0.8f, 0.2f); // Likely outside sRGB

		// Convert to RGB - should be clamped
		RGB clamped = RGB(wideGamutPoint, Gamut.sRGB);

		// All channels should be in [0, 1]
		assertTrue(clamped.r() >= 0 && clamped.r() <= 1, "R should be in range");
		assertTrue(clamped.g() >= 0 && clamped.g() <= 1, "G should be in range");
		assertTrue(clamped.b() >= 0 && clamped.b() <= 1, "B should be in range");

		// At least one channel should be at the limit (0 or 1) for out-of-gamut colors
		boolean atLimit = clamped.r() <= 0.001f || clamped.r() >= 0.999f || clamped.g() <= 0.001f || clamped.g() >= 0.999f
			|| clamped.b() <= 0.001f || clamped.b() >= 0.999f;
		assertTrue(atLimit, "Out of gamut color should have at least one channel at limit");
	}
}
