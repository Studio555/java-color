
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Colors.*;

import com.esotericsoftware.color.Gamut;
import com.esotericsoftware.color.Colors;

/** CIE 1960 UCS chromaticity coordinates. */
public record uv1960 (
	/** u chromaticity [0..1]. */
	float u,
	/** v chromaticity [0..1]. */
	float v) implements Color {

	public LRGB LRGB () {
		return xy().LRGB();
	}

	/** Uses {@link Gamut#sRGB}.
	 * @return Normalized or NaN if invalid. */
	public RGB RGB () {
		return Gamut.sRGB.RGB(uv());
	}

	public uv uv () {
		return new uv(u, 1.5f * v);
	}

	public UVW UVW (float Y, uv1960 whitePoint) {
		float W = 25 * (float)Math.pow(Y, 1 / 3f) - 17;
		return new UVW(13 * W * (u - whitePoint.u), 13 * W * (v - whitePoint.v), W);
	}

	/** @return NaN if invalid. */
	public xy xy () {
		float denom = 2 + u - 4 * v;
		if (Math.abs(denom) < EPSILON) return new xy(Float.NaN, Float.NaN);
		return new xy(u * 1.5f / denom, v / denom);
	}

	public XYZ XYZ () {
		return xy().XYZ();
	}

	/** @return [0..360] */
	public float angle (uv1960 origin) {
		return (float)Math.atan2(v - origin.v(), u - origin.u()) * radDeg;
	}

	public uv1960 add (float value) {
		return new uv1960(u + value, v + value);
	}

	public uv1960 add (float u, float v) {
		return new uv1960(this.u + u, this.v + v);
	}

	public uv1960 add (uv1960 uv) {
		return new uv1960(u + uv.u, v + uv.v);
	}

	/** CIE 13.3 chromatic adaptation.
	 * @return This color adapted to appear under the target illuminant as it would under the source illuminant. */
	public uv1960 chromaticAdaptation (uv1960 sourceIlluminant, uv1960 targetIlluminant) {
		float us = sourceIlluminant.u(), vs = sourceIlluminant.v();
		float ut = targetIlluminant.u(), vt = targetIlluminant.v();
		float cs = (4 - us - 10 * vs) / vs, ds = (1.708f * vs + 0.404f - 1.481f * us) / vs;
		float ct = (4 - ut - 10 * vt) / vt, dt = (1.708f * vt + 0.404f - 1.481f * ut) / vt;
		float c = (4 - u - 10 * v) / v, d = (1.708f * v + 0.404f - 1.481f * u) / v;
		float cratio = ct / cs, dratio = dt / ds;
		float denom = 16.518f + 1.481f * cratio * c - dratio * d;
		return new uv1960((10.872f + 0.404f * cratio * c - 4 * dratio * d) / denom, 5.520f / denom);
	}

	public uv1960 lerp (uv1960 other, float t) {
		return new uv1960(Colors.lerp(u, other.u, t), Colors.lerp(v, other.v, t));
	}

	public uv1960 mid (uv1960 other) {
		return lerp(other, 0.5f);
	}

	public uv1960 nor () {
		float length = len();
		if (length == 0) return this;
		return new uv1960(u / length, v / length);
	}

	public uv1960 scl (float scalar) {
		return new uv1960(u * scalar, v * scalar);
	}

	public uv1960 sub (float value) {
		return new uv1960(u - value, v - value);
	}

	public uv1960 sub (float u, float v) {
		return new uv1960(this.u - u, this.v - v);
	}

	public uv1960 sub (uv1960 uv) {
		return new uv1960(u - uv.u, v - uv.v);
	}

	public float dst (uv1960 other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (uv1960 other) {
		float du = u - other.u, dv = v - other.v;
		return du * du + dv * dv;
	}

	public float len () {
		return (float)Math.sqrt(len2());
	}

	public float len2 () {
		return u * u + v * v;
	}

	@SuppressWarnings("all")
	public uv1960 uv1960 () {
		return this;
	}
}
