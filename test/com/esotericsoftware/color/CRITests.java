
package com.esotericsoftware.color;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.CCT;
import com.esotericsoftware.color.space.XYZ;

public class CRITests extends Tests {
	@Test
	public void testPlanckianRadiator () {
		// Test with 2700K Planckian radiator
		CRI cri = new CCT(2700).illuminant().CRI();

		// Planckian radiator at 2700K should have CRI ~100
		assertTrue(cri.Ra() > 95 && cri.Ra() <= 100, "Planckian radiator at 2700K should have very high CRI, got: " + cri.Ra());

		// All individual samples should be high
		for (int i = 0; i < 8; i++) {
			assertTrue(cri.samples()[i] > 90, "Planckian radiator sample " + (i + 1) + " should be > 90, got: " + cri.samples()[i]);
		}
	}

	@Test
	public void testDaylight () {
		// Test with 6500K daylight
		CRI cri = new CCT(6500).illuminant().CRI();

		// CIE daylight at 6500K should have CRI ~100
		assertTrue(cri.Ra() > 95 && cri.Ra() <= 100, "CIE daylight at 6500K should have very high CRI, got: " + cri.Ra());

		// All individual samples should be high
		for (int i = 0; i < 8; i++) {
			assertTrue(cri.samples()[i] > 90, "Daylight sample " + (i + 1) + " should be > 90, got: " + cri.samples()[i]);
		}
	}

	@Test
	public void testNarrowbandSpectrum () {
		// Create a narrowband spectrum (simulating LED)
		float[] values = new float[81];
		// Add peaks at blue and yellow wavelengths
		for (int i = 0; i < 81; i++) {
			float wavelength = 380 + i * 5;
			// Blue peak around 450nm
			if (wavelength >= 440 && wavelength <= 460)
				values[i] = 100;
			// Yellow peak around 570nm
			else if (wavelength >= 560 && wavelength <= 580)
				values[i] = 150;
			else
				values[i] = 10; // Low baseline
		}
		Spectrum spectrum = new Spectrum(values, 5);

		// Normalize to reasonable values
		XYZ xyz = spectrum.XYZ();
		float scale = 100f / xyz.Y();
		for (int i = 0; i < 81; i++)
			values[i] *= scale;

		CRI cri = spectrum.CRI();

		// Narrowband spectrum should have lower CRI
		assertTrue(cri.Ra() < 90, "Narrowband spectrum should have lower CRI, got: " + cri.Ra());

		// Some samples (especially reds) should score poorly
		boolean hasLowScore = false;
		for (int i = 0; i < 8; i++) {
			if (cri.samples()[i] < 50) {
				hasLowScore = true;
				break;
			}
		}
		assertTrue(hasLowScore, "Narrowband spectrum should have at least one low-scoring sample");
	}

	@Test
	public void testSpectrumLength () {
		// Test that incorrect spectrum length throws exception
		Assertions.assertThrows(IllegalArgumentException.class, () -> new Spectrum(new float[64]).CRI());
	}

	@Test
	public void testRanges () {
		// Test various CCTs
		float[] cctValues = {2000, 2700, 3000, 4000, 5000, 6500, 10000};

		for (float K : cctValues) {
			CRI cri = new CCT(K).illuminant().CRI();

			// Reference illuminants should have high CRI
			assertTrue(cri.Ra() > 90, "CCT " + K + "K should have high CRI, got: " + cri.Ra());

			// Individual samples should be between 0 and 100
			for (int i = 0; i < 14; i++) {
				float sample = cri.samples()[i];
				assertTrue(sample >= -100 && sample <= 100, "Sample " + (i + 1) + " should be in [-100, 100], got: " + sample);
			}
		}
	}

	@Test
	public void testMonochromaticLight () {
		// Test with monochromatic light (very poor CRI)
		float[] values = new float[81];
		// Single wavelength at 550nm (green)
		int index550 = (550 - 380) / 5;
		values[index550] = 1000;

		CRI cri = new Spectrum(values).CRI();

		// Monochromatic light should have poor CRI
		assertTrue(cri.Ra() < 45, "Monochromatic light should have low CRI, got: " + cri.Ra());

		// Some samples should score very poorly
		int lowScoreCount = 0;
		int veryLowScoreCount = 0;
		for (int i = 0; i < 8; i++) {
			if (cri.samples()[i] < 0) lowScoreCount++;
			if (cri.samples()[i] < 50) veryLowScoreCount++;
		}
		assertTrue(lowScoreCount >= 2 || veryLowScoreCount >= 4,
			"Monochromatic light should have several samples with poor scores");
	}
}
