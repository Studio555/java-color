
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;

/** @author Nathan Sweet <misc@n4te.com> */
public class Util {
	static public final float PI = 3.1415927f, radDeg = 180 / PI, degRad = PI / 180;
	static public final float EPSILON = 1e-6f;

	static public float[] matrixMultiply (float row0, float row1, float row2, float[][] matrix) {
		return new float[] { //
			row0 * matrix[0][0] + row1 * matrix[0][1] + row2 * matrix[0][2],
			row0 * matrix[1][0] + row1 * matrix[1][1] + row2 * matrix[1][2],
			row0 * matrix[2][0] + row1 * matrix[2][1] + row2 * matrix[2][2]};
	}

	static class ACESccUtil {
		static float encode (float linear) {
			if (linear <= 0) return -0.3584474886f; // (log2(pow(2,-16)) + 9.72) / 17.52
			if (linear < 0.00003051757812f) // pow(2, -15)
				return (float)((Math.log(0.00001525878906f + linear * 0.5f) / Math.log(2) + 9.72f) / 17.52f);
			return (float)((Math.log(linear) / Math.log(2) + 9.72f) / 17.52f);
		}

		static float decode (float encoded) {
			if (encoded < -0.3014698893f) // (9.72 - 15) / 17.52
				return (float)((Math.pow(2, encoded * 17.52f - 9.72f) - 0.00001525878906f) * 2);
			return (float)Math.pow(2, encoded * 17.52f - 9.72f);
		}
	}

	static class CCTUtil {
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

	static class HCTUtil { // Based on Copyright 2021 Google LLC (Apache 2.0).
		/** @return null if failed to converge. */
		static RGB findRGB (float h, float C, float Y, CAM16.VC vc) {
			float Aw = vc.Aw(), jExponent = 1 / vc.c() / vc.z(), Ncb = vc.Ncb();
			float j = (float)Math.sqrt(Y) * 11;
			float tInnerCoeff = 1 / (float)Math.pow(1.64 - Math.pow(0.29, vc.n()), 0.73);
			float p1 = 0.25f * ((float)Math.cos(h + 2) + 3.8f) * 50000 / 13 * vc.Nc() * Ncb;
			float hSin = (float)Math.sin(h), hCos = (float)Math.cos(h);
			for (int i = 0; i < 5; i++) {
				float jNormalized = j / 100;
				float alpha = C == 0 || j == 0 ? 0 : C / (float)Math.sqrt(jNormalized);
				float t = (float)Math.pow(alpha * tInnerCoeff, 1.0 / 0.9);
				float p2 = Aw * (float)Math.pow(jNormalized, jExponent) / Ncb;
				float gamma = 23 * (p2 + 0.305f) * t / (23 * p1 + 11 * t * hCos + 108 * t * hSin);
				float a = gamma * hCos, b = gamma * hSin;
				float rCScaled = inverseChromaticAdaptation((460 * p2 + 451 * a + 288 * b) / 1403);
				float gCScaled = inverseChromaticAdaptation((460 * p2 - 891 * a - 261 * b) / 1403);
				float bCScaled = inverseChromaticAdaptation((460 * p2 - 220 * a - 6300 * b) / 1403);
				float rr = rCScaled * 1373.2198709594231f + gCScaled * -1100.4251190754821f + bCScaled * -7.278681089101213f;
				float gg = rCScaled * -271.815969077903f + gCScaled * 559.6580465940733f + bCScaled * -32.46047482791194f;
				float bb = rCScaled * 1.9622899599665666f + gCScaled * -57.173814538844006f + bCScaled * 308.7233197812385f;
				if (rr < 0 || gg < 0 || bb < 0) return null;
				float fnj = 0.2126f * rr + 0.7152f * gg + 0.0722f * bb;
				if (fnj <= 0) return null;
				if (i == 4 || Math.abs(fnj - Y) < 0.002f) {
					if (rr > 100.01f || gg > 100.01f || bb > 100.01f) return null;
					return new RGB(sRGB(rr / 100), sRGB(gg / 100), sRGB(bb / 100));
				}
				j -= (fnj - Y) * j / (2 * fnj);
			}
			return null;
		}

		static RGB bisectToLimit (float y, float targetHue) {
			float[][] segment = bisectToSegment(y, targetHue);
			float[] left = segment[0], right = segment[1];
			float leftHue = hueOf(left);
			for (int i = 0; i < 3; i++) {
				if (left[i] == right[i]) continue;
				int lPlane = -1, rPlane = 255;
				if (left[i] < right[i]) {
					lPlane = criticalPlaneBelow(sRGB255(left[i]));
					rPlane = criticalPlaneAbove(sRGB255(right[i]));
				} else {
					lPlane = criticalPlaneAbove(sRGB255(left[i]));
					rPlane = criticalPlaneBelow(sRGB255(right[i]));
				}
				for (int ii = 0; ii < 8; ii++) {
					if (Math.abs(rPlane - lPlane) <= 1) break;
					int mPlane = (lPlane + rPlane) / 2;
					if (mPlane < 0 || mPlane >= CRITICAL_PLANES.length) break;
					float midPlaneCoordinate = CRITICAL_PLANES[mPlane];
					float source1 = left[i];
					float t = (midPlaneCoordinate - source1) / (right[i] - source1);
					float[] mid = { //
						left[0] + (right[0] - left[0]) * t, //
						left[1] + (right[1] - left[1]) * t, //
						left[2] + (right[2] - left[2]) * t};
					float midHue = hueOf(mid);
					if (inCyclicOrder(leftHue, targetHue, midHue)) {
						right = mid;
						rPlane = mPlane;
					} else {
						left = mid;
						leftHue = midHue;
						lPlane = mPlane;
					}
				}
			}
			return new RGB( //
				sRGB((left[0] + right[0]) / 200), //
				sRGB((left[1] + right[1]) / 200), //
				sRGB((left[2] + right[2]) / 200));
		}

		static private float[][] bisectToSegment (float y, float targetHue) {
			float[] left = null, right = null;
			float leftHue = 0, rightHue = 0;
			boolean uncut = true;
			for (int i = 0; i < 12; i++) {
				float[] mid = nthVertex(y, i);
				if (mid == null) continue;
				float midHue = hueOf(mid);
				if (left == null) {
					left = right = mid;
					leftHue = rightHue = midHue;
				} else if (uncut || inCyclicOrder(leftHue, midHue, rightHue)) {
					uncut = false;
					if (inCyclicOrder(leftHue, targetHue, midHue)) {
						right = mid;
						rightHue = midHue;
					} else {
						left = mid;
						leftHue = midHue;
					}
				}
			}
			return new float[][] {left, right};
		}

		/** @return null if outside sRGB gamut. */
		static private float[] nthVertex (float y, int n) {
			float coordA = n % 4 <= 1 ? 0 : 100, coordB = n % 2 == 0 ? 0 : 100;
			if (n < 4) {
				float g = coordA, b = coordB, r = (y - g * 0.7152f - b * 0.0722f) / 0.2126f;
				if (r >= 0 && r <= 100) return new float[] {r, g, b};
			} else if (n < 8) {
				float b = coordA, r = coordB, g = (y - r * 0.2126f - b * 0.0722f) / 0.7152f;
				if (g >= 0 && g <= 100) return new float[] {r, g, b};
			} else {
				float r = coordA, g = coordB, b = (y - r * 0.2126f - g * 0.7152f) / 0.0722f;
				if (b >= 0 && b <= 100) return new float[] {r, g, b};
			}
			return null;
		}

		static private float hueOf (float[] rgb) {
			float rA = chromaticAdaptation(
				rgb[0] * 0.001200833568784504f + rgb[1] * 0.002389694492170889f + rgb[2] * 0.0002795742885861124f);
			float gA = chromaticAdaptation(
				rgb[0] * 0.0005891086651375999f + rgb[1] * 0.0029785502573438758f + rgb[2] * 0.0003270666104008398f);
			float bA = chromaticAdaptation(
				rgb[0] * 0.00010146692491640572f + rgb[1] * 0.0005364214359186694f + rgb[2] * 0.0032979401770712076f);
			float a = (11 * rA + -12 * gA + bA) / 11;
			float b = (rA + gA - 2 * bA) / 9;
			return (float)Math.atan2(b, a);
		}

		static private float chromaticAdaptation (float component) {
			float af = (float)Math.pow(Math.abs(component), 0.42);
			return Math.signum(component) * 400 * af / (af + 27.13f);
		}

		static private float inverseChromaticAdaptation (float adapted) {
			float adaptedAbs = Math.abs(adapted);
			float base = Math.max(0, 27.13f * adaptedAbs / (400 - adaptedAbs));
			return Math.signum(adapted) * (float)Math.pow(base, 1 / 0.42);
		}

		static private float sanitizeRadians (float angle) {
			return (angle + PI * 8) % (PI * 2);
		}

		static private boolean inCyclicOrder (float a, float b, float c) {
			return sanitizeRadians(b - a) < sanitizeRadians(c - a);
		}

		static private float sRGB255 (float rgbComponent) {
			float normalized = rgbComponent / 100;
			return (normalized <= 0.0031308f ? normalized * 12.92f : 1.055f * (float)Math.pow(normalized, 1.0 / 2.4) - 0.055f) * 255;
		}

		static private int criticalPlaneBelow (float x) {
			return (int)Math.floor(x - 0.5f);
		}

		static private int criticalPlaneAbove (float x) {
			return (int)Math.ceil(x - 0.5f);
		}

		static private final float[] CRITICAL_PLANES = {0.015176349f, 0.04552905f, 0.07588175f, 0.106234446f, 0.13658714f,
			0.16693984f, 0.19729254f, 0.22764523f, 0.25799793f, 0.28835064f, 0.3188301f, 0.35092592f, 0.3848315f, 0.4205748f,
			0.4581833f, 0.49768373f, 0.53910244f, 0.58246505f, 0.62779695f, 0.67512274f, 0.72446686f, 0.77585304f, 0.8293049f,
			0.8848453f, 0.9424971f, 1.0022825f, 1.0642236f, 1.1283422f, 1.1946592f, 1.263196f, 1.3339732f, 1.4070112f, 1.4823303f,
			1.5599504f, 1.6398909f, 1.7221717f, 1.8068115f, 1.8938295f, 1.9832443f, 2.0750744f, 2.1693382f, 2.266054f, 2.365239f,
			2.4669116f, 2.5710888f, 2.6777883f, 2.7870271f, 2.898822f, 3.0131903f, 3.1301482f, 3.2497122f, 3.371899f, 3.4967241f,
			3.6242044f, 3.7543552f, 3.8871925f, 4.022732f, 4.160989f, 4.3019786f, 4.4457164f, 4.5922174f, 4.7414966f, 4.8935685f,
			5.0484486f, 5.2061505f, 5.3666897f, 5.5300803f, 5.6963363f, 5.865472f, 6.0375013f, 6.2124386f, 6.3902974f, 6.5710917f,
			6.754835f, 6.941541f, 7.1312237f, 7.3238955f, 7.5195704f, 7.7182617f, 7.919982f, 8.124744f, 8.332562f, 8.543448f,
			8.757416f, 8.974477f, 9.194644f, 9.41793f, 9.644348f, 9.873909f, 10.106627f, 10.342513f, 10.58158f, 10.82384f,
			11.069304f, 11.3179865f, 11.569897f, 11.825048f, 12.083452f, 12.34512f, 12.610064f, 12.878296f, 13.149826f, 13.424667f,
			13.70283f, 13.984327f, 14.269169f, 14.557366f, 14.84893f, 15.143873f, 15.442205f, 15.743938f, 16.049082f, 16.357649f,
			16.66965f, 16.985094f, 17.303991f, 17.626356f, 17.952198f, 18.281525f, 18.61435f, 18.950684f, 19.290535f, 19.633915f,
			19.980835f, 20.331305f, 20.685333f, 21.042934f, 21.404114f, 21.768885f, 22.137257f, 22.50924f, 22.884842f, 23.264076f,
			23.646952f, 24.033478f, 24.423664f, 24.81752f, 25.215057f, 25.616285f, 26.021212f, 26.429848f, 26.842203f, 27.258287f,
			27.67811f, 28.10168f, 28.529009f, 28.960102f, 29.394974f, 29.83363f, 30.27608f, 30.722336f, 31.172403f, 31.626295f,
			32.08402f, 32.545586f, 33.010998f, 33.480274f, 33.95342f, 34.43044f, 34.911346f, 35.39615f, 35.884857f, 36.37748f,
			36.874023f, 37.374496f, 37.878914f, 38.387276f, 38.8996f, 39.41589f, 39.936153f, 40.4604f, 40.98864f, 41.52088f,
			42.057137f, 42.597404f, 43.1417f, 43.690037f, 44.242413f, 44.79884f, 45.359333f, 45.923893f, 46.49253f, 47.065254f,
			47.64207f, 48.222992f, 48.808025f, 49.397175f, 49.990456f, 50.58787f, 51.18943f, 51.795143f, 52.405014f, 53.019054f,
			53.63727f, 54.259674f, 54.88627f, 55.517063f, 56.15207f, 56.79129f, 57.434734f, 58.082413f, 58.734333f, 59.3905f,
			60.050922f, 60.71561f, 61.38457f, 62.05781f, 62.73534f, 63.417164f, 64.10329f, 64.793724f, 65.48848f, 66.18756f,
			66.89098f, 67.59874f, 68.310844f, 69.027306f, 69.74814f, 70.473335f, 71.20292f, 71.93688f, 72.67524f, 73.41801f,
			74.16518f, 74.91677f, 75.67278f, 76.43323f, 77.19811f, 77.967445f, 78.74123f, 79.51948f, 80.30219f, 81.08938f, 81.88106f,
			82.677216f, 83.47788f, 84.28305f, 85.09273f, 85.90693f, 86.72565f, 87.548904f, 88.37671f, 89.20905f, 90.04596f,
			90.88742f, 91.73345f, 92.58406f, 93.439255f, 94.29904f, 95.16342f, 96.0324f, 96.906f, 97.78421f, 98.66705f, 99.55453f};
	}

	static class HSLUtil {
		static float hueToRGB (float v1, float v2, float vH) {
			if (vH < 0) vH += 1;
			if (vH > 1) vH -= 1;
			if (6 * vH < 1) return v1 + (v2 - v1) * 6 * vH;
			if (2 * vH < 1) return v2;
			if (3 * vH < 2) return v1 + (v2 - v1) * (2 / 3f - vH) * 6;
			return v1;
		}
	}

	static class HSLuvUtil {
		static private final float[][] XYZ_RGB = {{3.2404542f, -1.5371385f, -0.4985314f}, {-0.9692660f, 1.8760108f, 0.0415560f},
			{0.0556434f, -0.2040259f, 1.0572252f}};

		static float maxChromaForLH (float L, float H) { // Based on Copyright (c) 2016 Alexei Boronine (MIT License).
			H *= degRad;
			float sin = (float)Math.sin(H), cos = (float)Math.cos(H);
			float sub1 = (L + 0.16f) / 1.16f;
			sub1 *= sub1 * sub1;
			float sub2 = sub1 > EPSILON ? sub1 : L / LabUtil.k;
			float min = Float.MAX_VALUE;
			for (int i = 0; i < 3; i++) {
				float m1 = XYZ_RGB[i][0] * sub2, m2 = XYZ_RGB[i][1] * sub2, m3 = XYZ_RGB[i][2] * sub2;
				for (int t = 0; t < 2; t++) {
					float top1 = 2845.17f * m1 - 948.39f * m3;
					float top2 = (8384.22f * m3 + 7698.60f * m2 + 7317.18f * m1 - 7698.60f * t) * L;
					float bottom = (6322.60f * m3 - 1264.52f * m2) + 1264.52f * t;
					float length = top2 / bottom / (sin - top1 / bottom * cos);
					if (length >= 0) min = Math.min(min, length);
				}
			}
			return min;
		}
	}

	static class ITPUtil {
		static private final float PQ_m1 = 0.1593017578125f; // 2610 / 16384
		static private final float PQ_m2 = 78.84375f; // 2523 / 32
		static private final float PQ_c1 = 0.8359375f; // 3424 / 4096
		static private final float PQ_c2 = 18.8515625f; // 2413 / 128
		static private final float PQ_c3 = 18.6875f; // 2392 /128
		static private final float PQ_n = 0.15930175781f; // 2610 / 16384

		static float PQ_EOTF (float value) {
			if (value <= 0) return 0;
			float pow = (float)Math.pow(value, 1 / PQ_m2);
			float num = Math.max(0, pow - PQ_c1);
			float denom = PQ_c2 - PQ_c3 * pow;
			if (denom < EPSILON) return 0;
			return (float)Math.pow(num / denom, 1 / PQ_n);
		}

		static float PQ_EOTF_inverse (float value) {
			if (value <= 0) return 0;
			float pow = (float)Math.pow(value, PQ_m1);
			float num = PQ_c1 + PQ_c2 * pow;
			float denom = 1 + PQ_c3 * pow;
			return (float)Math.pow(num / denom, PQ_m2);
		}
	}

	static public class LabUtil {
		static public final float k = 24389 / 27f;
		static public final float e = 216 / 24389f;

		/** @return [0..100] */
		static public float LstarToY (float Lstar) {
			float ft = (Lstar + 16) / 116;
			float ft3 = ft * ft * ft;
			return (ft3 > e ? ft3 : (116 * ft - 16) / k) * 100;
		}

		/** @return [0..1] */
		static public float LstarToYn (float Lstar) {
			float ft = (Lstar + 16) / 116;
			float ft3 = ft * ft * ft;
			return ft3 > e ? ft3 : (116 * ft - 16) / k;
		}

		/** L* is perceptual luminance (a linear scale) while Y in {@link XYZ} is relative luminance (a logarithmic scale).
		 * @return [0..100] */
		static public float YtoLstar (float Y) {
			float y = Y / 100;
			return y > e ? 116 * (float)Math.pow(y, 1 / 3f) - 16 : k * y;
		}
	}

	static class LMSUtil {
		static final float[][] HPE_forward = {{0.38971f, 0.68898f, -0.07868f}, {-0.22981f, 1.18340f, 0.04641f},
			{0.00000f, 0.00000f, 1.00000f}};
		static final float[][] HPE_backward = {{1.91019683f, -1.11212389f, 0.20190796f}, {0.37095009f, 0.62905426f, -0.00000806f},
			{0.00000f, 0.00000f, 1.00000f}};
		static final float[][] Bradford_forward = {{0.8951000f, 0.2664000f, -0.1614000f}, {-0.7502000f, 1.7135000f, 0.0367000f},
			{0.0389000f, -0.0685000f, 1.0296000f}};
		static final float[][] Bradford_backward = {{0.9869929f, -0.1470543f, 0.1599627f}, {0.4323053f, 0.5183603f, 0.0492912f},
			{-0.0085287f, 0.0400428f, 0.9684867f}};
		static final float[][] vonKries_forward = {{0.4002f, 0.7076f, -0.0808f}, {-0.2263f, 1.1653f, 0.0457f}, {0f, 0f, 0.9182f}};
		static final float[][] vonKries_backward = {{1.86006661f, -1.12948008f, 0.21989830f},
			{0.36122292f, 0.63880431f, -0.00000713f}, {0.00000f, 0.00000f, 1.08908734f}};
		static final float[][] CAT97_forward = {{0.8562f, 0.3372f, -0.1934f}, {-0.8360f, 1.8327f, 0.0033f},
			{0.0357f, -0.00469f, 1.0112f}};
		static final float[][] CAT97_backward = {{0.9838112f, -0.1805292f, 0.1887508f}, {0.4488317f, 0.4632779f, 0.0843307f},
			{-0.0326513f, 0.0085222f, 0.9826514f}};
		static final float[][] CAT02_forward = {{0.7328f, 0.4296f, -0.1624f}, {-0.7036f, 1.6975f, 0.0061f},
			{0.0030f, 0.0136f, 0.9834f}};
		static final float[][] CAT02_backward = {{1.0961238f, -0.2788690f, 0.1827452f}, {0.4543690f, 0.4735332f, 0.0720978f},
			{-0.0096276f, -0.0056980f, 1.0153256f}};
	}

	static class OkhsvUtil { // Based on Copyright (c) 2021 Björn Ottosson (MIT license).
		static private final float k_3 = 1.206f / 1.03f;

		static float toe (float x) {
			return 0.5f * (k_3 * x - 0.206f + (float)Math.sqrt((k_3 * x - 0.206f) * (k_3 * x - 0.206f) + 4 * 0.03f * k_3 * x));
		}

		static float toeInv (float x) {
			return (x * x + 0.206f * x) / (k_3 * (x + 0.03f));
		}

		static float[] cuspST (float a, float b) {
			float S_cusp = maxSaturation(a, b);
			LinearRGB rgb_at_max = new Oklab(1, S_cusp * a, S_cusp * b).LinearRGB();
			float L = (float)Math.cbrt(1 / max(rgb_at_max.r(), rgb_at_max.g(), rgb_at_max.b()));
			float C = L * S_cusp;
			return new float[] {C / L, C / (1 - L)};
		}

		static float[] Cs (float L, float a_, float b_) {
			float[][] M = {{4.0767416621f, -3.3077115913f, 0.2309699292f}, {-1.2684380046f, 2.6097574011f, -0.3413193965f},
				{-0.0041960863f, -0.7034186147f, 1.7076147010f}};
			float S_max = Float.MAX_VALUE;
			for (int i = 0; i < 3; i++) {
				float denom = a_ * M[i][0] + b_ * M[i][1];
				if (Math.abs(denom) < EPSILON) continue;
				float t = -M[i][2] / denom;
				if (t < 0) continue;
				float s = (1 + t) * denom;
				if (s > 0 && s < S_max) S_max = s;
			}
			float C_max = maxSaturation(a_, b_);
			float denom = Math.min(L * S_max, (1 - L) * C_max);
			float k = denom < EPSILON ? 0 : C_max / denom;
			float S = 0.11516993f + 1 / (7.44778970f + 4.15901240f * b_ + a_ * (-2.19557347f + 1.75198401f * b_
				+ a_ * (-2.13704948f - 10.02301043f * b_ + a_ * (-4.24894561f + 5.38770819f * b_ + 4.69891013f * a_))));
			float T = 0.11239642f + 1 / (1.61320320f - 0.68124379f * b_ + a_ * (0.40370612f + 0.90148123f * b_
				+ a_ * (-0.27087943f + 0.61223990f * b_ + a_ * (0.00299215f - 0.45399568f * b_ - 0.14661872f * a_))));
			float inv_scale = Math.min(L * S, (1 - L) * 1 / T);
			float denom2 = 1 + k;
			float L_mid = 0.5f * (1 + (denom2 < EPSILON ? 0 : Math.signum(L - 0.5f) * inv_scale / denom2));
			float C_mid = L_mid * S, C_a = L * 0.4f, C_b = (1 - L) * 0.8f;
			float C_0 = (float)Math.sqrt(1 / (1 / (C_a * C_a) + 1 / (C_b * C_b)));
			return new float[] {C_0, C_mid, C_max};
		}

		static private float maxSaturation (float a, float b) {
			float k0, k1, k2, k3, k4, wl, wm, ws;
			if (-1.88170328f * a - 0.80936493f * b > 1) { // Red.
				k0 = 1.19086277f;
				k1 = 1.76576728f;
				k2 = 0.59662641f;
				k3 = 0.75515197f;
				k4 = 0.56771245f;
				wl = 4.0767416621f;
				wm = -3.3077115913f;
				ws = 0.2309699292f;
			} else if (1.81444104f * a - 1.19445276f * b > 1) { // Green.
				k0 = 0.73956515f;
				k1 = -0.45954404f;
				k2 = 0.08285427f;
				k3 = 0.12541070f;
				k4 = 0.14503204f;
				wl = -1.2684380046f;
				wm = 2.6097574011f;
				ws = -0.3413193965f;
			} else { // Blue.
				k0 = 1.35733652f;
				k1 = -0.00915799f;
				k2 = -1.15130210f;
				k3 = -0.50559606f;
				k4 = 0.00692167f;
				wl = -0.0041960863f;
				wm = -0.7034186147f;
				ws = 1.7076147010f;
			}
			float S = k0 + k1 * a + k2 * b + k3 * a * a + k4 * a * b;
			float k_l = 0.3963377774f * a + 0.2158037573f * b;
			float k_m = -0.1055613458f * a - 0.0638541728f * b;
			float k_s = -0.0894841775f * a - 1.2914855480f * b;
			float l_ = 1 + S * k_l, m_ = 1 + S * k_m, s_ = 1 + S * k_s;
			float l = l_ * l_ * l_, m = m_ * m_ * m_, s = s_ * s_ * s_;
			float l_dS = 3 * k_l * l_ * l_, m_dS = 3 * k_m * m_ * m_, s_dS = 3 * k_s * s_ * s_;
			float l_dS2 = 6 * k_l * k_l * l_, m_dS2 = 6 * k_m * k_m * m_, s_dS2 = 6 * k_s * k_s * s_;
			float f = wl * l + wm * m + ws * s, f1 = wl * l_dS + wm * m_dS + ws * s_dS, f2 = wl * l_dS2 + wm * m_dS2 + ws * s_dS2;
			return S - f * f1 / (f1 * f1 - 0.5f * f * f2);
		}
	}

	static public class RGBUtil {
		static public boolean achromatic (RGB rgb) {
			return max(rgb.r(), rgb.g(), rgb.b()) - min(rgb.r(), rgb.g(), rgb.b()) < EPSILON;
		}

		/** Returns colors opposite on color wheel. */
		static public RGB complementary (RGB base) {
			HSL hsl = base.HSL();
			float h = hsl.H() + 180;
			if (h >= 360) h -= 360;
			return new HSL(h, hsl.S(), hsl.L()).RGB();
		}

		/** Returns 3 colors evenly spaced on color wheel. */
		static public RGB[] triadic (RGB base) {
			HSL hsl = base.HSL();
			float h1 = hsl.H() + 120;
			float h2 = hsl.H() + 240;
			if (h1 >= 360) h1 -= 360;
			if (h2 >= 360) h2 -= 360;
			return new RGB[] {base, new HSL(h1, hsl.S(), hsl.L()).RGB(), new HSL(h2, hsl.S(), hsl.L()).RGB()};
		}

		/** Returns 3 colors adjacent on color wheel.
		 * @param angle [0..360] */
		static public RGB[] analogous (RGB base, float angle) {
			HSL hsl = base.HSL();
			float h1 = hsl.H() + angle;
			float h2 = hsl.H() - angle;
			if (h1 >= 360) h1 -= 360;
			if (h2 < 0) h2 += 360;
			return new RGB[] {new HSL(h2, hsl.S(), hsl.L()).RGB(), base, new HSL(h1, hsl.S(), hsl.L()).RGB()};
		}

		/** Returns a split-complementary color scheme. */
		static public RGB[] splitComplementary (RGB base) {
			HSL hsl = base.HSL();
			float h1 = hsl.H() + 150;
			float h2 = hsl.H() + 210;
			if (h1 >= 360) h1 -= 360;
			if (h2 >= 360) h2 -= 360;
			return new RGB[] {base, new HSL(h1, hsl.S(), hsl.L()).RGB(), new HSL(h2, hsl.S(), hsl.L()).RGB()};
		}

		/** Returns the WCAG contrast ratio between foreground and background colors.
		 * @return Contrast ratio, 1:1 to 21:1. */
		static public float contrastRatio (RGB fg, RGB bg) {
			float fgLum = fg.Y() / 100;
			float bgLum = bg.Y() / 100;
			float L1 = Math.max(fgLum, bgLum);
			float L2 = Math.min(fgLum, bgLum);
			return (L1 + 0.05f) / (L2 + 0.05f);
		}

		/** Returns true if the colors meet the WCAG AA contrast accessibility standard.
		 * @param largeText true for 18pt+ normal or 14pt+ bold text */
		static public boolean WCAG_AA (RGB fg, RGB bg, boolean largeText) {
			return contrastRatio(fg, bg) >= (largeText ? 3 : 4.5f);
		}

		/** Returns true if the colors meet the WCAG AAA contrast accessibility standard.
		 * @param largeText true for 18pt+ normal or 14pt+ bold text */
		static public boolean WCAG_AAA (RGB fg, RGB bg, boolean largeText) {
			return contrastRatio(fg, bg) >= (largeText ? 4.5f : 7);
		}
	}

	static public class XYZUtil {
		/** Planck constant. **/
		static public float h = 6.62607015e-34f;
		/** Speed of light in m/s. **/
		static public float c = 299792458f;
		/** Boltzmann constant. **/
		static public float k = 1.380649e-23f;
		/** First radiation constant. **/
		static public float c1 = 2f * (float)Math.PI * h * c * c;
		/** Second radiation constant. **/
		static public float c2 = h * c / k;

		/** CIE 1931 x-bar color matching function (380-780nm at 5nm intervals). **/
		static public float[] Xbar = {0.001368f, 0.002236f, 0.004243f, 0.00765f, 0.01431f, 0.02319f, 0.04351f, 0.07763f, 0.13438f,
			0.21477f, 0.2839f, 0.3285f, 0.34828f, 0.34806f, 0.3362f, 0.3187f, 0.2908f, 0.2511f, 0.19536f, 0.1421f, 0.09564f,
			0.05795f, 0.03201f, 0.0147f, 0.0049f, 0.0024f, 0.0093f, 0.0291f, 0.06327f, 0.1096f, 0.1655f, 0.22575f, 0.2904f, 0.3597f,
			0.43345f, 0.51205f, 0.5945f, 0.6784f, 0.7621f, 0.8425f, 0.9163f, 0.9786f, 1.0263f, 1.0567f, 1.0622f, 1.0456f, 1.0026f,
			0.9384f, 0.85445f, 0.7514f, 0.6424f, 0.5419f, 0.4479f, 0.3608f, 0.2835f, 0.2187f, 0.1649f, 0.1212f, 0.0874f, 0.0636f,
			0.04677f, 0.0329f, 0.0227f, 0.01584f, 0.011359f, 0.008111f, 0.00579f, 0.004109f, 0.002899f, 0.002049f, 0.00144f, 0.001f,
			0.00069f, 0.000476f, 0.000332f, 0.000235f, 0.000166f, 0.000117f, 0.000083f, 0.000059f, 0.000042f};

		/** CIE 1931 y-bar color matching function (380-780nm at 5nm intervals). **/
		static public float[] Ybar = {0.000039f, 0.000064f, 0.00012f, 0.000217f, 0.000396f, 0.00064f, 0.00121f, 0.00218f, 0.004f,
			0.0073f, 0.0116f, 0.01684f, 0.023f, 0.0298f, 0.038f, 0.048f, 0.06f, 0.0739f, 0.09098f, 0.1126f, 0.13902f, 0.1693f,
			0.20802f, 0.2586f, 0.323f, 0.4073f, 0.503f, 0.6082f, 0.71f, 0.7932f, 0.862f, 0.91485f, 0.954f, 0.9803f, 0.99495f, 1f,
			0.995f, 0.9786f, 0.952f, 0.9154f, 0.87f, 0.8163f, 0.757f, 0.6949f, 0.631f, 0.5668f, 0.503f, 0.4412f, 0.381f, 0.321f,
			0.265f, 0.217f, 0.175f, 0.1382f, 0.107f, 0.0816f, 0.061f, 0.04458f, 0.032f, 0.0232f, 0.017f, 0.01192f, 0.00821f,
			0.005723f, 0.004102f, 0.002929f, 0.002091f, 0.001484f, 0.001047f, 0.00074f, 0.00052f, 0.000361f, 0.000249f, 0.000172f,
			0.00012f, 0.000085f, 0.00006f, 0.000042f, 0.00003f, 0.000021f, 0.000015f};

		/** CIE 1931 z-bar color matching function (380-780nm at 5nm intervals). **/
		static public float[] Zbar = {0.00645f, 0.01055f, 0.02005f, 0.03621f, 0.06785f, 0.1102f, 0.2074f, 0.3713f, 0.6456f,
			1.03905f, 1.3856f, 1.62296f, 1.74706f, 1.7826f, 1.77211f, 1.7441f, 1.6692f, 1.5281f, 1.28764f, 1.0419f, 0.81295f,
			0.6162f, 0.46518f, 0.3533f, 0.272f, 0.2123f, 0.1582f, 0.1117f, 0.07825f, 0.05725f, 0.04216f, 0.02984f, 0.0203f, 0.0134f,
			0.00875f, 0.00575f, 0.0039f, 0.00275f, 0.0021f, 0.0018f, 0.00165f, 0.0014f, 0.0011f, 0.001f, 0.0008f, 0.0006f, 0.00034f,
			0.00024f, 0.00019f, 0.0001f, 0.00005f, 0.00003f, 0.00002f, 0.00001f, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	}

	@FunctionalInterface
	public interface FloatOperator {
		public float apply (float value);
	}

	@FunctionalInterface
	public interface FloatIndexOperator {
		public float apply (int index, float value);
	}
}
