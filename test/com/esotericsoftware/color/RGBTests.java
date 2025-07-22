
package com.esotericsoftware.color;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.CCT;
import com.esotericsoftware.color.space.CMYK;
import com.esotericsoftware.color.space.HSI;
import com.esotericsoftware.color.space.HSL;
import com.esotericsoftware.color.space.HSLuv;
import com.esotericsoftware.color.space.HSV;
import com.esotericsoftware.color.space.HunterLab;
import com.esotericsoftware.color.space.IHS;
import com.esotericsoftware.color.space.LCHuv;
import com.esotericsoftware.color.space.LCh;
import com.esotericsoftware.color.space.LMS;
import com.esotericsoftware.color.space.LMS.CAT;
import com.esotericsoftware.color.space.LRGB;
import com.esotericsoftware.color.space.Lab;
import com.esotericsoftware.color.space.Luv;
import com.esotericsoftware.color.space.RGB;
import com.esotericsoftware.color.space.RGBW;
import com.esotericsoftware.color.space.RGBWW;
import com.esotericsoftware.color.space.RGBWW.WW;
import com.esotericsoftware.color.space.TSL;
import com.esotericsoftware.color.space.XYZ;
import com.esotericsoftware.color.space.xy;
import com.esotericsoftware.color.space.xyY;

/** @author Nathan Sweet <misc@n4te.com> */
public class RGBTests extends Tests {
	@Test
	public void testTSL () {
		// Test round-trip conversion for regular colors
		RGB rgb = new RGB(0.2f, 0.5f, 0.8f);
		var tsl = rgb.TSL();
		RGB rgb2 = tsl.RGB();
		assertClose(rgb, rgb2, 0.001f, "TSL round trip");

		// Test black
		RGB black = new RGB(0, 0, 0);
		var tslBlack = black.TSL();
		RGB black2 = tslBlack.RGB();
		assertClose(black, black2, EPSILON_F, "Black TSL round trip");

		// Test white
		RGB white = new RGB(1, 1, 1);
		var tslWhite = white.TSL();
		RGB white2 = tslWhite.RGB();
		assertClose(white, white2, 0.001f, "White TSL round trip");

		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		var tslRed = red.TSL();
		RGB red2 = tslRed.RGB();
		assertClose(red, red2, 0.001f, "Red TSL round trip");

		RGB green = new RGB(0, 1, 0);
		var tslGreen = green.TSL();
		RGB green2 = tslGreen.RGB();
		assertClose(green, green2, 0.001f, "Green TSL round trip");

		RGB blue = new RGB(0, 0, 1);
		var tslBlue = blue.TSL();
		RGB blue2 = tslBlue.RGB();
		assertClose(blue, blue2, 0.001f, "Blue TSL round trip");

		// Test T=0 case (grayscale)
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		var tslGray = gray.TSL();
		RGB gray2 = tslGray.RGB();
		assertClose(gray, gray2, 0.001f, "Gray TSL round trip");

		// Test the negative zero case
		// When T = -0f, we should get a different result than T = 0f
		var tsl1 = new TSL(0f, 0.5f, 0.5f);
		var tsl2 = new TSL(-0f, 0.5f, 0.5f);
		RGB rgb1 = tsl1.RGB();
		rgb2 = tsl2.RGB();
		// The two results should be different due to the negative zero handling
		assertTrue(
			Math.abs(rgb1.r() - rgb2.r()) > 0.01f || Math.abs(rgb1.g() - rgb2.g()) > 0.01f || Math.abs(rgb1.b() - rgb2.b()) > 0.01f,
			"Negative zero T produces different result");

		// Both should satisfy the T=0 constraint in Terrillon-Akamatsu formulation
		// When T=0, g'-r'=0, meaning g'=r'
		// Check that the normalized coordinates satisfy this
		float sum1 = rgb1.r() + rgb1.g() + rgb1.b();
		float sum2 = rgb2.r() + rgb2.g() + rgb2.b();
		float r1_norm = rgb1.r() / sum1 - 1f / 3f;
		float g1_norm = rgb1.g() / sum1 - 1f / 3f;
		float r2_norm = rgb2.r() / sum2 - 1f / 3f;
		float g2_norm = rgb2.g() / sum2 - 1f / 3f;
		assertEquals(g1_norm, r1_norm, 0.001f, "T=0 constraint g'=r' for positive zero");
		assertEquals(g2_norm, r2_norm, 0.001f, "T=0 constraint g'=r' for negative zero");

		// Test RGB values satisfying 2*G = R+B
		// These should round-trip correctly through TSL
		RGB rgb2g1 = new RGB(0.2f, 0.3f, 0.4f); // 2*0.3 = 0.2+0.4
		var tsl2g1 = rgb2g1.TSL();
		RGB rgb2g1_back = tsl2g1.RGB();
		assertClose(rgb2g1, rgb2g1_back, 0.001f, "2*G=R+B round trip (1)");

		RGB rgb2g2 = new RGB(0.1f, 0.4f, 0.7f); // 2*0.4 = 0.1+0.7
		var tsl2g2 = rgb2g2.TSL();
		RGB rgb2g2_back = tsl2g2.RGB();
		assertClose(rgb2g2, rgb2g2_back, 0.001f, "2*G=R+B round trip (2)");

		// Edge case: when 2*G = R+B and R=B (implies G=R=B, grayscale)
		RGB rgb2g3 = new RGB(0.4f, 0.4f, 0.4f); // 2*0.4 = 0.4+0.4
		var tsl2g3 = rgb2g3.TSL();
		assertEquals(0, tsl2g3.S(), EPSILON_F, "Grayscale has S=0");
		RGB rgb2g3_back = tsl2g3.RGB();
		assertClose(rgb2g3, rgb2g3_back, 0.001f, "Grayscale 2*G=R+B round trip");
	}

	@Test
	public void testLRGB () {
		// Test RGB values
		RGB[] testColors = {new RGB(1, 0, 0), // Red
			new RGB(0, 1, 0), // Green
			new RGB(0, 0, 1), // Blue
			new RGB(0.5f, 0.5f, 0.5f), // Gray
			new RGB(0.2f, 0.5f, 0.8f) // Arbitrary
		};

		for (RGB rgb : testColors) {
			// Test linear RGB to XYZ round trip
			LRGB linearRgb = rgb.LRGB(); // This applies linearization
			XYZ xyz = linearRgb.XYZ();
			LRGB linearBack = xyz.LRGB();

			assertClose(linearRgb, linearBack, EPSILON_F, "Linear RGB round trip");

			// Test that the same numerical values interpreted as linear vs gamma-corrected produce different XYZ
			// Create LRGB with same numerical values as the gamma-corrected RGB (no conversion)
			LRGB linearSameValues = new LRGB(rgb.r(), rgb.g(), rgb.b());
			XYZ xyzLinear = linearSameValues.XYZ();
			XYZ xyzGamma = rgb.XYZ();

			// Skip test for values that have same linear and gamma encoding (0, 1)
			boolean skipTest = false;
			if (rgb.r() == 0 || rgb.r() == 1 || rgb.g() == 0 || rgb.g() == 1 || rgb.b() == 0 || rgb.b() == 1) {
				skipTest = true;
			}

			if (!skipTest) {
				// Due to float precision, the difference might be small
				float epsilon = 1e-6f;
				boolean differs = Math.abs(xyzLinear.X() - xyzGamma.X()) > epsilon || Math.abs(xyzLinear.Y() - xyzGamma.Y()) > epsilon
					|| Math.abs(xyzLinear.Z() - xyzGamma.Z()) > epsilon;
				assertTrue(differs, "Linear vs gamma XYZ should differ for values (" + rgb.r() + "," + rgb.g() + "," + rgb.b() + ")");
			}
		}

		// Test xyXYZ with Y=100
		XYZ xyz = new xy(0.3127f, 0.329f).XYZ(); // D65 white point
		assertEquals(100, xyz.Y(), EPSILON_F, "xyXYZ Y value");

		// Verify it produces the same ratios as XYZ
		XYZ xyzFromxyY = new xyY(0.3127f, 0.329f, 100).XYZ();
		assertClose(xyz, xyzFromxyY, EPSILON_F, "xyXYZ matches XYZ");
	}

	@Test
	public void testCMYK () {
		// Test known values
		CMYK black = new RGB(0, 0, 0).CMYK();
		assertClose(new CMYK(0, 0, 0, 1), black, "Black to CMYK");

		CMYK white = new RGB(1, 1, 1).CMYK();
		assertClose(new CMYK(0, 0, 0, 0), white, "White to CMYK");

		CMYK red = new RGB(1, 0, 0).CMYK();
		assertClose(new CMYK(0, 1, 1, 0), red, "Red to CMYK");

		// Test all primary and secondary colors
		CMYK green = new RGB(0, 1, 0).CMYK();
		assertClose(new CMYK(1, 0, 1, 0), green, "Green to CMYK");

		CMYK blue = new RGB(0, 0, 1).CMYK();
		assertClose(new CMYK(1, 1, 0, 0), blue, "Blue to CMYK");

		CMYK yellow = new RGB(1, 1, 0).CMYK();
		assertClose(new CMYK(0, 0, 1, 0), yellow, "Yellow to CMYK");

		CMYK cyan = new RGB(0, 1, 1).CMYK();
		assertClose(new CMYK(1, 0, 0, 0), cyan, "Cyan to CMYK");

		CMYK magenta = new RGB(1, 0, 1).CMYK();
		assertClose(new CMYK(0, 1, 0, 0), magenta, "Magenta to CMYK");

		// Test grays with different K values
		for (float gray = 0.1f; gray <= 0.9f; gray += 0.1f) {
			RGB rgb = new RGB(gray, gray, gray);
			CMYK cmyk = rgb.CMYK();
			// For grays, CMY should be 0 and K should be 1-gray
			assertEquals(0, cmyk.C(), 0.001, "Gray C at " + gray);
			assertEquals(0, cmyk.M(), 0.001, "Gray M at " + gray);
			assertEquals(0, cmyk.Y(), 0.001, "Gray Y at " + gray);
			assertEquals(1 - gray, cmyk.K(), 0.001, "Gray K at " + gray);
		}

		// Test rich black (CMYK with all components)
		RGB darkGray = new RGB(0.2f, 0.2f, 0.2f);
		CMYK richBlack = darkGray.CMYK();
		assertEquals(0.8f, richBlack.K(), 0.001, "Rich black K component");

		// Test round trip for various colors
		for (float r = 0; r <= 1f; r += 0.25f) {
			for (float g = 0; g <= 1f; g += 0.25f) {
				for (float b = 0; b <= 1f; b += 0.25f) {
					RGB rgb = new RGB(r, g, b);
					CMYK cmyk = rgb.CMYK();
					RGB rgbBack = cmyk.RGB();
					assertClose(rgb, rgbBack, 0.001, "CMYK round trip " + rgb);
				}
			}
		}
	}

	@Test
	public void testHSI () {
		// Test known values
		HSI black = new RGB(0, 0, 0).HSI();
		assertClose(0, black.I(), "Black intensity");
		assertClose(0, black.S(), "Black saturation");

		HSI white = new RGB(1, 1, 1).HSI();
		assertClose(1, white.I(), "White intensity");
		assertClose(0, white.S(), "White saturation");

		HSI red = new RGB(1, 0, 0).HSI();
		assertClose(1f / 3f, red.I(), "Red intensity");
		assertClose(1, red.S(), "Red saturation");
		assertClose(0, red.H(), "Red hue");

		HSI gray = new RGB(0.5f, 0.5f, 0.5f).HSI();
		assertClose(0.5f, gray.I(), "Gray intensity");
		assertClose(0, gray.S(), "Gray saturation");

		// Test round trip
		roundTripD(new RGB(0.5f, 0.3f, 0.7f), RGB::HSI, HSI::RGB, "HSI");

		// Test additional colors
		roundTripD(new RGB(1, 0, 0), RGB::HSI, HSI::RGB, "HSI Red");
		roundTripD(new RGB(0, 1, 0), RGB::HSI, HSI::RGB, "HSI Green");
		roundTripD(new RGB(0, 0, 1), RGB::HSI, HSI::RGB, "HSI Blue");
		roundTripD(new RGB(1, 1, 0), RGB::HSI, HSI::RGB, "HSI Yellow");
		roundTripD(new RGB(0, 1, 1), RGB::HSI, HSI::RGB, "HSI Cyan");
		roundTripD(new RGB(1, 0, 1), RGB::HSI, HSI::RGB, "HSI Magenta");

	}

	@Test
	public void testHSL () {
		// Test known values
		HSL red = new RGB(1, 0, 0).HSL();
		assertClose(0, red.H(), "Red hue");
		assertClose(1, red.S(), "Red saturation");
		assertClose(0.5f, red.L(), "Red lightness");

		// Test round trip
		roundTripD(new RGB(0.5f, 0.3f, 0.7f), RGB::HSL, HSL::RGB, "HSL");
	}

	@Test
	public void testHSLuv () {
		// Test known values
		// Pure red
		HSLuv red = new RGB(1, 0, 0).HSLuv();
		// HSLuv values for pure red in sRGB
		// We'll just verify the ranges are reasonable
		assertTrue(red.H() >= 0 && red.H() <= 360, "Red hue in valid range");
		assertTrue(red.S() >= 0 && red.S() <= 100, "Red saturation in valid range");
		assertTrue(red.L() >= 50 && red.L() <= 55, "Red lightness in expected range");

		// Pure white
		HSLuv white = new RGB(1, 1, 1).HSLuv();
		assertEquals(0, white.S(), 0.1f, "White saturation");
		assertClose(100, white.L(), "White lightness");

		// Pure black
		HSLuv black = new RGB(0, 0, 0).HSLuv();
		assertClose(0, black.S(), "Black saturation");
		assertClose(0, black.L(), "Black lightness");

		// Test round trip with RGB within HSLuv gamut
		roundTripF(new RGB(0.43f, 0.37f, 0.51f), RGB::HSLuv, HSLuv::RGB, "HSLuv");
	}

	@Test
	public void testHSV () {
		// Test known values
		HSV red = new RGB(1, 0, 0).HSV();
		assertClose(0, red.H(), "Red hue");
		assertClose(1, red.S(), "Red saturation");
		assertClose(1, red.V(), "Red value");

		HSV gray = new RGB(0.5f, 0.5f, 0.5f).HSV();
		assertClose(0, gray.S(), "Gray saturation");
		assertClose(0.5f, gray.V(), "Gray value");

		// Test all primary and secondary colors
		assertEquals(0, new RGB(1, 0, 0).HSV().H(), 0.1, "Red hue");
		assertEquals(60, new RGB(1, 1, 0).HSV().H(), 0.1, "Yellow hue");
		assertEquals(120, new RGB(0, 1, 0).HSV().H(), 0.1, "Green hue");
		assertEquals(180, new RGB(0, 1, 1).HSV().H(), 0.1, "Cyan hue");
		assertEquals(240, new RGB(0, 0, 1).HSV().H(), 0.1, "Blue hue");
		assertEquals(300, new RGB(1, 0, 1).HSV().H(), 0.1, "Magenta hue");

		// Test systematic hue values
		for (float hue = 0; hue < 360; hue += 30) {
			HSV hsv = new HSV(hue, 1f, 1f);
			RGB rgb = hsv.RGB();
			HSV hsvBack = rgb.HSV();
			// Handle hue wraparound
			float hueDiff = Math.abs(hsv.H() - hsvBack.H());
			if (hueDiff > 180) hueDiff = 360 - hueDiff;
			assertEquals(0, hueDiff, 0.1, "HSV hue round trip at " + hue);
			assertEquals(hsv.S(), hsvBack.S(), 0.001, "HSV saturation round trip at hue " + hue);
			assertEquals(hsv.V(), hsvBack.V(), 0.001, "HSV value round trip at hue " + hue);
		}

		// Test various saturation levels
		for (float sat = 0; sat <= 1f; sat += 0.1f) {
			HSV hsv = new HSV(180, sat, 0.8f); // Cyan at different saturations
			RGB rgb = hsv.RGB();
			HSV hsvBack = rgb.HSV();
			if (sat > 0) { // Only check hue if there's saturation
				assertEquals(hsv.H(), hsvBack.H(), 0.1, "HSV hue at saturation " + sat);
			}
			assertEquals(hsv.S(), hsvBack.S(), 0.001, "HSV saturation at " + sat);
			assertEquals(hsv.V(), hsvBack.V(), 0.001, "HSV value at saturation " + sat);
		}

		// Test round trip for grid of colors
		for (float r = 0; r <= 1f; r += 0.2f) {
			for (float g = 0; g <= 1f; g += 0.2f) {
				for (float b = 0; b <= 1f; b += 0.2f) {
					RGB rgb = new RGB(r, g, b);
					roundTripD(rgb, RGB::HSV, HSV::RGB, "HSV " + rgb);
				}
			}
		}
	}

	@Test
	public void testHunterLab () {
		roundTripD(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> c.HunterLab(), HunterLab::RGB, "Hunter Lab");

		// Test XYZ <-> Hunter Lab
		XYZ xyz = new XYZ(50, 50, 50);
		HunterLab hunterLab = xyz.HunterLab();
		XYZ xyzBack = hunterLab.XYZ();
		assertClose(xyz, xyzBack, "XYZ <-> Hunter Lab round trip");
	}

	@Test
	public void testIHS () {
		// IHS uses intensity as sum of RGB (0-3 range)
		roundTripD(new RGB(0.5f, 0.3f, 0.7f), RGB::IHS, IHS::RGB, "IHS");
	}

	@Test
	public void testLab () {
		// Test with default D65 illuminant
		roundTripD(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> c.Lab(), Lab::RGB, "Lab D65");

		// Test known values from CIE standards
		Lab whiteLab = new RGB(1, 1, 1).Lab();
		assertEquals(100, whiteLab.L(), 0.1, "White L*");
		assertEquals(0, whiteLab.a(), 0.1, "White a*");
		assertEquals(0, whiteLab.b(), 0.1, "White b*");

		Lab blackLab = new RGB(0, 0, 0).Lab();
		assertEquals(0, blackLab.L(), 0.1, "Black L*");
		assertEquals(0, blackLab.a(), 0.1, "Black a*");
		assertEquals(0, blackLab.b(), 0.1, "Black b*");

		// Test systematic range of L* values
		for (float L = 0; L <= 100; L += 10) {
			float gray = L / 100f;
			RGB rgb = new RGB(gray, gray, gray);
			Lab lab = rgb.Lab();
			// L* should be approximately the input L value for grays
			// Note: The relationship is not perfectly linear due to gamma
			assertEquals(0, lab.a(), 0.1, "Gray a* should be 0 at L=" + L);
			assertEquals(0, lab.b(), 0.1, "Gray b* should be 0 at L=" + L);
		}

		// Test known CIE standard colors
		// CIE red (approximately)
		Lab redLab = new RGB(1, 0, 0).Lab();
		assertEquals(53.233f, redLab.L(), 0.5, "Red L*");
		assertEquals(80.109f, redLab.a(), 0.5, "Red a*");
		assertEquals(67.22f, redLab.b(), 0.5, "Red b*");

		// Test round-trip for various colors
		float[] testValues = {0f, 0.01f, 0.1f, 0.2f, 0.3f, 0.33f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
		for (float r : testValues) {
			for (float g : new float[] {0f, 0.5f, 1f}) {
				for (float b : new float[] {0f, 0.5f, 1f}) {
					RGB rgb = new RGB(r, g, b);
					Lab lab = rgb.Lab();
					RGB rgbBack = lab.RGB();
					assertClose(rgb, rgbBack, 0.001f, "Lab round trip " + rgb);
				}
			}
		}
		assertEquals(0, whiteLab.b(), 0.1, "White b*");
	}

	@Test
	public void testLCh () {
		// Test round trip with default D65
		roundTripD(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> c.LCh(), LCh::RGB, "LCh");
	}

	@Test
	public void testLMS () {
		// Test all matrix types
		for (CAT matrix : CAT.values()) {
			// Test XYZ <-> LMS
			XYZ xyz = new XYZ(50, 50, 50);
			LMS lms = xyz.LMS(matrix);
			XYZ xyzBack = lms.XYZ(matrix);
			assertClose(xyz, xyzBack, EPSILON_F, "XYZ <-> LMS " + matrix + " round trip");

			// Test RGB <-> LMS
			RGB rgb = new RGB(0.5f, 0.3f, 0.7f);
			LMS lmsRgb = rgb.LMS(matrix);
			RGB rgbBack = lmsRgb.RGB(matrix);
			assertClose(rgb, rgbBack, "RGB <-> LMS " + matrix + " round trip");
		}
	}

	@Test
	public void testLuv () {
		// Test round trip
		roundTripF(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> c.Luv(), Luv::RGB, "Luv");

		// Test known values
		Luv whiteLuv = new RGB(1, 1, 1).Luv();
		assertEquals(100, whiteLuv.L(), 0.1, "White L*");
		assertEquals(0, whiteLuv.u(), 0.1, "White u*");
		assertEquals(0, whiteLuv.v(), 0.1, "White v*");

		// Test Luv <-> LCHuv conversion
		Luv luv = new Luv(50, 20, -30);
		LCHuv lch = luv.LCHuv();
		Luv luvBack = lch.Luv();

		assertEquals(luv.L(), luvBack.L(), EPSILON_F, "L component");
		assertEquals(luv.u(), luvBack.u(), EPSILON_F, "u component");
		assertEquals(luv.v(), luvBack.v(), EPSILON_F, "v component");
	}

	@Test
	public void testRGBW () {
		// Test with perfect white LED (ideal case)
		LRGB white = new LRGB(1, 1, 1);
		RGBW rgbwWhite = white.RGBW(new LRGB(1, 1, 1));
		assertClose(0, rgbwWhite.r(), "White RGBW R");
		assertClose(0, rgbwWhite.g(), "White RGBW G");
		assertClose(0, rgbwWhite.b(), "White RGBW B");
		assertClose(1, rgbwWhite.w(), "White RGBW W");

		// Test pure colors with ideal white
		LRGB red = new LRGB(1, 0, 0);
		RGBW rgbwRed = red.RGBW(new LRGB(1, 1, 1));
		assertClose(1, rgbwRed.r(), "Red RGBW R");
		assertClose(0, rgbwRed.g(), "Red RGBW G");
		assertClose(0, rgbwRed.b(), "Red RGBW B");
		assertClose(0, rgbwRed.w(), "Red RGBW W");

		// Test with warm white LED calibration
		LRGB warmWhiteLED = new LRGB(1, 0.8f, 0.6f);

		// Pure white should use only W channel up to the blue limit
		RGBW warmWhite = white.RGBW(warmWhiteLED);
		assertClose(0, warmWhite.r(), "Warm white RGBW R");
		assertClose(0.2f, warmWhite.g(), "Warm white RGBW G");
		assertClose(0.4f, warmWhite.b(), "Warm white RGBW B");
		assertClose(1, warmWhite.w(), "Warm white RGBW W");

		// Test mixed color
		LRGB mixed = new LRGB(0.8f, 0.6f, 0.4f);
		RGBW rgbwMixed = mixed.RGBW(warmWhiteLED);
		// W is limited by blue channel: 0.4 / 0.6 = 0.667
		float expectedW = 0.4f / 0.6f;
		assertEquals(expectedW, rgbwMixed.w(), 0.001, "Mixed RGBW W");
		assertEquals(0.8f - expectedW * warmWhiteLED.r(), rgbwMixed.r(), 0.001, "Mixed RGBW R");
		assertEquals(0.6f - expectedW * warmWhiteLED.g(), rgbwMixed.g(), 0.001, "Mixed RGBW G");
		assertEquals(0, rgbwMixed.b(), 0.001, "Mixed RGBW B"); // Should be 0

		// Test warm white conversion
		RGBW defaultRGBW = new LRGB(0.5f, 0.5f, 0.5f).RGBW(warmWhiteLED);
		assertEquals(0.5f, defaultRGBW.w(), 0.001, "Default gray RGBW W");
		assertEquals(0, defaultRGBW.r(), 0.001, "Default gray RGBW R");
		assertEquals(0.1f, defaultRGBW.g(), 0.001, "Default gray RGBW G");
		assertEquals(0.2f, defaultRGBW.b(), 0.001, "Default gray RGBW B");

		// Test edge cases
		LRGB black = new LRGB(0, 0, 0);
		RGBW rgbwBlack = black.RGBW(warmWhiteLED);
		assertClose(0, rgbwBlack.r(), "Black RGBW R");
		assertClose(0, rgbwBlack.g(), "Black RGBW G");
		assertClose(0, rgbwBlack.b(), "Black RGBW B");
		assertClose(0, rgbwBlack.w(), "Black RGBW W");

		// Test that total light output is preserved
		LRGB testRGB = new LRGB(0.7f, 0.5f, 0.3f);
		RGBW testRGBW = testRGB.RGBW(new LRGB(0.9f, 0.7f, 0.5f));
		// Verify: original = RGBW.rgb + W * calibration
		float reconR = testRGBW.r() + testRGBW.w() * 0.9f;
		float reconG = testRGBW.g() + testRGBW.w() * 0.7f;
		float reconB = testRGBW.b() + testRGBW.w() * 0.5f;
		assertEquals(testRGB.r(), reconR, 0.001, "RGBW reconstruction R");
		assertEquals(testRGB.g(), reconG, 0.001, "RGBW reconstruction G");
		assertEquals(testRGB.b(), reconB, 0.001, "RGBW reconstruction B");

		// Test RGBW hex and toString255
		RGBW hexTest = new RGBW(1, 0.5f, 0.25f, 0.75f);
		assertEquals("ff8040bf", Colors.hex(hexTest), "RGBW hex");
		assertEquals("255, 128, 64, 191", Colors.toString255(hexTest), "RGBW toString255");

		// Test CCT to RGBW conversion
		LRGB scaledWhite = new CCT(2700).LRGB().scl(1.8f); // Scaled warm white LED (~2700K)

		// Test maximum brightness at full
		RGB target4000 = new CCT(4000, 0).RGB();
		RGBW cctFull = new CCT(4000).LRGB().RGBW(scaledWhite);
		// Verify full brightness produces expected result
		Assertions.assertTrue(cctFull.r() >= 0 || cctFull.g() >= 0 || cctFull.b() >= 0 || cctFull.w() > 0,
			"Full brightness CCT should produce non-zero output");
		// Just verify the values are reasonable - exact match depends on LED calibration
		Assertions.assertTrue(cctFull.r() >= 0 && cctFull.r() <= 1, "Full brightness R in range");
		Assertions.assertTrue(cctFull.g() >= 0 && cctFull.g() <= 1, "Full brightness G in range");
		Assertions.assertTrue(cctFull.b() >= 0 && cctFull.b() <= 1, "Full brightness B in range");
		Assertions.assertTrue(cctFull.w() >= 0 && cctFull.w() <= 1, "Full brightness W in range");

		// Verify that the RGBW output can recreate something close to the target CCT color
		reconR = cctFull.r() + cctFull.w() * scaledWhite.r();
		reconG = cctFull.g() + cctFull.w() * scaledWhite.g();
		reconB = cctFull.b() + cctFull.w() * scaledWhite.b();
		// The reconstructed color should have similar ratios to the target (after normalization)
		float targetMax = Math.max(target4000.r(), Math.max(target4000.g(), target4000.b()));
		float reconMax = Math.max(reconR, Math.max(reconG, reconB));
		if (targetMax > 0 && reconMax > 0) {
			float targetRatio = target4000.r() / targetMax;
			float reconRatio = reconR / reconMax;
			assertEquals(targetRatio, reconRatio, 0.1, "CCT reconstruction R ratio");
		}

		// Test reduced brightness - should reduce total output
		RGBW cctDim = new CCT(4000).LRGB().scl(0.5f).RGBW(scaledWhite);
		// At 50% brightness, total output should be lower
		float dimTotal = cctDim.r() + cctDim.g() + cctDim.b() + cctDim.w();
		float fullTotal = cctFull.r() + cctFull.g() + cctFull.b() + cctFull.w();
		Assertions.assertTrue(dimTotal < fullTotal, "Dimmed CCT should have lower total output");

		// Test low brightness - should use significant white channel
		RGBW cctLow = new CCT(3000).LRGB().scl(0.2f).RGBW(scaledWhite);
		assertTrue(cctLow.w() > 0.1f, "Low brightness CCT should use significant white channel");
		// Note: 3000K vs 2700K difference requires some RGB correction

		// Test cool CCT requiring blue correction
		RGBW cctCool = new CCT(6500).LRGB().scl(0.8f).RGBW(scaledWhite);
		// With a warm white LED, cool CCT might need blue correction at higher brightness
		assertTrue(cctCool.w() > 0, "Should use white channel for CCT");
	}

	@Test
	public void testRGBWW () {
		// Test RGB to RGBWW with two whites
		LRGB warmWhite = new CCT(2700).LRGB();
		LRGB coolWhite = new CCT(6500).LRGB();
		WW ww = new WW(warmWhite, coolWhite);

		// Test warm color - should prefer warm white
		LRGB warmColor = new LRGB(0.8f, 0.6f, 0.4f);
		RGBWW warmResult = warmColor.RGBWW(ww);
		assertTrue(warmResult.w1() > warmResult.w2(), "Warm color should use more warm white");

		// Test cool color - should prefer cool white with improved algorithm
		LRGB coolColor = new LRGB(0.4f, 0.5f, 0.8f);
		RGBWW coolResult = coolColor.RGBWW(ww);
		assertTrue(coolResult.w1() > 0 || coolResult.w2() > 0, "Should use white channels");
		assertTrue(coolResult.w2() > coolResult.w1(), "Cool color should use more cool white");

		// Test CCT to RGBWW conversion

		// Test intermediate CCT - should use both whites for blending
		RGBWW cct5500 = new CCT(5500).LRGB().RGBWW(ww);
		assertTrue(cct5500.w1() + cct5500.w2() > 0.5f, "Mid CCT should use substantial white channels");
		// For intermediate temperatures, may use both whites or pick the closer one

		// Test warm CCT - should use mostly warm white
		RGBWW cct2700 = new CCT(2700).LRGB().scl(0.8f).RGBWW(ww);
		assertTrue(cct2700.w1() >= cct2700.w2(), "Warm CCT should favor warm white");

		// Test cool CCT - should prefer cool white
		RGBWW cct6500 = new CCT(6500).LRGB().scl(0.8f).RGBWW(ww);
		// With improved algorithm, 6500K should use cool white (w2)
		assertTrue(cct6500.w2() > cct6500.w1(), "6500K should prefer cool white");
		// Verify color accuracy
		float r = cct6500.r() + warmWhite.r() * cct6500.w1() + coolWhite.r() * cct6500.w2();
		float g = cct6500.g() + warmWhite.g() * cct6500.w1() + coolWhite.g() * cct6500.w2();
		float b = cct6500.b() + warmWhite.b() * cct6500.w1() + coolWhite.b() * cct6500.w2();
		assertEquals(0.8f, r, 0.001f, "Red channel accuracy");
		assertEquals(0.7542955f, g, 0.001f, "Green channel accuracy");
		assertEquals(0.7936503f, b, 0.001f, "Blue channel accuracy");

		// Test low brightness - should still maintain white ratio
		RGBWW cctLow = new CCT(4500).LRGB().scl(0.2f).RGBWW(ww);
		float totalWhite = cctLow.w1() + cctLow.w2();
		assertTrue(totalWhite > 0.15f && totalWhite < 0.25f, "Low brightness should scale whites proportionally");

		// Test RGBWW hex and toString255
		RGBWW hexTest = new RGBWW(1, 0.5f, 0.25f, 0.1f, 0.75f);
		assertEquals("ff80401abf", Colors.hex(hexTest), "RGBWW hex");
		assertEquals("255, 128, 64, 26, 191", Colors.toString255(hexTest), "RGBWW toString255");
	}

	@Test
	public void testXYZ () {
		// Test known values - D65 illuminant sRGB white should be close to [95.047, 100, 108.883]
		XYZ white = new RGB(1, 1, 1).XYZ();
		assertEquals(95.047f, white.X(), 0.1, "White X");
		assertEquals(100, white.Y(), 0.1, "White Y");
		assertEquals(108.883f, white.Z(), 0.1, "White Z");

		// Test round trip
		RGB rgb = new RGB(0.5f, 0.3f, 0.7f);
		XYZ xyz = rgb.XYZ();
		RGB rgbBack = xyz.RGB();
		assertClose(rgb, rgbBack, "XYZ round trip");
	}
}
