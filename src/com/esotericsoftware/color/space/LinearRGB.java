
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Gamut;
import com.esotericsoftware.color.Util;

/** RGB without gamma correction. Values are not clamped. */
public record LinearRGB (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) implements Color {

	public LinearRGB (int rgb) {
		this( //
			((rgb & 0xff0000) >>> 16) / 255f, //
			((rgb & 0x00ff00) >>> 8) / 255f, //
			((rgb & 0x0000ff)) / 255f);
	}

	public float get (int index) {
		return switch (index) {
		case 0 -> r;
		case 1 -> g;
		case 2 -> b;
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LinearRGB set (int index, float value) {
		return switch (index) {
		case 0 -> new LinearRGB(value, g, b);
		case 1 -> new LinearRGB(r, value, b);
		case 2 -> new LinearRGB(r, g, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public RGB RGB () {
		return new RGB(sRGB(Util.clamp(r)), sRGB(Util.clamp(g)), sRGB(Util.clamp(b)));
	}

	/** Convert to RGBW using one calibrated white LED color. Brightness of this RGB is preserved.
	 * @param w White LED color scaled by relative luminance (may exceed 1). Eg: wr *= wlux / rlux */
	public RGBW RGBW (LinearRGB w) {
		// How much of each channel the white LED can provide.
		float ratioR = r / w.r, ratioG = g / w.g, ratioB = b / w.b;
		// The white level is limited by the channel that needs the least white contribution.
		float W = Util.min(ratioR, ratioG, ratioB);
		W = Math.min(W, 1);
		// Subtract the white contribution from each channel.
		return new RGBW(Math.max(0, r - W * w.r), Math.max(0, g - W * w.g), Math.max(0, b - W * w.b), W);
	}

	/** Convert to RGBWW using two calibrated white LED colors. Brightness of this RGB is preserved.
	 * @param w1 First white LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @param w2 Second white LED color. */
	public RGBWW RGBWW (LinearRGB w1, LinearRGB w2) {
		// How much of each channel the white LED can provide.
		float ratioR1 = r / w1.r, ratioG1 = g / w1.g, ratioB1 = b / w1.b;
		float ratioR2 = r / w2.r, ratioG2 = g / w2.g, ratioB2 = b / w2.b;
		// The white level is limited by the channel that needs the least white contribution.
		float W1 = Util.min(ratioR1, ratioG1, ratioB1);
		float W2 = Util.min(ratioR2, ratioG2, ratioB2);
		// Subtract the white contribution from each channel.
		if (W1 > W2) return new RGBWW(Math.max(0, r - W1 * w1.r), Math.max(0, g - W1 * w1.g), Math.max(0, b - W1 * w1.b), W1, 0);
		return new RGBWW(Math.max(0, r - W2 * w2.r), Math.max(0, g - W2 * w2.g), Math.max(0, b - W2 * w2.b), 0, W2);
	}

	/** @return NaN if invalid. */
	public uv uv () {
		return xy().uv();
	}

	/** Uses {@link Gamut#sRGB}.
	 * @return NaN if invalid. */
	public xy xy () {
		return Gamut.sRGB.xy(this);
	}

	public XYZ XYZ () {
		return new XYZ( //
			41.24564f * r + 35.75761f * g + 18.04375f * b, //
			21.26729f * r + 71.51522f * g + 7.2175f * b, //
			1.93339f * r + 11.9192f * g + 95.03041f * b);
	}

	public float Y () {
		return 21.26729f * r + 71.51522f * g + 7.2175f * b;
	}

	public LinearRGB add (float value) {
		return new LinearRGB(r + value, g + value, b + value);
	}

	public LinearRGB add (int index, float value) {
		return switch (index) {
		case 0 -> new LinearRGB(r + value, g, b);
		case 1 -> new LinearRGB(r, g + value, b);
		case 2 -> new LinearRGB(r, g, b + value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LinearRGB add (float r, float g, float b) {
		return new LinearRGB(this.r + r, this.g + g, this.b + b);
	}

	public LinearRGB clamp () {
		return new LinearRGB(Util.clamp(r), Util.clamp(g), Util.clamp(b));
	}

	public LinearRGB lerp (LinearRGB other, float t) {
		return new LinearRGB(Util.lerp(r, other.r, t), Util.lerp(g, other.g, t), Util.lerp(b, other.b, t));
	}

	public float max () {
		return Util.max(r, g, b);
	}

	public float min () {
		return Util.min(r, g, b);
	}

	public LinearRGB nor () {
		float max = max();
		return max < EPSILON ? this : new LinearRGB(r / max, g / max, b / max);
	}

	public LinearRGB sub (float value) {
		return new LinearRGB(r - value, g - value, b - value);
	}

	public LinearRGB sub (int index, float value) {
		return switch (index) {
		case 0 -> new LinearRGB(r - value, g, b);
		case 1 -> new LinearRGB(r, g - value, b);
		case 2 -> new LinearRGB(r, g, b - value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LinearRGB sub (float r, float g, float b) {
		return new LinearRGB(this.r - r, this.g - g, this.b - b);
	}

	public LinearRGB scl (float value) {
		return new LinearRGB(r * value, g * value, b * value);
	}

	public LinearRGB scl (int index, float value) {
		return switch (index) {
		case 0 -> new LinearRGB(r * value, g, b);
		case 1 -> new LinearRGB(r, g * value, b);
		case 2 -> new LinearRGB(r, g, b * value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LinearRGB scl (float r, float g, float b) {
		return new LinearRGB(this.r * r, this.g * g, this.b * b);
	}

	public float dst (LinearRGB other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (LinearRGB other) {
		float dr = r - other.r, dg = g - other.g, db = b - other.b;
		return dr * dr + dg * dg + db * db;
	}

	public float len () {
		return (float)Math.sqrt(len2());
	}

	public float len2 () {
		return r * r + g * g + b * b;
	}

	@SuppressWarnings("all")
	public LinearRGB LinearRGB () {
		return this;
	}
}
