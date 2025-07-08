
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

	/** Uses {@link CCT.Method#RobertsonImproved}.
	 * @return [1000K+] or NaN out of range. */
	public CCT CCT () {
		return CCT_Robertson(CCT.RobertsonImproved, 645);
	}

	public CCT CCT (CCT.Method method) {
		return switch (method) {
		case RobertsonImproved -> CCT_Robertson(CCT.RobertsonImproved, 645);
		case Robertson1968 -> CCT_Robertson(CCT.Robertson1968, 150);
		case Ohno2013 -> CCT_Ohno();
		};
	}

	private CCT CCT_Robertson (float[] Robertson, int last) {
		float u = this.u, v = this.v / 1.5f, pdt = 0;
		for (int i = 5;; i += 5) {
			float cu = Robertson[i + 1], cv = Robertson[i + 2], du = Robertson[i + 3], dv = Robertson[i + 4];
			float dt = (v - cv) * du - (u - cu) * dv;
			if (i >= 565) dt = -dt;
			if (dt <= 0 || i == last) {
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

	private CCT CCT_Ohno () {
		CCT.PlanckianTable();
		float[] KPlanckian = CCT.KPlanckian, uvPlanckian = CCT.uvPlanckian;
		float u = this.u, v = this.v / 1.5f, min = Float.MAX_VALUE;
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
		float side = (float)Math.sqrt(dx * dx + dy * dy), iside = 1 / side; // Triangular solution.
		float dist = (dp * dp - dn * dn + side * side) * 0.5f * iside, ds = dist * iside, Duv = dp * dp - dist * dist, K;
		if (Duv >= 0.000004f) { // 0.002^2, try parabolic solution.
			float Ki = KPlanckian[k], ui = uvPlanckian[i], vi = uvPlanckian[i + 1];
			float denom = (Kn - Ki) * (Kp - Kn) * (Ki - Kp);
			if (Math.abs(denom) >= EPSILON) {
				dx = u - ui;
				dy = v - vi;
				float di = (float)Math.sqrt(dx * dx + dy * dy), a = (Kp * (dn - di) + Ki * (dp - dn) + Kn * (di - dp)) / denom;
				if (a != 0) {
					float b = -(Kp * Kp * (dn - di) + Ki * Ki * (dp - dn) + Kn * Kn * (di - dp)) / denom;
					float c = -(dp * (Kn - Ki) * Ki * Kn + di * (Kp - Kn) * Kp * Kn + dn * (Ki - Kp) * Kp * Ki) / denom;
					K = -b / (2 * a);
					Duv = a * K * K + b * K + c;
					return new CCT(K, Duv * Math.signum(v - (vp + (vn - vp) * ds)));
				}
			}
		}
		K = Kp + (Kn - Kp) * ds;
		Duv = (float)Math.sqrt(Math.max(0, Duv));
		return new CCT(K, Duv * Math.signum(v - (vp + (vn - vp) * ds)));
	}

	/** @return Normalized. */
	public LinearRGB LinearRGB () {
		xy xy = xy();
		return new xyY(xy.x(), xy.y(), 1).LinearRGB().nor();
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
