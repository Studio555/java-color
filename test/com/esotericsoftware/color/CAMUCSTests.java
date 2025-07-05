
package com.esotericsoftware.color;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.CAM02LCD;
import com.esotericsoftware.color.space.CAM02SCD;
import com.esotericsoftware.color.space.CAM02UCS;
import com.esotericsoftware.color.space.CAM16LCD;
import com.esotericsoftware.color.space.CAM16SCD;
import com.esotericsoftware.color.space.CAM16UCS;
import com.esotericsoftware.color.space.XYZ;

/** Tests for CAM02 and CAM16 Uniform Color Space (UCS) conversions and operations. */
class CAMUCSTests {

	private static final float EPSILON = 0.1f;

	// CAM02 UCS Tests

	@Test
	void testCAM02UCSOperations () {
		CAM02UCS color1 = new CAM02UCS(50.0f, 10.0f, -20.0f);
		CAM02UCS color2 = new CAM02UCS(60.0f, -5.0f, 15.0f);

		// Test distance calculation
		float dist = color1.dst(color2);
		assertTrue(dist > 0, "Distance should be positive");
		assertEquals(dist, color2.dst(color1), EPSILON, "Distance should be symmetric");

		// Test lerp
		CAM02UCS mid = color1.lerp(color2, 0.5f);
		assertEquals(55.0f, mid.J(), EPSILON, "Lerp J at 0.5");
		assertEquals(2.5f, mid.a(), EPSILON, "Lerp a at 0.5");
		assertEquals(-2.5f, mid.b(), EPSILON, "Lerp b at 0.5");

		// Test vector operations
		CAM02UCS sum = color1.add(10.0f, -5.0f, 5.0f);
		assertEquals(60.0f, sum.J(), EPSILON, "Add J");
		assertEquals(5.0f, sum.a(), EPSILON, "Add a");
		assertEquals(-15.0f, sum.b(), EPSILON, "Add b");
	}

	@Test
	void testCAM02LCDOperations () {
		CAM02LCD color1 = new CAM02LCD(40.0f, 15.0f, -10.0f);
		CAM02LCD color2 = new CAM02LCD(45.0f, 12.0f, -8.0f);

		// Test chroma and hue
		float C = color1.C();
		float h = color1.h();
		assertTrue(C > 0, "Chroma should be positive");
		assertTrue(h >= 0 && h < 360, "Hue should be in [0, 360)");

		// Test distance for small color difference
		float dist = color1.dst(color2);
		assertTrue(dist < 10.0f, "Small color difference should have small distance");

		// Test withJ
		CAM02LCD newJ = color1.withJ(50.0f);
		assertEquals(50.0f, newJ.J(), EPSILON, "withJ should update J");
		assertEquals(color1.a(), newJ.a(), EPSILON, "withJ should preserve a");
		assertEquals(color1.b(), newJ.b(), EPSILON, "withJ should preserve b");
	}

	@Test
	void testCAM02SCDOperations () {
		CAM02SCD color1 = new CAM02SCD(55.0f, -8.0f, 12.0f);
		CAM02SCD color2 = new CAM02SCD(58.0f, -6.0f, 10.0f);

		// Test length (magnitude)
		float len = color1.len();
		assertTrue(len > 0, "Length should be positive");
		assertEquals(len * len, color1.len2(), EPSILON, "len2 should be len squared");

		// Test indexed operations
		assertEquals(55.0f, color1.get(0), EPSILON, "get(0) should return J");
		assertEquals(-8.0f, color1.get(1), EPSILON, "get(1) should return a");
		assertEquals(12.0f, color1.get(2), EPSILON, "get(2) should return b");

		CAM02SCD updated = color1.set(0, 60.0f);
		assertEquals(60.0f, updated.J(), EPSILON, "set(0) should update J");

		// Test distance between color1 and color2
		float dist = color1.dst(color2);
		assertTrue(dist > 0, "Distance between different colors should be positive");
		assertEquals(dist, color2.dst(color1), EPSILON, "Distance should be symmetric");
	}

	// CAM16 UCS Tests

	@Test
	void testCAM16UCSOperations () {
		CAM16UCS color1 = new CAM16UCS(45.0f, 8.0f, -15.0f);
		CAM16UCS color2 = new CAM16UCS(50.0f, 6.0f, -12.0f);

		// Test distance calculation
		float dist = color1.dst(color2);
		assertTrue(dist > 0, "Distance should be positive");

		// Test component-wise operations
		CAM16UCS added = color1.add(1, 2.0f); // Add to a component
		assertEquals(45.0f, added.J(), EPSILON, "Add to index 1 should preserve J");
		assertEquals(10.0f, added.a(), EPSILON, "Add to index 1 should update a");
		assertEquals(-15.0f, added.b(), EPSILON, "Add to index 1 should preserve b");

		// Test subtraction
		CAM16UCS diff = color2.sub(color1.J(), color1.a(), color1.b());
		assertEquals(5.0f, diff.J(), EPSILON, "Difference J");
		assertEquals(-2.0f, diff.a(), EPSILON, "Difference a");
		assertEquals(3.0f, diff.b(), EPSILON, "Difference b");
	}

	@Test
	void testCAM16LCDOperations () {
		CAM16LCD color = new CAM16LCD(60.0f, -10.0f, 20.0f);

		// Test polar coordinates
		float C = color.C();
		float h = color.h();

		// Verify conversion back to rectangular
		float a_calc = C * (float)Math.cos(h * Math.PI / 180);
		float b_calc = C * (float)Math.sin(h * Math.PI / 180);
		assertEquals(color.a(), a_calc, 0.5f, "a from polar conversion");
		assertEquals(color.b(), b_calc, 0.5f, "b from polar conversion");

		// Test withJ
		CAM16LCD newJ = color.withJ(70.0f);
		assertEquals(70.0f, newJ.J(), EPSILON, "withJ should update J");
		assertEquals(color.C(), newJ.C(), EPSILON, "withJ should preserve chroma");
		assertEquals(color.h(), newJ.h(), 1.0f, "withJ should preserve hue");
	}

	@Test
	void testCAM16SCDOperations () {
		CAM16SCD color1 = new CAM16SCD(65.0f, 5.0f, -8.0f);
		CAM16SCD color2 = new CAM16SCD(62.0f, 4.0f, -7.0f);

		// Test interpolation at various points
		float[] ts = {0.0f, 0.25f, 0.5f, 0.75f, 1.0f};
		for (float t : ts) {
			CAM16SCD interp = color1.lerp(color2, t);
			float expectedJ = color1.J() + t * (color2.J() - color1.J());
			assertEquals(expectedJ, interp.J(), EPSILON, "Lerp J at t=" + t);
		}

		// Test vector magnitude
		float len1 = color1.len();
		float len2 = color2.len();
		assertTrue(len1 > len2, "color1 should have larger magnitude");
	}

	// Cross-model UCS comparison tests

	@Test
	void testCAM02vsCAM16UCSConsistency () {
		// Same XYZ should produce similar UCS values in CAM02 and CAM16
		XYZ xyz = new XYZ(40.0f, 50.0f, 60.0f);

		CAM02UCS cam02ucs = xyz.CAM02().CAM02UCS();
		CAM16UCS cam16ucs = xyz.CAM16().CAM16UCS();

		// J* values should be somewhat similar
		assertTrue(Math.abs(cam02ucs.J() - cam16ucs.J()) < 20.0f, "CAM02 and CAM16 UCS J* should be reasonably similar");

		// Hue angles should be somewhat similar
		float h02 = cam02ucs.h();
		float h16 = cam16ucs.h();
		float hueDiff = Math.abs(h02 - h16);
		if (hueDiff > 180) hueDiff = 360 - hueDiff;
		assertTrue(hueDiff < 20.0f, "Hue angles should be somewhat similar between CAM02 and CAM16");
	}

	@Test
	void testUCSDeltaEProperties () {
		// Test triangle inequality for CAM02UCS
		CAM02UCS a = new CAM02UCS(50.0f, 10.0f, -5.0f);
		CAM02UCS b = new CAM02UCS(55.0f, 8.0f, -3.0f);
		CAM02UCS c = new CAM02UCS(52.0f, 12.0f, -7.0f);

		float ab = a.dst(b);
		float bc = b.dst(c);
		float ac = a.dst(c);

		assertTrue(ab + bc >= ac - EPSILON, "Triangle inequality: AB + BC >= AC");
		assertTrue(ab + ac >= bc - EPSILON, "Triangle inequality: AB + AC >= BC");
		assertTrue(bc + ac >= ab - EPSILON, "Triangle inequality: BC + AC >= AB");

		// Test symmetry
		assertEquals(ab, b.dst(a), EPSILON, "Distance should be symmetric");

		// Test identity
		assertEquals(0.0f, a.dst(a), EPSILON, "Distance to self should be zero");
	}

	@Test
	void testUCSGrayAxis () {
		// Test that neutral colors have near-zero a* and b* in all UCS variants
		XYZ gray50 = new XYZ(47.525f, 50.0f, 54.44f); // 50% gray under D65

		// CAM02 UCS
		CAM02UCS cam02ucs = gray50.CAM02().CAM02UCS();
		assertTrue(Math.abs(cam02ucs.a()) < 2.0f, "Gray should have near-zero a* in CAM02UCS");
		assertTrue(Math.abs(cam02ucs.b()) < 2.0f, "Gray should have near-zero b* in CAM02UCS");

		// CAM16 UCS
		CAM16UCS cam16ucs = gray50.CAM16().CAM16UCS();
		assertTrue(Math.abs(cam16ucs.a()) < 2.0f, "Gray should have near-zero a* in CAM16UCS");
		assertTrue(Math.abs(cam16ucs.b()) < 2.0f, "Gray should have near-zero b* in CAM16UCS");

		// LCD variants
		CAM02LCD cam02lcd = gray50.CAM02().CAM02LCD();
		assertTrue(cam02lcd.C() < 2.0f, "Gray should have very low chroma in CAM02LCD");

		CAM16LCD cam16lcd = gray50.CAM16().CAM16LCD();
		assertTrue(cam16lcd.C() < 2.0f, "Gray should have very low chroma in CAM16LCD");
	}
}
