
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;

import com.esotericsoftware.colors.Colors.Oklab;

class Util {
	static public float[] matrixMultiply (float[] a, float[][] b) {
		float[] result = new float[b[0].length];
		for (int i = 0; i < b[0].length; i++)
			for (int ii = 0; ii < b.length; ii++)
				result[i] += a[ii] * b[ii][i];
		return result;
	}

	class HSLuv {
		static private final float[][] HSLuv_XYZ_RGB = {{3.2404542f, -1.5371385f, -0.4985314f},
			{-0.9692660f, 1.8760108f, 0.0415560f}, {0.0556434f, -0.2040259f, 1.0572252f}};

		static float maxChromaForLH (float L, float H) {
			float hRad = H * degRad;
			float minChroma = Float.MAX_VALUE;
			for (float[] bound : getBounds(L)) {
				float m1 = bound[0], b1 = bound[1];
				if (Float.isNaN(m1) || Float.isNaN(b1)) continue;
				float x, y;
				if (Math.abs(Math.sin(hRad)) < 1e-10f) { // Hue is 0 or 180 degrees (vertical line).
					x = 0;
					y = b1;
				} else { // Calculate intersection based on hue angle.
					float m2 = -1 / (float)Math.tan(hRad);
					x = intersectLineLine(m1, b1, m2, 0);
					if (Float.isNaN(x)) continue; // Lines are parallel.
					y = m2 * x;
				}
				float chroma = (float)Math.sqrt(x * x + y * y);
				if (chroma >= 0 && chroma < minChroma) minChroma = chroma;
			}
			return minChroma == Float.MAX_VALUE ? 0 : minChroma;
		}

		static private float[][] getBounds (float L) {
			float[][] bounds = new float[6][2];
			float sub1 = (float)Math.pow(L + 16, 3) / 1560896;
			float sub2 = sub1 > e ? sub1 : L / k;
			for (int c = 0, index = 0; c < 3; c++) {
				float m1 = HSLuv_XYZ_RGB[c][0];
				float m2 = HSLuv_XYZ_RGB[c][1];
				float m3 = HSLuv_XYZ_RGB[c][2];
				for (int t = 0; t < 2; t++) {
					float top1 = (284517 * m1 - 94839 * m3) * sub2;
					float top2 = (838422 * m3 + 769860 * m2 + 731718 * m1) * L * sub2 - 769860 * t * L;
					float bottom = (632260 * m3 - 126452 * m2) * sub2 + 126452 * t;
					if (Math.abs(bottom) < 1e-10f) {
						bounds[index][0] = Float.NaN;
						bounds[index][1] = Float.NaN;
					} else {
						bounds[index][0] = top1 / bottom;
						bounds[index][1] = top2 / bottom;
					}
					index++;
				}
			}
			return bounds;
		}

		static private float intersectLineLine (float m1, float b1, float m2, float b2) {
			float denom = m1 - m2;
			if (Math.abs(denom) < 1e-10f) return Float.NaN; // Parallel lines
			return (b2 - b1) / denom;
		}
	}

	class Okhsv {
		static float toe (float x) {
			float k_1 = 0.206f;
			float k_2 = 0.03f;
			float k_3 = (1 + k_1) / (1 + k_2);
			return 0.5f * (k_3 * x - k_1 + (float)Math.sqrt((k_3 * x - k_1) * (k_3 * x - k_1) + 4 * k_2 * k_3 * x));
		}

		static float toeInv (float x) {
			float k_1 = 0.206f;
			float k_2 = 0.03f;
			float k_3 = (1 + k_1) / (1 + k_2);
			return (x * x + k_1 * x) / (k_3 * (x + k_2));
		}

		static float[] cuspST (float a, float b) {
			float S_cusp = maxSaturation(a, b);
			var rgb_at_max = LinearRGB(new Oklab(1, S_cusp * a, S_cusp * b));
			float L = (float)Math.cbrt(1.f / Math.max(Math.max(rgb_at_max.r(), rgb_at_max.g()), rgb_at_max.b()));
			float C = L * S_cusp;
			return new float[] {C / L, C / (1 - L)};
		}

		static float[] Cs (float L, float a_, float b_) {
			float[][] M = {{4.0767416621f, -3.3077115913f, 0.2309699292f}, {-1.2684380046f, 2.6097574011f, -0.3413193965f},
				{-0.0041960863f, -0.7034186147f, 1.7076147010f}};
			float S_max = Float.MAX_VALUE;
			for (int i = 0; i < 3; i++) {
				float denom = a_ * M[i][0] + b_ * M[i][1];
				if (Math.abs(denom) < 1e-10f) continue;
				float t = -M[i][2] / denom;
				if (t < 0) continue;
				float s = (1 + t) * denom;
				if (s > 0 && s < S_max) S_max = s;
			}
			float C_max = maxSaturation(a_, b_);
			float denom = Math.min(L * S_max, (1 - L) * C_max);
			float k = denom < 1e-10f ? 0 : C_max / denom;
			float S = 0.11516993f + 1 / (7.44778970f + 4.15901240f * b_ + a_ * (-2.19557347f + 1.75198401f * b_
				+ a_ * (-2.13704948f - 10.02301043f * b_ + a_ * (-4.24894561f + 5.38770819f * b_ + 4.69891013f * a_))));
			float T = 0.11239642f + 1 / (1.61320320f - 0.68124379f * b_ + a_ * (0.40370612f + 0.90148123f * b_
				+ a_ * (-0.27087943f + 0.61223990f * b_ + a_ * (0.00299215f - 0.45399568f * b_ - 0.14661872f * a_))));
			float inv_scale = Math.min(L * S, (1 - L) * 1 / T);
			float denom2 = 1 + k;
			float L_mid = 0.5f * (1 + (denom2 < 1e-10f ? 0 : (float)Math.signum(L - 0.5f) * inv_scale / denom2));
			float C_mid = L_mid * S, C_a = L * 0.4f, C_b = (1.f - L) * 0.8f;
			float C_0 = (float)Math.sqrt(1.f / (1.f / (C_a * C_a) + 1.f / (C_b * C_b)));
			return new float[] {C_0, C_mid, C_max};
		}

		static float maxSaturation (float a, float b) {
			float k0, k1, k2, k3, k4, wl, wm, ws;
			if (-1.88170328f * a - 0.80936493f * b > 1) { // Red.
				k0 = +1.19086277f;
				k1 = +1.76576728f;
				k2 = +0.59662641f;
				k3 = +0.75515197f;
				k4 = +0.56771245f;
				wl = +4.0767416621f;
				wm = -3.3077115913f;
				ws = +0.2309699292f;
			} else if (1.81444104f * a - 1.19445276f * b > 1) { // Green.
				k0 = +0.73956515f;
				k1 = -0.45954404f;
				k2 = +0.08285427f;
				k3 = +0.12541070f;
				k4 = +0.14503204f;
				wl = -1.2684380046f;
				wm = +2.6097574011f;
				ws = -0.3413193965f;
			} else { // Blue.
				k0 = +1.35733652f;
				k1 = -0.00915799f;
				k2 = -1.15130210f;
				k3 = -0.50559606f;
				k4 = +0.00692167f;
				wl = -0.0041960863f;
				wm = -0.7034186147f;
				ws = +1.7076147010f;
			}
			float S = k0 + k1 * a + k2 * b + k3 * a * a + k4 * a * b;
			float k_l = +0.3963377774f * a + 0.2158037573f * b;
			float k_m = -0.1055613458f * a - 0.0638541728f * b;
			float k_s = -0.0894841775f * a - 1.2914855480f * b;
			float l_ = 1.f + S * k_l;
			float m_ = 1.f + S * k_m;
			float s_ = 1.f + S * k_s;
			float l = l_ * l_ * l_;
			float m = m_ * m_ * m_;
			float s = s_ * s_ * s_;
			float l_dS = 3.f * k_l * l_ * l_;
			float m_dS = 3.f * k_m * m_ * m_;
			float s_dS = 3.f * k_s * s_ * s_;
			float l_dS2 = 6.f * k_l * k_l * l_;
			float m_dS2 = 6.f * k_m * k_m * m_;
			float s_dS2 = 6.f * k_s * k_s * s_;
			float f = wl * l + wm * m + ws * s;
			float f1 = wl * l_dS + wm * m_dS + ws * s_dS;
			float f2 = wl * l_dS2 + wm * m_dS2 + ws * s_dS2;
			S = S - f * f1 / (f1 * f1 - 0.5f * f * f2);
			return S;
		}
	}
}
