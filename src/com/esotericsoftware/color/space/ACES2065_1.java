
package com.esotericsoftware.color.space;

/** Academy Color Encoding System ACES2065-1 archival format (linear, AP0 primaries). */
public record ACES2065_1 (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) {

	public LinearRGB LinearRGB () {
		float rLin = 2.52140088f * r + -1.1338984f * g + -0.38750249f * b; // ACES AP0 to linear sRGB.
		float gLin = -0.27621892f * r + 1.37270743f * g + -0.09648852f * b;
		float bLin = -0.01538264f * r + -0.1529724f * g + 1.16835505f * b;
		return new LinearRGB(rLin, gLin, bLin);
	}

	public RGB RGB () {
		return LinearRGB().RGB();
	}

	public XYZ XYZ () {
		float X = 0.9525523959f * r + 0.3439664498f * g; // ACES AP0 to XYZ D65.
		float Y = 0.7281660966f * g;
		float Z = 0.0000936786f * r + -0.0721325464f * g + 1.0088251844f * b;
		return new XYZ(X * 100, Y * 100, Z * 100);
	}
}
