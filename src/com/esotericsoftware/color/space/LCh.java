
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Illuminant;
import com.esotericsoftware.color.Illuminant.CIE2;
import com.esotericsoftware.color.Color;
import com.esotericsoftware.color.Util;

/** Cylindrical CIELAB. */
public record LCh (
	/** Lightness (L*) [0..100]. */
	float L,
	/** Chroma (C*) [0+]. */
	float C,
	/** Hue [0..360] or NaN if achromatic. */
	float h) implements Color {

	public Lab Lab () {
		if (C < EPSILON || Float.isNaN(h)) return new Lab(L, 0, 0);
		return new Lab(L, C * (float)Math.cos(h * degRad), C * (float)Math.sin(h * degRad));
	}

	/** Uses {@link CIE2#D65}. */
	public LinearRGB LinearRGB () {
		return XYZ(CIE2.D65).LinearRGB();
	}

	/** @param whitePoint See {@link Illuminant}. */
	public LinearRGB LinearRGB (XYZ whitePoint) {
		return XYZ(whitePoint).LinearRGB();
	}

	/** Uses {@link CIE2#D65}. */
	public RGB RGB () {
		return XYZ(CIE2.D65).RGB();
	}

	/** @param whitePoint See {@link Illuminant}. */
	public RGB RGB (XYZ whitePoint) {
		return XYZ(whitePoint).RGB();
	}

	/** Uses {@link CIE2#D65}. */
	public xy xy () {
		return XYZ(CIE2.D65).xy();
	}

	/** @param whitePoint See {@link Illuminant}. */
	public xy xy (XYZ whitePoint) {
		return XYZ(whitePoint).xy();
	}

	/** Uses {@link CIE2#D65}. */
	public XYZ XYZ () {
		return XYZ(CIE2.D65);
	}

	/** @param whitePoint See {@link Illuminant}. */
	public XYZ XYZ (XYZ whitePoint) {
		return Lab().XYZ(whitePoint);
	}

	public LCh lerp (LCh other, float t) {
		return new LCh(Util.lerp(L, other.L, t), Util.lerp(C, other.C, t), lerpAngle(h, other.h, t));
	}
}
