
package com.esotericsoftware.color.space;

import com.esotericsoftware.color.Color;

/** Subtractive color model for printing. */
public record CMYK (
	/** Cyan [0..1]. */
	float C,
	/** Magenta [0..1]. */
	float M,
	/** Yellow [0..1]. */
	float Y,
	/** Key (black) [0..1]. */
	float K) implements Color {

	public RGB RGB () {
		return new RGB( //
			(1 - C) * (1 - K), //
			(1 - M) * (1 - K), //
			(1 - Y) * (1 - K));
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}
}
