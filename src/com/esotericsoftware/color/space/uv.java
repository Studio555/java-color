
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

	static private float[] KPlanckian;
	static private float[] uvPlanckian;

	/** Maximum error 1K [1000..7000], 2.7K [7000..20000], 3.52K [20000-100000].
	 * @return [1000..100000K] or NaN out of range. */
	public CCT CCT () {
		if (KPlanckian == null) PlanckianTable();
		float min = Float.MAX_VALUE;
		int i = 0;
		for (int t = 0; t < 1030; t += 2) {
			float dx = uvPlanckian[t] - u, dy = uvPlanckian[t + 1] - v, dist = dx * dx + dy * dy;
			if (dist < min) {
				min = dist;
				i = t;
			}
		}
		if (i == 0)
			i = 2;
		else if (i == 1028) //
			i = 1026;
		int k = i >> 1;
		float Kp = KPlanckian[k - 1], up = uvPlanckian[i - 2], vp = uvPlanckian[i - 1]; // 3 points.
		float Kn = KPlanckian[k + 1], un = uvPlanckian[i + 2], vn = uvPlanckian[i + 3];
		float dx = u - up, dy = v - vp, dp = (float)Math.sqrt(dx * dx + dy * dy);
		dx = u - un;
		dy = v - vn;
		float dn = (float)Math.sqrt(dx * dx + dy * dy);
		dx = up - un;
		dy = vp - vn;
		float side = (float)Math.sqrt(dx * dx + dy * dy), iside = 1f / side; // Triangular solution.
		float dist = (dp * dp - dn * dn + side * side) * 0.5f * iside, ds = dist * iside, Duv = dp * dp - dist * dist, K;
		if (Duv >= 0.000004f) { // 0.002^2
			float Ki = KPlanckian[k], ui = uvPlanckian[i], vi = uvPlanckian[i + 1];
			float denom = (Kn - Ki) * (Kp - Kn) * (Ki - Kp); // Parabolic solution.
			if (Math.abs(denom) < EPSILON) {
				K = Kp + (Kn - Kp) * ds;
				Duv = (float)Math.sqrt(Math.max(0, Duv));
			} else {
				dx = u - ui;
				dy = v - vi;
				float di = (float)Math.sqrt(dx * dx + dy * dy);
				float a = (Kp * (dn - di) + Ki * (dp - dn) + Kn * (di - dp)) / denom;
				float b = -(Kp * Kp * (dn - di) + Ki * Ki * (dp - dn) + Kn * Kn * (di - dp)) / denom;
				float c = -(dp * (Kn - Ki) * Ki * Kn + di * (Kp - Kn) * Kp * Kn + dn * (Ki - Kp) * Kp * Ki) / denom;
				K = -b / (2 * a);
				Duv = a * K * K + b * K + c;
			}
		} else {
			K = Kp + (Kn - Kp) * ds;
			Duv = (float)Math.sqrt(Duv);
		}
		if (K < 1000 || K > 100000) return new CCT(Float.NaN, Float.NaN);
		return new CCT(K, Duv * Math.signum(v - (vp + (vn - vp) * ds)));
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

	/** {@link Lab#deltaE2000(Lab, float, float, float)} with 1 for lightness, chroma, and hue. */
	public float deltaE2000 (uv other) {
		return XYZ().Lab().deltaE2000(other.XYZ().Lab(), 1, 1, 1);
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

	/** Ohno with 1.0134 spacing. */
	static private void PlanckianTable () {
		if (KPlanckian == null) {
			synchronized (uv.class) {
				if (KPlanckian == null) {
					float[] kTable = new float[515];
					float[] uvTable = new float[1030];
					kTable[0] = 1000;
					kTable[1] = 1001;
					float K = 1001, next = 1.0134f;
					for (int i = 2; i < 513; i++) {
						K *= next;
						kTable[i] = K;
						float D = clamp((K - 1000) / 99000);
						next = 1.0134f * (1 - D) + (1 + (1.0134f - 1) / 10) * D;
					}
					kTable[513] = 99999f;
					kTable[514] = 100000f;
					for (int i = 0; i < 515; i++) {
						uv uv = new CCT(kTable[i]).XYZ().uv();
						uvTable[i * 2] = uv.u();
						uvTable[i * 2 + 1] = uv.v();
					}
					KPlanckian = kTable;
					uvPlanckian = uvTable;
				}
			}
		}
	}
}
