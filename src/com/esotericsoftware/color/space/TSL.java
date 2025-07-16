
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

/** Tint, Saturation, Lightness. For skin tone detection and analysis. */
public record TSL (
	/** Tint [0..1]. */
	float T,
	/** Saturation [0..1]. */
	float S,
	/** Lightness [0..1]. */
	float L) implements Color {

	public RGB RGB () {
		if (L < EPSILON) return new RGB(0, 0, 0); // Black.
		if (S < EPSILON) return new RGB(L, L, L); // Gray.
		float a = T * 360 * degRad, r1, g1;
		if (Math.abs(T) < EPSILON) {
			r1 = g1 = (float)Math.sqrt(5 * S * S / 18);
			if (Float.floatToIntBits(T) == 0x80000000) r1 = g1 = -r1; // -0f preserves the sign.
		} else {
			float tan = (float)Math.tan(a), x = (1 - 2 * tan) / (1 + tan);
			g1 = (float)Math.sqrt(5 / 9f * S * S / (x * x + 1));
			if (a >= PI) g1 = -g1;
			r1 = x * g1;
		}
		float k = L / (0.185f * r1 + 0.473f * g1 + 1 / 3f), r = k * (r1 + 1 / 3f), g = k * (g1 + 1 / 3f);
		return new RGB(clamp(r), clamp(g), clamp(k - r - g));
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}

	public float Y () {
		return RGB().Y();
	}

	@SuppressWarnings("all")
	public TSL TSL () {
		return this;
	}
}
