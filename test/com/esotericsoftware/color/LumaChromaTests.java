
package com.esotericsoftware.color;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.RGB;
import com.esotericsoftware.color.space.YCC;
import com.esotericsoftware.color.space.YCbCr;
import com.esotericsoftware.color.space.YCbCr.YCbCrColorSpace;
import com.esotericsoftware.color.space.YIQ;
import com.esotericsoftware.color.space.YUV;

public class LumaChromaTests extends Tests {
	@Test
	public void testYCbCr () {
		// Test ITU_BT_601
		RGB rgb601 = new RGB(0.5f, 0.3f, 0.7f);
		YCbCr ycbcr601 = rgb601.YCbCr(YCbCrColorSpace.ITU_BT_601);
		RGB back601 = ycbcr601.RGB(YCbCrColorSpace.ITU_BT_601);
		assertClose(rgb601, back601, 0.005, "YCbCr ITU_BT_601 round trip");

		// Test ITU_BT_709_HDTV
		RGB rgb709 = new RGB(0.5f, 0.3f, 0.7f);
		YCbCr ycbcr709 = rgb709.YCbCr(YCbCrColorSpace.ITU_BT_709_HDTV);
		RGB back709 = ycbcr709.RGB(YCbCrColorSpace.ITU_BT_709_HDTV);
		assertClose(rgb709, back709, 0.005, "YCbCr ITU_BT_709_HDTV round trip");
	}

	@Test
	public void testYCC () {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		YCC yccRed = red.YCC();
		RGB redBack = yccRed.RGB();
		assertClose(red, redBack, 0.001f, "YCC red round trip");
		// Verify Y component for red
		assertEquals(0.213f, yccRed.Y(), 0.001f, "Red Y component");

		RGB green = new RGB(0, 1, 0);
		YCC yccGreen = green.YCC();
		RGB greenBack = yccGreen.RGB();
		assertClose(green, greenBack, 0.001f, "YCC green round trip");
		// Verify Y component for green
		assertEquals(0.419f, yccGreen.Y(), 0.001f, "Green Y component");

		RGB blue = new RGB(0, 0, 1);
		YCC yccBlue = blue.YCC();
		RGB blueBack = yccBlue.RGB();
		assertClose(blue, blueBack, 0.001f, "YCC blue round trip");
		// Verify Y component for blue
		assertEquals(0.081f, yccBlue.Y(), 0.001f, "Blue Y component");

		// Test black and white
		RGB black = new RGB(0, 0, 0);
		YCC yccBlack = black.YCC();
		RGB blackBack = yccBlack.RGB();
		assertClose(black, blackBack, 0.001f, "YCC black round trip");
		assertEquals(0, yccBlack.Y(), 0.001f, "Black Y component");
		assertEquals(0.612f, yccBlack.C1(), 0.001f, "Black C1 component");
		assertEquals(0.537f, yccBlack.C2(), 0.001f, "Black C2 component");

		RGB white = new RGB(1, 1, 1);
		YCC yccWhite = white.YCC();
		RGB whiteBack = yccWhite.RGB();
		assertClose(white, whiteBack, 0.001f, "YCC white round trip");
		assertEquals(0.213f + 0.419f + 0.081f, yccWhite.Y(), 0.001f, "White Y component");

		// Test grays - should have neutral chroma
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		YCC yccGray = gray.YCC();
		RGB grayBack = yccGray.RGB();
		assertClose(gray, grayBack, 0.001f, "YCC gray round trip");
		assertEquals(0.612f, yccGray.C1(), 0.001f, "Gray C1 should be neutral");
		assertEquals(0.537f, yccGray.C2(), 0.001f, "Gray C2 should be neutral");

		// Test secondary colors
		RGB yellow = new RGB(1, 1, 0);
		YCC yccYellow = yellow.YCC();
		RGB yellowBack = yccYellow.RGB();
		assertClose(yellow, yellowBack, 0.001f, "YCC yellow round trip");

		RGB cyan = new RGB(0, 1, 1);
		YCC yccCyan = cyan.YCC();
		RGB cyanBack = yccCyan.RGB();
		assertClose(cyan, cyanBack, 0.001f, "YCC cyan round trip");

		RGB magenta = new RGB(1, 0, 1);
		YCC yccMagenta = magenta.YCC();
		RGB magentaBack = yccMagenta.RGB();
		assertClose(magenta, magentaBack, 0.001f, "YCC magenta round trip");

		// Test systematic round-trip accuracy
		float[] testValues = {0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
		for (float r : testValues) {
			for (float g : testValues) {
				for (float b : testValues) {
					RGB rgb = new RGB(r, g, b);
					YCC ycc = rgb.YCC();
					RGB rgbBack = ycc.RGB();
					assertClose(rgb, rgbBack, 0.001f, "YCC round trip " + rgb);

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
		YCC yccDark = darkColor.YCC();
		RGB darkBack = yccDark.RGB();
		assertClose(darkColor, darkBack, 0.001f, "YCC dark color round trip");

		RGB brightColor = new RGB(0.98f, 0.97f, 0.99f);
		YCC yccBright = brightColor.YCC();
		RGB brightBack = yccBright.RGB();
		assertClose(brightColor, brightBack, 0.001f, "YCC bright color round trip");

		// Test specific color that was in original test
		RGB testColor = new RGB(0.5f, 0.3f, 0.7f);
		YCC yccTest = testColor.YCC();
		RGB testBack = yccTest.RGB();
		assertClose(testColor, testBack, 0.001f, "YCC test color round trip");
		// Verify the Y calculation: 0.213*0.5 + 0.419*0.3 + 0.081*0.7 = 0.2887
		assertEquals(0.2887f, yccTest.Y(), 0.001f, "YCC Y component for test color");

		// Test that chroma components properly encode color differences
		// For gray, C1 and C2 should be at their offset values
		RGB gray1 = new RGB(0.3f, 0.3f, 0.3f);
		YCC yccGray1 = gray1.YCC();
		assertEquals(0.612f, yccGray1.C1(), 0.001f, "Gray should have neutral C1");
		assertEquals(0.537f, yccGray1.C2(), 0.001f, "Gray should have neutral C2");

		// Colors with same luminance but different chromaticity should have different C1/C2
		RGB color1 = new RGB(0.5f, 0.3f, 0.2f);
		RGB color2 = new RGB(0.2f, 0.5f, 0.3f);
		YCC ycc1 = color1.YCC();
		YCC ycc2 = color2.YCC();
		assertTrue(Math.abs(ycc1.C1() - ycc2.C1()) > 0.01f, "Different colors should have different C1");
		assertTrue(Math.abs(ycc1.C2() - ycc2.C2()) > 0.01f, "Different colors should have different C2");
	}

	@Test
	public void testYCoCg () {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		var ycocgRed = red.YCoCg();
		RGB redBack = ycocgRed.RGB();
		assertClose(red, redBack, 0.0001f, "YCoCg red round trip");

		RGB green = new RGB(0, 1, 0);
		var ycocgGreen = green.YCoCg();
		RGB greenBack = ycocgGreen.RGB();
		assertClose(green, greenBack, 0.0001f, "YCoCg green round trip");

		RGB blue = new RGB(0, 0, 1);
		var ycocgBlue = blue.YCoCg();
		RGB blueBack = ycocgBlue.RGB();
		assertClose(blue, blueBack, 0.0001f, "YCoCg blue round trip");

		// Test black and white
		RGB black = new RGB(0, 0, 0);
		var ycocgBlack = black.YCoCg();
		RGB blackBack = ycocgBlack.RGB();
		assertClose(black, blackBack, 0.0001f, "YCoCg black round trip");
		assertEquals(0, ycocgBlack.Y(), 0.0001f, "Black Y");
		assertEquals(0, ycocgBlack.Co(), 0.0001f, "Black Co should be neutral");
		assertEquals(0, ycocgBlack.Cg(), 0.0001f, "Black Cg should be neutral");

		RGB white = new RGB(1, 1, 1);
		var ycocgWhite = white.YCoCg();
		RGB whiteBack = ycocgWhite.RGB();
		assertClose(white, whiteBack, 0.0001f, "YCoCg white round trip");
		assertEquals(1, ycocgWhite.Y(), 0.0001f, "White Y");
		assertEquals(0, ycocgWhite.Co(), 0.0001f, "White Co should be neutral");
		assertEquals(0, ycocgWhite.Cg(), 0.0001f, "White Cg should be neutral");

		// Test grays - should have neutral chroma
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		var ycocgGray = gray.YCoCg();
		RGB grayBack = ycocgGray.RGB();
		assertClose(gray, grayBack, 0.0001f, "YCoCg gray round trip");
		assertEquals(0.5f, ycocgGray.Y(), 0.0001f, "Gray Y");
		assertEquals(0, ycocgGray.Co(), 0.0001f, "Gray Co should be neutral");
		assertEquals(0, ycocgGray.Cg(), 0.0001f, "Gray Cg should be neutral");

		// Test secondary colors
		RGB yellow = new RGB(1, 1, 0);
		var ycocgYellow = yellow.YCoCg();
		RGB yellowBack = ycocgYellow.RGB();
		assertClose(yellow, yellowBack, 0.0001f, "YCoCg yellow round trip");

		RGB cyan = new RGB(0, 1, 1);
		var ycocgCyan = cyan.YCoCg();
		RGB cyanBack = ycocgCyan.RGB();
		assertClose(cyan, cyanBack, 0.0001f, "YCoCg cyan round trip");

		RGB magenta = new RGB(1, 0, 1);
		var ycocgMagenta = magenta.YCoCg();
		RGB magentaBack = ycocgMagenta.RGB();
		assertClose(magenta, magentaBack, 0.0001f, "YCoCg magenta round trip");

		// YCoCg specific property: Y = (R + 2G + B) / 4
		// Co = (R - B + 1) / 2, Cg = (-R + 2G - B + 1) / 2
		RGB testColor = new RGB(0.6f, 0.4f, 0.2f);
		var ycocgTest = testColor.YCoCg();
		float expectedY = (0.6f + 2 * 0.4f + 0.2f) / 4;
		assertEquals(expectedY, ycocgTest.Y(), 0.0001f, "YCoCg Y calculation");

		// Test systematic round-trip accuracy - YCoCg should be very accurate
		float[] testValues = {0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
		for (float r : testValues) {
			for (float g : testValues) {
				for (float b : testValues) {
					RGB rgb = new RGB(r, g, b);
					var ycocg = rgb.YCoCg();
					RGB rgbBack = ycocg.RGB();
					assertClose(rgb, rgbBack, 0.0001f, "YCoCg round trip " + rgb);

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
		var ycocg1 = color1.YCoCg();
		var ycocg2 = color2.YCoCg();
		assertTrue(Math.abs(ycocg1.Co() - ycocg2.Co()) > 0.01f, "Different colors should have different Co");
		assertTrue(Math.abs(ycocg1.Cg() - ycocg2.Cg()) > 0.01f, "Different colors should have different Cg");
	}

	@Test
	public void testYES () {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		var yesRed = red.YES();
		RGB redBack = yesRed.RGB();
		assertClose(red, redBack, 0.001f, "YES red round trip");

		RGB green = new RGB(0, 1, 0);
		var yesGreen = green.YES();
		RGB greenBack = yesGreen.RGB();
		assertClose(green, greenBack, 0.001f, "YES green round trip");

		RGB blue = new RGB(0, 0, 1);
		var yesBlue = blue.YES();
		RGB blueBack = yesBlue.RGB();
		assertClose(blue, blueBack, 0.001f, "YES blue round trip");

		// Test black and white
		RGB black = new RGB(0, 0, 0);
		var yesBlack = black.YES();
		RGB blackBack = yesBlack.RGB();
		assertClose(black, blackBack, 0.001f, "YES black round trip");
		assertEquals(0, yesBlack.Y(), 0.001f, "Black Y");
		assertEquals(0, yesBlack.E(), 0.001f, "Black E should be neutral");
		assertEquals(0, yesBlack.S(), 0.001f, "Black S should be neutral");

		RGB white = new RGB(1, 1, 1);
		var yesWhite = white.YES();
		RGB whiteBack = yesWhite.RGB();
		assertClose(white, whiteBack, 0.001f, "YES white round trip");
		// Y for white should be sum of coefficients
		assertEquals(0.253f + 0.684f + 0.063f, yesWhite.Y(), 0.001f, "White Y");
		assertEquals(0, yesWhite.E(), 0.001f, "White E should be neutral");
		assertEquals(0, yesWhite.S(), 0.001f, "White S should be neutral");

		// Test grays - should have neutral chroma
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		var yesGray = gray.YES();
		RGB grayBack = yesGray.RGB();
		assertClose(gray, grayBack, 0.001f, "YES gray round trip");
		assertEquals(0.5f, yesGray.Y(), 0.001f, "Gray Y");
		assertEquals(0, yesGray.E(), 0.001f, "Gray E should be neutral");
		assertEquals(0, yesGray.S(), 0.001f, "Gray S should be neutral");

		// Test secondary colors
		RGB yellow = new RGB(1, 1, 0);
		var yesYellow = yellow.YES();
		RGB yellowBack = yesYellow.RGB();
		assertClose(yellow, yellowBack, 0.001f, "YES yellow round trip");

		RGB cyan = new RGB(0, 1, 1);
		var yesCyan = cyan.YES();
		RGB cyanBack = yesCyan.RGB();
		assertClose(cyan, cyanBack, 0.001f, "YES cyan round trip");

		RGB magenta = new RGB(1, 0, 1);
		var yesMagenta = magenta.YES();
		RGB magentaBack = yesMagenta.RGB();
		assertClose(magenta, magentaBack, 0.001f, "YES magenta round trip");

		// Test systematic round-trip accuracy
		float[] testValues = {0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
		for (float r : testValues) {
			for (float g : testValues) {
				for (float b : testValues) {
					RGB rgb = new RGB(r, g, b);
					var yes = rgb.YES();
					RGB rgbBack = yes.RGB();
					assertClose(rgb, rgbBack, 0.001f, "YES round trip " + rgb);

					// Verify components are in valid range
					assertTrue(yes.Y() >= 0 && yes.Y() <= 1, "Y in range for " + rgb);
					assertTrue(yes.E() >= -0.5f && yes.E() <= 0.5f, "E in range for " + rgb);
					assertTrue(yes.S() >= -0.5f && yes.S() <= 0.5f, "S in range for " + rgb);
				}
			}
		}

		// Test edge cases
		RGB darkColor = new RGB(0.01f, 0.02f, 0.03f);
		var yesDark = darkColor.YES();
		RGB darkBack = yesDark.RGB();
		assertClose(darkColor, darkBack, 0.001f, "YES dark color round trip");

		RGB brightColor = new RGB(0.98f, 0.97f, 0.99f);
		var yesBright = brightColor.YES();
		RGB brightBack = yesBright.RGB();
		assertClose(brightColor, brightBack, 0.001f, "YES bright color round trip");

		// Test that different colors produce different E/S values
		RGB color1 = new RGB(0.7f, 0.3f, 0.1f);
		RGB color2 = new RGB(0.1f, 0.7f, 0.3f);
		var yes1 = color1.YES();
		var yes2 = color2.YES();
		assertTrue(Math.abs(yes1.E() - yes2.E()) > 0.01f, "Different colors should have different E");
		assertTrue(Math.abs(yes1.S() - yes2.S()) > 0.01f, "Different colors should have different S");

		// Verify Y calculation for specific color
		RGB testColor = new RGB(0.5f, 0.3f, 0.7f);
		var yesTest = testColor.YES();
		float expectedY = 0.253f * 0.5f + 0.684f * 0.3f + 0.063f * 0.7f;
		assertEquals(expectedY, yesTest.Y(), 0.001f, "YES Y calculation");
	}

	@Test
	public void testYIQ () {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		YIQ yiqRed = red.YIQ();
		RGB redBack = yiqRed.RGB();
		assertClose(red, redBack, 0.001f, "YIQ red round trip");
		// Verify Y component for red
		assertEquals(0.299f, yiqRed.Y(), 0.001f, "Red Y component");

		RGB green = new RGB(0, 1, 0);
		YIQ yiqGreen = green.YIQ();
		RGB greenBack = yiqGreen.RGB();
		assertClose(green, greenBack, 0.001f, "YIQ green round trip");
		// Verify Y component for green
		assertEquals(0.587f, yiqGreen.Y(), 0.001f, "Green Y component");

		RGB blue = new RGB(0, 0, 1);
		YIQ yiqBlue = blue.YIQ();
		RGB blueBack = yiqBlue.RGB();
		assertClose(blue, blueBack, 0.001f, "YIQ blue round trip");
		// Verify Y component for blue
		assertEquals(0.114f, yiqBlue.Y(), 0.001f, "Blue Y component");

		// Test black and white
		RGB black = new RGB(0, 0, 0);
		YIQ yiqBlack = black.YIQ();
		RGB blackBack = yiqBlack.RGB();
		assertClose(black, blackBack, 0.001f, "YIQ black round trip");
		assertEquals(0, yiqBlack.Y(), 0.001f, "Black Y");
		assertEquals(0, yiqBlack.I(), 0.001f, "Black I");
		assertEquals(0, yiqBlack.Q(), 0.001f, "Black Q");

		RGB white = new RGB(1, 1, 1);
		YIQ yiqWhite = white.YIQ();
		RGB whiteBack = yiqWhite.RGB();
		assertClose(white, whiteBack, 0.001f, "YIQ white round trip");
		assertEquals(1, yiqWhite.Y(), 0.001f, "White Y");
		assertEquals(0, yiqWhite.I(), 0.001f, "White I");
		assertEquals(0, yiqWhite.Q(), 0.001f, "White Q");

		// Test grays - should have zero I and Q
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		YIQ yiqGray = gray.YIQ();
		RGB grayBack = yiqGray.RGB();
		assertClose(gray, grayBack, 0.001f, "YIQ gray round trip");
		assertEquals(0.5f, yiqGray.Y(), 0.001f, "Gray Y");
		assertEquals(0, yiqGray.I(), 0.001f, "Gray I should be zero");
		assertEquals(0, yiqGray.Q(), 0.001f, "Gray Q should be zero");

		// Test secondary colors
		RGB yellow = new RGB(1, 1, 0);
		YIQ yiqYellow = yellow.YIQ();
		RGB yellowBack = yiqYellow.RGB();
		assertClose(yellow, yellowBack, 0.001f, "YIQ yellow round trip");

		RGB cyan = new RGB(0, 1, 1);
		YIQ yiqCyan = cyan.YIQ();
		RGB cyanBack = yiqCyan.RGB();
		assertClose(cyan, cyanBack, 0.001f, "YIQ cyan round trip");

		RGB magenta = new RGB(1, 0, 1);
		YIQ yiqMagenta = magenta.YIQ();
		RGB magentaBack = yiqMagenta.RGB();
		assertClose(magenta, magentaBack, 0.001f, "YIQ magenta round trip");

		// Test systematic round-trip accuracy
		float[] testValues = {0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
		for (float r : testValues) {
			for (float g : testValues) {
				for (float b : testValues) {
					RGB rgb = new RGB(r, g, b);
					YIQ yiq = rgb.YIQ();
					RGB rgbBack = yiq.RGB();
					assertClose(rgb, rgbBack, 0.001f, "YIQ round trip " + rgb);

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
		YIQ yiqDark = darkColor.YIQ();
		RGB darkBack = yiqDark.RGB();
		assertClose(darkColor, darkBack, 0.001f, "YIQ dark color round trip");

		RGB brightColor = new RGB(0.98f, 0.97f, 0.99f);
		YIQ yiqBright = brightColor.YIQ();
		RGB brightBack = yiqBright.RGB();
		assertClose(brightColor, brightBack, 0.001f, "YIQ bright color round trip");

		// Verify Y calculation for specific color
		RGB testColor = new RGB(0.5f, 0.3f, 0.7f);
		YIQ yiqTest = testColor.YIQ();
		float expectedY = 0.299f * 0.5f + 0.587f * 0.3f + 0.114f * 0.7f;
		assertEquals(expectedY, yiqTest.Y(), 0.001f, "YIQ Y calculation");

		// Test known color conversions from NTSC standards
		// Orange should have positive I (reddish)
		RGB orange = new RGB(1f, 0.5f, 0f);
		YIQ yiqOrange = orange.YIQ();
		assertTrue(yiqOrange.I() > 0, "Orange should have positive I (reddish)");

		// Blue-green should have negative I
		RGB blueGreen = new RGB(0f, 0.7f, 0.7f);
		YIQ yiqBlueGreen = blueGreen.YIQ();
		assertTrue(yiqBlueGreen.I() < 0, "Blue-green should have negative I");

		// Test that different colors produce different I/Q values
		RGB color1 = new RGB(0.8f, 0.2f, 0.3f);
		RGB color2 = new RGB(0.2f, 0.8f, 0.3f);
		YIQ yiq1 = color1.YIQ();
		YIQ yiq2 = color2.YIQ();
		assertTrue(Math.abs(yiq1.I() - yiq2.I()) > 0.01f, "Different colors should have different I");
		assertTrue(Math.abs(yiq1.Q() - yiq2.Q()) > 0.01f, "Different colors should have different Q");
	}

	@Test
	public void testYUV () {
		// Test primary colors
		RGB red = new RGB(1, 0, 0);
		YUV yuvRed = red.YUV();
		RGB redBack = yuvRed.RGB();
		assertClose(red, redBack, 0.001f, "YUV red round trip");
		// Verify Y component for red
		assertEquals(0.299f, yuvRed.Y(), 0.001f, "Red Y component");

		RGB green = new RGB(0, 1, 0);
		YUV yuvGreen = green.YUV();
		RGB greenBack = yuvGreen.RGB();
		assertClose(green, greenBack, 0.001f, "YUV green round trip");
		// Verify Y component for green
		assertEquals(0.587f, yuvGreen.Y(), 0.001f, "Green Y component");

		RGB blue = new RGB(0, 0, 1);
		YUV yuvBlue = blue.YUV();
		RGB blueBack = yuvBlue.RGB();
		assertClose(blue, blueBack, 0.001f, "YUV blue round trip");
		// Verify Y component for blue
		assertEquals(0.114f, yuvBlue.Y(), 0.001f, "Blue Y component");
		// Blue should have maximum positive U
		assertTrue(yuvBlue.U() > 0.4f, "Blue should have large positive U");

		// Test black and white
		RGB black = new RGB(0, 0, 0);
		YUV yuvBlack = black.YUV();
		RGB blackBack = yuvBlack.RGB();
		assertClose(black, blackBack, 0.001f, "YUV black round trip");
		assertEquals(0, yuvBlack.Y(), 0.001f, "Black Y");
		assertEquals(0, yuvBlack.U(), 0.001f, "Black U");
		assertEquals(0, yuvBlack.V(), 0.001f, "Black V");

		RGB white = new RGB(1, 1, 1);
		YUV yuvWhite = white.YUV();
		RGB whiteBack = yuvWhite.RGB();
		assertClose(white, whiteBack, 0.001f, "YUV white round trip");
		assertEquals(1, yuvWhite.Y(), 0.001f, "White Y");
		assertEquals(0, yuvWhite.U(), 0.001f, "White U");
		assertEquals(0, yuvWhite.V(), 0.001f, "White V");

		// Test grays - should have zero U and V
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		YUV yuvGray = gray.YUV();
		RGB grayBack = yuvGray.RGB();
		assertClose(gray, grayBack, 0.001f, "YUV gray round trip");
		assertEquals(0.5f, yuvGray.Y(), 0.001f, "Gray Y");
		assertEquals(0, yuvGray.U(), 0.001f, "Gray U should be zero");
		assertEquals(0, yuvGray.V(), 0.001f, "Gray V should be zero");

		// Test secondary colors
		RGB yellow = new RGB(1, 1, 0);
		YUV yuvYellow = yellow.YUV();
		RGB yellowBack = yuvYellow.RGB();
		assertClose(yellow, yellowBack, 0.001f, "YUV yellow round trip");
		// Yellow should have negative U
		assertTrue(yuvYellow.U() < -0.2f, "Yellow should have negative U");

		RGB cyan = new RGB(0, 1, 1);
		YUV yuvCyan = cyan.YUV();
		RGB cyanBack = yuvCyan.RGB();
		assertClose(cyan, cyanBack, 0.001f, "YUV cyan round trip");

		RGB magenta = new RGB(1, 0, 1);
		YUV yuvMagenta = magenta.YUV();
		RGB magentaBack = yuvMagenta.RGB();
		assertClose(magenta, magentaBack, 0.001f, "YUV magenta round trip");

		// Test systematic round-trip accuracy
		float[] testValues = {0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
		for (float r : testValues) {
			for (float g : testValues) {
				for (float b : testValues) {
					RGB rgb = new RGB(r, g, b);
					YUV yuv = rgb.YUV();
					RGB rgbBack = yuv.RGB();
					assertClose(rgb, rgbBack, 0.001f, "YUV round trip " + rgb);

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
		YUV yuvDark = darkColor.YUV();
		RGB darkBack = yuvDark.RGB();
		assertClose(darkColor, darkBack, 0.001f, "YUV dark color round trip");

		RGB brightColor = new RGB(0.98f, 0.97f, 0.99f);
		YUV yuvBright = brightColor.YUV();
		RGB brightBack = yuvBright.RGB();
		assertClose(brightColor, brightBack, 0.001f, "YUV bright color round trip");

		// Verify Y calculation for specific color
		RGB testColor = new RGB(0.5f, 0.3f, 0.7f);
		YUV yuvTest = testColor.YUV();
		float expectedY = 0.299f * 0.5f + 0.587f * 0.3f + 0.114f * 0.7f;
		assertEquals(expectedY, yuvTest.Y(), 0.001f, "YUV Y calculation");

		// Test YUV specific properties
		// U = 0.492 * (B - Y), V = 0.877 * (R - Y)
		float Y = yuvTest.Y();
		float expectedU = 0.492f * (0.7f - Y);
		float expectedV = 0.877f * (0.5f - Y);
		assertEquals(expectedU, yuvTest.U(), 0.001f, "YUV U calculation");
		assertEquals(expectedV, yuvTest.V(), 0.001f, "YUV V calculation");

		// Test that different colors produce different U/V values
		RGB color1 = new RGB(0.8f, 0.2f, 0.3f);
		RGB color2 = new RGB(0.2f, 0.8f, 0.3f);
		YUV yuv1 = color1.YUV();
		YUV yuv2 = color2.YUV();
		assertTrue(Math.abs(yuv1.U() - yuv2.U()) > 0.01f, "Different colors should have different U");
		assertTrue(Math.abs(yuv1.V() - yuv2.V()) > 0.01f, "Different colors should have different V");

		// Test colors at maximum chromaticity
		// Pure red should have maximum positive V
		assertTrue(yuvRed.V() > 0.4f, "Red should have large positive V");
		// Pure green should have negative U and V
		assertTrue(yuvGreen.U() < -0.2f, "Green should have negative U");
		assertTrue(yuvGreen.V() < -0.2f, "Green should have negative V");

		// Test round trip
		roundTripF(new RGB(0.5f, 0.3f, 0.7f), (RGB r) -> r.YUV(), YUV::RGB, "YUV");
	}
}
