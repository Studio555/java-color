
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Colors.*;

import com.esotericsoftware.color.Gamut;
import com.esotericsoftware.color.Colors;
import com.esotericsoftware.color.space.RGBWW.WW;

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
		return new RGB(sRGB(Colors.clamp(r)), sRGB(Colors.clamp(g)), sRGB(Colors.clamp(b)));
	}

	/** Convert to RGBW using one calibrated white LED color. Moves power from RGB to W.
	 * @param w White LED color. */
	public RGBW RGBW (LRGB w) {
		float W = 1;
		if (w.r > 0) W = Math.min(W, r / w.r);
		if (w.g > 0) W = Math.min(W, g / w.g);
		if (w.b > 0) W = Math.min(W, b / w.b);
		return new RGBW(Math.max(0, r - W * w.r), Math.max(0, g - W * w.g), Math.max(0, b - W * w.b), W);
	}

	/** Convert to RGBWW using two calibrated white LED colors. Moves power from RGB to WW.
	 * @param ww White LED colors with precomputed values. */
	public RGBWW RGBWW (WW ww) {
		float w1r = ww.r1(), w1g = ww.g1(), w1b = ww.b1();
		float w2r = ww.r2(), w2g = ww.g2(), w2b = ww.b2();
		// W1 at maximum possible value, find best W2.
		float W1 = Math.min(Math.min(r * ww.r1inv(), g * ww.g1inv()), b * ww.b1inv());
		float rt = r - W1 * w1r, gt = g - W1 * w1g, bt = b - W1 * w1b;
		float W2 = Math.max(0, rt * ww.r2inv());
		W2 = Math.min(W2, Math.max(0, gt * ww.g2inv()));
		W2 = Math.min(W2, Math.max(0, bt * ww.b2inv()));
		rt = Math.max(0, rt - W2 * w2r);
		gt = Math.max(0, gt - W2 * w2g);
		bt = Math.max(0, bt - W2 * w2b);
		float bestW1 = W1, bestW2 = W2, bestScore = W1 + W2 - (rt + gt + bt) * 0.1f;
		// W2 at maximum possible value, find best W1.
		W2 = Math.min(Math.min(r * ww.r2inv(), g * ww.g2inv()), b * ww.b2inv());
		rt = r - W2 * w2r;
		gt = g - W2 * w2g;
		bt = b - W2 * w2b;
		W1 = Math.max(0, rt * ww.r1inv());
		W1 = Math.min(W1, Math.max(0, gt * ww.g1inv()));
		W1 = Math.min(W1, Math.max(0, bt * ww.b1inv()));
		rt = Math.max(0, rt - W1 * w1r);
		gt = Math.max(0, gt - W1 * w1g);
		bt = Math.max(0, bt - W1 * w1b);
		float score = W1 + W2 - (rt + gt + bt) * 0.1f;
		if (score > bestScore) {
			bestW1 = W1;
			bestW2 = W2;
			bestScore = score;
		}
		// RG constraints.
		W1 = (r * w2g - g * w2r) * ww.rg();
		W2 = (g * w1r - r * w1g) * ww.rg();
		if (W1 >= 0 && W1 <= 1 && W2 >= 0 && W2 <= 1 && W1 * w1b + W2 * w2b <= b + EPSILON) {
			rt = Math.max(0, r - W1 * w1r - W2 * w2r);
			gt = Math.max(0, g - W1 * w1g - W2 * w2g);
			bt = Math.max(0, b - W1 * w1b - W2 * w2b);
			score = W1 + W2 - (rt + gt + bt) * 0.1f;
			if (score > bestScore) {
				bestW1 = W1;
				bestW2 = W2;
				bestScore = score;
			}
		}
		// RB constraints.
		W1 = (r * w2b - b * w2r) * ww.rb();
		W2 = (b * w1r - r * w1b) * ww.rb();
		if (W1 >= 0 && W1 <= 1 && W2 >= 0 && W2 <= 1 && W1 * w1g + W2 * w2g <= g + EPSILON) {
			rt = Math.max(0, r - W1 * w1r - W2 * w2r);
			gt = Math.max(0, g - W1 * w1g - W2 * w2g);
			bt = Math.max(0, b - W1 * w1b - W2 * w2b);
			score = W1 + W2 - (rt + gt + bt) * 0.1f;
			if (score > bestScore) {
				bestW1 = W1;
				bestW2 = W2;
				bestScore = score;
			}
		}
		// GB constraints.
		W1 = (g * w2b - b * w2g) * ww.gb();
		W2 = (b * w1g - g * w1b) * ww.gb();
		if (W1 >= 0 && W1 <= 1 && W2 >= 0 && W2 <= 1 && W1 * w1r + W2 * w2r <= r + EPSILON) {
			rt = Math.max(0, r - W1 * w1r - W2 * w2r);
			gt = Math.max(0, g - W1 * w1g - W2 * w2g);
			bt = Math.max(0, b - W1 * w1b - W2 * w2b);
			score = W1 + W2 - (rt + gt + bt) * 0.1f;
			if (score > bestScore) {
				bestW1 = W1;
				bestW2 = W2;
				bestScore = score;
			}
		}
		return new RGBWW( //
			Math.max(0, r - bestW1 * w1r - bestW2 * w2r), //
			Math.max(0, g - bestW1 * w1g - bestW2 * w2g), //
			Math.max(0, b - bestW1 * w1b - bestW2 * w2b), //
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
		return new LRGB(Colors.clamp(r), Colors.clamp(g), Colors.clamp(b));
	}

	public LRGB lerp (LRGB other, float t) {
		return new LRGB(Colors.lerp(r, other.r, t), Colors.lerp(g, other.g, t), Colors.lerp(b, other.b, t));
	}

	public float max () {
		return Colors.max(r, g, b);
	}

	public float min () {
		return Colors.min(r, g, b);
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

	/** @return [0..255] */
	public int r8 () {
		return Math.round(r * 255);
	}

	/** @return [0..255] */
	public int g8 () {
		return Math.round(g * 255);
	}

	/** @return [0..255] */
	public int b8 () {
		return Math.round(b * 255);
	}

	/** @return [0..65535] */
	public int r16 () {
		return Math.round(r * 65535);
	}

	/** @return [0..65535] */
	public int g16 () {
		return Math.round(g * 65535);
	}

	/** @return [0..65535] */
	public int b16 () {
		return Math.round(b * 65535);
	}

	@SuppressWarnings("all")
	public LRGB LRGB () {
		return this;
	}
}
