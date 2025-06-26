
package com.esotericsoftware.color.space;

/** Intensity, Hue, Saturation. Alternative to HSI with different hue calculation. */
public record IHS (
	/** Intensity [0..1]. */
	float I,
	/** Hue [0..3] RGB sector or NaN if achromatic. */
	float H,
	/** Saturation [0..1]. */
	float S) {

	public RGB RGB () {
		float r, g, b;
		if (H >= 0 && H <= 1) {
			r = I * (1 + 2 * S - 3 * S * H) / 3;
			g = I * (1 - S + 3 * S * H) / 3;
			b = I * (1 - S) / 3;
		} else if (H >= 1 && H <= 2) {
			r = I * (1 - S) / 3;
			g = I * (1 + 2 * S - 3 * S * (H - 1)) / 3;
			b = I * (1 - S + 3 * S * (H - 1)) / 3;
		} else {
			r = I * (1 - S + 3 * S * (H - 2)) / 3;
			g = I * (1 - S) / 3;
			b = I * (1 + 2 * S - 3 * S * (H - 2)) / 3;
		}
		return new RGB(r, g, b);
	}
}
