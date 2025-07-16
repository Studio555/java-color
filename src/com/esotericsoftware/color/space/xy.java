
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Gamut;
import com.esotericsoftware.color.Util;

/** CIE 1931 chromaticity coordinates. */
public record xy (
	/** x chromaticity [0..1]. */
	float x,
	/** y chromaticity [0..1]. */
	float y) implements Color {

	/** Uses {@link Gamut#sRGB}.
	 * @return Normalized or NaN if invalid. */
	public LRGB LRGB () {
		return Gamut.sRGB.LRGB(this);
	}

	/** Uses {@link Gamut#sRGB}.
	 * @return Normalized or NaN if invalid. */
	public RGB RGB () {
		return Gamut.sRGB.RGB(this);
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

	/** @return [0..360] */
	public float angle (xy origin) {
		return (float)Math.atan2(y - origin.y(), x - origin.x()) * radDeg;
	}

	public xy add (float value) {
		return new xy(x + value, y + value);
	}

	public xy add (float u, float v) {
		return new xy(this.x + u, this.y + v);
	}

	public xy add (xy xy) {
		return new xy(x + xy.x, y + xy.y);
	}

	public xy lerp (xy other, float t) {
		return new xy(Util.lerp(x, other.x, t), Util.lerp(y, other.y, t));
	}

	public xy mid (xy other) {
		return lerp(other, 0.5f);
	}

	public xy nor () {
		float length = len();
		if (length == 0) return this;
		return new xy(x / length, y / length);
	}

	public xy scl (float scalar) {
		return new xy(x * scalar, y * scalar);
	}

	public xy sub (float value) {
		return new xy(x - value, y - value);
	}

	public xy sub (float u, float v) {
		return new xy(this.x - u, this.y - v);
	}

	public xy sub (xy xy) {
		return new xy(x - xy.x, y - xy.y);
	}

	public float dst (xy other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (xy other) {
		float dx = x - other.x, dy = y - other.y;
		return dx * dx + dy * dy;
	}

	public float len () {
		return (float)Math.sqrt(len2());
	}

	public float len2 () {
		return x * x + y * y;
	}

	@SuppressWarnings("all")
	public xy xy () {
		return this;
	}
}
