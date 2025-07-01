
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Gamut;
import com.esotericsoftware.color.Illuminant;
import com.esotericsoftware.color.Illuminant.CIE2;
import com.esotericsoftware.color.Util;

/** CIE 1931 chromaticity coordinates. */
public record xy (
	/** x chromaticity [0..1]. */
	float x,
	/** y chromaticity [0..1]. */
	float y) {

	/** @return [1000..100000K] or NaN out of range.
	 * @see uv#CCT(CCT.Method) */
	public CCT CCT (CCT.Method method) {
		return uv().CCT(method);
	}

	/** Uses {@link CCT.Method#Robertson}.
	 * @return [1000..100000K] or NaN out of range. */
	public CCT CCT () {
		return uv().CCT();
	}

	/** Uses {@link CIE2#D65}. */
	public Lab Lab () {
		return Lab(CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	public Lab Lab (XYZ tristimulus) {
		return XYZ().Lab(tristimulus);
	}

	/** Uses {@link CIE2#D65}. */
	public LCh LCh () {
		return LCh(CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	public LCh LCh (XYZ tristimulus) {
		return Lab(tristimulus).LCh();
	}

	/** Uses {@link CIE2#D65}.
	 * @return NaN if invalid. */
	public LCHuv LChuv () {
		return Luv().LCHuv();
	}

	/** Uses {@link CIE2#D65}.
	 * @return NaN if invalid. */
	public Luv Luv () {
		return XYZ().Luv(CIE2.D65);
	}

	/** @return NaN if invalid. */
	public Luv Luv (XYZ tristimulus) {
		return XYZ().Luv(tristimulus);
	}

	/** Uses {@link Gamut#sRGB}.
	 * @return Normalized or NaN if invalid. */
	public LinearRGB LinearRGB () {
		return Gamut.sRGB.LinearRGB(this);
	}

	/** Uses {@link Gamut#sRGB}.
	 * @return Normalized or NaN if invalid. */
	public RGB RGB () {
		return Gamut.sRGB.RGB(this);
	}

	/** Compares perceptual chromaticity.
	 * @return NaN if invalid. */
	public float MacAdamSteps (xy xy) {
		return uv().MacAdamSteps(xy.uv());
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
}
