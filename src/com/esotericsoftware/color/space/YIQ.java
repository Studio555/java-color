
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

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
		float g = 1 * Y - 0.2721221f * I - 0.6473806f * Q;
		float b = 1 * Y - 1.10698902f * I + 1.704615f * Q;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}
}
