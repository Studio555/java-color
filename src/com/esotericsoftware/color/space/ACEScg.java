
package com.esotericsoftware.color.space;

/** Academy Color Encoding System working space for CGI (linear, AP1 primaries). */
public record ACEScg (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) implements Color {

	public LinearRGB LinearRGB () {
		float rLinear = 1.70482663f * r + -0.62151743f * g + -0.0833092f * b; // ACES AP1 to linear sRGB.
		float gLinear = -0.13028185f * r + 1.14085365f * g + -0.0105718f * b;
		float bLinear = -0.0240072f * r + -0.12895973f * g + 1.15296693f * b;
		return new LinearRGB(rLinear, gLinear, bLinear);
	}

	public RGB RGB () {
		return LinearRGB().RGB();
	}

	public XYZ XYZ () {
		float X = 0.6624541811f * r + 0.2722287168f * g + 0.0051619419f * b; // ACES AP1 to XYZ D65.
		float Y = 0.1340042065f * r + 0.6740817658f * g + 0.0040607335f * b;
		float Z = 0.156187687f * r + 0.0536951054f * g + 0.7237067219f * b;
		return new XYZ(X * 100, Y * 100, Z * 100);
	}

	@SuppressWarnings("all")
	public ACEScg ACEScg () {
		return this;
	}
}
