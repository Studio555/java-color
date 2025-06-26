
package com.esotericsoftware.color;

import static com.esotericsoftware.color.TestsUtil.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.HSL;
import com.esotericsoftware.color.space.RGB;

public class ColorHarmonyTests {
	@Test
	public void testAnalogous () {
		// Test with various angles
		RGB baseColor = new RGB(1, 0, 0); // Red

		// Test with 15° angle
		RGB[] analogous15 = baseColor.analogous(15);
		assertEquals(3, analogous15.length, "Analogous should return 3 colors");
		assertRecordClose(baseColor, analogous15[1], "Middle color should be base color");

		HSL base = baseColor.HSL();
		HSL left15 = analogous15[0].HSL();
		HSL right15 = analogous15[2].HSL();

		// Check hue differences
		float expectedLeft = base.H() - 15;
		if (expectedLeft < 0) expectedLeft += 360;
		float expectedRight = base.H() + 15;
		if (expectedRight >= 360) expectedRight -= 360;

		assertClose(expectedLeft, left15.H(), "Left analogous hue (15°)", 1);
		assertClose(expectedRight, right15.H(), "Right analogous hue (15°)", 1);

		// Test with 30° angle
		RGB[] analogous30 = baseColor.analogous(30);
		HSL left30 = analogous30[0].HSL();
		HSL right30 = analogous30[2].HSL();

		expectedLeft = base.H() - 30;
		if (expectedLeft < 0) expectedLeft += 360;
		expectedRight = base.H() + 30;
		if (expectedRight >= 360) expectedRight -= 360;

		assertClose(expectedLeft, left30.H(), "Left analogous hue (30°)", 1);
		assertClose(expectedRight, right30.H(), "Right analogous hue (30°)", 1);

		// Test with 45° angle
		RGB[] analogous45 = baseColor.analogous(45);
		HSL left45 = analogous45[0].HSL();
		HSL right45 = analogous45[2].HSL();

		expectedLeft = base.H() - 45;
		if (expectedLeft < 0) expectedLeft += 360;
		expectedRight = base.H() + 45;
		if (expectedRight >= 360) expectedRight -= 360;

		assertClose(expectedLeft, left45.H(), "Left analogous hue (45°)", 1);
		assertClose(expectedRight, right45.H(), "Right analogous hue (45°)", 1);

		// Test edge cases with 0° angle (all three should be the same)
		RGB[] analogous0 = baseColor.analogous(0);
		assertRecordClose(baseColor, analogous0[0], "0° analogous left should equal base");
		assertRecordClose(baseColor, analogous0[1], "0° analogous middle should equal base");
		assertRecordClose(baseColor, analogous0[2], "0° analogous right should equal base");

		// Test with 360° angle (should wrap around)
		RGB greenBase = new RGB(0, 1, 0);
		RGB[] analogous360 = greenBase.analogous(360);
		// All three should be the same since 360° is a full circle
		assertRecordClose(greenBase, analogous360[0], "360° analogous left should equal base");
		assertRecordClose(greenBase, analogous360[1], "360° analogous middle should equal base");
		assertRecordClose(greenBase, analogous360[2], "360° analogous right should equal base");

		// Test saturation and lightness preservation
		RGB[] testColors = new RGB(0.7f, 0.3f, 0.5f).analogous(20);
		HSL testBase = testColors[1].HSL();
		HSL testLeft = testColors[0].HSL();
		HSL testRight = testColors[2].HSL();

		assertClose(testBase.S(), testLeft.S(), "Left analogous should preserve saturation", 0.01);
		assertClose(testBase.S(), testRight.S(), "Right analogous should preserve saturation", 0.01);
		assertClose(testBase.L(), testLeft.L(), "Left analogous should preserve lightness", 0.01);
		assertClose(testBase.L(), testRight.L(), "Right analogous should preserve lightness", 0.01);
	}

	@Test
	public void testComplementary () {
		// Test with primary colors
		RGB red = new RGB(1, 0, 0);
		RGB redComplement = red.complementary();
		// Red (0°) complement should be cyan (180°)
		HSL redComplementHSL = redComplement.HSL();
		assertClose(180, redComplementHSL.H(), "Red complement hue", 1);

		RGB green = new RGB(0, 1, 0);
		RGB greenComplement = green.complementary();
		// Green (120°) complement should be magenta (300°)
		HSL greenComplementHSL = greenComplement.HSL();
		assertClose(300, greenComplementHSL.H(), "Green complement hue", 1);

		RGB blue = new RGB(0, 0, 1);
		RGB blueComplement = blue.complementary();
		// Blue (240°) complement should be yellow (60°)
		HSL blueComplementHSL = blueComplement.HSL();
		assertClose(60, blueComplementHSL.H(), "Blue complement hue", 1);

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
		assertClose(120, magentaComplementHSL.H(), "Magenta complement hue", 1);

		RGB yellow = new RGB(1, 1, 0);
		RGB yellowComplement = yellow.complementary();
		// Yellow (60°) complement should be blue (240°)
		HSL yellowComplementHSL = yellowComplement.HSL();
		assertClose(240, yellowComplementHSL.H(), "Yellow complement hue", 1);

		// Test with gray (should return gray)
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		RGB grayComplement = gray.complementary();
		assertRecordClose(gray, grayComplement, "Gray complement should be gray", 0.01);

		// Verify that applying complementary twice returns original color
		RGB testColor = new RGB(0.7f, 0.3f, 0.5f);
		RGB complement = testColor.complementary();
		RGB doubleComplement = complement.complementary();
		assertRecordClose(testColor, doubleComplement, "Double complement should return original", 0.01);
	}

	@Test
	public void testSplitComplementary () {
		// Test with primary colors
		RGB red = new RGB(1, 0, 0);
		RGB[] splitComp = red.splitComplementary();

		assertEquals(3, splitComp.length, "Split complementary should return 3 colors");
		assertRecordClose(red, splitComp[0], "First color should be base color");

		HSL base = red.HSL();
		HSL split1 = splitComp[1].HSL();
		HSL split2 = splitComp[2].HSL();

		// Check that splits are at +150° and +210° from base
		float expected1 = base.H() + 150;
		float expected2 = base.H() + 210;
		if (expected1 >= 360) expected1 -= 360;
		if (expected2 >= 360) expected2 -= 360;

		assertClose(expected1, split1.H(), "First split complementary hue", 1);
		assertClose(expected2, split2.H(), "Second split complementary hue", 1);

		// Test that splits are equidistant from true complement
		float complement = base.H() + 180;
		if (complement >= 360) complement -= 360;

		float dist1 = Math.abs(split1.H() - complement);
		float dist2 = Math.abs(complement - split2.H());
		assertClose(dist1, dist2, "Splits should be equidistant from complement", 1);

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

		assertClose(expected1, greenSplit1.H(), "Green first split hue", 1);
		assertClose(expected2, greenSplit2.H(), "Green second split hue", 1);

		// Test saturation and lightness preservation
		RGB testColor = new RGB(0.7f, 0.3f, 0.5f);
		RGB[] testSplit = testColor.splitComplementary();
		HSL testBase = testSplit[0].HSL();
		HSL testSplit1 = testSplit[1].HSL();
		HSL testSplit2 = testSplit[2].HSL();

		assertClose(testBase.S(), testSplit1.S(), "Split 1 should preserve saturation", 0.01);
		assertClose(testBase.S(), testSplit2.S(), "Split 2 should preserve saturation", 0.01);
		assertClose(testBase.L(), testSplit1.L(), "Split 1 should preserve lightness", 0.01);
		assertClose(testBase.L(), testSplit2.L(), "Split 2 should preserve lightness", 0.01);
	}

	@Test
	public void testTriadic () {
		// Test with primary colors
		RGB red = new RGB(1, 0, 0);
		RGB[] triadicRed = red.triadic();

		assertEquals(3, triadicRed.length, "Triadic should return 3 colors");
		assertRecordClose(red, triadicRed[0], "First color should be base color");

		HSL base = red.HSL();
		HSL color2 = triadicRed[1].HSL();
		HSL color3 = triadicRed[2].HSL();

		// Check that colors are 120° apart
		float expected2 = base.H() + 120;
		float expected3 = base.H() + 240;
		if (expected2 >= 360) expected2 -= 360;
		if (expected3 >= 360) expected3 -= 360;

		assertClose(expected2, color2.H(), "Second triadic hue", 1);
		assertClose(expected3, color3.H(), "Third triadic hue", 1);

		// Test that it forms the RGB primary triad
		RGB[] rgbTriad = red.triadic();
		// Red (0°) -> Green (120°) -> Blue (240°)
		assertClose(0, rgbTriad[0].HSL().H(), "Red hue", 1);
		assertClose(120, rgbTriad[1].HSL().H(), "Green hue", 1);
		assertClose(240, rgbTriad[2].HSL().H(), "Blue hue", 1);

		// Test with green
		RGB green = new RGB(0, 1, 0);
		RGB[] triadicGreen = green.triadic();
		// Green (120°) -> Blue (240°) -> Red (360°/0°)
		assertClose(120, triadicGreen[0].HSL().H(), "Green hue", 1);
		assertClose(240, triadicGreen[1].HSL().H(), "Blue hue", 1);
		float redHue = triadicGreen[2].HSL().H();
		assertTrue(redHue < 1 || redHue > 359, "Red hue should be ~0° (was " + redHue + ")");

		// Test saturation and lightness preservation for all colors
		RGB testColor = new RGB(0.8f, 0.3f, 0.5f);
		RGB[] testTriadic = testColor.triadic();
		HSL testBase = testTriadic[0].HSL();
		HSL test2 = testTriadic[1].HSL();
		HSL test3 = testTriadic[2].HSL();

		assertClose(testBase.S(), test2.S(), "Triadic 2 should preserve saturation", 0.01);
		assertClose(testBase.S(), test3.S(), "Triadic 3 should preserve saturation", 0.01);
		assertClose(testBase.L(), test2.L(), "Triadic 2 should preserve lightness", 0.01);
		assertClose(testBase.L(), test3.L(), "Triadic 3 should preserve lightness", 0.01);

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

			assertClose(120, diff12, "Triadic spacing 1->2 at hue " + hue, 2);
			assertClose(120, diff23, "Triadic spacing 2->3 at hue " + hue, 2);
			assertClose(120, diff31, "Triadic spacing 3->1 at hue " + hue, 2);
		}

		// Test with gray (should handle achromatic case)
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		RGB[] triadicGray = gray.triadic();
		// All three should remain gray since there's no hue
		for (RGB color : triadicGray) {
			HSL hsl = color.HSL();
			assertClose(0, hsl.S(), "Triadic gray should have no saturation", 0.01);
			assertClose(0.5f, hsl.L(), "Triadic gray should preserve lightness", 0.01);
		}
	}
}
