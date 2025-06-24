
package com.esotericsoftware.colors.space;

import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.Illuminant.CIE2;
import com.esotericsoftware.colors.Util;

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

	/** Uses {@link CIE2#D65}. */
	public RGB RGB () {
		return Lab().RGB(CIE2.D65);
	}

	public LCh lerp (LCh other, float t) {
		return new LCh(Util.lerp(L, other.L, t), Util.lerp(C, other.C, t), lerpAngle(h, other.h, t));
	}
}
