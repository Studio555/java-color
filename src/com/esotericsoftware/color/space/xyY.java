
package com.esotericsoftware.color.space;

/** CIE xyY combining chromaticity with luminance. */
public record xyY (
	/** x chromaticity [0..1]. */
	float x,
	/** y chromaticity [0..1]. */
	float y,
	/** Luminance Y [0+]. */
	float Y) {

	/** @return NaN X and Z if y is 0. */
	public XYZ XYZ () {
		double X = (double)x * Y / y;
		double Z = (1.0 - x - y) * Y / y;
		return new XYZ((float)X, Y, (float)Z);
	}

	public xy xy () {
		return new xy(x, y);
	}
}
