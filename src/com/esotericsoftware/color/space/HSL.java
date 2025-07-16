
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Util;

/** Hue, Saturation, Lightness. Cylindrical RGB. */
public record HSL (
	/** Hue [0..360] or NaN if achromatic. */
	float H,
	/** Saturation [0..1]. */
	float S,
	/** Lightness [0..1]. */
	float L) implements Color {

	public LinearRGB LinearRGB () {
		float r = 0, g = 0, b = 0;
		if (S < EPSILON) // Gray.
			r = g = b = L;
		else {
			float H = this.H / 360;
			float v2 = L < 0.5f ? L * (1 + S) : L + S - L * S, v1 = 2 * L - v2;
			r = hueToRGB(v1, v2, H + 1 / 3f);
			g = hueToRGB(v1, v2, H);
			b = hueToRGB(v1, v2, H - 1 / 3f);
		}
		return new LinearRGB(linear(r), linear(g), linear(b));
	}

	public RGB RGB () {
		float r = 0, g = 0, b = 0;
		if (S < EPSILON) // Gray.
			r = g = b = L;
		else {
			float H = this.H / 360;
			float v2 = L < 0.5f ? L * (1 + S) : L + S - L * S, v1 = 2 * L - v2;
			r = hueToRGB(v1, v2, H + 1 / 3f);
			g = hueToRGB(v1, v2, H);
			b = hueToRGB(v1, v2, H - 1 / 3f);
		}
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}

	public float Y () {
		return RGB().Y();
	}

	public HSL lerp (HSL other, float t) {
		return new HSL(lerpAngle(H, other.H, t), Util.lerp(S, other.S, t), Util.lerp(L, other.L, t));
	}

	static private float hueToRGB (float v1, float v2, float vH) {
		if (vH < 0) vH += 1;
		if (vH > 1) vH -= 1;
		if (6 * vH < 1) return v1 + (v2 - v1) * 6 * vH;
		if (2 * vH < 1) return v2;
		if (3 * vH < 2) return v1 + (v2 - v1) * (2 / 3f - vH) * 6;
		return v1;
	}

	@SuppressWarnings("all")
	public HSL HSL () {
		return this;
	}
}
