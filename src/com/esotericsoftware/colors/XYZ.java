
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.LMS.CAT;
import com.esotericsoftware.colors.Util.LMSUtil;
import com.esotericsoftware.colors.Util.LabUtil;

/** CIE 1931 tristimulus values. Foundation of colorimetry. */
public record XYZ (
	/** X tristimulus [0+]. */
	float X,
	/** Y tristimulus (luminance) [0+]. */
	float Y,
	/** Z tristimulus [0+]. */
	float Z) {

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

	/** Uses {@link Illuminant.CIE2#D65}. */
	public Lab Lab () {
		return Lab(Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	public Lab Lab (XYZ tristimulus) {
		float X = this.X / tristimulus.X, Y = this.Y / tristimulus.Y, Z = this.Z / tristimulus.Z;
		X = X > LabUtil.e ? (float)Math.pow(X, 1 / 3d) : (LabUtil.k * X + 16) / 116;
		Y = Y > LabUtil.e ? (float)Math.pow(Y, 1 / 3d) : (LabUtil.k * Y + 16) / 116;
		Z = Z > LabUtil.e ? (float)Math.pow(Z, 1 / 3d) : (LabUtil.k * Z + 16) / 116;
		return new Lab(116 * Y - 16, 500 * (X - Y), 200 * (Y - Z));
	}

	/** Uses {@link Illuminant.CIE2#D65}.
	 * @return NaN if invalid. */
	public Luv Luv () {
		return Luv(Illuminant.CIE2.D65);
	}

	/** @return NaN if invalid. */
	public Luv Luv (XYZ tristimulus) {
		float Xn = tristimulus.X, Yn = tristimulus.Y, Zn = tristimulus.Z;
		float yr = Y / Yn, L = yr > LabUtil.e ? 116 * (float)Math.cbrt(yr) - 16 : LabUtil.k * yr;
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
		case HPE -> LMSUtil.HPE_forward;
		case Bradford -> LMSUtil.Bradford_forward;
		case VonKries -> LMSUtil.vonKries_forward;
		case CAT97 -> LMSUtil.CAT97_forward;
		default -> LMSUtil.CAT02_forward;
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

	public XYZ lerp (XYZ other, float t) {
		return new XYZ(Colors.lerp(X, other.X, t), Colors.lerp(Y, other.Y, t), Colors.lerp(Z, other.Z, t));
	}
}
