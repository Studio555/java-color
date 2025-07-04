
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Util;

/** Uniform Color Space based on CAM02, for color difference calculations. */
public record CAM02SCD (
	/** Lightness (J*) [0..100]. */
	float J,
	/** Red-green component (a*) [-50..50]. */
	float a,
	/** Yellow-blue component (b*) [-50..50]. */
	float b) {

	public float C () {
		return (float)Math.sqrt(a * a + b * b);
	}

	public float h () {
		float h = (float)Math.atan2(b, a) * radDeg;
		return h < 0 ? h + 360 : h;
	}

	public float get (int index) {
		return switch (index) {
		case 0 -> J;
		case 1 -> a;
		case 2 -> b;
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM02SCD set (int index, float value) {
		return switch (index) {
		case 0 -> new CAM02SCD(value, a, b);
		case 1 -> new CAM02SCD(J, value, b);
		case 2 -> new CAM02SCD(J, a, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public CAM02 CAM02 () {
		return CAM02(CAM02.VC.sRGB);
	}

	public CAM02 CAM02 (CAM02.VC vc) {
		// Coefficients for CAM02-SCD
		float c_1 = 0.007f;
		float c_2 = 0.0363f;

		// Reverse transformation
		float Jp = this.J;
		float J = -Jp / (c_1 * Jp - 1 - 100 * c_1);

		// Calculate M' and h from a' and b'
		float Mstar = (float)Math.sqrt(a * a + b * b);
		float h = (float)Math.atan2(b, a);

		// Reverse M transformation
		float M = (float)(Math.expm1(Mstar / (1 / c_2)) / c_2);

		// Convert h to degrees
		h = h * radDeg;
		h = h < 0 ? h + 360 : h;

		// Compute C from M using viewing conditions
		float C = M / (float)Math.pow(vc.FL(), 0.25);

		// Compute Q and s from J and M
		float Q = 4 / vc.c() * (float)Math.sqrt(Math.abs(J) / 100) * (vc.Aw() + 4) * (float)Math.pow(vc.FL(), 0.25);
		float s = (M == 0 || Q == 0) ? 0 : 100 * (float)Math.sqrt(Math.abs(M / Q));

		return new CAM02(J, C, h, Q, M, s);
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public Lab Lab () {
		return Lab(CAM02.VC.sRGB);
	}

	public Lab Lab (CAM02.VC vc) {
		return CAM02(vc).Lab(vc);
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public LinearRGB LinearRGB () {
		return LinearRGB(CAM02.VC.sRGB);
	}

	public LinearRGB LinearRGB (CAM02.VC vc) {
		return CAM02(vc).LinearRGB(vc);
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public RGB RGB () {
		return RGB(CAM02.VC.sRGB);
	}

	public RGB RGB (CAM02.VC vc) {
		return CAM02(vc).RGB(vc);
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public uv uv () {
		return uv(CAM02.VC.sRGB);
	}

	public uv uv (CAM02.VC vc) {
		return CAM02(vc).uv(vc);
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public xy xy () {
		return xy(CAM02.VC.sRGB);
	}

	public xy xy (CAM02.VC vc) {
		return CAM02(vc).xy(vc);
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	public XYZ XYZ () {
		return XYZ(CAM02.VC.sRGB);
	}

	public XYZ XYZ (CAM02.VC vc) {
		return CAM02(vc).XYZ(vc);
	}

	public CAM02SCD add (float value) {
		return new CAM02SCD(J + value, a + value, b + value);
	}

	public CAM02SCD add (int index, float value) {
		return switch (index) {
		case 0 -> new CAM02SCD(J + value, a, b);
		case 1 -> new CAM02SCD(J, a + value, b);
		case 2 -> new CAM02SCD(J, a, b + value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM02SCD add (float J, float a, float b) {
		return new CAM02SCD(this.J + J, this.a + a, this.b + b);
	}

	public CAM02SCD lerp (CAM02SCD other, float t) {
		return new CAM02SCD(Util.lerp(J, other.J, t), Util.lerp(a, other.a, t), Util.lerp(b, other.b, t));
	}

	public CAM02SCD sub (float value) {
		return new CAM02SCD(J - value, a - value, b - value);
	}

	public CAM02SCD sub (int index, float value) {
		return switch (index) {
		case 0 -> new CAM02SCD(J - value, a, b);
		case 1 -> new CAM02SCD(J, a - value, b);
		case 2 -> new CAM02SCD(J, a, b - value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM02SCD sub (float J, float a, float b) {
		return new CAM02SCD(this.J - J, this.a - a, this.b - b);
	}

	public float dst (CAM02SCD other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (CAM02SCD other) {
		float dJ = J - other.J, da = a - other.a, db = b - other.b;
		return dJ * dJ + da * da + db * db;
	}

	public float len () {
		return (float)Math.sqrt(len2());
	}

	public float len2 () {
		return J * J + a * a + b * b;
	}

	public CAM02SCD withJ (float J) {
		return new CAM02SCD(J, a, b);
	}
}
