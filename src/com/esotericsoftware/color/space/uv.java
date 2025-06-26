
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Illuminant;
import com.esotericsoftware.color.Illuminant.CIE2;
import com.esotericsoftware.color.Util;

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

	/** Uses {@link CIE2#D65}. */
	public Lab Lab () {
		return Lab(CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	public Lab Lab (XYZ tristimulus) {
		return XYZ().Lab(tristimulus);
	}

	/** @return Normalized. */
	public LinearRGB LinearRGB () {
		xy xy = xy();
		return new xyY(xy.x(), xy.y(), 1).XYZ().LinearRGB().nor();
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

	/** @return Normalized. */
	public RGB RGB () {
		xy xy = xy();
		return new xyY(xy.x(), xy.y(), 1).XYZ().RGB().nor();
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

	/** @return [0..360] */
	public float angle (uv origin) {
		return (float)Math.atan2(v - origin.v(), u - origin.u()) * radDeg;
	}

	public uv add (float value) {
		return new uv(u + value, v + value);
	}

	public uv add (float u, float v) {
		return new uv(this.u + u, this.v + v);
	}

	public uv add (uv uv) {
		return new uv(u + uv.u, v + uv.v);
	}

	public uv lerp (uv other, float t) {
		return new uv(Util.lerp(u, other.u, t), Util.lerp(v, other.v, t));
	}

	public uv mid (uv other) {
		return lerp(other, 0.5f);
	}

	public uv sub (float value) {
		return new uv(u - value, v - value);
	}

	public uv sub (float u, float v) {
		return new uv(this.u - u, this.v - v);
	}

	public uv sub (uv uv) {
		return new uv(u - uv.u, v - uv.v);
	}

	/** {@link Lab#deltaE2000(Lab, float, float, float)} with 1 for lightness, chroma, and hue. */
	public float deltaE2000 (uv other) {
		return XYZ().Lab().deltaE2000(other.XYZ().Lab(), 1, 1, 1);
	}

	public float dst (uv other) {
		float du = u - other.u, dv = v - other.v;
		return (float)Math.sqrt(du * du + dv * dv);
	}

	public float dst2 (uv other) {
		float du = u - other.u, dv = v - other.v;
		return du * du + dv * dv;
	}

	/** @return NaN if invalid. */
	public float Duv () {
		return xy().Duv();
	}

	/** Compares perceptual chromaticity. */
	public float MacAdamSteps (uv uv) {
		return dst(uv) / 0.0011f;
	}
}
