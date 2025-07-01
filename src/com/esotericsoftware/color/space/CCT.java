
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Spectrum;

public record CCT ( //
	/** [427..100000K] */
	float K,
	float Duv) {

	public CCT (float K) {
		this(K, 0);
	}

	/** Returns a reference illuminant spectrum for this CCT. Uses Planckian radiator for CCT < 5000K, CIE daylight for >= 5000K.
	 * @return 380-780nm @ 5nm, 81 values normalized to Y=100. */
	public Spectrum illuminant () {
		if (K < 5000) return Planckian(380, 780, 5);
		xy xy = xy();
		float x = xy.x(), y = xy.y();
		float M1 = (-1.3515f - 1.7703f * x + 5.9114f * y) / (0.0241f + 0.2562f * x - 0.7341f * y);
		float M2 = (0.0300f - 31.4424f * x + 30.0717f * y) / (0.0241f + 0.2562f * x - 0.7341f * y);
		float[] values = new float[81];
		for (int i = 0; i < 81; i++)
			values[i] = S0[i] + M1 * S1[i] + M2 * S2[i];
		return new Spectrum(values, 5).normalize();
	}

	public boolean invalid () {
		return Float.isNaN(K);
	}

	/** @return Normalized to Y=100. */
	public Spectrum Planckian (int start, int end, int step) {
		int length = ((end - start) / step) + 1;
		float[] values = new float[length];
		for (int i = 0; i < length; i++) {
			double lambda = (start + i * step) * 1e-9; // nm to meters.
			double exponent = XYZ.c2 / (lambda * K);
			values[i] = (float)(exponent > 80 ? 0
				: XYZ.c1 / (lambda * lambda * lambda * lambda * lambda * (Math.exp(exponent) - 1)));
		}
		return new Spectrum(values, step, start).normalize();
	}

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
	public RGBW RGBW (float brightness, LinearRGB w) {
		LinearRGB target = xy().LinearRGB();
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
	public RGBWW RGBWW (float brightness, LinearRGB w1, LinearRGB w2) {
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

	/** Uses exact Planck calculation [427..100000].
	 * @return Normalized with Y=100 or NaN if invalid. */
	public XYZ XYZ () {
		if (K < 427 || K > 100000) return new XYZ(Float.NaN, Float.NaN, Float.NaN);
		double X = 0, Y = 0, Z = 0;
		for (int i = 0; i < 81; i++) {
			double lambda = (380 + i * 5) * 1e-9; // nm to meters.
			double exponent = XYZ.c2 / (lambda * K);
			double B = exponent > 80 ? 0 : XYZ.c1 / (lambda * lambda * lambda * lambda * lambda * (Math.exp(exponent) - 1));
			X += B * XYZ.Xbar[i];
			Y += B * XYZ.Ybar[i];
			Z += B * XYZ.Zbar[i];
		}
		if (Y > 0) {
			double scale = 100f / Y;
			X *= scale;
			Z *= scale;
		}
		return new XYZ((float)X, 100, (float)Z);
	}

	/** {@link #xy(float)} with 0 Duv. Worst case accuracy is 0.00058 [1667-100000K] else uses exact Planck calculation
	 * [427..1667].
	 * @return NaN if invalid. */
	public xy xy () {
		if (K < 427 || K > 100000) return new xy(Float.NaN, Float.NaN);
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
			else // CCT > 4000
				y = 3.0817580f * xx * x - 5.87338670f * xx + 3.75112997f * x - 0.37001483f;
			return new xy(x, y);
		}
// if (K >= 1000) {
// float x, t = 1000f / K, t2 = t * t, t3 = t2 * t; // Use reciprocal temperature for better numerical stability
// if (K >= 1667 && K <= 4000) {
// // Standard Krystek approximation
// x = -0.2661239f * t3 - 0.2343589f * t2 + 0.8776956f * t + 0.179910f;
// } else if (K > 4000 && K <= 25000) {
// // Standard Krystek approximation
// x = -3.0258469f * t3 + 2.1070379f * t2 + 0.2226347f * t + 0.240390f;
// } else if (K > 25000 && K <= 50000) {
// // Extended approximation for high temperatures
// x = -4.6070f * t3 + 2.9678f * t2 + 0.09911f * t + 0.244063f;
// } else if (K > 50000) {
// // Very high temperature approximation
// x = -2.0064f * t3 + 1.9018f * t2 + 0.24748f * t + 0.237040f;
// } else {
// // K < 1667 - use polynomial fit
// float t4 = t3 * t;
// x = -0.0803f * t4 - 0.3903f * t3 - 0.2887f * t2 + 0.8810f * t + 0.17991f;
// }
// float y, xx = x * x, xxx = xx * x;
// if (K >= 1667 && K <= 2222) {
// y = -1.1063814f * xxx - 1.34811020f * xx + 2.18555832f * x - 0.20219683f;
// } else if (K > 2222 && K <= 4000) {
// y = -0.9549476f * xxx - 1.37418593f * xx + 2.09137015f * x - 0.16748867f;
// } else if (K > 4000 && K <= 25000) {
// y = 3.0817580f * xxx - 5.87338670f * xx + 3.75112997f * x - 0.37001483f;
// } else if (K > 25000 && K <= 50000) {
// // Extended approximation for high temperatures
// y = 2.870f * xxx - 5.503f * xx + 3.583f * x - 0.35986f;
// } else if (K > 50000) {
// // Very high temperature approximation
// y = 2.511f * xxx - 4.894f * xx + 3.234f * x - 0.33684f;
// } else {
// // K < 1667 - use polynomial fit
// y = -0.9267f * xxx - 1.2481f * xx + 2.1532f * x - 0.19834f;
// }
// return new xy(x, y);
// }
		return XYZ().xy();
	}

	/** @return NaN if {@link #K()} is outside [1667..25000K]. */
	public xy xy (float Duv) {
		if (K < 1667 || K > 25000) return new xy(Float.NaN, Float.NaN);
		xy xy = xy();
		if (Duv == 0 || Duv == this.Duv) return xy;
		uv1960 perp = perpendicular(K, xy), uv = xy.uv1960();
		return new uv1960(uv.u() + perp.u() * Duv, uv.v() + perp.v() * Duv).xy();
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

	/** CIE daylight basis function S0, 380-780nm @ 5nm. */
	static public final float[] S0 = {63.4f, 65.8f, 94.8f, 104.8f, 105.9f, 96.8f, 113.9f, 125.6f, 125.5f, 121.3f, 121.3f, 113.5f,
		113.1f, 110.8f, 106.5f, 108.8f, 105.3f, 104.4f, 100.0f, 96.0f, 95.1f, 89.1f, 90.5f, 90.3f, 88.4f, 84.0f, 85.1f, 81.9f,
		82.6f, 84.9f, 81.3f, 71.9f, 74.3f, 76.4f, 63.3f, 71.7f, 77.0f, 65.2f, 47.7f, 68.6f, 65.0f, 66.0f, 61.0f, 53.3f, 58.9f,
		61.9f, 62.0f, 62.0f, 58.0f, 52.0f, 55.0f, 56.0f, 58.0f, 60.0f, 56.0f, 55.0f, 52.0f, 50.0f, 49.0f, 50.0f, 53.0f, 54.0f,
		53.0f, 51.0f, 50.0f, 48.0f, 46.0f, 46.0f, 47.0f, 47.0f, 46.0f, 45.0f, 44.0f, 44.0f, 44.0f, 44.0f, 44.0f, 43.0f, 43.0f,
		43.0f, 42.0f};

	/** CIE daylight basis function S1, 380-780nm @ 5nm. */
	static public final float[] S1 = {38.5f, 35.0f, 43.4f, 46.3f, 43.9f, 37.1f, 36.7f, 35.9f, 32.6f, 27.9f, 24.3f, 20.1f, 16.2f,
		13.2f, 8.6f, 6.1f, 4.2f, 1.9f, 0.0f, -1.6f, -3.5f, -3.5f, -5.8f, -7.2f, -8.6f, -9.5f, -10.9f, -10.7f, -12.0f, -14.0f,
		-13.6f, -12.0f, -13.3f, -12.9f, -10.6f, -11.6f, -12.2f, -10.2f, -7.8f, -11.2f, -10.4f, -10.6f, -9.7f, -8.3f, -9.3f, -9.8f,
		-9.0f, -9.2f, -8.6f, -7.4f, -8.0f, -8.1f, -8.4f, -8.7f, -8.0f, -7.9f, -7.4f, -7.0f, -6.9f, -7.0f, -7.4f, -7.6f, -7.5f,
		-7.2f, -7.1f, -6.7f, -6.4f, -6.4f, -6.5f, -6.5f, -6.4f, -6.2f, -6.1f, -6.1f, -6.1f, -6.1f, -6.0f, -6.0f, -6.0f, -5.9f,
		-5.8f};

	/** CIE daylight basis function S2, 380-780nm @ 5nm. */
	static public final float[] S2 = {3.0f, 1.2f, -1.1f, -0.5f, -0.7f, -1.2f, -2.6f, -2.9f, -2.8f, -2.6f, -2.6f, -1.8f, -1.5f,
		-1.3f, -1.2f, -1.0f, -0.5f, -0.3f, 0.0f, 0.2f, 0.5f, 2.1f, 3.2f, 4.1f, 4.7f, 5.1f, 6.7f, 7.3f, 8.6f, 9.8f, 10.2f, 8.3f,
		9.6f, 8.5f, 7.0f, 7.6f, 8.0f, 6.7f, 5.2f, 7.4f, 6.8f, 7.0f, 6.4f, 5.5f, 6.1f, 6.5f, 6.5f, 6.4f, 6.0f, 5.4f, 5.7f, 5.8f,
		6.0f, 6.2f, 5.8f, 5.7f, 5.4f, 5.2f, 5.1f, 5.2f, 5.5f, 5.6f, 5.5f, 5.3f, 5.2f, 5.0f, 4.8f, 4.8f, 4.9f, 4.9f, 4.8f, 4.7f,
		4.6f, 4.6f, 4.6f, 4.6f, 4.6f, 4.5f, 4.5f, 4.5f, 4.4f, 4.3f};
}
