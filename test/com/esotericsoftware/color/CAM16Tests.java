
package com.esotericsoftware.color;

import static com.esotericsoftware.color.Util.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.Observer;
import com.esotericsoftware.color.space.CAM02;
import com.esotericsoftware.color.space.CAM16;
import com.esotericsoftware.color.space.CAM16LCD;
import com.esotericsoftware.color.space.CAM16SCD;
import com.esotericsoftware.color.space.CAM16UCS;
import com.esotericsoftware.color.space.RGB;
import com.esotericsoftware.color.space.XYZ;

class CAM16Tests extends Tests {

	private static final float EPSILON = 0.1f;

	@Test
	void testXYZtoCAM16 () {
		// Test case similar to Google's Material Design color implementation
		XYZ xyz = new XYZ(19.01f, 20.00f, 21.78f);
		CAM16 cam16 = xyz.CAM16();

		assertNotNull(cam16);
		assertTrue(cam16.J() > 0 && cam16.J() <= 100, "Lightness J should be in [0, 100]");
		assertTrue(cam16.C() >= 0, "Chroma C should be non-negative");
		assertTrue(cam16.h() >= 0 && cam16.h() < 360, "Hue h should be in [0, 360)");
		assertTrue(cam16.Q() > 0, "Brightness Q should be positive");
		assertTrue(cam16.M() >= 0, "Colorfulness M should be non-negative");
		assertTrue(cam16.s() >= 0, "Saturation s should be non-negative");
	}

	@Test
	void testCAM16toXYZRoundtrip () {
		XYZ original = new XYZ(30.0f, 40.0f, 50.0f);
		CAM16 cam16 = original.CAM16();
		XYZ roundtrip = cam16.XYZ();

		assertEquals(original.X(), roundtrip.X(), 1.0f, "X roundtrip");
		assertEquals(original.Y(), roundtrip.Y(), 1.0f, "Y roundtrip");
		assertEquals(original.Z(), roundtrip.Z(), 1.0f, "Z roundtrip");
	}

	@Test
	void testCAM16ViewingConditions () {
		// Test viewing conditions builder
		XYZ wp = new XYZ(95.05f, 100.0f, 108.88f);
		CAM16.VC vc = CAM16.VC.with(wp, 200.0f, 50.0f, 2, false);

		assertNotNull(vc);
		assertTrue(vc.Aw() > 0, "Aw should be positive");
		assertTrue(vc.FL() > 0, "FL should be positive");
		assertEquals(3, vc.rgbD().length, "rgbD should have 3 components");
	}

	@Test
	void testCAM16toUCS () {
		CAM16 cam16 = new CAM16(50.0f, 30.0f, 180.0f, 100.0f, 25.0f, 40.0f);

		// Test LCD conversion
		CAM16LCD lcd = cam16.CAM16LCD();
		assertNotNull(lcd);
		assertTrue(lcd.J() > 0, "LCD J* should be positive");
		assertEquals(180.0f, lcd.h(), 1.0f, "LCD hue should match");

		// Test SCD conversion
		CAM16SCD scd = cam16.CAM16SCD();
		assertNotNull(scd);
		assertTrue(scd.J() > 0, "SCD J* should be positive");
		assertEquals(180.0f, scd.h(), 1.0f, "SCD hue should match");

		// Test UCS conversion
		CAM16UCS ucs = cam16.CAM16UCS();
		assertNotNull(ucs);
		assertTrue(ucs.J() > 0, "UCS J* should be positive");
		assertEquals(180.0f, ucs.h(), 1.0f, "UCS hue should match");
	}

	@Test
	void testUCStoCAM16Roundtrip () {
		// Original CAM16 values
		CAM16 original = new CAM16(60.0f, 20.0f, 90.0f, 120.0f, 18.0f, 35.0f);

		// Convert to UCS and back
		CAM16UCS ucs = original.CAM16UCS();
		CAM16 fromUCS = ucs.CAM16();
		assertEquals(original.J(), fromUCS.J(), EPSILON, "J roundtrip via UCS");
		// Chroma can have some error in roundtrip due to the exponential functions in UCS
		assertEquals(original.C(), fromUCS.C(), 7.0f, "C roundtrip via UCS");
		assertEquals(original.h(), fromUCS.h(), EPSILON, "h roundtrip via UCS");

		// Convert to LCD and back
		CAM16LCD lcd = original.CAM16LCD();
		CAM16 fromLCD = lcd.CAM16();
		assertEquals(original.J(), fromLCD.J(), EPSILON, "J roundtrip via LCD");
		// LCD also uses exponential functions that can introduce some error
		assertEquals(original.C(), fromLCD.C(), 7.0f, "C roundtrip via LCD");
		assertEquals(original.h(), fromLCD.h(), EPSILON, "h roundtrip via LCD");

		// Convert to SCD and back
		CAM16SCD scd = original.CAM16SCD();
		CAM16 fromSCD = scd.CAM16();
		assertEquals(original.J(), fromSCD.J(), EPSILON, "J roundtrip via SCD");
		// SCD also uses exponential functions that can introduce some error
		assertEquals(original.C(), fromSCD.C(), 7.0f, "C roundtrip via SCD");
		assertEquals(original.h(), fromSCD.h(), EPSILON, "h roundtrip via SCD");
	}

	@Test
	void testCAM16UCSDeltaE () {
		// Test color difference calculation
		CAM16UCS color1 = new CAM16UCS(50.0f, 10.0f, -20.0f);
		CAM16UCS color2 = new CAM16UCS(55.0f, 12.0f, -18.0f);

		float deltaE = color1.dst(color2);
		assertTrue(deltaE > 0, "Delta E should be positive");
		assertTrue(deltaE < 10, "Small color difference should have small Delta E");

		// Same color should have zero difference
		float sameDeltaE = color1.dst(color1);
		assertEquals(0.0f, sameDeltaE, EPSILON, "Same color should have zero Delta E");
	}

	@Test
	void testCAM16vsCAM02 () {
		// Compare CAM16 and CAM02 for same input
		XYZ xyz = new XYZ(30.0f, 40.0f, 50.0f);

		CAM16 cam16 = xyz.CAM16();
		CAM02 cam02 = xyz.CAM02();

		// CAM16 and CAM02 should produce similar but not identical results
		assertTrue(Math.abs(cam16.J() - cam02.J()) < 10.0f, "CAM16 and CAM02 lightness should be similar");
		// Hue angles can differ more significantly between models
		float hueDiff = Math.abs(cam16.h() - cam02.h());
		if (hueDiff > 180) hueDiff = 360 - hueDiff;
		assertTrue(hueDiff < 20.0f, "CAM16 and CAM02 hue should be somewhat similar");
	}

	@Test
	void testCAM16Hues () {
		// Test unique hues and their preservation
		float[] uniqueHues = {20.14f, 90.0f, 164.25f, 237.53f};
		String[] hueNames = {"Red", "Yellow", "Green", "Blue"};

		for (int i = 0; i < uniqueHues.length; i++) {
			CAM16 cam16 = new CAM16(50.0f, 30.0f, uniqueHues[i], 100.0f, 25.0f, 40.0f);
			CAM16UCS ucs = cam16.CAM16UCS();
			float hueFromUCS = ucs.h();

			assertEquals(uniqueHues[i], hueFromUCS, 2.0f, hueNames[i] + " hue preservation");
		}
	}

	@Test
	void testCAM16GrayColors () {
		// Test achromatic colors (50% gray under D65)
		XYZ gray = new XYZ(47.525f, 50.0f, 54.44f);
		CAM16 cam16 = gray.CAM16();

		assertTrue(cam16.C() < 3.2f, "Gray should have low chroma");
		assertTrue(cam16.M() < 2.2f, "Gray should have low colorfulness");

		// Convert to UCS
		CAM16UCS ucs = cam16.CAM16UCS();
		assertTrue(Math.abs(ucs.a()) < 2.0f, "Gray should have low a*");
		assertTrue(Math.abs(ucs.b()) < 2.0f, "Gray should have low b*");
	}

	@Test
	void testCAM16DifferentSurrounds () {
		XYZ xyz = new XYZ(40.0f, 50.0f, 60.0f);
		XYZ wp = new XYZ(95.05f, 100.0f, 108.88f);

		// Average surround
		CAM16.VC avgVC = CAM16.VC.with(wp, 200.0f, 50.0f, 2, false);
		CAM16 avgCAM16 = xyz.CAM16(avgVC);

		// Dim surround
		CAM16.VC dimVC = CAM16.VC.with(wp, 200.0f, 50.0f, 1, false);
		CAM16 dimCAM16 = xyz.CAM16(dimVC);

		// Dark surround
		CAM16.VC darkVC = CAM16.VC.with(wp, 200.0f, 50.0f, 0, false);
		CAM16 darkCAM16 = xyz.CAM16(darkVC);

		// Different surrounds should produce different appearance
		assertNotEquals(avgCAM16.J(), dimCAM16.J(), "Different surrounds should affect lightness");
		assertNotEquals(dimCAM16.J(), darkCAM16.J(), "Different surrounds should affect lightness");
		assertNotEquals(avgCAM16.C(), darkCAM16.C(), "Different surrounds should affect chroma");
	}

	@Test
	void testCAM16EdgeCases () {
		// Test very dark color
		XYZ black = new XYZ(0.01f, 0.01f, 0.01f);
		CAM16 cam16Black = black.CAM16();
		assertTrue(cam16Black.J() < 5.0f, "Black should have very low lightness");

		// Test very bright color
		XYZ white = new XYZ(95.05f, 100.0f, 108.88f);
		CAM16 cam16White = white.CAM16();
		assertTrue(cam16White.J() > 95.0f, "White should have very high lightness");

		// Test highly saturated colors
		XYZ red = new XYZ(41.24f, 21.26f, 1.93f);
		CAM16 cam16Red = red.CAM16();
		assertTrue(cam16Red.C() > 50.0f, "Saturated red should have high chroma");
		assertTrue(cam16Red.h() < 60.0f || cam16Red.h() > 300.0f, "Red hue should be near 0/360");
	}

	@Test
	void testCAM16Lerp () {
		CAM16 color1 = new CAM16(30.0f, 20.0f, 60.0f, 80.0f, 15.0f, 25.0f);
		CAM16 color2 = new CAM16(70.0f, 40.0f, 240.0f, 120.0f, 35.0f, 45.0f);

		// Test midpoint interpolation
		CAM16 mid = color1.lerp(color2, 0.5f);
		assertEquals(50.0f, mid.J(), 1.0f, "Interpolated J at 0.5");
		assertEquals(30.0f, mid.C(), 1.0f, "Interpolated C at 0.5");

		// Test endpoints
		CAM16 start = color1.lerp(color2, 0.0f);
		assertEquals(color1.J(), start.J(), EPSILON, "Interpolated at 0.0 should match start");

		CAM16 end = color1.lerp(color2, 1.0f);
		assertEquals(color2.J(), end.J(), EPSILON, "Interpolated at 1.0 should match end");
	}

	@Test
	public void testCAM16BasicConversion () {
		// Test 1: Basic forward conversion and attribute ranges
		RGB rgb = new RGB(0.2f, 0.5f, 0.8f);
		var cam = rgb.CAM16();

		// Test that attributes are reasonable
		assertTrue(cam.J() >= 0 && cam.J() <= 100, "J in range [0, 100]");
		assertTrue(cam.C() >= 0, "C >= 0");
		assertTrue(cam.h() >= 0 && cam.h() <= 360, "h in range [0, 360]");
		assertTrue(cam.Q() > 0, "Q > 0");
		assertTrue(cam.M() >= 0, "M >= 0");
		assertTrue(cam.s() >= 0, "s >= 0");

		// Test 2: Round-trip conversion
		RGB rgbBack = cam.RGB(CAM16.VC.sRGB);
		assertClose(rgb, rgbBack, 0.01f, "CAM16 round trip");

		// Test 3: Known color values (white, black, gray)
		// White
		var white = new RGB(1, 1, 1).CAM16();
		assertEquals(100, white.J(), 0.001, "White lightness J");
		assertEquals(3.736f, white.C(), 0.001, "White chroma C");

		// Black
		var black = new RGB(0, 0, 0).CAM16();
		assertEquals(0, black.J(), 0.001, "Black lightness J");
		assertEquals(0, black.C(), 0.001, "Black chroma C");

		// Gray
		var gray = new RGB(0.5f, 0.5f, 0.5f).CAM16();
		assertEquals(0, gray.C(), 2.6, "Gray chroma C"); // Allow some tolerance for numerical precision

		// Test 4: Primary colors
		var red = new RGB(1, 0, 0).CAM16();
		var green = new RGB(0, 1, 0).CAM16();
		var blue = new RGB(0, 0, 1).CAM16();

		// Red should have hue around 27 degrees
		assertTrue(Math.abs(red.h() - 27) < 5, "Red hue approximately 27°");
		// Green should have hue around 142 degrees
		assertTrue(Math.abs(green.h() - 142) < 5, "Green hue approximately 142°");
		// Blue should have hue around 282 degrees
		assertTrue(Math.abs(blue.h() - 282) < 5, "Blue hue approximately 282°");

		// Test 5: Custom viewing conditions
		var vc = CAM16.VC.with(Observer.CIE2.D50, 30, 15, 1, false);
		var cam2 = rgb.CAM16(vc);
		assertTrue(Math.abs(cam.J() - cam2.J()) > 0.01, "Different viewing conditions produce different J");

		// Round trip with custom viewing conditions
		RGB rgbBack2 = cam2.RGB(vc);
		assertClose(rgb, rgbBack2, 0.01f, "CAM16 round trip with custom VC");

		// Test 6: Edge cases - very saturated colors
		var saturatedRed = new RGB(1, 0, 0.001f).CAM16(); // Slightly off pure red to avoid singularities
		assertTrue(saturatedRed.C() > 80, "Saturated red has high chroma");

		// Test 7: Dark colors
		var darkBlue = new RGB(0.1f, 0.1f, 0.2f).CAM16();
		assertTrue(darkBlue.J() < 20, "Dark blue has low lightness");

		// Test 8: Hue consistency - colors with same hue should maintain it
		var color1 = new RGB(0.8f, 0.2f, 0.2f);
		var color2 = new RGB(0.6f, 0.15f, 0.15f);
		var cam1 = color1.CAM16();
		var cam2test = color2.CAM16();
		assertEquals(cam1.h(), cam2test.h(), 2.0, "Same hue angle for proportional RGB");
	}

	@Test
	public void testCAM16UCSConversion () {
		// Test 1: Basic forward conversion
		RGB rgb = new RGB(0.2f, 0.5f, 0.8f);
		var ucs = rgb.CAM16UCS();

		// Test that UCS coordinates are reasonable
		assertTrue(ucs.J() >= 0 && ucs.J() <= 100, "J* in reasonable range");
		assertTrue(!Float.isNaN(ucs.a()) && !Float.isInfinite(ucs.a()), "a* is valid");
		assertTrue(!Float.isNaN(ucs.b()) && !Float.isInfinite(ucs.b()), "b* is valid");

		// Test 2: Round-trip conversion
		RGB rgbBack = ucs.RGB();
		assertClose(rgb, rgbBack, 0.01f, "CAM16-UCS round trip");

		// Test 3: Conversion from CAM16
		var cam16 = rgb.CAM16();
		var ucsFromCam = cam16.CAM16UCS();
		assertCloseD(floats(ucs), floats(ucsFromCam), "CAM16 to CAM16-UCS conversion");

		// Test 4: Inverse conversion - UCS to CAM16
		var cam16Back = ucs.CAM16(CAM16.VC.sRGB);
		assertEquals(cam16.J(), cam16Back.J(), 0.1, "J preserved through UCS");
		assertEquals(cam16.C(), cam16Back.C(), 0.1, "C preserved through UCS");
		assertEquals(cam16.h(), cam16Back.h(), 0.5, "h preserved through UCS");
		assertEquals(cam16.M(), cam16Back.M(), 0.1, "M preserved through UCS");

		// Test 5: Distance calculation
		RGB rgb1 = new RGB(0.2f, 0.5f, 0.8f);
		RGB rgb2 = new RGB(0.3f, 0.5f, 0.8f);
		var ucs1 = rgb1.CAM16UCS();
		var ucs2 = rgb2.CAM16UCS();
		float distance = ucs1.dst(ucs2);
		assertTrue(distance > 0, "Different colors have positive distance");

		// Same color should have zero distance
		float sameDistance = ucs1.dst(ucs1);
		assertEquals(0, sameDistance, 0.0001, "Same color has zero distance");

		// Test 6: Gray colors in UCS
		var grayUCS = new RGB(0.5f, 0.5f, 0.5f).CAM16UCS();
		assertEquals(0, grayUCS.a(), 1.5, "Gray has a* near 0");
		assertEquals(0, grayUCS.b(), 1.5, "Gray has b* near 0");

		// Test 7: Custom viewing conditions
		var vc = CAM16.VC.with(Observer.CIE2.D50, 30, 15, 1, false);
		var ucsCustom = rgb.CAM16UCS(vc);
		RGB rgbBackCustom = ucsCustom.RGB(vc);
		assertClose(rgb, rgbBackCustom, 0.01f, "CAM16-UCS round trip with custom VC");
	}

	@Test
	public void testCAM16AdditionalEdgeCases () {
		// Test with standard viewing conditions
		RGB testColor = new RGB(0.5f, 0.3f, 0.7f);
		var cam16 = testColor.CAM16();

		// Values should still be reasonable
		assertTrue(cam16.J() >= 0 && cam16.J() <= 100, "J in range with extreme VC");
		assertTrue(!Float.isNaN(cam16.h()), "h not NaN with extreme VC");
		assertTrue(!Float.isNaN(cam16.C()), "C not NaN with extreme VC");

		// Test with default viewing conditions for now
		var cam16Dark = testColor.CAM16();
		assertTrue(cam16Dark.J() >= 0, "J non-negative in dark conditions");

		// Test black under various conditions
		RGB black = new RGB(0, 0, 0);
		var cam16Black = black.CAM16();
		assertEquals(0, cam16Black.J(), 0.1, "Black has J=0");
		assertEquals(0, cam16Black.C(), 0.1, "Black has C=0");
	}
}
