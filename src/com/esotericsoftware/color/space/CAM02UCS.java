
package com.esotericsoftware.color.space;

/** Uniform Color Space based on CAM02, for color difference calculations. */
public record CAM02UCS (
	/** Lightness (J') [0+]. */
	float J,
	/** Red-green (a'). */
	float a,
	/** Yellow-blue (b'). */
	float b) {

	public float C () {
		return (float)Math.sqrt(a * a + b * b);
	}

	public float h () {
		float h = (float)Math.atan2(b, a) * 180 / (float)Math.PI;
		return h < 0 ? h + 360 : h;
	}

	public float dst (CAM02UCS other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (CAM02UCS other) {
		float dJ = J - other.J, da = a - other.a, db = b - other.b;
		return dJ * dJ + da * da + db * db;
	}
}
