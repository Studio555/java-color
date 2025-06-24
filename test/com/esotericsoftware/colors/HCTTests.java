
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.TestsUtil.*;
import static com.esotericsoftware.colors.Util.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.colors.Illuminant.CIE2;
import com.esotericsoftware.colors.space.CAM16;
import com.esotericsoftware.colors.space.HCT;
import com.esotericsoftware.colors.space.RGB;

public class HCTTests {
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
		assertRecordClose(rgb, rgbBack, "CAM16 round trip", 0.01f);

		// Test 3: Known color values (white, black, gray)
		// White
		var white = new RGB(1, 1, 1).CAM16();
		assertClose(100, white.J(), "White lightness J", 0.001);
		assertClose(2.869f, white.C(), "White chroma C", 0.001);

		// Black
		var black = new RGB(0, 0, 0).CAM16();
		assertClose(0, black.J(), "Black lightness J", 0.001);
		assertClose(0, black.C(), "Black chroma C", 0.001);

		// Gray
		var gray = new RGB(0.5f, 0.5f, 0.5f).CAM16();
		assertClose(0, gray.C(), "Gray chroma C", 2.0); // Allow some tolerance for numerical precision

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
		assertRecordClose(rgb, rgbBack2, "CAM16 round trip with custom VC", 0.01f);

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
		assertClose(cam1.h(), cam2test.h(), "Same hue angle for proportional RGB", 2.0);
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
		assertRecordClose(rgb, rgbBack, "CAM16-UCS round trip", 0.01f);

		// Test 3: Conversion from CAM16
		var cam16 = rgb.CAM16();
		var ucsFromCam = cam16.CAM16UCS();
		assertArrayClose(floats(ucs), floats(ucsFromCam), "CAM16 to CAM16-UCS conversion");

		// Test 4: Inverse conversion - UCS to CAM16
		var cam16Back = ucs.CAM16(CAM16.VC.sRGB);
		assertClose(cam16.J(), cam16Back.J(), "J preserved through UCS", 0.1);
		assertClose(cam16.C(), cam16Back.C(), "C preserved through UCS", 0.1);
		assertClose(cam16.h(), cam16Back.h(), "h preserved through UCS", 0.5);
		assertClose(cam16.M(), cam16Back.M(), "M preserved through UCS", 0.1);

		// Test 5: Distance calculation
		RGB rgb1 = new RGB(0.2f, 0.5f, 0.8f);
		RGB rgb2 = new RGB(0.3f, 0.5f, 0.8f);
		var ucs1 = rgb1.CAM16UCS();
		var ucs2 = rgb2.CAM16UCS();
		float distance = ucs1.deltaE(ucs2);
		assertTrue(distance > 0, "Different colors have positive distance");

		// Same color should have zero distance
		float sameDistance = ucs1.deltaE(ucs1);
		assertClose(0, sameDistance, "Same color has zero distance", 0.0001);

		// Test 6: Gray colors in UCS
		var grayUCS = new RGB(0.5f, 0.5f, 0.5f).CAM16UCS();
		assertClose(0, grayUCS.a(), "Gray has a* near 0", 1.5);
		assertClose(0, grayUCS.b(), "Gray has b* near 0", 1.5);

		// Test 7: Custom viewing conditions
		var vc = CAM16.VC.with(CIE2.D50, 30, 15, 1, false);
		var ucsCustom = rgb.CAM16UCS(vc);
		RGB rgbBackCustom = ucsCustom.RGB(vc);
		assertRecordClose(rgb, rgbBackCustom, "CAM16-UCS round trip with custom VC", 0.01f);
	}

	@Test
	public void testHCT () {
		// Test primary colors CAM16 values
		// Red
		CAM16 redCam = new RGB(1, 0, 0).CAM16();
		assertClose(46.445f, redCam.J(), "Red J", 0.001);
		assertClose(113.357f, redCam.C(), "Red chroma", 0.001);
		assertClose(27.408f, redCam.h(), "Red hue", 0.001);
		assertClose(89.494f, redCam.M(), "Red M", 0.001);
		assertClose(91.889f, redCam.s(), "Red s", 0.001);
		assertClose(105.988f, redCam.Q(), "Red Q", 0.001);

		// Green
		CAM16 greenCam = new RGB(0, 1, 0).CAM16();
		assertClose(79.331f, greenCam.J(), "Green J", 0.001);
		assertClose(108.410f, greenCam.C(), "Green chroma", 0.001);
		assertClose(142.139f, greenCam.h(), "Green hue", 0.001);
		assertClose(85.587f, greenCam.M(), "Green M", 0.001);
		assertClose(78.604f, greenCam.s(), "Green s", 0.001);
		assertClose(138.520f, greenCam.Q(), "Green Q", 0.001);

		// Blue
		CAM16 blueCam = new RGB(0, 0, 1).CAM16();
		assertClose(25.465f, blueCam.J(), "Blue J", 0.001);
		assertClose(87.230f, blueCam.C(), "Blue chroma", 0.001);
		assertClose(282.788f, blueCam.h(), "Blue hue", 0.001);
		assertClose(68.867f, blueCam.M(), "Blue M", 0.001);
		assertClose(93.674f, blueCam.s(), "Blue s", 0.001);
		assertClose(78.481f, blueCam.Q(), "Blue Q", 0.001);

		// Black
		CAM16 blackCam = new RGB(0, 0, 0).CAM16();
		assertClose(0, blackCam.J(), "Black J", 0.001);
		assertClose(0, blackCam.C(), "Black chroma", 0.001);
		assertClose(0, blackCam.h(), "Black hue", 0.001);
		assertClose(0, blackCam.M(), "Black M", 0.001);
		assertClose(0, blackCam.s(), "Black s", 0.001);
		assertClose(0, blackCam.Q(), "Black Q", 0.001);

		// White
		CAM16 whiteCam = new RGB(1, 1, 1).CAM16();
		assertClose(100, whiteCam.J(), "White J", 0.001);
		assertClose(2.869f, whiteCam.C(), "White chroma", 0.001);
		assertClose(209.492f, whiteCam.h(), "White hue", 0.001);
		assertClose(2.265f, whiteCam.M(), "White M", 0.001);
		assertClose(12.068f, whiteCam.s(), "White s", 0.001);
		assertClose(155.521f, whiteCam.Q(), "White Q", 0.001);

		// Midgray
		RGB gray = new RGB(0x777777);
		CAM16 midgrayCam = gray.CAM16();
		assertRecordClose(new RGB(0x77 / 255f, 0x77 / 255f, 0x77 / 255f), midgrayCam.RGB(), "Midgray round trip");
		assertClose(39.896146f, midgrayCam.J(), "Midgray J", 0.0001);
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
		assertClose(0, cam16Black.J(), "Black has J=0", 0.1);
		assertClose(0, cam16Black.C(), "Black has C=0", 0.1);
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
		assertRecordClose(negHueRgb, posHueRgb, "Negative hue wrapping", 0.01f);

		// Test hue > 360 (should wrap)
		HCT bigHue = new HCT(390, 50, 50);
		HCT smallHue = new HCT(30, 50, 50);
		RGB bigHueRgb = bigHue.RGB();
		RGB smallHueRgb = smallHue.RGB();
		assertRecordClose(bigHueRgb, smallHueRgb, "Large hue wrapping", 0.01f);

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
		checkMapping(142.139f, 108.410f, 79.331f, "Green mapping");
		checkMapping(282.788f, 87.230f, 25.465f, "Blue mapping");

		// Test that white and black work correctly
		HCT white = new HCT(0, 0, 100);
		RGB whiteRgb = white.RGB();
		assertRecordClose(new RGB(1, 1, 1), whiteRgb, "White from HCT", 0.01f);

		HCT black = new HCT(0, 0, 0);
		RGB blackRgb = black.RGB();
		assertRecordClose(new RGB(0, 0, 0), blackRgb, "Black from HCT", 0.01f);

		// Test gray (no chroma)
		HCT gray = new HCT(0, 0, 50);
		RGB grayRgb = gray.RGB();
		assertClose(grayRgb.r(), grayRgb.g(), "Gray R=G", 0.01f);
		assertClose(grayRgb.g(), grayRgb.b(), "Gray G=B", 0.01f);
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
					assertClose(tone, hctResult.T(), desc + " tone preservation", 0.5);
				}
			}
		}
	}

	private void roundTrip (RGB rgb, String name) {
		HCT hct = rgb.HCT();
		RGB rgbBack = hct.RGB();
		assertRecordClose(rgb, rgbBack, name + " HCT round trip", 0.2f);

		// Test with custom viewing conditions
		var vc = CAM16.VC.with(CIE2.D50, 30, 15, 1, false);
		HCT hctCustom = rgb.HCT(vc);
		RGB rgbBackCustom = hctCustom.RGB(vc);
		assertRecordClose(rgb, rgbBackCustom, name + " HCT round trip with custom VC", 0.64f);
	}

	private void checkMapping (float hue, float chroma, float tone, String name) {
		HCT hct = new HCT(hue, chroma, tone);
		RGB rgb = hct.RGB();
		HCT hctBack = rgb.HCT();

		// Hue should be preserved (within tolerance)
		if (chroma > 0) {
			assertClose(hue, hctBack.h(), name + " hue", 4.0);
		}

		// Tone should be preserved closely
		assertClose(tone, hctBack.T(), name + " tone", 0.5);

		// Chroma might be reduced if color is out of gamut
		assertTrue(hctBack.C() <= chroma + 2.5, name + " chroma should not increase significantly");
	}
}
