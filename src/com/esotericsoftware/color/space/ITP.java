
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Util;

/** ITU-R BT.2100 for HDR and wide color gamut. Also known as ICtCp. */
public record ITP (
	/** Intensity [0..1]. */
	float I,
	/** Blue-yellow axis [-0.5..0.5]. */
	float Ct,
	/** Red-green axis [-0.5..0.5]. */
	float Cp) implements Color {

	static private final float PQ_m1 = 0.1593017578125f; // 2610 / 16384
	static private final float PQ_m2 = 78.84375f; // 2523 / 32
	static private final float PQ_c1 = 0.8359375f; // 3424 / 4096
	static private final float PQ_c2 = 18.8515625f; // 2413 / 128
	static private final float PQ_c3 = 18.6875f; // 2392 /128
	static private final float PQ_n = 0.15930175781f; // 2610 / 16384

	public RGB RGB () {
		float L = PQ_EOTF(I + 0.00860514f * Ct + 0.11103f * Cp); // PQ to linear.
		float M = PQ_EOTF(I + -0.00860514f * Ct + -0.11103f * Cp);
		float S = PQ_EOTF(I + 0.56003125f * Ct + -0.32062717f * Cp);
		float r2020 = 3.4366088f * L + -2.5064522f * M + 0.0698454f * S; // To BT.2020 RGB
		float g2020 = -0.7913296f * L + 1.9836005f * M + -0.1922709f * S;
		float b2020 = -0.0259499f * L + -0.0989138f * M + 1.1248637f * S;
		return new RGB( //
			sRGB(1.660491f * r2020 + -0.5876411f * g2020 + -0.0728499f * b2020), // BT.2020 to linear sRGB.
			sRGB(-0.1245505f * r2020 + 1.1328999f * g2020 + -0.0083494f * b2020), //
			sRGB(-0.0181508f * r2020 + -0.1005789f * g2020 + 1.1187297f * b2020));
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}

	public float Y () {
		return RGB().Y();
	}

	public ITP lerp (ITP other, float t) {
		return new ITP(Util.lerp(I, other.I, t), Util.lerp(Ct, other.Ct, t), Util.lerp(Cp, other.Cp, t));
	}

	@SuppressWarnings("all")
	public ITP ITP () {
		return this;
	}

	static float PQ_EOTF (float value) {
		if (value <= 0) return 0;
		float pow = (float)Math.pow(value, 1 / PQ_m2);
		float num = Math.max(0, pow - PQ_c1);
		float denom = PQ_c2 - PQ_c3 * pow;
		if (denom < EPSILON) return 0;
		return (float)Math.pow(num / denom, 1 / PQ_n);
	}

	static float PQ_EOTF_inverse (float value) {
		if (value <= 0) return 0;
		float pow = (float)Math.pow(value, PQ_m1);
		float num = PQ_c1 + PQ_c2 * pow;
		float denom = 1 + PQ_c3 * pow;
		return (float)Math.pow(num / denom, PQ_m2);
	}
}
