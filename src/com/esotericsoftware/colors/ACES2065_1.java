
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;

/** Academy Color Encoding System ACES2065-1 archival format (linear, AP0 primaries). */
public record ACES2065_1 (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) {

	public RGB RGB () {
		float rLin = 2.52140088f * r + -1.13389840f * g + -0.38750249f * b; // From AP0.
		float gLin = -0.27621892f * r + 1.37270743f * g + -0.09648852f * b;
		float bLin = -0.01538264f * r + -0.15297240f * g + 1.16835505f * b;
		return new RGB(sRGB(rLin), sRGB(gLin), sRGB(bLin));
	}
}
