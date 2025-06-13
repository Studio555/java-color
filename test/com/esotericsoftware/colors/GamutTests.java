package com.esotericsoftware.colors;

import static org.junit.Assert.*;

import org.junit.Test;

import com.esotericsoftware.colors.Colors.RGB;
import com.esotericsoftware.colors.Colors.xy;
import com.esotericsoftware.colors.Gamut;

public class GamutTests {
	private static final float EPSILON = 0.001f;

	@Test
	public void testGamutContains() {
		// Test if a point inside sRGB gamut is correctly identified
		xy insidePoint = new xy(0.3f, 0.3f); // Near center, should be inside
		assertTrue("Point should be inside sRGB gamut", Gamut.sRGB.contains(insidePoint));
		
		// Test if a point outside sRGB gamut is correctly identified
		xy outsidePoint = new xy(0.9f, 0.1f); // Far red, outside sRGB
		assertFalse("Point should be outside sRGB gamut", Gamut.sRGB.contains(outsidePoint));
		
		// Test gamut vertices - they should be on the boundary
		assertTrue("Red primary should be in gamut", Gamut.sRGB.contains(Gamut.sRGB.red));
		assertTrue("Green primary should be in gamut", Gamut.sRGB.contains(Gamut.sRGB.green));
		assertTrue("Blue primary should be in gamut", Gamut.sRGB.contains(Gamut.sRGB.blue));
	}

	@Test
	public void testGamutClamp() {
		// Test that a point inside the gamut remains unchanged
		xy insidePoint = new xy(0.3f, 0.3f);
		xy clamped = Gamut.sRGB.clamp(insidePoint);
		assertEquals("Inside point x should not change", insidePoint.x(), clamped.x(), EPSILON);
		assertEquals("Inside point y should not change", insidePoint.y(), clamped.y(), EPSILON);
		
		// Test that a point outside gets clamped to the gamut boundary
		xy outsidePoint = new xy(0.9f, 0.1f);
		xy clampedOutside = Gamut.sRGB.clamp(outsidePoint);
		assertFalse("Original point should be outside", Gamut.sRGB.contains(outsidePoint));
		assertTrue("Clamped point should be inside or on boundary", Gamut.sRGB.contains(clampedOutside));
		
		// Verify clamped point is different from original
		assertTrue("Clamped point should be different from original",
			Math.abs(outsidePoint.x() - clampedOutside.x()) > EPSILON ||
			Math.abs(outsidePoint.y() - clampedOutside.y()) > EPSILON);
	}

	@Test
	public void testRGBToXYConversion() {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		xy redXY = Gamut.sRGB.fromRGB(red);
		// Red should be close to sRGB red primary (considering gamma correction)
		assertEquals("Red x coordinate", 0.64f, redXY.x(), 0.01f);
		assertEquals("Red y coordinate", 0.33f, redXY.y(), 0.01f);
		
		// Test white (D65)
		RGB white = new RGB(1, 1, 1);
		xy whiteXY = Gamut.sRGB.fromRGB(white);
		assertEquals("White x should be D65", 0.3127f, whiteXY.x(), 0.001f);
		assertEquals("White y should be D65", 0.3290f, whiteXY.y(), 0.001f);
		
		// Test black
		RGB black = new RGB(0, 0, 0);
		xy blackXY = Gamut.sRGB.fromRGB(black);
		// Black should return D65 as default when sum is zero
		assertEquals("Black x should default to D65", 0.3127f, blackXY.x(), 0.001f);
		assertEquals("Black y should default to D65", 0.3290f, blackXY.y(), 0.001f);
	}

	@Test
	public void testXYToRGBConversion() {
		// Test converting xy back to RGB
		xy testPoint = new xy(0.3127f, 0.3290f); // D65 white point
		RGB rgb = Gamut.sRGB.toRGB(testPoint);
		
		// Should be close to white
		assertEquals("R should be close to 1", 1.0f, rgb.r(), 0.01f);
		assertEquals("G should be close to 1", 1.0f, rgb.g(), 0.01f);
		assertEquals("B should be close to 1", 1.0f, rgb.b(), 0.01f);
		
		// Test edge case: y = 0
		xy zeroY = new xy(0.3f, 0.0f);
		RGB blackResult = Gamut.sRGB.toRGB(zeroY);
		assertEquals("Should return black for y=0", 0.0f, blackResult.r(), EPSILON);
		assertEquals("Should return black for y=0", 0.0f, blackResult.g(), EPSILON);
		assertEquals("Should return black for y=0", 0.0f, blackResult.b(), EPSILON);
	}

	@Test
	public void testRoundTripConversion() {
		// Test that RGB -> xy -> RGB maintains color (within tolerance)
		RGB[] testColors = {
			new RGB(1, 0, 0),     // Red
			new RGB(0, 1, 0),     // Green
			new RGB(0, 0, 1),     // Blue
			new RGB(1, 1, 1),     // White
			new RGB(0.5f, 0.5f, 0.5f), // Gray
			new RGB(0.8f, 0.2f, 0.3f)  // Random color
		};
		
		for (RGB original : testColors) {
			xy intermediate = Gamut.sRGB.fromRGB(original);
			RGB recovered = Gamut.sRGB.toRGB(intermediate);
			
			// Allow for some tolerance due to gamma correction and normalization
			assertEquals("R channel round trip for " + original, original.r(), recovered.r(), 0.02f);
			assertEquals("G channel round trip for " + original, original.g(), recovered.g(), 0.02f);
			assertEquals("B channel round trip for " + original, original.b(), recovered.b(), 0.02f);
		}
	}

	@Test
	public void testDifferentGamuts() {
		// Test that different gamuts produce different results
		RGB testColor = new RGB(0.8f, 0.2f, 0.3f);
		
		xy srgbResult = Gamut.sRGB.fromRGB(testColor);
		xy p3Result = Gamut.displayP3.fromRGB(testColor);
		xy rec2020Result = Gamut.rec2020.fromRGB(testColor);
		
		// Results should be different (gamuts have different primaries)
		assertTrue("sRGB and P3 should produce different results",
			Math.abs(srgbResult.x() - p3Result.x()) > EPSILON ||
			Math.abs(srgbResult.y() - p3Result.y()) > EPSILON);
		
		assertTrue("sRGB and Rec2020 should produce different results",
			Math.abs(srgbResult.x() - rec2020Result.x()) > EPSILON ||
			Math.abs(srgbResult.y() - rec2020Result.y()) > EPSILON);
	}

	@Test
	public void testGamutPrimaries() {
		// Verify that pure RGB colors map close to their respective primaries
		RGB pureRed = new RGB(1, 0, 0);
		RGB pureGreen = new RGB(0, 1, 0);
		RGB pureBlue = new RGB(0, 0, 1);
		
		xy redXY = Gamut.sRGB.fromRGB(pureRed);
		xy greenXY = Gamut.sRGB.fromRGB(pureGreen);
		xy blueXY = Gamut.sRGB.fromRGB(pureBlue);
		
		// They should be close to the gamut's primaries
		assertEquals("Pure red should map to red primary x", Gamut.sRGB.red.x(), redXY.x(), 0.01f);
		assertEquals("Pure red should map to red primary y", Gamut.sRGB.red.y(), redXY.y(), 0.01f);
		
		assertEquals("Pure green should map to green primary x", Gamut.sRGB.green.x(), greenXY.x(), 0.01f);
		assertEquals("Pure green should map to green primary y", Gamut.sRGB.green.y(), greenXY.y(), 0.01f);
		
		assertEquals("Pure blue should map to blue primary x", Gamut.sRGB.blue.x(), blueXY.x(), 0.01f);
		assertEquals("Pure blue should map to blue primary y", Gamut.sRGB.blue.y(), blueXY.y(), 0.01f);
	}

	@Test
	public void testOutOfGamutHandling() {
		// Test that out-of-gamut colors are properly clamped
		xy wideGamutPoint = new xy(0.8f, 0.2f); // Likely outside sRGB
		
		// Convert to RGB - should be clamped
		RGB clamped = Gamut.sRGB.toRGB(wideGamutPoint);
		
		// All channels should be in [0, 1]
		assertTrue("R should be in range", clamped.r() >= 0 && clamped.r() <= 1);
		assertTrue("G should be in range", clamped.g() >= 0 && clamped.g() <= 1);
		assertTrue("B should be in range", clamped.b() >= 0 && clamped.b() <= 1);
		
		// At least one channel should be at the limit (0 or 1) for out-of-gamut colors
		boolean atLimit = clamped.r() <= 0.001f || clamped.r() >= 0.999f ||
		                  clamped.g() <= 0.001f || clamped.g() >= 0.999f ||
		                  clamped.b() <= 0.001f || clamped.b() >= 0.999f;
		assertTrue("Out of gamut color should have at least one channel at limit", atLimit);
	}
}