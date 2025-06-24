
package com.esotericsoftware.colors.space;

import static com.esotericsoftware.colors.Util.*;

/** Academy Color Encoding System working space for CGI (linear, AP1 primaries). */
public record ACEScg (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) {

	public RGB RGB () {
		float rLinear = 1.70482663f * r + -0.62151743f * g + -0.08330920f * b; // From AP1.
		float gLinear = -0.13028185f * r + 1.14085365f * g + -0.01057180f * b;
		float bLinear = -0.02400720f * r + -0.12895973f * g + 1.15296693f * b;
		return new RGB(sRGB(rLinear), sRGB(gLinear), sRGB(bLinear));
	}
}
