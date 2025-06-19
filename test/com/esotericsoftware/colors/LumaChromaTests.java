
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.TestsUtil.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.colors.Colors.RGB;
import com.esotericsoftware.colors.Colors.YCC;
import com.esotericsoftware.colors.Colors.YCbCr;
import com.esotericsoftware.colors.Colors.YCbCrColorSpace;
import com.esotericsoftware.colors.Colors.YIQ;
import com.esotericsoftware.colors.Colors.YUV;

public class LumaChromaTests {
	@Test
	public void testYCbCr () {
		// Test ITU_BT_601
		RGB rgb601 = new RGB(0.5f, 0.3f, 0.7f);
		YCbCr ycbcr601 = YCbCr(rgb601, YCbCrColorSpace.ITU_BT_601);
		RGB back601 = RGB(ycbcr601, YCbCrColorSpace.ITU_BT_601);
		assertRecordClose(rgb601, back601, "YCbCr ITU_BT_601 round trip", 0.005);

		// Test ITU_BT_709_HDTV
		RGB rgb709 = new RGB(0.5f, 0.3f, 0.7f);
		YCbCr ycbcr709 = YCbCr(rgb709, YCbCrColorSpace.ITU_BT_709_HDTV);
		RGB back709 = RGB(ycbcr709, YCbCrColorSpace.ITU_BT_709_HDTV);
		assertRecordClose(rgb709, back709, "YCbCr ITU_BT_709_HDTV round trip", 0.005);
	}

	@Test
	public void testYCC () {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		YCC yccRed = YCC(red);
		RGB redBack = RGB(yccRed);
		assertRecordClose(red, redBack, "YCC red round trip", 0.001f);
		// Verify Y component for red
		assertClose(0.213f, yccRed.Y(), "Red Y component", 0.001f);

		RGB green = new RGB(0, 1, 0);
		YCC yccGreen = YCC(green);
		RGB greenBack = RGB(yccGreen);
		assertRecordClose(green, greenBack, "YCC green round trip", 0.001f);
		// Verify Y component for green
		assertClose(0.419f, yccGreen.Y(), "Green Y component", 0.001f);

		RGB blue = new RGB(0, 0, 1);
		YCC yccBlue = YCC(blue);
		RGB blueBack = RGB(yccBlue);
		assertRecordClose(blue, blueBack, "YCC blue round trip", 0.001f);
		// Verify Y component for blue
		assertClose(0.081f, yccBlue.Y(), "Blue Y component", 0.001f);

		// Test black and white
		RGB black = new RGB(0, 0, 0);
		YCC yccBlack = YCC(black);
		RGB blackBack = RGB(yccBlack);
		assertRecordClose(black, blackBack, "YCC black round trip", 0.001f);
		assertClose(0, yccBlack.Y(), "Black Y component", 0.001f);
		assertClose(0.612f, yccBlack.C1(), "Black C1 component", 0.001f);
		assertClose(0.537f, yccBlack.C2(), "Black C2 component", 0.001f);

		RGB white = new RGB(1, 1, 1);
		YCC yccWhite = YCC(white);
		RGB whiteBack = RGB(yccWhite);
		assertRecordClose(white, whiteBack, "YCC white round trip", 0.001f);
		assertClose(0.213f + 0.419f + 0.081f, yccWhite.Y(), "White Y component", 0.001f);

		// Test grays - should have neutral chroma
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		YCC yccGray = YCC(gray);
		RGB grayBack = RGB(yccGray);
		assertRecordClose(gray, grayBack, "YCC gray round trip", 0.001f);
		assertClose(0.612f, yccGray.C1(), "Gray C1 should be neutral", 0.001f);
		assertClose(0.537f, yccGray.C2(), "Gray C2 should be neutral", 0.001f);

		// Test secondary colors
		RGB yellow = new RGB(1, 1, 0);
		YCC yccYellow = YCC(yellow);
		RGB yellowBack = RGB(yccYellow);
		assertRecordClose(yellow, yellowBack, "YCC yellow round trip", 0.001f);

		RGB cyan = new RGB(0, 1, 1);
		YCC yccCyan = YCC(cyan);
		RGB cyanBack = RGB(yccCyan);
		assertRecordClose(cyan, cyanBack, "YCC cyan round trip", 0.001f);

		RGB magenta = new RGB(1, 0, 1);
		YCC yccMagenta = YCC(magenta);
		RGB magentaBack = RGB(yccMagenta);
		assertRecordClose(magenta, magentaBack, "YCC magenta round trip", 0.001f);

		// Test systematic round-trip accuracy
		float[] testValues = {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
		for (float r : testValues) {
			for (float g : testValues) {
				for (float b : testValues) {
					RGB rgb = new RGB(r, g, b);
					YCC ycc = YCC(rgb);
					RGB rgbBack = RGB(ycc);
					assertRecordClose(rgb, rgbBack, "YCC round trip " + rgb, 0.001f);

					// Verify Y is in valid range [0..1]
					assertTrue(ycc.Y() >= 0 && ycc.Y() <= 1, "Y component in range for " + rgb);

					// Verify C1 and C2 are in reasonable ranges
					// With offsets, C1 should be around [0.112..1.112] and C2 around [0.037..1.037]
					assertTrue(ycc.C1() >= 0 && ycc.C1() <= 1.2f, "C1 component in expected range for " + rgb);
					assertTrue(ycc.C2() >= 0 && ycc.C2() <= 1.2f, "C2 component in expected range for " + rgb);
				}
			}
		}

		// Test edge cases - very dark and very bright colors
		RGB darkColor = new RGB(0.01f, 0.02f, 0.03f);
		YCC yccDark = YCC(darkColor);
		RGB darkBack = RGB(yccDark);
		assertRecordClose(darkColor, darkBack, "YCC dark color round trip", 0.001f);

		RGB brightColor = new RGB(0.98f, 0.97f, 0.99f);
		YCC yccBright = YCC(brightColor);
		RGB brightBack = RGB(yccBright);
		assertRecordClose(brightColor, brightBack, "YCC bright color round trip", 0.001f);

		// Test specific color that was in original test
		RGB testColor = new RGB(0.5f, 0.3f, 0.7f);
		YCC yccTest = YCC(testColor);
		RGB testBack = RGB(yccTest);
		assertRecordClose(testColor, testBack, "YCC test color round trip", 0.001f);
		// Verify the Y calculation: 0.213*0.5 + 0.419*0.3 + 0.081*0.7 = 0.2887
		assertClose(0.2887f, yccTest.Y(), "YCC Y component for test color", 0.001f);

		// Test that chroma components properly encode color differences
		// For gray, C1 and C2 should be at their offset values
		RGB gray1 = new RGB(0.3f, 0.3f, 0.3f);
		YCC yccGray1 = YCC(gray1);
		assertClose(0.612f, yccGray1.C1(), "Gray should have neutral C1", 0.001f);
		assertClose(0.537f, yccGray1.C2(), "Gray should have neutral C2", 0.001f);

		// Colors with same luminance but different chromaticity should have different C1/C2
		RGB color1 = new RGB(0.5f, 0.3f, 0.2f);
		RGB color2 = new RGB(0.2f, 0.5f, 0.3f);
		YCC ycc1 = YCC(color1);
		YCC ycc2 = YCC(color2);
		assertTrue(Math.abs(ycc1.C1() - ycc2.C1()) > 0.01f, "Different colors should have different C1");
		assertTrue(Math.abs(ycc1.C2() - ycc2.C2()) > 0.01f, "Different colors should have different C2");
	}

	@Test
	public void testYCoCg () {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		var ycocgRed = YCoCg(red);
		RGB redBack = RGB(ycocgRed);
		assertRecordClose(red, redBack, "YCoCg red round trip", 0.0001f);

		RGB green = new RGB(0, 1, 0);
		var ycocgGreen = YCoCg(green);
		RGB greenBack = RGB(ycocgGreen);
		assertRecordClose(green, greenBack, "YCoCg green round trip", 0.0001f);

		RGB blue = new RGB(0, 0, 1);
		var ycocgBlue = YCoCg(blue);
		RGB blueBack = RGB(ycocgBlue);
		assertRecordClose(blue, blueBack, "YCoCg blue round trip", 0.0001f);

		// Test black and white
		RGB black = new RGB(0, 0, 0);
		var ycocgBlack = YCoCg(black);
		RGB blackBack = RGB(ycocgBlack);
		assertRecordClose(black, blackBack, "YCoCg black round trip", 0.0001f);
		assertClose(0, ycocgBlack.Y(), "Black Y", 0.0001f);
		assertClose(0, ycocgBlack.Co(), "Black Co should be neutral", 0.0001f);
		assertClose(0, ycocgBlack.Cg(), "Black Cg should be neutral", 0.0001f);

		RGB white = new RGB(1, 1, 1);
		var ycocgWhite = YCoCg(white);
		RGB whiteBack = RGB(ycocgWhite);
		assertRecordClose(white, whiteBack, "YCoCg white round trip", 0.0001f);
		assertClose(1, ycocgWhite.Y(), "White Y", 0.0001f);
		assertClose(0, ycocgWhite.Co(), "White Co should be neutral", 0.0001f);
		assertClose(0, ycocgWhite.Cg(), "White Cg should be neutral", 0.0001f);

		// Test grays - should have neutral chroma
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		var ycocgGray = YCoCg(gray);
		RGB grayBack = RGB(ycocgGray);
		assertRecordClose(gray, grayBack, "YCoCg gray round trip", 0.0001f);
		assertClose(0.5f, ycocgGray.Y(), "Gray Y", 0.0001f);
		assertClose(0, ycocgGray.Co(), "Gray Co should be neutral", 0.0001f);
		assertClose(0, ycocgGray.Cg(), "Gray Cg should be neutral", 0.0001f);

		// Test secondary colors
		RGB yellow = new RGB(1, 1, 0);
		var ycocgYellow = YCoCg(yellow);
		RGB yellowBack = RGB(ycocgYellow);
		assertRecordClose(yellow, yellowBack, "YCoCg yellow round trip", 0.0001f);

		RGB cyan = new RGB(0, 1, 1);
		var ycocgCyan = YCoCg(cyan);
		RGB cyanBack = RGB(ycocgCyan);
		assertRecordClose(cyan, cyanBack, "YCoCg cyan round trip", 0.0001f);

		RGB magenta = new RGB(1, 0, 1);
		var ycocgMagenta = YCoCg(magenta);
		RGB magentaBack = RGB(ycocgMagenta);
		assertRecordClose(magenta, magentaBack, "YCoCg magenta round trip", 0.0001f);

		// YCoCg specific property: Y = (R + 2G + B) / 4
		// Co = (R - B + 1) / 2, Cg = (-R + 2G - B + 1) / 2
		RGB testColor = new RGB(0.6f, 0.4f, 0.2f);
		var ycocgTest = YCoCg(testColor);
		float expectedY = (0.6f + 2 * 0.4f + 0.2f) / 4;
		assertClose(expectedY, ycocgTest.Y(), "YCoCg Y calculation", 0.0001f);

		// Test systematic round-trip accuracy - YCoCg should be very accurate
		float[] testValues = {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
		for (float r : testValues) {
			for (float g : testValues) {
				for (float b : testValues) {
					RGB rgb = new RGB(r, g, b);
					var ycocg = YCoCg(rgb);
					RGB rgbBack = RGB(ycocg);
					assertRecordClose(rgb, rgbBack, "YCoCg round trip " + rgb, 0.0001f);

					// Verify components are in valid range
					assertTrue(ycocg.Y() >= 0 && ycocg.Y() <= 1, "Y in range for " + rgb);
					assertTrue(ycocg.Co() >= -0.5f && ycocg.Co() <= 0.5f, "Co in range for " + rgb);
					assertTrue(ycocg.Cg() >= -0.5f && ycocg.Cg() <= 0.5f, "Cg in range for " + rgb);
				}
			}
		}

		// Test that different colors produce different chromaticity
		RGB color1 = new RGB(0.8f, 0.2f, 0.4f);
		RGB color2 = new RGB(0.2f, 0.8f, 0.4f);
		var ycocg1 = YCoCg(color1);
		var ycocg2 = YCoCg(color2);
		assertTrue(Math.abs(ycocg1.Co() - ycocg2.Co()) > 0.01f, "Different colors should have different Co");
		assertTrue(Math.abs(ycocg1.Cg() - ycocg2.Cg()) > 0.01f, "Different colors should have different Cg");
	}

	@Test
	public void testYES () {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		var yesRed = YES(red);
		RGB redBack = RGB(yesRed);
		assertRecordClose(red, redBack, "YES red round trip", 0.001f);

		RGB green = new RGB(0, 1, 0);
		var yesGreen = YES(green);
		RGB greenBack = RGB(yesGreen);
		assertRecordClose(green, greenBack, "YES green round trip", 0.001f);

		RGB blue = new RGB(0, 0, 1);
		var yesBlue = YES(blue);
		RGB blueBack = RGB(yesBlue);
		assertRecordClose(blue, blueBack, "YES blue round trip", 0.001f);

		// Test black and white
		RGB black = new RGB(0, 0, 0);
		var yesBlack = YES(black);
		RGB blackBack = RGB(yesBlack);
		assertRecordClose(black, blackBack, "YES black round trip", 0.001f);
		assertClose(0, yesBlack.Y(), "Black Y", 0.001f);
		assertClose(0, yesBlack.E(), "Black E should be neutral", 0.001f);
		assertClose(0, yesBlack.S(), "Black S should be neutral", 0.001f);

		RGB white = new RGB(1, 1, 1);
		var yesWhite = YES(white);
		RGB whiteBack = RGB(yesWhite);
		assertRecordClose(white, whiteBack, "YES white round trip", 0.001f);
		// Y for white should be sum of coefficients
		assertClose(0.253f + 0.684f + 0.063f, yesWhite.Y(), "White Y", 0.001f);
		assertClose(0, yesWhite.E(), "White E should be neutral", 0.001f);
		assertClose(0, yesWhite.S(), "White S should be neutral", 0.001f);

		// Test grays - should have neutral chroma
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		var yesGray = YES(gray);
		RGB grayBack = RGB(yesGray);
		assertRecordClose(gray, grayBack, "YES gray round trip", 0.001f);
		assertClose(0.5f, yesGray.Y(), "Gray Y", 0.001f);
		assertClose(0, yesGray.E(), "Gray E should be neutral", 0.001f);
		assertClose(0, yesGray.S(), "Gray S should be neutral", 0.001f);

		// Test secondary colors
		RGB yellow = new RGB(1, 1, 0);
		var yesYellow = YES(yellow);
		RGB yellowBack = RGB(yesYellow);
		assertRecordClose(yellow, yellowBack, "YES yellow round trip", 0.001f);

		RGB cyan = new RGB(0, 1, 1);
		var yesCyan = YES(cyan);
		RGB cyanBack = RGB(yesCyan);
		assertRecordClose(cyan, cyanBack, "YES cyan round trip", 0.001f);

		RGB magenta = new RGB(1, 0, 1);
		var yesMagenta = YES(magenta);
		RGB magentaBack = RGB(yesMagenta);
		assertRecordClose(magenta, magentaBack, "YES magenta round trip", 0.001f);

		// Test systematic round-trip accuracy
		float[] testValues = {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
		for (float r : testValues) {
			for (float g : testValues) {
				for (float b : testValues) {
					RGB rgb = new RGB(r, g, b);
					var yes = YES(rgb);
					RGB rgbBack = RGB(yes);
					assertRecordClose(rgb, rgbBack, "YES round trip " + rgb, 0.001f);

					// Verify components are in valid range
					assertTrue(yes.Y() >= 0 && yes.Y() <= 1, "Y in range for " + rgb);
					assertTrue(yes.E() >= -0.5f && yes.E() <= 0.5f, "E in range for " + rgb);
					assertTrue(yes.S() >= -0.5f && yes.S() <= 0.5f, "S in range for " + rgb);
				}
			}
		}

		// Test edge cases
		RGB darkColor = new RGB(0.01f, 0.02f, 0.03f);
		var yesDark = YES(darkColor);
		RGB darkBack = RGB(yesDark);
		assertRecordClose(darkColor, darkBack, "YES dark color round trip", 0.001f);

		RGB brightColor = new RGB(0.98f, 0.97f, 0.99f);
		var yesBright = YES(brightColor);
		RGB brightBack = RGB(yesBright);
		assertRecordClose(brightColor, brightBack, "YES bright color round trip", 0.001f);

		// Test that different colors produce different E/S values
		RGB color1 = new RGB(0.7f, 0.3f, 0.1f);
		RGB color2 = new RGB(0.1f, 0.7f, 0.3f);
		var yes1 = YES(color1);
		var yes2 = YES(color2);
		assertTrue(Math.abs(yes1.E() - yes2.E()) > 0.01f, "Different colors should have different E");
		assertTrue(Math.abs(yes1.S() - yes2.S()) > 0.01f, "Different colors should have different S");

		// Verify Y calculation for specific color
		RGB testColor = new RGB(0.5f, 0.3f, 0.7f);
		var yesTest = YES(testColor);
		float expectedY = 0.253f * 0.5f + 0.684f * 0.3f + 0.063f * 0.7f;
		assertClose(expectedY, yesTest.Y(), "YES Y calculation", 0.001f);
	}

	@Test
	public void testYIQ () {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		YIQ yiqRed = YIQ(red);
		RGB redBack = RGB(yiqRed);
		assertRecordClose(red, redBack, "YIQ red round trip", 0.001f);
		// Verify Y component for red
		assertClose(0.299f, yiqRed.Y(), "Red Y component", 0.001f);

		RGB green = new RGB(0, 1, 0);
		YIQ yiqGreen = YIQ(green);
		RGB greenBack = RGB(yiqGreen);
		assertRecordClose(green, greenBack, "YIQ green round trip", 0.001f);
		// Verify Y component for green
		assertClose(0.587f, yiqGreen.Y(), "Green Y component", 0.001f);

		RGB blue = new RGB(0, 0, 1);
		YIQ yiqBlue = YIQ(blue);
		RGB blueBack = RGB(yiqBlue);
		assertRecordClose(blue, blueBack, "YIQ blue round trip", 0.001f);
		// Verify Y component for blue
		assertClose(0.114f, yiqBlue.Y(), "Blue Y component", 0.001f);

		// Test black and white
		RGB black = new RGB(0, 0, 0);
		YIQ yiqBlack = YIQ(black);
		RGB blackBack = RGB(yiqBlack);
		assertRecordClose(black, blackBack, "YIQ black round trip", 0.001f);
		assertClose(0, yiqBlack.Y(), "Black Y", 0.001f);
		assertClose(0, yiqBlack.I(), "Black I", 0.001f);
		assertClose(0, yiqBlack.Q(), "Black Q", 0.001f);

		RGB white = new RGB(1, 1, 1);
		YIQ yiqWhite = YIQ(white);
		RGB whiteBack = RGB(yiqWhite);
		assertRecordClose(white, whiteBack, "YIQ white round trip", 0.001f);
		assertClose(1, yiqWhite.Y(), "White Y", 0.001f);
		assertClose(0, yiqWhite.I(), "White I", 0.001f);
		assertClose(0, yiqWhite.Q(), "White Q", 0.001f);

		// Test grays - should have zero I and Q
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		YIQ yiqGray = YIQ(gray);
		RGB grayBack = RGB(yiqGray);
		assertRecordClose(gray, grayBack, "YIQ gray round trip", 0.001f);
		assertClose(0.5f, yiqGray.Y(), "Gray Y", 0.001f);
		assertClose(0, yiqGray.I(), "Gray I should be zero", 0.001f);
		assertClose(0, yiqGray.Q(), "Gray Q should be zero", 0.001f);

		// Test secondary colors
		RGB yellow = new RGB(1, 1, 0);
		YIQ yiqYellow = YIQ(yellow);
		RGB yellowBack = RGB(yiqYellow);
		assertRecordClose(yellow, yellowBack, "YIQ yellow round trip", 0.001f);

		RGB cyan = new RGB(0, 1, 1);
		YIQ yiqCyan = YIQ(cyan);
		RGB cyanBack = RGB(yiqCyan);
		assertRecordClose(cyan, cyanBack, "YIQ cyan round trip", 0.001f);

		RGB magenta = new RGB(1, 0, 1);
		YIQ yiqMagenta = YIQ(magenta);
		RGB magentaBack = RGB(yiqMagenta);
		assertRecordClose(magenta, magentaBack, "YIQ magenta round trip", 0.001f);

		// Test systematic round-trip accuracy
		float[] testValues = {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
		for (float r : testValues) {
			for (float g : testValues) {
				for (float b : testValues) {
					RGB rgb = new RGB(r, g, b);
					YIQ yiq = YIQ(rgb);
					RGB rgbBack = RGB(yiq);
					assertRecordClose(rgb, rgbBack, "YIQ round trip " + rgb, 0.001f);

					// Verify Y is in valid range [0..1]
					assertTrue(yiq.Y() >= 0 && yiq.Y() <= 1, "Y in range for " + rgb);
					// I and Q can be negative
					assertTrue(yiq.I() >= -0.6f && yiq.I() <= 0.6f, "I in expected range for " + rgb);
					assertTrue(yiq.Q() >= -0.53f && yiq.Q() <= 0.53f, "Q in expected range for " + rgb);
				}
			}
		}

		// Test edge cases
		RGB darkColor = new RGB(0.01f, 0.02f, 0.03f);
		YIQ yiqDark = YIQ(darkColor);
		RGB darkBack = RGB(yiqDark);
		assertRecordClose(darkColor, darkBack, "YIQ dark color round trip", 0.001f);

		RGB brightColor = new RGB(0.98f, 0.97f, 0.99f);
		YIQ yiqBright = YIQ(brightColor);
		RGB brightBack = RGB(yiqBright);
		assertRecordClose(brightColor, brightBack, "YIQ bright color round trip", 0.001f);

		// Verify Y calculation for specific color
		RGB testColor = new RGB(0.5f, 0.3f, 0.7f);
		YIQ yiqTest = YIQ(testColor);
		float expectedY = 0.299f * 0.5f + 0.587f * 0.3f + 0.114f * 0.7f;
		assertClose(expectedY, yiqTest.Y(), "YIQ Y calculation", 0.001f);

		// Test known color conversions from NTSC standards
		// Orange should have positive I (reddish)
		RGB orange = new RGB(1.0f, 0.5f, 0.0f);
		YIQ yiqOrange = YIQ(orange);
		assertTrue(yiqOrange.I() > 0, "Orange should have positive I (reddish)");

		// Blue-green should have negative I
		RGB blueGreen = new RGB(0.0f, 0.7f, 0.7f);
		YIQ yiqBlueGreen = YIQ(blueGreen);
		assertTrue(yiqBlueGreen.I() < 0, "Blue-green should have negative I");

		// Test that different colors produce different I/Q values
		RGB color1 = new RGB(0.8f, 0.2f, 0.3f);
		RGB color2 = new RGB(0.2f, 0.8f, 0.3f);
		YIQ yiq1 = YIQ(color1);
		YIQ yiq2 = YIQ(color2);
		assertTrue(Math.abs(yiq1.I() - yiq2.I()) > 0.01f, "Different colors should have different I");
		assertTrue(Math.abs(yiq1.Q() - yiq2.Q()) > 0.01f, "Different colors should have different Q");
	}

	@Test
	public void testYUV () {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		YUV yuvRed = YUV(red);
		RGB redBack = RGB(yuvRed);
		assertRecordClose(red, redBack, "YUV red round trip", 0.001f);
		// Verify Y component for red
		assertClose(0.299f, yuvRed.Y(), "Red Y component", 0.001f);

		RGB green = new RGB(0, 1, 0);
		YUV yuvGreen = YUV(green);
		RGB greenBack = RGB(yuvGreen);
		assertRecordClose(green, greenBack, "YUV green round trip", 0.001f);
		// Verify Y component for green
		assertClose(0.587f, yuvGreen.Y(), "Green Y component", 0.001f);

		RGB blue = new RGB(0, 0, 1);
		YUV yuvBlue = YUV(blue);
		RGB blueBack = RGB(yuvBlue);
		assertRecordClose(blue, blueBack, "YUV blue round trip", 0.001f);
		// Verify Y component for blue
		assertClose(0.114f, yuvBlue.Y(), "Blue Y component", 0.001f);
		// Blue should have maximum positive U
		assertTrue(yuvBlue.U() > 0.4f, "Blue should have large positive U");

		// Test black and white
		RGB black = new RGB(0, 0, 0);
		YUV yuvBlack = YUV(black);
		RGB blackBack = RGB(yuvBlack);
		assertRecordClose(black, blackBack, "YUV black round trip", 0.001f);
		assertClose(0, yuvBlack.Y(), "Black Y", 0.001f);
		assertClose(0, yuvBlack.U(), "Black U", 0.001f);
		assertClose(0, yuvBlack.V(), "Black V", 0.001f);

		RGB white = new RGB(1, 1, 1);
		YUV yuvWhite = YUV(white);
		RGB whiteBack = RGB(yuvWhite);
		assertRecordClose(white, whiteBack, "YUV white round trip", 0.001f);
		assertClose(1, yuvWhite.Y(), "White Y", 0.001f);
		assertClose(0, yuvWhite.U(), "White U", 0.001f);
		assertClose(0, yuvWhite.V(), "White V", 0.001f);

		// Test grays - should have zero U and V
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		YUV yuvGray = YUV(gray);
		RGB grayBack = RGB(yuvGray);
		assertRecordClose(gray, grayBack, "YUV gray round trip", 0.001f);
		assertClose(0.5f, yuvGray.Y(), "Gray Y", 0.001f);
		assertClose(0, yuvGray.U(), "Gray U should be zero", 0.001f);
		assertClose(0, yuvGray.V(), "Gray V should be zero", 0.001f);

		// Test secondary colors
		RGB yellow = new RGB(1, 1, 0);
		YUV yuvYellow = YUV(yellow);
		RGB yellowBack = RGB(yuvYellow);
		assertRecordClose(yellow, yellowBack, "YUV yellow round trip", 0.001f);
		// Yellow should have negative U
		assertTrue(yuvYellow.U() < -0.2f, "Yellow should have negative U");

		RGB cyan = new RGB(0, 1, 1);
		YUV yuvCyan = YUV(cyan);
		RGB cyanBack = RGB(yuvCyan);
		assertRecordClose(cyan, cyanBack, "YUV cyan round trip", 0.001f);

		RGB magenta = new RGB(1, 0, 1);
		YUV yuvMagenta = YUV(magenta);
		RGB magentaBack = RGB(yuvMagenta);
		assertRecordClose(magenta, magentaBack, "YUV magenta round trip", 0.001f);

		// Test systematic round-trip accuracy
		float[] testValues = {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
		for (float r : testValues) {
			for (float g : testValues) {
				for (float b : testValues) {
					RGB rgb = new RGB(r, g, b);
					YUV yuv = YUV(rgb);
					RGB rgbBack = RGB(yuv);
					assertRecordClose(rgb, rgbBack, "YUV round trip " + rgb, 0.001f);

					// Verify Y is in valid range [0..1]
					assertTrue(yuv.Y() >= 0 && yuv.Y() <= 1, "Y in range for " + rgb);
					// U and V can be negative
					assertTrue(yuv.U() >= -0.43601f && yuv.U() <= 0.43601f, "U in expected range for " + rgb);
					assertTrue(yuv.V() >= -0.61498f && yuv.V() <= 0.61498f, "V in expected range for " + rgb);
				}
			}
		}

		// Test edge cases
		RGB darkColor = new RGB(0.01f, 0.02f, 0.03f);
		YUV yuvDark = YUV(darkColor);
		RGB darkBack = RGB(yuvDark);
		assertRecordClose(darkColor, darkBack, "YUV dark color round trip", 0.001f);

		RGB brightColor = new RGB(0.98f, 0.97f, 0.99f);
		YUV yuvBright = YUV(brightColor);
		RGB brightBack = RGB(yuvBright);
		assertRecordClose(brightColor, brightBack, "YUV bright color round trip", 0.001f);

		// Verify Y calculation for specific color
		RGB testColor = new RGB(0.5f, 0.3f, 0.7f);
		YUV yuvTest = YUV(testColor);
		float expectedY = 0.299f * 0.5f + 0.587f * 0.3f + 0.114f * 0.7f;
		assertClose(expectedY, yuvTest.Y(), "YUV Y calculation", 0.001f);

		// Test YUV specific properties
		// U = 0.492 * (B - Y), V = 0.877 * (R - Y)
		float Y = yuvTest.Y();
		float expectedU = 0.492f * (0.7f - Y);
		float expectedV = 0.877f * (0.5f - Y);
		assertClose(expectedU, yuvTest.U(), "YUV U calculation", 0.001f);
		assertClose(expectedV, yuvTest.V(), "YUV V calculation", 0.001f);

		// Test that different colors produce different U/V values
		RGB color1 = new RGB(0.8f, 0.2f, 0.3f);
		RGB color2 = new RGB(0.2f, 0.8f, 0.3f);
		YUV yuv1 = YUV(color1);
		YUV yuv2 = YUV(color2);
		assertTrue(Math.abs(yuv1.U() - yuv2.U()) > 0.01f, "Different colors should have different U");
		assertTrue(Math.abs(yuv1.V() - yuv2.V()) > 0.01f, "Different colors should have different V");

		// Test colors at maximum chromaticity
		// Pure red should have maximum positive V
		assertTrue(yuvRed.V() > 0.4f, "Red should have large positive V");
		// Pure green should have negative U and V
		assertTrue(yuvGreen.U() < -0.2f, "Green should have negative U");
		assertTrue(yuvGreen.V() < -0.2f, "Green should have negative V");

		// Test round trip
		roundTripf(new RGB(0.5f, 0.3f, 0.7f), Colors::YUV, Colors::RGB, "YUV");
	}
}
