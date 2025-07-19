
package com.esotericsoftware.color.space;

/** Linear RGB with 1 white channel for LEDs. Values are clamped [0..1] */
public record RGBW (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b,
	/** White [0..1]. */
	float w) {

	public RGBW (int rgb) {
		this( //
			((rgb & 0xff0000) >>> 24) / 255f, //
			((rgb & 0xff0000) >>> 16) / 255f, //
			((rgb & 0x00ff00) >>> 8) / 255f, //
			((rgb & 0x0000ff)) / 255f);
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

	/** @return [0..255] */
	public int w8 () {
		return Math.round(w * 255);
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
	public int w16 () {
		return Math.round(w * 65535);
	}
}
