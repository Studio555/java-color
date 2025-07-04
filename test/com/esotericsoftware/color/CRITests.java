
package com.esotericsoftware.color;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.color.space.CCT;
import com.esotericsoftware.color.space.LMS;
import com.esotericsoftware.color.space.XYZ;

public class CRITests extends Tests {
	@Test
	public void testPlanckianRadiator () {
		// Test with 2700K Planckian radiator
		CRI cri = new CCT(2700).reference().CRI();

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
		CRI cri = new CCT(6500).reference().CRI();

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
		Spectrum spectrum = new Spectrum(values, 5);
		CRI cri = spectrum.CRI();
		// System.out.println(cri.Ra());
		// for (int i = 0; i < 14; i++)
		// System.out.println("TCS " + (i + 1) + ": " + cri.samples()[i]);
		assertEquals(46.977493f, cri.Ra(), "Wrong Ra");

		cri = spectrum.CRI(CRI.Method.CAM16UCS);
		assertEquals(47.474117f, cri.Ra(), "Wrong Ra");

		for (int i = 0; i < 12; i++)
			values[i] = 66;
		spectrum = new Spectrum(values, 5);
		cri = spectrum.CRI();
		assertEquals(37.610584f, cri.Ra(), "Wrong Ra");

		cri = spectrum.CRI(CRI.Method.CAM16UCS);
		assertEquals(40.88963f, cri.Ra(), "Wrong Ra");
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
			CRI cri = new CCT(K).reference().CRI();

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

		// Monochromatic light should have reduced CRI
		assertTrue(cri.Ra() < -40, "Monochromatic light should have reduced CRI, got: " + cri.Ra());

		// At least one sample should score below 85
		boolean hasLowerScore = false;
		for (int i = 0; i < 8; i++) {
			if (cri.samples()[i] < -75) {
				hasLowerScore = true;
				break;
			}
		}
		assertTrue(hasLowerScore, "Monochromatic light should have at least one sample below 85");
	}
}
