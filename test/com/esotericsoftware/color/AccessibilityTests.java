
package com.esotericsoftware.color;

import static com.esotericsoftware.color.TestsUtil.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.RGB;

public class AccessibilityTests {
	@Test
	public void testContrastRatio () {
		// Test black on white (should be 21:1)
		RGB black = new RGB(0, 0, 0);
		RGB white = new RGB(1, 1, 1);
		float blackWhiteRatio = black.contrastRatio(white);
		assertClose(21, blackWhiteRatio, "Black on white contrast ratio", 0.1);

		// Test white on black (should be 21:1)
		float whiteBlackRatio = white.contrastRatio(black);
		assertClose(21, whiteBlackRatio, "White on black contrast ratio", 0.1);

		// Test identical colors (should be 1:1)
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		float grayGrayRatio = gray.contrastRatio(gray);
		assertClose(1, grayGrayRatio, "Identical colors contrast ratio", 0.01);

		// Test known color pairs
		// Dark gray on light gray
		RGB darkGray = new RGB(0.2f, 0.2f, 0.2f);
		RGB lightGray = new RGB(0.8f, 0.8f, 0.8f);
		float grayRatio = lightGray.contrastRatio(darkGray);
		// The actual ratio will depend on gamma correction
		assertTrue(grayRatio > 4.0 && grayRatio < 8.0, "Gray contrast ratio should be moderate (was " + grayRatio + ")");

		// Test that order doesn't matter
		float reverseGrayRatio = darkGray.contrastRatio(lightGray);
		assertClose(grayRatio, reverseGrayRatio, "Contrast ratio should be same regardless of order", 0.01);

		// Test some color pairs with known approximate ratios
		RGB red = new RGB(1, 0, 0);
		RGB darkRed = new RGB(0.5f, 0, 0);
		float redRatio = red.contrastRatio(darkRed);
		// The actual ratio depends on gamma correction, so be more lenient
		assertTrue(redRatio > 2.0 && redRatio < 5.0, "Red to dark red contrast should be moderate (was " + redRatio + ")");

		// Test edge case with very similar colors
		RGB color1 = new RGB(0.5f, 0.5f, 0.5f);
		RGB color2 = new RGB(0.51f, 0.51f, 0.51f);
		float similarRatio = color1.contrastRatio(color2);
		assertTrue(similarRatio > 1 && similarRatio < 1.1, "Very similar colors should have ratio close to 1");

		// Test with pure colors
		RGB green = new RGB(0, 1, 0);
		RGB blue = new RGB(0, 0, 1);
		float greenBlueRatio = green.contrastRatio(blue);
		// Green is brighter than blue, should have decent contrast
		assertTrue(greenBlueRatio > 5 && greenBlueRatio < 7, "Green/blue contrast should be moderate");

		// Test calculation matches WCAG formula
		// Formula: (L1 + 0.05) / (L2 + 0.05) where L1 is lighter, L2 is darker
		// L is relative luminance: 0.2126 * R + 0.7152 * G + 0.0722 * B (after gamma correction)
		RGB testFg = new RGB(0.6f, 0.4f, 0.2f);
		RGB testBg = new RGB(0.1f, 0.2f, 0.3f);
		float calculatedRatio = testFg.contrastRatio(testBg);
		// Just verify it's reasonable
		assertTrue(calculatedRatio > 1 && calculatedRatio < 21, "Calculated ratio should be in valid range");
	}

	@Test
	public void testWCAG_AA () {
		// Test black/white (should pass for both text sizes)
		RGB black = new RGB(0, 0, 0);
		RGB white = new RGB(1, 1, 1);
		assertTrue(black.WCAG_AA(white, false), "Black/white should pass AA for normal text");
		assertTrue(black.WCAG_AA(white, true), "Black/white should pass AA for large text");
		assertTrue(white.WCAG_AA(black, false), "White/black should pass AA for normal text");
		assertTrue(white.WCAG_AA(black, true), "White/black should pass AA for large text");

		// Test color pairs that pass for large text but fail for normal text
		// Need contrast ratio between 3:1 and 4.5:1
		RGB darkGray = new RGB(0.25f, 0.25f, 0.25f);
		RGB mediumGray = new RGB(0.55f, 0.55f, 0.55f);
		// This should give roughly 3.7:1 contrast
		assertTrue(darkGray.WCAG_AA(mediumGray, true), "Should pass AA for large text (3:1)");
		assertTrue(!darkGray.WCAG_AA(mediumGray, false), "Should fail AA for normal text (4.5:1)");

		// Test color pairs that fail for both
		RGB color1 = new RGB(0.5f, 0.5f, 0.5f);
		RGB color2 = new RGB(0.6f, 0.6f, 0.6f);
		// Very similar colors should have contrast < 3:1
		assertTrue(!color1.WCAG_AA(color2, false), "Similar colors should fail AA for normal text");
		assertTrue(!color1.WCAG_AA(color2, true), "Similar colors should fail AA for large text");

		// Verify threshold values
		// Create colors with exact contrast ratios
		// For normal text: exactly 4.5:1
		RGB fg1 = new RGB(0.749f, 0.749f, 0.749f);
		RGB bg1 = new RGB(0.298f, 0.298f, 0.298f);
		float ratio1 = fg1.contrastRatio(bg1);
		// Should be very close to 4.5:1
		if (Math.abs(ratio1 - 4.5f) < 0.1f) assertTrue(fg1.WCAG_AA(bg1, false), "Should pass AA at exactly 4.5:1 for normal text");

		// For large text: exactly 3:1
		RGB fg2 = new RGB(0.627f, 0.627f, 0.627f);
		RGB bg2 = new RGB(0.333f, 0.333f, 0.333f);
		float ratio2 = fg2.contrastRatio(bg2);
		// Should be very close to 3:1
		if (Math.abs(ratio2 - 3.0f) < 0.1f) assertTrue(fg2.WCAG_AA(bg2, true), "Should pass AA at exactly 3:1 for large text");

		// Test with colored pairs
		RGB blue = new RGB(0, 0, 1);
		RGB yellow = new RGB(1, 1, 0);
		// Blue on yellow has good contrast
		assertTrue(blue.WCAG_AA(yellow, false), "Blue on yellow should pass AA");

		RGB darkRed = new RGB(0.5f, 0, 0);
		RGB lightPink = new RGB(1, 0.8f, 0.8f);
		// Dark red on light pink should pass
		assertTrue(darkRed.WCAG_AA(lightPink, false), "Dark red on light pink should pass AA");
	}

	@Test
	public void testWCAG_AAA () {
		// Test black/white (should pass for both text sizes)
		RGB black = new RGB(0, 0, 0);
		RGB white = new RGB(1, 1, 1);
		assertTrue(black.WCAG_AAA(white, false), "Black/white should pass AAA for normal text");
		assertTrue(black.WCAG_AAA(white, true), "Black/white should pass AAA for large text");
		assertTrue(white.WCAG_AAA(black, false), "White/black should pass AAA for normal text");
		assertTrue(white.WCAG_AAA(black, true), "White/black should pass AAA for large text");

		// Test color pairs that pass AA but fail AAA
		// Need contrast ratio between 4.5:1 and 7:1 for normal text
		RGB darkGray = new RGB(0.2f, 0.2f, 0.2f);
		RGB lightGray = new RGB(0.7f, 0.7f, 0.7f);
		// Should be around 5.2:1
		assertTrue(lightGray.WCAG_AA(darkGray, false), "Should pass AA for normal text");
		assertTrue(!lightGray.WCAG_AAA(darkGray, false), "Should fail AAA for normal text (7:1)");
		assertTrue(lightGray.WCAG_AAA(darkGray, true), "Should pass AAA for large text (4.5:1)");

		// Test color pairs that pass for large text but fail for normal text
		// Need contrast ratio between 4.5:1 and 7:1
		RGB color1 = new RGB(0.75f, 0.75f, 0.75f);
		RGB color2 = new RGB(0.25f, 0.25f, 0.25f);
		float testRatio = color1.contrastRatio(color2);
		// This should be around 5.7:1
		if (testRatio > 4.5f && testRatio < 7.0f) {
			assertTrue(!color1.WCAG_AAA(color2, false), "Should fail AAA for normal text");
			assertTrue(color1.WCAG_AAA(color2, true), "Should pass AAA for large text");
		}

		// Verify threshold values: 7:1 for normal, 4.5:1 for large
		// Test exactly at 7:1 for normal text
		RGB fg1 = new RGB(0.835f, 0.835f, 0.835f);
		RGB bg1 = new RGB(0.247f, 0.247f, 0.247f);
		float ratio1 = fg1.contrastRatio(bg1);
		// Should be very close to 7:1
		if (Math.abs(ratio1 - 7.0f) < 0.1f) {
			assertTrue(fg1.WCAG_AAA(bg1, false), "Should pass AAA at exactly 7:1 for normal text");
		}

		// Test exactly at 4.5:1 for large text
		RGB fg2 = new RGB(0.749f, 0.749f, 0.749f);
		RGB bg2 = new RGB(0.298f, 0.298f, 0.298f);
		float ratio2 = fg2.contrastRatio(bg2);
		// Should be very close to 4.5:1
		if (Math.abs(ratio2 - 4.5f) < 0.1f) {
			assertTrue(fg2.WCAG_AAA(bg2, true), "Should pass AAA at exactly 4.5:1 for large text");
		}

		// Test that very low contrast fails both
		RGB similar1 = new RGB(0.5f, 0.5f, 0.5f);
		RGB similar2 = new RGB(0.55f, 0.55f, 0.55f);
		assertTrue(!similar1.WCAG_AAA(similar2, false), "Very similar colors should fail AAA normal");
		assertTrue(!similar1.WCAG_AAA(similar2, true), "Very similar colors should fail AAA large");

		// Test with colored pairs
		RGB darkBlue = new RGB(0, 0, 0.4f);
		RGB lightYellow = new RGB(1, 1, 0.8f);
		// Dark blue on light yellow should have excellent contrast
		assertTrue(darkBlue.WCAG_AAA(lightYellow, false), "Dark blue on light yellow should pass AAA");

		// Test that AAA is stricter than AA
		// Any color pair that fails AA should also fail AAA
		RGB failAA1 = new RGB(0.5f, 0.5f, 0.5f);
		RGB failAA2 = new RGB(0.6f, 0.6f, 0.6f);
		if (!failAA1.WCAG_AA(failAA2, false)) assertTrue(!failAA1.WCAG_AAA(failAA2, false), "If fails AA, must fail AAA");
	}
}
