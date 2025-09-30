
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Colors.*;

/** PAL analog TV color encoding. */
public record YUV (
	/** Luma (Y') [0..1]. */
	float Y,
	/** Blue chrominance [-0.5..0.5]. */
	float U,
	/** Red chrominance [-0.5..0.5]. */
	float V) implements Color {

	public LRGB LRGB () {
		float r = Y - 0.00000055f * U + 1.1398836f * V;
		float g = Y - 0.39464236f * U - 0.58062209f * V;
		float b = Y + 2.03206343f * U - 0.00000025f * V;
		return new LRGB(linear(r), linear(g), linear(b));
	}

	public RGB RGB () {
		float r = Y - 0.00000055f * U + 1.1398836f * V;
		float g = Y - 0.39464236f * U - 0.58062209f * V;
		float b = Y + 2.03206343f * U - 0.00000025f * V;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}

	@SuppressWarnings("all")
	public YUV YUV () {
		return this;
	}
}
