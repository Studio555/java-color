
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

/** Normalized red-green color space. */
public record rg (
	/** Red chromaticity [0..1]. */
	float r,
	/** Green chromaticity [0..1]. */
	float g,
	/** Blue chromaticity [0..1]. */
	float b,
	/** Saturation [0..1]. */
	float s,
	/** Hue [0..360] or NaN if achromatic. */
	float h) implements Color {

	public RGB RGB (float luminance) {
		return new RGB(clamp(r * luminance), clamp(g * luminance), clamp(b * luminance));
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}

	@SuppressWarnings("all")
	public rg rg () {
		return this;
	}
}
