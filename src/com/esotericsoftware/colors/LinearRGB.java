
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;

/** RGB without gamma correction. Values are not clamped. */
public record LinearRGB (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) {

	public RGB RGB () {
		return new RGB(sRGB(clamp(r)), sRGB(clamp(g)), sRGB(clamp(b)));
	}

	public XYZ XYZ () {
		return new XYZ( //
			(0.4124564f * r + 0.3575761f * g + 0.1804375f * b) * 100, //
			(0.2126729f * r + 0.7151522f * g + 0.0721750f * b) * 100, //
			(0.0193339f * r + 0.1191920f * g + 0.9503041f * b) * 100);
	}

	public XYZ XYZ (Gamut gamut) {
		float[][] rgbToXYZ = gamut.RGB_XYZ;
		float X = rgbToXYZ[0][0] * r + rgbToXYZ[0][1] * g + rgbToXYZ[0][2] * b;
		float Y = rgbToXYZ[1][0] * r + rgbToXYZ[1][1] * g + rgbToXYZ[1][2] * b;
		float Z = rgbToXYZ[2][0] * r + rgbToXYZ[2][1] * g + rgbToXYZ[2][2] * b;
		return new XYZ(X * 100, Y * 100, Z * 100);
	}

	public LinearRGB lerp (LinearRGB other, float t) {
		return new LinearRGB(Colors.lerp(r, other.r, t), Colors.lerp(g, other.g, t), Colors.lerp(b, other.b, t));
	}
}
