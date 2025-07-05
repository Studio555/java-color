
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Util;
import com.esotericsoftware.color.space.CAMSpace.CAM16Space;

/** Uniform Color Space based on CAM16, specialized for small color difference calculations. */
public record CAM16SCD (
	/** Lightness (J*) [0..100]. */
	float J,
	/** Red-green component (a*) [-50..50]. */
	float a,
	/** Yellow-blue component (b*) [-50..50]. */
	float b) implements CAM16Space {

	public CAM16SCD set (int index, float value) {
		return switch (index) {
		case 0 -> new CAM16SCD(value, a, b);
		case 1 -> new CAM16SCD(J, value, b);
		case 2 -> new CAM16SCD(J, a, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM16 CAM16 (CAM16.VC vc) {
		float C = (float)(Math.expm1(C() * 0.0228) / 0.0228) / vc.FLRoot();
		float h = (float)Math.atan2(b, a) * radDeg;
		if (h < 0) h += 360;
		float J = this.J / (1.65f - 0.007f * this.J);
		float sqrtJ = (float)Math.sqrt(J / 100);
		return new CAM16(J, C, h, 4 / vc.c() * sqrtJ * (vc.Aw() + 4) * vc.FLRoot(), C * vc.FLRoot(),
			50 * (float)Math.sqrt((C / sqrtJ * vc.c()) / (vc.Aw() + 4)));
	}

	public CAM16SCD add (float value) {
		return new CAM16SCD(J + value, a + value, b + value);
	}

	public CAM16SCD add (int index, float value) {
		return switch (index) {
		case 0 -> new CAM16SCD(J + value, a, b);
		case 1 -> new CAM16SCD(J, a + value, b);
		case 2 -> new CAM16SCD(J, a, b + value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM16SCD add (float J, float a, float b) {
		return new CAM16SCD(this.J + J, this.a + a, this.b + b);
	}

	public CAM16SCD lerp (CAM16SCD other, float t) {
		return new CAM16SCD(Util.lerp(J, other.J, t), Util.lerp(a, other.a, t), Util.lerp(b, other.b, t));
	}

	public CAM16SCD sub (float value) {
		return new CAM16SCD(J - value, a - value, b - value);
	}

	public CAM16SCD sub (int index, float value) {
		return switch (index) {
		case 0 -> new CAM16SCD(J - value, a, b);
		case 1 -> new CAM16SCD(J, a - value, b);
		case 2 -> new CAM16SCD(J, a, b - value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM16SCD sub (float J, float a, float b) {
		return new CAM16SCD(this.J - J, this.a - a, this.b - b);
	}

	public float dst (CAM16SCD other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (CAM16SCD other) {
		float dJ = J - other.J, da = a - other.a, db = b - other.b;
		return dJ * dJ + da * da + db * db;
	}

	public CAM16SCD withJ (float J) {
		return new CAM16SCD(J, a, b);
	}

	@SuppressWarnings("all")
	public CAM16SCD CAM16SCD () {
		return this;
	}
}
