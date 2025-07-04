
package com.esotericsoftware.color.space;

/** CIE 1964 U*V*W* space (obsolete). */
public record UVW (float U, float V, float W) {
	public float dst (UVW other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (UVW other) {
		float dU = U - other.U, dV = V - other.V, dW = W - other.W;
		return dU * dU + dV * dV + dW * dW;
	}
}
