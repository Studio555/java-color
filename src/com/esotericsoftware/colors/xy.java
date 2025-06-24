
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.Util.CCTUtil;

/** CIE 1931 chromaticity coordinates. */
public record xy (
	/** x chromaticity [0..1]. */
	float x,
	/** y chromaticity [0..1]. */
	float y) {

	/** <0.5K error, 0.021K average at <7000K. <1.1K error, 0.065K average at <14000K. <4.9K error, 0.3K average at <25000K.
	 * @return CCT [1667..25000K] or NaN if outside valid range. */
	public float CCT () {
		if (x < 0.25f || x > 0.565f || y < 0.20f || y > 0.45f) return Float.NaN;
		float n = (x - 0.3320f) / (0.1858f - y);
		float CCT = 449 * n * n * n + 3525 * n * n + 6823.3f * n + 5520.33f; // McCamy initial guess.
		if (CCT < 1667 || CCT > 25000) return Float.NaN;
		float adjust = CCT < 7000 ? 0.000489f : (CCT < 15000 ? 0.0024f : 0.00095f);
		for (int i = 0; i < 3; i++) {
			xy current = xy(CCT, 0);
			float ex = x - current.x, ey = y - current.y;
			if (ex * ex + ey * ey < 1e-10f) break;
			float h = CCT * adjust;
			xy next = xy(CCT + h, 0);
			float tx = (next.x - current.x) / h, ty = (next.y - current.y) / h;
			CCT += (ex * tx + ey * ty) / (tx * tx + ty * ty);
		}
		return CCT;
	}

	/** @return NaN if invalid. */
	public float Duv () {
		float CCT = CCT();
		xy xyBB = xy(CCT, 0);
		uv1960 perp = CCTUtil.perpendicular(CCT, xyBB), uvBB = xyBB.uv1960(), uv = uv1960();
		return (uv.u() - uvBB.u()) * perp.u() + (uv.v() - uvBB.v()) * perp.v();
	}

	/** Compares perceptual chromaticity.
	 * @return NaN if invalid. */
	public float MacAdamSteps (xy xy) {
		return uv().MacAdamSteps(xy.uv());
	}

	/** Uses {@link Gamut#sRGB}.
	 * @return Normalized or NaN if invalid. */
	public RGB RGB () {
		return RGB(Gamut.sRGB);
	}

	/** @return NaN if invalid. */
	public RGB RGB (Gamut gamut) {
		xy xy = gamut.clamp(this);
		if (xy.y < EPSILON) return new RGB(Float.NaN, Float.NaN, Float.NaN);
		float X = xy.x / xy.y;
		float Z = (1 - xy.x - xy.y) / xy.y;
		float[][] xyzToRGB = gamut.XYZ_RGB;
		float r = xyzToRGB[0][0] * X + xyzToRGB[0][1] + xyzToRGB[0][2] * Z; // Y=1.
		float g = xyzToRGB[1][0] * X + xyzToRGB[1][1] + xyzToRGB[1][2] * Z;
		float b = xyzToRGB[2][0] * X + xyzToRGB[2][1] + xyzToRGB[2][2] * Z;
		float max = max(r, g, b);
		if (max > 0) {
			r /= max;
			g /= max;
			b /= max;
		}
		return new RGB(sRGB(Math.max(0, r)), sRGB(Math.max(0, g)), sRGB(Math.max(0, b)));
	}

	/** @return NaN if invalid. */
	public uv uv () {
		float denom = -2 * x + 12 * y + 3;
		if (Math.abs(denom) < EPSILON) return new uv(Float.NaN, Float.NaN);
		return new uv(4 * x / denom, 9 * y / denom);
	}

	/** @return NaN if invalid. */
	public uv1960 uv1960 () {
		float denom = -2 * x + 12 * y + 3;
		if (Math.abs(denom) < EPSILON) return new uv1960(Float.NaN, Float.NaN);
		return new uv1960(4 * x / denom, 6 * y / denom);
	}

	/** Uses Y=100. */
	public XYZ XYZ () {
		return XYZ(100);
	}

	public XYZ XYZ (float Y) {
		return new xyY(x, y, Y).XYZ();
	}
}
