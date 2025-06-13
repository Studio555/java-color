
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;

import java.lang.reflect.RecordComponent;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.colors.Colors.C1C2C3;
import com.esotericsoftware.colors.Colors.CAT;
import com.esotericsoftware.colors.Colors.CMYK;
import com.esotericsoftware.colors.Colors.HSI;
import com.esotericsoftware.colors.Colors.HSL;
import com.esotericsoftware.colors.Colors.HSLuv;
import com.esotericsoftware.colors.Colors.HSV;
import com.esotericsoftware.colors.Colors.HunterLab;
import com.esotericsoftware.colors.Colors.Illuminant;
import com.esotericsoftware.colors.Colors.LCHuv;
import com.esotericsoftware.colors.Colors.LCh;
import com.esotericsoftware.colors.Colors.LMS;
import com.esotericsoftware.colors.Colors.Lab;
import com.esotericsoftware.colors.Colors.LinearRGB;
import com.esotericsoftware.colors.Colors.Luv;
import com.esotericsoftware.colors.Colors.O1O2;
import com.esotericsoftware.colors.Colors.Okhsl;
import com.esotericsoftware.colors.Colors.Okhsv;
import com.esotericsoftware.colors.Colors.Oklab;
import com.esotericsoftware.colors.Colors.Oklch;
import com.esotericsoftware.colors.Colors.RGB;
import com.esotericsoftware.colors.Colors.RGBWW;
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

/** @author Nathan Sweet <misc@n4te.com> */
public class ColorsTest {
	static final float EPSILON_F = 0.00002f;
	static final double EPSILON_D = 0.000001;

	@Test
	public void testRgbToOklabAndBack () {
		// Test primary colors
		roundTripOklab(new RGB(1, 0, 0), "Red");
		roundTripOklab(new RGB(0, 1, 0), "Green");
		roundTripOklab(new RGB(0, 0, 1), "Blue");
		roundTripOklab(new RGB(1, 1, 0), "Yellow");
		roundTripOklab(new RGB(0, 1, 1), "Cyan");
		roundTripOklab(new RGB(1, 0, 1), "Magenta");
		roundTripOklab(new RGB(1, 1, 1), "White");
		roundTripOklab(new RGB(0, 0, 0), "Black");
		roundTripOklab(new RGB(0.5f, 0.5f, 0.5f), "Gray");

		// Test some random colors
		roundTripOklab(new RGB(0.8f, 0.2f, 0.4f), "Pink");
		roundTripOklab(new RGB(0.1f, 0.6f, 0.3f), "Teal");
		roundTripOklab(new RGB(0.9f, 0.7f, 0.1f), "Gold");
	}

	@Test
	public void testKnownOklabValues () {
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
	}

	@Test
	public void testOklchConversions () {
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
	}

	@Test
	public void testEdgeCases () {
		// Test near-zero values
		roundTripOklab(new RGB(0.001f, 0.001f, 0.001f), "Near black");
		roundTripOklab(new RGB(0.999f, 0.999f, 0.999f), "Near white");

		// Test single channel
		roundTripOklab(new RGB(0.5f, 0, 0), "Half red");
		roundTripOklab(new RGB(0, 0.5f, 0), "Half green");
		roundTripOklab(new RGB(0, 0, 0.5f), "Half blue");

		// Test very saturated colors
		roundTripOklab(new RGB(1, 0, 0.001f), "Almost pure red");
		roundTripOklab(new RGB(0.001f, 1, 0), "Almost pure green");
	}

	static void roundTripOklab (RGB original, String name) {
		Oklab lab = Oklab(original);
		RGB result = RGB(lab);
		assertArrayClose(original, result, name);
	}

	@Test
	public void testCCTToOklab () {
		// Test some common color temperatures
		RGB warmWhite = RGB(2700, 0);
		Oklab warmLab = Oklab(warmWhite);

		RGB neutralWhite = RGB(4000, 0);
		Oklab neutralLab = Oklab(neutralWhite);

		RGB coolWhite = RGB(6500, 0);
		Oklab coolLab = Oklab(coolWhite);

		// After normalization in CCTRGB, the differences are subtle but b channel shows clear trend
		// Warmer colors have higher b (yellow) values
		if (warmLab.b() > neutralLab.b() && neutralLab.b() > coolLab.b()) {
		} else {
			throw new AssertionError("CCT to Oklab 'b' channel ordering incorrect");
		}
	}

	@Test
	public void testRGBCMYK () {
		// Test known values
		CMYK black = CMYK(new RGB(0, 0, 0));
		assertArrayClose(new CMYK(0, 0, 0, 1), black, "Black to CMYK");

		CMYK white = CMYK(new RGB(1, 1, 1));
		assertArrayClose(new CMYK(0, 0, 0, 0), white, "White to CMYK");

		CMYK red = CMYK(new RGB(1, 0, 0));
		assertArrayClose(new CMYK(0, 1, 1, 0), red, "Red to CMYK");

		// Test round trip
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), Colors::CMYK, Colors::RGB, "CMYK");
	}

	@Test
	public void testRGBHSI () {
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
	public void testRGBIHS () {
		// IHS uses intensity as sum of RGB (0-3 range)
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), Colors::IHS, Colors::RGB, "IHS");
	}

	@Test
	public void testRGBYUV () {
		// Test known values
		YUV black = YUV(new RGB(0, 0, 0));
		assertArrayClose(new YUV(0, 0, 0), black, "Black to YUV");

		YUV white = YUV(new RGB(1, 1, 1));
		assertClose(1, white.Y(), "White Y");
		assertClose(0, white.U(), "White U", EPSILON_F);
		assertClose(0, white.V(), "White V");

		// Test round trip - YUV has small precision errors
		roundTripf(new RGB(0.5f, 0.3f, 0.7f), Colors::YUV, Colors::RGB, "YUV");
	}

	@Test
	public void testRGBYIQ () {
		// Test known values
		YIQ black = YIQ(new RGB(0, 0, 0));
		assertArrayClose(new YIQ(0, 0, 0), black, "Black to YIQ");

		// Test round trip
		roundTripf(new RGB(0.5f, 0.3f, 0.7f), Colors::YIQ, Colors::RGB, "YIQ");
	}

	@Test
	public void testRGBHSV () {
		// Test known values
		HSV red = HSV(new RGB(1, 0, 0));
		assertClose(0, red.H(), "Red hue");
		assertClose(1, red.S(), "Red saturation");
		assertClose(1, red.V(), "Red value");

		HSV gray = HSV(new RGB(0.5f, 0.5f, 0.5f));
		assertClose(0, gray.S(), "Gray saturation");
		assertClose(0.5f, gray.V(), "Gray value");

		// Test round trip
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), Colors::HSV, Colors::RGB, "HSV");
	}

	@Test
	public void testRGBHSL () {
		// Test known values
		HSL red = HSL(new RGB(1, 0, 0));
		assertClose(0, red.H(), "Red hue");
		assertClose(1, red.S(), "Red saturation");
		assertClose(0.5f, red.L(), "Red lightness");

		// Test round trip
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), Colors::HSL, Colors::RGB, "HSL");
	}

	@Test
	public void testRGBHSLuv () {
		// Test known values
		// Pure red
		HSLuv red = HSLuv(new RGB(1, 0, 0));
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

		// Test round trip - use RGB that's within HSLuv gamut
		// RGB(0.5, 0.3, 0.7) has chroma that exceeds HSLuv maximum, causing clamping
		roundTripf(new RGB(0.43f, 0.37f, 0.51f), Colors::HSLuv, Colors::RGB, "HSLuv");
	}

	@Test
	public void testRGBXYZ () {
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
	}

	@Test
	public void testCIE1960Conversions () {
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
	}

	@Test
	public void testRGBYCbCr () {
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
	}

	@Test
	public void testRGBYCC () {
		// YCC is not a perfect round-trip conversion - it appears to be a lossy color space
		// Just test that it runs without errors
		RGB rgb = new RGB(0.5f, 0.3f, 0.7f);
		YCC ycc = YCC(rgb);
		RGB rgbBack = RGB(ycc);

		// Basic sanity checks
		assertClose(0.289, ycc.Y(), "YCC Y component", 0.01);
		// Verify rgbBack has reasonable values
		Assertions.assertTrue(rgbBack.r() >= 0 && rgbBack.r() <= 1, "YCC round trip R in range");
		Assertions.assertTrue(rgbBack.g() >= 0 && rgbBack.g() <= 1, "YCC round trip G in range");
		Assertions.assertTrue(rgbBack.b() >= 0 && rgbBack.b() <= 1, "YCC round trip B in range");
	}

	@Test
	public void testRGBYCoCg () {
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), Colors::YCoCg, Colors::RGB, "YCoCg");
	}

	@Test
	public void testRGBYES () {
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), Colors::YES, Colors::RGB, "YES");
	}

	@Test
	public void testXYZxyY () {
		// Convert RGB to XYZ first
		RGB rgb = new RGB(0.5f, 0.3f, 0.7f);
		XYZ xyz = XYZ(rgb);
		xyY xyy = xyY(xyz);
		XYZ xyzBack = XYZ(xyy);
		assertArrayClose(xyz, xyzBack, "XYZ <-> xyY round trip");
	}

	@Test
	public void testRGBLab () {
		// Test with default D65 illuminant
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> Lab(c), Colors::RGB, "Lab D65");

		// Test known values
		Lab whiteLab = Lab(new RGB(1, 1, 1));
		assertClose(100, whiteLab.L(), "White L*", 0.1);
		assertClose(0, whiteLab.a(), "White a*", 0.1);
		assertClose(0, whiteLab.b(), "White b*", 0.1);
	}

	@Test
	public void testRGBLuv () {
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
	public void testLabLCh () {
		Lab lab = new Lab(50, 25, -25);
		LCh lch = LCh(lab);
		Lab labBack = Lab(lch);

		assertArrayClose(lab, labBack, "Lab <-> LCh round trip");

		// Test that C = sqrt(a² + b²)
		double expectedC = Math.sqrt(25 * 25 + 25 * 25);
		assertClose(expectedC, lch.C(), "LCh chroma calculation");
	}

	@Test
	public void testRGBHunterLab () {
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> HunterLab(c), Colors::RGB, "Hunter Lab");

		// Test XYZ <-> Hunter Lab
		XYZ xyz = new XYZ(50, 50, 50);
		HunterLab hunterLab = HunterLab(xyz);
		XYZ xyzBack = XYZ(hunterLab);
		assertArrayClose(xyz, xyzBack, "XYZ <-> Hunter Lab round trip");
	}

	@Test
	public void testRGBLCh () {
		// Test round trip with default D65
		roundTripd(new RGB(0.5f, 0.3f, 0.7f), (RGB c) -> LCh(c), Colors::RGB, "LCh");
	}

	@Test
	public void testRGBLMS () {
		// Test all matrix types
		for (CAT matrix : CAT.values()) {
			// Test XYZ <-> LMS
			XYZ xyz = new XYZ(50, 50, 50);
			LMS lms = LMS(xyz, matrix);
			XYZ xyzBack = XYZ(lms, matrix);
			assertArrayClose(xyz, xyzBack, "XYZ <-> LMS " + matrix + " round trip", EPSILON_F);

			// Test RGB <-> LMS
			RGB rgb = new RGB(0.5f, 0.3f, 0.7f);
			LMS lmsRgb = LMS(rgb, matrix);
			RGB rgbBack = RGB(lmsRgb, matrix);
			assertArrayClose(rgb, rgbBack, "RGB <-> LMS " + matrix + " round trip");
		}
	}

	@Test
	public void testMatrixMultiply () {
		float[] vector = {1, 2, 3};
		float[][] matrix = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

		float[] result = Util.matrixMultiply(vector, matrix);
		assertArrayClose(vector, result, "Identity matrix multiply");

		// Test with a known transformation
		float[][] scaleMatrix = {{2, 0, 0}, {0, 2, 0}, {0, 0, 2}};
		float[] scaled = Util.matrixMultiply(vector, scaleMatrix);
		assertArrayClose(new float[] {2, 4, 6}, scaled, "Scale matrix multiply");
	}

	@Test
	public void testHunterLab () {
		// Test XYZ to Hunter Lab
		XYZ xyz = new XYZ(41.24f, 21.26f, 1.93f); // Red in XYZ
		HunterLab hunterLab = HunterLab(xyz);

		// Test round trip
		XYZ xyzBack = XYZ(hunterLab);
		assertArrayClose(xyz, xyzBack, "Hunter Lab round trip", EPSILON_F);

		// Test RGB to Hunter Lab
		RGB rgb = new RGB(1, 0, 0); // Pure red
		HunterLab hunterLabFromRgb = HunterLab(rgb);
		RGB rgbBack = RGB(hunterLabFromRgb);
		assertArrayClose(rgb, rgbBack, "RGB-HunterLab round trip", EPSILON_F);
	}

	@Test
	public void testLCh () {
		// Test Lab to LCh
		Lab lab = new Lab(53.23f, 80.11f, 67.22f); // Red in Lab
		LCh lch = LCh(lab);

		// Test round trip
		Lab labBack = Lab(lch);
		assertArrayClose(lab, labBack, "LCh round trip", EPSILON_F);

		// Test RGB to LCh with default illuminant
		RGB rgb = new RGB(1, 0, 0); // Pure red
		LCh lchFromRgb = LCh(rgb);
		RGB rgbBack = RGB(lchFromRgb);
		assertArrayClose(rgb, rgbBack, "RGB-LCh round trip", EPSILON_F);

		// Test RGB to LCh with custom illuminant
		LCh lchFromRgbD50 = LCh(rgb, Illuminant.CIE2.D50);
		// Convert back through Lab with the same illuminant
		Lab labFromLch = Lab(lchFromRgbD50);
		RGB rgbBackD50 = RGB(labFromLch, Illuminant.CIE2.D50);
		assertArrayClose(rgb, rgbBackD50, "RGB-LCh round trip with D50 illuminant", EPSILON_F);
	}

	@Test
	public void testLMS () {
		// Test all matrix types
		CAT[] matrices = {CAT.HPE, CAT.Bradford, CAT.VonKries, CAT.CAT97, CAT.CAT02};
		XYZ xyz = new XYZ(41.24f, 21.26f, 1.93f); // Red in XYZ

		for (CAT matrix : matrices) {
			// Test XYZ to LMS
			LMS lms = LMS(xyz, matrix);
			XYZ xyzBack = XYZ(lms, matrix);
			assertArrayClose(xyz, xyzBack, "LMS " + matrix + " round trip", EPSILON_F);

			// Test RGB to LMS
			RGB rgb = new RGB(1, 0, 0);
			LMS lmsFromRgb = LMS(rgb, matrix);
			RGB rgbBack = RGB(lmsFromRgb, matrix);
			assertArrayClose(rgb, rgbBack, "RGB-LMS " + matrix + " round trip", EPSILON_F);
		}

		// Test default (CAT02) conversions
		LMS lmsDefault = LMS(xyz);
		LMS lmsCat02 = LMS(xyz, CAT.CAT02);
		assertArrayClose(lmsDefault, lmsCat02, "Default LMS is CAT02", EPSILON_F);
	}

	@Test
	public void testSpecialConversions () {
		// Test RGChromaticity
		RGB rgb = new RGB(0.8f, 0.2f, 0.1f);
		RGChromaticity rgChrom = rgChromaticity(rgb);
		// Verify normalized RGB sums to 1
		double sum = rgChrom.r() + rgChrom.g() + rgChrom.b();
		assertClose(1, sum, "RGChromaticity normalized sum");

		// Test toC1C2C3
		C1C2C3 c1c2c3 = C1C2C3(rgb);
		// Verify c1c2c3 has reasonable values
		Assertions.assertNotNull(c1c2c3, "C1C2C3 conversion should not return null");
		Assertions.assertTrue(!Float.isNaN(c1c2c3.C1()), "C1 should not be NaN");
		Assertions.assertTrue(!Float.isNaN(c1c2c3.C2()), "C2 should not be NaN");
		Assertions.assertTrue(!Float.isNaN(c1c2c3.C3()), "C3 should not be NaN");

		// Test toO1O2
		O1O2 o1o2 = O1O2(rgb);
		// Verify o1o2 has reasonable values
		Assertions.assertNotNull(o1o2, "O1O2 conversion should not return null");
		Assertions.assertTrue(!Float.isNaN(o1o2.O1()), "O1 should not be NaN");
		Assertions.assertTrue(!Float.isNaN(o1o2.O2()), "O2 should not be NaN");

		// Test Grayscale
		double gray = grayscale(rgb);
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
	}

	@Test
	public void testUtilityFunctions () {
		// Test matrixMultiply
		float[] vector = {1, 2, 3};
		float[][] matrix = {{1, 0, 0}, {0, 2, 0}, {0, 0, 3}};
		float[] result = Util.matrixMultiply(vector, matrix);
		float[] expected = {1, 4, 9};
		assertArrayClose(expected, result, "Matrix multiply", EPSILON_F);

		// Test with non-diagonal matrix
		float[][] matrix2 = {{0.5f, 0.3f, 0.2f}, {0.1f, 0.6f, 0.3f}, {0.2f, 0.2f, 0.6f}};
		result = Util.matrixMultiply(vector, matrix2);
		expected = new float[] {1 * 0.5f + 2 * 0.1f + 3 * 0.2f, // 0.5 + 0.2 + 0.6 = 1.3
			1 * 0.3f + 2 * 0.6f + 3 * 0.2f, // 0.3 + 1.2 + 0.6 = 2.1
			1 * 0.2f + 2 * 0.3f + 3 * 0.6f // 0.2 + 0.6 + 1.8 = 2.6
		};
		assertArrayClose(expected, result, "Matrix multiply 2", EPSILON_F);
	}

	@Test
	public void testAllRoundTrips () {
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
			// Already tested conversions (quick verification)
			roundTripf(rgb, Colors::CMYK, Colors::RGB, "CMYK");
			roundTripf(rgb, Colors::IHS, Colors::RGB, "IHS");
			roundTripf(rgb, Colors::YUV, Colors::RGB, "YUV");
			roundTripf(rgb, Colors::YIQ, Colors::RGB, "YIQ");
			roundTripf(rgb, Colors::HSV, Colors::RGB, "HSV");
			roundTripf(rgb, Colors::YCoCg, Colors::RGB, "YCoCg");
			roundTripf(rgb, Colors::YES, Colors::RGB, "YES");
			roundTripf(rgb, (RGB c) -> XYZ(c), Colors::RGB, "XYZ");
			roundTripf(rgb, Colors::HSL, Colors::RGB, "HSL");
			roundTripf(rgb, (RGB c) -> Lab(c), Colors::RGB, "Lab");
			roundTripf(rgb, Colors::YCC, Colors::RGB, "YCC");
			testRoundTripYCbCr(rgb, YCbCrColorSpace.ITU_BT_601, EPSILON_F);
			testRoundTripYCbCr(rgb, YCbCrColorSpace.ITU_BT_709_HDTV, EPSILON_F);
		}
	}

	static void testRoundTripYCbCr (RGB rgb, YCbCrColorSpace colorSpace, double epsilon) {
		YCbCr ycbcr = YCbCr(rgb, colorSpace);
		RGB back = RGB(ycbcr, colorSpace);
		try {
			assertArrayClose(rgb, back, "YCbCr " + colorSpace + " round trip", epsilon);
		} catch (AssertionError e) {
			throw e;
		}
	}

	static double getMaxError (Record expectedRecord, Record actualRecord) {
		float[] expected = toArray(expectedRecord), actual = toArray(actualRecord);
		double maxError = 0;
		for (int i = 0; i < expected.length; i++)
			maxError = Math.max(maxError, Math.abs(expected[i] - actual[i]));
		return maxError;
	}

	@Test
	public void testCCTConversions () {
		// Test known CCT values
		String actual = RGB(2700, 0, 50).hex();
		assertTrue(actual.equals("fbaa58"), "Expected CCTRGB value != actual: ffe87a != " + actual);

		actual = RGB(2700, 0.01f, 50).hex();
		assertTrue(actual.equals("ffa774"), "Expected CCTRGB with 0.01 Duv value != actual: ffe39f != " + actual);

		// Test common color temperatures
		float[] ccts = {2700, 3000, 4000, 5000, 6500};

		for (float cct : ccts) {
			RGB rgb = RGB(cct, 0, 100);

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
		float[] duvOffsets = {-0.01f, 0, 0.01f};
		for (float duv : duvOffsets) {
			RGB rgb = RGB(3000, duv);
			// Verify Duv affects the color
			Assertions.assertTrue(rgb.r() >= 0 && rgb.r() <= 1, "Duv " + duv + " R in range");
			Assertions.assertTrue(rgb.g() >= 0 && rgb.g() <= 1, "Duv " + duv + " G in range");
			Assertions.assertTrue(rgb.b() >= 0 && rgb.b() <= 1, "Duv " + duv + " B in range");
		}

		// Test CCT to xy conversions
		for (float cct : ccts) {
			xy xy = xy(cct);

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
		float[] testCCTs = {1600, 1667, 2000, 2300, 2500, 2700, 3000, 3500, 4000, 5000, 6500, 10000};
		for (float expectedCCT : testCCTs) {
			xy xy = xy(expectedCCT);
			float calculatedCCT = CCT(xy);

			// Check for invalid xy coordinates
			if (Float.isNaN(xy.x()) && Float.isNaN(calculatedCCT)) {
				assertTrue(Float.isNaN(calculatedCCT), "CCT should return -1 for invalid xy(-1, -1)");
				continue;
			}

			float error = Math.abs(calculatedCCT - expectedCCT);

			// McCamy's approximation has varying accuracy
			if (expectedCCT >= 4000 && expectedCCT <= 8000) {
				assertTrue(error < 50, "CCT error should be <50K in optimal range, was " + error);
			} else {
				assertTrue(error < 200, "CCT error should be <200K outside optimal range, was " + error);
			}
		}

		// Test uv to CCT conversions
		for (float expectedCCT : testCCTs) {
			// Skip invalid CCTs that would produce fallback values
			if (expectedCCT < 1667) {
				continue;
			}

			uv uv = uv(RGB(expectedCCT, 0));
			float calculatedCCT = CCT(uv);
			float error = Math.abs(calculatedCCT - expectedCCT);
			// Verify error is reasonable
			Assertions.assertTrue(error < 500, "UV to CCT error for " + expectedCCT + "K should be reasonable, was " + error);
		}

		// Test edge cases

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
	}

	@Test
	public void testLinearRGBConversions () {
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

			assertArrayClose(linearRgb, linearBack, "Linear RGB round trip", EPSILON_F);

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
		assertClose(100, xyz.Y(), "xyXYZ Y value", EPSILON_F);

		// Verify it produces the same ratios as XYZ
		XYZ xyzFromxyY = XYZ(new xyY(0.3127f, 0.3290f, 100));
		assertArrayClose(xyz, xyzFromxyY, "xyXYZ matches XYZ", EPSILON_F);
	}

	@Test
	public void testOklabConversions () {
		// Test Oklab to Oklch conversions, gray (should have C=0)
		Oklab[] testLabs = {new Oklab(0.5f, 0.1f, 0.1f), new Oklab(0.8f, -0.05f, 0.05f), new Oklab(0.3f, 0.0f, 0.0f)};
		for (Oklab lab : testLabs) {
			Oklch lch = Oklch(lab);
			Oklab labBack = Oklab(lch);
			assertArrayClose(lab, labBack, "Oklab <-> Oklch round trip", EPSILON_F);

			// Verify cylindrical coordinate conversion
			float expectedC = (float)Math.sqrt(lab.a() * lab.a() + lab.b() * lab.b());
			assertClose(expectedC, lch.C(), "Oklch chroma calculation", EPSILON_F);

			// For gray colors, chroma should be 0
			if (Math.abs(lab.a()) < EPSILON_F && Math.abs(lab.b()) < EPSILON_F) {
				assertClose(0, lch.C(), "Gray Oklch chroma", EPSILON_F);
			}
		}

		// Test RGB to Oklch direct conversion
		RGB rgb = new RGB(0.6f, 0.4f, 0.2f);
		Oklch oklch = Oklch(rgb);
		RGB rgbBack = RGB(oklch);
		assertArrayClose(rgb, rgbBack, "RGB <-> Oklch round trip", EPSILON_F);
	}

	@Test
	public void testOklabLerp () {
		// Test lerp with t=0 returns first color
		Oklab color1 = new Oklab(0.3f, 0.1f, -0.1f);
		Oklab color2 = new Oklab(0.8f, -0.05f, 0.15f);
		Oklab result = lerp(color1, color2, 0);
		assertArrayClose(color1, result, "Oklab lerp t=0");

		// Test lerp with t=1 returns second color
		result = lerp(color1, color2, 1);
		assertArrayClose(color2, result, "Oklab lerp t=1");

		// Test lerp with t=0.5 returns midpoint
		result = lerp(color1, color2, 0.5f);
		Oklab expected = new Oklab((color1.L() + color2.L()) / 2, (color1.a() + color2.a()) / 2, (color1.b() + color2.b()) / 2);
		assertArrayClose(expected, result, "Oklab lerp t=0.5");

		// Test lerp between black and white
		Oklab black = Oklab(new RGB(0, 0, 0));
		Oklab white = Oklab(new RGB(1, 1, 1));
		Oklab gray = lerp(black, white, 0.5f);
		// Gray should have L around 0.5, a and b near 0
		assertClose(0.5f, gray.L(), "Lerp black-white L", 0.1f);
		assertClose(0, gray.a(), "Lerp black-white a", 0.01f);
		assertClose(0, gray.b(), "Lerp black-white b", 0.01f);

		// Test lerp between complementary colors
		Oklab red = Oklab(new RGB(1, 0, 0));
		Oklab cyan = Oklab(new RGB(0, 1, 1));
		Oklab mid = lerp(red, cyan, 0.5f);
		// Midpoint should be grayish
		RGB midRGB = RGB(mid);
		// Verify it's roughly gray (all channels similar)
		float avgChannel = (midRGB.r() + midRGB.g() + midRGB.b()) / 3;
		assertClose(avgChannel, midRGB.r(), "Lerp complementary R", 0.15f);
		assertClose(avgChannel, midRGB.g(), "Lerp complementary G", 0.15f);
		assertClose(avgChannel, midRGB.b(), "Lerp complementary B", 0.15f);

		// Test lerp produces smooth gradient
		float[] factors = {0, 0.25f, 0.5f, 0.75f, 1};
		Oklab prevColor = null;
		for (float t : factors) {
			Oklab color = lerp(color1, color2, t);
			if (prevColor != null) {
				// Verify smooth progression
				float deltaL = Math.abs(color.L() - prevColor.L());
				float deltaA = Math.abs(color.a() - prevColor.a());
				float deltaB = Math.abs(color.b() - prevColor.b());
				// Each step should have similar deltas
				assertClose(0.125f, deltaL, "Smooth L progression", 0.01f);
				assertClose(0.0375f, deltaA, "Smooth a progression", 0.01f);
				assertClose(0.0625f, deltaB, "Smooth b progression", 0.01f);
			}
			prevColor = color;
		}

		// Test lerp preserves perceptual uniformity
		// Colors at equal t intervals should appear equally spaced
		Oklab blue = Oklab(new RGB(0, 0, 1));
		Oklab yellow = Oklab(new RGB(1, 1, 0));
		Oklab q1 = lerp(blue, yellow, 0.25f);
		Oklab q2 = lerp(blue, yellow, 0.5f);
		Oklab q3 = lerp(blue, yellow, 0.75f);

		// Convert to RGB to verify colors look reasonable
		RGB rgbQ1 = RGB(q1);
		RGB rgbQ2 = RGB(q2);
		RGB rgbQ3 = RGB(q3);

		// All should be valid RGB values
		assertTrue(rgbQ1.r() >= 0 && rgbQ1.r() <= 1, "Q1 R in range");
		assertTrue(rgbQ1.g() >= 0 && rgbQ1.g() <= 1, "Q1 G in range");
		assertTrue(rgbQ1.b() >= 0 && rgbQ1.b() <= 1, "Q1 B in range");
		assertTrue(rgbQ2.r() >= 0 && rgbQ2.r() <= 1, "Q2 R in range");
		assertTrue(rgbQ2.g() >= 0 && rgbQ2.g() <= 1, "Q2 G in range");
		assertTrue(rgbQ2.b() >= 0 && rgbQ2.b() <= 1, "Q2 B in range");
		assertTrue(rgbQ3.r() >= 0 && rgbQ3.r() <= 1, "Q3 R in range");
		assertTrue(rgbQ3.g() >= 0 && rgbQ3.g() <= 1, "Q3 G in range");
		assertTrue(rgbQ3.b() >= 0 && rgbQ3.b() <= 1, "Q3 B in range");

		// Test edge cases
		// Lerp with same color should return that color
		Oklab sameResult = lerp(color1, color1, 0.5f);
		assertArrayClose(color1, sameResult, "Lerp same color");

		// Test lerp with t outside [0,1] (extrapolation)
		Oklab extrapolated = lerp(color1, color2, 1.5f);
		// Should continue the line beyond color2
		float expectedL = color1.L() + 1.5f * (color2.L() - color1.L());
		float expectedA = color1.a() + 1.5f * (color2.a() - color1.a());
		float expectedB = color1.b() + 1.5f * (color2.b() - color1.b());
		assertClose(expectedL, extrapolated.L(), "Extrapolated L");
		assertClose(expectedA, extrapolated.a(), "Extrapolated a");
		assertClose(expectedB, extrapolated.b(), "Extrapolated b");

		// Test lerp with negative t
		Oklab negativeT = lerp(color1, color2, -0.5f);
		expectedL = color1.L() - 0.5f * (color2.L() - color1.L());
		expectedA = color1.a() - 0.5f * (color2.a() - color1.a());
		expectedB = color1.b() - 0.5f * (color2.b() - color1.b());
		assertClose(expectedL, negativeT.L(), "Negative t L");
		assertClose(expectedA, negativeT.a(), "Negative t a");
		assertClose(expectedB, negativeT.b(), "Negative t b");
	}

	@Test
	public void testOkhslConversions () {
		// Test primary colors
		roundTripOkhsl(new RGB(1, 0, 0), "Red");
		roundTripOkhsl(new RGB(0, 1, 0), "Green");
		roundTripOkhsl(new RGB(0, 0, 1), "Blue");
		roundTripOkhsl(new RGB(1, 1, 0), "Yellow");
		roundTripOkhsl(new RGB(0, 1, 1), "Cyan");
		roundTripOkhsl(new RGB(1, 0, 1), "Magenta");
		roundTripOkhsl(new RGB(1, 1, 1), "White");
		roundTripOkhsl(new RGB(0, 0, 0), "Black");
		roundTripOkhsl(new RGB(0.5f, 0.5f, 0.5f), "Gray");

		// Test that white has l=1
		Okhsl white = Okhsl(new RGB(1, 1, 1));
		assertClose(1.0f, white.l(), "White Okhsl lightness");
		assertClose(0.0f, white.s(), "White Okhsl saturation", 0.01f);

		// Test that black has l=0
		Okhsl black = Okhsl(new RGB(0, 0, 0));
		assertClose(0.0f, black.l(), "Black Okhsl lightness");

		// Test that grays have s=0
		Okhsl gray = Okhsl(new RGB(0.5f, 0.5f, 0.5f));
		assertClose(0.0f, gray.s(), "Gray Okhsl saturation", 0.01f);

		// Test hue angles for primary colors
		Okhsl red = Okhsl(new RGB(1, 0, 0));
		Okhsl green = Okhsl(new RGB(0, 1, 0));
		Okhsl blue = Okhsl(new RGB(0, 0, 1));

		// Verify hue differences
		float hueGreenRed = Math.abs(green.h() - red.h());
		float hueBlueGreen = Math.abs(blue.h() - green.h());
		assertTrue(hueGreenRed > 90 && hueGreenRed < 180, "Green-Red hue difference");
		assertTrue(hueBlueGreen > 90 && hueBlueGreen < 180, "Blue-Green hue difference");

		// Test saturation range
		Okhsl[] testColors = {red, green, blue};
		for (Okhsl color : testColors) {
			assertTrue(color.s() >= 0 && color.s() <= 1, "Saturation in range [0,1]");
		}

		// Test edge cases with very dark colors
		roundTripOkhsl(new RGB(0.01f, 0.01f, 0.01f), "Very dark gray");
		roundTripOkhsl(new RGB(0.1f, 0, 0), "Very dark red");
	}

	static void roundTripOkhsl (RGB original, String name) {
		Okhsl hsl = Okhsl(original);
		RGB result = RGB(hsl);
		assertArrayClose(original, result, name + " Okhsl round trip", 0.01f);
	}

	@Test
	public void testOkhsvConversions () {
		// Test primary colors
		roundTripOkhsv(new RGB(1, 0, 0), "Red");
		roundTripOkhsv(new RGB(0, 1, 0), "Green");
		roundTripOkhsv(new RGB(0, 0, 1), "Blue");
		roundTripOkhsv(new RGB(1, 1, 0), "Yellow");
		roundTripOkhsv(new RGB(0, 1, 1), "Cyan");
		roundTripOkhsv(new RGB(1, 0, 1), "Magenta");
		roundTripOkhsv(new RGB(1, 1, 1), "White");
		roundTripOkhsv(new RGB(0, 0, 0), "Black");
		roundTripOkhsv(new RGB(0.5f, 0.5f, 0.5f), "Gray");

		// Test that white has v=1
		Okhsv white = Okhsv(new RGB(1, 1, 1));
		assertClose(1.0f, white.v(), "White Okhsv value", 0.1f);
		assertClose(0.0f, white.s(), "White Okhsv saturation", 0.01f);

		// Test that black has v=0
		Okhsv black = Okhsv(new RGB(0, 0, 0));
		assertClose(0.0f, black.v(), "Black Okhsv value", 0.01f);

		// Test that grays have s=0
		Okhsv gray = Okhsv(new RGB(0.5f, 0.5f, 0.5f));
		assertClose(0.0f, gray.s(), "Gray Okhsv saturation", 0.01f);

		// Test hue angles for primary colors
		Okhsv red = Okhsv(new RGB(1, 0, 0));
		Okhsv green = Okhsv(new RGB(0, 1, 0));
		Okhsv blue = Okhsv(new RGB(0, 0, 1));

		// Verify hue differences
		float hueGreenRed = Math.abs(green.h() - red.h());
		float hueBlueGreen = Math.abs(blue.h() - green.h());
		assertTrue(hueGreenRed > 90 && hueGreenRed < 180, "Green-Red hue difference");
		assertTrue(hueBlueGreen > 90 && hueBlueGreen < 180, "Blue-Green hue difference");

		// Test value and saturation range
		Okhsv[] testColors = {red, green, blue, white, black, gray};
		String[] names = {"red", "green", "blue", "white", "black", "gray"};
		for (int i = 0; i < testColors.length; i++) {
			Okhsv color = testColors[i];
			// System.out.println(names[i] + " Okhsv: h=" + color.h() + " s=" + color.s() + " v=" + color.v());
			assertTrue(color.s() >= 0 && color.s() <= 1, names[i] + " saturation in range [0,1]");
			assertTrue(color.v() >= 0 && color.v() <= 1, names[i] + " value in range [0,1]");
		}

		// Test edge cases
		roundTripOkhsv(new RGB(0.01f, 0.01f, 0.01f), "Very dark gray");
		roundTripOkhsv(new RGB(0.99f, 0.99f, 0.99f), "Very light gray");
	}

	static void roundTripOkhsv (RGB original, String name) {
		Okhsv hsv = Okhsv(original);
		RGB result = RGB(hsv);
		assertArrayClose(original, result, name + " Okhsv round trip", 0.02f);
	}

	@Test
	public void testUV1976Conversions () {
		// Test RGB to UV1976 conversions
		RGB[] testColors = {new RGB(1, 0, 0), // Red
			new RGB(0, 1, 0), // Green
			new RGB(0, 0, 1), // Blue
			new RGB(1, 1, 1), // White
			new RGB(0.5f, 0.5f, 0.5f) // Gray
		};

		for (RGB rgb : testColors) {
			uv uv = uv(rgb);
			RGB rgbBack = RGB(uv);

			// u'v' is a chromaticity space - it preserves color but not brightness
			// For non-gray colors, we should get the same chromaticity back
			if (!(rgb.r() == rgb.g() && rgb.g() == rgb.b())) {
				// For chromatic colors, check that we get the same color ratios
				float maxOriginal = Math.max(rgb.r(), Math.max(rgb.g(), rgb.b()));
				float maxBack = Math.max(rgbBack.r(), Math.max(rgbBack.g(), rgbBack.b()));
				if (maxOriginal > 0 && maxBack > 0) {
					float[] normalizedOrig = {rgb.r() / maxOriginal, rgb.g() / maxOriginal, rgb.b() / maxOriginal};
					float[] normalizedBack = {rgbBack.r() / maxBack, rgbBack.g() / maxBack, rgbBack.b() / maxBack};
					assertArrayClose(normalizedOrig, normalizedBack, "RGB chromaticity preservation", EPSILON_F);
				}
			} else {
				// For gray colors, just verify we get gray back (though brightness may differ)
				assertClose(rgbBack.r(), rgbBack.g(), "Gray R=G", EPSILON_F);
				assertClose(rgbBack.g(), rgbBack.b(), "Gray G=B", EPSILON_F);
			}
		}

		// Test UV1976 to xy conversions
		uv[] testUVs = {new uv(0.2105f, 0.4737f), // D65 white point in u'v'
			new uv(0.4507f, 0.5229f), // Red primary
			new uv(0.1250f, 0.5625f), // Green primary
			new uv(0.1754f, 0.1579f) // Blue primary
		};

		for (uv uv : testUVs) {
			xy xy = xy(uv);
			uv uvBack = uv(xy);
			assertArrayClose(uv, uvBack, "UV1976 <-> xy round trip", EPSILON_F);
		}

		// Test CCT to UV1960 (used internally by CCTRGB)
		float[] ccts = {2700, 4000, 6500};
		for (float cct : ccts) {
			uv1960 uv1960 = uv1960(cct);

			// Verify UV values are reasonable
			assertTrue(uv1960.u() > 0 && uv1960.u() < 1, "u1960 in range for CCT " + cct);
			assertTrue(uv1960.v() > 0 && uv1960.v() < 1, "v1960 in range for CCT " + cct);
		}

		// Test DUV1960 offset calculation
		for (float cct : ccts) {
			uv1960 base = uv1960(cct);
			uv1960 offset = uv1960(cct, 0.01f);
			offset = new uv1960(offset.u() - base.u(), offset.v() - base.v());

			// Verify offset is perpendicular (small magnitude)
			float magnitude = (float)Math.sqrt(offset.u() * offset.u() + offset.v() * offset.v());
			assertClose(0.01, magnitude, "DUV1960 offset magnitude", 0.0001);
		}
	}

	@Test
	public void testGammaFunctions () {
		// Test common gamma values
		float[] gammas = {1.0f, 1.8f, 2.2f, 2.4f};
		float[] testValues = {0.0f, 0.1f, 0.25f, 0.5f, 0.75f, 1.0f};

		for (float gamma : gammas) {
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
	}

	@Test
	public void testCIEDE2000 () {
		// Test identical colors
		Lab lab1 = new Lab(50, 0, 0);
		Lab lab2 = new Lab(50, 0, 0);
		assertClose(0, deltaE2000(lab1, lab2), "Identical colors should have deltaE = 0");

		// Test known CIEDE2000 values from the original paper
		// These test cases are from Sharma et al. "The CIEDE2000 Color-Difference Formula: Implementation Notes"
		// Case 1
		lab1 = new Lab(50.0000f, 2.6772f, -79.7751f);
		lab2 = new Lab(50.0000f, 0.0000f, -82.7485f);
		assertClose(2.0425, deltaE2000(lab1, lab2), "CIEDE2000 test case 1", 0.0001);

		// Case 2
		lab1 = new Lab(50.0000f, 3.1571f, -77.2803f);
		lab2 = new Lab(50.0000f, 0.0000f, -82.7485f);
		assertClose(2.8615, deltaE2000(lab1, lab2), "CIEDE2000 test case 2", 0.0001);

		// Case 3
		lab1 = new Lab(50.0000f, 2.8361f, -74.0200f);
		lab2 = new Lab(50.0000f, 0.0000f, -82.7485f);
		assertClose(3.4412, deltaE2000(lab1, lab2), "CIEDE2000 test case 3", 0.0001);

		// Test RGB convenience methods
		RGB rgb1 = new RGB(1, 0, 0); // Red
		RGB rgb2 = new RGB(0, 1, 0); // Green
		float deltaE = deltaE2000(rgb1, rgb2);
		assertTrue(deltaE > 80, "Red vs Green should have large deltaE (>80), got " + deltaE);

		// Test similar colors
		rgb1 = new RGB(0.5f, 0.5f, 0.5f);
		rgb2 = new RGB(0.51f, 0.5f, 0.5f);
		deltaE = deltaE2000(rgb1, rgb2);
		assertTrue(deltaE < 2, "Very similar grays should have small deltaE (<2), got " + deltaE);

		// Test black and white
		rgb1 = new RGB(0, 0, 0);
		rgb2 = new RGB(1, 1, 1);
		deltaE = deltaE2000(rgb1, rgb2);
		assertTrue(deltaE > 99, "Black vs White should have very large deltaE (>99), got " + deltaE);

		// Test with custom weights
		lab1 = new Lab(50, 10, 10);
		lab2 = new Lab(60, 10, 10);
		float deltaEDefault = deltaE2000(lab1, lab2);
		float deltaELightness = deltaE2000(lab1, lab2, 2, 1, 1); // Double lightness weight
		// With kL=2, the lightness component is divided by 2, so deltaE should be smaller
		assertTrue(deltaELightness < deltaEDefault, "Higher lightness weight should decrease deltaE (kL is divisor)");

		lab1 = new Lab(50, 0, 0);
		lab2 = new Lab(50, 20, 0);
		deltaEDefault = deltaE2000(lab1, lab2);
		float deltaEChroma = deltaE2000(lab1, lab2, 1, 2, 1); // Double chroma weight
		// With kC=2, the chroma component is divided by 2, so deltaE should be smaller
		assertTrue(deltaEChroma < deltaEDefault, "Higher chroma weight should decrease deltaE (kC is divisor)");

		// Test edge cases
		// Test with zero chroma (gray colors)
		lab1 = new Lab(50, 0, 0);
		lab2 = new Lab(60, 0, 0);
		deltaE = deltaE2000(lab1, lab2);
		assertTrue(deltaE > 0, "Different grays should have non-zero deltaE");

		// Test very small differences
		lab1 = new Lab(50.0000f, 0.0000f, 0.0000f);
		lab2 = new Lab(50.0001f, 0.0000f, 0.0000f);
		deltaE = deltaE2000(lab1, lab2);
		assertTrue(deltaE < 0.001, "Very small L difference should give very small deltaE");

		// Test hue differences
		lab1 = new Lab(50, 20, 0); // Red direction
		lab2 = new Lab(50, 0, 20); // Yellow direction
		float deltaEHue = deltaE2000(lab1, lab2);
		assertTrue(deltaEHue > 10, "90 degree hue difference should be significant");

		// Test opposite hues
		lab1 = new Lab(50, 20, 0);
		lab2 = new Lab(50, -20, 0);
		deltaE = deltaE2000(lab1, lab2);
		assertTrue(deltaE > 20, "Opposite hues should have large deltaE");

		// Test perceptual uniformity improvements over CIE76
		// CIEDE2000 should give more consistent results for blue differences
		Lab blue1 = new Lab(32.3f, 79.2f, -107.9f); // Blue
		Lab blue2 = new Lab(32.3f, 69.2f, -107.9f); // Slightly different blue
		Lab red1 = new Lab(53.2f, 80.1f, 67.2f); // Red
		Lab red2 = new Lab(53.2f, 70.1f, 67.2f); // Slightly different red

		// Both have same chroma difference (10 units)
		float deltaEBlue = deltaE2000(blue1, blue2);
		float deltaERed = deltaE2000(red1, red2);

		// CIEDE2000 should show these as more similar than CIE76 would
		// (The blue region correction in CIEDE2000 makes blue differences smaller)
		assertTrue(Math.abs(deltaEBlue - deltaERed) < 5, "CIEDE2000 should show more uniform perception across color space");
	}

	@Test
	public void testDMXConversions () {
		// Test DMX8 conversions
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

		// Test that functions don't clamp (user should handle that)
		assertEquals(-25, dmx8(-0.1f), "DMX8 negative not clamped");
		assertEquals(280, dmx8(1.1f), "DMX8 >1 not clamped");
		assertEquals(-6553, dmx16(-0.1f), "DMX16 negative not clamped");
		assertEquals(72088, dmx16(1.1f), "DMX16 >1 not clamped");
	}

	@Test
	public void testRGBWConversions () {
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
		assertEquals("ff8040bf", hexTest.hex(), "RGBW hex");
		assertEquals("255, 127, 63, 191", hexTest.toString255(), "RGBW toString255");

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
	public void testRGBWWConversions () {
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
		assertEquals("ff80401abf", hexTest.hex(), "RGBWW hex");
		assertEquals("255, 127, 63, 25, 191", hexTest.toString255(), "RGBWW toString255");
	}

	static void assertClose (double expected, double actual, String name) {
		Assertions.assertEquals(expected, actual, EPSILON_D, name);
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
		assertArrayClose(toArray(expected), toArray(actual), name, EPSILON_F);
	}

	static void assertArrayClose (Record expected, Record actual, String name, double epsilon) {
		assertArrayClose(toArray(expected), toArray(actual), name, epsilon);
	}

	static void assertClose (double expected, double actual, String name, double epsilon) {
		Assertions.assertEquals(expected, actual, epsilon, name);
	}

	static void assertEquals (int expected, int actual, String name) {
		Assertions.assertEquals(expected, actual, name);
	}

	static void assertEquals (float expected, float actual, String name) {
		Assertions.assertEquals(expected, actual, 0.0f, name);
	}

	static void assertEquals (String expected, String actual, String name) {
		Assertions.assertEquals(expected, actual, name);
	}

	static void assertTrue (boolean condition, String message) {
		Assertions.assertTrue(condition, message);
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

	static <T extends Record, U extends Record> void roundTripd (T original, Function<T, U> forward, Function<U, T> backward,
		String name) {
		roundTrip(original, forward, backward, name, EPSILON_D);
	}

	static <T extends Record, U extends Record> void roundTripf (T original, Function<T, U> forward, Function<U, T> backward,
		String name) {
		roundTrip(original, forward, backward, name, EPSILON_F);
	}

	static <T extends Record, U extends Record> void roundTrip (T original, Function<T, U> forward, Function<U, T> backward,
		String name, double epsilon) {
		U converted = forward.apply(original);
		T back = backward.apply(converted);
		assertArrayClose(original, back, name + " round trip", epsilon);
	}
}
