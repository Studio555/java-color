
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Illuminant.CIE2;
import com.esotericsoftware.color.Util;

public record CAM02 (
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

	public CAM02LCD CAM02LCD () {
		float h = this.h * degRad;
		float Jstar = ((1 + 100 * 0.007f) * J) / (1 + 0.007f * J);
		float Mstar = 1 / 0.0053f * (float)Math.log1p(0.0053f * M);
		return new CAM02LCD(Jstar, Mstar * (float)Math.cos(h), Mstar * (float)Math.sin(h));
	}

	public CAM02SCD CAM02SCD () {
		float h = this.h * degRad;
		float Jstar = ((1 + 100 * 0.007f) * J) / (1 + 0.007f * J);
		float Mstar = 1 / 0.0363f * (float)Math.log1p(0.0363f * M);
		return new CAM02SCD(Jstar, Mstar * (float)Math.cos(h), Mstar * (float)Math.sin(h));
	}

	public CAM02UCS CAM02UCS () {
		float h = this.h * degRad;
		float Jstar = ((1 + 100 * 0.007f) * J) / (1 + 0.007f * J);
		float Mstar = 1 / 0.0228f * (float)Math.log1p(0.0228f * M);
		return new CAM02UCS(Jstar, Mstar * (float)Math.cos(h), Mstar * (float)Math.sin(h));
	}

	public Lab Lab (CAM02.VC vc) {
		return XYZ(vc).Lab();
	}

	public LinearRGB LinearRGB (CAM02.VC vc) {
		return XYZ(vc).LinearRGB();
	}

	public RGB RGB (CAM02.VC vc) {
		return XYZ(vc).RGB();
	}

	public uv uv (CAM02.VC vc) {
		return XYZ(vc).uv();
	}

	public xy xy (CAM02.VC vc) {
		return XYZ(vc).xy();
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public XYZ XYZ () {
		return XYZ(CAM02.VC.sRGB);
	}

	public XYZ XYZ (CAM02.VC vc) {
		if (J == 0) return new XYZ(0, 0, 0);
		float C = this.C;
		if (C == 0 && M != 0) C = M / (float)Math.pow(vc.FL(), 0.25); // Use colorfulness if chroma not available.
		float Jprime = Math.max(Math.abs(J), EPSILON); // Compute temporary magnitude quantity.
		float t = (float)Math.pow(C / ((float)Math.sqrt(Jprime / 100) * (float)Math.pow(1.64 - Math.pow(0.29, vc.n()), 0.73)),
			1 / 0.9);
		float hRad = h * degRad, eT = 0.25f * ((float)Math.cos(hRad + 2) + 3.8f); // Eccentricity factor.
		float A = vc.Aw() * Math.signum(J) * (float)Math.pow(Math.abs(J) / 100, 1 / (vc.c() * vc.z()));
		float p1 = (50000 / 13f) * vc.Nc() * vc.Ncb() * eT / t, p2 = A / vc.Nbb() + 0.305f, p3 = 21 / 20f;
		float sinH = (float)Math.sin(hRad), cosH = (float)Math.cos(hRad);
		float a, b; // Compute opponent color dimensions based on which is dominant.
		if (t < EPSILON) {
			a = 0;
			b = 0;
		} else {
			float n = p2 * (2 + p3) * (460 / 1403f);
			if (Math.abs(sinH) < EPSILON && Math.abs(cosH) < EPSILON) {
				a = 0;
				b = 0;
			} else if (Math.abs(sinH) >= Math.abs(cosH)) {
				float cosH_sinH = cosH / sinH;
				b = n / (p1 / sinH + (2 + p3) * (220 / 1403f) * cosH_sinH - (27f / 1403f) + p3 * (6300 / 1403f));
				a = b * cosH_sinH;
			} else {
				float sinH_cosH = sinH / cosH;
				a = n / (p1 / cosH + (2 + p3) * (220 / 1403f) - ((27f / 1403f) - p3 * (6300 / 1403f)) * sinH_cosH);
				b = a * sinH_cosH;
			}
		}
		float rA = (460 * p2 + 451 * a + 288 * b) / 1403; // Post-adaptation non-linear response compression matrix.
		float gA = (460 * p2 - 891 * a - 261 * b) / 1403;
		float bA = (460 * p2 - 220 * a - 6300 * b) / 1403;
		// Reverse post-adaptation non-linear response compression. The forward transform adds 0.1, so subtract it.
		float rA_adj = rA - 0.1f, gA_adj = gA - 0.1f, bA_adj = bA - 0.1f;
		float rCBase = Math.max(0, 27.13f * Math.abs(rA_adj) / (400 - Math.abs(rA_adj)));
		float gCBase = Math.max(0, 27.13f * Math.abs(gA_adj) / (400 - Math.abs(gA_adj)));
		float bCBase = Math.max(0, 27.13f * Math.abs(bA_adj) / (400 - Math.abs(bA_adj)));
		float rC = Math.signum(rA_adj) * (100 / vc.FL()) * (float)Math.pow(rCBase, 1 / 0.42);
		float gC = Math.signum(gA_adj) * (100 / vc.FL()) * (float)Math.pow(gCBase, 1 / 0.42);
		float bC = Math.signum(bA_adj) * (100 / vc.FL()) * (float)Math.pow(bCBase, 1 / 0.42);
		float rP = rC * 1.5591524f + gC * -0.54472268f + bC * -0.01444531f; // Reverse HPE, combined inverse CAT02/HPE matrix.
		float gP = rC * -0.71432672f + gC * 1.85030997f + bC * -0.13597611f;
		float bP = rC * 0.01077551f + gC * 0.00521877f + bC * 0.98400561f;
		float rF = rP / vc.rgbD()[0], gF = gP / vc.rgbD()[1], bF = bP / vc.rgbD()[2]; // Reverse chromatic adaptation.
		return new XYZ( // Reverse CAT02 transform.
			rF * 1.0961f + gF * -0.2789f + bF * 0.1828f, //
			rF * 0.4544f + gF * 0.4735f + bF * 0.0721f, //
			rF * -0.0096f + gF * -0.0057f + bF * 1.0153f);
	}

	/** @return JCh are interpolated, QMs are NaN. */
	public CAM02 lerp (CAM02 other, float t) {
		return new CAM02(Util.lerp(J, other.J, t), Util.lerp(C, other.C, t), lerpAngle(h, other.h, t), //
			Float.NaN, Float.NaN, Float.NaN);
	}

	@SuppressWarnings("all")
	public CAM02 CAM02 () {
		return this;
	}

	/** {@link CAM02} viewing conditions. */
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
		float Yb,
		float F) {

		/** @param La Adapting luminance in cd/m², typically 20% of white luminance.
		 * @param Yb Background luminous factor (typically 20 for average surround).
		 * @param surround Surround factor, typically: 0=dark (0% surround), 1=dim (0-20% surround), 2=average (>20% surround).
		 * @param discounting True when the eye is assumed to be fully adapted. False for most applications (incomplete chromatic
		 *           adaptation). */
		static public VC with (XYZ wp, float La, float Yb, float surround, boolean discounting) {
			float rW = wp.X() * 0.7328f + wp.Y() * 0.4296f + wp.Z() * -0.1624f; // To CAT02 RGB.
			float gW = wp.X() * -0.7036f + wp.Y() * 1.6975f + wp.Z() * 0.0061f;
			float bW = wp.X() * 0.003f + wp.Y() * 0.0136f + wp.Z() * 0.9834f;
			float F, c, Nc;
			if (surround >= 2) { // Average.
				F = 1;
				c = 0.69f;
				Nc = 1;
			} else if (surround >= 1) { // Dim.
				F = 0.9f;
				c = 0.59f;
				Nc = 0.9f;
			} else { // Dark.
				F = 0.8f;
				c = 0.525f;
				Nc = 0.8f;
			}
			float d = discounting ? 1 : F * (1 - 1 / 3.6f * (float)Math.exp((-La - 42) / 92)); // Degree of adaptation.
			float[] rgbD = {d * (100 / rW) + 1 - d, d * (100 / gW) + 1 - d, d * (100 / bW) + 1 - d};
			float k = 1 / (5 * La + 1), k4 = k * k * k * k;// Luminance level adaptation factor.
			float FL = 0.2f * k4 * (5 * La) + 0.1f * (1 - k4) * (1 - k4) * (float)Math.cbrt(5 * La);
			float n = Yb / wp.Y(), z = 1.48f + (float)Math.sqrt(n), Nbb = 0.725f / (float)Math.pow(n, 0.2); // Background parameters.
			float rAF = (float)Math.pow(FL * rgbD[0] * rW / 100, 0.42); // Achromatic response for white.
			float gAF = (float)Math.pow(FL * rgbD[1] * gW / 100, 0.42);
			float bAF = (float)Math.pow(FL * rgbD[2] * bW / 100, 0.42);
			float rA = 400 * rAF / (rAF + 27.13f);
			float gA = 400 * gAF / (gAF + 27.13f);
			float bA = 400 * bAF / (bAF + 27.13f);
			float Aw = (2 * rA + gA + 0.05f * bA) * Nbb;
			return new VC(Aw, Nbb, Nbb, c, Nc, n, rgbD, FL, (float)Math.pow(FL, 0.25), z, wp, La, Yb, F);
		}

		static public final VC sRGB = VC.with(CIE2.D65, 200 / PI * Lab.LstarToYn(20), 20, 2, false);
	}
}
