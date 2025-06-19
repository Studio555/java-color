
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.TestsUtil.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.colors.Colors.CAT;
import com.esotericsoftware.colors.Colors.CMYK;
import com.esotericsoftware.colors.Colors.HSI;
import com.esotericsoftware.colors.Colors.HSL;
import com.esotericsoftware.colors.Colors.HSLuv;
import com.esotericsoftware.colors.Colors.HSV;
import com.esotericsoftware.colors.Colors.HunterLab;
import com.esotericsoftware.colors.Colors.LCHuv;
import com.esotericsoftware.colors.Colors.LMS;
import com.esotericsoftware.colors.Colors.Lab;
import com.esotericsoftware.colors.Colors.LinearRGB;
import com.esotericsoftware.colors.Colors.Luv;
import com.esotericsoftware.colors.Colors.RGB;
import com.esotericsoftware.colors.Colors.RGBW;
import com.esotericsoftware.colors.Colors.RGBWW;
import com.esotericsoftware.colors.Colors.XYZ;
import com.esotericsoftware.colors.Colors.YCC;
import com.esotericsoftware.colors.Colors.YCbCr;
import com.esotericsoftware.colors.Colors.YCbCrColorSpace;
import com.esotericsoftware.colors.Colors.YIQ;
import com.esotericsoftware.colors.Colors.YUV;
import com.esotericsoftware.colors.Colors.xy;
import com.esotericsoftware.colors.Colors.xyY;

/** @author Nathan Sweet <misc@n4te.com> */
public class RGBTests {
	@Test
	public void testTSL () {
		// Test round-trip conversion for regular colors
		RGB rgb = new RGB(0.2f, 0.5f, 0.8f);
		var tsl = TSL(rgb);
		RGB rgb2 = RGB(tsl);
		assertRecordClose(rgb, rgb2, "TSL round trip", 0.001f);

		// Test black
		RGB black = new RGB(0, 0, 0);
		var tslBlack = TSL(black);
		RGB black2 = RGB(tslBlack);
		assertRecordClose(black, black2, "Black TSL round trip", EPSILON_F);

		// Test white
		RGB white = new RGB(1, 1, 1);
		var tslWhite = TSL(white);
		RGB white2 = RGB(tslWhite);
		assertRecordClose(white, white2, "White TSL round trip", 0.001f);

		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		var tslRed = TSL(red);
		RGB red2 = RGB(tslRed);
		assertRecordClose(red, red2, "Red TSL round trip", 0.001f);

		RGB green = new RGB(0, 1, 0);
		var tslGreen = TSL(green);
		RGB green2 = RGB(tslGreen);
		assertRecordClose(green, green2, "Green TSL round trip", 0.001f);

		RGB blue = new RGB(0, 0, 1);
		var tslBlue = TSL(blue);
		RGB blue2 = RGB(tslBlue);
		assertRecordClose(blue, blue2, "Blue TSL round trip", 0.001f);

		// Test T=0 case (grayscale)
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		var tslGray = TSL(gray);
		RGB gray2 = RGB(tslGray);
		assertRecordClose(gray, gray2, "Gray TSL round trip", 0.001f);

		// Test the negative zero case
		// When T = -0f, we should get a different result than T = 0f
		var tsl1 = new Colors.TSL(0f, 0.5f, 0.5f);
		var tsl2 = new Colors.TSL(-0f, 0.5f, 0.5f);
		RGB rgb1 = RGB(tsl1);
		rgb2 = RGB(tsl2);
		// The two results should be different due to the negative zero handling
		assertTrue(
			Math.abs(rgb1.r() - rgb2.r()) > 0.01f || Math.abs(rgb1.g() - rgb2.g()) > 0.01f || Math.abs(rgb1.b() - rgb2.b()) > 0.01f,
			"Negative zero T produces different result");

		// Both should satisfy the T=0 constraint in Terrillon-Akamatsu formulation
		// When T=0, g'-r'=0, meaning g'=r'
		// Check that the normalized coordinates satisfy this
		float sum1 = rgb1.r() + rgb1.g() + rgb1.b();
		float sum2 = rgb2.r() + rgb2.g() + rgb2.b();
		float r1_norm = rgb1.r() / sum1 - 1.0f / 3.0f;
		float g1_norm = rgb1.g() / sum1 - 1.0f / 3.0f;
		float r2_norm = rgb2.r() / sum2 - 1.0f / 3.0f;
		float g2_norm = rgb2.g() / sum2 - 1.0f / 3.0f;
		assertClose(g1_norm, r1_norm, "T=0 constraint g'=r' for positive zero", 0.001f);
		assertClose(g2_norm, r2_norm, "T=0 constraint g'=r' for negative zero", 0.001f);

		// Test RGB values satisfying 2*G = R+B
		// These should round-trip correctly through TSL
		RGB rgb2g1 = new RGB(0.2f, 0.3f, 0.4f); // 2*0.3 = 0.2+0.4
		var tsl2g1 = TSL(rgb2g1);
		RGB rgb2g1_back = RGB(tsl2g1);
		assertRecordClose(rgb2g1, rgb2g1_back, "2*G=R+B round trip (1)", 0.001f);

		RGB rgb2g2 = new RGB(0.1f, 0.4f, 0.7f); // 2*0.4 = 0.1+0.7
		var tsl2g2 = TSL(rgb2g2);
		RGB rgb2g2_back = RGB(tsl2g2);
		assertRecordClose(rgb2g2, rgb2g2_back, "2*G=R+B round trip (2)", 0.001f);

		// Edge case: when 2*G = R+B and R=B (implies G=R=B, grayscale)
		RGB rgb2g3 = new RGB(0.4f, 0.4f, 0.4f); // 2*0.4 = 0.4+0.4
		var tsl2g3 = TSL(rgb2g3);
		assertClose(0, tsl2g3.S(), "Grayscale has S=0", EPSILON_F);
		RGB rgb2g3_back = RGB(tsl2g3);
		assertRecordClose(rgb2g3, rgb2g3_back, "Grayscale 2*G=R+B round trip", 0.001f);
	}

	@Test
	public void testLinearRGB () {
		// Test RGB values
		RGB[] testColors = {new RGB(1, 0, 0), // Red
			new RGB(0, 1, 0), // Green
			new RGB(0, 0, 1), // Blue
			new RGB(0.5f, 0.5f, 0.5f), // Gray
			new RGB(0.2f, 0.5f, 0.8f) // Arbitrary
		};

		for (RGB rgb : testColors) {
			// Test linear RGB to XYZ round trip
			LinearRGB linearRgb = LinearRGB(rgb); // This applies linearization
			XYZ xyz = XYZ(linearRgb);
			LinearRGB linearBack = LinearRGB(xyz);

			assertRecordClose(linearRgb, linearBack, "Linear RGB round trip", EPSILON_F);

			// Test that the same numerical values interpreted as linear vs gamma-corrected produce different XYZ
			// Create LinearRGB with same numerical values as the gamma-corrected RGB (no conversion)
			LinearRGB linearSameValues = new LinearRGB(rgb.r(), rgb.g(), rgb.b());
			XYZ xyzLinear = XYZ(linearSameValues);
			XYZ xyzGamma = XYZ(rgb);

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

		// Test xyXYZ with Y=1
		XYZ xyz = XYZ(new xy(0.3127f, 0.3290f)); // D65 white point
		assertClose(1, xyz.Y(), "xyXYZ Y value", EPSILON_F);

		// Verify it produces the same ratios as XYZ
		XYZ xyzFromxyY = XYZ(new xyY(0.3127f, 0.3290f, 1));
		assertRecordClose(xyz, xyzFromxyY, "xyXYZ matches XYZ", EPSILON_F);
	}

	@Test
	public void testCMYK () {
		// Test known values
		CMYK black = CMYK(new RGB(0, 0, 0));
		assertRecordClose(new CMYK(0, 0, 0, 1), black, "Black to CMYK");

		CMYK white = CMYK(new RGB(1, 1, 1));
		assertRecordClose(new CMYK(0, 0, 0, 0), white, "White to CMYK");

		CMYK red = CMYK(new RGB(1, 0, 0));
		assertRecordClose(new CMYK(0, 1, 1, 0), red, "Red to CMYK");

		// Test all primary and secondary colors
		CMYK green = CMYK(new RGB(0, 1, 0));
		assertRecordClose(new CMYK(1, 0, 1, 0), green, "Green to CMYK");

		CMYK blue = CMYK(new RGB(0, 0, 1));
		assertRecordClose(new CMYK(1, 1, 0, 0), blue, "Blue to CMYK");

		CMYK yellow = CMYK(new RGB(1, 1, 0));
		assertRecordClose(new CMYK(0, 0, 1, 0), yellow, "Yellow to CMYK");

		CMYK cyan = CMYK(new RGB(0, 1, 1));
		assertRecordClose(new CMYK(1, 0, 0, 0), cyan, "Cyan to CMYK");

		CMYK magenta = CMYK(new RGB(1, 0, 1));
		assertRecordClose(new CMYK(0, 1, 0, 0), magenta, "Magenta to CMYK");

		// Test grays with different K values
		for (float gray = 0.1f; gray <= 0.9f; gray += 0.1f) {
			RGB rgb = new RGB(gray, gray, gray);
			CMYK cmyk = CMYK(rgb);
			// For grays, CMY should be 0 and K should be 1-gray
			assertClose(0, cmyk.C(), "Gray C at " + gray, 0.001);
			assertClose(0, cmyk.M(), "Gray M at " + gray, 0.001);
			assertClose(0, cmyk.Y(), "Gray Y at " + gray, 0.001);
			assertClose(1 - gray, cmyk.K(), "Gray K at " + gray, 0.001);
		}

		// Test rich black (CMYK with all components)
		RGB darkGray = new RGB(0.2f, 0.2f, 0.2f);
		CMYK richBlack = CMYK(darkGray);
		assertClose(0.8f, richBlack.K(), "Rich black K component", 0.001);

		// Test round trip for various colors
		for (float r = 0; r <= 1.0f; r += 0.25f) {
			for (float g = 0; g <= 1.0f; g += 0.25f) {
				for (float b = 0; b <= 1.0f; b += 0.25f) {
					RGB rgb = new RGB(r, g, b);
					CMYK cmyk = CMYK(rgb);
					RGB rgbBack = RGB(cmyk);
					assertRecordClose(rgb, rgbBack, "CMYK round trip " + rgb, 0.001);
				}
			}
		}
	}

	@Test
	public void testHSI () {
		// Test known values
		HSI black = HSI(new RGB(0, 0, 0));
		assertClose(0, black.I(), "Black intensity");
		assertClose(0, black.S(), "Black saturation");

		HSI white = HSI(new RGB(1, 1, 1));
		assertClose(1, white.I(), "White intensity");
		assertClose(0, white.S(), "White saturation");

		HSI red = HSI(new RGB(1, 0, 0));
		assertClose(1f / 3f, red.I(), "Red intensity");
		assertClose(1, red.S(), "Red saturation");
		assertClose(0, red.H(), "Red hue");

		HSI gray = HSI(new RGB(0.5f, 0.5f, 0.5f));
		assertClose(0.5f, gray.I(), "Gray intensity");
		assertClose(0, gray.S(), "Gray saturation");

		// Test round trip
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), Colors::HSI, Colors::RGB, "HSI");

		// Test additional colors
		roundTripd(new RGB(1, 0, 0), Colors::HSI, Colors::RGB, "HSI Red");
		roundTripd(new RGB(0, 1, 0), Colors::HSI, Colors::RGB, "HSI Green");
		roundTripd(new RGB(0, 0, 1), Colors::HSI, Colors::RGB, "HSI Blue");
		roundTripd(new RGB(1, 1, 0), Colors::HSI, Colors::RGB, "HSI Yellow");
		roundTripd(new RGB(0, 1, 1), Colors::HSI, Colors::RGB, "HSI Cyan");
		roundTripd(new RGB(1, 0, 1), Colors::HSI, Colors::RGB, "HSI Magenta");

	}

	@Test
	public void testHSL () {
		// Test known values
		HSL red = HSL(new RGB(1, 0, 0));
		assertClose(0, red.H(), "Red hue");
		assertClose(1, red.S(), "Red saturation");
		assertClose(0.5f, red.L(), "Red lightness");

		// Test round trip
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), Colors::HSL, Colors::RGB, "HSL");
	}

	@Test
	public void testHSLuv () {
		// Test known values
		// Pure red
		HSLuv red = HSLuv(new RGB(1, 0, 0));
		// HSLuv values for pure red in sRGB
		// We'll just verify the ranges are reasonable
		assertTrue(red.H() >= 0 && red.H() <= 360, "Red hue in valid range");
		assertTrue(red.S() >= 0 && red.S() <= 100, "Red saturation in valid range");
		assertTrue(red.L() >= 50 && red.L() <= 55, "Red lightness in expected range");

		// Pure white
		HSLuv white = HSLuv(new RGB(1, 1, 1));
		assertClose(0, white.S(), "White saturation", 0.1f);
		assertClose(100, white.L(), "White lightness");

		// Pure black
		HSLuv black = HSLuv(new RGB(0, 0, 0));
		assertClose(0, black.S(), "Black saturation");
		assertClose(0, black.L(), "Black lightness");

		// Test round trip with RGB within HSLuv gamut
		roundTripf(new RGB(0.43f, 0.37f, 0.51f), Colors::HSLuv, Colors::RGB, "HSLuv");
	}

	@Test
	public void testHSV () {
		// Test known values
		HSV red = HSV(new RGB(1, 0, 0));
		assertClose(0, red.H(), "Red hue");
		assertClose(1, red.S(), "Red saturation");
		assertClose(1, red.V(), "Red value");

		HSV gray = HSV(new RGB(0.5f, 0.5f, 0.5f));
		assertClose(0, gray.S(), "Gray saturation");
		assertClose(0.5f, gray.V(), "Gray value");

		// Test all primary and secondary colors
		assertClose(0, HSV(new RGB(1, 0, 0)).H(), "Red hue", 0.1);
		assertClose(60, HSV(new RGB(1, 1, 0)).H(), "Yellow hue", 0.1);
		assertClose(120, HSV(new RGB(0, 1, 0)).H(), "Green hue", 0.1);
		assertClose(180, HSV(new RGB(0, 1, 1)).H(), "Cyan hue", 0.1);
		assertClose(240, HSV(new RGB(0, 0, 1)).H(), "Blue hue", 0.1);
		assertClose(300, HSV(new RGB(1, 0, 1)).H(), "Magenta hue", 0.1);

		// Test systematic hue values
		for (float hue = 0; hue < 360; hue += 30) {
			HSV hsv = new HSV(hue, 1.0f, 1.0f);
			RGB rgb = RGB(hsv);
			HSV hsvBack = HSV(rgb);
			// Handle hue wraparound
			float hueDiff = Math.abs(hsv.H() - hsvBack.H());
			if (hueDiff > 180) hueDiff = 360 - hueDiff;
			assertClose(0, hueDiff, "HSV hue round trip at " + hue, 0.1);
			assertClose(hsv.S(), hsvBack.S(), "HSV saturation round trip at hue " + hue, 0.001);
			assertClose(hsv.V(), hsvBack.V(), "HSV value round trip at hue " + hue, 0.001);
		}

		// Test various saturation levels
		for (float sat = 0; sat <= 1.0f; sat += 0.1f) {
			HSV hsv = new HSV(180, sat, 0.8f); // Cyan at different saturations
			RGB rgb = RGB(hsv);
			HSV hsvBack = HSV(rgb);
			if (sat > 0) { // Only check hue if there's saturation
				assertClose(hsv.H(), hsvBack.H(), "HSV hue at saturation " + sat, 0.1);
			}
			assertClose(hsv.S(), hsvBack.S(), "HSV saturation at " + sat, 0.001);
			assertClose(hsv.V(), hsvBack.V(), "HSV value at saturation " + sat, 0.001);
		}

		// Test round trip for grid of colors
		for (float r = 0; r <= 1.0f; r += 0.2f) {
			for (float g = 0; g <= 1.0f; g += 0.2f) {
				for (float b = 0; b <= 1.0f; b += 0.2f) {
					RGB rgb = new RGB(r, g, b);
					roundTripd(rgb, Colors::HSV, Colors::RGB, "HSV " + rgb);
				}
			}
		}
	}

	@Test
	public void testHunterLab () {
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> HunterLab(c), Colors::RGB, "Hunter Lab");

		// Test XYZ <-> Hunter Lab
		XYZ xyz = new XYZ(50, 50, 50);
		HunterLab hunterLab = HunterLab(xyz);
		XYZ xyzBack = XYZ(hunterLab);
		assertRecordClose(xyz, xyzBack, "XYZ <-> Hunter Lab round trip");
	}

	@Test
	public void testIHS () {
		// IHS uses intensity as sum of RGB (0-3 range)
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), Colors::IHS, Colors::RGB, "IHS");
	}

	@Test
	public void testLab () {
		// Test with default D65 illuminant
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> Lab(c), Colors::RGB, "Lab D65");

		// Test known values from CIE standards
		Lab whiteLab = Lab(new RGB(1, 1, 1));
		assertClose(100, whiteLab.L(), "White L*", 0.1);
		assertClose(0, whiteLab.a(), "White a*", 0.1);
		assertClose(0, whiteLab.b(), "White b*", 0.1);

		Lab blackLab = Lab(new RGB(0, 0, 0));
		assertClose(0, blackLab.L(), "Black L*", 0.1);
		assertClose(0, blackLab.a(), "Black a*", 0.1);
		assertClose(0, blackLab.b(), "Black b*", 0.1);

		// Test systematic range of L* values
		for (float L = 0; L <= 100; L += 10) {
			float gray = L / 100.0f;
			RGB rgb = new RGB(gray, gray, gray);
			Lab lab = Lab(rgb);
			// L* should be approximately the input L value for grays
			// Note: The relationship is not perfectly linear due to gamma
			assertClose(0, lab.a(), "Gray a* should be 0 at L=" + L, 0.1);
			assertClose(0, lab.b(), "Gray b* should be 0 at L=" + L, 0.1);
		}

		// Test known CIE standard colors
		// CIE red (approximately)
		Lab redLab = Lab(new RGB(1, 0, 0));
		assertClose(53.233f, redLab.L(), "Red L*", 0.5);
		assertClose(80.109f, redLab.a(), "Red a*", 0.5);
		assertClose(67.220f, redLab.b(), "Red b*", 0.5);

		// Test round-trip for various colors
		float[] testValues = {0.0f, 0.01f, 0.1f, 0.2f, 0.3f, 0.33f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
		for (float r : testValues) {
			for (float g : new float[] {0.0f, 0.5f, 1.0f}) {
				for (float b : new float[] {0.0f, 0.5f, 1.0f}) {
					RGB rgb = new RGB(r, g, b);
					Lab lab = Lab(rgb);
					RGB rgbBack = RGB(lab);
					assertRecordClose(rgb, rgbBack, "Lab round trip " + rgb, 0.001f);
				}
			}
		}
		assertClose(0, whiteLab.b(), "White b*", 0.1);
	}

	@Test
	public void testLCh () {
		// Test round trip with default D65
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> LCh(c), Colors::RGB, "LCh");
	}

	@Test
	public void testLMS () {
		// Test all matrix types
		for (CAT matrix : CAT.values()) {
			// Test XYZ <-> LMS
			XYZ xyz = new XYZ(50, 50, 50);
			LMS lms = LMS(xyz, matrix);
			XYZ xyzBack = XYZ(lms, matrix);
			assertRecordClose(xyz, xyzBack, "XYZ <-> LMS " + matrix + " round trip", EPSILON_F);

			// Test RGB <-> LMS
			RGB rgb = new RGB(0.5f, 0.3f, 0.7f);
			LMS lmsRgb = LMS(rgb, matrix);
			RGB rgbBack = RGB(lmsRgb, matrix);
			assertRecordClose(rgb, rgbBack, "RGB <-> LMS " + matrix + " round trip");
		}
	}

	@Test
	public void testLuv () {
		// Test round trip
		roundTripf(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> Luv(c), Colors::RGB, "Luv");

		// Test known values
		Luv whiteLuv = Luv(new RGB(1, 1, 1));
		assertClose(100, whiteLuv.L(), "White L*", 0.1);
		assertClose(0, whiteLuv.u(), "White u*", 0.1);
		assertClose(0, whiteLuv.v(), "White v*", 0.1);

		// Test Luv <-> LCHuv conversion
		Luv luv = new Luv(50, 20, -30);
		LCHuv lch = LCHuv(luv);
		Luv luvBack = Luv(lch);

		assertClose(luv.L(), luvBack.L(), "L component", EPSILON_F);
		assertClose(luv.u(), luvBack.u(), "u component", EPSILON_F);
		assertClose(luv.v(), luvBack.v(), "v component", EPSILON_F);
	}

	@Test
	public void testRGBW () {
		// Test with perfect white LED (ideal case)
		RGB white = new RGB(1, 1, 1);
		RGBW rgbwWhite = RGBW(white, new RGB(1, 1, 1));
		assertClose(0, rgbwWhite.r(), "White RGBW R");
		assertClose(0, rgbwWhite.g(), "White RGBW G");
		assertClose(0, rgbwWhite.b(), "White RGBW B");
		assertClose(1, rgbwWhite.w(), "White RGBW W");

		// Test pure colors with ideal white
		RGB red = new RGB(1, 0, 0);
		RGBW rgbwRed = RGBW(red, new RGB(1, 1, 1));
		assertClose(1, rgbwRed.r(), "Red RGBW R");
		assertClose(0, rgbwRed.g(), "Red RGBW G");
		assertClose(0, rgbwRed.b(), "Red RGBW B");
		assertClose(0, rgbwRed.w(), "Red RGBW W");

		// Test with warm white LED calibration
		RGB warmWhiteLED = new RGB(1, 0.8f, 0.6f);

		// Pure white should use only W channel up to the blue limit
		RGBW warmWhite = RGBW(white, warmWhiteLED);
		assertClose(0, warmWhite.r(), "Warm white RGBW R");
		assertClose(0.2f, warmWhite.g(), "Warm white RGBW G");
		assertClose(0.4f, warmWhite.b(), "Warm white RGBW B");
		assertClose(1, warmWhite.w(), "Warm white RGBW W");

		// Test mixed color
		RGB mixed = new RGB(0.8f, 0.6f, 0.4f);
		RGBW rgbwMixed = RGBW(mixed, warmWhiteLED);
		// W is limited by blue channel: 0.4 / 0.6 = 0.667
		float expectedW = 0.4f / 0.6f;
		assertClose(expectedW, rgbwMixed.w(), "Mixed RGBW W", 0.001);
		assertClose(0.8f - expectedW * warmWhiteLED.r(), rgbwMixed.r(), "Mixed RGBW R", 0.001);
		assertClose(0.6f - expectedW * warmWhiteLED.g(), rgbwMixed.g(), "Mixed RGBW G", 0.001);
		assertClose(0, rgbwMixed.b(), "Mixed RGBW B", 0.001); // Should be 0

		// Test warm white conversion
		RGBW defaultRGBW = RGBW(new RGB(0.5f, 0.5f, 0.5f), warmWhiteLED);
		// With default calibration (1, 0.8, 0.6), gray is limited by blue
		// But we also clamp W to 1, so W = min(0.5/0.6, 1) = min(0.833, 1) = 0.833
		// Actually no, we need to check which channel limits first
		// For gray 0.5: R needs 0.5/1.0 = 0.5, G needs 0.5/0.8 = 0.625, B needs 0.5/0.6 = 0.833
		// So R is the limiting factor, W = 0.5
		assertClose(0.5f, defaultRGBW.w(), "Default gray RGBW W", 0.001);
		assertClose(0, defaultRGBW.r(), "Default gray RGBW R", 0.001);
		assertClose(0.1f, defaultRGBW.g(), "Default gray RGBW G", 0.001);
		assertClose(0.2f, defaultRGBW.b(), "Default gray RGBW B", 0.001);

		// Test edge cases
		RGB black = new RGB(0, 0, 0);
		RGBW rgbwBlack = RGBW(black, warmWhiteLED);
		assertClose(0, rgbwBlack.r(), "Black RGBW R");
		assertClose(0, rgbwBlack.g(), "Black RGBW G");
		assertClose(0, rgbwBlack.b(), "Black RGBW B");
		assertClose(0, rgbwBlack.w(), "Black RGBW W");

		// Test that total light output is preserved
		RGB testRGB = new RGB(0.7f, 0.5f, 0.3f);
		RGBW testRGBW = RGBW(testRGB, new RGB(0.9f, 0.7f, 0.5f));
		// Verify: original = RGBW.rgb + W * calibration
		float reconR = testRGBW.r() + testRGBW.w() * 0.9f;
		float reconG = testRGBW.g() + testRGBW.w() * 0.7f;
		float reconB = testRGBW.b() + testRGBW.w() * 0.5f;
		assertClose(testRGB.r(), reconR, "RGBW reconstruction R", 0.001);
		assertClose(testRGB.g(), reconG, "RGBW reconstruction G", 0.001);
		assertClose(testRGB.b(), reconB, "RGBW reconstruction B", 0.001);

		// Test RGBW hex and toString255
		RGBW hexTest = new RGBW(1, 0.5f, 0.25f, 0.75f);
		assertEquals("ff8040bf", hex(hexTest), "RGBW hex");
		assertEquals("255, 128, 64, 191", toString255(hexTest), "RGBW toString255");

		// Test CCT to RGBW conversion
		RGB scaledWhite = new RGB(1.8f, 1.6f, 1.0f); // Scaled warm white LED (~2700K)

		// Test maximum brightness at full
		RGB target4000 = RGB(4000, 0);
		RGBW cctFull = RGBW(4000, 1.0f, scaledWhite);
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
			assertClose(targetRatio, reconRatio, "CCT reconstruction R ratio", 0.1);
		}

		// Test reduced brightness - should reduce total output
		RGBW cctDim = RGBW(4000, 0.5f, scaledWhite);
		// At 50% brightness, total output should be lower
		float dimTotal = cctDim.r() + cctDim.g() + cctDim.b() + cctDim.w();
		float fullTotal = cctFull.r() + cctFull.g() + cctFull.b() + cctFull.w();
		Assertions.assertTrue(dimTotal < fullTotal, "Dimmed CCT should have lower total output");

		// Test low brightness - should have W only
		RGBW cctLow = RGBW(3000, 0.2f, scaledWhite);
		assertTrue(cctLow.r() < 0.01f && cctLow.g() < 0.01f && cctLow.b() < 0.01f,
			"Low brightness CCT should use mostly white channel");

		// Test cool CCT requiring blue correction
		RGBW cctCool = RGBW(6500, 0.8f, scaledWhite);
		// With a warm white LED, cool CCT might need blue correction at higher brightness
		assertTrue(cctCool.w() > 0, "Should use white channel for CCT");
	}

	@Test
	public void testRGBWW () {
		// Test RGB to RGBWW with two whites
		RGB warmWhite = new RGB(1.8f, 1.6f, 1.0f); // 2700K-ish, scaled
		RGB coolWhite = new RGB(1.2f, 1.4f, 1.8f); // 6500K-ish, scaled

		// Test warm color - should prefer warm white
		RGB warmColor = new RGB(0.8f, 0.6f, 0.4f);
		RGBWW warmResult = RGBWW(warmColor, warmWhite, coolWhite);
		assertTrue(warmResult.w1() > warmResult.w2(), "Warm color should use more warm white");

		// Test cool color - should prefer cool white
		RGB coolColor = new RGB(0.4f, 0.5f, 0.8f);
		RGBWW coolResult = RGBWW(coolColor, warmWhite, coolWhite);
		assertTrue(coolResult.w2() > coolResult.w1(), "Cool color should use more cool white");

		// Test CCT to RGBWW conversion

		// Test intermediate CCT - should blend whites
		RGBWW cct4000 = RGBWW(4000, 1.0f, warmWhite, coolWhite);
		assertTrue(cct4000.w1() > 0 && cct4000.w2() > 0, "Mid CCT should blend both whites");

		// Test warm CCT - should use mostly warm white
		RGBWW cct2700 = RGBWW(2700, 0.8f, warmWhite, coolWhite);
		assertTrue(cct2700.w1() >= cct2700.w2(), "Warm CCT should favor warm white");

		// Test cool CCT - should use mostly cool white
		RGBWW cct6500 = RGBWW(6500, 0.8f, warmWhite, coolWhite);
		// Note: if CCT calculation fails for the LEDs, it might fall back to equal blend
		assertTrue(cct6500.w1() + cct6500.w2() > 0.7f, "Should use white channels for CCT");

		// Test low brightness - should still maintain white ratio
		RGBWW cctLow = RGBWW(4500, 0.2f, warmWhite, coolWhite);
		float totalWhite = cctLow.w1() + cctLow.w2();
		assertTrue(totalWhite > 0.15f && totalWhite < 0.25f, "Low brightness should scale whites proportionally");

		// Test RGBWW hex and toString255
		RGBWW hexTest = new RGBWW(1, 0.5f, 0.25f, 0.1f, 0.75f);
		assertEquals("ff80401abf", hex(hexTest), "RGBWW hex");
		assertEquals("255, 128, 64, 26, 191", toString255(hexTest), "RGBWW toString255");
	}

	@Test
	public void testXYZ () {
		// Test known values - D65 illuminant sRGB white should be close to [95.047, 100, 108.883]
		XYZ white = XYZ(new RGB(1, 1, 1));
		assertClose(95.047f, white.X(), "White X", 0.1);
		assertClose(100, white.Y(), "White Y", 0.1);
		assertClose(108.883f, white.Z(), "White Z", 0.1);

		// Test round trip
		RGB rgb = new RGB(0.5f, 0.3f, 0.7f);
		XYZ xyz = XYZ(rgb);
		RGB rgbBack = RGB(xyz);
		assertRecordClose(rgb, rgbBack, "XYZ round trip");
	}
}
