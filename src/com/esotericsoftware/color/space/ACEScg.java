
package com.esotericsoftware.color.space;

/** Academy Color Encoding System working space for CGI (linear, AP1 primaries). */
public record ACEScg (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) implements Color {

	public LRGB LRGB () {
		float rLinear = 1.70482663f * r + -0.62151743f * g + -0.0833092f * b; // ACES AP1 to linear sRGB.
		float gLinear = -0.13028185f * r + 1.14085365f * g + -0.0105718f * b;
		float bLinear = -0.0240072f * r + -0.12895973f * g + 1.15296693f * b;
		return new LRGB(rLinear, gLinear, bLinear);
	}

	public RGB RGB () {
		return LRGB().RGB();
	}

	public XYZ XYZ () {
		float X = 66.24541811f * r + 27.22287168f * g + 0.51619419f * b; // ACES AP1 to XYZ D65.
		float Y = 13.40042065f * r + 67.40817658f * g + 0.40607335f * b;
		float Z = 15.6187687f * r +  5.36951054f * g +  72.37067219f * b;
		return new XYZ(X, Y, Z);
	}

	public float Y () {
		return 13.40042065f * r + 67.40817658f * g + 0.40607335f * b;
	}

	@SuppressWarnings("all")
	public ACEScg ACEScg () {
		return this;
	}
}
