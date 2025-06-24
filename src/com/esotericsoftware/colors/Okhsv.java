
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.Util.OkhsvUtil;

/** Oklab-based {@link HSV}. More perceptually uniform than HSV. */
public record Okhsv (
	/** Hue [0..360] or NaN if achromatic. */
	float h,
	/** Saturation [0..1]. */
	float s,
	/** Value [0..1]. */
	float v) {

	public RGB RGB () {
		float h = this.h * degRad;
		if (v < EPSILON) return new RGB(0, 0, 0); // Black.
		if (s < EPSILON) return new Oklab(v, 0, 0).RGB(); // Gray.
		float a_ = (float)Math.cos(h), b_ = (float)Math.sin(h);
		float[] ST_max = OkhsvUtil.cuspST(a_, b_);
		float T_max = ST_max[1], S_0 = 0.5f, k = 1 - S_0 / ST_max[0];
		float L_v = 1 - s * S_0 / (S_0 + T_max - T_max * k * s);
		float C_v = s * T_max * S_0 / (S_0 + T_max - T_max * k * s);
		float L = v * L_v, C = v * C_v;
		float L_vt = OkhsvUtil.toeInv(L_v);
		float C_vt = C_v * L_vt / L_v;
		float L_new = OkhsvUtil.toeInv(L);
		C *= L_new / L;
		LinearRGB l_r = new Oklab(L_vt, a_ * C_vt, b_ * C_vt).LinearRGB();
		float scale = (float)Math.cbrt(1 / Math.max(0, max(l_r.r(), l_r.g(), l_r.b())));
		C *= scale;
		return new Oklab(L_new * scale, C * a_, C * b_).RGB();
	}

	public Okhsv lerp (Okhsv other, float t) {
		return new Okhsv(lerpAngle(h, other.h, t), Colors.lerp(s, other.s, t), Colors.lerp(v, other.v, t));
	}
}
