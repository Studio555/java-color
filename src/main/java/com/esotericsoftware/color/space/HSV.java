
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Colors.*;

import com.esotericsoftware.color.Colors;

/** Hue, Saturation, Value. Also known as HSB. */
public record HSV (
	/** Hue [0..360] or NaN if achromatic. */
	float H,
	/** Saturation [0..1]. */
	float S,
	/** Value/Brightness [0..1]. */
	float V) implements Color {

	public LRGB LRGB () {
		if (Float.isNaN(H) || S < EPSILON) return new LRGB(linear(V), linear(V), linear(V));
		float f = H / 60 - (float)Math.floor(H / 60);
		float p = V * (1 - S);
		float q = V * (1 - f * S);
		float t = V * (1 - (1 - f) * S);
		float r, g, b;
		switch ((int)Math.floor(H / 60) % 6) {
		case 0 -> {
			r = V;
			g = t;
			b = p;
		}
		case 1 -> {
			r = q;
			g = V;
			b = p;
		}
		case 2 -> {
			r = p;
			g = V;
			b = t;
		}
		case 3 -> {
			r = p;
			g = q;
			b = V;
		}
		case 4 -> {
			r = t;
			g = p;
			b = V;
		}
		default -> {
			r = V;
			g = p;
			b = q;
		}
		}
		return new LRGB(linear(r), linear(g), linear(b));
	}

	public RGB RGB () {
		if (Float.isNaN(H) || S < EPSILON) return new RGB(V, V, V);
		float f = H / 60 - (float)Math.floor(H / 60);
		float p = V * (1 - S);
		float q = V * (1 - f * S);
		float t = V * (1 - (1 - f) * S);
		float r, g, b;
		switch ((int)Math.floor(H / 60) % 6) {
		case 0 -> {
			r = V;
			g = t;
			b = p;
		}
		case 1 -> {
			r = q;
			g = V;
			b = p;
		}
		case 2 -> {
			r = p;
			g = V;
			b = t;
		}
		case 3 -> {
			r = p;
			g = q;
			b = V;
		}
		case 4 -> {
			r = t;
			g = p;
			b = V;
		}
		default -> {
			r = V;
			g = p;
			b = q;
		}
		}
		return new RGB(r, g, b);
	}

	public XYZ XYZ () {
		return RGB().XYZ();
	}

	public float Y () {
		return RGB().Y();
	}

	public HSV lerp (HSV other, float t) {
		return new HSV(lerpAngle(H, other.H, t), Colors.lerp(S, other.S, t), Colors.lerp(V, other.V, t));
	}

	@SuppressWarnings("all")
	public HSV HSV () {
		return this;
	}
}
