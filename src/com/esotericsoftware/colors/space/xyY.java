
package com.esotericsoftware.colors.space;

import static com.esotericsoftware.colors.Util.*;

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
		if (y < EPSILON) return new XYZ(Float.NaN, Y, Float.NaN);
		return new XYZ( //
			x * Y / y, //
			Y, //
			(1 - x - y) * Y / y);
	}

	public xy xy () {
		return new xy(x, y);
	}
}
