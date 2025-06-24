
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.Util.HSLUtil;

/** Hue, Saturation, Lightness. Cylindrical RGB. */
public record HSL (
	/** Hue [0..360] or NaN if achromatic. */
	float H,
	/** Saturation [0..1]. */
	float S,
	/** Lightness [0..1]. */
	float L) {

	public RGB RGB () {
		float r = 0, g = 0, b = 0;
		if (S < EPSILON) // Gray.
			r = g = b = L;
		else {
			float H = this.H / 360;
			float v2 = L < 0.5f ? L * (1 + S) : L + S - L * S, v1 = 2 * L - v2;
			r = HSLUtil.hueToRGB(v1, v2, H + 1 / 3f);
			g = HSLUtil.hueToRGB(v1, v2, H);
			b = HSLUtil.hueToRGB(v1, v2, H - 1 / 3f);
		}
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	public HSL lerp (HSL other, float t) {
		return new HSL(lerpAngle(H, other.H, t), Colors.lerp(S, other.S, t), Colors.lerp(L, other.L, t));
	}
}
