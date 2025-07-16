
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

/** Photo YCC for Kodak PhotoCD. */
public record YCC (
	/** Luma [0..1]. */
	float Y,
	/** Chroma 1 [-0.5..0.5]. */
	float C1,
	/** Chroma 2 [-0.5..0.5]. */
	float C2) implements Color {

	public LRGB LRGB () {
		float r = 1.402525f * Y + 0.002952f * (C1 - 0.612f) + 1.881096f * (C2 - 0.537f);
		float g = 1.402525f * Y - 0.444393f * (C1 - 0.612f) - 0.956979f * (C2 - 0.537f);
		float b = 1.402525f * Y + 2.291013f * (C1 - 0.612f) + 0.003713f * (C2 - 0.537f);
		return new LRGB(linear(r), linear(g), linear(b));
	}

	public RGB RGB () {
		float r = 1.402525f * Y + 0.002952f * (C1 - 0.612f) + 1.881096f * (C2 - 0.537f);
		float g = 1.402525f * Y - 0.444393f * (C1 - 0.612f) - 0.956979f * (C2 - 0.537f);
		float b = 1.402525f * Y + 2.291013f * (C1 - 0.612f) + 0.003713f * (C2 - 0.537f);
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}

	@SuppressWarnings("all")
	public YCC YCC () {
		return this;
	}
}
