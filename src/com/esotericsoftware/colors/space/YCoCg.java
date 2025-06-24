
package com.esotericsoftware.colors.space;

import static com.esotericsoftware.colors.Util.*;

/** Luma with orange and green chroma. Simple reversible transform. */
public record YCoCg (
	/** Luma [0..1]. */
	float Y,
	/** Orange chroma [-0.5..0.5]. */
	float Co,
	/** Green chroma [-0.5..0.5]. */
	float Cg) {

	public RGB RGB () {
		float r = Y + Co - Cg;
		float g = Y + Cg;
		float b = Y - Co - Cg;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}
}
