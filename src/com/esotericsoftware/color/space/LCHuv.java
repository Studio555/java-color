
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Illuminant;
import com.esotericsoftware.color.Illuminant.CIE2;

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

	/** Uses {@link CIE2#D65}. */
	public RGB RGB () {
		return XYZ(CIE2.D65).RGB();
	}

	/** @param tristimulus See {@link Illuminant}. */
	public RGB RGB (XYZ tristimulus) {
		return XYZ(tristimulus).RGB();
	}

	/** Uses {@link CIE2#D65}. */
	public LinearRGB LinearRGB () {
		return XYZ(CIE2.D65).LinearRGB();
	}

	/** @param tristimulus See {@link Illuminant}. */
	public LinearRGB LinearRGB (XYZ tristimulus) {
		return XYZ(tristimulus).LinearRGB();
	}

	/** Uses {@link CIE2#D65}. */
	public uv uv () {
		return XYZ(CIE2.D65).uv();
	}

	/** @param tristimulus See {@link Illuminant}. */
	public uv uv (XYZ tristimulus) {
		return XYZ(tristimulus).uv();
	}

	/** Uses {@link CIE2#D65}. */
	public xy xy () {
		return XYZ(CIE2.D65).xy();
	}

	/** @param tristimulus See {@link Illuminant}. */
	public xy xy (XYZ tristimulus) {
		return XYZ(tristimulus).xy();
	}

	/** Uses {@link CIE2#D65}. */
	public XYZ XYZ () {
		return XYZ(CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	public XYZ XYZ (XYZ tristimulus) {
		return Luv().XYZ(tristimulus);
	}
}
