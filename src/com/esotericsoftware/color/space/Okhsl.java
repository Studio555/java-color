
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

/** Oklab-based {@link HSL}. More perceptually uniform than HSL. */
public record Okhsl (
	/** Hue [0..360] or NaN if achromatic. */
	float h,
	/** Saturation [0..1]. */
	float s,
	/** Lightness [0..1]. */
	float l) implements Color {

	public RGB RGB () {
		float L = Okhsv.toeInv(l), h = this.h * degRad;
		if (l >= 1 - EPSILON) return new RGB(1, 1, 1); // White.
		if (l <= EPSILON) return new RGB(0, 0, 0); // Black.
		if (s < EPSILON) return new Oklab(L, 0, 0).RGB(); // Gray.
		float a_ = (float)Math.cos(h), b_ = (float)Math.sin(h);
		float[] Cs = Okhsv.Cs(L, a_, b_);
		float C_0 = Cs[0], C_mid = Cs[1], C_max = Cs[2], C;
		if (s < 0.8f) {
			float t = 1.25f * s, k_1 = 0.8f * C_0, k_2 = (1 - k_1 / C_mid);
			C = t * k_1 / (1 - k_2 * t);
		} else {
			float t = 5 * (s - 0.8f);
			float k_0 = C_mid, k_1 = 0.2f * C_mid * C_mid * 1.25f * 1.25f / C_0, k_2 = 1 - (k_1) / (C_max - C_mid);
			C = k_0 + t * k_1 / (1 - k_2 * t);
		}
		return new Oklab(L, C * a_, C * b_).RGB();
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}

	@SuppressWarnings("all")
	public Okhsl Okhsl () {
		return this;
	}
}
