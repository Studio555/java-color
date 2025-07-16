
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Gamut;
import com.esotericsoftware.color.Util;

/** Linear RGB, without gamma correction. Values are not clamped. */
public record LRGB (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) implements Color {

	public LRGB (int rgb) {
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

	public LRGB set (int index, float value) {
		return switch (index) {
		case 0 -> new LRGB(value, g, b);
		case 1 -> new LRGB(r, value, b);
		case 2 -> new LRGB(r, g, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public RGB RGB () {
		return new RGB(sRGB(Util.clamp(r)), sRGB(Util.clamp(g)), sRGB(Util.clamp(b)));
	}

	/** Convert to RGBW using one calibrated white LED color. Moves power from RGB to W, maintaining this LRGB's brightness.
	 * 
	 * <pre>
	 * LRGB W = new LRGB(WR, WG, WB);
	 * W = W.scl(Wlux / W.Y());
	 * RGBWW result = targetLRGB.RGBW(W);
	 * </pre>
	 * 
	 * @param w White LED color scaled by relative luminance (may exceed 1). */
	public RGBW RGBW (LRGB w) {
		float ratioR = r / w.r, ratioG = g / w.g, ratioB = b / w.b; // How much of each channel white can provide.
		float W = Util.min(ratioR, ratioG, ratioB); // White is limited by the channel that needs the least white contribution.
		W = Math.min(W, 1); // Subtract white from each channel.
		return new RGBW(Math.max(0, r - W * w.r), Math.max(0, g - W * w.g), Math.max(0, b - W * w.b), W);
	}

	/** Convert to RGBWW using two calibrated white LED colors. Moves power from RGB to WW, maintaining this LRGB's brightness.
	 * 
	 * <pre>
	 * LRGB W1 = new LRGB(W1R, W1G, W1B);
	 * W1 = W1.scl(W1lux / W1.Y());
	 * LRGB W2 = new LRGB(W2R, W2G, W2B);
	 * W2 = W2.scl(W2lux / W2.Y());
	 * RGBWW result = targetLRGB.RGBWW(W1, W2);
	 * </pre>
	 * 
	 * @param w1 First white LED color scaled by relative luminance (may exceed 1).
	 * @param w2 Second white LED color. */
	public RGBWW RGBWW (LRGB w1, LRGB w2) {
		// W1 at maximum possible value, find best W2.
		float W1 = Float.MAX_VALUE;
		if (w1.r > EPSILON) W1 = Math.min(W1, r / w1.r);
		if (w1.g > EPSILON) W1 = Math.min(W1, g / w1.g);
		if (w1.b > EPSILON) W1 = Math.min(W1, b / w1.b);
		W1 = Math.min(W1, 1);
		float W2 = Float.MAX_VALUE;
		if (w2.r > EPSILON) W2 = Math.min(W2, Math.max(0, (r - W1 * w1.r) / w2.r));
		if (w2.g > EPSILON) W2 = Math.min(W2, Math.max(0, (g - W1 * w1.g) / w2.g));
		if (w2.b > EPSILON) W2 = Math.min(W2, Math.max(0, (b - W1 * w1.b) / w2.b));
		W2 = Math.min(W2, 1);
		float rOut = Math.max(0, r - W1 * w1.r - W2 * w2.r);
		float gOut = Math.max(0, g - W1 * w1.g - W2 * w2.g);
		float bOut = Math.max(0, b - W1 * w1.b - W2 * w2.b);
		float bestW1 = W1, bestW2 = W2, bestScore = (W1 + W2) - 0.1f * (rOut + gOut + bOut);
		// W2 at maximum possible value, find best W1.
		W2 = Float.MAX_VALUE;
		if (w2.r > EPSILON) W2 = Math.min(W2, r / w2.r);
		if (w2.g > EPSILON) W2 = Math.min(W2, g / w2.g);
		if (w2.b > EPSILON) W2 = Math.min(W2, b / w2.b);
		W2 = Math.min(W2, 1);
		W1 = Float.MAX_VALUE;
		if (w1.r > EPSILON) W1 = Math.min(W1, Math.max(0, (r - W2 * w2.r) / w1.r));
		if (w1.g > EPSILON) W1 = Math.min(W1, Math.max(0, (g - W2 * w2.g) / w1.g));
		if (w1.b > EPSILON) W1 = Math.min(W1, Math.max(0, (b - W2 * w2.b) / w1.b));
		W1 = Math.min(W1, 1);
		rOut = Math.max(0, r - W1 * w1.r - W2 * w2.r);
		gOut = Math.max(0, g - W1 * w1.g - W2 * w2.g);
		bOut = Math.max(0, b - W1 * w1.b - W2 * w2.b);
		float score = (W1 + W2) - 0.1f * (rOut + gOut + bOut);
		if (score > bestScore) {
			bestW1 = W1;
			bestW2 = W2;
			bestScore = score;
		}
		float det = w1.r * w2.g - w1.g * w2.r; // RG constraints.
		if (Math.abs(det) > EPSILON) {
			W1 = (r * w2.g - g * w2.r) / det;
			W2 = (g * w1.r - r * w1.g) / det;
			if (W1 >= 0 && W1 <= 1 && W2 >= 0 && W2 <= 1 && W1 * w1.b + W2 * w2.b <= b + EPSILON) {
				rOut = Math.max(0, r - W1 * w1.r - W2 * w2.r);
				gOut = Math.max(0, g - W1 * w1.g - W2 * w2.g);
				bOut = Math.max(0, b - W1 * w1.b - W2 * w2.b);
				score = (W1 + W2) - 0.1f * (rOut + gOut + bOut);
				if (score > bestScore) {
					bestW1 = W1;
					bestW2 = W2;
					bestScore = score;
				}
			}
		}
		det = w1.r * w2.b - w1.b * w2.r; // RB constraints.
		if (Math.abs(det) > EPSILON) {
			W1 = (r * w2.b - b * w2.r) / det;
			W2 = (b * w1.r - r * w1.b) / det;
			if (W1 >= 0 && W1 <= 1 && W2 >= 0 && W2 <= 1 && W1 * w1.g + W2 * w2.g <= g + EPSILON) {
				rOut = Math.max(0, r - W1 * w1.r - W2 * w2.r);
				gOut = Math.max(0, g - W1 * w1.g - W2 * w2.g);
				bOut = Math.max(0, b - W1 * w1.b - W2 * w2.b);
				score = (W1 + W2) - 0.1f * (rOut + gOut + bOut);
				if (score > bestScore) {
					bestW1 = W1;
					bestW2 = W2;
					bestScore = score;
				}
			}
		}
		det = w1.g * w2.b - w1.b * w2.g; // GB constraints.
		if (Math.abs(det) > EPSILON) {
			W1 = (g * w2.b - b * w2.g) / det;
			W2 = (b * w1.g - g * w1.b) / det;
			if (W1 >= 0 && W1 <= 1 && W2 >= 0 && W2 <= 1 && W1 * w1.r + W2 * w2.r <= r + EPSILON) {
				rOut = Math.max(0, r - W1 * w1.r - W2 * w2.r);
				gOut = Math.max(0, g - W1 * w1.g - W2 * w2.g);
				bOut = Math.max(0, b - W1 * w1.b - W2 * w2.b);
				score = (W1 + W2) - 0.1f * (rOut + gOut + bOut);
				if (score > bestScore) {
					bestW1 = W1;
					bestW2 = W2;
					bestScore = score;
				}
			}
		}
		return new RGBWW( //
			Math.max(0, r - bestW1 * w1.r - bestW2 * w2.r), //
			Math.max(0, g - bestW1 * w1.g - bestW2 * w2.g), //
			Math.max(0, b - bestW1 * w1.b - bestW2 * w2.b), //
			bestW1, bestW2);
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

	public LRGB add (float value) {
		return new LRGB(r + value, g + value, b + value);
	}

	public LRGB add (int index, float value) {
		return switch (index) {
		case 0 -> new LRGB(r + value, g, b);
		case 1 -> new LRGB(r, g + value, b);
		case 2 -> new LRGB(r, g, b + value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LRGB add (float r, float g, float b) {
		return new LRGB(this.r + r, this.g + g, this.b + b);
	}

	public LRGB clamp () {
		return new LRGB(Util.clamp(r), Util.clamp(g), Util.clamp(b));
	}

	public LRGB lerp (LRGB other, float t) {
		return new LRGB(Util.lerp(r, other.r, t), Util.lerp(g, other.g, t), Util.lerp(b, other.b, t));
	}

	public float max () {
		return Util.max(r, g, b);
	}

	public float min () {
		return Util.min(r, g, b);
	}

	public LRGB nor () {
		float max = max();
		return max < EPSILON ? this : new LRGB(r / max, g / max, b / max);
	}

	public LRGB sub (float value) {
		return new LRGB(r - value, g - value, b - value);
	}

	public LRGB sub (int index, float value) {
		return switch (index) {
		case 0 -> new LRGB(r - value, g, b);
		case 1 -> new LRGB(r, g - value, b);
		case 2 -> new LRGB(r, g, b - value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LRGB sub (float r, float g, float b) {
		return new LRGB(this.r - r, this.g - g, this.b - b);
	}

	public LRGB scl (float value) {
		return new LRGB(r * value, g * value, b * value);
	}

	public LRGB scl (int index, float value) {
		return switch (index) {
		case 0 -> new LRGB(r * value, g, b);
		case 1 -> new LRGB(r, g * value, b);
		case 2 -> new LRGB(r, g, b * value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LRGB scl (float r, float g, float b) {
		return new LRGB(this.r * r, this.g * g, this.b * b);
	}

	public float dst (LRGB other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (LRGB other) {
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
	public LRGB LRGB () {
		return this;
	}
}
