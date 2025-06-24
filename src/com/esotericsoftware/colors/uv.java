
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.Util.*;

/** CIE 1976 u'v' chromaticity coordinates. */
public record uv (
	/** u' chromaticity [0..1]. */
	float u,
	/** v' chromaticity [0..1]. */
	float v) {

	/** @return [1667..25000K] or NaN if invalid. */
	public CCT CCT () {
		return xy().CCT();
	}

	/** @return Normalized. */
	public RGB RGB () {
		xy xy = xy();
		RGB rgb = new xyY(xy.x(), xy.y(), 1).XYZ().RGB();
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float max = max(r, g, b);
		if (max > 0) {
			r /= max;
			g /= max;
			b /= max;
		}
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	public uv1960 uv1960 () {
		return new uv1960(u, v / 1.5f);
	}

	/** @return NaN if invalid. */
	public xy xy () {
		float denom = 6 * u - 16 * v + 12;
		if (Math.abs(denom) < EPSILON) return new xy(Float.NaN, Float.NaN);
		return new xy(9 * u / denom, 4 * v / denom);
	}

	/** Uses Y=100. */
	public XYZ XYZ () {
		return xy().XYZ();
	}

	/** {@link Lab#deltaE2000(Lab, float, float, float)} with 1 for lightness, chroma, and hue. */
	public float deltaE2000 (uv other) {
		return XYZ().Lab().deltaE2000(other.XYZ().Lab(), 1, 1, 1);
	}

	public float distance (uv other) {
		float du = u - other.u, dv = v - other.v;
		return (float)Math.sqrt(du * du + dv * dv);
	}

	/** @return NaN if invalid. */
	public float Duv () {
		return xy().Duv();
	}

	public uv lerp (uv other, float t) {
		return new uv(Colors.lerp(u, other.u, t), Colors.lerp(v, other.v, t));
	}

	/** Compares perceptual chromaticity. */
	public float MacAdamSteps (uv uv) {
		return distance(uv) / 0.0011f;
	}
}
