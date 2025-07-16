
package com.esotericsoftware.color.space;

/** Academy Color Encoding System ACES2065-1 archival format (linear, AP0 primaries). */
public record ACES2065_1 (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) implements Color {

	public LinearRGB LinearRGB () {
		float rLinear = 2.52140088f * r + -1.1338984f * g + -0.38750249f * b; // ACES AP0 to linear sRGB.
		float gLinear = -0.27621892f * r + 1.37270743f * g + -0.09648852f * b;
		float bLinear = -0.01538264f * r + -0.1529724f * g + 1.16835505f * b;
		return new LinearRGB(rLinear, gLinear, bLinear);
	}

	public RGB RGB () {
		return LinearRGB().RGB();
	}

	public XYZ XYZ () {
		float X = 95.25523959f * r + 34.39664498f * g; // ACES AP0 to XYZ D65.
		float Y = 72.81660966f * g;
		float Z = 0.00936786f * r + -7.21325464f * g + 100.88251844f * b;
		return new XYZ(X, Y, Z);
	}

	public float Y () {
		return 72.81660966f * g;
	}

	@SuppressWarnings("all")
	public ACES2065_1 ACES2065_1 () {
		return this;
	}
}
