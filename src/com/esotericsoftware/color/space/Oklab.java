
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Util;

/** Perceptually uniform color space. Based on CAM16 and IPT. */
public record Oklab (
	/** Lightness [0..1]. */
	float L,
	/** Red-green axis [-0.5..0.5]. */
	float a,
	/** Yellow-blue axis [-0.5..0.5]. */
	float b) implements Color {

	public float get (int index) {
		return switch (index) {
		case 0 -> L;
		case 1 -> a;
		case 2 -> b;
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public Oklab set (int index, float value) {
		return switch (index) {
		case 0 -> new Oklab(value, a, b);
		case 1 -> new Oklab(L, value, b);
		case 2 -> new Oklab(L, a, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LRGB LRGB () {
		float l = L + 0.3963377774f * a + 0.2158037573f * b;
		float m = L - 0.1055613458f * a - 0.0638541728f * b;
		float s = L - 0.0894841775f * a - 1.291485548f * b;
		l *= l * l;
		m *= m * m;
		s *= s * s;
		return new LRGB( //
			(+4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s), //
			(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s), //
			(-0.0041960863f * l - 0.7034186147f * m + 1.707614701f * s));
	}

	public Oklch Oklch () {
		float C = (float)Math.sqrt(a * a + b * b);
		float h = C < EPSILON ? Float.NaN : (float)Math.atan2(b, a) * radDeg;
		if (h < 0) h += 360;
		return new Oklch(L, C, h);
	}

	public RGB RGB () {
		float l = L + 0.3963377774f * a + 0.2158037573f * b;
		float m = L - 0.1055613458f * a - 0.0638541728f * b;
		float s = L - 0.0894841775f * a - 1.291485548f * b;
		l *= l * l;
		m *= m * m;
		s *= s * s;
		return new RGB( //
			sRGB(clamp(4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s)), //
			sRGB(clamp(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s)), //
			sRGB(clamp(-0.0041960863f * l - 0.7034186147f * m + 1.707614701f * s)));
	}

	public XYZ XYZ () {
		float l = L + 0.3963377774f * a + 0.2158037573f * b;
		float m = L - 0.1055613458f * a - 0.0638541728f * b;
		float s = L - 0.0894841775f * a - 1.291485548f * b;
		l *= l * l;
		m *= m * m;
		s *= s * s;
		float r = 4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s;
		float g = -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s;
		float b = -0.0041960863f * l - 0.7034186147f * m + 1.707614701f * s;
		return new XYZ( //
			41.24564f * r + 35.75761f * g + 18.04375f * b, // Linear RGB to XYZ, D65.
			21.26729f * r + 71.51522f * g + 7.2175f * b, //
			1.93339f * r + 11.9192f * g + 95.03041f * b);
	}

	public float Y () {
		float l = L + 0.3963377774f * a + 0.2158037573f * b;
		float m = L - 0.1055613458f * a - 0.0638541728f * b;
		float s = L - 0.0894841775f * a - 1.291485548f * b;
		l *= l * l;
		m *= m * m;
		s *= s * s;
		float r = 4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s;
		float g = -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s;
		float b = -0.0041960863f * l - 0.7034186147f * m + 1.707614701f * s;
		return 21.26729f * r + 71.51522f * g + 7.2175f * b; // Linear RGB to Y, D65.
	}

	public Oklab add (float value) {
		return new Oklab(L + value, a + value, b + value);
	}

	public Oklab add (int index, float value) {
		return switch (index) {
		case 0 -> new Oklab(L + value, a, b);
		case 1 -> new Oklab(L, a + value, b);
		case 2 -> new Oklab(L, a, b + value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public Oklab add (float L, float a, float b) {
		return new Oklab(this.L + L, this.a + a, this.b + b);
	}

	public Oklab lerp (Oklab other, float t) {
		return new Oklab(Util.lerp(L, other.L, t), Util.lerp(a, other.a, t), Util.lerp(b, other.b, t));
	}

	public Oklab sub (float value) {
		return new Oklab(L - value, a - value, b - value);
	}

	public Oklab sub (int index, float value) {
		return switch (index) {
		case 0 -> new Oklab(L - value, a, b);
		case 1 -> new Oklab(L, a - value, b);
		case 2 -> new Oklab(L, a, b - value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public Oklab sub (float L, float a, float b) {
		return new Oklab(this.L - L, this.a - a, this.b - b);
	}

	public float dst (Oklab other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (Oklab other) {
		float dL = L - other.L, da = a - other.a, db = b - other.b;
		return dL * dL + da * da + db * db;
	}

	public float len () {
		return (float)Math.sqrt(len2());
	}

	public float len2 () {
		return L * L + a * a + b * b;
	}

	public Oklab withL (float L) {
		return new Oklab(L, a, b);
	}

	@SuppressWarnings("all")
	public Oklab Oklab () {
		return this;
	}
}
