
package com.esotericsoftware.color.space;

/** Academy Color Encoding System for color grading (logarithmic, AP1 primaries). */
public record ACEScc (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) implements Color {

	public LRGB LRGB () {
		return new ACEScg(decode(r), decode(g), decode(b)).LRGB();
	}

	public RGB RGB () {
		return LRGB().RGB();
	}

	public XYZ XYZ () {
		return new ACEScg(decode(r), decode(g), decode(b)).XYZ();
	}

	static float encode (float linear) {
		if (linear <= 0) return -0.3584474886f; // (log2(pow(2,-16)) + 9.72) / 17.52
		if (linear < 0.00003051757812f) // pow(2, -15)
			return (float)((Math.log(0.00001525878906f + linear * 0.5f) / Math.log(2) + 9.72f) / 17.52f);
		return (float)((Math.log(linear) / Math.log(2) + 9.72f) / 17.52f);
	}

	static float decode (float encoded) {
		if (encoded < -0.3014698893f) // (9.72 - 15) / 17.52
			return (float)((Math.pow(2, encoded * 17.52f - 9.72f) - 0.00001525878906f) * 2);
		return (float)Math.pow(2, encoded * 17.52f - 9.72f);
	}

	@SuppressWarnings("all")
	public ACEScc ACEScc () {
		return this;
	}
}
