
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Colors.*;

import com.esotericsoftware.color.Colors;

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

	public LRGB LRGB () {
		return Oklab().LRGB();
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

	public float Y () {
		return Oklab().Y();
	}

	public Oklch lerp (Oklch other, float t) {
		return new Oklch(Colors.lerp(L, other.L, t), Colors.lerp(C, other.C, t), lerpAngle(h, other.h, t));
	}

	@SuppressWarnings("all")
	public Oklch Oklch () {
		return this;
	}
}
