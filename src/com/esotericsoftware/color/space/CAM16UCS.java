
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Util;
import com.esotericsoftware.color.space.CAMSpace.CAM16Space;

/** Uniform Color Space based on CAM16, for general color difference calculations. */
public record CAM16UCS (
	/** Lightness (J*) [0..100]. */
	float J,
	/** Red-green component (a*) [-50..50]. */
	float a,
	/** Yellow-blue component (b*) [-50..50]. */
	float b) implements CAM16Space {

	public CAM16UCS set (int index, float value) {
		return switch (index) {
		case 0 -> new CAM16UCS(value, a, b);
		case 1 -> new CAM16UCS(J, value, b);
		case 2 -> new CAM16UCS(J, a, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public CAM16 CAM16 (CAM16.VC vc) {
		float C = (float)(Math.expm1(C() * 0.0228) / 0.0228) / vc.FLRoot();
		float h = (float)Math.atan2(b, a) * radDeg;
		if (h < 0) h += 360;
		float J = this.J / (1 - (this.J - 100) * 0.007f), sqrtJ = (float)Math.sqrt(J / 100);
		return new CAM16(J, C, h, 4 / vc.c() * sqrtJ * (vc.Aw() + 4) * vc.FLRoot(), C * vc.FLRoot(),
			50 * (float)Math.sqrt((C / sqrtJ * vc.c()) / (vc.Aw() + 4)));
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

	public CAM16UCS withJ (float J) {
		return new CAM16UCS(J, a, b);
	}

	@SuppressWarnings("all")
	public CAM16UCS CAM16UCS () {
		return this;
	}
}
