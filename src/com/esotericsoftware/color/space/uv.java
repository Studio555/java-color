
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Gamut;
import com.esotericsoftware.color.Util;

/** CIE 1976 u'v' chromaticity coordinates. */
public record uv (
	/** u' chromaticity [0..1]. */
	float u,
	/** v' chromaticity [0..1]. */
	float v) implements Color {

	/** @return [1000K+] or NaN out of range. */
	public CCT CCT () {
		float[] Robertson = CCT.Robertson;
		float u = this.u, v = this.v / 1.5f, pdt = 0;
		for (int i = 5;; i += 5) {
			float cu = Robertson[i + 1], cv = Robertson[i + 2], du = Robertson[i + 3], dv = Robertson[i + 4];
			float dt = (v - cv) * du - (u - cu) * dv;
			if (i >= 565) dt = -dt;
			if (dt <= 0 || i == 645) {
				float pu = Robertson[i - 4], pv = Robertson[i - 3], pdu = Robertson[i - 2], pdv = Robertson[i - 1];
				dt = -Math.min(dt, 0);
				if (i == 5) { // 100000K to infinity.
					pdt = (v - pv) * pdu - (u - pu) * pdv;
					if (pdt <= 0) {
						float length = (float)Math.sqrt(pdu * pdu + pdv * pdv);
						return new CCT(Float.POSITIVE_INFINITY, ((pu - u) * pdu + (pv - v) * pdv) / length);
					}
				}
				float f = dt / (pdt + dt), fc = 1 - f;
				if (i == 565) {
					pdu = -pdu;
					pdv = -pdv;
				}
				du = du * fc + pdu * f;
				dv = dv * fc + pdv * f;
				float length = (float)Math.sqrt(du * du + dv * dv);
				if (i >= 565) length = -length;
				return new CCT(1e6f / (Robertson[i] * fc + Robertson[i - 5] * f),
					((cu * fc + pu * f - u) * du + (cv * fc + pv * f - v) * dv) / length);
			}
			pdt = dt;
		}
	}

	/** @return Normalized. */
	public LRGB LRGB () {
		xy xy = xy();
		return new xyY(xy.x(), xy.y(), 1).LRGB().nor();
	}

	/** Uses {@link Gamut#sRGB}.
	 * @return Normalized or NaN if invalid. */
	public RGB RGB () {
		return Gamut.sRGB.RGB(this);
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

	/** Compares perceptual chromaticity. */
	public float MacAdamSteps (uv uv) {
		return dst(uv) / 0.0011f;
	}

	public uv mid (uv other) {
		return lerp(other, 0.5f);
	}

	public uv nor () {
		float length = len();
		if (length == 0) return this;
		return new uv(u / length, v / length);
	}

	public uv scl (float scalar) {
		return new uv(u * scalar, v * scalar);
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

	public float dst (uv other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (uv other) {
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
	public uv uv () {
		return this;
	}
}
