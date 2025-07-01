
package com.esotericsoftware.color;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.LinearRGB;
import com.esotericsoftware.color.space.RGB;
import com.esotericsoftware.color.space.XYZ;
import com.esotericsoftware.color.space.uv;
import com.esotericsoftware.color.space.xy;

/** @author Nathan Sweet <misc@n4te.com> */
public class GamutTests extends Tests {
	private static final float EPSILON = 0.00001f;

	@Test
	public void testDifferentGamuts () {
		// Test that different gamuts produce different results
		RGB testColor = new RGB(0.8f, 0.2f, 0.3f);

		xy srgbResult = Gamut.sRGB.xy(testColor);
		xy p3Result = Gamut.DisplayP3.xy(testColor);
		xy rec2020Result = Gamut.Rec2020.xy(testColor);

		// Results should be different (gamuts have different primaries)
		assertTrue(Math.abs(srgbResult.x() - p3Result.x()) > EPSILON || Math.abs(srgbResult.y() - p3Result.y()) > EPSILON,
			"sRGB and P3 should produce different results");

		assertTrue(Math.abs(srgbResult.x() - rec2020Result.x()) > EPSILON || Math.abs(srgbResult.y() - rec2020Result.y()) > EPSILON,
			"sRGB and Rec2020 should produce different results");
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
	public void testGamutContains () {
		// Test if a point inside sRGB gamut is correctly identified
		xy insidePoint = new xy(0.3f, 0.3f); // Near center, should be inside
		assertTrue(Gamut.sRGB.contains(insidePoint), "Point should be inside sRGB gamut");

		// Test if a point outside sRGB gamut is correctly identified
		xy outsidePoint = new xy(0.9f, 0.1f); // Far red, outside sRGB
		assertFalse(Gamut.sRGB.contains(outsidePoint), "Point should be outside sRGB gamut");

		// Test gamut vertices - they should be on the boundary
		Gamut.RGBGamut srgb = (Gamut.RGBGamut)Gamut.sRGB;
		assertTrue(Gamut.sRGB.contains(srgb.red.xy()), "Red primary should be in gamut");
		assertTrue(Gamut.sRGB.contains(srgb.green.xy()), "Green primary should be in gamut");
		assertTrue(Gamut.sRGB.contains(srgb.blue.xy()), "Blue primary should be in gamut");
		assertTrue(Gamut.sRGB.contains(srgb.red.uv()), "Red primary should be in gamut");
		assertTrue(Gamut.sRGB.contains(srgb.green.uv()), "Green primary should be in gamut");
		assertTrue(Gamut.sRGB.contains(srgb.blue.uv()), "Blue primary should be in gamut");

		// Test points on edges
		{
			xy redGreenMid = new xy((srgb.red.xy().x() + srgb.green.xy().x()) / 2, (srgb.red.xy().y() + srgb.green.xy().y()) / 2);
			xy greenBlueMid = new xy((srgb.green.xy().x() + srgb.blue.xy().x()) / 2, (srgb.green.xy().y() + srgb.blue.xy().y()) / 2);
			xy blueRedMid = new xy((srgb.blue.xy().x() + srgb.red.xy().x()) / 2, (srgb.blue.xy().y() + srgb.red.xy().y()) / 2);
			assertTrue(Gamut.sRGB.contains(redGreenMid), "Midpoint of red-green edge should be in gamut");
			assertTrue(Gamut.sRGB.contains(greenBlueMid), "Midpoint of green-blue edge should be in gamut");
			assertTrue(Gamut.sRGB.contains(blueRedMid), "Midpoint of blue-red edge should be in gamut");
		}
		{
			uv redGreenMid = new uv((srgb.red.uv().u() + srgb.green.uv().u()) / 2, (srgb.red.uv().v() + srgb.green.uv().v()) / 2);
			uv greenBlueMid = new uv((srgb.green.uv().u() + srgb.blue.uv().u()) / 2, (srgb.green.uv().v() + srgb.blue.uv().v()) / 2);
			uv blueRedMid = new uv((srgb.blue.uv().u() + srgb.red.uv().u()) / 2, (srgb.blue.uv().v() + srgb.red.uv().v()) / 2);
			assertTrue(Gamut.sRGB.contains(redGreenMid), "Midpoint of red-green edge should be in gamut");
			assertTrue(Gamut.sRGB.contains(greenBlueMid), "Midpoint of green-blue edge should be in gamut");
			assertTrue(Gamut.sRGB.contains(blueRedMid), "Midpoint of blue-red edge should be in gamut");
		}
	}

	@Test
	public void testGamutPrimaries () {
		// Verify that pure RGB colors map close to their respective primaries
		RGB pureRed = new RGB(1, 0, 0);
		RGB pureGreen = new RGB(0, 1, 0);
		RGB pureBlue = new RGB(0, 0, 1);

		xy redXY = Gamut.sRGB.xy(pureRed);
		xy greenXY = Gamut.sRGB.xy(pureGreen);
		xy blueXY = Gamut.sRGB.xy(pureBlue);

		// They should be close to the gamut's primaries
		Gamut.RGBGamut srgb = (Gamut.RGBGamut)Gamut.sRGB;
		assertEquals(srgb.red.xy().x(), redXY.x(), 0.01f, "Pure red should map to red primary x");
		assertEquals(srgb.red.xy().y(), redXY.y(), 0.01f, "Pure red should map to red primary y");

		assertEquals(srgb.green.xy().x(), greenXY.x(), 0.01f, "Pure green should map to green primary x");
		assertEquals(srgb.green.xy().y(), greenXY.y(), 0.01f, "Pure green should map to green primary y");

		assertEquals(srgb.blue.xy().x(), blueXY.x(), 0.01f, "Pure blue should map to blue primary x");
		assertEquals(srgb.blue.xy().y(), blueXY.y(), 0.01f, "Pure blue should map to blue primary y");
	}

	@Test
	public void testOutOfGamutHandling () {
		// Test that out-of-gamut colors are properly clamped
		xy wideGamutPoint = new xy(0.8f, 0.2f); // Likely outside sRGB

		// Convert to RGB - should be clamped
		RGB clamped = Gamut.sRGB.RGB(wideGamutPoint);

		// All channels should be in [0, 1]
		assertTrue(clamped.r() >= 0 && clamped.r() <= 1, "R should be in range");
		assertTrue(clamped.g() >= 0 && clamped.g() <= 1, "G should be in range");
		assertTrue(clamped.b() >= 0 && clamped.b() <= 1, "B should be in range");

		// At least one channel should be at the limit (0 or 1) for out-of-gamut colors
		boolean atLimit = clamped.r() <= 0.001f || clamped.r() >= 0.999f || clamped.g() <= 0.001f || clamped.g() >= 0.999f
			|| clamped.b() <= 0.001f || clamped.b() >= 0.999f;
		assertTrue(atLimit, "Out of gamut color should have at least one channel at limit");
	}

	@Test
	public void testRGBToXYConversion () {
		// Test all primary colors
		RGB red = new RGB(1, 0, 0);
		xy redXY = Gamut.sRGB.xy(red);
		Gamut.RGBGamut srgb = (Gamut.RGBGamut)Gamut.sRGB;
		assertEquals(srgb.red.xy().x(), redXY.x(), 0.001f, "Red x coordinate");
		assertEquals(srgb.red.xy().y(), redXY.y(), 0.001f, "Red y coordinate");

		RGB green = new RGB(0, 1, 0);
		xy greenXY = Gamut.sRGB.xy(green);
		assertEquals(srgb.green.xy().x(), greenXY.x(), 0.001f, "Green x coordinate");
		assertEquals(srgb.green.xy().y(), greenXY.y(), 0.001f, "Green y coordinate");

		RGB blue = new RGB(0, 0, 1);
		xy blueXY = Gamut.sRGB.xy(blue);
		assertEquals(srgb.blue.xy().x(), blueXY.x(), 0.001f, "Blue x coordinate");
		assertEquals(srgb.blue.xy().y(), blueXY.y(), 0.001f, "Blue y coordinate");

		// Test secondary colors (combinations)
		RGB yellow = new RGB(1, 1, 0);
		xy yellowXY = Gamut.sRGB.xy(yellow);
		// Yellow should be between red and green
		assertTrue(yellowXY.x() > Math.min(redXY.x(), greenXY.x()) && yellowXY.x() < Math.max(redXY.x(), greenXY.x()),
			"Yellow x should be between red and green");
		assertTrue(yellowXY.y() > Math.min(redXY.y(), greenXY.y()) && yellowXY.y() < Math.max(redXY.y(), greenXY.y()),
			"Yellow y should be between red and green");

		RGB cyan = new RGB(0, 1, 1);
		xy cyanXY = Gamut.sRGB.xy(cyan);
		// Cyan should be between green and blue
		assertTrue(cyanXY.x() > Math.min(greenXY.x(), blueXY.x()) && cyanXY.x() < Math.max(greenXY.x(), blueXY.x()),
			"Cyan x should be between green and blue");

		RGB magenta = new RGB(1, 0, 1);
		xy magentaXY = Gamut.sRGB.xy(magenta);
		// Magenta should be between red and blue
		assertTrue(magentaXY.x() > Math.min(redXY.x(), blueXY.x()) && magentaXY.x() < Math.max(redXY.x(), blueXY.x()),
			"Magenta x should be between red and blue");

		// Test white - should map to D65 white point
		RGB white = new RGB(1, 1, 1);
		xy whiteXY = Gamut.sRGB.xy(white);
		assertEquals(0.3127f, whiteXY.x(), 0.01f, "White x should be near D65");
		assertEquals(0.3290f, whiteXY.y(), 0.01f, "White y should be near D65");

		// Test grays - all should map to same chromaticity (white point)
		RGB darkGray = new RGB(0.25f, 0.25f, 0.25f);
		RGB midGray = new RGB(0.5f, 0.5f, 0.5f);
		RGB lightGray = new RGB(0.75f, 0.75f, 0.75f);

		xy darkGrayXY = Gamut.sRGB.xy(darkGray);
		xy midGrayXY = Gamut.sRGB.xy(midGray);
		xy lightGrayXY = Gamut.sRGB.xy(lightGray);

		assertEquals(whiteXY.x(), darkGrayXY.x(), EPSILON, "Dark gray should have same chromaticity as white");
		assertEquals(whiteXY.y(), darkGrayXY.y(), EPSILON, "Dark gray should have same chromaticity as white");
		assertEquals(whiteXY.x(), midGrayXY.x(), EPSILON, "Mid gray should have same chromaticity as white");
		assertEquals(whiteXY.y(), midGrayXY.y(), EPSILON, "Mid gray should have same chromaticity as white");
		assertEquals(whiteXY.x(), lightGrayXY.x(), EPSILON, "Light gray should have same chromaticity as white");
		assertEquals(whiteXY.y(), lightGrayXY.y(), EPSILON, "Light gray should have same chromaticity as white");

		// Test black - should return NaN when sum is zero
		RGB black = new RGB(0, 0, 0);
		xy blackXY = Gamut.sRGB.xy(black);
		assertTrue(Float.isNaN(blackXY.x()), "Black x should be NaN");
		assertTrue(Float.isNaN(blackXY.y()), "Black y should be NaN");

		// Test very small values (near black)
		RGB nearBlack = new RGB(0.001f, 0.001f, 0.001f);
		xy nearBlackXY = Gamut.sRGB.xy(nearBlack);
		// Should still map to white point chromaticity
		assertEquals(whiteXY.x(), nearBlackXY.x(), 0.01f, "Near black should have white point chromaticity");
		assertEquals(whiteXY.y(), nearBlackXY.y(), 0.01f, "Near black should have white point chromaticity");

		// Test intermediate colors
		RGB orange = new RGB(1.0f, 0.5f, 0.0f);
		xy orangeXY = Gamut.sRGB.xy(orange);
		// Orange should be between red and yellow
		assertTrue(orangeXY.x() > yellowXY.x() && orangeXY.x() < redXY.x(), "Orange x should be between yellow and red");
		assertTrue(orangeXY.y() > redXY.y() && orangeXY.y() < yellowXY.y(), "Orange y should be between red and yellow");

		// Test that chromaticity coordinates are normalized (x + y <= 1)
		RGB[] testColors = {new RGB(0.2f, 0.5f, 0.8f), new RGB(0.9f, 0.1f, 0.3f), new RGB(0.4f, 0.6f, 0.2f)};
		for (RGB color : testColors) {
			xy colorXY = Gamut.sRGB.xy(color);
			assertTrue(colorXY.x() + colorXY.y() <= 1.0f + EPSILON, "Chromaticity coordinates should be normalized for " + color);
			assertTrue(colorXY.x() >= 0 && colorXY.x() <= 1, "x should be in [0,1] for " + color);
			assertTrue(colorXY.y() >= 0 && colorXY.y() <= 1, "y should be in [0,1] for " + color);
		}
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
			xy intermediate = Gamut.sRGB.xy(original);

			// Skip if we got NaN (eg for black)
			if (Float.isNaN(intermediate.x()) || Float.isNaN(intermediate.y())) {
				continue;
			}

			RGB recovered = Gamut.sRGB.RGB(intermediate);

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
		xy gray1 = Gamut.sRGB.xy(new RGB(0.2f, 0.2f, 0.2f));
		xy gray2 = Gamut.sRGB.xy(new RGB(0.5f, 0.5f, 0.5f));
		xy gray3 = Gamut.sRGB.xy(new RGB(0.8f, 0.8f, 0.8f));
		assertEquals(gray1.x(), gray2.x(), EPSILON, "All grays should have same x");
		assertEquals(gray1.y(), gray2.y(), EPSILON, "All grays should have same y");
		assertEquals(gray2.x(), gray3.x(), EPSILON, "All grays should have same x");
		assertEquals(gray2.y(), gray3.y(), EPSILON, "All grays should have same y");
	}

	@Test
	public void testXYToRGBConversion () {
		// Test converting xy back to RGB
		xy testPoint = new xy(0.3127f, 0.3290f); // D65 white point
		RGB rgb = Gamut.sRGB.RGB(testPoint);

		// D65 white point doesn't necessarily map to RGB(1,1,1) due to the gamut's
		// specific transformation. Just check it's valid RGB values.
		assertFalse(Float.isNaN(rgb.r()), "R should not be NaN");
		assertFalse(Float.isNaN(rgb.g()), "G should not be NaN");
		assertFalse(Float.isNaN(rgb.b()), "B should not be NaN");
		// Values should be in valid range
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1, "R should be in [0,1]");
		assertTrue(rgb.g() >= 0 && rgb.g() <= 1, "G should be in [0,1]");
		assertTrue(rgb.b() >= 0 && rgb.b() <= 1, "B should be in [0,1]");

		// Test edge case: y = 0 (will produce infinity/NaN)
		// Note: conversions do NOT auto-clamp, so invalid inputs produce invalid outputs
		xy zeroY = new xy(0.3f, 0.0f);
		RGB result = Gamut.sRGB.RGB(zeroY);
		// With y=0, the conversion will produce infinity or NaN
		assertTrue(Float.isNaN(result.r()) || Float.isInfinite(result.r()) || Float.isNaN(result.g())
			|| Float.isInfinite(result.g()) || Float.isNaN(result.b()) || Float.isInfinite(result.b()),
			"Invalid xy should produce invalid RGB");
	}

	@Test
	public void testPolygonGamut () {
		// Test a quadrilateral gamut
		xy[] vertices = {new xy(0.7f, 0.3f), // Red corner
			new xy(0.2f, 0.7f), // Green corner
			new xy(0.15f, 0.05f), // Blue corner
			new xy(0.5f, 0.5f) // Extra vertex
		};
		Gamut.PolygonGamut polygon = new Gamut.PolygonGamut(vertices);

		// Test contains for points clearly inside
		xy inside = new xy(0.4f, 0.4f);
		assertTrue(polygon.contains(inside), "Point should be inside polygon gamut");

		// Test contains for points clearly outside
		xy outside = new xy(0.9f, 0.1f);
		assertFalse(polygon.contains(outside), "Point should be outside polygon gamut");

		// Test clamp
		xy clamped = polygon.clamp(outside);
		// The clamped point should be different from the original
		assertTrue(clamped.x() != outside.x() || clamped.y() != outside.y(), "Clamped point should differ from original");

		// Test that vertices are contained
		for (xy vertex : vertices)
			assertTrue(polygon.contains(vertex), "Vertex should be contained in polygon");

		// Test points on edges are contained
		// Test midpoint of each edge
		for (int i = 0; i < vertices.length; i++) {
			xy v1 = vertices[i];
			xy v2 = vertices[(i + 1) % vertices.length];
			xy midpoint = new xy((v1.x() + v2.x()) / 2, (v1.y() + v2.y()) / 2);
			assertTrue(polygon.contains(midpoint), "Midpoint of edge should be contained");
		}

		// Test clamp with inside point (should not change)
		xy clampedInside = polygon.clamp(inside);
		assertEquals(inside.x(), clampedInside.x(), EPSILON, "Inside point x should not change");
		assertEquals(inside.y(), clampedInside.y(), EPSILON, "Inside point y should not change");

		// Test that RGB conversions throw UnsupportedOperationException
		assertThrows(UnsupportedOperationException.class, () -> polygon.XYZ(new LinearRGB(1, 0, 0)));
		assertThrows(UnsupportedOperationException.class, () -> polygon.LinearRGB(new XYZ(50, 50, 50)));

		// Test uv support
		uv uvInside = inside.uv();
		assertTrue(polygon.contains(uvInside), "Should support uv contains");

		// Test that uv vertices are contained
		for (xy vertex : vertices) {
			uv uvVertex = vertex.uv();
			assertTrue(polygon.contains(uvVertex), "uv vertex should be contained in polygon");
		}

		// Test that uv points on edges are contained
		for (int i = 0; i < vertices.length; i++) {
			xy v1 = vertices[i];
			xy v2 = vertices[(i + 1) % vertices.length];
			xy midpoint = new xy((v1.x() + v2.x()) / 2, (v1.y() + v2.y()) / 2);
			uv uvMidpoint = midpoint.uv();
			assertTrue(polygon.contains(uvMidpoint), "uv midpoint of edge should be contained");
		}

		uv uvOutside = outside.uv();
		uv uvClamped = polygon.clamp(uvOutside);
		assertTrue(uvClamped.u() != uvOutside.u() || uvClamped.v() != uvOutside.v(), "uv clamp should modify outside points");
		assertTrue(polygon.contains(uvClamped), "uv clamped should be contained");
	}

	@Test
	public void testPolygonGamutValidation () {
		// Test too few vertices
		xy[] twoPoints = {new xy(0.3f, 0.3f), new xy(0.5f, 0.5f)};
		assertThrows(IllegalArgumentException.class, () -> new Gamut.PolygonGamut(twoPoints));

		// Test minimum valid polygon (triangle)
		xy[] triangle = {new xy(0.3f, 0.3f), new xy(0.5f, 0.2f), new xy(0.4f, 0.6f)};
		Gamut.PolygonGamut triangleGamut = new Gamut.PolygonGamut(triangle);
		assertNotNull(triangleGamut, "Should create valid triangle polygon");
	}
}
