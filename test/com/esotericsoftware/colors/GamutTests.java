
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.colors.Colors.RGB;
import com.esotericsoftware.colors.Colors.xy;

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

		// Test white (D65)
		RGB white = new RGB(1, 1, 1);
		xy whiteXY = xy(white, Gamut.sRGB);
		assertEquals(0.3127f, whiteXY.x(), 0.001f, "White x should be D65");
		assertEquals(0.3290f, whiteXY.y(), 0.001f, "White y should be D65");

		// Test black
		RGB black = new RGB(0, 0, 0);
		xy blackXY = xy(black, Gamut.sRGB);
		// Black should return D65 as default when sum is zero
		assertEquals(0.3127f, blackXY.x(), 0.001f, "Black x should default to D65");
		assertEquals(0.3290f, blackXY.y(), 0.001f, "Black y should default to D65");
	}

	@Test
	public void testXYToRGBConversion () {
		// Test converting xy back to RGB
		xy testPoint = new xy(0.3127f, 0.3290f); // D65 white point
		RGB rgb = RGB(testPoint, Gamut.sRGB);

		// Should be close to white
		assertEquals(1.0f, rgb.r(), 0.01f, "R should be close to 1");
		assertEquals(1.0f, rgb.g(), 0.01f, "G should be close to 1");
		assertEquals(1.0f, rgb.b(), 0.01f, "B should be close to 1");

		// Test edge case: y = 0
		xy zeroY = new xy(0.3f, 0.0f);
		RGB blackResult = RGB(zeroY, Gamut.sRGB);
		assertEquals(0.0f, blackResult.r(), EPSILON, "Should return black for y=0");
		assertEquals(0.0f, blackResult.g(), EPSILON, "Should return black for y=0");
		assertEquals(0.0f, blackResult.b(), EPSILON, "Should return black for y=0");
	}

	@Test
	public void testRoundTripConversion () {
		// Test that RGB -> xy -> RGB maintains color (within tolerance)
		RGB[] testColors = {new RGB(1, 0, 0), // Red
			new RGB(0, 1, 0), // Green
			new RGB(0, 0, 1), // Blue
			new RGB(1, 1, 1), // White
			new RGB(0.5f, 0.5f, 0.5f), // Gray
			new RGB(0.8f, 0.2f, 0.3f) // Random color
		};

		for (RGB original : testColors) {
			xy intermediate = xy(original, Gamut.sRGB);
			RGB recovered = RGB(intermediate, Gamut.sRGB);

			// Allow for some tolerance due to gamma correction and normalization
			assertEquals(original.r(), recovered.r(), 0.02f, "R channel round trip for " + original);
			assertEquals(original.g(), recovered.g(), 0.02f, "G channel round trip for " + original);
			assertEquals(original.b(), recovered.b(), 0.02f, "B channel round trip for " + original);
		}
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
