
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.Colors.Illuminant;
import com.esotericsoftware.colors.Colors.XYZ;
import com.esotericsoftware.colors.Colors.uv;
import com.esotericsoftware.colors.Colors.xy;

/** @author Nathan Sweet <misc@n4te.com> */
public class SpectralLocus {
	/** 64 wavelength, u', v' entries from CIE 1931 2 degree data at 1nm with colinear points in u'v' space removed, 380-700nm. */
	static public final float[] points = {380, 0.2568657f, 0.016464427f, 394, 0.25605628f, 0.016393442f, 399, 0.25582084f,
		0.015968513f, 404, 0.25540012f, 0.015835252f, 415, 0.2536536f, 0.016018247f, 422, 0.25127298f, 0.017623201f, 426,
		0.24901703f, 0.019670222f, 434, 0.24221061f, 0.026608719f, 442, 0.23176162f, 0.03814886f, 454, 0.20605913f, 0.06581993f,
		458, 0.19424264f, 0.07921142f, 461, 0.1842386f, 0.091372184f, 463, 0.17698844f, 0.10075823f, 464, 0.17311583f, 0.10604715f,
		466, 0.16455999f, 0.118391134f, 468, 0.15482037f, 0.13343947f, 470, 0.14410105f, 0.15098567f, 472, 0.13275325f, 0.17068633f,
		474, 0.12084262f, 0.19260298f, 476, 0.108390965f, 0.21686608f, 477, 0.10204629f, 0.22978286f, 479, 0.08922779f, 0.25685638f,
		481, 0.076424845f, 0.2849871f, 483, 0.063917525f, 0.31373343f, 484, 0.057901084f, 0.32823032f, 485, 0.05213973f, 0.3427062f,
		486, 0.046693884f, 0.35705063f, 488, 0.036816426f, 0.38500172f, 490, 0.02815329f, 0.4116651f, 492, 0.02066682f, 0.4366151f,
		494, 0.014468307f, 0.4593753f, 496, 0.009593704f, 0.4796657f, 498, 0.0059690946f, 0.49748757f, 500, 0.0034601416f,
		0.51306874f, 502, 0.0019410703f, 0.5265283f, 504, 0.0013750325f, 0.53802776f, 505, 0.0014230257f, 0.54316264f, 506,
		0.0016718432f, 0.547952f, 507, 0.002119664f, 0.552407f, 508, 0.00276277f, 0.55653566f, 509, 0.0035994062f, 0.5603435f, 510,
		0.004633163f, 0.5638387f, 511, 0.005849349f, 0.5670354f, 512, 0.007226011f, 0.5699476f, 513, 0.0087608965f, 0.5725737f, 514,
		0.010444313f, 0.57491213f, 515, 0.012268543f, 0.57696736f, 516, 0.014230083f, 0.5787552f, 517, 0.016309358f, 0.5803012f,
		518, 0.018487642f, 0.58162445f, 519, 0.020762732f, 0.58274055f, 520, 0.023115812f, 0.58366644f, 522, 0.028063279f,
		0.58501124f, 525, 0.035995018f, 0.5861401f, 527, 0.04155638f, 0.5865299f, 529, 0.047226105f, 0.58671445f, 532, 0.055735257f,
		0.5867426f, 535, 0.06432658f, 0.58652556f, 540, 0.079229034f, 0.58562356f, 546, 0.09862341f, 0.58374643f, 551, 0.11639242f,
		0.58160305f, 556, 0.13596386f, 0.57899094f, 568, 0.19187802f, 0.57094526f, 700, 0.6233662f, 0.5064951f, 380f, 0.2568657f,
		0.016464427f};

	static public uv uv (float wavelength) {
		if (wavelength < 380 || wavelength > 700) throw new IllegalArgumentException("wavelength must be 380-700: " + wavelength);
		int left = 0, right = (points.length / 3) - 2;
		while (left <= right) {
			int mid = (left + right) / 2, i = mid * 3;
			float w1 = points[i];
			if (wavelength < w1)
				right = mid - 1;
			else if (wavelength > points[i + 3])
				left = mid + 1;
			else {
				float t = (wavelength - w1) / (points[i + 3] - w1);
				float u1 = points[i + 1], v1 = points[i + 2];
				float u2 = points[i + 4], v2 = points[i + 5];
				return new uv(u1 + t * (u2 - u1), v1 + t * (v2 - v1));
			}
		}
		return new uv(Float.NaN, Float.NaN);
	}

	static public xy xy (float wavelength) {
		return Colors.xy(uv(wavelength));
	}

	static public boolean contains (uv uv) {
		float u = uv.u(), v = uv.v();
		if (u < 0.001625f || u > 0.65f || v < 0.0159f || v > 0.6f) return false;
		boolean inside = false;
		float u1 = points[1], v1 = points[2], minDistSq = EPSILON * EPSILON;
		for (int i = 4, n = points.length; i < n; i += 3) {
			float u2 = points[i], v2 = points[i + 1];
			if (v1 > v != v2 > v && u < u1 + (v - v1) * (u2 - u1) / (v2 - v1)) inside = !inside;
			float dx = u2 - u1, dy = v2 - v1; // Check line distance for epsilon.
			float lengthSq = dx * dx + dy * dy;
			if (lengthSq > 0) {
				float t = clamp(((u - u1) * dx + (v - v1) * dy) / lengthSq);
				float nearestU = u1 + t * dx, nearestV = v1 + t * dy;
				float distSq = (u - nearestU) * (u - nearestU) + (v - nearestV) * (v - nearestV);
				if (distSq < minDistSq) return true;
			}
			u1 = u2;
			v1 = v2;
		}
		return inside;
	}

	/** Returns the wavelength of the pure spectral color that, when mixed with the white point, produces the specified color.
	 * @return [380..700] nm, or [-380..-700] for complementary colors (purples), or NaN if the color is achromatic. */
	static public float dominantWavelength (uv color, XYZ whitePoint) {
		float cu = color.u(), cv = color.v();
		uv wuv = Colors.uv(Colors.xy(whitePoint));
		float wu = wuv.u(), wv = wuv.v(), dx = cu - wu, dy = cv - wv;
		if (Math.abs(dx) < EPSILON && Math.abs(dy) < EPSILON) return Float.NaN; // Achromatic (on white point).
		// Spectral locus intersections.
		float w1 = points[0], w2, bestWavelength = Float.NaN, bestDistance = Integer.MAX_VALUE;
		float u1 = points[1], v1 = points[2], u2, v2;
		for (int i = 3, n = points.length - 3; i < n; i += 3, w1 = w2, u1 = u2, v1 = v2) {
			w2 = points[i];
			u2 = points[i + 1];
			v2 = points[i + 2];
			float sx = u2 - u1, sy = v2 - v1, denom = dx * sy - dy * sx;
			if (Math.abs(denom) < EPSILON) continue; // Parallel lines
			float distance = ((u1 - wu) * sy - (v1 - wv) * sx) / denom;
			if (distance < 0) continue; // Intersection is in wrong direction.
			float s = ((u1 - wu) * dy - (v1 - wv) * dx) / denom;
			if (s < 0 || s > 1) continue; // Intersection outside line segment.
			float wavelength = w1 + s * (w2 - w1);
			if (distance < bestDistance) {
				bestDistance = distance;
				bestWavelength = wavelength;
			}
		}
		// Check purple line intersection.
		int lastIndex = points.length - 3; // Purple line connects violet (first point) to red (second-to-last point).
		float violetU = points[1], violetV = points[2];
		float redU = points[lastIndex - 2], redV = points[lastIndex - 1];
		float px = redU - violetU, py = redV - violetV, denom = dx * py - dy * px;
		if (Math.abs(denom) > EPSILON) {
			float t = ((violetU - wu) * py - (violetV - wv) * px) / denom;
			float s = ((violetU - wu) * dy - (violetV - wv) * dx) / denom;
			if (t > 0 && s >= 0 && s <= 1 && t < bestDistance) return -(380 + s * (700 - 380)); // Interpolate on the opposite side.
		}
		return bestWavelength;
	}

	/** Uses {@link Colors.Illuminant.CIE2#D65}. */
	static public float dominantWavelength (uv color) {
		return dominantWavelength(color, Illuminant.CIE2.D65);
	}

	/** Returns the ratio of the distance from the white point to the color divided by the distance from the white point to the
	 * spectral locus (or purple line).
	 * @return 0 (achromatic) to 1 (pure spectral color), or NaN if the color is outside the spectral locus or invalid. */
	static public float excitationPurity (uv color, XYZ whitePoint) {
		float cu = color.u(), cv = color.v();
		uv wuv = Colors.uv(Colors.xy(whitePoint));
		float wu = wuv.u(), wv = wuv.v(), dx = cu - wu, dy = cv - wv;
		float colorDist = (float)Math.sqrt(dx * dx + dy * dy); // White point to color.
		if (colorDist < EPSILON) return 0; // Achromatic (on white point).
		float wavelength = dominantWavelength(color, whitePoint);
		if (Float.isNaN(wavelength)) return Float.NaN;
		uv locusPoint;
		if (wavelength > 0)
			locusPoint = uv(wavelength);
		else { // Complementary wavelength.
			locusPoint = purpleLineIntersection(dx, dy, wu, wv);
			if (Float.isNaN(locusPoint.u())) return Float.NaN;
		}
		dx = locusPoint.u() - wu;
		dy = locusPoint.v() - wv;
		float locusDist = (float)Math.sqrt(dx * dx + dy * dy); // White point to locus.
		return Math.min(colorDist / locusDist, 1);
	}

	static private uv purpleLineIntersection (float dx, float dy, float wu, float wv) {
		int lastIndex = points.length - 3;
		float u1 = points[1], v1 = points[2]; // Violet end.
		float u2 = points[lastIndex - 2], v2 = points[lastIndex - 1]; // Red end.
		float sx = u2 - u1, sy = v2 - v1, denom = dx * sy - dy * sx;
		if (Math.abs(denom) < EPSILON) return new uv(Float.NaN, Float.NaN); // Parallel lines.
		float t = ((u1 - wu) * sy - (v1 - wv) * sx) / denom;
		if (t < 0) return new uv(Float.NaN, Float.NaN); // Wrong direction.
		float s = ((u1 - wu) * dy - (v1 - wv) * dx) / denom;
		if (s < 0 || s > 1) return new uv(Float.NaN, Float.NaN); // Outside purple line.
		return new uv(u1 + s * sx, v1 + s * sy);
	}
}
