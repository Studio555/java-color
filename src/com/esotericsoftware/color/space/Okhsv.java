
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Util;

/** Oklab-based {@link HSV}. More perceptually uniform than HSV. */
public record Okhsv (
	/** Hue [0..360] or NaN if achromatic. */
	float h,
	/** Saturation [0..1]. */
	float s,
	/** Value [0..1]. */
	float v) implements Color {

	static private final float k_3 = 1.206f / 1.03f;

	public RGB RGB () {
		float h = this.h * degRad;
		if (v < EPSILON) return new RGB(0, 0, 0); // Black.
		if (s < EPSILON) return new Oklab(v, 0, 0).RGB(); // Gray.
		float a_ = (float)Math.cos(h), b_ = (float)Math.sin(h);
		float[] ST_max = cuspST(a_, b_);
		float T_max = ST_max[1], S_0 = 0.5f, k = 1 - S_0 / ST_max[0];
		float L_v = 1 - s * S_0 / (S_0 + T_max - T_max * k * s);
		float C_v = s * T_max * S_0 / (S_0 + T_max - T_max * k * s);
		float L = v * L_v, C = v * C_v;
		float L_vt = toeInv(L_v);
		float C_vt = C_v * L_vt / L_v;
		float L_new = toeInv(L);
		C *= L_new / L;
		LinearRGB l_r = new Oklab(L_vt, a_ * C_vt, b_ * C_vt).LinearRGB();
		float scale = (float)Math.cbrt(1 / Math.max(0, max(l_r.r(), l_r.g(), l_r.b())));
		C *= scale;
		return new Oklab(L_new * scale, C * a_, C * b_).RGB();
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}

	public float Y () {
		return RGB().Y();
	}

	public Okhsv lerp (Okhsv other, float t) {
		return new Okhsv(lerpAngle(h, other.h, t), Util.lerp(s, other.s, t), Util.lerp(v, other.v, t));
	}

	@SuppressWarnings("all")
	public Okhsv Okhsv () {
		return this;
	}

	// Based on Copyright (c) 2021 Björn Ottosson (MIT license):

	static float toe (float x) {
		return 0.5f * (k_3 * x - 0.206f + (float)Math.sqrt((k_3 * x - 0.206f) * (k_3 * x - 0.206f) + 4 * 0.03f * k_3 * x));
	}

	static float toeInv (float x) {
		return (x * x + 0.206f * x) / (k_3 * (x + 0.03f));
	}

	static float[] cuspST (float a, float b) {
		float S_cusp = maxSaturation(a, b);
		LinearRGB rgb_at_max = new Oklab(1, S_cusp * a, S_cusp * b).LinearRGB();
		float L = (float)Math.cbrt(1 / max(rgb_at_max.r(), rgb_at_max.g(), rgb_at_max.b()));
		float C = L * S_cusp;
		return new float[] {C / L, C / (1 - L)};
	}

	static float[] Cs (float L, float a_, float b_) {
		float[][] M = {{4.0767416621f, -3.3077115913f, 0.2309699292f}, {-1.2684380046f, 2.6097574011f, -0.3413193965f},
			{-0.0041960863f, -0.7034186147f, 1.707614701f}};
		float S_max = Float.MAX_VALUE;
		for (int i = 0; i < 3; i++) {
			float denom = a_ * M[i][0] + b_ * M[i][1];
			if (Math.abs(denom) < EPSILON) continue;
			float t = -M[i][2] / denom;
			if (t < 0) continue;
			float s = (1 + t) * denom;
			if (s > 0 && s < S_max) S_max = s;
		}
		float C_max = maxSaturation(a_, b_);
		float denom = Math.min(L * S_max, (1 - L) * C_max);
		float k = denom < EPSILON ? 0 : C_max / denom;
		float S = 0.11516993f + 1 / (7.4477897f + 4.1590124f * b_ + a_ * (-2.19557347f + 1.75198401f * b_
			+ a_ * (-2.13704948f - 10.02301043f * b_ + a_ * (-4.24894561f + 5.38770819f * b_ + 4.69891013f * a_))));
		float T = 0.11239642f + 1 / (1.6132032f - 0.68124379f * b_ + a_ * (0.40370612f + 0.90148123f * b_
			+ a_ * (-0.27087943f + 0.6122399f * b_ + a_ * (0.00299215f - 0.45399568f * b_ - 0.14661872f * a_))));
		float inv_scale = Math.min(L * S, (1 - L) * 1 / T);
		float denom2 = 1 + k;
		float L_mid = 0.5f * (1 + (denom2 < EPSILON ? 0 : Math.signum(L - 0.5f) * inv_scale / denom2));
		float C_mid = L_mid * S, C_a = L * 0.4f, C_b = (1 - L) * 0.8f;
		float C_0 = (float)Math.sqrt(1 / (1 / (C_a * C_a) + 1 / (C_b * C_b)));
		return new float[] {C_0, C_mid, C_max};
	}

	static private float maxSaturation (float a, float b) {
		float k0, k1, k2, k3, k4, wl, wm, ws;
		if (-1.88170328f * a - 0.80936493f * b > 1) { // Red.
			k0 = 1.19086277f;
			k1 = 1.76576728f;
			k2 = 0.59662641f;
			k3 = 0.75515197f;
			k4 = 0.56771245f;
			wl = 4.0767416621f;
			wm = -3.3077115913f;
			ws = 0.2309699292f;
		} else if (1.81444104f * a - 1.19445276f * b > 1) { // Green.
			k0 = 0.73956515f;
			k1 = -0.45954404f;
			k2 = 0.08285427f;
			k3 = 0.1254107f;
			k4 = 0.14503204f;
			wl = -1.2684380046f;
			wm = 2.6097574011f;
			ws = -0.3413193965f;
		} else { // Blue.
			k0 = 1.35733652f;
			k1 = -0.00915799f;
			k2 = -1.1513021f;
			k3 = -0.50559606f;
			k4 = 0.00692167f;
			wl = -0.0041960863f;
			wm = -0.7034186147f;
			ws = 1.707614701f;
		}
		float S = k0 + k1 * a + k2 * b + k3 * a * a + k4 * a * b;
		float k_l = 0.3963377774f * a + 0.2158037573f * b;
		float k_m = -0.1055613458f * a - 0.0638541728f * b;
		float k_s = -0.0894841775f * a - 1.291485548f * b;
		float l_ = 1 + S * k_l, m_ = 1 + S * k_m, s_ = 1 + S * k_s;
		float l = l_ * l_ * l_, m = m_ * m_ * m_, s = s_ * s_ * s_;
		float l_dS = 3 * k_l * l_ * l_, m_dS = 3 * k_m * m_ * m_, s_dS = 3 * k_s * s_ * s_;
		float l_dS2 = 6 * k_l * k_l * l_, m_dS2 = 6 * k_m * k_m * m_, s_dS2 = 6 * k_s * k_s * s_;
		float f = wl * l + wm * m + ws * s, f1 = wl * l_dS + wm * m_dS + ws * s_dS, f2 = wl * l_dS2 + wm * m_dS2 + ws * s_dS2;
		return S - f * f1 / (f1 * f1 - 0.5f * f * f2);
	}
}
