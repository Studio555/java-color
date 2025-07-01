
package com.esotericsoftware.color;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.CCT;
import com.esotericsoftware.color.space.XYZ;

public class TM30Tests extends Tests {
	@Test
	public void testPlanckianRadiator () {
		// Test with 3000K Planckian radiator
		TM30 tm30 = new CCT(3000).illuminant().TM30();

		// Planckian radiator should have high fidelity
		assertTrue(tm30.Rf() > 90 && tm30.Rf() <= 100, "Planckian radiator at 3000K should have high Rf, got: " + tm30.Rf());

		// Gamut index should be near 100 (no increase or decrease)
		assertTrue(Math.abs(tm30.Rg() - 100) < 10, "Planckian radiator should have Rg near 100, got: " + tm30.Rg());

		// Color vector graphics coordinates should be reasonable
		float[] rcs = tm30.Rcs();
		float[] rhs = tm30.Rhs();
		assertEquals(16, rcs.length, "Should have 16 hue bins for chroma shift");
		assertEquals(16, rhs.length, "Should have 16 hue bins for hue shift");

		// All hue bin scores should be high
		for (int i = 0; i < 16; i++) {
			assertTrue(rcs[i] > -0.2, "Hue bin " + i + " chroma shift should be > -0.2, got: " + rcs[i]);
			assertTrue(Math.abs(rhs[i]) < 20, "Hue bin " + i + " hue shift should be small, got: " + rhs[i]);
		}
	}

	@Test
	public void testDaylight () {
		// Test with 5000K daylight
		TM30 tm30 = new CCT(5000).illuminant().TM30();

		// CIE daylight should have excellent scores
		assertTrue(tm30.Rf() > 85 && tm30.Rf() <= 100, "CIE daylight at 5000K should have very high Rf, got: " + tm30.Rf());

		// Gamut index should be near 100
		assertTrue(Math.abs(tm30.Rg() - 100) < 10, "CIE daylight should have Rg close to 100, got: " + tm30.Rg());
	}

	@Test
	public void testNarrowbandSpectrum () {
		// Create RGB LED-like spectrum
		float[] values = new float[81];
		for (int i = 0; i < 81; i++) {
			float wavelength = 380 + i * 5;
			// Blue peak around 450nm
			if (wavelength >= 440 && wavelength <= 460)
				values[i] = 80;
			// Green peak around 530nm
			else if (wavelength >= 520 && wavelength <= 540)
				values[i] = 100;
			// Red peak around 630nm
			else if (wavelength >= 620 && wavelength <= 640)
				values[i] = 90;
			else
				values[i] = 5; // Low baseline
		}
		// Normalize
		XYZ xyz = new Spectrum(values).XYZ();
		float scale = 100f / xyz.Y();
		for (int i = 0; i < 81; i++)
			values[i] *= scale;
		Spectrum spectrum = new Spectrum(values);

		TM30 tm30 = spectrum.TM30();

		// RGB LED with narrow peaks should have moderate to good fidelity
		assertTrue(tm30.Rf() > 80 && tm30.Rf() < 90, "RGB LED spectrum should have moderate Rf, got: " + tm30.Rf());

		// Gamut might be enlarged (oversaturation)
		assertTrue(tm30.Rg() > 90, "RGB LED might have enlarged gamut, got: " + tm30.Rg());

		// Some hue bins should show significant chroma shifts
		boolean hasExtremeChroma = false;
		for (float rc : tm30.Rcs()) {
			if (Math.abs(rc) > 0.3) { // More than 30% change in chroma
				hasExtremeChroma = true;
				break;
			}
		}
		assertTrue(hasExtremeChroma, "RGB LED should have some hue bins with extreme chroma shifts");
	}

	@Test
	public void testSpectrumLength () {
		// Test that incorrect spectrum length throws exception
		Assertions.assertThrows(IllegalArgumentException.class, () -> new Spectrum(new float[64], 5).TM30());
	}

	@Test
	public void testRanges () {
		// Test various light sources
		float[] cctValues = {2700, 3500, 4000, 5000, 6500};

		for (float K : cctValues) {
			Spectrum spectrum = new CCT(K).illuminant();
			TM30 tm30 = spectrum.TM30();

			// All metrics should be in valid ranges
			assertTrue(tm30.Rf() >= 0 && tm30.Rf() <= 100, "Rf should be in [0, 100], got: " + tm30.Rf());
			assertTrue(tm30.Rg() >= 0 && tm30.Rg() <= 200, "Rg should be in reasonable range, got: " + tm30.Rg());

			// Hue bin scores should be reasonable
			for (int i = 0; i < 16; i++) {
				assertTrue(tm30.Rcs()[i] >= -1 && tm30.Rcs()[i] <= 2,
					"chromaShift[" + i + "] should be in reasonable range, got: " + tm30.Rcs()[i]);
				assertTrue(tm30.Rhs()[i] >= -180 && tm30.Rhs()[i] <= 180,
					"hueShift[" + i + "] should be in reasonable range, got: " + tm30.Rhs()[i]);
			}

			// Individual color sample scores should be reasonable
			for (float sample : tm30.samples())
				assertTrue(sample >= -50 && sample <= 100, "Color sample score should be in reasonable range");
		}
	}

	@Test
	public void testMonochromaticLight () {
		// Test with near-monochromatic light
		float[] values = new float[81];
		// Narrow peak at 590nm (yellow)
		for (int i = 0; i < 81; i++) {
			float wavelength = 380 + i * 5;
			if (wavelength >= 585 && wavelength <= 595)
				values[i] = 1000;
			else
				values[i] = 0.1f;
		}
		Spectrum spectrum = new Spectrum(values);

		TM30 tm30 = spectrum.TM30();

		// Monochromatic light should have poor fidelity
		assertTrue(tm30.Rf() < 80, "Near-monochromatic light should have low Rf, got: " + tm30.Rf());

		// Gamut should be very distorted
		assertTrue(tm30.Rg() < 80 || tm30.Rg() > 120, "Monochromatic light should have distorted gamut, got: " + tm30.Rg());

		// Chroma shifts should be extreme (either very positive or very negative)
		int extremeBins = 0;
		for (float rc : tm30.Rcs()) {
			if (Math.abs(rc) > 0.5) extremeBins++; // More than 50% change
		}
		assertTrue(extremeBins >= 8, "Many hue bins should have extreme chroma shifts for monochromatic light");
	}

	@Test
	public void testComparisonWithCRI () {
		// Compare TM-30 and CRI for the same spectrum
		Spectrum spectrum = new CCT(4000).illuminant();
		CRI cri = spectrum.CRI();
		TM30 tm30 = spectrum.TM30();

		// For a Planckian radiator, both should give high scores
		assertTrue(cri.Ra() > 95, "CRI Ra should be high");
		assertTrue(tm30.Rf() > 95, "TM-30 Rf should be high");

		// Create a spectrum with good CRI but poor gamut
		float[] values = new float[81];
		System.arraycopy(spectrum.values(), 0, values, 0, 81);
		// Reduce red region
		for (int i = 50; i < 81; i++) {
			values[i] *= 0.7f;
		}
		Spectrum modifiedSpectrum = new Spectrum(values);

		CRI cri2 = modifiedSpectrum.CRI();
		TM30 tm302 = modifiedSpectrum.TM30();

		// Both metrics should decrease, but TM-30 might show more detail
		assertTrue(cri2.Ra() < cri.Ra(), "Modified spectrum should have lower CRI");
		assertTrue(tm302.Rf() < tm30.Rf(), "Modified spectrum should have lower Rf");
		assertTrue(tm302.Rg() < 100, "Modified spectrum should have reduced gamut");
	}
}
