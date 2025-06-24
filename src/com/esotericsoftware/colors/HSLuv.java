
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.Util.HSLuvUtil;

/** Human-friendly {@link HSL}. Perceptually uniform saturation and lightness. */
public record HSLuv (
	/** Hue [0..360] or NaN if achromatic. */
	float H,
	/** Saturation [0..100]. */
	float S,
	/** Lightness [0..100]. */
	float L) {

	/** @return NaN if invalid. */
	public RGB RGB () {
		if (L > 100 - EPSILON) return new RGB(1, 1, 1);
		if (L < EPSILON) return new RGB(0, 0, 0);
		return new LCHuv(L, HSLuvUtil.maxChromaForLH(L, H) * S / 100, H).Luv().RGB();
	}

	public HSLuv lerp (HSLuv other, float t) {
		return new HSLuv(lerpAngle(H, other.H, t), Colors.lerp(S, other.S, t), Colors.lerp(L, other.L, t));
	}
}
