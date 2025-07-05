
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Observer;
import com.esotericsoftware.color.Util;

/** Color Appearance Model 2016. Predicts color appearance under viewing conditions. */
public record CAM16 (
	/** Lightness [0..100]. */
	float J,
	/** Chroma [0+]. */
	float C,
	/** Hue angle [0..360]. */
	float h,
	/** Brightness [0+]. */
	float Q,
	/** Colorfulness [0+]. */
	float M,
	/** Saturation [0+]. */
	float s) implements Color {

	public CAM16LCD CAM16LCD () {
		float h = this.h * degRad;
		float Jstar = 0.77f * J / (1 + 0.007f * J);
		float Mstar = 1 / 0.15f * (float)Math.log1p(0.15f * M);
		return new CAM16LCD(Jstar, Mstar * (float)Math.cos(h), Mstar * (float)Math.sin(h));
	}

	public CAM16SCD CAM16SCD () {
		float h = this.h * degRad;
		float Jstar = 1.65f * J / (1 + 0.007f * J);
		float Mstar = 1 / 0.0228f * (float)Math.log1p(0.0228f * M);
		return new CAM16SCD(Jstar, Mstar * (float)Math.cos(h), Mstar * (float)Math.sin(h));
	}

	public CAM16UCS CAM16UCS () {
		float h = this.h * degRad;
		float Jstar = 1.7f * J / (1 + 0.007f * J);
		float Mstar = 1 / 0.0228f * (float)Math.log1p(0.0228f * M);
		return new CAM16UCS(Jstar, Mstar * (float)Math.cos(h), Mstar * (float)Math.sin(h));
	}

	public Lab Lab (CAM16.VC vc) {
		return XYZ(vc).Lab();
	}

	public LinearRGB LinearRGB (CAM16.VC vc) {
		return XYZ(vc).LinearRGB();
	}

	public RGB RGB (CAM16.VC vc) {
		return XYZ(vc).RGB();
	}

	public uv uv (CAM16.VC vc) {
		return XYZ(vc).uv();
	}

	public xy xy (CAM16.VC vc) {
		return XYZ(vc).xy();
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public XYZ XYZ () {
		return XYZ(CAM16.VC.sRGB);
	}

	public XYZ XYZ (CAM16.VC vc) {
		float h = this.h * degRad;
		float alpha = C == 0 || J == 0 ? 0 : C / (float)Math.sqrt(J / 100);
		float t = (float)Math.pow(alpha / Math.pow(1.64 - Math.pow(0.29, vc.n), 0.73), 1 / 0.9);
		float ac = vc.Aw * (float)Math.pow(J / 100.0, 1.0 / vc.c / vc.z);
		float p1 = 0.25f * ((float)Math.cos(h + 2.0) + 3.8f) * (50000 / 13f) * vc.Nc * vc.Ncb, p2 = ac / vc.Nbb;
		float hSin = (float)Math.sin(h), hCos = (float)Math.cos(h);
		float gamma = 23 * (p2 + 0.305f) * t / (23 * p1 + 11 * t * hCos + 108 * t * hSin);
		float a = gamma * hCos, b = gamma * hSin;
		float rA = (460 * p2 + 451 * a + 288 * b) / 1403;
		float gA = (460 * p2 - 891 * a - 261 * b) / 1403;
		float bA = (460 * p2 - 220 * a - 6300 * b) / 1403;
		float rCBase = Math.max(0, 27.13f * Math.abs(rA) / (400 - Math.abs(rA)));
		float gCBase = Math.max(0, 27.13f * Math.abs(gA) / (400 - Math.abs(gA)));
		float bCBase = Math.max(0, 27.13f * Math.abs(bA) / (400 - Math.abs(bA)));
		float rC = Math.signum(rA) * (100 / vc.FL) * (float)Math.pow(rCBase, 1.0 / 0.42);
		float gC = Math.signum(gA) * (100 / vc.FL) * (float)Math.pow(gCBase, 1.0 / 0.42);
		float bC = Math.signum(bA) * (100 / vc.FL) * (float)Math.pow(bCBase, 1.0 / 0.42);
		float rF = rC / vc.rgbD[0], gF = gC / vc.rgbD[1], bF = bC / vc.rgbD[2];
		return new XYZ( //
			rF * 1.8620678f + gF * -1.0112547f + bF * 0.14918678f, //
			rF * 0.38752654f + gF * 0.62144744f + bF * -0.00897398f, //
			rF * -0.0158415f + gF * -0.03412294f + bF * 1.0499644f);
	}

	/** @return JCh are interpolated, QMs are NaN. */
	public CAM16 lerp (CAM16 other, float t) {
		return new CAM16(Util.lerp(J, other.J, t), Util.lerp(C, other.C, t), lerpAngle(h, other.h, t), //
			Float.NaN, Float.NaN, Float.NaN);
	}

	@SuppressWarnings("all")
	public CAM16 CAM16 () {
		return this;
	}

	/** {@link CAM16} viewing conditions. */
	public record VC (
		float Aw,
		float Nbb,
		float Ncb,
		float c,
		float Nc,
		float n,
		float[] rgbD,
		float FL,
		float FLRoot,
		float z,
		XYZ wp,
		float La,
		float Yb) {

		/** @param La Adapting luminance in cd/m², typically 20% of white luminance.
		 * @param Yb Background luminous factor (typically 20 for average surround).
		 * @param surround Surround factor, typically: 0=dark (0% surround), 1=dim (0-20% surround), 2=average (>20% surround).
		 * @param discounting True when the eye is assumed to be fully adapted. False for most applications (incomplete chromatic
		 *           adaptation). */
		static public VC with (XYZ wp, float La, float Yb, float surround, boolean discounting) {
			Yb = Math.max(0.1f, Yb); // Avoid non-physical black infinities.
			float rW = wp.X() * 0.401288f + wp.Y() * 0.650173f + wp.Z() * -0.051461f; // To cone/RGB responses.
			float gW = wp.X() * -0.250268f + wp.Y() * 1.204414f + wp.Z() * 0.045854f;
			float bW = wp.X() * -0.002079f + wp.Y() * 0.048952f + wp.Z() * 0.953127f;
			float Nc = 0.8f + surround / 10;
			float c = Nc >= 0.9f ? Util.lerp(0.59f, 0.69f, (Nc - 0.9f) * 10) : Util.lerp(0.525f, 0.59f, (Nc - 0.8f) * 10);
			float d = clamp(discounting ? 1 : Nc * (1 - 1 / 3.6f * (float)Math.exp((-La - 42) / 92)));
			float[] rgbD = {d * (100 / rW) + 1 - d, d * (100 / gW) + 1 - d, d * (100 / bW) + 1 - d};
			float k = 1 / (5 * La + 1), k4 = k * k * k * k, k4F = 1 - k4;
			float FL = k4 * La + 0.1f * k4F * k4F * (float)Math.cbrt(5 * La);
			float n = Lab.LstarToY(Yb) / wp.Y(), z = 1.48f + (float)Math.sqrt(n), Nbb = 0.725f / (float)Math.pow(n, 0.2);
			float rAF = (float)Math.pow(FL * rgbD[0] * rW / 100, 0.42);
			float gAF = (float)Math.pow(FL * rgbD[1] * gW / 100, 0.42);
			float bAF = (float)Math.pow(FL * rgbD[2] * bW / 100, 0.42);
			float rA = 400 * rAF / (rAF + 27.13f);
			float gA = 400 * gAF / (gAF + 27.13f);
			float bA = 400 * bAF / (bAF + 27.13f);
			float Aw = (2 * rA + gA + 0.05f * bA) * Nbb;
			return new VC(Aw, Nbb, Nbb, c, Nc, n, rgbD, FL, (float)Math.pow(FL, 0.25), z, wp, La, Yb);
		}

		static public final VC sRGB = VC.with(Observer.CIE2.D65, 200 / PI * Lab.LstarToYn(20), 20, 2, false);
		static public final VC HCT = VC.with(Observer.CIE2.D65, 200 / PI * Lab.LstarToYn(50), 50, 2, false);
	}
}
