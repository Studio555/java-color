
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

/** CIE 1960 UCS chromaticity coordinates. */
public record uv1960 (
	/** u chromaticity [0..1]. */
	float u,
	/** v chromaticity [0..1]. */
	float v) {

	public uv uv () {
		return new uv(u, 1.5f * v);
	}

	/** @return NaN if invalid. */
	public xy xy () {
		float denom = 2 + u - 4 * v;
		if (Math.abs(denom) < EPSILON) return new xy(Float.NaN, Float.NaN);
		return new xy(u * 1.5f / denom, v / denom);
	}
}
