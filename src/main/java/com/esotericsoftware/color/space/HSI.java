
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Colors.*;

/** Hue, Saturation, Intensity. */
public record HSI (
	/** Hue [0..360] or NaN if achromatic. */
	float H,
	/** Saturation [0..1]. */
	float S,
	/** Intensity [0..1]. */
	float I) implements Color {

	public LRGB LRGB () {
		float H = this.H * degRad;
		float r, g, b;
		if (S < EPSILON) // Gray.
			r = g = b = I;
		else if (H >= 0 && H < 2 * PI / 3) {
			b = I * (1 - S);
			r = I * (1 + S * (float)Math.cos(H) / (float)Math.cos(PI / 3 - H));
			g = 3 * I - r - b;
		} else if (H >= 2 * PI / 3 && H < 4 * PI / 3) {
			H = H - 2 * PI / 3;
			r = I * (1 - S);
			g = I * (1 + S * (float)Math.cos(H) / (float)Math.cos(PI / 3 - H));
			b = 3 * I - r - g;
		} else {
			H = H - 4 * PI / 3;
			g = I * (1 - S);
			b = I * (1 + S * (float)Math.cos(H) / (float)Math.cos(PI / 3 - H));
			r = 3 * I - g - b;
		}
		return new LRGB(linear(r), linear(g), linear(b));
	}

	public RGB RGB () {
		float H = this.H * degRad;
		float r, g, b;
		if (S < EPSILON) // Gray.
			r = g = b = I;
		else if (H >= 0 && H < 2 * PI / 3) {
			b = I * (1 - S);
			r = I * (1 + S * (float)Math.cos(H) / (float)Math.cos(PI / 3 - H));
			g = 3 * I - r - b;
		} else if (H >= 2 * PI / 3 && H < 4 * PI / 3) {
			H = H - 2 * PI / 3;
			r = I * (1 - S);
			g = I * (1 + S * (float)Math.cos(H) / (float)Math.cos(PI / 3 - H));
			b = 3 * I - r - g;
		} else {
			H = H - 4 * PI / 3;
			g = I * (1 - S);
			b = I * (1 + S * (float)Math.cos(H) / (float)Math.cos(PI / 3 - H));
			r = 3 * I - g - b;
		}
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}

	public float Y () {
		return RGB().Y();
	}

	@SuppressWarnings("all")
	public HSI HSI () {
		return this;
	}
}
