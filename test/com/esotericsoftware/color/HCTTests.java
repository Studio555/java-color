
package com.esotericsoftware.color;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.CAM16;
import com.esotericsoftware.color.space.HCT;
import com.esotericsoftware.color.space.RGB;

public class HCTTests extends Tests {
	@Test
	public void testHCT () {
		// Test primary colors with HCT viewing conditions (Yb=50)
		// Red
		CAM16 redCam = new RGB(1, 0, 0).CAM16(CAM16.VC.HCT);
		assertEquals(46.454f, redCam.J(), 0.001, "Red J");
		assertEquals(113.356f, redCam.C(), 0.001, "Red chroma");
		assertEquals(27.409f, redCam.h(), 0.001, "Red hue");
		assertEquals(89.493f, redCam.M(), 0.001, "Red M");
		assertEquals(91.885f, redCam.s(), 0.001, "Red s");
		assertEquals(105.998f, redCam.Q(), 0.001, "Red Q");

		// Green
		CAM16 greenCam = new RGB(0, 1, 0).CAM16(CAM16.VC.HCT);
		assertEquals(79.328f, greenCam.J(), 0.001, "Green J");
		assertEquals(108.407f, greenCam.C(), 0.001, "Green chroma");
		assertEquals(142.144f, greenCam.h(), 0.001, "Green hue");
		assertEquals(85.585f, greenCam.M(), 0.001, "Green M");
		assertEquals(78.605f, greenCam.s(), 0.001, "Green s");
		assertEquals(138.517f, greenCam.Q(), 0.001, "Green Q");

		// Blue
		CAM16 blueCam = new RGB(0, 0, 1).CAM16(CAM16.VC.HCT);
		assertEquals(25.459f, blueCam.J(), 0.001, "Blue J");
		assertEquals(87.228f, blueCam.C(), 0.001, "Blue chroma");
		assertEquals(282.762f, blueCam.h(), 0.001, "Blue hue");
		assertEquals(68.865f, blueCam.M(), 0.001, "Blue M");
		assertEquals(93.679f, blueCam.s(), 0.001, "Blue s");
		assertEquals(78.472f, blueCam.Q(), 0.001, "Blue Q");

		// Black
		CAM16 blackCam = new RGB(0, 0, 0).CAM16(CAM16.VC.HCT);
		assertEquals(0, blackCam.J(), 0.001, "Black J");
		assertEquals(0, blackCam.C(), 0.001, "Black chroma");
		assertEquals(0, blackCam.h(), 0.001, "Black hue");
		assertEquals(0, blackCam.M(), 0.001, "Black M");
		assertEquals(0, blackCam.s(), 0.001, "Black s");
		assertEquals(0, blackCam.Q(), 0.001, "Black Q");

		// White
		CAM16 whiteCam = new RGB(1, 1, 1).CAM16(CAM16.VC.HCT);
		assertEquals(100, whiteCam.J(), 0.001, "White J");
		assertEquals(2.869f, whiteCam.C(), 0.001, "White chroma");
		assertEquals(209.492f, whiteCam.h(), 0.001, "White hue");
		assertEquals(2.265f, whiteCam.M(), 0.001, "White M");
		assertEquals(12.068f, whiteCam.s(), 0.001, "White s");
		assertEquals(155.521f, whiteCam.Q(), 0.001, "White Q");

		// Midgray
		RGB gray = new RGB(0x777777);
		CAM16 midgrayCam = gray.CAM16(CAM16.VC.HCT);
		assertClose(new RGB(0x77 / 255f, 0x77 / 255f, 0x77 / 255f), midgrayCam.RGB(CAM16.VC.HCT), "Midgray round trip");
		assertEquals(39.896f, midgrayCam.J(), 0.01, "Midgray J");
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
		checkMapping(27.409f, 113.356f, 46.454f, "Red mapping");
		checkMapping(142.144f, 108.407f, 79.328f, "Green mapping");
		checkMapping(282.762f, 87.228f, 25.459f, "Blue mapping");

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
					// The HCT algorithm can increase chroma when mapping to sRGB gamut
					// The increase is proportional to the requested chroma
					float chromaTolerance;
					if (chroma == 0) {
						chromaTolerance = 3.5f; // Small tolerance for grays
					} else {
						// Tolerance scales with requested chroma (about 30% increase is normal)
						chromaTolerance = Math.max(7.0f, chroma * 0.35f);
					}
					assertTrue(hctResult.C() <= chroma + chromaTolerance, desc + " chroma should be close or less");

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
		assertTrue(hctBack.C() <= chroma + 20, name + " chroma should not increase significantly");
	}
}
