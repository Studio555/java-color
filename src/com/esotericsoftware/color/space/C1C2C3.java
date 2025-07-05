
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
		// Handle special cases
		if (Float.isNaN(C1) || Float.isNaN(C2) || Float.isNaN(C3)) {
			return new RGB(0, 0, 0); // Black
		}

		// If all angles are equal, it's a gray color
		if (Math.abs(C1 - C2) < EPSILON && Math.abs(C2 - C3) < EPSILON) {
			// For gray: r = g = b, so atan(r/max(g,b)) = atan(1) = π/4
			// The intensity can be derived from the fact that for equal RGB values,
			// each component divided by the max of the others equals 1
			float gray = (float)Math.tan(C1); // tan(π/4) = 1 for white, less for darker grays
			return new RGB(gray, gray, gray);
		}

		// General case: solve the system of equations
		// C1 = atan(r / max(g, b))
		// C2 = atan(g / max(r, b))
		// C3 = atan(b / max(r, g))

		float tanC1 = (float)Math.tan(C1);
		float tanC2 = (float)Math.tan(C2);
		float tanC3 = (float)Math.tan(C3);

		// Determine which component is largest based on the tangent values
		// If tan(angle) > 1, then the numerator > denominator
		// If tan(angle) = 1, then numerator = denominator (π/4)
		// If tan(angle) < 1, then numerator < denominator

		float r, g, b;

		// Check which configuration makes sense
		if (C1 >= Math.PI / 4 && C2 < Math.PI / 4 && C3 < Math.PI / 4) {
			// r is dominant (r > g and r > b)
			r = 1;
			// From C2: g = tan(C2) * max(r,b) = tan(C2) * r (since r > b)
			g = tanC2;
			// From C3: b = tan(C3) * max(r,g) = tan(C3) * r (since r > g)
			b = tanC3;
		} else if (C2 >= Math.PI / 4 && C1 < Math.PI / 4 && C3 < Math.PI / 4) {
			// g is dominant
			g = 1;
			r = tanC1;
			b = tanC3;
		} else if (C3 >= Math.PI / 4 && C1 < Math.PI / 4 && C2 < Math.PI / 4) {
			// b is dominant
			b = 1;
			r = tanC1;
			g = tanC2;
		} else {
			// No single dominant channel or multiple dominant channels
			// This is a more complex case - use an iterative approach or approximation
			// For now, normalize based on the sum of tangents
			float sum = tanC1 + tanC2 + tanC3;
			if (sum > EPSILON) {
				r = tanC1 / sum;
				g = tanC2 / sum;
				b = tanC3 / sum;
			} else {
				r = g = b = 0; // Black
			}
		}

		return new RGB(r, g, b);
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}
}
