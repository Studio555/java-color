
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Util;

/** Uniform Color Space based on CAM02, specialized for small color difference calculations. */
public record CAM02SCD (
	/** Lightness (J*) [0..100]. */
	float J,
	/** Red-green component (a*) [-50..50]. */
	float a,
	/** Yellow-blue component (b*) [-50..50]. */
	float b) implements CAM02Space {

	public CAM02SCD set (int index, float value) {
		return switch (index) {
		case 0 -> new CAM02SCD(value, a, b);
		case 1 -> new CAM02SCD(J, value, b);
		case 2 -> new CAM02SCD(J, a, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM02 CAM02 (CAM02.VC vc) {
		if (J == 0) return new CAM02(0, 0, 0, 0, 0, 0);
		float c1 = 0.007f, c2 = 0.0363f, J = -this.J / (c1 * this.J - 1 - 100 * c1);
		float Mstar = (float)Math.sqrt(a * a + b * b), h = (float)Math.atan2(b, a) * radDeg;
		h = h < 0 ? h + 360 : h;
		float M = Mstar == 0 ? 0 : (float)(Math.expm1(Mstar / (1 / c2)) / c2), C = M / (float)Math.pow(vc.FL(), 0.25);
		float Q = J == 0 ? 0 : 4 / vc.c() * (float)Math.sqrt(Math.abs(J) / 100) * (vc.Aw() + 4) * (float)Math.pow(vc.FL(), 0.25);
		float s = M == 0 || Q == 0 ? 0 : 100 * (float)Math.sqrt(Math.abs(M / Q));
		return new CAM02(J, C, h, Q, M, s);
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

	public CAM02SCD withJ (float J) {
		return new CAM02SCD(J, a, b);
	}

	@SuppressWarnings("all")
	public CAM02SCD CAM02SCD () {
		return this;
	}
}
