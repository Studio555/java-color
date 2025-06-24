
package com.esotericsoftware.colors.space;

import static com.esotericsoftware.colors.Util.*;

/** Xerox YES color space. */
public record YES (
	/** Luminance [0..1]. */
	float Y,
	/** Red-green chrominance [-0.5..0.5]. */
	float E,
	/** Yellow-blue chrominance [-0.5..0.5]. */
	float S) {

	public RGB RGB () {
		float r = Y + E * 1.431f + S * 0.126f;
		float g = Y + E * -0.569f + S * 0.126f;
		float b = Y + E * 0.431f + S * -1.874f;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}
}
