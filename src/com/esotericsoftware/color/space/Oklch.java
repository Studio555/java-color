
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Util;

/** Cylindrical Oklab. */
public record Oklch (
	/** Lightness [0..1]. */
	float L,
	/** Chroma [0+]. */
	float C,
	/** Hue [0..360] or NaN if achromatic. */
	float h) implements Color {

	public Oklab Oklab () {
		float h = this.h * degRad;
		if (C < EPSILON || Float.isNaN(h)) return new Oklab(L, 0, 0);
		return new Oklab(L, C * (float)Math.cos(h), C * (float)Math.sin(h));
	}

	public RGB RGB () {
		return Oklab().RGB();
	}

	public LinearRGB LinearRGB () {
		return Oklab().LinearRGB();
	}

	public uv uv () {
		return Oklab().uv();
	}

	public xy xy () {
		return Oklab().xy();
	}

	public XYZ XYZ () {
		return Oklab().XYZ();
	}

	public Oklch lerp (Oklch other, float t) {
		return new Oklch(Util.lerp(L, other.L, t), Util.lerp(C, other.C, t), lerpAngle(h, other.h, t));
	}

	@SuppressWarnings("all")
	public Oklch Oklch () {
		return this;
	}
}
