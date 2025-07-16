
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Illuminant;
import com.esotericsoftware.color.Observer;

/** Cylindrical CIELUV. */
public record LCHuv (
	/** Lightness (L*) [0..100]. */
	float L,
	/** Chroma (C*) [0+]. */
	float C,
	/** Hue [0..360] or NaN if achromatic. */
	float H) implements Color {

	public Luv Luv () {
		if (C < EPSILON || Float.isNaN(H)) return new Luv(L, 0, 0);
		float rad = H * degRad;
		return new Luv(L, C * (float)Math.cos(rad), C * (float)Math.sin(rad));
	}

	/** @param whitePoint See {@link Illuminant}. */
	public RGB RGB (XYZ whitePoint) {
		return XYZ(whitePoint).RGB();
	}

	/** @param whitePoint See {@link Illuminant}. */
	public LRGB LRGB (XYZ whitePoint) {
		return XYZ(whitePoint).LRGB();
	}

	/** @param whitePoint See {@link Illuminant}. */
	public uv uv (XYZ whitePoint) {
		return XYZ(whitePoint).uv();
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
		return Luv().XYZ(whitePoint);
	}

	/** Uses {@link Observer#Default} D65. */
	public float Y () {
		return Y(Observer.Default.D65);
	}

	public float Y (XYZ whitePoint) {
		return Lab().Y(whitePoint);
	}

	@SuppressWarnings("all")
	public LCHuv LCHuv () {
		return this;
	}
}
