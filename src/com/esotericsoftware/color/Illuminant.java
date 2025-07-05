
package com.esotericsoftware.color;

import java.util.Arrays;

import com.esotericsoftware.color.space.xy;

public class Illuminant {
	/** CIE daylight basis function S0, 380-780nm @ 5nm. */
	static public final float[] S0 = {63.4f, 64.6f, 65.8f, 80.3f, 94.8f, 99.8f, 104.8f, 105.35f, 105.9f, 101.35f, 96.8f, 105.35f,
		113.9f, 119.75f, 125.6f, 125.55f, 125.5f, 123.4f, 121.3f, 121.3f, 121.3f, 117.4f, 113.5f, 113.3f, 113.1f, 111.95f, 110.8f,
		108.65f, 106.5f, 107.65f, 108.8f, 107.05f, 105.3f, 104.85f, 104.4f, 102.2f, 100, 98, 96, 95.55f, 95.1f, 92.1f, 89.1f, 89.8f,
		90.5f, 90.4f, 90.3f, 89.35f, 88.4f, 86.2f, 84, 84.55f, 85.1f, 83.5f, 81.9f, 82.25f, 82.6f, 83.75f, 84.9f, 83.1f, 81.3f,
		76.6f, 71.9f, 73.1f, 74.3f, 75.35f, 76.4f, 69.85f, 63.3f, 67.5f, 71.7f, 74.35f, 77, 71.1f, 65.2f, 56.45f, 47.7f, 58.15f,
		68.6f, 66.8f, 65};

	/** CIE daylight basis function S1, 380-780nm @ 5nm. */
	static public final float[] S1 = {38.5f, 36.75f, 35, 39.2f, 43.4f, 44.85f, 46.3f, 45.1f, 43.9f, 40.5f, 37.1f, 36.9f, 36.7f,
		36.3f, 35.9f, 34.25f, 32.6f, 30.25f, 27.9f, 26.1f, 24.3f, 22.2f, 20.1f, 18.15f, 16.2f, 14.7f, 13.2f, 10.9f, 8.6f, 7.35f,
		6.1f, 5.15f, 4.2f, 3.05f, 1.9f, 0.95f, 0, -0.8f, -1.6f, -2.55f, -3.5f, -3.5f, -3.5f, -4.65f, -5.8f, -6.5f, -7.2f, -7.9f,
		-8.6f, -9.05f, -9.5f, -10.2f, -10.9f, -10.8f, -10.7f, -11.35f, -12, -13, -14, -13.8f, -13.6f, -12.8f, -12, -12.65f, -13.3f,
		-13.1f, -12.9f, -11.75f, -10.6f, -11.1f, -11.6f, -11.9f, -12.2f, -11.2f, -10.2f, -9, -7.8f, -9.5f, -11.2f, -10.8f, -10.4f};

	/** CIE daylight basis function S2, 380-780nm @ 5nm. */
	static public final float[] S2 = {3, 2.1f, 1.2f, 0.05f, -1.1f, -0.8f, -0.5f, -0.6f, -0.7f, -0.95f, -1.2f, -1.9f, -2.6f, -2.75f,
		-2.9f, -2.85f, -2.8f, -2.7f, -2.6f, -2.6f, -2.6f, -2.2f, -1.8f, -1.65f, -1.5f, -1.4f, -1.3f, -1.25f, -1.2f, -1.1f, -1,
		-0.75f, -0.5f, -0.4f, -0.3f, -0.15f, 0, 0.1f, 0.2f, 0.35f, 0.5f, 1.3f, 2.1f, 2.65f, 3.2f, 3.65f, 4.1f, 4.4f, 4.7f, 4.9f,
		5.1f, 5.9f, 6.7f, 7, 7.3f, 7.95f, 8.6f, 9.2f, 9.8f, 10, 10.2f, 9.25f, 8.3f, 8.95f, 9.6f, 9.05f, 8.5f, 7.75f, 7, 7.3f, 7.6f,
		7.8f, 8, 7.35f, 6.7f, 5.95f, 5.2f, 6.3f, 7.4f, 7.1f, 6.8f};

	/** Returns a CIE daylight illuminant spectrum.
	 * @return 380-780nm @ 5nm, 81 values unnormalized. */
	static public Spectrum D (xy xy) {
		float x = xy.x(), y = xy.y();
		float M = (0.0241f + 0.2562f * x - 0.7341f * y);
		float M1 = (-1.3515f - 1.7703f * x + 5.9114f * y) / M;
		float M2 = (0.03f - 31.4424f * x + 30.0717f * y) / M;
		float[] values = new float[81];
		for (int i = 0; i < 81; i++)
			values[i] = S0[i] + M1 * S1[i] + M2 * S2[i];
		return new Spectrum(values, 5);
	}

	/** Returns an equal energy spectrum (all values 1, illuminant E). */
	static public Spectrum E (int start, int end, int step) {
		float[] values = new float[(end - start) / step + 1];
		Arrays.fill(values, 1);
		return new Spectrum(values, step, start);
	}
}
