
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Util;

/** Human-friendly {@link HSL}. Perceptually uniform saturation and lightness. */
public record HSLuv (
	/** Hue [0..360] or NaN if achromatic. */
	float H,
	/** Saturation [0..100]. */
	float S,
	/** Lightness [0..100]. */
	float L) {

	static private final float[][] XYZ_RGB = {{3.2404542f, -1.5371385f, -0.4985314f}, {-0.9692660f, 1.8760108f, 0.0415560f},
		{0.0556434f, -0.2040259f, 1.0572252f}};

	/** @return NaN if invalid. */
	public RGB RGB () {
		if (L > 100 - EPSILON) return new RGB(1, 1, 1);
		if (L < EPSILON) return new RGB(0, 0, 0);
		return new LCHuv(L, maxChromaForLH(L, H) * S / 100, H).Luv().RGB();
	}

	public HSLuv lerp (HSLuv other, float t) {
		return new HSLuv(lerpAngle(H, other.H, t), Util.lerp(S, other.S, t), Util.lerp(L, other.L, t));
	}

	static float maxChromaForLH (float L, float H) { // Based on Copyright (c) 2016 Alexei Boronine (MIT License).
		H *= degRad;
		float sin = (float)Math.sin(H), cos = (float)Math.cos(H);
		float sub1 = (L + 0.16f) / 1.16f;
		sub1 *= sub1 * sub1;
		float sub2 = sub1 > EPSILON ? sub1 : L / Lab.k;
		float min = Float.MAX_VALUE;
		for (int i = 0; i < 3; i++) {
			float m1 = XYZ_RGB[i][0] * sub2, m2 = XYZ_RGB[i][1] * sub2, m3 = XYZ_RGB[i][2] * sub2;
			for (int t = 0; t < 2; t++) {
				float top1 = 2845.17f * m1 - 948.39f * m3;
				float top2 = (8384.22f * m3 + 7698.60f * m2 + 7317.18f * m1 - 7698.60f * t) * L;
				float bottom = (6322.60f * m3 - 1264.52f * m2) + 1264.52f * t;
				float length = top2 / bottom / (sin - top1 / bottom * cos);
				if (length >= 0) min = Math.min(min, length);
			}
		}
		return min;
	}
}
