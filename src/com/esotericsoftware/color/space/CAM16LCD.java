
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Colors.*;

import com.esotericsoftware.color.Colors;
import com.esotericsoftware.color.space.CAMSpace.CAM16Space;

/** Uniform Color Space based on CAM16, specialized for large color difference calculations. */
public record CAM16LCD (
	/** Lightness (J*) [0..100]. */
	float J,
	/** Red-green component (a*) [-50..50]. */
	float a,
	/** Yellow-blue component (b*) [-50..50]. */
	float b) implements CAM16Space {

	public CAM16LCD set (int index, float value) {
		return switch (index) {
		case 0 -> new CAM16LCD(value, a, b);
		case 1 -> new CAM16LCD(J, value, b);
		case 2 -> new CAM16LCD(J, a, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM16 CAM16 (CAM16.VC vc) {
		float C = (float)(Math.expm1(C() * 0.15) / 0.15) / vc.FLRoot();
		float h = (float)Math.atan2(b, a) * radDeg;
		if (h < 0) h += 360;
		float J = this.J / (0.77f - 0.007f * this.J);
		float sqrtJ = (float)Math.sqrt(J / 100);
		return new CAM16(J, C, h, 4 / vc.c() * sqrtJ * (vc.Aw() + 4) * vc.FLRoot(), C * vc.FLRoot(),
			50 * (float)Math.sqrt((C / sqrtJ * vc.c()) / (vc.Aw() + 4)));
	}

	public CAM16LCD add (float value) {
		return new CAM16LCD(J + value, a + value, b + value);
	}

	public CAM16LCD add (int index, float value) {
		return switch (index) {
		case 0 -> new CAM16LCD(J + value, a, b);
		case 1 -> new CAM16LCD(J, a + value, b);
		case 2 -> new CAM16LCD(J, a, b + value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM16LCD add (float J, float a, float b) {
		return new CAM16LCD(this.J + J, this.a + a, this.b + b);
	}

	public CAM16LCD lerp (CAM16LCD other, float t) {
		return new CAM16LCD(Colors.lerp(J, other.J, t), Colors.lerp(a, other.a, t), Colors.lerp(b, other.b, t));
	}

	public CAM16LCD sub (float value) {
		return new CAM16LCD(J - value, a - value, b - value);
	}

	public CAM16LCD sub (int index, float value) {
		return switch (index) {
		case 0 -> new CAM16LCD(J - value, a, b);
		case 1 -> new CAM16LCD(J, a - value, b);
		case 2 -> new CAM16LCD(J, a, b - value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM16LCD sub (float J, float a, float b) {
		return new CAM16LCD(this.J - J, this.a - a, this.b - b);
	}

	public float dst (CAM16LCD other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (CAM16LCD other) {
		float dJ = J - other.J, da = a - other.a, db = b - other.b;
		return dJ * dJ + da * da + db * db;
	}

	public CAM16LCD withJ (float J) {
		return new CAM16LCD(J, a, b);
	}

	@SuppressWarnings("all")
	public CAM16LCD CAM16LCD () {
		return this;
	}
}
