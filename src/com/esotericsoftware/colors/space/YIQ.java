
package com.esotericsoftware.colors.space;

import static com.esotericsoftware.colors.Util.*;

/** NTSC analog TV color encoding. */
public record YIQ (
	/** Luma (Y') [0..1]. */
	float Y,
	/** In-phase (orange-blue) [-0.5..0.5]. */
	float I,
	/** Quadrature (purple-green) [-0.5..0.5]. */
	float Q) {

	public RGB RGB () {
		float r = 1 * Y + 0.95629572f * I + 0.62102442f * Q;
		float g = 1 * Y - 0.27212210f * I - 0.64738060f * Q;
		float b = 1 * Y - 1.10698902f * I + 1.70461500f * Q;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}
}
