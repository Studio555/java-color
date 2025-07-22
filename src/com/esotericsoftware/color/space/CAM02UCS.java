
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Colors.*;

import com.esotericsoftware.color.Colors;
import com.esotericsoftware.color.space.CAMSpace.CAM02Space;

/** Uniform Color Space based on CAM02, for general color difference calculations. */
public record CAM02UCS (
	/** Lightness (J') [0+]. */
	float J,
	/** Red-green (a'). */
	float a,
	/** Yellow-blue (b'). */
	float b) implements CAM02Space {

	public CAM02UCS set (int index, float value) {
		return switch (index) {
		case 0 -> new CAM02UCS(value, a, b);
		case 1 -> new CAM02UCS(J, value, b);
		case 2 -> new CAM02UCS(J, a, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM02 CAM02 (CAM02.VC vc) {
		if (J == 0) return new CAM02(0, 0, 0, 0, 0, 0);
		float c1 = 0.007f, c2 = 0.0228f, J = -this.J / (c1 * this.J - 1 - 100 * c1);
		float Mstar = (float)Math.sqrt(a * a + b * b), h = (float)Math.atan2(b, a) * radDeg;
		h = h < 0 ? h + 360 : h;
		float M = Mstar == 0 ? 0 : (float)(Math.expm1(Mstar / (1 / c2)) / c2), C = M / (float)Math.pow(vc.FL(), 0.25);
		float Q = J == 0 ? 0 : 4 / vc.c() * (float)Math.sqrt(Math.abs(J) / 100) * (vc.Aw() + 4) * (float)Math.pow(vc.FL(), 0.25);
		float s = M == 0 || Q == 0 ? 0 : 100 * (float)Math.sqrt(Math.abs(M / Q));
		return new CAM02(J, C, h, Q, M, s);
	}

	public CAM02UCS add (float value) {
		return new CAM02UCS(J + value, a + value, b + value);
	}

	public CAM02UCS add (int index, float value) {
		return switch (index) {
		case 0 -> new CAM02UCS(J + value, a, b);
		case 1 -> new CAM02UCS(J, a + value, b);
		case 2 -> new CAM02UCS(J, a, b + value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM02UCS add (float J, float a, float b) {
		return new CAM02UCS(this.J + J, this.a + a, this.b + b);
	}

	public CAM02UCS lerp (CAM02UCS other, float t) {
		return new CAM02UCS(Colors.lerp(J, other.J, t), Colors.lerp(a, other.a, t), Colors.lerp(b, other.b, t));
	}

	public CAM02UCS sub (float value) {
		return new CAM02UCS(J - value, a - value, b - value);
	}

	public CAM02UCS sub (int index, float value) {
		return switch (index) {
		case 0 -> new CAM02UCS(J - value, a, b);
		case 1 -> new CAM02UCS(J, a - value, b);
		case 2 -> new CAM02UCS(J, a, b - value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM02UCS sub (float J, float a, float b) {
		return new CAM02UCS(this.J - J, this.a - a, this.b - b);
	}

	public float dst (CAM02UCS other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (CAM02UCS other) {
		float dJ = J - other.J, da = a - other.a, db = b - other.b;
		return dJ * dJ + da * da + db * db;
	}

	public CAM02UCS withJ (float J) {
		return new CAM02UCS(J, a, b);
	}

	@SuppressWarnings("all")
	public CAM02UCS CAM02UCS () {
		return this;
	}
}
