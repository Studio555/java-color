
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Illuminant.CIE2.*;
import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.space.LinearRGB;
import com.esotericsoftware.colors.space.RGB;
import com.esotericsoftware.colors.space.xy;

/** @see RGB#XYZ(Gamut)
 * @see LinearRGB#XYZ(Gamut)
 * @see RGB#xy(Gamut gamut)
 * @see xy#RGB(Gamut)
 * @author Nathan Sweet <misc@n4te.com> */
public class Gamut {
	static public final Gamut //
	sRGB = new Gamut(new xy(0.64f, 0.33f), new xy(0.30f, 0.60f), new xy(0.15f, 0.06f)), //
		DisplayP3 = new Gamut(new xy(0.68f, 0.32f), new xy(0.265f, 0.69f), new xy(0.15f, 0.06f)), //
		Rec2020 = new Gamut(new xy(0.708f, 0.292f), new xy(0.170f, 0.797f), new xy(0.131f, 0.046f)), //
		all = new Gamut(new xy(1, 0), new xy(0, 1), new xy(0, 0)); //

	public final xy red, green, blue;
	public final float[][] RGB_XYZ, XYZ_RGB;

	public Gamut (xy red, xy green, xy blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		RGB_XYZ = RGB_XYZ();
		XYZ_RGB = invert3x3(RGB_XYZ);
	}

	private float[][] RGB_XYZ () {
		float Xr = red.x() / red.y();
		float Yr = 1;
		float Zr = (1 - red.x() - red.y()) / red.y();
		float Xg = green.x() / green.y();
		float Yg = 1;
		float Zg = (1 - green.x() - green.y()) / green.y();
		float Xb = blue.x() / blue.y();
		float Yb = 1;
		float Zb = (1 - blue.x() - blue.y()) / blue.y();
		float[][] M = { //
			{Xr, Xg, Xb}, //
			{Yr, Yg, Yb}, //
			{Zr, Zg, Zb}};
		float[] white = {D65.X() / D65.Y(), 1, D65.Z() / D65.Y()};
		float[] S = matrixSolve(M, white);
		return new float[][] { //
			{Xr * S[0], Xg * S[1], Xb * S[2]}, //
			{Yr * S[0], Yg * S[1], Yb * S[2]}, //
			{Zr * S[0], Zg * S[1], Zb * S[2]}};
	}

	public boolean contains (xy xy) {
		return isBelow(xy, blue, green) && isBelow(xy, green, red) && isAbove(xy, red, blue);
	}

	public xy clamp (xy xy) {
		if (contains(xy)) return xy;
		xy pAB = closestPointOnSegment(xy, red, green);
		xy pAC = closestPointOnSegment(xy, red, blue);
		xy pBC = closestPointOnSegment(xy, green, blue);
		float dAB = xy.dst2(pAB), dAC = xy.dst2(pAC), dBC = xy.dst2(pBC);
		float lowest = dAB;
		xy closestPoint = pAB;
		if (dAC < lowest) {
			lowest = dAC;
			closestPoint = pAC;
		}
		if (dBC < lowest) return pBC;
		return closestPoint;
	}

	/** Solves Ax = b for x using Cramer's rule for 3x3. */
	static private float[] matrixSolve (float[][] A, float[] b) {
		float det = A[0][0] * (A[1][1] * A[2][2] - A[2][1] * A[1][2]) //
			- A[0][1] * (A[1][0] * A[2][2] - A[1][2] * A[2][0]) //
			+ A[0][2] * (A[1][0] * A[2][1] - A[1][1] * A[2][0]);
		float det1 = b[0] * (A[1][1] * A[2][2] - A[2][1] * A[1][2]) //
			- A[0][1] * (b[1] * A[2][2] - A[1][2] * b[2]) //
			+ A[0][2] * (b[1] * A[2][1] - A[1][1] * b[2]);
		float det2 = A[0][0] * (b[1] * A[2][2] - b[2] * A[1][2]) //
			- b[0] * (A[1][0] * A[2][2] - A[1][2] * A[2][0]) //
			+ A[0][2] * (A[1][0] * b[2] - A[2][0] * b[1]);
		float det3 = A[0][0] * (A[1][1] * b[2] - A[2][1] * b[1]) //
			- A[0][1] * (A[1][0] * b[2] - A[2][0] * b[1]) //
			+ b[0] * (A[1][0] * A[2][1] - A[2][0] * A[1][1]);
		return new float[] {det1 / det, det2 / det, det3 / det};
	}

	static private float[][] invert3x3 (float[][] m) {
		float det = m[0][0] * (m[1][1] * m[2][2] - m[2][1] * m[1][2]) //
			- m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) //
			+ m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
		float invdet = 1 / det;
		float[][] inv = new float[3][3];
		inv[0][0] = (m[1][1] * m[2][2] - m[2][1] * m[1][2]) * invdet;
		inv[0][1] = (m[0][2] * m[2][1] - m[0][1] * m[2][2]) * invdet;
		inv[0][2] = (m[0][1] * m[1][2] - m[0][2] * m[1][1]) * invdet;
		inv[1][0] = (m[1][2] * m[2][0] - m[1][0] * m[2][2]) * invdet;
		inv[1][1] = (m[0][0] * m[2][2] - m[0][2] * m[2][0]) * invdet;
		inv[1][2] = (m[1][0] * m[0][2] - m[0][0] * m[1][2]) * invdet;
		inv[2][0] = (m[1][0] * m[2][1] - m[2][0] * m[1][1]) * invdet;
		inv[2][1] = (m[2][0] * m[0][1] - m[0][0] * m[2][1]) * invdet;
		inv[2][2] = (m[0][0] * m[1][1] - m[1][0] * m[0][1]) * invdet;
		return inv;
	}

	static private boolean isBelow (xy xy, xy a, xy b) {
		float xDiff = a.x() - b.x();
		if (xDiff < EPSILON) return true;
		float slope = (a.y() - b.y()) / xDiff;
		float intercept = a.y() - slope * a.x();
		float maxY = xy.x() * slope + intercept;
		return xy.y() <= maxY;
	}

	static private boolean isAbove (xy xy, xy a, xy b) {
		float xDiff = a.x() - b.x();
		if (xDiff < EPSILON) return true;
		float slope = (a.y() - b.y()) / xDiff;
		float intercept = a.y() - slope * a.x();
		float minY = xy.x() * slope + intercept;
		return xy.y() >= minY;
	}

	static private xy closestPointOnSegment (xy xy, xy a, xy b) {
		float APx = xy.x() - a.x(), APy = xy.y() - a.y();
		float ABx = b.x() - a.x(), ABy = b.y() - a.y();
		float ab2 = ABx * ABx + ABy * ABy;
		float ap_ab = APx * ABx + APy * ABy;
		float t = ap_ab / ab2;
		if (t < 0)
			t = 0;
		else if (t > 1) //
			t = 1;
		return new xy(a.x() + ABx * t, a.y() + ABy * t);
	}

	static public class PhilipsHue {
		static public final Gamut //
		wide = new Gamut(new xy(0.700607f, 0.299301f), new xy(0.172416f, 0.746797f), new xy(0.135503f, 0.039879f)),
			A = new Gamut(new xy(0.704f, 0.296f), new xy(0.2151f, 0.7106f), new xy(0.138f, 0.08f)), //
			B = new Gamut(new xy(0.675f, 0.322f), new xy(0.409f, 0.518f), new xy(0.167f, 0.04f)), //
			C = new Gamut(new xy(0.692f, 0.308f), new xy(0.17f, 0.7f), new xy(0.153f, 0.048f));

		/** Returns the gamut for the model identifier, or null.
		 * <p>
		 * This model list is no longer used by Philips. Use the gamut from the light. */
		static public Gamut forModel (String model) {
			return switch (model) {
			case "LLC001", // Monet, Renoir, Mondriaan (gen II)
				"LLC005", // Bloom (gen II)
				"LLC006", // Iris (gen III)
				"LLC007", // Bloom, Aura (gen III)
				"LLC010", // Iris
				"LLC011", // Hue Bloom
				"LLC012", // Hue Bloom
				"LLC013", // Storylight
				"LST001", // Light Strips
				"LLC014" // Bloom, Aura (gen III)
				-> A;
			case "LCT001", // Hue A19
				"LCT002", // Hue BR30
				"LCT003", // Hue GU10
				"LCT007", // Hue A19
				"LLM001" // Color Light Module
				-> B;
			case "LLC020", // Hue Go
				"LST002", // Hue LightStrips Plus
				"LCT010", // Hue A19 gen 3
				"LCT011", // Hue BR30
				"LCT012", // Hue color candle
				"LCT014", // Hue A19 gen 3
				"LCT015", // Hue A19 gen 3
				"LCT016" // Hue A19 gen 3
				-> C;
			default -> null;
			};
		}
	}
}
