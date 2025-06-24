
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Util.*;

/** Uniform Color Space based on CAM16. For color difference calculations. */
public record CAM16UCS (
	/** Lightness (J*) [0..100]. */
	float J,
	/** Red-green component (a*) [-50..50]. */
	float a,
	/** Yellow-blue component (b*) [-50..50]. */
	float b) {

	/** Uses {@link CAM16.VC#sRGB}. */
	public CAM16 CAM16 () {
		return CAM16(CAM16.VC.sRGB);
	}

	public CAM16 CAM16 (CAM16.VC vc) { // Based on Copyright 2021 Google LLC (Apache 2.0).
		float C = (float)(Math.expm1(Math.hypot(a, b) * 0.0228) / 0.0228) / vc.FLRoot();
		float h = (float)Math.atan2(b, a) * radDeg;
		if (h < 0) h += 360;
		float J = this.J / (1 - (this.J - 100) * 0.007f), sqrtJ = (float)Math.sqrt(J / 100);
		return new CAM16(J, C, h, 4 / vc.c() * sqrtJ * (vc.Aw() + 4) * vc.FLRoot(), C * vc.FLRoot(),
			50 * (float)Math.sqrt((C / sqrtJ * vc.c()) / (vc.Aw() + 4)));
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public RGB RGB () {
		return RGB(CAM16.VC.sRGB);
	}

	public RGB RGB (CAM16.VC vc) {
		return CAM16(vc).RGB(vc);
	}

	/** Perceptual color difference. */
	public float deltaE (CAM16UCS other) {
		float dJ = J - other.J(), da = a - other.a(), db = b - other.b();
		return 1.41f * (float)Math.pow(Math.sqrt(dJ * dJ + da * da + db * db), 0.63);
	}

	public CAM16UCS lerp (CAM16UCS other, float t) {
		return new CAM16UCS(Colors.lerp(J, other.J, t), Colors.lerp(a, other.a, t), Colors.lerp(b, other.b, t));
	}
}
