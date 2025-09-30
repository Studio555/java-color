
package com.esotericsoftware.color;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.HSL;
import com.esotericsoftware.color.space.RGB;

public class ColorHarmonyTests extends Tests {
	@Test
	public void testAnalogous () {
		// Test with various angles
		RGB baseColor = new RGB(1, 0, 0); // Red

		// Test with 15° angle
		RGB[] analogous15 = baseColor.analogous(15);
		assertEquals(3, analogous15.length, "Analogous should return 3 colors");
		assertClose(baseColor, analogous15[1], "Middle color should be base color");

		HSL base = baseColor.HSL();
		HSL left15 = analogous15[0].HSL();
		HSL right15 = analogous15[2].HSL();

		// Check hue differences
		float expectedLeft = base.H() - 15;
		if (expectedLeft < 0) expectedLeft += 360;
		float expectedRight = base.H() + 15;
		if (expectedRight >= 360) expectedRight -= 360;

		assertEquals(expectedLeft, left15.H(), 1, "Left analogous hue (15°)");
		assertEquals(expectedRight, right15.H(), 1, "Right analogous hue (15°)");

		// Test with 30° angle
		RGB[] analogous30 = baseColor.analogous(30);
		HSL left30 = analogous30[0].HSL();
		HSL right30 = analogous30[2].HSL();

		expectedLeft = base.H() - 30;
		if (expectedLeft < 0) expectedLeft += 360;
		expectedRight = base.H() + 30;
		if (expectedRight >= 360) expectedRight -= 360;

		assertEquals(expectedLeft, left30.H(), 1, "Left analogous hue (30°)");
		assertEquals(expectedRight, right30.H(), 1, "Right analogous hue (30°)");

		// Test with 45° angle
		RGB[] analogous45 = baseColor.analogous(45);
		HSL left45 = analogous45[0].HSL();
		HSL right45 = analogous45[2].HSL();

		expectedLeft = base.H() - 45;
		if (expectedLeft < 0) expectedLeft += 360;
		expectedRight = base.H() + 45;
		if (expectedRight >= 360) expectedRight -= 360;

		assertEquals(expectedLeft, left45.H(), 1, "Left analogous hue (45°)");
		assertEquals(expectedRight, right45.H(), 1, "Right analogous hue (45°)");

		// Test edge cases with 0° angle (all three should be the same)
		RGB[] analogous0 = baseColor.analogous(0);
		assertClose(baseColor, analogous0[0], "0° analogous left should equal base");
		assertClose(baseColor, analogous0[1], "0° analogous middle should equal base");
		assertClose(baseColor, analogous0[2], "0° analogous right should equal base");

		// Test with 360° angle (should wrap around)
		RGB greenBase = new RGB(0, 1, 0);
		RGB[] analogous360 = greenBase.analogous(360);
		// All three should be the same since 360° is a full circle
		assertClose(greenBase, analogous360[0], "360° analogous left should equal base");
		assertClose(greenBase, analogous360[1], "360° analogous middle should equal base");
		assertClose(greenBase, analogous360[2], "360° analogous right should equal base");

		// Test saturation and lightness preservation
		RGB[] testColors = new RGB(0.7f, 0.3f, 0.5f).analogous(20);
		HSL testBase = testColors[1].HSL();
		HSL testLeft = testColors[0].HSL();
		HSL testRight = testColors[2].HSL();

		assertEquals(testBase.S(), testLeft.S(), 0.01, "Left analogous should preserve saturation");
		assertEquals(testBase.S(), testRight.S(), 0.01, "Right analogous should preserve saturation");
		assertEquals(testBase.L(), testLeft.L(), 0.01, "Left analogous should preserve lightness");
		assertEquals(testBase.L(), testRight.L(), 0.01, "Right analogous should preserve lightness");
	}

	@Test
	public void testComplementary () {
		// Test with primary colors
		RGB red = new RGB(1, 0, 0);
		RGB redComplement = red.complementary();
		// Red (0°) complement should be cyan (180°)
		HSL redComplementHSL = redComplement.HSL();
		assertEquals(180, redComplementHSL.H(), 1, "Red complement hue");

		RGB green = new RGB(0, 1, 0);
		RGB greenComplement = green.complementary();
		// Green (120°) complement should be magenta (300°)
		HSL greenComplementHSL = greenComplement.HSL();
		assertEquals(300, greenComplementHSL.H(), 1, "Green complement hue");

		RGB blue = new RGB(0, 0, 1);
		RGB blueComplement = blue.complementary();
		// Blue (240°) complement should be yellow (60°)
		HSL blueComplementHSL = blueComplement.HSL();
		assertEquals(60, blueComplementHSL.H(), 1, "Blue complement hue");

		// Test with secondary colors
		RGB cyan = new RGB(0, 1, 1);
		RGB cyanComplement = cyan.complementary();
		// Cyan (180°) complement should be red (0° or 360°)
		HSL cyanComplementHSL = cyanComplement.HSL();
		// 359° and 0° are equivalent (both are red)
		float cyanCompHue = cyanComplementHSL.H();
		assertTrue(cyanCompHue < 1 || cyanCompHue > 358, "Cyan complement hue should be ~0° (was " + cyanCompHue + ")");

		RGB magenta = new RGB(1, 0, 1);
		RGB magentaComplement = magenta.complementary();
		// Magenta (300°) complement should be green (120°)
		HSL magentaComplementHSL = magentaComplement.HSL();
		assertEquals(120, magentaComplementHSL.H(), 1, "Magenta complement hue");

		RGB yellow = new RGB(1, 1, 0);
		RGB yellowComplement = yellow.complementary();
		// Yellow (60°) complement should be blue (240°)
		HSL yellowComplementHSL = yellowComplement.HSL();
		assertEquals(240, yellowComplementHSL.H(), 1, "Yellow complement hue");

		// Test with gray (should return gray)
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		RGB grayComplement = gray.complementary();
		assertClose(gray, grayComplement, 0.01, "Gray complement should be gray");

		// Verify that applying complementary twice returns original color
		RGB testColor = new RGB(0.7f, 0.3f, 0.5f);
		RGB complement = testColor.complementary();
		RGB doubleComplement = complement.complementary();
		assertClose(testColor, doubleComplement, 0.01, "Double complement should return original");
	}

	@Test
	public void testSplitComplementary () {
		// Test with primary colors
		RGB red = new RGB(1, 0, 0);
		RGB[] splitComp = red.splitComplementary();

		assertEquals(3, splitComp.length, "Split complementary should return 3 colors");
		assertClose(red, splitComp[0], "First color should be base color");

		HSL base = red.HSL();
		HSL split1 = splitComp[1].HSL();
		HSL split2 = splitComp[2].HSL();

		// Check that splits are at +150° and +210° from base
		float expected1 = base.H() + 150;
		float expected2 = base.H() + 210;
		if (expected1 >= 360) expected1 -= 360;
		if (expected2 >= 360) expected2 -= 360;

		assertEquals(expected1, split1.H(), 1, "First split complementary hue");
		assertEquals(expected2, split2.H(), 1, "Second split complementary hue");

		// Test that splits are equidistant from true complement
		float complement = base.H() + 180;
		if (complement >= 360) complement -= 360;

		float dist1 = Math.abs(split1.H() - complement);
		float dist2 = Math.abs(complement - split2.H());
		assertEquals(dist1, dist2, 1, "Splits should be equidistant from complement");

		// Test with different base colors
		RGB green = new RGB(0, 1, 0);
		RGB[] greenSplit = green.splitComplementary();
		HSL greenBase = green.HSL();
		HSL greenSplit1 = greenSplit[1].HSL();
		HSL greenSplit2 = greenSplit[2].HSL();

		expected1 = greenBase.H() + 150;
		expected2 = greenBase.H() + 210;
		if (expected1 >= 360) expected1 -= 360;
		if (expected2 >= 360) expected2 -= 360;

		assertEquals(expected1, greenSplit1.H(), 1, "Green first split hue");
		assertEquals(expected2, greenSplit2.H(), 1, "Green second split hue");

		// Test saturation and lightness preservation
		RGB testColor = new RGB(0.7f, 0.3f, 0.5f);
		RGB[] testSplit = testColor.splitComplementary();
		HSL testBase = testSplit[0].HSL();
		HSL testSplit1 = testSplit[1].HSL();
		HSL testSplit2 = testSplit[2].HSL();

		assertEquals(testBase.S(), testSplit1.S(), 0.01, "Split 1 should preserve saturation");
		assertEquals(testBase.S(), testSplit2.S(), 0.01, "Split 2 should preserve saturation");
		assertEquals(testBase.L(), testSplit1.L(), 0.01, "Split 1 should preserve lightness");
		assertEquals(testBase.L(), testSplit2.L(), 0.01, "Split 2 should preserve lightness");
	}

	@Test
	public void testTriadic () {
		// Test with primary colors
		RGB red = new RGB(1, 0, 0);
		RGB[] triadicRed = red.triadic();

		assertEquals(3, triadicRed.length, "Triadic should return 3 colors");
		assertClose(red, triadicRed[0], "First color should be base color");

		HSL base = red.HSL();
		HSL color2 = triadicRed[1].HSL();
		HSL color3 = triadicRed[2].HSL();

		// Check that colors are 120° apart
		float expected2 = base.H() + 120;
		float expected3 = base.H() + 240;
		if (expected2 >= 360) expected2 -= 360;
		if (expected3 >= 360) expected3 -= 360;

		assertEquals(expected2, color2.H(), 1, "Second triadic hue");
		assertEquals(expected3, color3.H(), 1, "Third triadic hue");

		// Test that it forms the RGB primary triad
		RGB[] rgbTriad = red.triadic();
		// Red (0°) -> Green (120°) -> Blue (240°)
		assertEquals(0, rgbTriad[0].HSL().H(), 1, "Red hue");
		assertEquals(120, rgbTriad[1].HSL().H(), 1, "Green hue");
		assertEquals(240, rgbTriad[2].HSL().H(), 1, "Blue hue");

		// Test with green
		RGB green = new RGB(0, 1, 0);
		RGB[] triadicGreen = green.triadic();
		// Green (120°) -> Blue (240°) -> Red (360°/0°)
		assertEquals(120, triadicGreen[0].HSL().H(), 1, "Green hue");
		assertEquals(240, triadicGreen[1].HSL().H(), 1, "Blue hue");
		float redHue = triadicGreen[2].HSL().H();
		assertTrue(redHue < 1 || redHue > 359, "Red hue should be ~0° (was " + redHue + ")");

		// Test saturation and lightness preservation for all colors
		RGB testColor = new RGB(0.8f, 0.3f, 0.5f);
		RGB[] testTriadic = testColor.triadic();
		HSL testBase = testTriadic[0].HSL();
		HSL test2 = testTriadic[1].HSL();
		HSL test3 = testTriadic[2].HSL();

		assertEquals(testBase.S(), test2.S(), 0.01, "Triadic 2 should preserve saturation");
		assertEquals(testBase.S(), test3.S(), 0.01, "Triadic 3 should preserve saturation");
		assertEquals(testBase.L(), test2.L(), 0.01, "Triadic 2 should preserve lightness");
		assertEquals(testBase.L(), test3.L(), 0.01, "Triadic 3 should preserve lightness");

		// Test with various hues to ensure correct spacing
		for (float hue = 0; hue < 360; hue += 45) {
			HSL hsl = new HSL(hue, 0.8f, 0.5f);
			RGB rgb = hsl.RGB();
			RGB[] triad = rgb.triadic();

			float h1 = triad[0].HSL().H();
			float h2 = triad[1].HSL().H();
			float h3 = triad[2].HSL().H();

			// Calculate the differences between hues
			float diff12 = h2 - h1;
			float diff23 = h3 - h2;
			float diff31 = h1 - h3;

			// Handle wraparound
			if (diff12 < 0) diff12 += 360;
			if (diff23 < 0) diff23 += 360;
			if (diff31 < 0) diff31 += 360;

			assertEquals(120, diff12, 2, "Triadic spacing 1->2 at hue " + hue);
			assertEquals(120, diff23, 2, "Triadic spacing 2->3 at hue " + hue);
			assertEquals(120, diff31, 2, "Triadic spacing 3->1 at hue " + hue);
		}

		// Test with gray (should handle achromatic case)
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		RGB[] triadicGray = gray.triadic();
		// All three should remain gray since there's no hue
		for (RGB color : triadicGray) {
			HSL hsl = color.HSL();
			assertEquals(0, hsl.S(), 0.01, "Triadic gray should have no saturation");
			assertEquals(0.5f, hsl.L(), 0.01, "Triadic gray should preserve lightness");
		}
	}
}
