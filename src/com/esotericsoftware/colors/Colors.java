
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Util.*;

import java.lang.reflect.RecordComponent;

import com.esotericsoftware.colors.Util.CCTUtil;
import com.esotericsoftware.colors.Util.XYZUtil;

/** @author Nathan Sweet <misc@n4te.com> */
public class Colors {
	/** @param CCT [1667..25000K]
	 * @return NaN if invalid. */
	static public RGB RGB (float CCT, float Duv) {
		return xy(CCT, Duv).RGB();
	}

	/** Convert CCT to RGBW using one calibrated white LED color. Brightness is maximized.
	 * @param CCT [1667..25000K]
	 * @param brightness [0..1]
	 * @param w White LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @return NaN if invalid. */
	static public RGBW RGBW (float CCT, float brightness, RGB w) {
		RGB target = RGB(CCT, 0);
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

	/** Convert CCT to RGBWW using two calibrated white LED colors. Brightness is maximized.
	 * @param CCT [1667..25000K]
	 * @param brightness [0..1]
	 * @param w1 First white LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @param w2 Second white LED color.
	 * @return NaN if invalid. */
	static public RGBWW RGBWW (float CCT, float brightness, RGB w1, RGB w2) {
		float cct1 = w1.uv().CCT();
		float cct2 = w2.uv().CCT();
		float W1, W2;
		if (Math.abs(cct2 - cct1) < EPSILON) // Both whites have same CCT.
			W1 = W2 = 0.5f;
		else {
			float ratio = clamp((CCT - cct1) / (cct2 - cct1));
			W1 = 1 - ratio;
			W2 = ratio;
		}
		RGB target = RGB(CCT, 0);
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

	/** @param CCT [1667..25000K]
	 * @return NaN if invalid. */
	static public uv uv (float CCT, float Duv) {
		return xy(CCT, Duv).uv();
	}

	/** Computes CIE xy chromaticity coordinates from CCT using Planck's law. This is more accurate but slower than the polynomial
	 * approximation in xy(CCT, Duv). Use this method for CCT values below 1667K where xy(CCT, Duv) is not supported.
	 * @param CCT [100..100000K]
	 * @return NaN if invalid. */
	static public XYZ XYZ (float CCT) {
		if (CCT < 100 || CCT > 100000) return new XYZ(Float.NaN, Float.NaN, Float.NaN);
		float X = 0, Y = 0, Z = 0;
		for (int i = 0; i < 81; i++) {
			float lambda = (380 + i * 5) * 1e-9f; // nm to meters.
			float exponent = XYZUtil.c2 / (lambda * CCT);
			float B = exponent > 80 ? 0
				: XYZUtil.c1 / (lambda * lambda * lambda * lambda * lambda * ((float)Math.exp(exponent) - 1f));
			X += B * XYZUtil.Xbar[i];
			Y += B * XYZUtil.Ybar[i];
			Z += B * XYZUtil.Zbar[i];
		}
		return new XYZ(X, Y, Z);
	}

	/** @param CCT [1667..25000K]
	 * @return NaN if invalid. */
	static public xy xy (float CCT, float Duv) {
		if (CCT < 1667 || CCT > 25000) return new xy(Float.NaN, Float.NaN);
		float x, t2 = CCT * CCT; // Krystek's approximation.
		if (CCT >= 1667 && CCT <= 4000)
			x = -0.2661239f * 1e9f / (t2 * CCT) - 0.2343589f * 1e6f / t2 + 0.8776956f * 1e3f / CCT + 0.179910f;
		else // CCT > 4000 && CCT <= 25000
			x = -3.0258469f * 1e9f / (t2 * CCT) + 2.1070379f * 1e6f / t2 + 0.2226347f * 1e3f / CCT + 0.240390f;
		float y, xx = x * x;
		if (CCT >= 1667 && CCT <= 2222)
			y = -1.1063814f * xx * x - 1.34811020f * xx + 2.18555832f * x - 0.20219683f;
		else if (CCT > 2222 && CCT <= 4000)
			y = -0.9549476f * xx * x - 1.37418593f * xx + 2.09137015f * x - 0.16748867f;
		else // CCT > 4000 && CCT <= 25000
			y = 3.0817580f * xx * x - 5.87338670f * xx + 3.75112997f * x - 0.37001483f;
		xy xy = new xy(x, y);
		if (Duv == 0) return xy;
		uv1960 perp = CCTUtil.perpendicular(CCT, xy), uvBB = xy.uv1960();
		return new uv1960(uvBB.u() + perp.u() * Duv, uvBB.v() + perp.v() * Duv).xy();
	}

	static public float max (float a, float b, float c) {
		return Math.max(a, Math.max(b, c));
	}

	static public float min (float a, float b, float c) {
		return Math.min(a, Math.min(b, c));
	}

	/** @return [0..1]. */
	static public float clamp (float value) {
		return Math.max(0, Math.min(1, value));
	}

	static public float lerp (float from, float to, float t) {
		return from + (to - from) * t;
	}

	static public float lerpAngle (float from, float to, float t) {
		if (Float.isNaN(from) && Float.isNaN(to)) return 0;
		if (Float.isNaN(from)) return to;
		if (Float.isNaN(to)) return from;
		float diff = to - from;
		if (diff > 180)
			diff -= 360;
		else if (diff < -180) //
			diff += 360;
		float result = from + diff * t;
		if (result < 0)
			result += 360;
		else if (result >= 360) //
			result -= 360;
		return result;
	}

	/** @param linear [0..1]. */
	static public float gammaEncode (float linear, float gamma) {
		if (linear <= 0) return 0;
		if (linear >= 1) return 1;
		return (float)Math.pow(linear, 1 / gamma);
	}

	static public float gammaDecode (float encoded, float gamma) {
		if (encoded <= 0) return 0;
		if (encoded >= 1) return 1;
		return (float)Math.pow(encoded, gamma);
	}

	/** Linear to sRGB gamma correction. */
	static public float sRGB (float linear) {
		if (linear <= 0.0031308f) return 12.92f * linear;
		return (float)(1.055f * Math.pow(linear, 1 / 2.4) - 0.055);
	}

	/** sRGB to linear inverse gamma correction. */
	static public float linear (float srgb) {
		if (srgb <= 0.040449936f) return srgb / 12.92f;
		return (float)Math.pow((srgb + 0.055) / 1.055, 2.4);
	}

	/** @return [0..255] */
	static public int dmx8 (float value) {
		return Math.round(value * 255);
	}

	/** @return [0..65535] */
	static public int dmx16 (float value) {
		return (int)(value * 65535);
	}

	static public float[] floats (Record record) {
		RecordComponent[] components = record.getClass().getRecordComponents();
		float[] values = new float[components.length];
		try {
			for (int i = 0; i < components.length; i++)
				values[i] = (float)components[i].getAccessor().invoke(record);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return values;
	}

	static public String hex (Record record) {
		return hex(floats(record));
	}

	static public String hex (float... values) {
		StringBuilder buffer = new StringBuilder(values.length << 1);
		for (float value : values) {
			String hex = Integer.toHexString(Math.round(value * 255));
			if (hex.length() == 1) buffer.append('0');
			buffer.append(hex);
		}
		return buffer.toString();
	}

	static public String toString (Record record) {
		return toString(floats(record));
	}

	static public String toString (float... values) {
		StringBuilder buffer = new StringBuilder(values.length * 5);
		for (float value : values) {
			buffer.append(value);
			buffer.append(", ");
		}
		buffer.setLength(buffer.length() - 2);
		return buffer.toString();
	}

	static public String toString255 (Record record) {
		return toString255(floats(record));
	}

	static public String toString255 (float... values) {
		StringBuilder buffer = new StringBuilder(values.length * 5);
		for (float value : values) {
			buffer.append(Math.round(value * 255));
			buffer.append(", ");
		}
		buffer.setLength(buffer.length() - 2);
		return buffer.toString();
	}
}
