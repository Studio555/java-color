
package com.esotericsoftware.color;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.Observer;
import com.esotericsoftware.color.space.RGB;
import com.esotericsoftware.color.space.XYZ;
import com.esotericsoftware.color.space.uv;
import com.esotericsoftware.color.space.xy;

public class SpectralLocusTests extends Tests {
	@Test
	public void testWavelengthToUV () {
		// Test boundary wavelengths
		uv uv380 = SpectralLocus.uv(380);
		assertEquals(0.2568657f, uv380.u(), 0.0001f, "380nm u'");
		assertEquals(0.016464427f, uv380.v(), 0.0001f, "380nm v'");

		uv uv700 = SpectralLocus.uv(700);
		assertEquals(0.6233662f, uv700.u(), 0.0001f, "700nm u'");
		assertEquals(0.5064951f, uv700.v(), 0.0001f, "700nm v'");

		// Test exact wavelength in the data
		uv uv500 = SpectralLocus.uv(500);
		assertEquals(0.0034601416f, uv500.u(), 0.0001f, "500nm u'");
		assertEquals(0.51306874f, uv500.v(), 0.0001f, "500nm v'");

		// Test interpolation between points
		uv uv550 = SpectralLocus.uv(550);
		assertTrue(uv550.u() > 0.1f && uv550.u() < 0.2f, "550nm u' in expected range");
		assertTrue(uv550.v() > 0.57f && uv550.v() < 0.59f, "550nm v' in expected range");

		// Test out of range
		try {
			SpectralLocus.uv(379);
			Assertions.fail("Should throw exception for wavelength < 380");
		} catch (IllegalArgumentException expected) {
		}

		try {
			SpectralLocus.uv(701);
			Assertions.fail("Should throw exception for wavelength > 700");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testWavelengthToXY () {
		// Test conversion through xy
		xy xy500 = SpectralLocus.xy(500);
		Assertions.assertNotNull(xy500);

		// Verify xy values are reasonable
		assertTrue(xy500.x() >= 0 && xy500.x() <= 1, "xy x coordinate in range");
		assertTrue(xy500.y() >= 0 && xy500.y() <= 1, "xy y coordinate in range");

		// Actual values for 500nm
		assertEquals(0.00817f, xy500.x(), 0.0001f, "500nm x");
		assertEquals(0.53842f, xy500.y(), 0.0001f, "500nm y");
	}

	@Test
	public void testContains () {
		// Test points on the spectral locus - should be inside with EPSILON tolerance
		assertTrue(SpectralLocus.contains(SpectralLocus.uv(450)), "450nm on locus");
		assertTrue(SpectralLocus.contains(SpectralLocus.uv(550)), "550nm on locus");
		assertTrue(SpectralLocus.contains(SpectralLocus.uv(650)), "650nm on locus");

		// Test a point clearly inside
		uv inside = new uv(0.2f, 0.3f);
		assertTrue(SpectralLocus.contains(inside), "Point inside locus");

		// Test white point (should be inside)
		uv d65 = Observer.Default.D65.uv();
		assertTrue(SpectralLocus.contains(d65), "D65 inside locus");

		// Test points outside
		Assertions.assertFalse(SpectralLocus.contains(new uv(0f, 0f)), "(0,0) outside locus");
		Assertions.assertFalse(SpectralLocus.contains(new uv(0.8f, 0.8f)), "(0.8,0.8) outside locus");

		// Test boundary conditions
		Assertions.assertFalse(SpectralLocus.contains(new uv(0.001f, 0.01f)), "Near origin outside locus");
		Assertions.assertFalse(SpectralLocus.contains(new uv(0.7f, 0.7f)), "Far corner outside locus");

		// Test near the purple line
		uv purple = new uv(0.3f, 0.15f);
		assertTrue(SpectralLocus.contains(purple), "Purple region inside locus");
	}

	@Test
	public void testDominantWavelength () {
		XYZ d65 = Observer.Default.D65;

		// Test pure spectral colors
		float wavelength = SpectralLocus.dominantWavelength(SpectralLocus.uv(500), d65);
		assertEquals(500f, wavelength, 1f, "500nm dominant wavelength");

		wavelength = SpectralLocus.dominantWavelength(SpectralLocus.uv(600), d65);
		assertEquals(600f, wavelength, 1f, "600nm dominant wavelength");

		// Test achromatic (white point itself)
		uv d65uv = d65.uv();
		assertTrue(Float.isNaN(SpectralLocus.dominantWavelength(d65uv, d65)), "White point returns NaN");

		// Test complementary colors (purples)
		// Create a purple that's between red and violet on the purple line
		uv violetEnd = SpectralLocus.uv(380);
		uv redEnd = SpectralLocus.uv(700);
		// Point on purple line
		float t = 0.5f;
		float pu = violetEnd.u() + t * (redEnd.u() - violetEnd.u());
		float pv = violetEnd.v() + t * (redEnd.v() - violetEnd.v());
		// Move from white point toward purple line (80% of the way)
		uv d65uv2 = d65.uv();
		float dx = pu - d65uv2.u();
		float dy = pv - d65uv2.v();
		uv purple = new uv(d65uv2.u() + 0.8f * dx, d65uv2.v() + 0.8f * dy);
		wavelength = SpectralLocus.dominantWavelength(purple, d65);
		assertTrue(wavelength < 0, "Purple returns negative wavelength"); // Should be negative for complementary

		// Test default white point method
		wavelength = SpectralLocus.dominantWavelength(SpectralLocus.uv(550));
		assertEquals(550f, wavelength, 1f, "550nm default white point");
	}

	@Test
	public void testDominantWavelengthRGB () {
		XYZ d65 = Observer.Default.D65;

		// Test some RGB colors
		RGB red = new RGB(1, 0, 0);
		uv redUV = red.XYZ().uv();
		float redWavelength = SpectralLocus.dominantWavelength(redUV, d65);
		assertTrue(redWavelength > 600 && redWavelength < 700, "Red wavelength in expected range"); // Red should be ~700nm

		// Green should be ~520-550nm
		RGB green = new RGB(0, 1, 0);
		uv greenUV = green.uv();
		float greenWavelength = SpectralLocus.dominantWavelength(greenUV, d65);
		assertTrue(greenWavelength > 500 && greenWavelength < 570, "Green wavelength in expected range");

		// Blue should be ~470nm
		RGB blue = new RGB(0, 0, 1);
		uv blueUV = blue.XYZ().xy().uv();
		float blueWavelength = SpectralLocus.dominantWavelength(blueUV, d65);
		assertTrue(blueWavelength > 440 && blueWavelength < 480, "Blue wavelength in expected range");

		// Magenta should be complementary (negative)
		RGB magenta = new RGB(1, 0, 1);
		uv magentaUV = magenta.XYZ().uv();
		float magentaWavelength = SpectralLocus.dominantWavelength(magentaUV, d65);
		assertTrue(magentaWavelength < 0, "Magenta returns negative wavelength");
	}

	@Test
	public void testExcitationPurity () {
		XYZ d65 = Observer.Default.D65;
		uv d65uv = d65.xy().uv();

		// Test achromatic (white point)
		assertEquals(0f, SpectralLocus.excitationPurity(d65uv, d65), 0.0001f, "White point purity");

		// Test pure spectral colors (should be close to 1)
		float purity = SpectralLocus.excitationPurity(SpectralLocus.uv(500), d65);
		assertTrue(purity > 0.95f && purity <= 1f, "Pure spectral color has high purity");

		// Test desaturated colors
		RGB gray = new RGB(0.5f, 0.5f, 0.5f);
		uv grayUV = gray.xy().uv();
		purity = SpectralLocus.excitationPurity(grayUV, d65);
		assertTrue(purity < 0.1f, "Gray has low purity"); // Gray should have very low purity

		// Test moderately saturated colors
		RGB orange = new RGB(1, 0.5f, 0);
		uv orangeUV = orange.uv();
		purity = SpectralLocus.excitationPurity(orangeUV, d65);
		assertTrue(purity > 0.3f && purity < 0.9f, "Orange has moderate purity"); // Orange should have moderate purity

		// Test purple (complementary)
		RGB purple = new RGB(0.5f, 0, 0.5f);
		uv purpleUV = purple.XYZ().uv();
		purity = SpectralLocus.excitationPurity(purpleUV, d65);
		assertTrue(purity > 0 && purity <= 1f, "Purple has valid purity"); // Should still be valid
	}

	@Test
	public void testExcitationPurityEdgeCases () {
		XYZ d65 = Observer.Default.D65;

		// Test point outside spectral locus
		uv outside = new uv(0.8f, 0.8f);
		float purity = SpectralLocus.excitationPurity(outside, d65);
		// The current implementation may return 1.0 for points outside
		assertTrue(purity >= 0, "Purity is non-negative");

		// Test with different white points
		XYZ a = Observer.Default.A; // Incandescent
		RGB blue = new RGB(0, 0, 1);
		uv blueUV = blue.uv();

		float purityD65 = SpectralLocus.excitationPurity(blueUV, d65);
		float purityA = SpectralLocus.excitationPurity(blueUV, a);

		// Purity should differ with different white points
		Assertions.assertNotEquals(purityD65, purityA, 0.01f, "Purity differs with white point");
	}

	@Test
	public void testPurpleLineColors () {
		XYZ d65 = Observer.Default.D65;

		// Test various purple/magenta colors that should return negative wavelengths

		// Test magenta (should be complementary)
		RGB magenta = new RGB(1, 0, 1);
		uv magentaUV = magenta.uv();
		float magentaWavelength = SpectralLocus.dominantWavelength(magentaUV, d65);
		assertTrue(magentaWavelength < 0, "Magenta has negative wavelength");
		assertTrue(Math.abs(magentaWavelength) >= 380 && Math.abs(magentaWavelength) <= 700,
			"Magenta complementary wavelength in valid range");

		// Test a color on the purple line itself
		uv violetEnd = SpectralLocus.uv(380);
		uv redEnd = SpectralLocus.uv(700);
		// Point halfway on purple line
		float purpleU = violetEnd.u() + 0.5f * (redEnd.u() - violetEnd.u());
		float purpleV = violetEnd.v() + 0.5f * (redEnd.v() - violetEnd.v());
		uv purpleLine = new uv(purpleU, purpleV);

		// Test point on purple line itself
		assertTrue(SpectralLocus.contains(purpleLine), "Point on purple line is inside spectral locus");
		float purpleLineWavelength = SpectralLocus.dominantWavelength(purpleLine, d65);
		// A point exactly on the purple line should still work (might be positive or negative depending on rounding)
		Assertions.assertFalse(Float.isNaN(purpleLineWavelength), "Purple line point returns valid wavelength");

		// Move slightly inside from purple line toward white point
		uv d65uv = d65.uv();
		float dx = d65uv.u() - purpleU;
		float dy = d65uv.v() - purpleV;
		uv insidePurple = new uv(purpleU + 0.1f * dx, purpleV + 0.1f * dy);

		float insidePurpleWavelength = SpectralLocus.dominantWavelength(insidePurple, d65);
		assertTrue(insidePurpleWavelength < 0, "Color inside purple line has negative wavelength");

		// Test excitation purity for purple colors
		float magentaPurity = SpectralLocus.excitationPurity(magentaUV, d65);
		assertTrue(magentaPurity > 0 && magentaPurity <= 1, "Magenta purity in valid range");
		assertTrue(magentaPurity > 0.5f, "Magenta should have high purity");

		// Test that different purple shades give different complementary wavelengths
		RGB purple1 = new RGB(0.6f, 0, 0.8f);
		RGB purple2 = new RGB(0.8f, 0, 0.6f);
		uv purple1UV = purple1.XYZ().uv();
		uv purple2UV = purple2.uv();

		float wavelength1 = SpectralLocus.dominantWavelength(purple1UV, d65);
		float wavelength2 = SpectralLocus.dominantWavelength(purple2UV, d65);

		assertTrue(wavelength1 < 0 && wavelength2 < 0, "Both purples have negative wavelengths");
		Assertions.assertNotEquals(wavelength1, wavelength2, 1f, "Different purples have different complementary wavelengths");
	}
}
