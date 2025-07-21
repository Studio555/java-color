
package com.esotericsoftware.color.space;

/** Linear RGB with 2 white channels for LEDs. Values are clamped [0..1]. */
public record RGBWW (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b,
	/** White 1 [0..1]. */
	float w1,
	/** White 2 [0..1]. */
	float w2) {

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

	/** @return [0..255] */
	public int w1_8 () {
		return Math.round(w1 * 255);
	}

	/** @return [0..255] */
	public int w2_8 () {
		return Math.round(w2 * 255);
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

	/** @return [0..65535] */
	public int w1_16 () {
		return Math.round(w2 * 65535);
	}

	/** @return [0..65535] */
	public int w2_16 () {
		return Math.round(w2 * 65535);
	}

	public record Wdet (float rg, float rb, float gb) {
		public Wdet (LRGB w1, LRGB w2) {
			this( //
				clamp(w1.r() * w2.g() - w1.g() * w2.r()), //
				clamp(w1.r() * w2.b() - w1.b() * w2.r()), //
				clamp(w1.g() * w2.b() - w1.b() * w2.g()));
		}

		static private float clamp (float value) {
			return 1 / (Math.abs(value) < 0.001f ? Math.copySign(0.001f, value) : value);
		}
	}
}
