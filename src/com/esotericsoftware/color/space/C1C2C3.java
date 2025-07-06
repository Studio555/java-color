
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

/** Opponent 3 color space. */
public record C1C2C3 (
	/** Achromatic channel [0..pi/2]. */
	float C1,
	/** Red-cyan opponent [0..pi/2]. */
	float C2,
	/** Yellow-violet opponent [0..pi/2]. */
	float C3) implements Color {

	public LinearRGB LinearRGB () {
		return RGB().LinearRGB();
	}

	public RGB RGB () {
		if (Float.isNaN(C1) || Float.isNaN(C2) || Float.isNaN(C3)) return new RGB(0, 0, 0); // Black
		if (Math.abs(C1 - C2) < EPSILON && Math.abs(C2 - C3) < EPSILON) { // If all angles are equal, it's gray.
			float gray = (float)Math.tan(C1);
			return new RGB(gray, gray, gray);
		}
		float tanC1 = (float)Math.tan(C1), tanC2 = (float)Math.tan(C2), tanC3 = (float)Math.tan(C3);
		float r, g, b;
		if (C1 >= Math.PI / 4 && C2 < Math.PI / 4 && C3 < Math.PI / 4) { // r is dominant (r > g and r > b).
			r = 1;
			g = tanC2;
			b = tanC3;
		} else if (C2 >= Math.PI / 4 && C1 < Math.PI / 4 && C3 < Math.PI / 4) { // g is dominant.
			g = 1;
			r = tanC1;
			b = tanC3;
		} else if (C3 >= Math.PI / 4 && C1 < Math.PI / 4 && C2 < Math.PI / 4) { // b is dominant.
			b = 1;
			r = tanC1;
			g = tanC2;
		} else { // No single dominant channel or multiple dominant channels. Just normalize based on the sum of tangents.
			float sum = tanC1 + tanC2 + tanC3;
			if (sum > EPSILON) {
				r = tanC1 / sum;
				g = tanC2 / sum;
				b = tanC3 / sum;
			} else
				r = g = b = 0; // Black
		}
		return new RGB(r, g, b);
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}
}
