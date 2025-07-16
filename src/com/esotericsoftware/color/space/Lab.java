
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Illuminant;
import com.esotericsoftware.color.Observer;
import com.esotericsoftware.color.Util;

/** CIELAB perceptually uniform color space. */
public record Lab (
	/** Lightness (L*) [0..100]. */
	float L,
	/** Red-green axis (a*) [-100..100]. */
	float a,
	/** Yellow-blue axis (b*) [-100..100]. */
	float b) implements Color {

	static public final float k = 24389 / 27f;
	static public final float e = 216 / 24389f;

	public float get (int index) {
		return switch (index) {
		case 0 -> L;
		case 1 -> a;
		case 2 -> b;
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public Lab set (int index, float value) {
		return switch (index) {
		case 0 -> new Lab(value, a, b);
		case 1 -> new Lab(L, value, b);
		case 2 -> new Lab(L, a, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	/** @param whitePoint See {@link Illuminant}. */
	public LinearRGB LinearRGB (XYZ whitePoint) {
		return XYZ(whitePoint).LinearRGB();
	}

	public LCh LCh () {
		float C = (float)Math.sqrt(a * a + b * b);
		float h = C < EPSILON ? Float.NaN : (float)Math.atan2(b, a) * radDeg;
		if (h < 0) h += 360;
		return new LCh(L, C, h);
	}

	/** @param whitePoint See {@link Illuminant}. */
	public RGB RGB (XYZ whitePoint) {
		return XYZ(whitePoint).RGB();
	}

	/** Uses {@link Observer#Default} D65. */
	public XYZ XYZ () {
		return XYZ(Observer.Default.D65);
	}

	/** @param whitePoint See {@link Illuminant}. */
	public XYZ XYZ (XYZ whitePoint) {
		float Y = (L + 16) / 116;
		float X = a / 500 + Y;
		float Z = Y - b / 200;
		float X3 = X * X * X;
		X = X3 > Lab.e ? X3 : (116 * X - 16) / Lab.k;
		Y = Lab.LstarToYn(L);
		float Z3 = Z * Z * Z;
		Z = Z3 > Lab.e ? Z3 : (116 * Z - 16) / Lab.k;
		return new XYZ(X * whitePoint.X(), Y * whitePoint.Y(), Z * whitePoint.Z());
	}

	/** Uses {@link Observer#Default} D65. */
	public float Y () {
		return Y(Observer.Default.D65);
	}

	public float Y (XYZ whitePoint) {
		return (L + 16) / 116 * whitePoint.Y();
	}

	/** CIEDE2000 color difference, considering lightness, chromaticity, and hue.
	 * @param kL Lightness scaling factor. The lightness component is divided by this value (>1 less impact).
	 * @param kC Chroma scaling factor. The chroma component is divided by this value (>1 less impact).
	 * @param kH Hue scaling factor. The hue component is divided by this value (>1 less impact).
	 * @return <1: imperceptible to the human eye, 1..2 just noticeable difference (JND), 2..10 clearly visible difference, >50
	 *         very different colors. */
	public float deltaE2000 (Lab other, float kL, float kC, float kH) {
		float L1 = L(), a1 = a(), b1 = b();
		float L2 = other.L(), a2 = other.a(), b2 = other.b();
		float C1 = (float)Math.sqrt(a1 * a1 + b1 * b1), C2 = (float)Math.sqrt(a2 * a2 + b2 * b2); // Chroma.
		float Cab = (C1 + C2) / 2, Cab7 = (float)Math.pow(Cab, 7), G = 0.5f * (1 - (float)Math.sqrt(Cab7 / (Cab7 + 6103515625d)));
		float a1p = (1 + G) * a1, a2p = (1 + G) * a2;
		float C1p = (float)Math.sqrt(a1p * a1p + b1 * b1), C2p = (float)Math.sqrt(a2p * a2p + b2 * b2);
		float h1p = (float)Math.atan2(b1, a1p) * radDeg, h2p = (float)Math.atan2(b2, a2p) * radDeg; // Hue angle.
		if (h1p < 0) h1p += 360;
		if (h2p < 0) h2p += 360;
		float dLp = L2 - L1, dCp = C2p - C1p, dhp = h2p - h1p; // Delta L'C'h'
		if (dhp > 180)
			dhp -= 360;
		else if (dhp < -180) //
			dhp += 360;
		float dHp = 2 * (float)Math.sqrt(C1p * C2p) * (float)Math.sin(dhp * degRad / 2);
		float Lp = (L1 + L2) / 2, Cp = (C1p + C2p) / 2, hp = h1p + h2p; // Average.
		if (Math.abs(h1p - h2p) > 180) hp += hp < 360 ? 360 : -360;
		hp /= 2;
		float hpRad = hp * degRad;
		float T = 1 - 0.17f * (float)Math.cos(hpRad - 30 * degRad) + 0.24f * (float)Math.cos(2 * hpRad)
			+ 0.32f * (float)Math.cos(3 * hpRad + 6 * degRad) - 0.2f * (float)Math.cos(4 * hpRad - 63 * degRad);
		float SL = 1 + 0.015f * (Lp - 50) * (Lp - 50) / (float)Math.sqrt(20 + (Lp - 50) * (Lp - 50));
		float SC = 1 + 0.045f * Cp;
		float SH = 1 + 0.015f * Cp * T;
		float dTheta = 30 * (float)Math.exp(-((hp - 275) / 25) * ((hp - 275) / 25));
		float Cp7 = (float)Math.pow(Cp, 7), RC = 2 * (float)Math.sqrt(Cp7 / (Cp7 + 6103515625d)); // 25^7
		float RT = -RC * (float)Math.sin(2 * dTheta * degRad);
		float dLpKlSl = kL == 0 ? 0 : dLp / (kL * SL);
		float dCpKcSc = kC == 0 ? 0 : dCp / (kC * SC);
		float dHpKhSh = kH == 0 ? 0 : dHp / (kH * SH);
		float deltaE2 = dLpKlSl * dLpKlSl + dCpKcSc * dCpKcSc + dHpKhSh * dHpKhSh + RT * dCpKcSc * dHpKhSh;
		return deltaE2 == 0 ? 0 : (float)Math.sqrt(deltaE2);
	}

	/** {@link #deltaE2000(Lab, float, float, float)} with 1 for lightness, chroma, and hue. */
	public float deltaE2000 (Lab other) {
		return deltaE2000(other, 1, 1, 1);
	}

	public Lab add (float value) {
		return new Lab(L + value, a + value, b + value);
	}

	public Lab add (int index, float value) {
		return switch (index) {
		case 0 -> new Lab(L + value, a, b);
		case 1 -> new Lab(L, a + value, b);
		case 2 -> new Lab(L, a, b + value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public Lab add (float L, float a, float b) {
		return new Lab(this.L + L, this.a + a, this.b + b);
	}

	public Lab lerp (Lab other, float t) {
		return new Lab(Util.lerp(L, other.L, t), Util.lerp(a, other.a, t), Util.lerp(b, other.b, t));
	}

	public Lab sub (float value) {
		return new Lab(L - value, a - value, b - value);
	}

	public Lab sub (int index, float value) {
		return switch (index) {
		case 0 -> new Lab(L - value, a, b);
		case 1 -> new Lab(L, a - value, b);
		case 2 -> new Lab(L, a, b - value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public Lab sub (float L, float a, float b) {
		return new Lab(this.L - L, this.a - a, this.b - b);
	}

	/** CIE76 color difference (Euclidian distance in Lab space). */
	public float dst (Lab other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (Lab other) {
		float dL = L - other.L, da = a - other.a, db = b - other.b;
		return dL * dL + da * da + db * db;
	}

	public float len () {
		return (float)Math.sqrt(len2());
	}

	public float len2 () {
		return L * L + a * a + b * b;
	}

	public Lab withL (float L) {
		return new Lab(L, a, b);
	}

	@SuppressWarnings("all")
	public Lab Lab () {
		return this;
	}

	/** @return [0..100] */
	static public float LstarToY (float Lstar) {
		float ft = (Lstar + 16) / 116;
		float ft3 = ft * ft * ft;
		return (ft3 > e ? ft3 : (116 * ft - 16) / k) * 100;
	}

	/** @return [0..1] */
	static public float LstarToYn (float Lstar) {
		float ft = (Lstar + 16) / 116;
		float ft3 = ft * ft * ft;
		return ft3 > e ? ft3 : (116 * ft - 16) / k;
	}

	/** L* is perceptual luminance (a linear scale) while Y in {@link XYZ} is relative luminance (a logarithmic scale).
	 * @return [0..100] */
	static public float YtoLstar (float Y) {
		float y = Y / 100;
		return y > e ? 116 * (float)Math.pow(y, 1 / 3f) - 16 : k * y;
	}
}
