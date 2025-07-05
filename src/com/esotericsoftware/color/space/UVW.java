
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Observer;

/** CIE 1964 U*V*W* space (obsolete). */
public record UVW (float U, float V, float W) implements Color {
	/** Uses {@link Observer#CIE2} D65. */
	public XYZ XYZ () {
		return XYZ(Observer.CIE2.D65);
	}

	public XYZ XYZ (XYZ whitePoint) {
		float yRatio = (float)Math.pow((W + 17) / 25, 3); // Recover Y/Yn.
		float Y = yRatio * whitePoint.Y();
		float denom = whitePoint.X() + 15 * whitePoint.Y() + 3 * whitePoint.Z();
		if (denom < EPSILON) return new XYZ(0, 0, 0);
		float un = 4 * whitePoint.X() / denom, vn = 9 * whitePoint.Y() / denom;
		if (W <= -17 + EPSILON) return new XYZ(0, 0, 0); // Y=0 (black).
		float u = U / (13 * W) + un, v = V / (13 * W) + vn;
		if (Math.abs(v) < EPSILON) return new XYZ(0, Y, 0);
		return new XYZ((9 * Y * u) / (4 * v), Y, Y * (12 - 3 * u - 20 * v) / (4 * v));
	}

	public float dst (UVW other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (UVW other) {
		float dU = U - other.U, dV = V - other.V, dW = W - other.W;
		return dU * dU + dV * dV + dW * dW;
	}
}
