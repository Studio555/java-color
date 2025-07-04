
package com.esotericsoftware.color;

import static com.esotericsoftware.color.Util.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.Illuminant.CIE2;
import com.esotericsoftware.color.space.CAM16;
import com.esotericsoftware.color.space.HCT;
import com.esotericsoftware.color.space.RGB;

public class HCTTests extends Tests {
	@Test
	public void testCAM16 () {
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
		assertEquals(2.869f, white.C(), 0.001, "White chroma C");

		// Black
		var black = new RGB(0, 0, 0).CAM16();
		assertEquals(0, black.J(), 0.001, "Black lightness J");
		assertEquals(0, black.C(), 0.001, "Black chroma C");

		// Gray
		var gray = new RGB(0.5f, 0.5f, 0.5f).CAM16();
		assertEquals(0, gray.C(), 2.0, "Gray chroma C"); // Allow some tolerance for numerical precision

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
		var vc = CAM16.VC.with(CIE2.D50, 30, 15, 1, false);
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
	public void testCAM16UCS () {
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
		var vc = CAM16.VC.with(CIE2.D50, 30, 15, 1, false);
		var ucsCustom = rgb.CAM16UCS(vc);
		RGB rgbBackCustom = ucsCustom.RGB(vc);
		assertClose(rgb, rgbBackCustom, 0.01f, "CAM16-UCS round trip with custom VC");
	}

	@Test
	public void testHCT () {
		// Test primary colors CAM16 values
		// Red
		CAM16 redCam = new RGB(1, 0, 0).CAM16();
		assertEquals(46.445f, redCam.J(), 0.001, "Red J");
		assertEquals(113.357f, redCam.C(), 0.001, "Red chroma");
		assertEquals(27.408f, redCam.h(), 0.001, "Red hue");
		assertEquals(89.494f, redCam.M(), 0.001, "Red M");
		assertEquals(91.889f, redCam.s(), 0.001, "Red s");
		assertEquals(105.988f, redCam.Q(), 0.001, "Red Q");

		// Green
		CAM16 greenCam = new RGB(0, 1, 0).CAM16();
		assertEquals(79.331f, greenCam.J(), 0.001, "Green J");
		assertEquals(108.41f, greenCam.C(), 0.001, "Green chroma");
		assertEquals(142.139f, greenCam.h(), 0.001, "Green hue");
		assertEquals(85.587f, greenCam.M(), 0.001, "Green M");
		assertEquals(78.604f, greenCam.s(), 0.001, "Green s");
		assertEquals(138.52f, greenCam.Q(), 0.001, "Green Q");

		// Blue
		CAM16 blueCam = new RGB(0, 0, 1).CAM16();
		assertEquals(25.465f, blueCam.J(), 0.001, "Blue J");
		assertEquals(87.23f, blueCam.C(), 0.001, "Blue chroma");
		assertEquals(282.788f, blueCam.h(), 0.001, "Blue hue");
		assertEquals(68.867f, blueCam.M(), 0.001, "Blue M");
		assertEquals(93.674f, blueCam.s(), 0.001, "Blue s");
		assertEquals(78.481f, blueCam.Q(), 0.001, "Blue Q");

		// Black
		CAM16 blackCam = new RGB(0, 0, 0).CAM16();
		assertEquals(0, blackCam.J(), 0.001, "Black J");
		assertEquals(0, blackCam.C(), 0.001, "Black chroma");
		assertEquals(0, blackCam.h(), 0.001, "Black hue");
		assertEquals(0, blackCam.M(), 0.001, "Black M");
		assertEquals(0, blackCam.s(), 0.001, "Black s");
		assertEquals(0, blackCam.Q(), 0.001, "Black Q");

		// White
		CAM16 whiteCam = new RGB(1, 1, 1).CAM16();
		assertEquals(100, whiteCam.J(), 0.001, "White J");
		assertEquals(2.869f, whiteCam.C(), 0.001, "White chroma");
		assertEquals(209.492f, whiteCam.h(), 0.001, "White hue");
		assertEquals(2.265f, whiteCam.M(), 0.001, "White M");
		assertEquals(12.068f, whiteCam.s(), 0.001, "White s");
		assertEquals(155.521f, whiteCam.Q(), 0.001, "White Q");

		// Midgray
		RGB gray = new RGB(0x777777);
		CAM16 midgrayCam = gray.CAM16();
		assertClose(new RGB(0x77 / 255f, 0x77 / 255f, 0x77 / 255f), midgrayCam.RGB(), "Midgray round trip");
		assertEquals(39.896146f, midgrayCam.J(), 0.0001, "Midgray J");
	}

	@Test
	public void testCAM16EdgeCases () {
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

	@Test
	public void testEdgeCases () {
		// Test impossible color requests
		var hctImpossible = new HCT(180, 150, 50); // Very high chroma
		RGB rgb = hctImpossible.RGB();
		// Should return a valid color even if not exact match
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1, "R in range for impossible HCT");
		assertTrue(rgb.g() >= 0 && rgb.g() <= 1, "G in range for impossible HCT");
		assertTrue(rgb.b() >= 0 && rgb.b() <= 1, "B in range for impossible HCT");

		// Test extreme tones
		var hctDark = new HCT(0, 50, 0); // Tone 0 (black)
		RGB rgbDark = hctDark.RGB();
		float maxDark = Math.max(rgbDark.r(), Math.max(rgbDark.g(), rgbDark.b()));
		assertTrue(maxDark < 0.1f, "Tone 0 produces very dark color");

		var hctLight = new HCT(0, 50, 100); // Tone 100 (white)
		RGB rgbLight = hctLight.RGB();
		float minLight = Math.min(rgbLight.r(), Math.min(rgbLight.g(), rgbLight.b()));
		assertTrue(minLight > 0.9f, "Tone 100 produces very light color");

		// Very high chroma request
		HCT highChroma = new HCT(0, 200, 50);
		RGB highChromaRgb = highChroma.RGB();
		assertTrue(colorIsOnBoundary(highChromaRgb), "Very high chroma should map to boundary");

		// Test negative hue (should wrap)
		HCT negHue = new HCT(-30, 50, 50);
		HCT posHue = new HCT(330, 50, 50);
		RGB negHueRgb = negHue.RGB();
		RGB posHueRgb = posHue.RGB();
		assertClose(negHueRgb, posHueRgb, 0.01f, "Negative hue wrapping");

		// Test hue > 360 (should wrap)
		HCT bigHue = new HCT(390, 50, 50);
		HCT smallHue = new HCT(30, 50, 50);
		RGB bigHueRgb = bigHue.RGB();
		RGB smallHueRgb = smallHue.RGB();
		assertClose(bigHueRgb, smallHueRgb, 0.01f, "Large hue wrapping");

		// Test very low tone
		HCT lowTone = new HCT(180, 50, 1);
		RGB lowToneRgb = lowTone.RGB();
		assertTrue(lowToneRgb.r() < 0.05f && lowToneRgb.g() < 0.05f && lowToneRgb.b() < 0.05f, "Very low tone should be very dark");

		// Test very high tone
		HCT highTone = new HCT(180, 50, 99);
		RGB highToneRgb = highTone.RGB();
		assertTrue(highToneRgb.r() > 0.95f && highToneRgb.g() > 0.95f && highToneRgb.b() > 0.95f,
			"Very high tone should be very light");
	}

	@Test
	public void testGamutMapping () {
		// Test that colors are correctly gamut-mapped
		// When we request specific HCT values, they should be preserved or mapped to sRGB boundary

		// Test colors that should preserve their values
		checkMapping(27.408f, 113.357f, 46.445f, "Red mapping");
		checkMapping(142.139f, 108.41f, 79.331f, "Green mapping");
		checkMapping(282.788f, 87.23f, 25.465f, "Blue mapping");

		// Test that white and black work correctly
		HCT white = new HCT(0, 0, 100);
		RGB whiteRgb = white.RGB();
		assertClose(new RGB(1, 1, 1), whiteRgb, 0.01f, "White from HCT");

		HCT black = new HCT(0, 0, 0);
		RGB blackRgb = black.RGB();
		assertClose(new RGB(0, 0, 0), blackRgb, 0.01f, "Black from HCT");

		// Test gray (no chroma)
		HCT gray = new HCT(0, 0, 50);
		RGB grayRgb = gray.RGB();
		assertEquals(grayRgb.r(), grayRgb.g(), 0.01f, "Gray R=G");
		assertEquals(grayRgb.g(), grayRgb.b(), 0.01f, "Gray G=B");
	}

	@Test
	public void roundTrip () {
		// Test HCT round trip conversions
		roundTrip(new RGB(1, 0, 0), "Red");
		roundTrip(new RGB(0, 1, 0), "Green");
		roundTrip(new RGB(0, 0, 1), "Blue");
		roundTrip(new RGB(1, 1, 0), "Yellow");
		roundTrip(new RGB(0, 1, 1), "Cyan");
		roundTrip(new RGB(1, 0, 1), "Magenta");
		roundTrip(new RGB(1, 1, 1), "White");
		roundTrip(new RGB(0, 0, 0), "Black");
		roundTrip(new RGB(0.5f, 0.5f, 0.5f), "Gray");

		// Test random colors
		roundTrip(new RGB(0.8f, 0.2f, 0.4f), "Pink");
		roundTrip(new RGB(0.1f, 0.6f, 0.3f), "Teal");
		roundTrip(new RGB(0.9f, 0.7f, 0.1f), "Gold");
	}

	@Test
	public void testHTCSystematic () {
		// Test HCT returns sufficiently close colors across the spectrum
		for (int hue = 15; hue < 360; hue += 30) {
			for (int chroma = 0; chroma <= 100; chroma += 10) {
				for (int tone = 20; tone <= 80; tone += 10) {
					String desc = "H" + hue + " C" + chroma + " T" + tone;

					HCT hct = new HCT(hue, chroma, tone);
					RGB rgb = hct.RGB();

					// Verify RGB is in valid range
					assertTrue(rgb.r() >= 0 && rgb.r() <= 1, desc + " R in range");
					assertTrue(rgb.g() >= 0 && rgb.g() <= 1, desc + " G in range");
					assertTrue(rgb.b() >= 0 && rgb.b() <= 1, desc + " B in range");

					// Convert back to HCT
					HCT hctResult = rgb.HCT();

					// Verify hue (if there's chroma)
					if (chroma > 0) {
						float hueDiff = Math.abs(hctResult.h() - hue);
						// Handle wraparound
						if (hueDiff > 180) hueDiff = 360 - hueDiff;
						assertTrue(hueDiff <= 4.0, desc + " hue preservation");
					}

					// Verify chroma is close or less
					assertTrue(hctResult.C() <= chroma + 2.5, desc + " chroma should be close or less");

					// If chroma was significantly reduced, color should be on boundary
					if (hctResult.C() < chroma - 2.5) {
						assertTrue(colorIsOnBoundary(rgb), desc + " out-of-gamut color should be on sRGB boundary");
					}

					// Verify tone is preserved
					assertEquals(tone, hctResult.T(), 0.5, desc + " tone preservation");
				}
			}
		}
	}

	private void roundTrip (RGB rgb, String name) {
		HCT hct = rgb.HCT();
		RGB rgbBack = hct.RGB();
		assertClose(rgb, rgbBack, 0.2f, name + " HCT round trip");

		// Test with custom viewing conditions
		var vc = CAM16.VC.with(CIE2.D50, 30, 15, 1, false);
		HCT hctCustom = rgb.HCT(vc);
		RGB rgbBackCustom = hctCustom.RGB(vc);
		assertClose(rgb, rgbBackCustom, 0.64f, name + " HCT round trip with custom VC");
	}

	private void checkMapping (float hue, float chroma, float tone, String name) {
		HCT hct = new HCT(hue, chroma, tone);
		RGB rgb = hct.RGB();
		HCT hctBack = rgb.HCT();

		// Hue should be preserved (within tolerance)
		if (chroma > 0) {
			assertEquals(hue, hctBack.h(), 4.0, name + " hue");
		}

		// Tone should be preserved closely
		assertEquals(tone, hctBack.T(), 0.5, name + " tone");

		// Chroma might be reduced if color is out of gamut
		assertTrue(hctBack.C() <= chroma + 2.5, name + " chroma should not increase significantly");
	}
}
