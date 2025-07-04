
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Util;

/** Uniform Color Space based on CAM16, for color difference calculations. */
public record CAM16UCS (
	/** Lightness (J*) [0..100]. */
	float J,
	/** Red-green component (a*) [-50..50]. */
	float a,
	/** Yellow-blue component (b*) [-50..50]. */
	float b) {

	public float h () {
		float h = (float)Math.atan2(b, a) * radDeg;
		return h < 0 ? h + 360 : h;
	}

	public float C () {
		return (float)Math.sqrt(a * a + b * b);
	}

	public float get (int index) {
		return switch (index) {
		case 0 -> J;
		case 1 -> a;
		case 2 -> b;
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM16UCS set (int index, float value) {
		return switch (index) {
		case 0 -> new CAM16UCS(value, a, b);
		case 1 -> new CAM16UCS(J, value, b);
		case 2 -> new CAM16UCS(J, a, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public CAM16 CAM16 () {
		return CAM16(CAM16.VC.sRGB);
	}

	public CAM16 CAM16 (CAM16.VC vc) { // Based on Copyright 2021 Google LLC (Apache 2.0).
		float C = (float)(Math.expm1(C() * 0.0228) / 0.0228) / vc.FLRoot();
		float h = (float)Math.atan2(b, a) * radDeg;
		if (h < 0) h += 360;
		float J = this.J / (1 - (this.J - 100) * 0.007f), sqrtJ = (float)Math.sqrt(J / 100);
		return new CAM16(J, C, h, 4 / vc.c() * sqrtJ * (vc.Aw() + 4) * vc.FLRoot(), C * vc.FLRoot(),
			50 * (float)Math.sqrt((C / sqrtJ * vc.c()) / (vc.Aw() + 4)));
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public Lab Lab () {
		return Lab(CAM16.VC.sRGB);
	}

	public Lab Lab (CAM16.VC vc) {
		return CAM16(vc).Lab(vc);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public LinearRGB LinearRGB () {
		return LinearRGB(CAM16.VC.sRGB);
	}

	public LinearRGB LinearRGB (CAM16.VC vc) {
		return CAM16(vc).LinearRGB(vc);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public RGB RGB () {
		return RGB(CAM16.VC.sRGB);
	}

	public RGB RGB (CAM16.VC vc) {
		return CAM16(vc).RGB(vc);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public uv uv () {
		return uv(CAM16.VC.sRGB);
	}

	public uv uv (CAM16.VC vc) {
		return CAM16(vc).uv(vc);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public xy xy () {
		return xy(CAM16.VC.sRGB);
	}

	public xy xy (CAM16.VC vc) {
		return CAM16(vc).xy(vc);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public XYZ XYZ () {
		return XYZ(CAM16.VC.sRGB);
	}

	public XYZ XYZ (CAM16.VC vc) {
		return CAM16(vc).XYZ(vc);
	}

	public CAM16UCS add (float value) {
		return new CAM16UCS(J + value, a + value, b + value);
	}

	public CAM16UCS add (int index, float value) {
		return switch (index) {
		case 0 -> new CAM16UCS(J + value, a, b);
		case 1 -> new CAM16UCS(J, a + value, b);
		case 2 -> new CAM16UCS(J, a, b + value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM16UCS add (float J, float a, float b) {
		return new CAM16UCS(this.J + J, this.a + a, this.b + b);
	}

	public CAM16UCS lerp (CAM16UCS other, float t) {
		return new CAM16UCS(Util.lerp(J, other.J, t), Util.lerp(a, other.a, t), Util.lerp(b, other.b, t));
	}

	public CAM16UCS sub (float value) {
		return new CAM16UCS(J - value, a - value, b - value);
	}

	public CAM16UCS sub (int index, float value) {
		return switch (index) {
		case 0 -> new CAM16UCS(J - value, a, b);
		case 1 -> new CAM16UCS(J, a - value, b);
		case 2 -> new CAM16UCS(J, a, b - value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM16UCS sub (float J, float a, float b) {
		return new CAM16UCS(this.J - J, this.a - a, this.b - b);
	}

	public float dst (CAM16UCS other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (CAM16UCS other) {
		float dJ = J - other.J, da = a - other.a, db = b - other.b;
		return dJ * dJ + da * da + db * db;
	}

	public float len () {
		return (float)Math.sqrt(len2());
	}

	public float len2 () {
		return J * J + a * a + b * b;
	}

	public CAM16UCS withJ (float J) {
		return new CAM16UCS(J, a, b);
	}
}
