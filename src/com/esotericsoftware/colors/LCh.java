
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.Util.*;

/** Cylindrical CIELAB. */
public record LCh (
	/** Lightness (L*) [0..100]. */
	float L,
	/** Chroma (C*) [0+]. */
	float C,
	/** Hue [0..360] or NaN if achromatic. */
	float h) {

	public Lab Lab () {
		if (C < EPSILON || Float.isNaN(h)) return new Lab(L, 0, 0);
		return new Lab(L, C * (float)Math.cos(h * degRad), C * (float)Math.sin(h * degRad));
	}

	/** Uses {@link Illuminant.CIE2#D65}. */
	public RGB RGB () {
		return Lab().RGB(Illuminant.CIE2.D65);
	}

	public LCh lerp (LCh other, float t) {
		return new LCh(Colors.lerp(L, other.L, t), Colors.lerp(C, other.C, t), lerpAngle(h, other.h, t));
	}
}
