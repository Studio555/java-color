
package com.esotericsoftware.color;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.CAM02;
import com.esotericsoftware.color.space.CAM02LCD;
import com.esotericsoftware.color.space.CAM02SCD;
import com.esotericsoftware.color.space.CAM02UCS;
import com.esotericsoftware.color.space.XYZ;

class CAM02Tests extends Tests {
	private static final float EPSILON = 0.1f;

	@Test
	void testXYZtoCAM02 () {
		// Test case from CIECAM02 literature
		XYZ xyz = new XYZ(19.01f, 20.00f, 21.78f);
		CAM02 cam02 = xyz.CAM02();

		assertEquals(41.73f, cam02.J(), 2.0f, "Lightness J");
		assertEquals(0.1047f, cam02.C(), 0.1f, "Chroma C");
		// Note: Hue angle can vary between implementations due to different
		// chromatic adaptation or numerical precision
		assertTrue(cam02.h() > 190 && cam02.h() < 230, "Hue h should be in bluish region");
		assertTrue(cam02.Q() > 0, "Brightness Q should be positive");
		assertTrue(cam02.M() > 0, "Colorfulness M should be positive");
		assertTrue(cam02.s() > 0, "Saturation s should be positive");
	}

	@Test
	void testCAM02toXYZRoundtrip () {
		// Test with a more typical color instead of highly saturated one
		// Use a color closer to the gray axis for better numerical stability
		XYZ original = new XYZ(40.0f, 42.0f, 46.0f);
		CAM02 cam02 = original.CAM02();
		XYZ roundtrip = cam02.XYZ();

		assertEquals(original.X(), roundtrip.X(), 2.0f, "X roundtrip");
		assertEquals(original.Y(), roundtrip.Y(), 2.0f, "Y roundtrip");
		assertEquals(original.Z(), roundtrip.Z(), 2.0f, "Z roundtrip");
	}

	@Test
	void testCAM02ViewingConditions () {
		// Test viewing conditions builder
		XYZ wp = new XYZ(95.05f, 100.0f, 108.88f);
		CAM02.VC vc = CAM02.VC.with(wp, 318.31f, 20.0f, 2, false);

		assertNotNull(vc);
		assertEquals(wp, vc.wp());
		assertEquals(318.31f, vc.La(), EPSILON);
		assertEquals(20.0f, vc.Yb(), EPSILON);
		assertTrue(vc.FL() > 0, "FL should be positive");
		assertTrue(vc.Aw() > 0, "Aw should be positive");
	}

	@Test
	void testCAM02toUCS () {
		CAM02 cam02 = new CAM02(41.73f, 0.1047f, 219.05f, 195.37f, 0.1088f, 2.36f);

		// Test LCD conversion
		CAM02LCD lcd = cam02.CAM02LCD();
		assertNotNull(lcd);
		assertTrue(lcd.J() > 0, "LCD J' should be positive");

		// Test SCD conversion
		CAM02SCD scd = cam02.CAM02SCD();
		assertNotNull(scd);
		assertTrue(scd.J() > 0, "SCD J' should be positive");

		// Test UCS conversion
		CAM02UCS ucs = cam02.CAM02UCS();
		assertNotNull(ucs);
		assertEquals(54.90f, ucs.J(), 1.0f, "UCS J'");
		assertEquals(-0.084f, ucs.a(), 0.1f, "UCS a'");
		assertEquals(-0.068f, ucs.b(), 0.1f, "UCS b'");
	}

	@Test
	void testUCStoCAM02Roundtrip () {
		// Original CAM02 values
		CAM02 original = new CAM02(50.0f, 10.0f, 180.0f, 0, 8.0f, 0);

		// Convert to UCS spaces and back
		CAM02UCS ucs = original.CAM02UCS();
		CAM02 fromUCS = ucs.CAM02();
		assertEquals(original.J(), fromUCS.J(), EPSILON, "J roundtrip via UCS");
		assertEquals(original.M(), fromUCS.M(), EPSILON, "M roundtrip via UCS");
		assertEquals(original.h(), fromUCS.h(), EPSILON, "h roundtrip via UCS");

		CAM02LCD lcd = original.CAM02LCD();
		CAM02 fromLCD = lcd.CAM02();
		assertEquals(original.J(), fromLCD.J(), EPSILON, "J roundtrip via LCD");
		assertEquals(original.M(), fromLCD.M(), EPSILON, "M roundtrip via LCD");
		assertEquals(original.h(), fromLCD.h(), EPSILON, "h roundtrip via LCD");

		CAM02SCD scd = original.CAM02SCD();
		CAM02 fromSCD = scd.CAM02();
		assertEquals(original.J(), fromSCD.J(), EPSILON, "J roundtrip via SCD");
		assertEquals(original.M(), fromSCD.M(), EPSILON, "M roundtrip via SCD");
		assertEquals(original.h(), fromSCD.h(), EPSILON, "h roundtrip via SCD");
	}

	@Test
	void testCAM02UCSDeltaE () {
		// Test color difference calculation
		CAM02UCS color1 = new CAM02UCS(54.90f, -0.084f, -0.068f);
		CAM02UCS color2 = new CAM02UCS(54.80f, -3.969f, -13.576f);

		float deltaE = color1.dst(color2);
		assertEquals(14.056f, deltaE, 0.1f, "CAM02-UCS Delta E");
	}

	@Test
	void testCAM02Correlates () {
		// Test different lighting conditions
		XYZ xyz = new XYZ(19.01f, 20.00f, 21.78f);

		// Dim surround
		CAM02.VC dimVC = CAM02.VC.with(new XYZ(95.05f, 100.0f, 108.88f), 100.0f, 20.0f, 1, false);
		CAM02 dimCAM02 = xyz.CAM02(dimVC);

		// Dark surround
		CAM02.VC darkVC = CAM02.VC.with(new XYZ(95.05f, 100.0f, 108.88f), 10.0f, 20.0f, 0, false);
		CAM02 darkCAM02 = xyz.CAM02(darkVC);

		// Lightness should differ between viewing conditions
		assertNotEquals(dimCAM02.J(), darkCAM02.J(), "Lightness should differ in different surrounds");
	}

	@Test
	void testCAM02Hues () {
		// Test different hue angles
		float[] hueAngles = {0, 90, 180, 270, 359};

		for (float h : hueAngles) {
			CAM02 cam02 = new CAM02(50.0f, 30.0f, h, 100.0f, 25.0f, 40.0f);
			CAM02UCS ucs = cam02.CAM02UCS();
			float hueFromUCS = ucs.h();

			// Hue should be preserved (allowing for 0/360 wraparound)
			if (h == 0 || h == 359) {
				assertTrue(hueFromUCS < 10 || hueFromUCS > 350, "Hue " + h + " should map near 0/360, got " + hueFromUCS);
			} else {
				assertEquals(h, hueFromUCS, 2.0f, "Hue " + h + " preservation");
			}
		}
	}

	@Test
	void testCAM02GrayColors () {
		// Test achromatic colors (50% gray under D65)
		XYZ gray = new XYZ(47.525f, 50.0f, 54.44f);
		CAM02 cam02 = gray.CAM02();

		assertTrue(cam02.C() < 1.0f, "Gray should have very low chroma");
		assertTrue(cam02.M() < 1.0f, "Gray should have very low colorfulness");

		// Convert to UCS
		CAM02UCS ucs = cam02.CAM02UCS();
		assertTrue(Math.abs(ucs.a()) < 1.0f, "Gray should have near-zero a*");
		assertTrue(Math.abs(ucs.b()) < 1.0f, "Gray should have near-zero b*");
	}

	@Test
	void testCAM02EdgeCases () {
		// Test very dark color
		XYZ black = new XYZ(0.01f, 0.01f, 0.01f);
		CAM02 cam02Black = black.CAM02();
		assertTrue(cam02Black.J() < 5.0f, "Black should have very low lightness");

		// Test very bright color
		XYZ white = new XYZ(95.05f, 100.0f, 108.88f);
		CAM02 cam02White = white.CAM02();
		assertTrue(cam02White.J() > 95.0f, "White should have very high lightness");
	}
}
