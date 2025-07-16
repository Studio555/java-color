
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Illuminant;
import com.esotericsoftware.color.Observer;
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

	/** @param whitePoint See {@link Illuminant}. */
	public LinearRGB LinearRGB (XYZ whitePoint) {
		return XYZ(whitePoint).LinearRGB();
	}

	/** @param whitePoint See {@link Illuminant}. */
	public RGB RGB (XYZ whitePoint) {
		return XYZ(whitePoint).RGB();
	}

	/** @param whitePoint See {@link Illuminant}. */
	public xy xy (XYZ whitePoint) {
		return XYZ(whitePoint).xy();
	}

	/** Uses {@link Observer#Default} D65. */
	public XYZ XYZ () {
		return XYZ(Observer.Default.D65);
	}

	/** @param whitePoint See {@link Illuminant}. */
	public XYZ XYZ (XYZ whitePoint) {
		return Lab().XYZ(whitePoint);
	}

	/** Uses {@link Observer#Default} D65. */
	public float Y () {
		return Y(Observer.Default.D65);
	}

	public float Y (XYZ whitePoint) {
		return Lab().Y(whitePoint);
	}

	public LCh lerp (LCh other, float t) {
		return new LCh(Util.lerp(L, other.L, t), Util.lerp(C, other.C, t), lerpAngle(h, other.h, t));
	}

	@SuppressWarnings("all")
	public LCh LCh () {
		return this;
	}
}
