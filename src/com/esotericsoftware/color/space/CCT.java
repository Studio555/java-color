
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Spectrum;

public record CCT ( //
	/** [427..100000K] */
	float K,
	float Duv) {

	static float[] KPlanckian, uvPlanckian;

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

	/** Uses exact Planck calculation [427..100000K].
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

	/** Ohno (2013) with 1.0134 spacing. */
	static void PlanckianTable () {
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

	/** Improved Robertson isotemperature lines: larger LUT (130*5) with adaptive increments [1000..100000K], precomputed
	 * direction. */
	static public final float[] Robertson = { // mired, u, v, slope, du, dv
		0, 0.18006f, 0.26352f, 0.9716304f, -0.23650457f, // infinity K
		10.002358f, 0.1806384f, 0.2659509f, 0.96886784f, -0.24757847f, // 99976.42 K
		10.160177f, 0.18064836f, 0.2659898f, 0.9688213f, -0.24776046f, // 98423.48 K
		10.330461f, 0.18065913f, 0.2660319f, 0.9687709f, -0.24795745f, // 96801.1 K
		10.51417f, 0.18067077f, 0.26607737f, 0.9687164f, -0.24817069f, // 95109.74 K
		10.71234f, 0.18068336f, 0.26612645f, 0.9686572f, -0.24840157f, // 93350.28 K
		10.926082f, 0.18069696f, 0.2661795f, 0.968593f, -0.24865156f, // 91524.12 K
		11.1565895f, 0.18071169f, 0.2662368f, 0.96852344f, -0.24892235f, // 89633.125 K
		11.405147f, 0.1807276f, 0.2662987f, 0.96844804f, -0.24921568f, // 87679.71 K
		11.673132f, 0.18074483f, 0.26636556f, 0.9683661f, -0.24953346f, // 85666.81 K
		11.9620285f, 0.18076345f, 0.2664378f, 0.96827734f, -0.24987788f, // 83597.86 K
		12.273426f, 0.1807836f, 0.26651582f, 0.9681809f, -0.2502512f, // 81476.84 K
		12.609034f, 0.1808054f, 0.26660013f, 0.9680762f, -0.250656f, // 79308.22 K
		12.97069f, 0.180829f, 0.2666912f, 0.9679624f, -0.25109506f, // 77096.9 K
		13.360368f, 0.18085456f, 0.2667896f, 0.96783864f, -0.25157142f, // 74848.24 K
		13.780186f, 0.18088223f, 0.26689592f, 0.96770424f, -0.25208846f, // 72567.96 K
		14.232422f, 0.18091218f, 0.26701078f, 0.9675578f, -0.2526498f, // 70262.11 K
		14.719522f, 0.18094465f, 0.26713493f, 0.96739835f, -0.2532596f, // 67936.984 K
		15.244118f, 0.18097983f, 0.26726913f, 0.96722466f, -0.25392225f, // 65599.07 K
		15.809029f, 0.18101797f, 0.26741418f, 0.96703523f, -0.25464278f, // 63254.992 K
		16.417292f, 0.18105933f, 0.26757103f, 0.96682847f, -0.25542656f, // 60911.387 K
		17.06349f, 0.18110362f, 0.26773834f, 0.96660566f, -0.2562683f, // 58604.66 K
		17.744291f, 0.18115066f, 0.26791543f, 0.9663675f, -0.2571652f, // 56356.152 K
		18.461544f, 0.18120064f, 0.26810288f, 0.9661125f, -0.25812134f, // 54166.65 K
		19.21719f, 0.18125378f, 0.26830134f, 0.9658395f, -0.2591411f, // 52036.746 K
		20.013271f, 0.18131028f, 0.2685115f, 0.96554685f, -0.26022914f, // 49966.844 K
		20.851944f, 0.1813704f, 0.26873407f, 0.9652331f, -0.2613907f, // 47957.16 K
		21.735481f, 0.18143442f, 0.26896986f, 0.9648962f, -0.26263127f, // 46007.723 K
		22.666271f, 0.18150261f, 0.26921967f, 0.9645344f, -0.26395696f, // 44118.418 K
		23.646833f, 0.18157527f, 0.2694844f, 0.9641454f, -0.26537427f, // 42288.96 K
		24.67982f, 0.18165274f, 0.26976502f, 0.9637268f, -0.26689035f, // 40518.938 K
		25.867485f, 0.18174301f, 0.2700898f, 0.9632344f, -0.26866248f, // 38658.57 K
		27.106613f, 0.18183856f, 0.2704311f, 0.9627075f, -0.2705444f, // 36891.367 K
		28.399431f, 0.18193975f, 0.27078977f, 0.9621434f, -0.27254373f, // 35211.973 K
		29.748264f, 0.182047f, 0.2711668f, 0.96153903f, -0.27466848f, // 33615.406 K
		31.155539f, 0.1821607f, 0.2715631f, 0.9608909f, -0.27692735f, // 32097.021 K
		32.62378f, 0.18228136f, 0.27197978f, 0.9601952f, -0.27932972f, // 30652.488 K
		34.155632f, 0.18240944f, 0.27241787f, 0.9594481f, -0.2818855f, // 29277.748 K
		35.753845f, 0.1825455f, 0.2728786f, 0.9586448f, -0.28460532f, // 27969.02 K
		37.421295f, 0.18269013f, 0.27336305f, 0.9577804f, -0.2875007f, // 26722.752 K
		39.16098f, 0.18284397f, 0.27387258f, 0.9568495f, -0.29058382f, // 25535.623 K
		40.97603f, 0.18300772f, 0.27440846f, 0.955846f, -0.29386777f, // 24404.512 K
		42.869698f, 0.18318212f, 0.27497205f, 0.9547635f, -0.29736647f, // 23326.5 K
		44.845398f, 0.183368f, 0.27556482f, 0.9535942f, -0.3010948f, // 22298.832 K
		46.906677f, 0.18356627f, 0.27618822f, 0.9523304f, -0.30506864f, // 21318.926 K
		49.057247f, 0.18377788f, 0.27684382f, 0.95096296f, -0.30930483f, // 20384.348 K
		51.29458f, 0.18400325f, 0.27753118f, 0.9494863f, -0.31380832f, // 19495.238 K
		53.617355f, 0.18424292f, 0.27825028f, 0.9478939f, -0.31858608f, // 18650.678 K
		56.028828f, 0.18449795f, 0.2790024f, 0.94617534f, -0.32365453f, // 17847.955 K
		58.53237f, 0.18476947f, 0.27978885f, 0.9443191f, -0.3290309f, // 17084.562 K
		61.131493f, 0.1850587f, 0.28061098f, 0.942313f, -0.33473304f, // 16358.181 K
		63.82983f, 0.18536696f, 0.28147006f, 0.94014317f, -0.34077963f, // 15666.656 K
		66.63117f, 0.18569571f, 0.28236744f, 0.93779474f, -0.34718996f, // 15007.99 K
		69.53944f, 0.18604645f, 0.2833044f, 0.9352516f, -0.35398394f, // 14380.329 K
		72.55869f, 0.18642084f, 0.2842822f, 0.93249536f, -0.36118186f, // 13781.946 K
		75.693184f, 0.18682066f, 0.28530207f, 0.92950684f, -0.3688047f, // 13211.2295 K
		78.94729f, 0.18724781f, 0.28636518f, 0.9262647f, -0.37687352f, // 12666.68 K
		82.32557f, 0.18770435f, 0.28747258f, 0.9227456f, -0.38540962f, // 12146.895 K
		85.83276f, 0.18819244f, 0.28862533f, 0.9189242f, -0.39443418f, // 11650.5625 K
		89.473785f, 0.18871446f, 0.28982425f, 0.91477305f, -0.40396816f, // 11176.458 K
		93.253716f, 0.18927287f, 0.29107016f, 0.91026235f, -0.4140319f, // 10723.434 K
		97.17787f, 0.18987034f, 0.29236367f, 0.9053599f, -0.42464492f, // 10290.408 K
		101.25174f, 0.19050972f, 0.29370525f, 0.90003127f, -0.43582553f, // 9876.373 K
		105.48104f, 0.19119401f, 0.29509515f, 0.89423877f, -0.44759023f, // 9480.377 K
		109.87169f, 0.19192639f, 0.2965334f, 0.8879429f, -0.45995364f, // 9101.525 K
		114.42984f, 0.19271024f, 0.29801992f, 0.8811013f, -0.47292763f, // 8738.9795 K
		119.16188f, 0.19354908f, 0.29955417f, 0.8736691f, -0.4865207f, // 8391.945 K
		124.07443f, 0.19444668f, 0.30113557f, 0.8655991f, -0.5007377f, // 8059.678 K
		129.17438f, 0.19540694f, 0.30276307f, 0.85684216f, -0.5155788f, // 7741.473 K
		134.46889f, 0.19643395f, 0.3044354f, 0.8473473f, -0.531039f, // 7436.6646 K
		139.96535f, 0.19753198f, 0.30615097f, 0.83706254f, -0.5471073f, // 7144.6255 K
		145.6488f, 0.19870077f, 0.3079009f, 0.82597995f, -0.5636995f, // 6865.8306 K
		151.50146f, 0.19993897f, 0.30967546f, 0.81410563f, -0.5807167f, // 6600.596 K
		157.52835f, 0.20124976f, 0.31147188f, 0.80140644f, -0.59812015f, // 6348.0635 K
		163.73459f, 0.20263633f, 0.31328714f, 0.78785217f, -0.61586446f, // 6107.445 K
		170.12547f, 0.2041019f, 0.31511804f, 0.7734166f, -0.6338981f, // 5878.0146 K
		176.70645f, 0.2056497f, 0.31696126f, 0.7580784f, -0.6521633f, // 5659.103 K
		183.48314f, 0.20728296f, 0.31881326f, 0.7418222f, -0.6705967f, // 5450.0923 K
		190.46132f, 0.2090049f, 0.3206704f, 0.724639f, -0.68912864f, // 5250.41 K
		197.64696f, 0.21081874f, 0.32252884f, 0.706528f, -0.70768505f, // 5059.5264 K
		205.0462f, 0.21272767f, 0.3243847f, 0.68749696f, -0.7261872f, // 4876.9497 K
		212.66536f, 0.21473484f, 0.32623395f, 0.66756296f, -0.7445534f, // 4702.223 K
		220.51093f, 0.21684337f, 0.32807252f, 0.6467533f, -0.7626992f, // 4534.923 K
		228.5896f, 0.21905631f, 0.32989624f, 0.62510616f, -0.7805398f, // 4374.6523 K
		236.90828f, 0.2213767f, 0.33170092f, 0.60267013f, -0.7979904f, // 4221.043 K
		245.47408f, 0.22380745f, 0.33348233f, 0.57950526f, -0.8149685f, // 4073.75 K
		254.29433f, 0.22635145f, 0.33523628f, 0.5556817f, -0.83139515f, // 3932.4512 K
		263.37656f, 0.22901145f, 0.33695856f, 0.53128f, -0.8471962f, // 3796.8452 K
		272.7285f, 0.23179011f, 0.33864498f, 0.50638974f, -0.8623047f, // 3666.6504 K
		282.35815f, 0.23469001f, 0.34029147f, 0.48110834f, -0.8766611f, // 3541.6013 K
		292.27377f, 0.23771359f, 0.341894f, 0.45553976f, -0.89021546f, // 3421.4497 K
		302.4838f, 0.24086313f, 0.3434487f, 0.42979273f, -0.9029276f, // 3305.9622 K
		312.99698f, 0.24414082f, 0.3449518f, 0.40397906f, -0.9147682f, // 3194.919 K
		323.82224f, 0.24754864f, 0.3463997f, 0.37821144f, -0.9257192f, // 3088.114 K
		334.9689f, 0.25108844f, 0.347789f, 0.3526019f, -0.93577343f, // 2985.3518 K
		346.4465f, 0.25476184f, 0.34911644f, 0.32725948f, -0.9449345f, // 2886.4485 K
		358.26477f, 0.2585703f, 0.35037908f, 0.30228895f, -0.9532164f, // 2791.2317 K
		370.43384f, 0.26251513f, 0.3515742f, 0.27778906f, -0.9606421f, // 2699.5374 K
		382.96414f, 0.2665973f, 0.3526994f, 0.25385097f, -0.9672434f, // 2611.2104 K
		395.86633f, 0.27081764f, 0.35375246f, 0.23055771f, -0.9730587f, // 2526.1052 K
		409.15143f, 0.2751767f, 0.35473162f, 0.20798288f, -0.9781324f, // 2444.0828 K
		422.83084f, 0.27967486f, 0.35563534f, 0.18619043f, -0.98251367f, // 2365.012 K
		436.91623f, 0.28431216f, 0.3564625f, 0.16523431f, -0.98625433f, // 2288.7683 K
		451.41965f, 0.2890884f, 0.35721228f, 0.14515851f, -0.98940843f, // 2215.2336 K
		466.3535f, 0.29400313f, 0.35788426f, 0.1259972f, -0.9920306f, // 2144.2961 K
		481.73047f, 0.2990556f, 0.35847828f, 0.107775085f, -0.9941753f, // 2075.8496 K
		497.56372f, 0.30424476f, 0.3589947f, 0.09050793f, -0.99589574f, // 2009.7928 K
		513.8164f, 0.30955285f, 0.35943288f, 0.07425155f, -0.99723953f, // 1946.2205 K
		530.49036f, 0.31497455f, 0.35979432f, 0.05900309f, -0.99825776f, // 1885.0485 K
		547.5965f, 0.3205072f, 0.36008114f, 0.044744156f, -0.9989985f, // 1826.1621 K
		565.146f, 0.32614785f, 0.36029562f, 0.03145084f, -0.9995053f, // 1769.4542 K
		583.15027f, 0.33189338f, 0.36044034f, 0.019094577f, -0.99981767f, // 1714.8239 K
		601.62115f, 0.33774033f, 0.36051798f, 0.007643231f, -0.99997085f, // 1662.1755 K
		620.57056f, 0.34368506f, 0.36053145f, 0.0029384496f, -0.9999957f, // 1611.4203 K
		640.011f, 0.34972364f, 0.3604838f, 0.012688f, -0.9999195f, // 1562.4731 K
		659.95514f, 0.3558518f, 0.36037812f, 0.021644777f, -0.99976575f, // 1515.2545 K
		680.4159f, 0.36206508f, 0.36021766f, 0.029849311f, -0.99955446f, // 1469.6893 K
		701.4067f, 0.36835867f, 0.36000568f, 0.03734293f, -0.9993025f, // 1425.7064 K
		722.9412f, 0.3747275f, 0.3597455f, 0.044167273f, -0.99902415f, // 1383.2383 K
		745.03357f, 0.3811661f, 0.35944048f, 0.050363887f, -0.99873096f, // 1342.2214 K
		767.69806f, 0.3876688f, 0.3590939f, 0.055973966f, -0.9984322f, // 1302.5955 K
		790.9496f, 0.39422947f, 0.3587091f, 0.06103805f, -0.9981354f, // 1264.3031 K
		814.8032f, 0.40084177f, 0.3582893f, 0.065595716f, -0.9978463f, // 1227.2902 K
		839.2747f, 0.40749893f, 0.3578377f, 0.069685385f, -0.997569f, // 1191.505 K
		864.3799f, 0.4141939f, 0.35735744f, 0.073344156f, -0.9973067f, // 1156.8988 K
		890.13513f, 0.42091924f, 0.35685158f, 0.07660761f, -0.9970613f, // 1123.4249 K
		916.5573f, 0.42766723f, 0.356323f, 0.07950973f, -0.9968341f, // 1091.0393 K
		943.66364f, 0.4344298f, 0.35577464f, 0.08208277f, -0.9966255f, // 1059.6996 K
		971.4718f, 0.4411986f, 0.35520923f, 0.084357195f, -0.9964355f, // 1029.366 K
		1000, 0.44796494f, 0.3546294f, 0.086361654f, -0.99626386f, // 1000.0 K
	};

	public enum Method {
		/** Nearest isotemperature line. */
		Robertson,
		/** Nearest point on Planckian locus. */
		Ohno
	}
}
