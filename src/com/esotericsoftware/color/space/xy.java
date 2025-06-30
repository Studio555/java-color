
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

	/** Improved CCT calculation with better accuracy at all temperature ranges.
	 * @return CCT [1000..100000K] or NaN out of range. */
	public CCT CCT () {
		if (x < 0.01f || x > 0.99f || y < 0.01f || y > 0.99f) return new CCT(Float.NaN);

		// Use uv for better numerical stability
		uv1960 uv = uv1960();
		float u = uv.u(), v = uv.v();

		// For initial guess, try multiple methods and pick best
		float K1, K2, K3;

		// Method 1: McCamy's formula
		float n = (x - 0.3320f) / (0.1858f - y);
		K1 = 449 * n * n * n + 3525 * n * n + 6823.3f * n + 5520.33f;

		// Method 2: Extended formula for all ranges
		float n2 = n * n;
		float n3 = n2 * n;
		if (n >= 0.24f && n <= 0.463f) {
			K2 = 437 * n3 + 3601 * n2 + 6861 * n + 5517;
		} else {
			// Hernandez-Andres formula for extended range
			float xe = x - 0.3366f;
			float ye = y - 0.1735f;
			float A0 = -949.86315f;
			float A1 = 6253.80338f;
			float t1 = 0.92159f;
			float A2 = 28.70599f;
			float t2 = 0.20039f;
			float A3 = 0.00004f;
			float t3 = 0.07125f;
			K2 = A0 + A1 * (float)Math.exp(-xe / t1) + A2 * (float)Math.exp(-xe / t2) + A3 * (float)Math.exp(-xe / t3);
		}

		// Method 3: Using u'v' coordinates for better stability at extremes
		float du = u - 0.292f; // Approximate u' at infinite temperature
		float dv = v - 0.24f; // Approximate v' at infinite temperature
		float slope = dv / du;
		if (Math.abs(du) > EPSILON) {
			// Approximation based on slope in u'v' space
			K3 = 10000f / (1.0f + 15f * slope * slope);
			if (slope < 0) K3 = 100000f - K3; // High temperatures have negative slope
		} else {
			K3 = K2;
		}

		// Choose best initial guess based on range
		float K;
		if (n >= -0.1f && n <= 0.5f) {
			K = K1; // McCamy is good in this range
		} else if (Math.abs(K2 - K3) < 5000) {
			K = (K2 + K3) / 2; // Average if they agree
		} else {
			// Pick the one that gives xy closest to our target
			CCT test2 = new CCT(K2);
			CCT test3 = new CCT(K3);
			xy xy2 = test2.xy();
			xy xy3 = test3.xy();
			float dist2 = (x - xy2.x()) * (x - xy2.x()) + (y - xy2.y()) * (y - xy2.y());
			float dist3 = (x - xy3.x()) * (x - xy3.x()) + (y - xy3.y()) * (y - xy3.y());
			K = dist2 < dist3 ? K2 : K3;
		}

		// Clamp to reasonable range
		K = Math.max(1000, Math.min(100000, K));

		// Newton-Raphson refinement with adaptive step size
		int maxIterations = 10;
		float tolerance = 0.0001f;
		float lastK = K;

		for (int i = 0; i < maxIterations; i++) {
			// Get current position on Planckian locus
			CCT cctObj = new CCT(K);
			xy xyPlanck = cctObj.xy();
			if (Float.isNaN(xyPlanck.x())) {
				// Fall back to computing from spectrum for extreme temperatures
				xyPlanck = cctObj.XYZ().xy();
			}
			uv1960 uvPlanck = xyPlanck.uv1960();

			// Distance in uv space
			float deltaU = u - uvPlanck.u();
			float deltaV = v - uvPlanck.v();
			float dist2 = deltaU * deltaU + deltaV * deltaV;

			// Check convergence
			if (dist2 < tolerance * tolerance) break;

			// Compute gradient by finite differences
			float h = K * 0.001f; // 0.1% step
			CCT cctNext = new CCT(K + h);
			xy xyNext = cctNext.xy();
			if (Float.isNaN(xyNext.x())) {
				xyNext = cctNext.XYZ().xy();
			}
			uv1960 uvNext = xyNext.uv1960();

			float ddu_dT = (uvNext.u() - uvPlanck.u()) / h;
			float ddv_dT = (uvNext.v() - uvPlanck.v()) / h;

			// Newton step
			float denominator = ddu_dT * ddu_dT + ddv_dT * ddv_dT;
			if (denominator < EPSILON) break;

			float step = (deltaU * ddu_dT + deltaV * ddv_dT) / denominator;

			// Adaptive damping to prevent overshooting
			float damping = 1.0f;
			if (Math.abs(step) > K * 0.5f) {
				damping = K * 0.5f / Math.abs(step);
			}

			K += step * damping;

			// Ensure K stays in valid range
			K = Math.max(1000, Math.min(100000, K));

			// Check if we're oscillating
			if (i > 0 && Math.abs(K - lastK) < 0.1f) break;
			lastK = K;
		}

		return new CCT(K);
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

	/** @return NaN if invalid. */
	public float Duv () {
		CCT cct = CCT();
		xy xyBB = cct.xy();
		uv1960 perp = CCT.perpendicular(cct.K(), xyBB), uvBB = xyBB.uv1960(), uv = uv1960();
		return (uv.u() - uvBB.u()) * perp.u() + (uv.v() - uvBB.v()) * perp.v();
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
