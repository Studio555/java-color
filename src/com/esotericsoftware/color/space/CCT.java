
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

public record CCT (
	/** [427..100000K] */
	float K) {

	/** {@link #RGB(float)} with 0 Duv.
	 * @return NaN if invalid. */
	public RGB RGB () {
		return xy().RGB();
	}

	/** @return NaN if invalid. */
	public RGB RGB (float Duv) {
		return xy(Duv).RGB();
	}

	/** Convert to RGBW using one calibrated white LED color. Brightness is maximized.
	 * @param brightness [0..1]
	 * @param w White LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @return NaN if invalid. */
	public RGBW RGBW (float brightness, RGB w) {
		RGB target = RGB();
		float W = 1;
		float r = Math.max(0, target.r() - W * w.r());
		float g = Math.max(0, target.g() - W * w.g());
		float b = Math.max(0, target.b() - W * w.b());
		float total = r + g + b + W;
		if (total > brightness) {
			float excess = total - brightness;
			// Reduce RGB proportionally.
			float sum = r + g + b;
			if (sum > 0 && excess <= sum) { // Achieve target by only reducing RGB.
				float scale = (sum - excess) / sum;
				r *= scale;
				g *= scale;
				b *= scale;
			} else { // Need to also reduce white.
				r = g = b = 0;
				W = brightness;
			}
		} else {
			float scale = brightness / total;
			r *= scale;
			g *= scale;
			b *= scale;
			W *= scale;
		}
		if (r > 1) r = 1;
		if (g > 1) g = 1;
		if (b > 1) b = 1;
		if (W > 1) W = 1;
		return new RGBW(r, g, b, W);
	}

	/** Convert to RGBWW using two calibrated white LED colors. Brightness is maximized.
	 * @param brightness [0..1]
	 * @param w1 First white LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @param w2 Second white LED color.
	 * @return NaN if invalid. */
	public RGBWW RGBWW (float brightness, RGB w1, RGB w2) {
		float K1 = w1.uv().CCT().K;
		float K2 = w2.uv().CCT().K;
		float W1, W2;
		if (Math.abs(K2 - K1) < EPSILON) // Both whites have same CCT.
			W1 = W2 = 0.5f;
		else {
			float ratio = clamp((K - K1) / (K2 - K1));
			W1 = 1 - ratio;
			W2 = ratio;
		}
		RGB target = RGB();
		float r = Math.max(0, target.r() - (W1 * w1.r() + W2 * w2.r()));
		float g = Math.max(0, target.g() - (W1 * w1.g() + W2 * w2.g()));
		float b = Math.max(0, target.b() - (W1 * w1.b() + W2 * w2.b()));
		float total = r + g + b + W1 + W2;
		if (total > brightness) {
			float excess = total - brightness;
			// Reduce RGB proportionally.
			float sum = r + g + b;
			if (sum > 0 && excess <= sum) { // Achieve target by only reducing RGB.
				float scale = (sum - excess) / sum;
				r *= scale;
				g *= scale;
				b *= scale;
			} else { // Need to also reduce white.
				r = g = b = 0;
				float scale = brightness / (W1 + W2);
				W1 *= scale;
				W2 *= scale;
			}
		} else {
			float scale = brightness / total;
			r *= scale;
			g *= scale;
			b *= scale;
			W1 *= scale;
			W2 *= scale;
		}
		if (r > 1) r = 1;
		if (g > 1) g = 1;
		if (b > 1) b = 1;
		if (W1 > 1) W1 = 1;
		if (W2 > 1) W2 = 1;
		return new RGBWW(r, g, b, W1, W2);
	}

	/** {@link #uv(float)} with 0 Duv.
	 * @return NaN if invalid. */
	public uv uv () {
		return xy().uv();
	}

	/** @return NaN if invalid. */
	public uv uv (float Duv) {
		return xy(Duv).uv();
	}

	/** Uses exact Planck calculation [427..1667].
	 * @return Normalized with Y=100 or NaN if invalid. */
	public XYZ XYZ () {
		if (K < 100 || K > 100000) return new XYZ(Float.NaN, Float.NaN, Float.NaN);
		float X = 0, Y = 0, Z = 0;
		for (int i = 0; i < 81; i++) {
			float lambda = (380 + i * 5) * 1e-9f; // nm to meters.
			float exponent = XYZ.c2 / (lambda * K);
			float B = exponent > 80 ? 0 : XYZ.c1 / (lambda * lambda * lambda * lambda * lambda * ((float)Math.exp(exponent) - 1f));
			X += B * XYZ.Xbar[i];
			Y += B * XYZ.Ybar[i];
			Z += B * XYZ.Zbar[i];
		}
		if (Y > 0) {
			float scale = 100f / Y;
			X *= scale;
			Z *= scale;
		}
		return new XYZ(X, 100, Z);
	}

	/** {@link #xy(float)} with 0 Duv. Worst case accuracy is 0.00058 [1667-100000K] else uses exact Planck calculation
	 * [427..1667].
	 * @return NaN if invalid. */
	public xy xy () {
		if (K < 100 || K > 100000) return new xy(Float.NaN, Float.NaN);
		if (K >= 1667) {
			float x, t2 = K * K; // Krystek's approximation.
			if (K >= 1667 && K <= 4000)
				x = -0.2661239f * 1e9f / (t2 * K) - 0.2343589f * 1e6f / t2 + 0.8776956f * 1e3f / K + 0.179910f;
			else // CCT > 4000 && CCT <= 25000
				x = -3.0258469f * 1e9f / (t2 * K) + 2.1070379f * 1e6f / t2 + 0.2226347f * 1e3f / K + 0.240390f;
			float y, xx = x * x;
			if (K >= 1667 && K <= 2222)
				y = -1.1063814f * xx * x - 1.34811020f * xx + 2.18555832f * x - 0.20219683f;
			else if (K > 2222 && K <= 4000)
				y = -0.9549476f * xx * x - 1.37418593f * xx + 2.09137015f * x - 0.16748867f;
			else // CCT > 4000 && CCT <= 25000
				y = 3.0817580f * xx * x - 5.87338670f * xx + 3.75112997f * x - 0.37001483f;
			return new xy(x, y);
		}
		return XYZ().xy();
	}

	/** @return NaN if {@link #K()} is outside [1667..25000K]. */
	public xy xy (float Duv) {
		if (K < 1667 || K > 25000) return new xy(Float.NaN, Float.NaN);
		xy xy = xy();
		if (Duv == 0) return xy;
		uv1960 perp = perpendicular(K, xy), uvBB = xy.uv1960();
		return new uv1960(uvBB.u() + perp.u() * Duv, uvBB.v() + perp.v() * Duv).xy();
	}

	static uv1960 perpendicular (float K, xy xyPoint) {
		float x = xyPoint.x(), y = xyPoint.y();
		float dx_dT, dy_dx, t2 = K * K;
		if (K <= 4000)
			dx_dT = 0.2661239f * 3e9f / (t2 * t2) + 0.2343589f * 2e6f / t2 * K - 0.8776956f * 1e3f / t2;
		else
			dx_dT = 3.0258469f * 3e9f / (t2 * t2) - 2.1070379f * 2e6f / t2 * K - 0.2226347f * 1e3f / t2;
		if (K <= 2222)
			dy_dx = -3.3191442f * x * x - 2.69622040f * x + 2.18555832f;
		else if (K <= 4000)
			dy_dx = -2.8648428f * x * x - 2.74837186f * x + 2.09137015f;
		else
			dy_dx = 9.2452740f * x * x - 11.74677340f * x + 3.75112997f;
		float dy_dT = dy_dx * dx_dT;
		float denom2 = -2 * x + 12 * y + 3;
		denom2 *= denom2;
		float du_dx = 4 * (12 * y + 3) / denom2, du_dy = -4 * x * 12 / denom2;
		float dv_dx = 6 * y * 2 / denom2, dv_dy = 6 * (-2 * x + 3) / denom2;
		float du_dT = du_dx * dx_dT + du_dy * dy_dT, dv_dT = dv_dx * dx_dT + dv_dy * dy_dT;
		float length = (float)Math.sqrt(du_dT * du_dT + dv_dT * dv_dT);
		if (length < EPSILON) return new uv1960(0, 0);
		// Ensure consistent orientation: positive Duv should increase v (towards green)
		float perp_u = -dv_dT / length;
		float perp_v = du_dT / length;
		// If perp_v is negative, flip the vector to maintain consistent orientation
		if (perp_v < 0) {
			perp_u = -perp_u;
			perp_v = -perp_v;
		}
		return new uv1960(perp_u, perp_v);
	}
}
