
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Util;

/** Material color system. {@link CAM16} hue/chroma with {@link Lab} L* tone. */
public record HCT (
	/** Hue angle [0..360]. */
	float h,
	/** Chroma [0+]. */
	float C,
	/** Tone (L*) [0..100]. */
	float T) implements Color {

	/** Uses {@link CAM16.VC#HCT}. */
	public LinearRGB LinearRGB () {
		return RGB().LinearRGB();
	}

	/** Uses {@link CAM16.VC#HCT}. */
	public RGB RGB () {
		return RGB(CAM16.VC.HCT);
	}

	public RGB RGB (CAM16.VC vc) {
		float h = this.h * degRad;
		if (T < 0.0001f) return new RGB(0, 0, 0); // Black.
		if (T > 99.9999f) return new RGB(1, 1, 1); // White.
		if (C < 0.0001f) { // Gray.
			float gray = sRGB(Lab.LstarToYn(T));
			return new RGB(gray, gray, gray);
		}
		float Y = Lab.LstarToY(T);
		RGB rgb = findRGB(h, C, Y, vc);
		return rgb != null ? rgb : bisectToLimit(Y, h);
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}

	public float Y () {
		return RGB().Y();
	}

	public HCT lerp (HCT other, float t) {
		return new HCT(lerpAngle(h, other.h, t), Util.lerp(C, other.C, t), Util.lerp(T, other.T, t));
	}

	@SuppressWarnings("all")
	public HCT HCT () {
		return this;
	}

	// Based on Copyright 2021 Google LLC (Apache 2.0):

	/** @return null if failed to converge. */
	static private RGB findRGB (float h, float C, float Y, CAM16.VC vc) {
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

	static private RGB bisectToLimit (float y, float targetHue) {
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

	static private final float[] CRITICAL_PLANES = {0.015176349f, 0.04552905f, 0.07588175f, 0.106234446f, 0.13658714f, 0.16693984f,
		0.19729254f, 0.22764523f, 0.25799793f, 0.28835064f, 0.3188301f, 0.35092592f, 0.3848315f, 0.4205748f, 0.4581833f,
		0.49768373f, 0.53910244f, 0.58246505f, 0.62779695f, 0.67512274f, 0.72446686f, 0.77585304f, 0.8293049f, 0.8848453f,
		0.9424971f, 1.0022825f, 1.0642236f, 1.1283422f, 1.1946592f, 1.263196f, 1.3339732f, 1.4070112f, 1.4823303f, 1.5599504f,
		1.6398909f, 1.7221717f, 1.8068115f, 1.8938295f, 1.9832443f, 2.0750744f, 2.1693382f, 2.266054f, 2.365239f, 2.4669116f,
		2.5710888f, 2.6777883f, 2.7870271f, 2.898822f, 3.0131903f, 3.1301482f, 3.2497122f, 3.371899f, 3.4967241f, 3.6242044f,
		3.7543552f, 3.8871925f, 4.022732f, 4.160989f, 4.3019786f, 4.4457164f, 4.5922174f, 4.7414966f, 4.8935685f, 5.0484486f,
		5.2061505f, 5.3666897f, 5.5300803f, 5.6963363f, 5.865472f, 6.0375013f, 6.2124386f, 6.3902974f, 6.5710917f, 6.754835f,
		6.941541f, 7.1312237f, 7.3238955f, 7.5195704f, 7.7182617f, 7.919982f, 8.124744f, 8.332562f, 8.543448f, 8.757416f, 8.974477f,
		9.194644f, 9.41793f, 9.644348f, 9.873909f, 10.106627f, 10.342513f, 10.58158f, 10.82384f, 11.069304f, 11.3179865f,
		11.569897f, 11.825048f, 12.083452f, 12.34512f, 12.610064f, 12.878296f, 13.149826f, 13.424667f, 13.70283f, 13.984327f,
		14.269169f, 14.557366f, 14.84893f, 15.143873f, 15.442205f, 15.743938f, 16.049082f, 16.357649f, 16.66965f, 16.985094f,
		17.303991f, 17.626356f, 17.952198f, 18.281525f, 18.61435f, 18.950684f, 19.290535f, 19.633915f, 19.980835f, 20.331305f,
		20.685333f, 21.042934f, 21.404114f, 21.768885f, 22.137257f, 22.50924f, 22.884842f, 23.264076f, 23.646952f, 24.033478f,
		24.423664f, 24.81752f, 25.215057f, 25.616285f, 26.021212f, 26.429848f, 26.842203f, 27.258287f, 27.67811f, 28.10168f,
		28.529009f, 28.960102f, 29.394974f, 29.83363f, 30.27608f, 30.722336f, 31.172403f, 31.626295f, 32.08402f, 32.545586f,
		33.010998f, 33.480274f, 33.95342f, 34.43044f, 34.911346f, 35.39615f, 35.884857f, 36.37748f, 36.874023f, 37.374496f,
		37.878914f, 38.387276f, 38.8996f, 39.41589f, 39.936153f, 40.4604f, 40.98864f, 41.52088f, 42.057137f, 42.597404f, 43.1417f,
		43.690037f, 44.242413f, 44.79884f, 45.359333f, 45.923893f, 46.49253f, 47.065254f, 47.64207f, 48.222992f, 48.808025f,
		49.397175f, 49.990456f, 50.58787f, 51.18943f, 51.795143f, 52.405014f, 53.019054f, 53.63727f, 54.259674f, 54.88627f,
		55.517063f, 56.15207f, 56.79129f, 57.434734f, 58.082413f, 58.734333f, 59.3905f, 60.050922f, 60.71561f, 61.38457f, 62.05781f,
		62.73534f, 63.417164f, 64.10329f, 64.793724f, 65.48848f, 66.18756f, 66.89098f, 67.59874f, 68.310844f, 69.027306f, 69.74814f,
		70.473335f, 71.20292f, 71.93688f, 72.67524f, 73.41801f, 74.16518f, 74.91677f, 75.67278f, 76.43323f, 77.19811f, 77.967445f,
		78.74123f, 79.51948f, 80.30219f, 81.08938f, 81.88106f, 82.677216f, 83.47788f, 84.28305f, 85.09273f, 85.90693f, 86.72565f,
		87.548904f, 88.37671f, 89.20905f, 90.04596f, 90.88742f, 91.73345f, 92.58406f, 93.439255f, 94.29904f, 95.16342f, 96.0324f,
		96.906f, 97.78421f, 98.66705f, 99.55453f};
}
