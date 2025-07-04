
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
	float s) {

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

	/** Uses {@link CAM02.VC#sRGB}. */
	public Lab Lab () {
		return XYZ(CAM02.VC.sRGB).Lab();
	}

	public Lab Lab (CAM02.VC vc) {
		return XYZ(vc).Lab();
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public LinearRGB LinearRGB () {
		return XYZ(CAM02.VC.sRGB).LinearRGB();
	}

	public LinearRGB LinearRGB (CAM02.VC vc) {
		return XYZ(vc).LinearRGB();
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public RGB RGB () {
		return XYZ(CAM02.VC.sRGB).RGB();
	}

	public RGB RGB (CAM02.VC vc) {
		return XYZ(vc).RGB();
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public uv uv () {
		return XYZ(CAM02.VC.sRGB).uv();
	}

	public uv uv (CAM02.VC vc) {
		return XYZ(vc).uv();
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public xy xy () {
		return XYZ(CAM02.VC.sRGB).xy();
	}

	public xy xy (CAM02.VC vc) {
		return XYZ(vc).xy();
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public XYZ XYZ () {
		return XYZ(CAM02.VC.sRGB);
	}

	public XYZ XYZ (CAM02.VC vc) {
		// Handle edge case where J=0
		if (J == 0) {
			return new XYZ(0, 0, 0);
		}

		// Use colourfulness if chroma not available
		float C = this.C;
		if (C == 0 && M != 0) {
			C = M / (float)Math.pow(vc.FL(), 0.25);
		}

		// Compute temporary magnitude quantity
		float Jprime = Math.max(Math.abs(J), EPSILON);
		float t = (float)Math.pow(C / ((float)Math.sqrt(Jprime / 100) * (float)Math.pow(1.64 - Math.pow(0.29, vc.n()), 0.73)),
			1 / 0.9);

		// Eccentricity factor
		float hRad = h * degRad;
		float eT = 0.25f * ((float)Math.cos(hRad + 2) + 3.8f);

		// Achromatic response
		// Use signed power to handle negative J values
		float A = vc.Aw() * Math.signum(J) * (float)Math.pow(Math.abs(J) / 100, 1 / (vc.c() * vc.z()));

		// Compute P_1, P_2, P_3
		float p1 = (50000 / 13f) * vc.Nc() * vc.Ncb() * eT / t;
		float p2 = A / vc.Nbb() + 0.305f;
		float p3 = 21 / 20f;

		// Compute a and b using the correct approach from colour-science
		float sinH = (float)Math.sin(hRad);
		float cosH = (float)Math.cos(hRad);

		// Compute opponent color dimensions based on which is dominant
		float a, b;

		// Handle edge case where t is very small or zero
		if (t < EPSILON) {
			a = 0;
			b = 0;
		} else {
			float n = p2 * (2 + p3) * (460f / 1403f);

			// Avoid division by zero
			if (Math.abs(sinH) < EPSILON && Math.abs(cosH) < EPSILON) {
				a = 0;
				b = 0;
			} else if (Math.abs(sinH) >= Math.abs(cosH)) {
				// When |sin(h)| >= |cos(h)|
				float cosH_sinH = cosH / sinH;
				float denomB = p1 / sinH + (2 + p3) * (220f / 1403f) * cosH_sinH - (27f / 1403f) + p3 * (6300f / 1403f);
				b = n / denomB;
				a = b * cosH_sinH;
			} else {
				// When |sin(h)| < |cos(h)|
				float sinH_cosH = sinH / cosH;
				float denomA = p1 / cosH + (2 + p3) * (220f / 1403f) - ((27f / 1403f) - p3 * (6300f / 1403f)) * sinH_cosH;
				a = n / denomA;
				b = a * sinH_cosH;
			}
		}

		// Post-adaptation non-linear response compression matrix
		float rA = (460 * p2 + 451 * a + 288 * b) / 1403;
		float gA = (460 * p2 - 891 * a - 261 * b) / 1403;
		float bA = (460 * p2 - 220 * a - 6300 * b) / 1403;

		// Reverse post-adaptation non-linear response compression
		// Note: The forward transform adds 0.1, so we need to subtract it in the inverse
		float rA_adj = rA - 0.1f;
		float gA_adj = gA - 0.1f;
		float bA_adj = bA - 0.1f;

		float rCBase = Math.max(0, 27.13f * Math.abs(rA_adj) / (400 - Math.abs(rA_adj)));
		float gCBase = Math.max(0, 27.13f * Math.abs(gA_adj) / (400 - Math.abs(gA_adj)));
		float bCBase = Math.max(0, 27.13f * Math.abs(bA_adj) / (400 - Math.abs(bA_adj)));
		float rC = Math.signum(rA_adj) * (100 / vc.FL()) * (float)Math.pow(rCBase, 1 / 0.42);
		float gC = Math.signum(gA_adj) * (100 / vc.FL()) * (float)Math.pow(gCBase, 1 / 0.42);
		float bC = Math.signum(bA_adj) * (100 / vc.FL()) * (float)Math.pow(bCBase, 1 / 0.42);

		// Reverse Hunt-Pointer-Estevez transform
		// Using combined inverse matrix (CAT02 @ HPE_INVERSE)
		float rP = rC * 1.55915240f + gC * -0.54472268f + bC * -0.01444531f;
		float gP = rC * -0.71432672f + gC * 1.85030997f + bC * -0.13597611f;
		float bP = rC * 0.01077551f + gC * 0.00521877f + bC * 0.98400561f;

		// Reverse chromatic adaptation
		float rF = rP / vc.rgbD()[0];
		float gF = gP / vc.rgbD()[1];
		float bF = bP / vc.rgbD()[2];

		// Reverse CAT02 transform using inverse matrix
		return new XYZ(rF * 1.0961f + gF * -0.2789f + bF * 0.1828f, rF * 0.4544f + gF * 0.4735f + bF * 0.0721f,
			rF * -0.0096f + gF * -0.0057f + bF * 1.0153f);
	}

	public CAM02 lerp (CAM02 other, float t) {
		return new CAM02(Util.lerp(J, other.J, t), Util.lerp(C, other.C, t), lerpAngle(h, other.h, t), Util.lerp(Q, other.Q, t),
			Util.lerp(M, other.M, t), Util.lerp(s, other.s, t));
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

		/** @param wp Whitepoint tristimulus values.
		 * @param La Adapting luminance in cd/m², typically 20% of white luminance.
		 * @param Yb Background luminous factor (typically 20 for average surround).
		 * @param surround Surround factor, typically: 0=dark (0% surround), 1=dim (0-20% surround), 2=average (>20% surround).
		 * @param discounting True when the eye is assumed to be fully adapted. False for most applications (incomplete chromatic
		 *           adaptation). */
		static public VC with (XYZ wp, float La, float Yb, float surround, boolean discounting) {
			// Transform whitepoint to CAT02 RGB
			float rW = wp.X() * 0.7328f + wp.Y() * 0.4296f + wp.Z() * -0.1624f;
			float gW = wp.X() * -0.7036f + wp.Y() * 1.6975f + wp.Z() * 0.0061f;
			float bW = wp.X() * 0.0030f + wp.Y() * 0.0136f + wp.Z() * 0.9834f;

			// Determine viewing condition parameters
			float F, c, Nc;
			if (surround >= 2) { // Average
				F = 1.0f;
				c = 0.69f;
				Nc = 1.0f;
			} else if (surround >= 1) { // Dim
				F = 0.9f;
				c = 0.59f;
				Nc = 0.9f;
			} else { // Dark
				F = 0.8f;
				c = 0.525f;
				Nc = 0.8f;
			}

			// Degree of adaptation
			float d = discounting ? 1 : F * (1 - 1 / 3.6f * (float)Math.exp((-La - 42) / 92));
			float[] rgbD = {d * (100 / rW) + 1 - d, d * (100 / gW) + 1 - d, d * (100 / bW) + 1 - d};

			// Luminance level adaptation factor
			float k = 1 / (5 * La + 1);
			float k4 = k * k * k * k;
			float FL = 0.2f * k4 * (5 * La) + 0.1f * (1 - k4) * (1 - k4) * (float)Math.cbrt(5 * La);

			// Background parameters
			float n = Yb / wp.Y();
			float z = 1.48f + (float)Math.sqrt(n);
			float Nbb = 0.725f / (float)Math.pow(n, 0.2);
			float Ncb = Nbb;

			// Achromatic response for white
			float rAF = (float)Math.pow(FL * rgbD[0] * rW / 100, 0.42);
			float gAF = (float)Math.pow(FL * rgbD[1] * gW / 100, 0.42);
			float bAF = (float)Math.pow(FL * rgbD[2] * bW / 100, 0.42);
			float rA = 400 * rAF / (rAF + 27.13f);
			float gA = 400 * gAF / (gAF + 27.13f);
			float bA = 400 * bAF / (bAF + 27.13f);
			float Aw = (2 * rA + gA + 0.05f * bA) * Nbb;

			return new VC(Aw, Nbb, Ncb, c, Nc, n, rgbD, FL, (float)Math.pow(FL, 0.25), z, wp, La, Yb, F);
		}

		static public final VC sRGB = VC.with(CIE2.D65, 318.31f, 20, 2, false);
	}
}
