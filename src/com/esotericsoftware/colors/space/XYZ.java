
package com.esotericsoftware.colors.space;

import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.Illuminant;
import com.esotericsoftware.colors.Illuminant.CIE2;
import com.esotericsoftware.colors.Util;
import com.esotericsoftware.colors.space.LMS.CAT;

/** CIE 1931 tristimulus values. Foundation of colorimetry. */
public record XYZ (
	/** X tristimulus [0+]. */
	float X,
	/** Y tristimulus (luminance) [0+]. */
	float Y,
	/** Z tristimulus [0+]. */
	float Z) {

	/** Planck constant. **/
	static public float h = 6.62607015e-34f;
	/** Speed of light in m/s. **/
	static public float c = 299792458f;
	/** Boltzmann constant. **/
	static public float k = 1.380649e-23f;
	/** First radiation constant. **/
	static public float c1 = 2f * (float)Math.PI * h * c * c;
	/** Second radiation constant. **/
	static public float c2 = h * c / k;

	public float get (int index) {
		return switch (index) {
		case 0 -> X;
		case 1 -> Y;
		case 2 -> Z;
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public XYZ set (int index, float value) {
		return switch (index) {
		case 0 -> new XYZ(value, Y, Z);
		case 1 -> new XYZ(X, value, Z);
		case 2 -> new XYZ(X, Y, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public CAM16 CAM16 () {
		return CAM16(CAM16.VC.sRGB);
	}

	public CAM16 CAM16 (CAM16.VC vc) { // Based on Copyright 2021 Google LLC (Apache 2.0).
		float rT = (X * 0.401288f) + (Y * 0.650173f) + (Z * -0.051461f); // To cone/RGB responses.
		float gT = (X * -0.250268f) + (Y * 1.204414f) + (Z * 0.045854f);
		float bT = (X * -0.002079f) + (Y * 0.048952f) + (Z * 0.953127f);
		float rD = vc.rgbD()[0] * rT; // Discount illuminant.
		float gD = vc.rgbD()[1] * gT;
		float bD = vc.rgbD()[2] * bT;
		float rAF = (float)Math.pow(vc.FL() * Math.abs(rD) / 100, 0.42); // Chromatic adaptation.
		float gAF = (float)Math.pow(vc.FL() * Math.abs(gD) / 100, 0.42);
		float bAF = (float)Math.pow(vc.FL() * Math.abs(bD) / 100, 0.42);
		float rA = Math.signum(rD) * 400 * rAF / (rAF + 27.13f);
		float gA = Math.signum(gD) * 400 * gAF / (gAF + 27.13f);
		float bA = Math.signum(bD) * 400 * bAF / (bAF + 27.13f);
		float a = (11 * rA + -12 * gA + bA) / 11; // Redness-greenness.
		float b = (rA + gA - 2 * bA) / 9; // Yellowness-blueness.
		float u = (20 * rA + 20 * gA + 21 * bA) / 20; // Auxiliary components.
		float p2 = (40 * rA + 20 * gA + bA) / 20;
		float hDeg = (float)Math.atan2(b, a) * radDeg; // Hue.
		float h = hDeg < 0 ? hDeg + 360 : hDeg >= 360 ? hDeg - 360 : hDeg;
		float ac = p2 * vc.Nbb(); // Achromatic response to color.
		float J = 100 * (float)Math.pow(ac / vc.Aw(), vc.c() * vc.z()); // CAM16 lightness and brightness.
		float huePrime = (h < 20.14f) ? h + 360 : h; // CAM16 chroma, colorfulness, and saturation.
		float eHue = 0.25f * ((float)Math.cos(huePrime * degRad + 2) + 3.8f);
		float p1 = 50000 / 13f * eHue * vc.Nc() * vc.Ncb();
		float t = p1 * (float)Math.hypot(a, b) / (u + 0.305f);
		float alpha = (float)Math.pow(1.64 - Math.pow(0.29, vc.n()), 0.73) * (float)Math.pow(t, 0.9);
		float C = alpha * (float)Math.sqrt(J / 100); // CAM16 chroma, colorfulness, saturation.
		return new CAM16(J, C, h, 4 / vc.c() * (float)Math.sqrt(J / 100) * (vc.Aw() + 4) * vc.FLRoot(), C * vc.FLRoot(),
			50 * (float)Math.sqrt((alpha * vc.c()) / (vc.Aw() + 4)));
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public CAM16UCS CAM16UCS () {
		return CAM16().CAM16UCS();
	}

	public CAM16UCS CAM16UCS (CAM16.VC vc) {
		return CAM16(vc).CAM16UCS();
	}

	/** @return NaN if invalid. */
	public HunterLab HunterLab () {
		if (Y < EPSILON) return new HunterLab(0, 0, 0);
		float sqrt = (float)Math.sqrt(Y);
		return new HunterLab(10 * sqrt, 17.5f * ((1.02f * X - Y) / sqrt), 7 * ((Y - 0.847f * Z) / sqrt));
	}

	/** Uses {@link CIE2#D65}. */
	public Lab Lab () {
		return Lab(CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	public Lab Lab (XYZ tristimulus) {
		float X = this.X / tristimulus.X, Y = this.Y / tristimulus.Y, Z = this.Z / tristimulus.Z;
		X = X > Lab.e ? (float)Math.pow(X, 1 / 3d) : (Lab.k * X + 16) / 116;
		Y = Y > Lab.e ? (float)Math.pow(Y, 1 / 3d) : (Lab.k * Y + 16) / 116;
		Z = Z > Lab.e ? (float)Math.pow(Z, 1 / 3d) : (Lab.k * Z + 16) / 116;
		return new Lab(116 * Y - 16, 500 * (X - Y), 200 * (Y - Z));
	}

	/** Uses {@link CIE2#D65}.
	 * @return NaN if invalid. */
	public Luv Luv () {
		return Luv(CIE2.D65);
	}

	/** @return NaN if invalid. */
	public Luv Luv (XYZ tristimulus) {
		float Xn = tristimulus.X, Yn = tristimulus.Y, Zn = tristimulus.Z;
		float yr = Y / Yn, L = yr > Lab.e ? 116 * (float)Math.cbrt(yr) - 16 : Lab.k * yr;
		float divisor = X + 15 * Y + 3 * Z, divisorN = Xn + 15 * Yn + 3 * Zn;
		if (divisor < EPSILON || divisorN < EPSILON) return new Luv(L, Float.NaN, Float.NaN);
		float u_prime = 4 * X / divisor, v_prime = 9 * Y / divisor;
		float un_prime = 4 * Xn / divisorN, vn_prime = 9 * Yn / divisorN;
		return new Luv(L, 13 * L * (u_prime - un_prime), 13 * L * (v_prime - vn_prime));
	}

	public LinearRGB LinearRGB () {
		float X = this.X / 100, Y = this.Y / 100, Z = this.Z / 100;
		return new LinearRGB( //
			3.2404542f * X - 1.5371385f * Y - 0.4985314f * Z, //
			-0.9692660f * X + 1.8760108f * Y + 0.0415560f * Z, //
			0.0556434f * X - 0.2040259f * Y + 1.0572252f * Z);
	}

	/** Uses the LMS CIECAM02 transformation matrix. */
	public LMS LMS () {
		return LMS(CAT.CAT02);
	}

	public LMS LMS (CAT matrix) {
		float[] lms = matrixMultiply(X, Y, Z, switch (matrix) {
		case HPE -> CAT.HPE_forward;
		case Bradford -> CAT.Bradford_forward;
		case VonKries -> CAT.vonKries_forward;
		case CAT97 -> CAT.CAT97_forward;
		default -> CAT.CAT02_forward;
		});
		return new LMS(lms[0], lms[1], lms[2]);
	}

	public Oklab Oklab () {
		float X = this.X / 100f, Y = this.Y / 100f, Z = this.Z / 100f;
		float r = 3.2404542f * X - 1.5371385f * Y - 0.4985314f * Z; // To linear RGB without clamp, D65.
		float g = -0.9692660f * X + 1.8760108f * Y + 0.0415560f * Z;
		float b = 0.0556434f * X - 0.2040259f * Y + 1.0572252f * Z;
		float l = (float)Math.cbrt(0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b);
		float m = (float)Math.cbrt(0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b);
		float s = (float)Math.cbrt(0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b);
		return new Oklab( //
			0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s, //
			1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s, //
			0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s);
	}

	public RGB RGB () {
		float X = this.X / 100, Y = this.Y / 100, Z = this.Z / 100;
		return new RGB(sRGB(clamp(3.2404542f * X - 1.5371385f * Y - 0.4985314f * Z)),
			sRGB(clamp(-0.9692660f * X + 1.8760108f * Y + 0.0415560f * Z)),
			sRGB(clamp(0.0556434f * X - 0.2040259f * Y + 1.0572252f * Z)));
	}

	/** @return NaN if invalid. */
	public xy xy () {
		float sum = X + Y + Z;
		if (sum < EPSILON) return new xy(Float.NaN, Float.NaN);
		return new xy(X / sum, Y / sum);
	}

	/** @return NaN if invalid. */
	public xyY xyY () {
		float sum = X + Y + Z;
		if (sum < EPSILON) return new xyY(Float.NaN, Float.NaN, Float.NaN);
		return new xyY(X / sum, Y / sum, Y);
	}

	public XYZ add (float value) {
		return new XYZ(X + value, Y + value, Z + value);
	}

	public XYZ add (int index, float value) {
		return switch (index) {
		case 0 -> new XYZ(X + value, Y, Z);
		case 1 -> new XYZ(X, Y + value, Z);
		case 2 -> new XYZ(X, Y, Z + value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public XYZ add (float X, float Y, float Z) {
		return new XYZ(this.X + X, this.Y + Y, this.Z + Z);
	}

	public XYZ lerp (XYZ other, float t) {
		return new XYZ(Util.lerp(X, other.X, t), Util.lerp(Y, other.Y, t), Util.lerp(Z, other.Z, t));
	}

	public float max () {
		return Util.max(X, Y, Z);
	}

	public float min () {
		return Util.min(X, Y, Z);
	}

	public XYZ nor () {
		float max = max();
		return max < EPSILON ? this : new XYZ(X / max, Y / max, Z / max);
	}

	public XYZ sub (float value) {
		return new XYZ(X - value, Y - value, Z - value);
	}

	public XYZ sub (int index, float value) {
		return switch (index) {
		case 0 -> new XYZ(X - value, Y, Z);
		case 1 -> new XYZ(X, Y - value, Z);
		case 2 -> new XYZ(X, Y, Z - value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public XYZ sub (float X, float Y, float Z) {
		return new XYZ(this.X - X, this.Y - Y, this.Z - Z);
	}

	public XYZ scl (float value) {
		return new XYZ(X * value, Y * value, Z * value);
	}

	public XYZ scl (int index, float value) {
		return switch (index) {
		case 0 -> new XYZ(X * value, Y, Z);
		case 1 -> new XYZ(X, Y * value, Z);
		case 2 -> new XYZ(X, Y, Z * value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public XYZ scl (float X, float Y, float Z) {
		return new XYZ(this.X * X, this.Y * Y, this.Z * Z);
	}

	public float dst (XYZ other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (XYZ other) {
		float dx = X - other.X, dy = Y - other.Y, dz = Z - other.Z;
		return dx * dx + dy * dy + dz * dz;
	}

	public XYZ withY (float Y) {
		return set(1, Y);
	}

	/** CIE 1931 x-bar color matching function (380-780nm at 5nm intervals). **/
	static public float[] Xbar = {0.001368f, 0.002236f, 0.004243f, 0.00765f, 0.01431f, 0.02319f, 0.04351f, 0.07763f, 0.13438f,
		0.21477f, 0.2839f, 0.3285f, 0.34828f, 0.34806f, 0.3362f, 0.3187f, 0.2908f, 0.2511f, 0.19536f, 0.1421f, 0.09564f, 0.05795f,
		0.03201f, 0.0147f, 0.0049f, 0.0024f, 0.0093f, 0.0291f, 0.06327f, 0.1096f, 0.1655f, 0.22575f, 0.2904f, 0.3597f, 0.43345f,
		0.51205f, 0.5945f, 0.6784f, 0.7621f, 0.8425f, 0.9163f, 0.9786f, 1.0263f, 1.0567f, 1.0622f, 1.0456f, 1.0026f, 0.9384f,
		0.85445f, 0.7514f, 0.6424f, 0.5419f, 0.4479f, 0.3608f, 0.2835f, 0.2187f, 0.1649f, 0.1212f, 0.0874f, 0.0636f, 0.04677f,
		0.0329f, 0.0227f, 0.01584f, 0.011359f, 0.008111f, 0.00579f, 0.004109f, 0.002899f, 0.002049f, 0.00144f, 0.001f, 0.00069f,
		0.000476f, 0.000332f, 0.000235f, 0.000166f, 0.000117f, 0.000083f, 0.000059f, 0.000042f};

	/** CIE 1931 y-bar color matching function (380-780nm at 5nm intervals). **/
	static public float[] Ybar = {0.000039f, 0.000064f, 0.00012f, 0.000217f, 0.000396f, 0.00064f, 0.00121f, 0.00218f, 0.004f,
		0.0073f, 0.0116f, 0.01684f, 0.023f, 0.0298f, 0.038f, 0.048f, 0.06f, 0.0739f, 0.09098f, 0.1126f, 0.13902f, 0.1693f, 0.20802f,
		0.2586f, 0.323f, 0.4073f, 0.503f, 0.6082f, 0.71f, 0.7932f, 0.862f, 0.91485f, 0.954f, 0.9803f, 0.99495f, 1f, 0.995f, 0.9786f,
		0.952f, 0.9154f, 0.87f, 0.8163f, 0.757f, 0.6949f, 0.631f, 0.5668f, 0.503f, 0.4412f, 0.381f, 0.321f, 0.265f, 0.217f, 0.175f,
		0.1382f, 0.107f, 0.0816f, 0.061f, 0.04458f, 0.032f, 0.0232f, 0.017f, 0.01192f, 0.00821f, 0.005723f, 0.004102f, 0.002929f,
		0.002091f, 0.001484f, 0.001047f, 0.00074f, 0.00052f, 0.000361f, 0.000249f, 0.000172f, 0.00012f, 0.000085f, 0.00006f,
		0.000042f, 0.00003f, 0.000021f, 0.000015f};

	/** CIE 1931 z-bar color matching function (380-780nm at 5nm intervals). **/
	static public float[] Zbar = {0.00645f, 0.01055f, 0.02005f, 0.03621f, 0.06785f, 0.1102f, 0.2074f, 0.3713f, 0.6456f, 1.03905f,
		1.3856f, 1.62296f, 1.74706f, 1.7826f, 1.77211f, 1.7441f, 1.6692f, 1.5281f, 1.28764f, 1.0419f, 0.81295f, 0.6162f, 0.46518f,
		0.3533f, 0.272f, 0.2123f, 0.1582f, 0.1117f, 0.07825f, 0.05725f, 0.04216f, 0.02984f, 0.0203f, 0.0134f, 0.00875f, 0.00575f,
		0.0039f, 0.00275f, 0.0021f, 0.0018f, 0.00165f, 0.0014f, 0.0011f, 0.001f, 0.0008f, 0.0006f, 0.00034f, 0.00024f, 0.00019f,
		0.0001f, 0.00005f, 0.00003f, 0.00002f, 0.00001f, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0};
}
