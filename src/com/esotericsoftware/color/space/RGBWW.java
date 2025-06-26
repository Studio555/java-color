
package com.esotericsoftware.color.space;

/** RGB with 2 white channels for LEDs. */
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
	float w2) {}
