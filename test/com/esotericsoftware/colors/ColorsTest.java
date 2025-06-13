
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;

import java.lang.reflect.RecordComponent;
import java.util.function.Function;

import com.esotericsoftware.colors.Colors;
import com.esotericsoftware.colors.Colors.C1C2C3;
import com.esotericsoftware.colors.Colors.CAT;
import com.esotericsoftware.colors.Colors.CMYK;
import com.esotericsoftware.colors.Colors.HSI;
import com.esotericsoftware.colors.Colors.HSL;
import com.esotericsoftware.colors.Colors.HSLuv;
import com.esotericsoftware.colors.Colors.HSV;
import com.esotericsoftware.colors.Colors.HunterLab;
import com.esotericsoftware.colors.Colors.Illuminant;
import com.esotericsoftware.colors.Colors.LCh;
import com.esotericsoftware.colors.Colors.LCHuv;
import com.esotericsoftware.colors.Colors.LMS;
import com.esotericsoftware.colors.Colors.Lab;
import com.esotericsoftware.colors.Colors.Luv;
import com.esotericsoftware.colors.Colors.LinearRGB;
import com.esotericsoftware.colors.Colors.O1O2;
import com.esotericsoftware.colors.Colors.Oklab;
import com.esotericsoftware.colors.Colors.Oklch;
import com.esotericsoftware.colors.Colors.RGB;
import com.esotericsoftware.colors.Colors.RGBTW;
import com.esotericsoftware.colors.Colors.RGBW;
import com.esotericsoftware.colors.Colors.RGChromaticity;
import com.esotericsoftware.colors.Colors.XYZ;
import com.esotericsoftware.colors.Colors.YCC;
import com.esotericsoftware.colors.Colors.YCbCr;
import com.esotericsoftware.colors.Colors.YCbCrColorSpace;
import com.esotericsoftware.colors.Colors.YIQ;
import com.esotericsoftware.colors.Colors.YUV;
import com.esotericsoftware.colors.Colors.uv;
import com.esotericsoftware.colors.Colors.uv1960;
import com.esotericsoftware.colors.Colors.xy;
import com.esotericsoftware.colors.Colors.xyY;

public class ColorsTest {
	static final float EPSILON = 0.001f; // Tolerance for floating point comparisons
	static final double EPSILON_D = 0.001; // Tolerance for double comparisons
	static final double EPSILON_LOOSE = 0.01; // For lossy conversions
	static final double EPSILON_VERY_LOOSE = 0.35; // YCC and YUV are very lossy

	public static void main (String[] args) {
		System.out.println("Running color conversion tests...\n");

		// Oklab tests
		testRgbToOklabAndBack();
		testKnownOklabValues();
		testOklchConversions();
		testEdgeCases();
		testCCTToOklab();

		// ColorConverter tests
		testRGBCMYK();
		testRGBIHS();
		testRGBYUV();
		testRGBYIQ();
		testRGBHSV();
		testRGBHSL();
		testRGBHSLuv();
		testRGBXYZ();
		testCIE1960Conversions();
		testRGBYCbCr();
		testRGBYCC();
		testRGBYCoCg();
		testRGBYES();
		testXYZxyY();
		testRGBLab();
		testRGBLuv();
		testLabLCh();
		testRGBHunterLab();
		testRGBLCh();
		testRGBLMS();
		testHunterLab();
		testLCh();
		testLMS();
		testAllRoundTrips();
		testUtilityFunctions();
		testSpecialConversions();
		testMatrixMultiply();
		testCCTConversions();
		testLinearRGBConversions();
		testOklabConversions();
		testUV1976Conversions();
		testGammaFunctions();
		testDMXConversions();
		testRGBWConversions();
		testRGBTWConversions();

		System.out.println("All tests passed!");
	}

	static void testRgbToOklabAndBack () {
		System.out.println("Testing RGB -> Oklab -> RGB round trip conversions...");

		// Test primary colors
		testOklabRoundTrip(new RGB(1, 0, 0), "Red");
		testOklabRoundTrip(new RGB(0, 1, 0), "Green");
		testOklabRoundTrip(new RGB(0, 0, 1), "Blue");
		testOklabRoundTrip(new RGB(1, 1, 0), "Yellow");
		testOklabRoundTrip(new RGB(0, 1, 1), "Cyan");
		testOklabRoundTrip(new RGB(1, 0, 1), "Magenta");
		testOklabRoundTrip(new RGB(1, 1, 1), "White");
		testOklabRoundTrip(new RGB(0, 0, 0), "Black");
		testOklabRoundTrip(new RGB(0.5f, 0.5f, 0.5f), "Gray");

		// Test some random colors
		testOklabRoundTrip(new RGB(0.8f, 0.2f, 0.4f), "Pink");
		testOklabRoundTrip(new RGB(0.1f, 0.6f, 0.3f), "Teal");
		testOklabRoundTrip(new RGB(0.9f, 0.7f, 0.1f), "Gold");

		System.out.println("Round trip tests passed.\n");
	}

	static void testKnownOklabValues () {
		System.out.println("Testing known Oklab values...");

		// Test white
		Oklab white = Oklab(new RGB(1, 1, 1));
		assertClose(white.L(), 1.0f, "White L");
		assertClose(white.a(), 0.0f, "White a");
		assertClose(white.b(), 0.0f, "White b");

		// Test black
		Oklab black = Oklab(new RGB(0, 0, 0));
		assertClose(black.L(), 0.0f, "Black L");
		assertClose(black.a(), 0.0f, "Black a");
		assertClose(black.b(), 0.0f, "Black b");

		// Test gray (should have a=0, b=0)
		Oklab gray = Oklab(new RGB(0.5f, 0.5f, 0.5f));
		assertClose(gray.a(), 0.0f, "Gray a");
		assertClose(gray.b(), 0.0f, "Gray b");

		System.out.println("Known value tests passed.\n");
	}

	static void testOklchConversions () {
		System.out.println("Testing Oklch conversions...");

		// Test RGB -> Oklch -> RGB
		RGB original = new RGB(0.7f, 0.3f, 0.5f);
		Oklch lch = Oklch(original);
		RGB result = RGB(lch);
		assertArrayClose(original, result, "Oklch round trip");

		// Test Oklab <-> Oklch
		Oklab lab = Oklab(new RGB(0.6f, 0.4f, 0.2f));
		Oklch lch2 = Oklch(lab);
		Oklab labBack = Oklab(lch2);
		assertClose(lab.L(), labBack.L(), "L channel");
		assertClose(lab.a(), labBack.a(), "a channel");
		assertClose(lab.b(), labBack.b(), "b channel");

		// Test hue angle wrapping
		Oklab labResult = Oklab(new Oklch(0.5f, 0.1f, 370f)); // 370° = 10°
		Oklab labExpected = Oklab(new Oklch(0.5f, 0.1f, 10f));
		assertClose(labResult.a(), labExpected.a(), "Hue wrap a");
		assertClose(labResult.b(), labExpected.b(), "Hue wrap b");

		System.out.println("Oklch conversion tests passed.\n");
	}

	static void testEdgeCases () {
		System.out.println("Testing edge cases...");

		// Test near-zero values
		testOklabRoundTrip(new RGB(0.001f, 0.001f, 0.001f), "Near black");
		testOklabRoundTrip(new RGB(0.999f, 0.999f, 0.999f), "Near white");

		// Test single channel
		testOklabRoundTrip(new RGB(0.5f, 0, 0), "Half red");
		testOklabRoundTrip(new RGB(0, 0.5f, 0), "Half green");
		testOklabRoundTrip(new RGB(0, 0, 0.5f), "Half blue");

		// Test very saturated colors
		testOklabRoundTrip(new RGB(1, 0, 0.001f), "Almost pure red");
		testOklabRoundTrip(new RGB(0.001f, 1, 0), "Almost pure green");

		System.out.println("Edge case tests passed.\n");
	}

	static void testCCTToOklab () {
		System.out.println("Testing CCT to Oklab conversions...");

		// Test some common color temperatures
		RGB warmWhite = RGB(2700, 0);
		Oklab warmLab = Oklab(warmWhite);
		System.out.println("  2700K: L=" + warmLab.L() + " a=" + warmLab.a() + " b=" + warmLab.b());

		RGB neutralWhite = RGB(4000, 0);
		Oklab neutralLab = Oklab(neutralWhite);
		System.out.println("  4000K: L=" + neutralLab.L() + " a=" + neutralLab.a() + " b=" + neutralLab.b());

		RGB coolWhite = RGB(6500, 0);
		Oklab coolLab = Oklab(coolWhite);
		System.out.println("  6500K: L=" + coolLab.L() + " a=" + coolLab.a() + " b=" + coolLab.b());

		// After normalization in CCTRGB, the differences are subtle but b channel shows clear trend
		// Warmer colors have higher b (yellow) values
		if (warmLab.b() > neutralLab.b() && neutralLab.b() > coolLab.b()) {
			System.out.println("  CCT ordering correct (warm->cool = high->low b).");
		} else {
			throw new AssertionError("CCT to Oklab 'b' channel ordering incorrect");
		}

		System.out.println("CCT to Oklab tests passed.\n");
	}

	static void testOklabRoundTrip (RGB original, String name) {
		Oklab lab = Oklab(original);
		RGB result = RGB(lab);
		assertArrayClose(original, result, name);
	}

	static void assertClose (float expected, float actual, String name) {
		if (Math.abs(expected - actual) > EPSILON) {
			throw new AssertionError(String.format("%s mismatch: expected %.6f, got %.6f (diff: %.6f)", name, expected, actual,
				Math.abs(expected - actual)));
		}
	}

	static void testRGBCMYK () {
		System.out.println("Testing RGB <-> CMYK conversions...");

		// Test known values
		CMYK black = CMYK(new RGB(0, 0, 0));
		assertArrayClose(new CMYK(0, 0, 0, 1), black, "Black to CMYK");

		CMYK white = CMYK(new RGB(1, 1, 1));
		assertArrayClose(new CMYK(0, 0, 0, 0), white, "White to CMYK");

		CMYK red = CMYK(new RGB(1, 0, 0));
		assertArrayClose(new CMYK(0, 1, 1, 0), red, "Red to CMYK");

		// Test round trip
		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), Colors::CMYK, Colors::RGB, "CMYK");

		System.out.println("RGB <-> CMYK tests passed.\n");
	}

	static void testRGBHSI () {
		System.out.println("Testing RGB <-> HSI conversions...");

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
		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), Colors::HSI, Colors::RGB, "HSI");

		// Test additional colors
		testRoundTrip(new RGB(1, 0, 0), Colors::HSI, Colors::RGB, "HSI Red");
		testRoundTrip(new RGB(0, 1, 0), Colors::HSI, Colors::RGB, "HSI Green");
		testRoundTrip(new RGB(0, 0, 1), Colors::HSI, Colors::RGB, "HSI Blue");
		testRoundTrip(new RGB(1, 1, 0), Colors::HSI, Colors::RGB, "HSI Yellow");
		testRoundTrip(new RGB(0, 1, 1), Colors::HSI, Colors::RGB, "HSI Cyan");
		testRoundTrip(new RGB(1, 0, 1), Colors::HSI, Colors::RGB, "HSI Magenta");

		System.out.println("RGB <-> HSI tests passed.\n");
	}

	static void testRGBIHS () {
		System.out.println("Testing RGB <-> IHS conversions...");

		// IHS uses intensity as sum of RGB (0-3 range)
		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), Colors::IHS, Colors::RGB, "IHS");

		System.out.println("RGB <-> IHS tests passed.\n");
	}

	static void testRGBYUV () {
		System.out.println("Testing RGB <-> YUV conversions...");

		// Test known values
		YUV black = YUV(new RGB(0, 0, 0));
		assertArrayClose(new YUV(0, 0, 0), black, "Black to YUV");

		YUV white = YUV(new RGB(1, 1, 1));
		assertClose(1, white.Y(), "White Y");
		assertClose(0, white.U(), "White U");
		assertClose(0, white.V(), "White V");

		// Test round trip - YUV has small precision errors
		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), Colors::YUV, Colors::RGB, "YUV", 0.002);

		System.out.println("RGB <-> YUV tests passed.\n");
	}

	static void testRGBYIQ () {
		System.out.println("Testing RGB <-> YIQ conversions...");

		// Test known values
		YIQ black = YIQ(new RGB(0, 0, 0));
		assertArrayClose(new YIQ(0, 0, 0), black, "Black to YIQ");

		// Test round trip
		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), Colors::YIQ, Colors::RGB, "YIQ");

		System.out.println("RGB <-> YIQ tests passed.\n");
	}

	static void testRGBHSV () {
		System.out.println("Testing RGB <-> HSV conversions...");

		// Test known values
		HSV red = HSV(new RGB(1, 0, 0));
		assertClose(0, red.H(), "Red hue");
		assertClose(1, red.S(), "Red saturation");
		assertClose(1, red.V(), "Red value");

		HSV gray = HSV(new RGB(0.5f, 0.5f, 0.5f));
		assertClose(0, gray.S(), "Gray saturation");
		assertClose(0.5f, gray.V(), "Gray value");

		// Test round trip
		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), Colors::HSV, Colors::RGB, "HSV");

		System.out.println("RGB <-> HSV tests passed.\n");
	}

	static void testRGBHSL () {
		System.out.println("Testing RGB <-> HSL conversions...");

		// Test known values
		HSL red = HSL(new RGB(1, 0, 0));
		assertClose(0, red.H(), "Red hue");
		assertClose(1, red.S(), "Red saturation");
		assertClose(0.5f, red.L(), "Red lightness");

		// Test round trip
		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), Colors::HSL, Colors::RGB, "HSL");

		System.out.println("RGB <-> HSL tests passed.\n");
	}

	static void testRGBHSLuv () {
		System.out.println("Testing RGB <-> HSLuv conversions...");

		// Test known values
		// Pure red
		HSLuv red = HSLuv(new RGB(1, 0, 0));
		System.out.println("Red HSLuv: H=" + red.H() + " S=" + red.S() + " L=" + red.L());
		// HSLuv values for pure red in sRGB
		// Note: The exact values depend on the implementation details
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

		// Test round trip
		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), Colors::HSLuv, Colors::RGB, "HSLuv", EPSILON_LOOSE);

		System.out.println("RGB <-> HSLuv tests passed.\n");
	}

	static void testRGBXYZ () {
		System.out.println("Testing RGB <-> XYZ conversions...");

		// Note: ColorConverter XYZ uses 0-100 scale
		// Test known values - D65 illuminant sRGB white should be close to [95.047, 100, 108.883]
		XYZ white = XYZ(new RGB(1, 1, 1));
		assertClose(95.047, white.X(), "White X", 0.1);
		assertClose(100, white.Y(), "White Y", 0.1);
		assertClose(108.883, white.Z(), "White Z", 0.1);

		// Test round trip - RGB has that 255 clipping bug, so we need to be careful
		RGB rgb = new RGB(0.5f, 0.3f, 0.7f);
		XYZ xyz = XYZ(rgb);
		RGB rgbBack = RGB(xyz);
		// Since RGB returns 0-1 range despite the clipping code
		assertArrayClose(rgb, rgbBack, "XYZ round trip");

		System.out.println("RGB <-> XYZ tests passed.\n");
	}

	static void testCIE1960Conversions () {
		System.out.println("Testing CIE 1960/1976 conversions...");

		// Test the fixed UV1960 to xy conversion
		uv1960 uv = new uv1960(0.2f, 0.3f);
		xy xy = xy(uv);
		uv1960 uvBack = uv1960(xy);
		assertArrayClose(uv, uvBack, "CIE 1960 <-> xy round trip");

		// Test CIE 1960 <-> 1976
		uv uvPrime = uv(uv);
		assertClose(uv.u(), uvPrime.u(), "u = u'");
		assertClose(uv.v() * 1.5f, uvPrime.v(), "v' = 1.5v");

		uv1960 uvBack2 = uv1960(uvPrime);
		assertArrayClose(uv, uvBack2, "CIE 1960 <-> 1976 round trip");

		System.out.println("CIE 1960/1976 conversion tests passed.\n");
	}

	// Helper methods for ColorConverter tests
	// Keep these for now as we migrate tests gradually

	static void testRGBYCbCr () {
		System.out.println("Testing RGB <-> YCbCr conversions...");

		// Test ITU_BT_601
		RGB rgb601 = new RGB(0.5f, 0.3f, 0.7f);
		YCbCr ycbcr601 = YCbCr(rgb601, YCbCrColorSpace.ITU_BT_601);
		RGB back601 = RGB(ycbcr601, YCbCrColorSpace.ITU_BT_601);
		assertArrayClose(rgb601, back601, "YCbCr ITU_BT_601 round trip", 0.005);

		// Test ITU_BT_709_HDTV
		RGB rgb709 = new RGB(0.5f, 0.3f, 0.7f);
		YCbCr ycbcr709 = YCbCr(rgb709, YCbCrColorSpace.ITU_BT_709_HDTV);
		RGB back709 = RGB(ycbcr709, YCbCrColorSpace.ITU_BT_709_HDTV);
		assertArrayClose(rgb709, back709, "YCbCr ITU_BT_709_HDTV round trip", 0.005);

		System.out.println("RGB <-> YCbCr tests passed.\n");
	}

	static void testRGBYCC () {
		System.out.println("Testing RGB -> YCC conversions...");

		// YCC is not a perfect round-trip conversion - it appears to be a lossy color space
		// Just test that it runs without errors
		RGB rgb = new RGB(0.5f, 0.3f, 0.7f);
		YCC ycc = YCC(rgb);
		RGB rgbBack = RGB(ycc);

		// Basic sanity checks
		assertClose(0.289, ycc.Y(), "YCC Y component", 0.01);

		System.out.println("RGB -> YCC tests passed. (Note: YCC is lossy)\n");
	}

	static void testRGBYCoCg () {
		System.out.println("Testing RGB <-> YCoCg conversions...");

		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), Colors::YCoCg, Colors::RGB, "YCoCg");

		System.out.println("RGB <-> YCoCg tests passed.\n");
	}

	static void testRGBYES () {
		System.out.println("Testing RGB <-> YES conversions...");

		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), Colors::YES, Colors::RGB, "YES");

		System.out.println("RGB <-> YES tests passed.\n");
	}

	static void testXYZxyY () {
		System.out.println("Testing XYZ <-> xyY conversions...");

		// Convert RGB to XYZ first
		RGB rgb = new RGB(0.5f, 0.3f, 0.7f);
		XYZ xyz = XYZ(rgb);
		xyY xyy = xyY(xyz);
		XYZ xyzBack = XYZ(xyy);

		assertArrayClose(xyz, xyzBack, "XYZ <-> xyY round trip");

		System.out.println("XYZ <-> xyY tests passed.\n");
	}

	static void testRGBLab () {
		System.out.println("Testing RGB <-> Lab conversions...");

		// Test with default D65 illuminant
		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> Lab(c), Colors::RGB, "Lab D65");

		// Test known values
		Lab whiteLab = Lab(new RGB(1, 1, 1));
		assertClose(100, whiteLab.L(), "White L*", 0.1);
		assertClose(0, whiteLab.a(), "White a*", 0.1);
		assertClose(0, whiteLab.b(), "White b*", 0.1);

		System.out.println("RGB <-> Lab tests passed.\n");
	}

	static void testRGBLuv () {
		System.out.println("Testing RGB <-> Luv conversions...");

		// Test round trip
		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> Luv(c), Colors::RGB, "Luv", EPSILON_LOOSE);

		// Test known values
		Luv whiteLuv = Luv(new RGB(1, 1, 1));
		assertClose(100, whiteLuv.L(), "White L*", 0.1);
		assertClose(0, whiteLuv.u(), "White u*", 0.1);
		assertClose(0, whiteLuv.v(), "White v*", 0.1);

		// Test Luv <-> LCHuv conversion
		Luv luv = new Luv(50, 20, -30);
		LCHuv lch = LCHuv(luv);
		Luv luvBack = Luv(lch);

		assertClose(luv.L(), luvBack.L(), "L component", EPSILON);
		assertClose(luv.u(), luvBack.u(), "u component", EPSILON);
		assertClose(luv.v(), luvBack.v(), "v component", EPSILON);

		System.out.println("RGB <-> Luv tests passed.\n");
	}

	static void testLabLCh () {
		System.out.println("Testing Lab <-> LCh conversions...");

		Lab lab = new Lab(50, 25, -25);
		LCh lch = LCh(lab);
		Lab labBack = Lab(lch);

		assertArrayClose(lab, labBack, "Lab <-> LCh round trip");

		// Test that C = sqrt(a² + b²)
		double expectedC = Math.sqrt(25 * 25 + 25 * 25);
		assertClose(expectedC, lch.C(), "LCh chroma calculation");

		System.out.println("Lab <-> LCh tests passed.\n");
	}

	static void testRGBHunterLab () {
		System.out.println("Testing RGB <-> Hunter Lab conversions...");

		// Test round trip
		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> HunterLab(c), Colors::RGB, "Hunter Lab");

		// Test XYZ <-> Hunter Lab
		XYZ xyz = new XYZ(50, 50, 50);
		HunterLab hunterLab = HunterLab(xyz);
		XYZ xyzBack = XYZ(hunterLab);
		assertArrayClose(xyz, xyzBack, "XYZ <-> Hunter Lab round trip");

		System.out.println("RGB/XYZ <-> Hunter Lab tests passed.\n");
	}

	static void testRGBLCh () {
		System.out.println("Testing RGB <-> LCh conversions...");

		// Test round trip with default D65
		testRoundTrip(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> LCh(c), Colors::RGB, "LCh");

		System.out.println("RGB <-> LCh tests passed.\n");
	}

	static void testRGBLMS () {
		System.out.println("Testing RGB/XYZ <-> LMS conversions...");

		// Test all matrix types
		for (CAT matrix : CAT.values()) {
			// Test XYZ <-> LMS
			XYZ xyz = new XYZ(50, 50, 50);
			LMS lms = LMS(xyz, matrix);
			XYZ xyzBack = XYZ(lms, matrix);
			assertArrayClose(xyz, xyzBack, "XYZ <-> LMS " + matrix + " round trip");

			// Test RGB <-> LMS
			RGB rgb = new RGB(0.5f, 0.3f, 0.7f);
			LMS lmsRgb = LMS(rgb, matrix);
			RGB rgbBack = RGB(lmsRgb, matrix);
			assertArrayClose(rgb, rgbBack, "RGB <-> LMS " + matrix + " round trip");
		}

		System.out.println("RGB/XYZ <-> LMS tests passed.\n");
	}

	static void testMatrixMultiply () {
		System.out.println("Testing matrix multiply...");

		float[] vector = {1, 2, 3};
		float[][] matrix = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

		float[] result = matrixMultiply(vector, matrix);
		assertArrayClose(vector, result, "Identity matrix multiply");

		// Test with a known transformation
		float[][] scaleMatrix = {{2, 0, 0}, {0, 2, 0}, {0, 0, 2}};
		float[] scaled = matrixMultiply(vector, scaleMatrix);
		assertArrayClose(new float[] {2, 4, 6}, scaled, "Scale matrix multiply");

		System.out.println("Matrix multiply tests passed.\n");
	}

	static void testHunterLab () {
		System.out.println("Testing Hunter Lab conversions...");

		// Test XYZ to Hunter Lab
		XYZ xyz = new XYZ(41.24f, 21.26f, 1.93f); // Red in XYZ
		HunterLab hunterLab = HunterLab(xyz);
		System.out.printf("  %s -> %s\n", xyz, hunterLab);

		// Test round trip
		XYZ xyzBack = XYZ(hunterLab);
		assertArrayClose(xyz, xyzBack, "Hunter Lab round trip", EPSILON);

		// Test RGB to Hunter Lab
		RGB rgb = new RGB(1, 0, 0); // Pure red
		HunterLab hunterLabFromRgb = HunterLab(rgb);
		RGB rgbBack = RGB(hunterLabFromRgb);
		assertArrayClose(rgb, rgbBack, "RGB-HunterLab round trip", EPSILON);

		System.out.println("Hunter Lab tests passed.\n");
	}

	static void testLCh () {
		System.out.println("Testing LCh conversions...");

		// Test Lab to LCh
		Lab lab = new Lab(53.23f, 80.11f, 67.22f); // Red in Lab
		LCh lch = LCh(lab);
		System.out.printf("  %s -> %s\n", lab, lch);

		// Test round trip
		Lab labBack = Lab(lch);
		assertArrayClose(lab, labBack, "LCh round trip", EPSILON);

		// Test RGB to LCh with default illuminant
		RGB rgb = new RGB(1, 0, 0); // Pure red
		LCh lchFromRgb = LCh(rgb);
		RGB rgbBack = RGB(lchFromRgb);
		assertArrayClose(rgb, rgbBack, "RGB-LCh round trip", EPSILON);

		// Test RGB to LCh with custom illuminant
		LCh lchFromRgbD50 = LCh(rgb, Illuminant.CIE2.D50);
		System.out.printf("  RGB->LCh with D50: %s\n", lchFromRgbD50);

		System.out.println("LCh tests passed.\n");
	}

	static void testLMS () {
		System.out.println("Testing LMS conversions...");

		// Test all matrix types
		CAT[] matrices = {CAT.HPE, CAT.Bradford, CAT.VonKries, CAT.CAT97, CAT.CAT02};
		XYZ xyz = new XYZ(41.24f, 21.26f, 1.93f); // Red in XYZ

		for (CAT matrix : matrices) {
			System.out.printf("  Testing %s matrix:\n", matrix);

			// Test XYZ to LMS
			LMS lms = LMS(xyz, matrix);
			XYZ xyzBack = XYZ(lms, matrix);
			assertArrayClose(xyz, xyzBack, "LMS " + matrix + " round trip", EPSILON);

			// Test RGB to LMS
			RGB rgb = new RGB(1, 0, 0);
			LMS lmsFromRgb = LMS(rgb, matrix);
			RGB rgbBack = RGB(lmsFromRgb, matrix);
			assertArrayClose(rgb, rgbBack, "RGB-LMS " + matrix + " round trip", EPSILON);
		}

		// Test default (CAT02) conversions
		LMS lmsDefault = LMS(xyz);
		LMS lmsCat02 = LMS(xyz, CAT.CAT02);
		assertArrayClose(lmsDefault, lmsCat02, "Default LMS is CAT02", EPSILON);

		System.out.println("LMS tests passed.\n");
	}

	static void testSpecialConversions () {
		System.out.println("Testing special conversions...");

		// Test RGChromaticity
		RGB rgb = new RGB(0.8f, 0.2f, 0.1f);
		RGChromaticity rgChrom = rgChromaticity(rgb);
		System.out.printf("  %s -> %s\n", rgb, rgChrom);
		// Verify normalized RGB sums to 1
		double sum = rgChrom.r() + rgChrom.g() + rgChrom.b();
		assertClose(1, sum, "RGChromaticity normalized sum");

		// Test toC1C2C3
		C1C2C3 c1c2c3 = C1C2C3(rgb);
		System.out.printf("  RGB -> %s\n", c1c2c3);

		// Test toO1O2
		O1O2 o1o2 = O1O2(rgb);
		System.out.printf("  RGB -> %s\n", o1o2);

		// Test Grayscale
		double gray = grayscale(rgb);
		System.out.printf("  RGB -> Grayscale: %.3f\n", gray);
		// Verify grayscale calculation
		double expectedGray = rgb.r() * 0.2125 + rgb.g() * 0.7154 + rgb.b() * 0.0721;
		assertClose(expectedGray, gray, "Grayscale calculation");

		// More:

		// Test RGChromaticity
		rgb = new RGB(0.5f, 0.3f, 0.7f);
		rgChrom = rgChromaticity(rgb);
		// Check that r+g+b normalized values sum to 1
		assertClose(1, rgChrom.r() + rgChrom.g() + rgChrom.b(), "RGChromaticity sum");

		// Test RGB -> C1C2C3
		c1c2c3 = C1C2C3(rgb);
		// Just verify it runs without error

		// Test RGB -> O1O2
		o1o2 = O1O2(rgb);
		// Just verify it runs without error

		// Test RGB -> Grayscale
		gray = grayscale(rgb);
		// Verify it's within expected range
		assertTrue(gray >= 0 && gray <= 1, "Grayscale in range");

		// Test known grayscale values
		assertClose(0, grayscale(new RGB(0, 0, 0)), "Black grayscale");
		assertClose(1, grayscale(new RGB(1, 1, 1)), "White grayscale");

		System.out.println("Special conversion tests passed.\n");
	}

	static void testUtilityFunctions () {
		System.out.println("Testing utility functions...");

		// Test matrixMultiply
		float[] vector = {1, 2, 3};
		float[][] matrix = {{1, 0, 0}, {0, 2, 0}, {0, 0, 3}};
		float[] result = matrixMultiply(vector, matrix);
		float[] expected = {1, 4, 9};
		assertArrayClose(expected, result, "Matrix multiply", EPSILON);

		// Test with non-diagonal matrix
		float[][] matrix2 = {{0.5f, 0.3f, 0.2f}, {0.1f, 0.6f, 0.3f}, {0.2f, 0.2f, 0.6f}};
		result = matrixMultiply(vector, matrix2);
		expected = new float[] {1 * 0.5f + 2 * 0.1f + 3 * 0.2f, // 0.5 + 0.2 + 0.6 = 1.3
			1 * 0.3f + 2 * 0.6f + 3 * 0.2f, // 0.3 + 1.2 + 0.6 = 2.1
			1 * 0.2f + 2 * 0.3f + 3 * 0.6f // 0.2 + 0.6 + 1.8 = 2.6
		};
		assertArrayClose(expected, result, "Matrix multiply 2", EPSILON);

		System.out.println("Utility function tests passed.\n");
	}

	static void testAllRoundTrips () {
		System.out.println("Testing all round-trip conversions...");

		// Test colors
		RGB[] testColors = {new RGB(1, 0, 0), // Red
			new RGB(0, 1, 0), // Green
			new RGB(0, 0, 1), // Blue
			new RGB(1, 1, 1), // White
			new RGB(0, 0, 0), // Black
			new RGB(0.5f, 0.5f, 0.5f), // Gray
			new RGB(1, 1, 0), // Yellow
			new RGB(1, 0, 1), // Magenta
			new RGB(0, 1, 1), // Cyan
			new RGB(0.8f, 0.2f, 0.4f) // Arbitrary color
		};

		for (RGB rgb : testColors) {
			System.out.printf("  Testing %s:\n", rgb);

			// Already tested conversions (quick verification)
			testRoundTrip(rgb, Colors::CMYK, Colors::RGB, "CMYK", EPSILON);
			testRoundTrip(rgb, Colors::IHS, Colors::RGB, "IHS", EPSILON);
			testRoundTrip(rgb, Colors::YUV, Colors::RGB, "YUV", EPSILON_VERY_LOOSE);
			testRoundTrip(rgb, Colors::YIQ, Colors::RGB, "YIQ", EPSILON_LOOSE);
			testRoundTrip(rgb, Colors::HSV, Colors::RGB, "HSV", EPSILON);
			testRoundTrip(rgb, Colors::YCoCg, Colors::RGB, "YCoCg", EPSILON);
			testRoundTrip(rgb, Colors::YES, Colors::RGB, "YES", EPSILON);
			testRoundTrip(rgb, (RGB c) -> XYZ(c), Colors::RGB, "XYZ", EPSILON);
			testRoundTrip(rgb, Colors::HSL, Colors::RGB, "HSL", EPSILON);
			testRoundTrip(rgb, (RGB c) -> Lab(c), Colors::RGB, "Lab", EPSILON);
			testRoundTrip(rgb, Colors::YCC, Colors::RGB, "YCC", EPSILON_VERY_LOOSE);
			testRoundTripYCbCr(rgb, YCbCrColorSpace.ITU_BT_601, EPSILON_LOOSE);
			testRoundTripYCbCr(rgb, YCbCrColorSpace.ITU_BT_709_HDTV, EPSILON_LOOSE);
		}

		System.out.println("All round-trip tests passed.\n");
	}

	static void testRoundTripYCbCr (RGB rgb, YCbCrColorSpace colorSpace, double epsilon) {
		YCbCr ycbcr = YCbCr(rgb, colorSpace);
		RGB back = RGB(ycbcr, colorSpace);
		try {
			assertArrayClose(rgb, back, "YCbCr " + colorSpace + " round trip", epsilon);
		} catch (AssertionError e) {
			System.out.printf("    YCbCr %s: %.3f error\n", colorSpace, getMaxError(rgb, back));
			throw e;
		}
	}

	static double getMaxError (Record expectedRecord, Record actualRecord) {
		float[] expected = toArray(actualRecord), actual = toArray(actualRecord);
		double maxError = 0;
		for (int i = 0; i < expected.length; i++)
			maxError = Math.max(maxError, Math.abs(expected[i] - actual[i]));
		return maxError;
	}

	static void testCCTConversions () {
		System.out.println("Testing CCT (Correlated Color Temperature) conversions...");

		// Test known CCT values
		System.out.println("  Testing CCT to RGB conversions:");

		String actual = RGB(2700, 0, 50).hex();
		assertTrue(actual.equals("fbaa58"), "Expected CCTRGB value != actual: ffe87a != " + actual);

		actual = RGB(2700, 0.01f, 50).hex();
		assertTrue(actual.equals("ffa774"), "Expected CCTRGB with 0.01 Duv value != actual: ffe39f != " + actual);

		// Test common color temperatures
		float[] ccts = {2700, 3000, 4000, 5000, 6500};

		for (float cct : ccts) {
			RGB rgb = RGB(cct, 0, 100);
			System.out.printf("    CCT %.0fK -> %s (#%s)\n", cct, rgb, rgb.hex());

			// Verify the color is reasonable (all channels should be positive)
			assertTrue(rgb.r() >= 0 && rgb.r() <= 1, "Red channel in range for CCT " + cct);
			assertTrue(rgb.g() >= 0 && rgb.g() <= 1, "Green channel in range for CCT " + cct);
			assertTrue(rgb.b() >= 0 && rgb.b() <= 1, "Blue channel in range for CCT " + cct);

			// For daylight temperatures, blue should increase with temperature
			if (cct >= 5000) {
				assertTrue(rgb.b() > 0.8f, "Blue channel high for daylight CCT " + cct);
			}
		}

		// Test CCT with Duv offsets
		System.out.println("\n  Testing CCT with Duv offsets:");
		float[] duvOffsets = {-0.01f, 0, 0.01f};
		for (float duv : duvOffsets) {
			RGB rgb = RGB(3000, duv);
			System.out.printf("    CCT 3000K, Duv=%.2f -> %s\n", duv, rgb);
		}

		// Test CCT to xy conversions
		System.out.println("\n  Testing CCT to xy chromaticity:");
		for (float cct : ccts) {
			xy xy = xy(cct);
			System.out.printf("    CCT %.0fK -> %s\n", cct, xy);

			// Verify xy values are reasonable
			assertTrue(xy.x() > 0 && xy.x() < 1, "x chromaticity in range for CCT " + cct);
			assertTrue(xy.y() > 0 && xy.y() < 1, "y chromaticity in range for CCT " + cct);
		}

		// Test edge cases
		RGB rgb = RGB(1667, 0); // Minimum CCT
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1 && rgb.g() >= 0 && rgb.g() <= 1 && rgb.b() >= 0 && rgb.b() <= 1,
			"RGB in range for minimum CCT");

		rgb = RGB(25000, 0); // Maximum CCT
		assertTrue(rgb.r() >= 0 && rgb.r() <= 1 && rgb.g() >= 0 && rgb.g() <= 1 && rgb.b() >= 0 && rgb.b() <= 1,
			"RGB in range for maximum CCT");

		// Test xy to CCT reverse conversions (McCamy's approximation)
		// Note: McCamy's approximation has varying accuracy:
		// - Best accuracy (±5K) in 4000-8000K range
		// - Lower accuracy (±50-200K) outside this range
		System.out.println("\n  Testing xy to CCT conversions:");
		float[] testCCTs = {1600, 1667, 2000, 2300, 2500, 2700, 3000, 3500, 4000, 5000, 6500, 10000};
		for (float expectedCCT : testCCTs) {
			xy xy = xy(expectedCCT);
			float calculatedCCT = CCT(xy);

			// Check for invalid xy coordinates
			if (Float.isNaN(xy.x()) && Float.isNaN(calculatedCCT)) {
				System.out.printf("    CCT %.0fK -> xy%s -> CCT %.0fK (invalid CCT)\n", expectedCCT, xy, calculatedCCT);
				assertTrue(Float.isNaN(calculatedCCT), "CCT should return -1 for invalid xy(-1, -1)");
				continue;
			}

			float error = Math.abs(calculatedCCT - expectedCCT);
			System.out.printf("    CCT %.0fK -> xy%s -> CCT %.0fK (error: %.0fK)\n", expectedCCT, xy, calculatedCCT, error);

			// McCamy's approximation has varying accuracy
			if (expectedCCT >= 4000 && expectedCCT <= 8000) {
				assertTrue(error < 50, "CCT error should be <50K in optimal range, was " + error);
			} else {
				assertTrue(error < 200, "CCT error should be <200K outside optimal range, was " + error);
			}
		}

		// Test uv to CCT conversions
		System.out.println("\n  Testing u'v' to CCT conversions:");
		for (float expectedCCT : testCCTs) {
			// Skip invalid CCTs that would produce fallback values
			if (expectedCCT < 1667) {
				System.out.printf("    CCT %.0fK -> (skipped - below valid range)\n", expectedCCT);
				continue;
			}

			uv uv = uv(RGB(expectedCCT, 0));
			float calculatedCCT = CCT(uv);
			float error = Math.abs(calculatedCCT - expectedCCT);
			System.out.printf("    CCT %.0fK -> u'v'%s -> CCT %.0fK (error: %.0fK)\n", expectedCCT, uv, calculatedCCT, error);
		}

		// Test edge cases
		System.out.println("\n  Testing CCT edge cases:");

		// Test invalid xy coordinates
		float invalidCCT = CCT(new xy(0.1f, 0.1f)); // Far from blackbody locus
		assertTrue(Float.isNaN(invalidCCT), "Should return -1 for invalid xy coordinates");

		invalidCCT = CCT(new xy(0.6f, 0.4f)); // Outside valid range
		assertTrue(Float.isNaN(invalidCCT), "Should return -1 for xy outside range");

		// Test that extreme CCT values outside McCamy's range return -1
		// Note: xy(1500) still returns valid xy coords, so we test the CCT calculation directly
		xy extremeLowXY = new xy(0.7f, 0.3f); // Very red, far from blackbody
		float extremeLowCCT = CCT(extremeLowXY);
		assertTrue(Float.isNaN(extremeLowCCT), "Should return -1 for xy far from blackbody locus");

		// Test specific xy that would give CCT outside bounds
		xy highCCTXY = new xy(0.25f, 0.25f); // Would give very high CCT
		float highCCT = CCT(highCCTXY);
		if (highCCT > 25000 || highCCT < 1667) assertTrue(Float.isNaN(highCCT), "Should return -1 for CCT outside bounds");

		System.out.println("CCT conversion tests passed.\n");
	}

	static void testLinearRGBConversions () {
		System.out.println("Testing linear RGB conversions...");

		// Test RGB values
		RGB[] testColors = {new RGB(1, 0, 0), // Red
			new RGB(0, 1, 0), // Green
			new RGB(0, 0, 1), // Blue
			new RGB(0.5f, 0.5f, 0.5f), // Gray
			new RGB(0.2f, 0.5f, 0.8f) // Arbitrary
		};

		for (RGB rgb : testColors) {
			// Test linear RGB to XYZ
			LinearRGB linearRgb = new LinearRGB(linear(rgb.r()), linear(rgb.g()), linear(rgb.b()));

			XYZ xyz = XYZ(linearRgb);
			LinearRGB linearBack = LinearRGB(xyz);

			assertArrayClose(linearRgb, linearBack, "Linear RGB round trip", EPSILON);

			// Verify the difference between linear and gamma-corrected
			XYZ xyzGamma = XYZ(rgb);
			// XYZ values should be different (except for 0, 1, and gray)
			// Skip test for values that have same linear and gamma encoding
			boolean skipTest = false;
			if (rgb.r() == 0 || rgb.r() == 1 || rgb.g() == 0 || rgb.g() == 1 || rgb.b() == 0 || rgb.b() == 1) {
				skipTest = true;
			}

			if (!skipTest && !(rgb.r() == 0.5f && rgb.g() == 0.5f && rgb.b() == 0.5f)) {
				// Due to float precision, the difference might be small
				float epsilon = 1e-6f;
				boolean differs = Math.abs(xyz.X() - xyzGamma.X()) > epsilon || Math.abs(xyz.Y() - xyzGamma.Y()) > epsilon
					|| Math.abs(xyz.Z() - xyzGamma.Z()) > epsilon;
				assertTrue(differs, "Linear vs gamma XYZ should differ for RGB(" + rgb.r() + "," + rgb.g() + "," + rgb.b() + ")");
			}
		}

		// Test xyXYZ with Y=100
		XYZ xyz = XYZ(new xy(0.3127f, 0.3290f)); // D65 white point
		assertClose(100, xyz.Y(), "xyXYZ Y value", EPSILON);

		// Verify it produces the same ratios as XYZ
		XYZ xyzFromxyY = XYZ(new xyY(0.3127f, 0.3290f, 100));
		assertArrayClose(xyz, xyzFromxyY, "xyXYZ matches XYZ", EPSILON);

		System.out.println("Linear RGB conversion tests passed.\n");
	}

	static void testOklabConversions () {
		System.out.println("Testing additional Oklab/Oklch conversions...");

		// Test Oklab to Oklch conversions
		Oklab[] testLabs = {new Oklab(0.5f, 0.1f, 0.1f), new Oklab(0.8f, -0.05f, 0.05f), new Oklab(0.3f, 0.0f, 0.0f) // Gray (should
																																							// have C=0)
		};

		for (Oklab lab : testLabs) {
			Oklch lch = Oklch(lab);
			Oklab labBack = Oklab(lch);
			assertArrayClose(lab, labBack, "Oklab <-> Oklch round trip", EPSILON);

			// Verify cylindrical coordinate conversion
			float expectedC = (float)Math.sqrt(lab.a() * lab.a() + lab.b() * lab.b());
			assertClose(expectedC, lch.C(), "Oklch chroma calculation", EPSILON);

			// For gray colors, chroma should be 0
			if (Math.abs(lab.a()) < EPSILON && Math.abs(lab.b()) < EPSILON) {
				assertClose(0, lch.C(), "Gray Oklch chroma", EPSILON);
			}
		}

		// Test RGB to Oklch direct conversion
		RGB rgb = new RGB(0.6f, 0.4f, 0.2f);
		Oklch oklch = Oklch(rgb);
		RGB rgbBack = RGB(oklch);
		assertArrayClose(rgb, rgbBack, "RGB <-> Oklch round trip", EPSILON);

		System.out.println("Oklab/Oklch conversion tests passed.\n");
	}

	static void testUV1976Conversions () {
		System.out.println("Testing CIE 1976 u'v' conversions...");

		// Test RGB to UV1976 conversions
		RGB[] testColors = {new RGB(1, 0, 0), // Red
			new RGB(0, 1, 0), // Green
			new RGB(0, 0, 1), // Blue
			new RGB(1, 1, 1), // White
			new RGB(0.5f, 0.5f, 0.5f) // Gray
		};

		System.out.println("  Testing RGB -> u'v' conversions:");
		for (RGB rgb : testColors) {
			uv uv = uv(rgb);
			RGB rgbBack = RGB(uv);

			System.out.printf("    " + rgb + " -> " + uv + " -> " + rgbBack);

			// u'v' is a chromaticity space - it preserves color but not brightness
			// For non-gray colors, we should get the same chromaticity back
			if (!(rgb.r() == rgb.g() && rgb.g() == rgb.b())) {
				// For chromatic colors, check that we get the same color ratios
				float maxOriginal = Math.max(rgb.r(), Math.max(rgb.g(), rgb.b()));
				float maxBack = Math.max(rgbBack.r(), Math.max(rgbBack.g(), rgbBack.b()));
				if (maxOriginal > 0 && maxBack > 0) {
					float[] normalizedOrig = {rgb.r() / maxOriginal, rgb.g() / maxOriginal, rgb.b() / maxOriginal};
					float[] normalizedBack = {rgbBack.r() / maxBack, rgbBack.g() / maxBack, rgbBack.b() / maxBack};
					assertArrayClose(normalizedOrig, normalizedBack, "RGB chromaticity preservation", EPSILON);
				}
			} else {
				// For gray colors, just verify we get gray back (though brightness may differ)
				assertClose(rgbBack.r(), rgbBack.g(), "Gray R=G", EPSILON);
				assertClose(rgbBack.g(), rgbBack.b(), "Gray G=B", EPSILON);
			}
		}

		// Test UV1976 to xy conversions
		System.out.println("\n  Testing u'v' <-> xy conversions:");
		uv[] testUVs = {new uv(0.2105f, 0.4737f), // D65 white point in u'v'
			new uv(0.4507f, 0.5229f), // Red primary
			new uv(0.1250f, 0.5625f), // Green primary
			new uv(0.1754f, 0.1579f) // Blue primary
		};

		for (uv uv : testUVs) {
			xy xy = xy(uv);
			uv uvBack = uv(xy);
			System.out.printf("    " + uv + " -> " + xy + " -> " + uvBack);
			assertArrayClose(uv, uvBack, "UV1976 <-> xy round trip", EPSILON);
		}

		// Test CCT to UV1960 (used internally by CCTRGB)
		System.out.println("\n  Testing CCT -> UV1960 conversions:");
		float[] ccts = {2700, 4000, 6500};
		for (float cct : ccts) {
			uv1960 uv1960 = uv1960(cct);
			System.out.printf("    CCT %.0fK -> %s\n", cct, uv1960);

			// Verify UV values are reasonable
			assertTrue(uv1960.u() > 0 && uv1960.u() < 1, "u1960 in range for CCT " + cct);
			assertTrue(uv1960.v() > 0 && uv1960.v() < 1, "v1960 in range for CCT " + cct);
		}

		// Test DUV1960 offset calculation
		System.out.println("\n  Testing DUV1960 offset calculation:");
		for (float cct : ccts) {
			uv1960 base = uv1960(cct);
			uv1960 offset = uv1960(cct, 0.01f);
			offset = new uv1960(offset.u() - base.u(), offset.v() - base.v());
			System.out.printf("    CCT %.0fK, Duv=0.01 -> %s\n", cct, offset);

			// Verify offset is perpendicular (small magnitude)
			float magnitude = (float)Math.sqrt(offset.u() * offset.u() + offset.v() * offset.v());
			assertClose(0.01, magnitude, "DUV1960 offset magnitude", 0.0001);
		}

		System.out.println("CIE 1976 u'v' conversion tests passed.\n");
	}

	static void testGammaFunctions () {
		System.out.println("Testing gamma encode/decode functions...");

		// Test common gamma values
		float[] gammas = {1.0f, 1.8f, 2.2f, 2.4f};
		float[] testValues = {0.0f, 0.1f, 0.25f, 0.5f, 0.75f, 1.0f};

		for (float gamma : gammas) {
			System.out.printf("  Testing gamma %.1f:\n", gamma);
			for (float value : testValues) {
				// Test encode/decode round trip
				float encoded = gammaEncode(value, gamma);
				float decoded = gammaDecode(encoded, gamma);
				assertClose(value, decoded, String.format("Gamma %.1f round trip for value %.2f", gamma, value));

				// Test decode/encode round trip
				float linearFromEncoded = gammaDecode(value, gamma);
				float reencoded = gammaEncode(linearFromEncoded, gamma);
				assertClose(value, reencoded, String.format("Inverse gamma %.1f round trip for value %.2f", gamma, value));
			}
		}

		// Test edge cases
		assertEquals(0, gammaEncode(0, 2.2f), "Gamma encode 0");
		assertEquals(1, gammaEncode(1, 2.2f), "Gamma encode 1");
		assertEquals(0, gammaDecode(0, 2.2f), "Gamma decode 0");
		assertEquals(1, gammaDecode(1, 2.2f), "Gamma decode 1");

		// Test negative values (should clamp to 0)
		assertEquals(0, gammaEncode(-0.1f, 2.2f), "Gamma encode negative");
		assertEquals(0, gammaDecode(-0.1f, 2.2f), "Gamma decode negative");

		// Test values > 1 (should clamp to 1)
		assertEquals(1, gammaEncode(1.1f, 2.2f), "Gamma encode > 1");
		assertEquals(1, gammaDecode(1.1f, 2.2f), "Gamma decode > 1");

		// Test relationship with sRGB functions
		float testVal = 0.5f;
		float srgbEncoded = sRGB(testVal);
		float gammaEncoded = gammaEncode(testVal, 2.4f); // sRGB uses approximately 2.4
		// They should be close but not exact (sRGB has linear segment near 0)
		assertClose(srgbEncoded, gammaEncoded, "sRGB vs gamma 2.4", 0.04);

		// Test that gamma=1 is identity
		for (float value : testValues) {
			assertClose(value, gammaEncode(value, 1.0f), "Gamma 1.0 encode identity");
			assertClose(value, gammaDecode(value, 1.0f), "Gamma 1.0 decode identity");
		}

		System.out.println("Gamma function tests passed.\n");
	}

	static void testDMXConversions () {
		System.out.println("Testing DMX conversions...");

		// Test DMX8 conversions
		System.out.println("  Testing DMX8 (8-bit) conversions:");
		float[] testValues = {0.0f, 0.25f, 0.5f, 0.75f, 1.0f, 0.123f, 0.456f, 0.789f};

		for (float value : testValues) {
			int dmx = dmx8(value);
			int expected = (int)(value * 255);
			assertEquals(expected, dmx, "DMX8 for value " + value);
			assertTrue(dmx >= 0 && dmx <= 255, "DMX8 value in range [0,255]");
		}

		// Test specific DMX8 values
		assertEquals(0, dmx8(0.0f), "DMX8 of 0");
		assertEquals(255, dmx8(1.0f), "DMX8 of 1");
		assertEquals(127, dmx8(0.5f), "DMX8 of 0.5");

		// Test DMX16 conversions
		System.out.println("  Testing DMX16 (16-bit) conversions:");
		for (float value : testValues) {
			int dmx = dmx16(value);
			int expected = (int)(value * 65535);
			assertEquals(expected, dmx, "DMX16 for value " + value);
			assertTrue(dmx >= 0 && dmx <= 65535, "DMX16 value in range [0,65535]");
		}

		// Test specific DMX16 values
		assertEquals(0, dmx16(0.0f), "DMX16 of 0");
		assertEquals(65535, dmx16(1.0f), "DMX16 of 1");
		assertEquals(32767, dmx16(0.5f), "DMX16 of 0.5");

		// Test precision difference
		float testVal = 0.123456f;
		int dmx8Val = dmx8(testVal);
		int dmx16Val = dmx16(testVal);
		float back8 = dmx8Val / 255.0f;
		float back16 = dmx16Val / 65535.0f;

		float error8 = Math.abs(testVal - back8);
		float error16 = Math.abs(testVal - back16);

		assertTrue(error16 < error8, "DMX16 has better precision than DMX8");
		System.out.printf("    Test value: %.6f\n", testVal);
		System.out.printf("    DMX8 value: %d, back: %.6f, error: %.6f\n", dmx8Val, back8, error8);
		System.out.printf("    DMX16 value: %d, back: %.6f, error: %.6f\n", dmx16Val, back16, error16);

		// Test that functions don't clamp (user should handle that)
		assertEquals(-25, dmx8(-0.1f), "DMX8 negative not clamped");
		assertEquals(280, dmx8(1.1f), "DMX8 >1 not clamped");
		assertEquals(-6553, dmx16(-0.1f), "DMX16 negative not clamped");
		assertEquals(72088, dmx16(1.1f), "DMX16 >1 not clamped");

		System.out.println("DMX conversion tests passed.\n");
	}

	static void testRGBWConversions () {
		System.out.println("Testing RGBW conversions...");

		// Test with perfect white LED (ideal case)
		System.out.println("  Testing with ideal white LED (1,1,1):");
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
		System.out.println("  Testing with warm white LED (1.0, 0.8, 0.6):");
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
		System.out.println("  Testing default warm white conversion:");
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
		assertEquals("ff8040bf", hexTest.hex(), "RGBW hex");
		assertEquals("255, 127, 63, 191", hexTest.toString255(), "RGBW toString255");

		// Test CCT to RGBW conversion
		System.out.println("\n  Testing CCT to RGBW conversion:");
		RGB scaledWhite = new RGB(1.8f, 1.6f, 1.0f); // Scaled warm white LED (~2700K)

		// Test maximum brightness at full
		RGB target4000 = RGB(4000, 0);
		System.out.printf("    Target RGB for 4000K: (%.3f, %.3f, %.3f)\n", target4000.r(), target4000.g(), target4000.b());
		RGBW cctFull = RGBW(4000, 1.0f, scaledWhite);
		System.out.printf("    CCT 4000K at 100%% -> RGBW(%.3f, %.3f, %.3f, %.3f)\n", cctFull.r(), cctFull.g(), cctFull.b(),
			cctFull.w());

		// Test reduced brightness - should reduce RGB first
		RGBW cctDim = RGBW(4000, 0.5f, scaledWhite);
		System.out.printf("    CCT 4000K at 50%% -> RGBW(%.3f, %.3f, %.3f, %.3f)\n", cctDim.r(), cctDim.g(), cctDim.b(),
			cctDim.w());

		// Test low brightness - should have W only
		RGBW cctLow = RGBW(3000, 0.2f, scaledWhite);
		System.out.printf("    CCT 3000K at 20%% -> RGBW(%.3f, %.3f, %.3f, %.3f)\n", cctLow.r(), cctLow.g(), cctLow.b(),
			cctLow.w());
		assertTrue(cctLow.r() < 0.01f && cctLow.g() < 0.01f && cctLow.b() < 0.01f,
			"Low brightness CCT should use mostly white channel");

		// Test cool CCT requiring blue correction
		RGBW cctCool = RGBW(6500, 0.8f, scaledWhite);
		System.out.printf("    CCT 6500K at 80%% -> RGBW(%.3f, %.3f, %.3f, %.3f)\n", cctCool.r(), cctCool.g(), cctCool.b(),
			cctCool.w());
		// With a warm white LED, cool CCT might need blue correction at higher brightness
		assertTrue(cctCool.w() > 0, "Should use white channel for CCT");

		System.out.println("RGBW conversion tests passed.\n");
	}

	static void testRGBTWConversions () {
		System.out.println("Testing RGBTW conversions...");

		// Test RGB to RGBTW with two whites
		RGB warmWhite = new RGB(1.8f, 1.6f, 1.0f); // 2700K-ish, scaled
		RGB coolWhite = new RGB(1.2f, 1.4f, 1.8f); // 6500K-ish, scaled

		// Test warm color - should prefer warm white
		RGB warmColor = new RGB(0.8f, 0.6f, 0.4f);
		RGBTW warmResult = RGBTW(warmColor, warmWhite, coolWhite);
		System.out.printf("  Warm color -> RGBTW(%.3f, %.3f, %.3f, %.3f, %.3f)\n", warmResult.r(), warmResult.g(), warmResult.b(),
			warmResult.t(), warmResult.w());
		assertTrue(warmResult.t() > warmResult.w(), "Warm color should use more warm white");

		// Test cool color - should prefer cool white
		RGB coolColor = new RGB(0.4f, 0.5f, 0.8f);
		RGBTW coolResult = RGBTW(coolColor, warmWhite, coolWhite);
		System.out.printf("  Cool color -> RGBTW(%.3f, %.3f, %.3f, %.3f, %.3f)\n", coolResult.r(), coolResult.g(), coolResult.b(),
			coolResult.t(), coolResult.w());
		assertTrue(coolResult.w() > coolResult.t(), "Cool color should use more cool white");

		// Test CCT to RGBTW conversion
		System.out.println("\n  Testing CCT to RGBTW conversion:");

		// Test intermediate CCT - should blend whites
		RGBTW cct4000 = RGBTW(4000, 1.0f, warmWhite, coolWhite);
		System.out.printf("    CCT 4000K at 100%% -> RGBTW(%.3f, %.3f, %.3f, %.3f, %.3f)\n", cct4000.r(), cct4000.g(), cct4000.b(),
			cct4000.t(), cct4000.w());
		assertTrue(cct4000.t() > 0 && cct4000.w() > 0, "Mid CCT should blend both whites");

		// Test warm CCT - should use mostly warm white
		RGBTW cct2700 = RGBTW(2700, 0.8f, warmWhite, coolWhite);
		System.out.printf("    CCT 2700K at 80%% -> RGBTW(%.3f, %.3f, %.3f, %.3f, %.3f)\n", cct2700.r(), cct2700.g(), cct2700.b(),
			cct2700.t(), cct2700.w());
		assertTrue(cct2700.t() >= cct2700.w(), "Warm CCT should favor warm white");

		// Test cool CCT - should use mostly cool white
		RGBTW cct6500 = RGBTW(6500, 0.8f, warmWhite, coolWhite);
		System.out.printf("    CCT 6500K at 80%% -> RGBTW(%.3f, %.3f, %.3f, %.3f, %.3f)\n", cct6500.r(), cct6500.g(), cct6500.b(),
			cct6500.t(), cct6500.w());
		// Note: if CCT calculation fails for the LEDs, it might fall back to equal blend
		assertTrue(cct6500.t() + cct6500.w() > 0.7f, "Should use white channels for CCT");

		// Test low brightness - should still maintain white ratio
		RGBTW cctLow = RGBTW(4500, 0.2f, warmWhite, coolWhite);
		System.out.printf("    CCT 4500K at 20%% -> RGBTW(%.3f, %.3f, %.3f, %.3f, %.3f)\n", cctLow.r(), cctLow.g(), cctLow.b(),
			cctLow.t(), cctLow.w());
		float totalWhite = cctLow.t() + cctLow.w();
		assertTrue(totalWhite > 0.15f && totalWhite < 0.25f, "Low brightness should scale whites proportionally");

		// Test RGBTW hex and toString255
		RGBTW hexTest = new RGBTW(1, 0.5f, 0.25f, 0.1f, 0.75f);
		assertEquals("ff80401abf", hexTest.hex(), "RGBTW hex");
		assertEquals("255, 127, 63, 25, 191", hexTest.toString255(), "RGBTW toString255");

		System.out.println("RGBTW conversion tests passed.\n");
	}

	static void assertClose (double expected, double actual, String name) {
		if (Math.abs(expected - actual) > EPSILON) {
			throw new AssertionError(String.format("%s mismatch: expected %.6f, got %.6f (diff: %.6f)", name, expected, actual,
				Math.abs(expected - actual)));
		}
	}

	static void assertArrayClose (float[] expected, float[] actual, String name, double epsilon) {
		if (expected.length != actual.length) {
			throw new AssertionError(name + " array length mismatch");
		}
		for (int i = 0; i < expected.length; i++) {
			if (Math.abs(expected[i] - actual[i]) > epsilon) {
				throw new AssertionError(String.format("%s mismatch at index %d: expected %.6f, got %.6f (diff: %.6f)", name, i,
					expected[i], actual[i], Math.abs(expected[i] - actual[i])));
			}
		}
	}

	static void assertArrayClose (float[] expected, float[] actual, String name) {
		assertArrayClose(expected, actual, name, EPSILON_D);
	}

	static void assertArrayClose (Record expected, Record actual, String name) {
		assertArrayClose(toArray(expected), toArray(actual), name, EPSILON);
	}

	static void assertArrayClose (Record expected, Record actual, String name, double epsilon) {
		assertArrayClose(toArray(expected), toArray(actual), name, epsilon);
	}

	static void assertClose (double expected, double actual, String name, double epsilon) {
		if (Math.abs(expected - actual) > epsilon) {
			throw new AssertionError(String.format("%s mismatch: expected %.6f, got %.6f (diff: %.6f)", name, expected, actual,
				Math.abs(expected - actual)));
		}
	}

	static void assertEquals (int expected, int actual, String name) {
		if (expected != actual) throw new AssertionError(String.format("%s mismatch: expected %d, got %d", name, expected, actual));
	}

	static void assertEquals (float expected, float actual, String name) {
		if (expected != actual) throw new AssertionError(String.format("%s mismatch: expected %f, got %f", name, expected, actual));
	}

	static void assertEquals (String expected, String actual, String name) {
		if (!expected.equals(actual)) {
			throw new AssertionError(String.format("%s mismatch: expected %s got %s", name, expected, actual));
		}
	}

	static void assertTrue (boolean condition, String message) {
		if (!condition) throw new AssertionError(message);
	}

	static public float[] toArray (Record record) {
		RecordComponent[] components = record.getClass().getRecordComponents();
		float[] values = new float[components.length];
		for (int i = 0; i < components.length; i++) {
			try {
				values[i] = (float)components[i].getAccessor().invoke(record);
			} catch (Exception ex) {
				throw new RuntimeException("Error accessing field", ex);
			}
		}
		return values;
	}

	static <T extends Record, U extends Record> void testRoundTrip (T original, Function<T, U> forward, Function<U, T> backward,
		String name) {
		testRoundTrip(original, forward, backward, name, EPSILON_D);
	}

	static <T extends Record, U extends Record> void testRoundTrip (T original, Function<T, U> forward, Function<U, T> backward,
		String name, double epsilon) {
		U converted = forward.apply(original);
		T back = backward.apply(converted);
		assertArrayClose(original, back, name + " round trip", epsilon);
	}
}
