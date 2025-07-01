
package com.esotericsoftware.color;

import static com.esotericsoftware.color.Util.*;

import java.util.Arrays;

import com.esotericsoftware.color.Illuminant.CIE2;
import com.esotericsoftware.color.space.CAM02UCS;
import com.esotericsoftware.color.space.CAM16UCS;
import com.esotericsoftware.color.space.CCT;
import com.esotericsoftware.color.space.Lab;
import com.esotericsoftware.color.space.XYZ;
import com.esotericsoftware.color.space.uv;
import com.esotericsoftware.color.space.xy;

/** @author Nathan Sweet <misc@n4te.com> */
public record Spectrum (float[] values, int step, int start) {
	public Spectrum {
		if (values.length == 0) throw new IllegalArgumentException("values must be > 0.");
		if (step <= 0) throw new IllegalArgumentException("step must be > 0: " + step);
		if (start < 0) throw new IllegalArgumentException("start must be >= 0: " + start);
	}

	/** Uses 380 {@link #start()}. */
	public Spectrum (float[] values, int step) {
		this(values, step, 380);
	}

	/** Uses 380 {@link #start()} and 5 {@link #step()}. */
	public Spectrum (float[] values) {
		this(values, 5, 380);
	}

	/** @return [1000..100000K] or NaN out of range.
	 * @see uv#CCT() */
	public CCT CCT () {
		return uv().CCT();
	}

	/** Requires 380nm @ 5nm to [700..780+]nm. */
	public CRI CRI () {
		checkVisibleRange();
		XYZ testXYZ = XYZ();
		CCT cct = testXYZ.uv().CCT();
		Spectrum reference = (cct.invalid() ? new CCT(6500) : cct).illuminant();
		XYZ refXYZ = reference.XYZ();
		float[] samples = new float[14];
		float sumRa = 0;
		for (int i = 0; i < 14; i++) {
			float[] tcs = CRI.TCS[i];
			Lab testLab = illuminate(tcs).chromaticAdaptation(testXYZ, refXYZ).Lab(refXYZ);
			Lab refLab = reference.illuminate(tcs).Lab(refXYZ);
			samples[i] = 100 - 4.6f * testLab.dst(refLab);
			if (i < 8) sumRa += samples[i];
		}
		return new CRI(sumRa / 8, samples);
	}

	/** Uses {@link CIE2#D65}. */
	public Lab Lab () {
		return XYZ().Lab();
	}

	public Lab Lab (XYZ whitePoint) {
		return XYZ().Lab(whitePoint);
	}

	/** Requires 380nm @ 5nm to [700..780+]nm. Uses CAM16-UCS. */
	public TM30 TM30 () {
		return TM30(false);
	}

	/** Requires 380nm @ 5nm to [700..780+]nm.
	 * @param useCAM02 If true, uses CAM02-UCS; if false, uses CAM16-UCS. */
	public TM30 TM30 (boolean useCAM02) {
		checkVisibleRange();
		XYZ testXYZ = XYZ();
		CCT cct = testXYZ.uv().CCT();
		Spectrum reference = (cct.invalid() ? new CCT(6500) : cct).illuminant();
		float sum = 0;
		float[] colorSamples = new float[99];
		float[] chromaShift = new float[16];
		float[] hueShift = new float[16];
		int[] binCounts = new int[16];
		float chromaRef = 0;
		float chromaTest = 0;
		if (useCAM02) {
			CAM02UCS[] testColors = new CAM02UCS[99], refColors = new CAM02UCS[99];
			for (int i = 0; i < 99; i++) {
				float[] reflectance = TM30.CES[i];
				testColors[i] = illuminate(reflectance).CAM02UCS();
				refColors[i] = reference.illuminate(reflectance).CAM02UCS();
			}
			for (int i = 0; i < 99; i++) {
				float deltaE = testColors[i].dst(refColors[i]);
				colorSamples[i] = 100 - 7.18f * deltaE;
				sum += deltaE;
			}
			for (int i = 0; i < 99; i++) {
				float hue = refColors[i].h(), refChroma = refColors[i].C();
				int bin = (int)((hue + 11.25f) / 22.5f) % 16;
				hueShift[bin] += angleDifference(refColors[i].h(), testColors[i].h());
				chromaShift[bin] += (testColors[i].C() - refChroma) / refChroma;
				binCounts[bin]++;
				chromaRef += refChroma;
				chromaTest += testColors[i].C();
			}
		} else {
			CAM16UCS[] testColors = new CAM16UCS[99], refColors = new CAM16UCS[99];
			for (int i = 0; i < 99; i++) {
				float[] reflectance = TM30.CES[i];
				testColors[i] = illuminate(reflectance).CAM16().CAM16UCS();
				refColors[i] = reference.illuminate(reflectance).CAM16().CAM16UCS();
			}
			for (int i = 0; i < 99; i++) {
				float deltaE = testColors[i].deltaE(refColors[i]);
				colorSamples[i] = 100 - 7.18f * deltaE;
				sum += deltaE;
			}
			for (int i = 0; i < 99; i++) {
				float hue = refColors[i].h(), refChroma = refColors[i].C();
				int bin = (int)((hue + 11.25f) / 22.5f) % 16;
				hueShift[bin] += angleDifference(refColors[i].h(), testColors[i].h());
				chromaShift[bin] += (testColors[i].C() - refChroma) / refChroma;
				binCounts[bin]++;
				chromaRef += refChroma;
				chromaTest += testColors[i].C();
			}
		}
		float[] hueAngleBins = new float[16];
		for (int i = 0; i < 16; i++) {
			if (binCounts[i] > 0) {
				hueShift[i] /= binCounts[i];
				chromaShift[i] /= binCounts[i];
				hueAngleBins[i] = 100 - 7.18f * Math.abs(hueShift[i]);
			} else
				hueAngleBins[i] = 100;
		}
		return new TM30(100 - 7.18f * (sum / 99), 100 * (chromaTest / chromaRef), chromaShift, hueShift, hueAngleBins,
			colorSamples);
	}

	/** @return NaN if invalid. */
	public uv uv () {
		return XYZ().uv();
	}

	/** @return NaN if invalid. */
	public xy xy () {
		return XYZ().xy();
	}

	/** Requires 380nm @ 5nm to [700..780+]nm. Missing wavelengths are treated as zero. */
	public XYZ XYZ () {
		checkVisibleRange();
		float X = 0, Y = 0, Z = 0;
		for (int i = 0, n = Math.min(values.length, 81); i < n; i++) {
			X += values[i] * XYZ.Xbar[i];
			Y += values[i] * XYZ.Ybar[i];
			Z += values[i] * XYZ.Zbar[i];
		}
		float normalize = XYZ.Km * 5 / 100;
		return new XYZ(X * normalize, Y * normalize, Z * normalize);
	}

	/** Requires 380nm @ 5nm to [700..780+]nm. */
	public float Y () {
		return luminousFlux() / 100;
	}

	/** Total luminous flux (relative) weighted by the photopic luminosity function. Requires 380nm @ 5nm to [700..780+]nm. */
	public float luminousFlux () {
		checkVisibleRange();
		float flux = 0;
		for (int i = 0, n = Math.min(values.length, 81); i < n; i++)
			flux += values[i] * XYZ.Ybar[i];
		return flux * XYZ.Km * 5;
	}

	/** Calculates total radiant flux (relative). */
	public float radiantFlux () {
		float flux = 0;
		for (float value : values)
			flux += value;
		return flux * step;
	}

	/** Requires 380nm @ 5nm to [700..780+]nm.
	 * @return Luminous efficacy of radiation, maximum 683 lm/W. */
	public float LER () {
		float radiant = radiantFlux();
		return radiant == 0 ? 0 : luminousFlux() / radiant;
	}

	/** @see SpectralLocus#dominantWavelength(uv) */
	public float dominantWavelength () {
		return SpectralLocus.dominantWavelength(uv());
	}

	/** @see SpectralLocus#dominantWavelength(uv, XYZ) */
	public float dominantWavelength (XYZ whitePoint) {
		return SpectralLocus.dominantWavelength(uv(), whitePoint);
	}

	/** Normalizes spectrum so Y = 100. */
	public Spectrum normalize () {
		float Y = Y();
		if (Y == 0) return this;
		return scl(100 / Y);
	}

	/** Normalizes spectrum so maximum value = 1. */
	public Spectrum normalizeMax () {
		float max = 0;
		for (float v : values)
			max = Math.max(max, v);
		if (max == 0) return this;
		return scl(1f / max);
	}

	/** Requires 380nm @ 5nm to [700..780+]nm.
	 * @param reflectance Must have 81 or at least as many entries as this spectrum.
	 * @return XYZ for the specified reflective sample illuminated by this spectrum. */
	public XYZ illuminate (float[] reflectance) {
		checkVisibleRange();
		int n = Math.min(values.length, 81);
		if (reflectance.length < n) throw new IllegalArgumentException("reflectance requires as many entries as spectrum: " + n);
		float X = 0, Y = 0, Z = 0;
		for (int i = 0; i < n; i++) {
			float product = reflectance[i] * values[i];
			X += product * XYZ.Xbar[i];
			Y += product * XYZ.Ybar[i];
			Z += product * XYZ.Zbar[i];
		}
		float normalize = XYZ.Km * 5 / 100;
		return new XYZ(X * normalize, Y * normalize, Z * normalize);
	}

	/** Resamples to new wavelength range and interval using linear interpolation and zero padding. */
	public Spectrum resample (int start, int end, int step) {
		if (step <= 0) throw new IllegalArgumentException("step must be positive: " + step);
		int length = ((end - start) / step) + 1;
		float[] values = new float[length];
		for (int i = 0; i < length; i++)
			values[i] = interpolate(start + i * step);
		return new Spectrum(values, step, start);
	}

	public Spectrum scl (float scalar) {
		float[] newValues = new float[values.length];
		for (int i = 0; i < values.length; i++)
			newValues[i] = values[i] * scalar;
		return new Spectrum(newValues, step, start);
	}

	/** Spectra must have same range and step. */
	public Spectrum scl (Spectrum other) {
		checkSame(other);
		float[] newValues = new float[values.length];
		for (int i = 0; i < values.length; i++)
			newValues[i] = values[i] * other.values[i];
		return new Spectrum(newValues, step, start);
	}

	/** Spectra must have same range and step. */
	public Spectrum add (Spectrum other) {
		checkSame(other);
		float[] newValues = new float[values.length];
		for (int i = 0; i < values.length; i++)
			newValues[i] = values[i] + other.values[i];
		return new Spectrum(newValues, step, start);
	}

	/** Spectra must have same range and step. */
	public Spectrum lerp (Spectrum other, float t) {
		checkSame(other);
		float[] newValues = new float[values.length];
		for (int i = 0; i < values.length; i++)
			newValues[i] = Util.lerp(values[i], other.values[i], t);
		return new Spectrum(newValues, step, start);
	}

	/** Throw if spectrum is not 380nm @ 5nm to [700..780+]nm. */
	private void checkVisibleRange () {
		if (values.length < 65) throw new IllegalArgumentException("Spectrum must extend to at least 700nm, ends at: " + end());
		if (start != 380) throw new IllegalArgumentException("start must be 380: " + start);
		if (step != 5) throw new IllegalArgumentException("step must be 5: " + step);
	}

	private void checkSame (Spectrum other) {
		if (start != other.start) throw new IllegalArgumentException("other must have same start: " + start + " != " + other.start);
		if (step != other.step) throw new IllegalArgumentException("other must have same step: " + step + " != " + other.step);
		if (values.length != other.values.length) throw new IllegalArgumentException(
			"other must have same values length: " + values.length + " != " + other.values.length);
	}

	/** Applies smoothing using a moving average window. */
	public Spectrum smooth (int windowSize) {
		if (windowSize <= 0 || windowSize % 2 == 0)
			throw new IllegalArgumentException("windowSize must be a positive odd number: " + windowSize);
		float[] newValues = new float[values.length];
		int halfWindow = windowSize / 2;
		for (int i = 0; i < values.length; i++) {
			float sum = 0;
			int count = 0;
			for (int j = Math.max(0, i - halfWindow); j <= Math.min(values.length - 1, i + halfWindow); j++) {
				sum += values[j];
				count++;
			}
			newValues[i] = sum / count;
		}
		return new Spectrum(newValues, step, start);
	}

	/** Clamps negative values to zero. */
	public Spectrum clampNegative () {
		float[] newValues = new float[values.length];
		for (int i = 0; i < values.length; i++)
			newValues[i] = Math.max(0, values[i]);
		return new Spectrum(newValues, step, start);
	}

	/** Gets interpolated value at the specified wavelength. Returns 0 outside of range. */
	public float interpolate (float wavelength) {
		if (wavelength < start || wavelength > end()) return 0;
		float position = (wavelength - start) / step;
		int index = (int)position;
		float fraction = position - index;
		if (index >= values.length - 1) return values[values.length - 1];
		if (fraction == 0) return values[index];
		return Util.lerp(values[index], values[index + 1], fraction);
	}

	public int end () {
		return start + (values.length - 1) * step;
	}

	public boolean invalid () {
		for (float value : values)
			if (!Float.isFinite(value)) return true;
		return false;
	}

	public boolean hasNegative () {
		for (float v : values)
			if (v < 0) return true;
		return false;
	}

	public int wavelength (int index) {
		if (index < 0 || index >= values.length)
			throw new IndexOutOfBoundsException("Index: " + index + ", length: " + values.length);
		return start + index * step;
	}

	public float value (int wavelength) {
		if ((wavelength - start) % step != 0) throw new IllegalArgumentException("Wavelength not in spectrum: " + wavelength);
		int index = (wavelength - start) / step;
		if (index < 0 || index >= values.length) throw new IllegalArgumentException("Wavelength out of range: " + wavelength);
		return values[index];
	}

	/** Returns an equal energy spectrum (all values 1, illuminantE). */
	static public Spectrum equalEnergy (int start, int end, int step) {
		float[] values = new float[((end - start) / step) + 1];
		Arrays.fill(values, 1);
		return new Spectrum(values, step, start);
	}
}
