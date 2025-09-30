
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Colors.*;

/** Luma with orange and green chroma. Simple reversible transform. */
public record YCoCg (
	/** Luma [0..1]. */
	float Y,
	/** Orange chroma [-0.5..0.5]. */
	float Co,
	/** Green chroma [-0.5..0.5]. */
	float Cg) implements Color {

	public LRGB LRGB () {
		float r = Y + Co - Cg;
		float g = Y + Cg;
		float b = Y - Co - Cg;
		return new LRGB(linear(r), linear(g), linear(b));
	}

	public RGB RGB () {
		float r = Y + Co - Cg;
		float g = Y + Cg;
		float b = Y - Co - Cg;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}

	@SuppressWarnings("all")
	public YCoCg YCoCg () {
		return this;
	}
}
