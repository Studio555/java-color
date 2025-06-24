
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;
import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.Util.HCTUtil;
import com.esotericsoftware.colors.Util.LabUtil;

/** Material color system. {@link CAM16} hue/chroma with {@link Lab} L* tone. */
public record HCT (
	/** Hue angle [0..360]. */
	float h,
	/** Chroma [0+]. */
	float C,
	/** Tone (L*) [0..100]. */
	float T) {

	/** Uses {@link CAM16.VC#sRGB}. */
	public RGB RGB () {
		return RGB(CAM16.VC.sRGB);
	}

	public RGB RGB (CAM16.VC vc) {
		float h = this.h * degRad;
		if (T < 0.0001f) return new RGB(0, 0, 0); // Black.
		if (T > 99.9999f) return new RGB(1, 1, 1); // White.
		if (C < 0.0001f) { // Gray.
			float gray = sRGB(LabUtil.LstarToYn(T));
			return new RGB(gray, gray, gray);
		}
		float Y = LabUtil.LstarToY(T);
		RGB rgb = HCTUtil.findRGB(h, C, Y, vc);
		return rgb != null ? rgb : HCTUtil.bisectToLimit(Y, h);
	}

	public HCT lerp (HCT other, float t) {
		return new HCT(lerpAngle(h, other.h, t), Colors.lerp(C, other.C, t), Colors.lerp(T, other.T, t));
	}
}
