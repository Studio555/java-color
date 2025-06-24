
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.TestsUtil.*;
import static com.esotericsoftware.colors.Util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.colors.Illuminant.CIE2;
import com.esotericsoftware.colors.space.C1C2C3;
import com.esotericsoftware.colors.space.CMYK;
import com.esotericsoftware.colors.space.HSL;
import com.esotericsoftware.colors.space.HSV;
import com.esotericsoftware.colors.space.HunterLab;
import com.esotericsoftware.colors.space.IHS;
import com.esotericsoftware.colors.space.LCh;
import com.esotericsoftware.colors.space.LMS;
import com.esotericsoftware.colors.space.LMS.CAT;
import com.esotericsoftware.colors.space.Lab;
import com.esotericsoftware.colors.space.O1O2;
import com.esotericsoftware.colors.space.Oklab;
import com.esotericsoftware.colors.space.RGB;
import com.esotericsoftware.colors.space.XYZ;
import com.esotericsoftware.colors.space.YCC;
import com.esotericsoftware.colors.space.YCbCr;
import com.esotericsoftware.colors.space.YCbCr.YCbCrColorSpace;
import com.esotericsoftware.colors.space.YCoCg;
import com.esotericsoftware.colors.space.YES;
import com.esotericsoftware.colors.space.YIQ;
import com.esotericsoftware.colors.space.YUV;
import com.esotericsoftware.colors.space.rg;
import com.esotericsoftware.colors.space.uv;
import com.esotericsoftware.colors.space.uv1960;
import com.esotericsoftware.colors.space.xy;
import com.esotericsoftware.colors.space.xyY;

/** @author Nathan Sweet <misc@n4te.com> */
public class ColorsTest {
	@Test
	public void ACESTest () {
		RGB rgb = new RGB(0.2f, 0.5f, 0.8f);

		// Test ACES2065-1
		var aces2065 = rgb.ACES2065_1();
		RGB rgb2065 = aces2065.RGB();
		assertRecordClose(rgb, rgb2065, "ACES2065-1 round trip", 0.01f);

		// Test ACEScg
		var acesCg = rgb.ACEScg();
		RGB rgbCg = acesCg.RGB();
		assertRecordClose(rgb, rgbCg, "ACEScg round trip", 0.01f);

		// Test ACEScc
		var acesCc = rgb.ACEScc();
		RGB rgbCc = acesCc.RGB();
		assertRecordClose(rgb, rgbCc, "ACEScc round trip", 0.01f);

		// Test wide gamut - ACES can represent colors outside sRGB
		var acesWide = new RGB(1.2f, 0.8f, -0.1f).ACES2065_1();
		assertTrue(acesWide.r() != 0 || acesWide.g() != 0 || acesWide.b() != 0, "RGB must be zero");
	}

	@Test
	public void ITPTest () {
		RGB rgb = new RGB(0.2f, 0.5f, 0.8f);
		var ITP = rgb.ITP();
		RGB rgb2 = ITP.RGB();
		assertRecordClose(rgb, rgb2, "ITP round trip", 0.0005f);

		// Test edge cases
		assertRecordClose(new RGB(0, 0, 0), new RGB(0, 0, 0).ITP().RGB(), "ITP black round trip");
		assertRecordClose(new RGB(1, 1, 1), new RGB(1, 1, 1).ITP().RGB(), "ITP white round trip", 0.0005f);

		// Test primary colors - slightly higher tolerance for extreme values
		assertRecordClose(new RGB(1, 0, 0), new RGB(1, 0, 0).ITP().RGB(), "ITP red round trip", 0.0006f);
		assertRecordClose(new RGB(0, 1, 0), new RGB(0, 1, 0).ITP().RGB(), "ITP green round trip", 0.006f); // Higher tolerance.
		assertRecordClose(new RGB(0, 0, 1), new RGB(0, 0, 1).ITP().RGB(), "ITP blue round trip", 0.0006f);

		// Test various colors
		RGB[] testColors = {new RGB(0.1f, 0.2f, 0.3f), new RGB(0.5f, 0.5f, 0.5f), new RGB(0.7f, 0.3f, 0.1f),
			new RGB(0.9f, 0.8f, 0.7f), new RGB(0.25f, 0.75f, 0.5f)};
		for (RGB color : testColors) {
			var ITP_test = color.ITP();
			RGB back = ITP_test.RGB();
			assertRecordClose(color, back, "ITP round trip for " + color, 0.001f);
		}

		// Test that ITP values are reasonable
		assertTrue(ITP.I() >= 0 && ITP.I() <= 1, "I in range");
		assertTrue(Math.abs(ITP.Ct()) < 0.5, "Ct in reasonable range");
		assertTrue(Math.abs(ITP.Cp()) < 0.5, "Cp in reasonable range");

		// Test that different colors produce different values
		var red = new RGB(1, 0, 0).ITP();
		var green = new RGB(0, 1, 0).ITP();
		var blue = new RGB(0, 0, 1).ITP();

		assertTrue(Math.abs(red.Cp() - green.Cp()) > 0.1, "Red and green have different Cp");
		assertTrue(Math.abs(blue.Ct() - red.Ct()) > 0.1, "Blue and red have different Ct");
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
			roundTripf(rgb, RGB::CMYK, CMYK::RGB, "CMYK");
			roundTripf(rgb, RGB::IHS, IHS::RGB, "IHS");
			roundTripf(rgb, RGB::YUV, YUV::RGB, "YUV");
			roundTripf(rgb, RGB::YIQ, YIQ::RGB, "YIQ");
			roundTripf(rgb, RGB::HSV, HSV::RGB, "HSV");
			roundTripf(rgb, RGB::YCoCg, YCoCg::RGB, "YCoCg");
			roundTripf(rgb, RGB::YES, YES::RGB, "YES");
			roundTripf(rgb, RGB::XYZ, XYZ::RGB, "XYZ");
			roundTripf(rgb, RGB::HSL, HSL::RGB, "HSL");
			roundTripf(rgb, (RGB c) -> c.Lab(), Lab::RGB, "Lab");
			roundTripf(rgb, RGB::YCC, YCC::RGB, "YCC");
			roundTripf(rgb, RGB::Oklab, Oklab::RGB, "Oklab");
			roundTripYCbCr(rgb, YCbCrColorSpace.ITU_BT_601, EPSILON_F);
			roundTripYCbCr(rgb, YCbCrColorSpace.ITU_BT_709_HDTV, EPSILON_F);
		}
	}

	@Test
	public void testCIE1960Conversions () {
		// Test the fixed UV1960 to xy conversion
		uv1960 uv = new uv1960(0.2f, 0.3f);
		xy xy = uv.xy();
		uv1960 uvBack = xy.uv1960();
		assertRecordClose(uv, uvBack, "CIE 1960 <-> xy round trip");

		// Test CIE 1960 <-> 1976
		uv uvPrime = uv.uv();
		assertClose(uv.u(), uvPrime.u(), "u = u'");
		assertClose(uv.v() * 1.5f, uvPrime.v(), "v' = 1.5v");

		uv1960 uvBack2 = uvPrime.uv1960();
		assertRecordClose(uv, uvBack2, "CIE 1960 <-> 1976 round trip");
	}

	@Test
	public void testCIEDE2000 () {
		// Test identical colors
		Lab lab1 = new Lab(50, 0, 0);
		Lab lab2 = new Lab(50, 0, 0);
		assertClose(0, lab1.deltaE2000(lab2), "Identical colors should have deltaE = 0");

		// Test known CIEDE2000 values from the original paper
		// These test cases are from Sharma et al. "The CIEDE2000 Color-Difference Formula: Implementation Notes"
		// Case 1
		lab1 = new Lab(50.0000f, 2.6772f, -79.7751f);
		lab2 = new Lab(50.0000f, 0.0000f, -82.7485f);
		assertClose(2.0425f, lab1.deltaE2000(lab2), "CIEDE2000 test case 1", 0.0001);

		// Case 2
		lab1 = new Lab(50.0000f, 3.1571f, -77.2803f);
		lab2 = new Lab(50.0000f, 0.0000f, -82.7485f);
		assertClose(2.8615f, lab1.deltaE2000(lab2), "CIEDE2000 test case 2", 0.0001);

		// Case 3
		lab1 = new Lab(50.0000f, 2.8361f, -74.0200f);
		lab2 = new Lab(50.0000f, 0.0000f, -82.7485f);
		assertClose(3.4412f, lab1.deltaE2000(lab2), "CIEDE2000 test case 3", 0.0001);

		// Test RGB convenience methods
		RGB rgb1 = new RGB(1, 0, 0); // Red
		RGB rgb2 = new RGB(0, 1, 0); // Green
		float deltaE = rgb1.deltaE2000(rgb2);
		assertTrue(deltaE > 80, "Red vs Green should have large deltaE (>80), got " + deltaE);

		// Test similar colors
		rgb1 = new RGB(0.5f, 0.5f, 0.5f);
		rgb2 = new RGB(0.51f, 0.5f, 0.5f);
		deltaE = rgb1.deltaE2000(rgb2);
		assertTrue(deltaE < 2, "Very similar grays should have small deltaE (<2), got " + deltaE);

		// Test black and white
		rgb1 = new RGB(0, 0, 0);
		rgb2 = new RGB(1, 1, 1);
		deltaE = rgb1.deltaE2000(rgb2);
		assertTrue(deltaE > 99, "Black vs White should have very large deltaE (>99), got " + deltaE);

		// Test with custom weights
		lab1 = new Lab(50, 10, 10);
		lab2 = new Lab(60, 10, 10);
		float deltaEDefault = lab1.deltaE2000(lab2);
		float deltaELightness = lab1.deltaE2000(lab2, 2, 1, 1); // Double lightness weight
		// With kL=2, the lightness component is divided by 2, so deltaE should be smaller
		assertTrue(deltaELightness < deltaEDefault, "Higher lightness weight should decrease deltaE (kL is divisor)");

		lab1 = new Lab(50, 0, 0);
		lab2 = new Lab(50, 20, 0);
		deltaEDefault = lab1.deltaE2000(lab2);
		float deltaEChroma = lab1.deltaE2000(lab2, 1, 2, 1); // Double chroma weight
		// With kC=2, the chroma component is divided by 2, so deltaE should be smaller
		assertTrue(deltaEChroma < deltaEDefault, "Higher chroma weight should decrease deltaE (kC is divisor)");

		// Test edge cases
		// Test with zero chroma (gray colors)
		lab1 = new Lab(50, 0, 0);
		lab2 = new Lab(60, 0, 0);
		deltaE = lab1.deltaE2000(lab2);
		assertTrue(deltaE > 0, "Different grays should have non-zero deltaE");

		// Test very small differences
		lab1 = new Lab(50.0000f, 0.0000f, 0.0000f);
		lab2 = new Lab(50.0001f, 0.0000f, 0.0000f);
		deltaE = lab1.deltaE2000(lab2);
		assertTrue(deltaE < 0.001, "Very small L difference should give very small deltaE");

		// Test hue differences
		lab1 = new Lab(50, 20, 0); // Red direction
		lab2 = new Lab(50, 0, 20); // Yellow direction
		float deltaEHue = lab1.deltaE2000(lab2);
		assertTrue(deltaEHue > 10, "90 degree hue difference should be significant");

		// Test opposite hues
		lab1 = new Lab(50, 20, 0);
		lab2 = new Lab(50, -20, 0);
		deltaE = lab1.deltaE2000(lab2);
		assertTrue(deltaE > 20, "Opposite hues should have large deltaE");

		// Test perceptual uniformity improvements over CIE76
		// CIEDE2000 should give more consistent results for blue differences
		Lab blue1 = new Lab(32.3f, 79.2f, -107.9f); // Blue
		Lab blue2 = new Lab(32.3f, 69.2f, -107.9f); // Slightly different blue
		Lab red1 = new Lab(53.2f, 80.1f, 67.2f); // Red
		Lab red2 = new Lab(53.2f, 70.1f, 67.2f); // Slightly different red

		// Both have same chroma difference (10 units)
		float deltaEBlue = blue1.deltaE2000(blue2);
		float deltaERed = red1.deltaE2000(red2);

		// CIEDE2000 should show these as more similar than CIE76 would
		// (The blue region correction in CIEDE2000 makes blue differences smaller)
		assertTrue(Math.abs(deltaEBlue - deltaERed) < 5, "CIEDE2000 should show more uniform perception across color space");
	}

	@Test
	public void testDMXConversions () {
		// Test DMX8 conversions
		float[] testValues = {0.0f, 0.25f, 0.5f, 0.75f, 1.0f, 0.123f, 0.456f, 0.789f, 1 / 255f, 0.5f / 255};

		for (float value : testValues) {
			int dmx = dmx8(value);
			int expected = Math.round(value * 255);
			assertEquals(expected, dmx, "DMX8 for value " + value);
			assertTrue(dmx >= 0 && dmx <= 255, "DMX8 value in range [0,255]");
		}

		// Test specific DMX8 values
		assertEquals(0, dmx8(0.0f), "DMX8 of 0");
		assertEquals(255, dmx8(1.0f), "DMX8 of 1");
		assertEquals(128, dmx8(0.5f), "DMX8 of 0.5");

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
		assertEquals(281, dmx8(1.1f), "DMX8 >1 not clamped");
		assertEquals(-6553, dmx16(-0.1f), "DMX16 negative not clamped");
		assertEquals(72088, dmx16(1.1f), "DMX16 >1 not clamped");
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
				assertClose(value, decoded, "Gamma " + gamma + " round trip for value " + value);

				// Test decode/encode round trip
				float linearFromEncoded = gammaDecode(value, gamma);
				float reencoded = gammaEncode(linearFromEncoded, gamma);
				assertClose(value, reencoded, "Inverse gamma " + gamma + " round trip for value " + value);
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
	public void testHunterLab () {
		// Test XYZ to Hunter Lab
		XYZ xyz = new XYZ(41.24f, 21.26f, 1.93f); // Red in XYZ
		HunterLab hunterLab = xyz.HunterLab();

		// Test round trip
		XYZ xyzBack = hunterLab.XYZ();
		assertRecordClose(xyz, xyzBack, "Hunter Lab round trip", EPSILON_F);

		// Test RGB to Hunter Lab
		RGB rgb = new RGB(1, 0, 0); // Pure red
		HunterLab hunterLabFromRgb = rgb.HunterLab();
		RGB rgbBack = hunterLabFromRgb.RGB();
		assertRecordClose(rgb, rgbBack, "RGB-HunterLab round trip", EPSILON_F);
	}

	@Test
	public void testLabLCh () {
		Lab lab = new Lab(50, 25, -25);
		LCh lch = lab.LCh();
		Lab labBack = lch.Lab();

		assertRecordClose(lab, labBack, "Lab <-> LCh round trip");

		// Test that C = sqrt(a² + b²)
		float expectedC = (float)Math.sqrt(25 * 25 + 25 * 25);
		assertClose(expectedC, lch.C(), "LCh chroma calculation");
	}

	@Test
	public void testLCh () {
		// Test Lab to LCh
		Lab lab = new Lab(53.23f, 80.11f, 67.22f); // Red in Lab
		LCh lch = lab.LCh();

		// Test round trip
		Lab labBack = lch.Lab();
		assertRecordClose(lab, labBack, "LCh round trip", EPSILON_F);

		// Test RGB to LCh with default illuminant
		RGB rgb = new RGB(1, 0, 0); // Pure red
		LCh lchFromRgb = rgb.LCh();
		RGB rgbBack = lchFromRgb.RGB();
		assertRecordClose(rgb, rgbBack, "RGB-LCh round trip", EPSILON_F);

		// Test RGB to LCh with custom illuminant
		LCh lchFromRgbD50 = rgb.LCh(CIE2.D50);
		// Convert back through Lab with the same illuminant
		Lab labFromLch = lchFromRgbD50.Lab();
		RGB rgbBackD50 = labFromLch.RGB(CIE2.D50);
		assertRecordClose(rgb, rgbBackD50, "RGB-LCh round trip with D50 illuminant", EPSILON_F);
	}

	@Test
	public void testLMS () {
		// Test all matrix types
		CAT[] matrices = {CAT.HPE, CAT.Bradford, CAT.VonKries, CAT.CAT97, CAT.CAT02};
		XYZ xyz = new XYZ(41.24f, 21.26f, 1.93f); // Red in XYZ

		for (CAT matrix : matrices) {
			// Test XYZ to LMS
			LMS lms = xyz.LMS(matrix);
			XYZ xyzBack = lms.XYZ(matrix);
			assertRecordClose(xyz, xyzBack, "LMS " + matrix + " round trip", EPSILON_F);

			// Test RGB to LMS
			RGB rgb = new RGB(1, 0, 0);
			LMS lmsFromRgb = rgb.LMS(matrix);
			RGB rgbBack = lmsFromRgb.RGB(matrix);
			assertRecordClose(rgb, rgbBack, "RGB-LMS " + matrix + " round trip", EPSILON_F);
		}

		// Test default (CAT02) conversions
		LMS lmsDefault = xyz.LMS();
		LMS lmsCat02 = xyz.LMS(CAT.CAT02);
		assertRecordClose(lmsDefault, lmsCat02, "Default LMS is CAT02", EPSILON_F);
	}

	@Test
	public void testMatrixMultiply () {
		float[] vector = {1, 2, 3};
		float[][] matrix = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

		float[] result = Util.matrixMultiply(vector[0], vector[1], vector[2], matrix);
		assertArrayClose(vector, result, "Identity matrix multiply");

		// Test with a known transformation
		float[][] scaleMatrix = {{2, 0, 0}, {0, 2, 0}, {0, 0, 2}};
		float[] scaled = Util.matrixMultiply(vector[0], vector[1], vector[2], scaleMatrix);
		assertArrayClose(new float[] {2, 4, 6}, scaled, "Scale matrix multiply");

		// Test matrixMultiply
		vector = new float[] {1, 2, 3};
		matrix = new float[][] {{1, 0, 0}, {0, 2, 0}, {0, 0, 3}};
		result = Util.matrixMultiply(vector[0], vector[1], vector[2], matrix);
		float[] expected = {1, 4, 9};
		assertArrayClose(expected, result, "Matrix multiply", EPSILON_F);

		// Test with non-diagonal matrix
		float[][] matrix2 = {{0.5f, 0.3f, 0.2f}, {0.1f, 0.6f, 0.3f}, {0.2f, 0.2f, 0.6f}};
		result = Util.matrixMultiply(vector[0], vector[1], vector[2], matrix2);
		expected = new float[] {1 * 0.5f + 2 * 0.3f + 3 * 0.2f, // 0.5 + 0.6 + 0.6 = 1.7
			1 * 0.1f + 2 * 0.6f + 3 * 0.3f, // 0.1 + 1.2 + 0.9 = 2.2
			1 * 0.2f + 2 * 0.2f + 3 * 0.6f // 0.2 + 0.4 + 1.8 = 2.4
		};
		assertArrayClose(expected, result, "Matrix multiply 2", EPSILON_F);

		// Test with identity matrix
		float[][] identity = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
		result = Util.matrixMultiply(0.5f, 0.3f, 0.7f, identity);
		expected = new float[] {0.5f, 0.3f, 0.7f};
		assertArrayClose(expected, result, "Identity matrix multiply", EPSILON_F);
	}

	@Test
	public void testSpecialConversions () {
		// Test rg
		RGB rgb = new RGB(0.8f, 0.2f, 0.1f);
		rg rg = rgb.rg();
		// Verify normalized RGB sums to 1
		float sum = rg.r() + rg.g() + rg.b();
		assertClose(1, sum, "rg normalized sum");

		// Test toC1C2C3
		C1C2C3 c1c2c3 = rgb.C1C2C3();
		// Verify c1c2c3 has reasonable values
		Assertions.assertNotNull(c1c2c3, "C1C2C3 conversion should not return null");
		Assertions.assertTrue(!Float.isNaN(c1c2c3.C1()), "C1 should not be NaN");
		Assertions.assertTrue(!Float.isNaN(c1c2c3.C2()), "C2 should not be NaN");
		Assertions.assertTrue(!Float.isNaN(c1c2c3.C3()), "C3 should not be NaN");

		// Test toO1O2
		O1O2 o1o2 = rgb.O1O2();
		// Verify o1o2 has reasonable values
		Assertions.assertNotNull(o1o2, "O1O2 conversion should not return null");
		Assertions.assertTrue(!Float.isNaN(o1o2.O1()), "O1 should not be NaN");
		Assertions.assertTrue(!Float.isNaN(o1o2.O2()), "O2 should not be NaN");

		// Test Grayscale
		float gray = rgb.grayscale();
		// Verify grayscale calculation
		float expectedGray = rgb.r() * 0.2125f + rgb.g() * 0.7154f + rgb.b() * 0.0721f;
		assertClose(expectedGray, gray, "Grayscale calculation");

		// Test rg
		rgb = new RGB(0.5f, 0.3f, 0.7f);
		rg = rgb.rg();
		// Check that r+g+b normalized values sum to 1
		assertClose(1, rg.r() + rg.g() + rg.b(), "rg sum");

		// Test RGB -> C1C2C3
		c1c2c3 = rgb.C1C2C3(); // Just verify it runs without error

		// Test RGB -> O1O2
		o1o2 = rgb.O1O2(); // Just verify it runs without error

		// Test RGB -> Grayscale
		gray = rgb.grayscale();
		// Verify it's within expected range
		assertTrue(gray >= 0 && gray <= 1, "Grayscale in range");

		// Test known grayscale values
		assertClose(0, new RGB(0, 0, 0).grayscale(), "Black grayscale");
		assertClose(1, new RGB(1, 1, 1).grayscale(), "White grayscale");
	}

	@Test
	public void testTSL () {
		RGB rgb = new RGB(0.2f, 0.5f, 0.8f);
		var tsl = rgb.TSL();

		// Test TSL attributes are reasonable
		assertTrue(tsl.T() >= 0 && tsl.T() <= 1, "T in range");
		assertTrue(tsl.S() >= 0 && tsl.S() <= 1, "S in range");
		assertTrue(tsl.L() >= 0 && tsl.L() <= 1, "L in range");

		// Test expected lightness calculation
		float expectedL = 0.299f * rgb.r() + 0.587f * rgb.g() + 0.114f * rgb.b();
		assertClose(expectedL, tsl.L(), "L", EPSILON_F);

		// Test grayscale
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		var tslGray = gray.TSL();
		assertClose(0, tslGray.S(), "S", EPSILON_F);

		// Test that different colors produce different tints
		var tsl1 = new RGB(1, 0, 0).TSL(); // Red
		var tsl2 = new RGB(0, 1, 0).TSL(); // Green
		var tsl3 = new RGB(0, 0, 1).TSL(); // Blue
		assertTrue(Math.abs(tsl1.T() - tsl2.T()) > 0.1, "Red and green have different tints");
		assertTrue(Math.abs(tsl2.T() - tsl3.T()) > 0.1, "Green and blue have different tints");
		assertTrue(Math.abs(tsl1.T() - tsl3.T()) > 0.1, "Red and blue have different tints");

		// Test Terrillon-Akamatsu S calculation
		// For pure colors, verify S is calculated correctly
		// Red (1,0,0): r'=1-1/3=2/3, g'=0-1/3=-1/3, S=sqrt(9/5*(4/9+1/9))=sqrt(9/5*5/9)=1
		assertClose(1, tsl1.S(), "Red S", 0.001f);

		// Test 2*G = R+B constraint
		// When 2*G = R+B, in normalized coordinates: 2*g/(r+g+b) = r/(r+g+b) + b/(r+g+b)
		// This simplifies to: 2*g = r+b, or g = (r+b)/2
		// In normalized form: g' = g/(r+g+b) - 1/3 = (r+b)/2/(r+g+b) - 1/3
		// And r' = r/(r+g+b) - 1/3, b' = b/(r+g+b) - 1/3
		// Since r+g+b = r+(r+b)/2+b = 3(r+b)/2, we have:
		// g' = 1/3 - 1/3 = 0, and r' + b' = 2/3 - 2/3 = 0
		// Therefore g' = r' + b' = 0, which means g' - r' = -r' and g' - b' = -b'
		// So tan(T) = -r'/(-b') = r'/b'

		// Test with RGB values satisfying 2*G = R+B
		RGB rgb2g = new RGB(0.3f, 0.4f, 0.5f); // 2*0.4 = 0.3+0.5
		var tsl2g = rgb2g.TSL();
		// Verify the RGB constraint
		assertClose(2 * rgb2g.g(), rgb2g.r() + rgb2g.b(), "2*G = R+B", EPSILON_F);

		// When 2*G = R+B and r≠b, T should reflect the r'/b' ratio
		// Calculate expected angle from normalized coordinates
		float sum = rgb2g.r() + rgb2g.g() + rgb2g.b();
		float r_prime = rgb2g.r() / sum - 1.0f / 3.0f;
		float g_prime = rgb2g.g() / sum - 1.0f / 3.0f;
		float b_prime = rgb2g.b() / sum - 1.0f / 3.0f;

		// Verify g' = 0 when 2*G = R+B
		assertClose(0, g_prime, "g' should be 0 when 2*G = R+B", 0.001f);

		// Verify TSL values are reasonable
		assertTrue(tsl2g.T() >= 0 && tsl2g.T() <= 1, "T in range for 2*G = R+B case");
		assertTrue(tsl2g.S() >= 0 && tsl2g.S() <= 1, "S in range for 2*G = R+B case");
		assertTrue(tsl2g.L() >= 0 && tsl2g.L() <= 1, "L in range for 2*G = R+B case");

		// Calculate expected tint angle
		// T = atan2(g' - r', 2*g' + r')
		// When g' = 0: T = atan2(-r', r') = atan2(-1, 1) for r' > 0
		float expectedAngleRad = (float)Math.atan2(g_prime - r_prime, 2 * g_prime + r_prime);
		float expectedAngleDeg = expectedAngleRad * 180 / PI;
		if (expectedAngleDeg < 0) expectedAngleDeg += 360;
		float expectedT = expectedAngleDeg / 360;
		assertClose(expectedT, tsl2g.T(), "T value for 2*G = R+B case", 0.01f);

		// Verify r' + b' = 0
		assertClose(0, r_prime + b_prime, "r' + b' = 0 when 2*G = R+B", 0.001f);
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
			uv uv = rgb.uv();
			RGB rgbBack = uv.RGB();

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
			xy xy = uv.xy();
			uv uvBack = xy.uv();
			assertRecordClose(uv, uvBack, "UV1976 <-> xy round trip", EPSILON_F);
		}
	}

	@Test
	public void testXYZxyY () {
		// Convert RGB to XYZ first
		RGB rgb = new RGB(0.5f, 0.3f, 0.7f);
		XYZ xyz = rgb.XYZ();
		xyY xyy = xyz.xyY();
		XYZ xyzBack = xyy.XYZ();
		assertRecordClose(xyz, xyzBack, "XYZ <-> xyY round trip");
	}

	static void roundTripYCbCr (RGB rgb, YCbCrColorSpace colorSpace, double epsilon) {
		YCbCr ycbcr = rgb.YCbCr(colorSpace);
		RGB back = ycbcr.RGB(colorSpace);
		try {
			assertRecordClose(rgb, back, "YCbCr " + colorSpace + " round trip", epsilon);
		} catch (AssertionError e) {
			throw e;
		}
	}

	@Test
	public void testEdgeCases () {
		// Test NaN handling
		RGB nanRGB = new RGB(Float.NaN, 0.5f, 0.5f);
		XYZ nanXYZ = nanRGB.XYZ();
		assertTrue(Float.isNaN(nanXYZ.X()), "NaN propagation in XYZ");

		// Test infinity handling
		RGB infRGB = new RGB(Float.POSITIVE_INFINITY, 0.5f, 0.5f);
		XYZ infXYZ = infRGB.XYZ();
		assertTrue(Float.isInfinite(infXYZ.X()), "Infinity propagation in XYZ");

		// Test boundary values
		RGB black = new RGB(0, 0, 0);
		RGB white = new RGB(1, 1, 1);

		// Test HSL with achromatic colors (hue undefined)
		var hslBlack = black.HSL();
		assertClose(0, hslBlack.S(), "Black has 0 saturation", EPSILON_F);
		assertClose(0, hslBlack.L(), "Black has 0 lightness", EPSILON_F);

		var hslWhite = white.HSL();
		assertClose(0, hslWhite.S(), "White has 0 saturation", EPSILON_F);
		assertClose(1, hslWhite.L(), "White has 1 lightness", EPSILON_F);

		// Test HSV with achromatic colors
		var hsvGray = new RGB(0.5f, 0.5f, 0.5f).HSV();
		assertClose(0, hsvGray.S(), "Gray has 0 saturation", EPSILON_F);

		// Test division by zero scenarios
		RGB almostBlack = new RGB(0.00001f, 0.00001f, 0.00001f);
		var labAlmostBlack = almostBlack.Lab();
		assertTrue(labAlmostBlack.L() >= 0, "Lab L must be non-negative");

		// Test values outside [0,1] range
		RGB outOfRange = new RGB(-0.1f, 1.2f, 0.5f);
		var xyzOut = outOfRange.XYZ();
		// XYZ can represent out-of-gamut colors
		assertTrue(xyzOut.Y() >= -10 && xyzOut.Y() <= 200, "XYZ Y in reasonable range");

		// Test extreme Lab values
		Lab extremeLab = new Lab(100, 127, 127);
		RGB rgbExtreme = extremeLab.RGB();
		// Should be clamped to valid range
		assertTrue(rgbExtreme.r() >= 0 && rgbExtreme.r() <= 1, "R clamped");
		assertTrue(rgbExtreme.g() >= 0 && rgbExtreme.g() <= 1, "G clamped");
		assertTrue(rgbExtreme.b() >= 0 && rgbExtreme.b() <= 1, "B clamped");
	}

	@Test
	public void testOtherMethods () {
		RGB d65 = new RGB(1, 1, 1);
		float duvD65 = d65.xy().Duv();
		assertTrue(Math.abs(duvD65) < 0.1f, "D65 should be close to Planckian locus: " + duvD65);

		uv uvPoint = new uv(0.2105f, 0.4737f); // D65 in u'v'
		float duvDirect = uvPoint.xy().Duv();
		assertTrue(Math.abs(duvDirect) < 0.1f, "D65 UV should be close to Planckian locus");

		// Test MacAdamSteps - method not implemented yet
		RGB color1 = new RGB(0.5f, 0.5f, 0.5f);
		RGB color2 = new RGB(0.51f, 0.51f, 0.51f);
		float steps = color1.xy().MacAdamSteps(color2.xy());
		assertTrue(steps > 0, "MacAdam steps must be positive");
		assertTrue(steps < 10, "Small difference should be few MacAdam steps");

		// Test clamp functions
		assertClose(0.5f, clamp(0.5f), "Clamp in range", EPSILON_F);
		assertClose(0.0f, clamp(-0.1f), "Clamp negative", EPSILON_F);
		assertClose(1.0f, clamp(1.1f), "Clamp over 1", EPSILON_F);

		// Test linear/sRGB conversions
		float srgbValue = 0.5f;
		float linearValue = linear(srgbValue);
		float backToSRGB = sRGB(linearValue);
		assertClose(srgbValue, backToSRGB, "sRGB <-> linear round trip", EPSILON_F);

		// Test linear/sRGB edge cases
		assertClose(0.0f, linear(0.0f), "Linear of 0", EPSILON_F);
		assertClose(1.0f, linear(1.0f), "Linear of 1", EPSILON_F);
		assertClose(0.0f, sRGB(0.0f), "sRGB of 0", EPSILON_F);
		assertClose(1.0f, sRGB(1.0f), "sRGB of 1", EPSILON_F);

		// Test linear cutoff point
		float cutoff = 0.04045f;
		float belowCutoff = linear(cutoff / 2);
		float aboveCutoff = linear(cutoff * 2);
		assertTrue(belowCutoff < aboveCutoff, "Linear function is monotonic");
	}

	@Test
	public void testMoreColorSpaces () {
		RGB[] testColors = {new RGB(0, 0, 0), new RGB(1, 1, 1), new RGB(0.5f, 0.5f, 0.5f), new RGB(1, 0, 0),
			new RGB(0.2f, 0.5f, 0.8f), new RGB(0.5f, 0.8f, 0.2f), new RGB(0.8f, 0.2f, 0.5f), new RGB(0.33f, 0.17f, 0.19f)};

		for (RGB rgb : testColors) {
			var linear = rgb.LinearRGB();
			RGB back = linear.RGB();
			assertRecordClose(rgb, back, "LinearRGB round trip", EPSILON_F);
		}

		// Test LCHuv round trips
		for (RGB rgb : testColors) {
			var luv = rgb.Luv();
			var lchuv = luv.LCHuv();
			var luvBack = lchuv.Luv();
			RGB back = luvBack.RGB();
			if (rgb.r() > 0 || rgb.g() > 0 || rgb.b() > 0) { // Skip black (hue undefined)
				assertRecordClose(rgb, back, "LCHuv round trip", 0.01f);
			}
		}

		// Test xy chromaticity properties
		for (RGB rgb : testColors) {
			if (rgb.r() == 0 && rgb.g() == 0 && rgb.b() == 0) continue; // Skip black

			// Test that xy -> xyY -> xy preserves chromaticity perfectly
			XYZ xyz = rgb.XYZ();
			xy chromaticity = xyz.xy();
			xyY xyy = new xyY(chromaticity.x(), chromaticity.y(), 50); // arbitrary Y
			xy xyFromXyy = new xy(xyy.x(), xyy.y());
			assertRecordClose(chromaticity, xyFromXyy, "xy -> xyY -> xy preserves chromaticity");

			// Test that different Y values produce different RGB but same chromaticity direction
			XYZ xyz1 = new xyY(chromaticity.x(), chromaticity.y(), 20).XYZ();
			XYZ xyz2 = new xyY(chromaticity.x(), chromaticity.y(), 80).XYZ();
			RGB rgb1 = xyz1.RGB();
			RGB rgb2 = xyz2.RGB();

			// The colors should be different (different luminance)
			assertTrue(Math.abs(rgb1.r() - rgb2.r()) > 0.01 || //
				Math.abs(rgb1.g() - rgb2.g()) > 0.01 || //
				Math.abs(rgb1.b() - rgb2.b()) > 0.01, "Different Y values should produce different RGB");

			// But if neither is clipped, they should have the same chromaticity
			boolean rgb1Clipped = rgb1.r() >= 0.999f || rgb1.g() >= 0.999f || rgb1.b() >= 0.999f //
				|| rgb1.r() <= 0.001f || rgb1.g() <= 0.001f || rgb1.b() <= 0.001f;
			boolean rgb2Clipped = rgb2.r() >= 0.999f || rgb2.g() >= 0.999f || rgb2.b() >= 0.999f //
				|| rgb2.r() <= 0.001f || rgb2.g() <= 0.001f || rgb2.b() <= 0.001f;

			if (!rgb1Clipped && !rgb2Clipped) {
				xy chrom1 = rgb1.XYZ().xy();
				xy chrom2 = rgb2.XYZ().xy();
				assertRecordClose(chrom1, chrom2, "Same xy with different Y should preserve chromaticity", 0.001f);
			}
		}

		// Test xyY round trips
		for (RGB rgb : testColors) {
			if (rgb.r() == 0 && rgb.g() == 0 && rgb.b() == 0) continue; // Skip black
			XYZ xyz = rgb.XYZ();
			xyY xyy = xyz.xyY();
			XYZ xyzBack = xyy.XYZ();
			RGB back = xyzBack.RGB();
			assertRecordClose(rgb, back, "xyY round trip", 0.01f);
		}
	}

	@Test
	public void testHueWraparound () {
		// Test HSL hue wraparound
		var hsl1 = new HSL(-10, 0.5f, 0.5f); // Negative hue
		var hsl2 = new HSL(350, 0.5f, 0.5f); // Equivalent positive hue
		RGB rgb1 = hsl1.RGB();
		RGB rgb2 = hsl2.RGB();
		assertRecordClose(rgb1, rgb2, "HSL negative hue wraparound", EPSILON_F);

		// Test HSL hue > 360
		var hsl3 = new HSL(370, 0.5f, 0.5f);
		var hsl4 = new HSL(10, 0.5f, 0.5f);
		RGB rgb3 = hsl3.RGB();
		RGB rgb4 = hsl4.RGB();
		assertRecordClose(rgb3, rgb4, "HSL hue > 360 wraparound", EPSILON_F);

		// Test HSV hue wraparound
		var hsv1 = new HSV(-10, 0.5f, 0.5f);
		var hsv2 = new HSV(350, 0.5f, 0.5f);
		RGB rgbHsv1 = hsv1.RGB();
		RGB rgbHsv2 = hsv2.RGB();
		assertRecordClose(rgbHsv1, rgbHsv2, "HSV negative hue wraparound", EPSILON_F);
	}

	@Test
	public void testExtremeLabValues () {
		// Test extreme L* values
		Lab[] extremeLabs = {new Lab(0, 0, 0), // Black
			new Lab(100, 0, 0), // White
			new Lab(50, 128, 0), // Extreme positive a*
			new Lab(50, -128, 0), // Extreme negative a*
			new Lab(50, 0, 128), // Extreme positive b*
			new Lab(50, 0, -128), // Extreme negative b*
			new Lab(50, 100, 100), // High chroma
		};

		for (Lab lab : extremeLabs) {
			RGB rgb = lab.RGB();
			// Values should be clamped to valid range
			assertTrue(rgb.r() >= 0 && rgb.r() <= 1, "R in range for Lab " + lab);
			assertTrue(rgb.g() >= 0 && rgb.g() <= 1, "G in range for Lab " + lab);
			assertTrue(rgb.b() >= 0 && rgb.b() <= 1, "B in range for Lab " + lab);
		}
	}

	@Test
	public void testUtilMethods () {
		// Test complementary colors - using HSL hue shift
		RGB red = new RGB(1, 0, 0);
		var hsl = red.HSL();
		var compHsl = new HSL((hsl.H() + 180) % 360, hsl.S(), hsl.L());
		RGB comp = compHsl.RGB();
		assertTrue(comp.g() > comp.r(), "Complementary of red has more green");
		assertTrue(comp.b() > comp.r(), "Complementary of red has more blue");

		// Test simple contrast calculation
		RGB black = new RGB(0, 0, 0);
		RGB white = new RGB(1, 1, 1);
		// Relative luminance: Y = 0.2126 * R + 0.7152 * G + 0.0722 * B
		float lumBlack = 0.2126f * black.r() + 0.7152f * black.g() + 0.0722f * black.b();
		float lumWhite = 0.2126f * white.r() + 0.7152f * white.g() + 0.0722f * white.b();
		float contrast = (lumWhite + 0.05f) / (lumBlack + 0.05f);
		assertClose(21.0f, contrast, "Black/white contrast ratio", 0.1f);

		// Test WCAG compliance thresholds
		assertTrue(contrast >= 4.5f, "Black/white passes WCAG AA");
		assertTrue(contrast >= 7.0f, "Black/white passes WCAG AAA");

		// Test low contrast
		RGB gray1 = new RGB(0.4f, 0.4f, 0.4f);
		RGB gray2 = new RGB(0.5f, 0.5f, 0.5f);
		float lumGray1 = 0.2126f * gray1.r() + 0.7152f * gray1.g() + 0.0722f * gray1.b();
		float lumGray2 = 0.2126f * gray2.r() + 0.7152f * gray2.g() + 0.0722f * gray2.b();
		float lowContrast = (lumGray2 + 0.05f) / (lumGray1 + 0.05f);
		assertTrue(lowContrast < 2, "Similar grays have low contrast");
		assertTrue(lowContrast < 4.5f, "Low contrast fails WCAG AA");
	}

	@Test
	public void testHexConversions () {
		// Test hex() method with various color types
		RGB rgb = new RGB(0.5f, 0.25f, 0.75f);
		String hexRgb = hex(rgb);
		Assertions.assertEquals("8040bf", hexRgb.toLowerCase());

		// Test with CMYK
		var cmyk = rgb.CMYK();
		String hexCmyk = hex(cmyk);
		Assertions.assertNotNull(hexCmyk);

		// Test toString methods
		String rgbStr = Util.toString(rgb);
		assertTrue(rgbStr.contains("0.5"), "toString contains R value");

		String rgb255Str = toString255(rgb);
		assertTrue(rgb255Str.contains("128"), "toString255 contains R value scaled to 255");
	}

	@Test
	public void testYCbCrAllColorSpaces () {
		// Test all YCbCr color spaces
		RGB testColor = new RGB(0.5f, 0.3f, 0.7f);
		float tolerance = 0.001f; // Some color spaces have lower precision
		for (YCbCrColorSpace cs : YCbCrColorSpace.values()) {
			var ycbcr = testColor.YCbCr(cs);
			RGB back = ycbcr.RGB(cs);
			assertRecordClose(testColor, back, "YCbCr " + cs + " round trip", tolerance);
		}
	}

	@Test
	public void testGamutBoundaries () {
		// Test colors at gamut boundaries
		RGB[] boundaryColors = {new RGB(1, 0, 0), // Pure red
			new RGB(0, 1, 0), // Pure green
			new RGB(0, 0, 1), // Pure blue
			new RGB(1, 1, 0), // Yellow
			new RGB(1, 0, 1), // Magenta
			new RGB(0, 1, 1), // Cyan
			new RGB(1, 0.5f, 0), // Orange
			new RGB(0.5f, 0, 1), // Purple
		};

		for (RGB boundary : boundaryColors) {
			// Test that extreme colors maintain high chroma in perceptual spaces
			var lab = boundary.Lab();
			float chroma = (float)Math.sqrt(lab.a() * lab.a() + lab.b() * lab.b());
			assertTrue(chroma > 30, "Boundary color has high chroma in Lab");

			var oklab = boundary.Oklab();
			float chromaOk = (float)Math.sqrt(oklab.a() * oklab.a() + oklab.b() * oklab.b());
			assertTrue(chromaOk > 0.1f, "Boundary color has high chroma in Oklab");
		}
	}

	@Test
	public void testChromaticAdaptation () {
		// Test different illuminants with Lab
		RGB testColor = new RGB(0.5f, 0.3f, 0.7f);

		// Test Lab with D50 illuminant
		XYZ xyzD65 = testColor.XYZ();
		Lab labD65 = xyzD65.Lab();
		Lab labD50 = xyzD65.Lab(CIE2.D50);

		// Colors should be different under different illuminants
		assertTrue(Math.abs(labD65.L() - labD50.L()) > 0.01 //
			|| Math.abs(labD65.a() - labD50.a()) > 0.01 //
			|| Math.abs(labD65.b() - labD50.b()) > 0.01, "Lab values differ under different illuminants");
	}

	@Test
	public void testLMSConversions () {
		// Test LMS color space conversions
		RGB[] testColors = {new RGB(0, 0, 0), new RGB(1, 1, 1), new RGB(1, 0, 0), new RGB(0.5f, 0.3f, 0.7f)};

		for (RGB rgb : testColors) {
			var lms = rgb.LMS();
			RGB back = lms.RGB();
			assertRecordClose(rgb, back, "LMS round trip", 0.001f);

			// LMS values should be non-negative for visible colors
			assertTrue(lms.L() >= 0, "L >= 0");
			assertTrue(lms.M() >= 0, "M >= 0");
			assertTrue(lms.S() >= 0, "S >= 0");
		}
	}

	@Test
	public void testOklabXYZConversions () {
		// Test Oklab <-> XYZ conversions
		RGB[] testColors = {new RGB(0, 0, 0), // Black
			new RGB(1, 1, 1), // White
			new RGB(1, 0, 0), // Red
			new RGB(0, 1, 0), // Green
			new RGB(0, 0, 1), // Blue
			new RGB(0.5f, 0.3f, 0.7f), // Arbitrary color
			new RGB(0.18f, 0.18f, 0.18f), // 18% gray
		};

		for (RGB rgb : testColors) {
			// Test RGB -> Oklab -> XYZ -> RGB round trip
			Oklab oklab = rgb.Oklab();
			XYZ xyz = oklab.XYZ();
			RGB rgbBack = xyz.RGB();
			assertRecordClose(rgb, rgbBack, "RGB -> Oklab -> XYZ -> RGB round trip", 0.001f);

			// Test RGB -> XYZ -> Oklab -> RGB round trip
			XYZ xyz2 = rgb.XYZ();
			Oklab oklab2 = xyz2.Oklab();
			RGB rgbBack2 = oklab2.RGB();
			assertRecordClose(rgb, rgbBack2, "RGB -> XYZ -> Oklab -> RGB round trip", 0.001f);

			// Test that Oklab values are reasonable
			assertTrue(oklab.L() >= 0 && oklab.L() <= 1, "Oklab L in range [0,1]");
			assertTrue(Math.abs(oklab.a()) < 0.5f, "Oklab a in reasonable range");
			assertTrue(Math.abs(oklab.b()) < 0.5f, "Oklab b in reasonable range");

			// Test that direct conversions match indirect ones
			Oklab oklabDirect = rgb.Oklab();
			Oklab oklabViaXYZ = rgb.XYZ().Oklab();
			assertRecordClose(oklabDirect, oklabViaXYZ, "Direct vs XYZ path to Oklab", 0.001f);
		}

		// Test wide gamut preservation
		// Create an XYZ color outside sRGB gamut
		XYZ wideGamutXYZ = new XYZ(150, 100, 50); // Bright, outside sRGB
		Oklab oklabWide = wideGamutXYZ.Oklab();
		XYZ xyzBack = oklabWide.XYZ();
		assertRecordClose(wideGamutXYZ, xyzBack, "Wide gamut XYZ -> Oklab -> XYZ preserves values", 0.1f);

		// Test that Oklab(XYZ) handles edge cases
		XYZ blackXYZ = new XYZ(0, 0, 0);
		Oklab blackOklab = blackXYZ.Oklab();
		assertClose(0, blackOklab.L(), "Black has L=0 in Oklab");
		assertClose(0, blackOklab.a(), "Black has a=0 in Oklab");
		assertClose(0, blackOklab.b(), "Black has b=0 in Oklab");
	}

	@Test
	public void testOtherColorSpaces () {
		// Test remaining untested color spaces
		RGB testColor = new RGB(0.5f, 0.3f, 0.7f);

		// Test YCC
		var ycc = testColor.YCC();
		RGB backYCC = ycc.RGB();
		assertRecordClose(testColor, backYCC, "YCC round trip", 0.001f);

		// Test YIQ
		var yiq = testColor.YIQ();
		RGB backYIQ = yiq.RGB();
		assertRecordClose(testColor, backYIQ, "YIQ round trip", 0.001f);

		// Test YUV
		var yuv = testColor.YUV();
		RGB backYUV = yuv.RGB();
		assertRecordClose(testColor, backYUV, "YUV round trip", 0.001f);

		// Test YCoCg
		var ycocg = testColor.YCoCg();
		RGB backYCoCg = ycocg.RGB();
		assertRecordClose(testColor, backYCoCg, "YCoCg round trip", 0.001f);

		// Test YES
		var yes = testColor.YES();
		RGB backYES = yes.RGB();
		assertRecordClose(testColor, backYES, "YES round trip", 0.001f);

		// Test IHS
		var ihs = testColor.IHS();
		RGB backIHS = ihs.RGB();
		assertRecordClose(testColor, backIHS, "IHS round trip", 0.001f);
	}

	@Test
	public void testInterpolation () {
		// Test simple linear interpolation in different color spaces
		RGB color1 = new RGB(1, 0, 0); // Red
		RGB color2 = new RGB(0, 0, 1); // Blue

		// Test RGB interpolation manually
		float t = 0.5f;
		RGB midRGB = new RGB(color1.r() * (1 - t) + color2.r() * t, color1.g() * (1 - t) + color2.g() * t,
			color1.b() * (1 - t) + color2.b() * t);
		assertClose(0.5f, midRGB.r(), "Mid RGB R", EPSILON_F);
		assertClose(0, midRGB.g(), "Mid RGB G", EPSILON_F);
		assertClose(0.5f, midRGB.b(), "Mid RGB B", EPSILON_F);

		// Test Oklab interpolation preserves perceptual uniformity better
		var oklab1 = color1.Oklab();
		var oklab2 = color2.Oklab();
		var midOklab = new Oklab(oklab1.L() * (1 - t) + oklab2.L() * t, oklab1.a() * (1 - t) + oklab2.a() * t,
			oklab1.b() * (1 - t) + oklab2.b() * t);
		RGB midOklabRGB = midOklab.RGB();

		// Middle should be purplish, not dark
		float brightness = (midOklabRGB.r() + midOklabRGB.g() + midOklabRGB.b()) / 3;
		assertTrue(brightness > 0.2f, "Oklab interpolation maintains brightness");
	}

	@Test
	public void testSpecialMatrices () {
		// Test matrix multiplication consistency
		RGB testRGB = new RGB(0.5f, 0.3f, 0.7f);

		// Test RGB to XYZ and back
		XYZ xyz = testRGB.XYZ();
		RGB backRGB = xyz.RGB();
		assertRecordClose(testRGB, backRGB, "RGB <-> XYZ round trip", 0.001f);

		// Test LMS conversions
		LMS lms = testRGB.LMS();
		RGB backFromLMS = lms.RGB();
		assertRecordClose(testRGB, backFromLMS, "RGB <-> LMS round trip", 0.001f);
	}

	@Test
	public void testNumericalStability () {
		// Test with very small values
		RGB tiny = new RGB(1e-6f, 1e-6f, 1e-6f);
		var labTiny = tiny.Lab();
		assertTrue(!Float.isNaN(labTiny.L()), "Lab L not NaN for tiny values");
		assertTrue(!Float.isNaN(labTiny.a()), "Lab a not NaN for tiny values");
		assertTrue(!Float.isNaN(labTiny.b()), "Lab b not NaN for tiny values");

		// Test repeated conversions for error accumulation
		RGB start = new RGB(0.5f, 0.3f, 0.7f);
		RGB current = start;

		for (int i = 0; i < 10; i++) {
			var lab = current.Lab();
			current = lab.RGB();
		}

		// Error should not accumulate significantly
		assertRecordClose(start, current, "Lab conversion stability", 0.01f);
	}
}
