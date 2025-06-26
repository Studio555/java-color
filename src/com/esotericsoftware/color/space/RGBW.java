
package com.esotericsoftware.color.space;

/** RGB with 1 white channel for LEDs. */
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
}
