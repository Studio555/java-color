
package com.esotericsoftware.color;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.space.CAM02;
import com.esotericsoftware.color.space.CAM02UCS;
import com.esotericsoftware.color.space.CAM16;
import com.esotericsoftware.color.space.CAM16UCS;
import com.esotericsoftware.color.space.CCT;
import com.esotericsoftware.color.space.CCT.Method;
import com.esotericsoftware.color.space.Lab;
import com.esotericsoftware.color.space.UVW;
import com.esotericsoftware.color.space.XYZ;
import com.esotericsoftware.color.space.uv;
import com.esotericsoftware.color.space.uv1960;
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

	/** @return [1000K+] or NaN out of range.
	 * @see uv#CCT(CCT.Method) */
	public CCT CCT (CCT.Method method) {
		return uv().CCT(method);
	}

	/** Uses {@link Method#RobertsonImproved}.
	 * @return [1000K+] or NaN out of range. */
	public CCT CCT () {
		return uv().CCT();
	}

	/** CIE 13.3-1995 with {@link CRI.Method#UVW}. Requires 380nm @ 5nm to [700..780+]nm. */
	public CRI CRI () {
		return CRI(CRI.Method.UVW);
	}

	/** CIE 13.3-1995. Requires 380nm @ 5nm to [700..780+]nm. */
	public CRI CRI (CRI.Method method) {
		checkVisibleRange();
		XYZ testXYZ = XYZ();
		CCT cct = testXYZ.CCT();
		if (cct.invalid()) throw new IllegalStateException("Cannot calculate CRI for spectrum with invalid CCT.");
		Spectrum reference = cct.reference();
		XYZ refXYZ = reference.XYZ();
		uv1960 testuv = null, refuv = null;
		CAM16.VC testVC = null, refVC = null;
		switch (method) {
		case CAM16UCS -> {
			refVC = CAM16.VC.with(refXYZ.scl(100 / refXYZ.Y()), 100, 20, 2, true);
			testVC = CAM16.VC.with(testXYZ.scl(100 / testXYZ.Y()), 100, 20, 2, true);
		}
		case UVW -> {
			testuv = testXYZ.uv1960();
			refuv = refXYZ.uv1960();
		}
		}
		float[] samples = new float[14];
		float sumRa = 0;
		for (int i = 0; i < 14; i++) {
			float[] tcs = CRI.TCS[i];
			XYZ test = illuminate(tcs).scl(100 / testXYZ.Y());
			XYZ ref = reference.illuminate(tcs).scl(100 / refXYZ.Y());
			float deltaE = switch (method) {
			case CAM16UCS -> test.CAM16UCS(testVC).dst(ref.CAM16UCS(refVC));
			case UVW -> {
				UVW testUVW = test.uv1960().chromaticAdaptation(testuv, refuv).UVW(test.Y(), refuv);
				UVW refUVW = ref.uv1960().UVW(ref.Y(), refuv);
				yield testUVW.dst(refUVW);
			}
			};
			samples[i] = 100 - 4.6f * deltaE;
			if (i < 8) sumRa += samples[i];
		}
		return new CRI(sumRa / 8, samples);
	}

	/** Uses {@link Observer#CIE2} D65. */
	public Lab Lab () {
		return XYZ().Lab();
	}

	public Lab Lab (XYZ whitePoint, Observer observer) {
		return XYZ(observer).Lab(whitePoint);
	}

	/** ANSI/IES TM-30-18 with {@link TM30.Method#CAM02UCS}. Requires 380nm @ 5nm to [700..780+]nm. */
	public TM30 TM30 () {
		return TM30(TM30.Method.CAM02UCS);
	}

	/** ANSI/IES TM-30-18. Requires 380nm @ 5nm to [700..780+]nm. */
	public TM30 TM30 (TM30.Method method) {
		checkVisibleRange();
		XYZ testXYZ = XYZ(Observer.CIE10);
		CCT cct = testXYZ.CCT();
		if (cct.invalid()) throw new IllegalStateException("Cannot calculate TM30 for spectrum with invalid CCT.");
		Spectrum reference = cct.reference();
		XYZ refXYZ = reference.XYZ(Observer.CIE10);
		XYZ refWP = refXYZ.scl(100 / refXYZ.Y());
		XYZ testWP = testXYZ.scl(100 / testXYZ.Y());
		float[] colorSamples = new float[99], chromaShift = new float[16], hueShift = new float[16];
		float[][] testBinSums = new float[16][2], refBinSums = new float[16][2];
		int[] binCounts = new int[16];
		float Rf = 0;
		switch (method) {
		case CAM02UCS -> {
			CAM02.VC refVC = CAM02.VC.with(refWP, 100, 20, 2, true);
			CAM02.VC testVC = CAM02.VC.with(testWP, 100, 20, 2, true);
			for (int i = 0; i < 99; i++) {
				float[] ces = TM30.CES[i];
				CAM02UCS testColor = illuminate(ces, Observer.CIE10).scl(100 / testXYZ.Y()).CAM02UCS(testVC);
				CAM02UCS refColor = reference.illuminate(ces, Observer.CIE10).scl(100 / refXYZ.Y()).CAM02UCS(refVC);
				float deltaE = testColor.dst(refColor);
				colorSamples[i] = deltaEtoRf(deltaE);
				Rf += deltaE;
				float hue = refColor.h();
				int bin = (int)((hue + 11.25f) / 22.5f) % 16;
				hueShift[bin] += angleDifference(refColor.h(), testColor.h());
				chromaShift[bin] += (testColor.C() - refColor.C()) / refColor.C();
				binCounts[bin]++;
				testBinSums[bin][0] += testColor.a();
				testBinSums[bin][1] += testColor.b();
				refBinSums[bin][0] += refColor.a();
				refBinSums[bin][1] += refColor.b();
			}
		}
		case CAM16UCS -> {
			CAM16.VC refVC = CAM16.VC.with(refWP, 100, 20, 2, true);
			CAM16.VC testVC = CAM16.VC.with(testWP, 100, 20, 2, true);
			for (int i = 0; i < 99; i++) {
				float[] ces = TM30.CES[i];
				CAM16UCS testColor = illuminate(ces, Observer.CIE10).scl(100 / testXYZ.Y()).CAM16UCS(testVC);
				CAM16UCS refColor = reference.illuminate(ces, Observer.CIE10).scl(100 / refXYZ.Y()).CAM16UCS(refVC);
				float deltaE = testColor.dst(refColor);
				colorSamples[i] = deltaEtoRf(deltaE);
				Rf += deltaE;
				float hue = (float)(Math.atan2(refColor.b(), refColor.a()) * radDeg);
				if (hue < 0) hue += 360;
				float testHue = (float)(Math.atan2(testColor.b(), testColor.a()) * radDeg);
				if (testHue < 0) testHue += 360;
				int bin = (int)((hue + 11.25f) / 22.5f) % 16;
				hueShift[bin] += angleDifference(hue, testHue);
				float refChroma = (float)Math.sqrt(refColor.a() * refColor.a() + refColor.b() * refColor.b());
				float testChroma = (float)Math.sqrt(testColor.a() * testColor.a() + testColor.b() * testColor.b());
				chromaShift[bin] += (testChroma - refChroma) / refChroma;
				binCounts[bin]++;
				testBinSums[bin][0] += testColor.a();
				testBinSums[bin][1] += testColor.b();
				refBinSums[bin][0] += refColor.a();
				refBinSums[bin][1] += refColor.b();
			}
		}
		}
		float[][] testAverages = new float[16][2], refAverages = new float[16][2];
		float[] hueAngleBins = new float[16];
		for (int i = 0; i < 16; i++) {
			if (binCounts[i] > 0) {
				hueShift[i] /= binCounts[i];
				chromaShift[i] /= binCounts[i];
				hueAngleBins[i] = deltaEtoRf(Math.abs(hueShift[i]));
				testAverages[i][0] = testBinSums[i][0] / binCounts[i];
				testAverages[i][1] = testBinSums[i][1] / binCounts[i];
				refAverages[i][0] = refBinSums[i][0] / binCounts[i];
				refAverages[i][1] = refBinSums[i][1] / binCounts[i];
			} else
				hueAngleBins[i] = 100;
		}
		float Rg = polygonArea(testAverages) / polygonArea(refAverages) * 100;
		return new TM30(deltaEtoRf(Rf / 99), Rg, chromaShift, hueShift, hueAngleBins, colorSamples);
	}

	static private float deltaEtoRf (float deltaE) {
		return 10 * (float)Math.log1p(Math.exp((100 - 6.73f * deltaE) / 10));
	}

	static private float polygonArea (float[][] vertices) {
		float area = 0;
		int n = vertices.length;
		for (int i = 0; i < n; i++) {
			float[] u = vertices[i];
			float[] v = vertices[(i + 1) % n];
			area += (u[0] * v[1] - u[1] * v[0]) / 2;
		}
		return Math.abs(area);
	}

	/** Uses {@link Observer#CIE2}.
	 * @return NaN if invalid. */
	public uv uv () {
		return XYZ().uv();
	}

	public uv uv (Observer observer) {
		return XYZ(observer).uv();
	}

	/** Uses {@link Observer#CIE2}.
	 * @return NaN if invalid. */
	public xy xy () {
		return XYZ().xy();
	}

	public xy xy (Observer observer) {
		return XYZ(observer).xy();
	}

	/** Uses {@link Observer#CIE2}. */
	public XYZ XYZ () {
		return XYZ(Observer.CIE2);
	}

	/** Requires 380nm @ 5nm to [700..780+]nm. Missing wavelengths are treated as zero.
	 * @return XYZ in relative colorimetric units (equal energy white gives Y=100). */
	public XYZ XYZ (Observer observer) {
		checkVisibleRange();
		float X = 0, Y = 0, Z = 0;
		for (int i = 0, n = Math.min(values.length, 81); i < n; i++) {
			float value = values[i];
			X += value * observer.xbar[i];
			Y += value * observer.ybar[i];
			Z += value * observer.zbar[i];
		}
		if (Y < EPSILON) return new XYZ(Float.NaN, Float.NaN, Float.NaN);
		float factor = 100 / observer.ybarIntegral * step;
		return new XYZ(X * factor, Y * factor, Z * factor);
	}

	/** Uses {@link Observer#CIE2}. Requires 380nm @ 5nm to [700..780+]nm. */
	public float Y () {
		return XYZ().Y();
	}

	/** Requires 380nm @ 5nm to [700..780+]nm. */
	public float Y (Observer observer) {
		return XYZ(observer).Y();
	}

	/** Total luminous flux (relative) weighted by the photopic luminosity function. Requires 380nm @ 5nm to [700..780+]nm. */
	public float luminousFlux (Observer observer) {
		checkVisibleRange();
		float flux = 0;
		for (int i = 0, n = Math.min(values.length, 81); i < n; i++)
			flux += values[i] * observer.ybar[i];
		return flux * 5;
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
	public float LER (Observer observer) {
		float radiant = radiantFlux();
		return radiant == 0 ? 0 : luminousFlux(observer) * XYZ.Km / radiant;
	}

	/** Requires 380nm @ 5nm to [700..780+]nm.
	 * @return Luminous efficacy of radiation as ratio [0..1], where 1 = 683 lm/W. */
	public float LERratio (Observer observer) {
		float radiant = radiantFlux();
		return radiant == 0 ? 0 : luminousFlux(observer) / radiant;
	}

	/** @see SpectralLocus#dominantWavelength(uv) */
	public float dominantWavelength () {
		return SpectralLocus.dominantWavelength(uv());
	}

	/** @see SpectralLocus#dominantWavelength(uv, XYZ) */
	public float dominantWavelength (XYZ whitePoint) {
		return SpectralLocus.dominantWavelength(uv(), whitePoint);
	}

	/** Normalizes spectrum so Y=100.
	 * @return NaN if Y=0. */
	public Spectrum normalize () {
		return scl(100 / Y());
	}

	/** Normalizes spectrum so the maximum value is 1.
	 * @return NaN if no value is > 0. */
	public Spectrum normalizeMax () {
		float max = 0;
		for (float v : values)
			max = Math.max(max, v);
		return scl(1 / max);
	}

	/** Uses {@link Observer#CIE2}. */
	public XYZ illuminate (float[] reflectance) {
		return illuminate(reflectance, Observer.CIE2);
	}

	/** Requires 380nm @ 5nm to [700..780+]nm.
	 * @param reflectance Must have at least as many entries as this spectrum.
	 * @return XYZ in relative colorimetric units (equal energy white gives Y=100) for the specified reflective sample illuminated
	 *         by this spectrum. */
	public XYZ illuminate (float[] reflectance, Observer observer) {
		checkVisibleRange();
		int n = Math.min(values.length, 81);
		if (reflectance.length < n) throw new IllegalArgumentException("reflectance requires as many entries as spectrum: " + n);
		float X = 0, Y = 0, Z = 0;
		for (int i = 0; i < n; i++) {
			float product = reflectance[i] * values[i];
			X += product * observer.xbar[i];
			Y += product * observer.ybar[i];
			Z += product * observer.zbar[i];
		}
		if (Y < EPSILON) return new XYZ(Float.NaN, Float.NaN, Float.NaN);
		float factor = 100 / observer.ybarIntegral * 5;
		return new XYZ(X * factor, Y * factor, Z * factor);
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
}
