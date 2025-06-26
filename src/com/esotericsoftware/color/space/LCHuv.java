
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

/** Cylindrical CIELUV. */
public record LCHuv (
	/** Lightness (L*) [0..100]. */
	float L,
	/** Chroma (C*) [0+]. */
	float C,
	/** Hue [0..360] or NaN if achromatic. */
	float H) {

	public Luv Luv () {
		if (C < EPSILON || Float.isNaN(H)) return new Luv(L, 0, 0);
		float rad = H * degRad;
		return new Luv(L, C * (float)Math.cos(rad), C * (float)Math.sin(rad));
	}
}
