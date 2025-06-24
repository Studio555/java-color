
package com.esotericsoftware.colors.space;

import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.Util;

/** Perceptually uniform color space. Based on CAM16 and IPT. */
public record Oklab (
	/** Lightness [0..1]. */
	float L,
	/** Red-green axis [-0.5..0.5]. */
	float a,
	/** Yellow-blue axis [-0.5..0.5]. */
	float b) {

	public LinearRGB LinearRGB () {
		float l = L + 0.3963377774f * a + 0.2158037573f * b;
		float m = L - 0.1055613458f * a - 0.0638541728f * b;
		float s = L - 0.0894841775f * a - 1.2914855480f * b;
		l *= l * l;
		m *= m * m;
		s *= s * s;
		return new LinearRGB( //
			(+4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s), //
			(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s), //
			(-0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s));
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
		float s = L - 0.0894841775f * a - 1.2914855480f * b;
		l *= l * l;
		m *= m * m;
		s *= s * s;
		return new RGB( //
			sRGB(clamp(4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s)), //
			sRGB(clamp(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s)), //
			sRGB(clamp(-0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s)));
	}

	public XYZ XYZ () {
		float l = L + 0.3963377774f * a + 0.2158037573f * b;
		float m = L - 0.1055613458f * a - 0.0638541728f * b;
		float s = L - 0.0894841775f * a - 1.2914855480f * b;
		l *= l * l;
		m *= m * m;
		s *= s * s;
		float r = 4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s;
		float g = -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s;
		float b = -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s;
		return new XYZ( //
			(0.4124564f * r + 0.3575761f * g + 0.1804375f * b) * 100, // Linear RGB to XYZ, D65.
			(0.2126729f * r + 0.7151522f * g + 0.0721750f * b) * 100, //
			(0.0193339f * r + 0.1191920f * g + 0.9503041f * b) * 100);
	}

	public Oklab lerp (Oklab other, float t) {
		return new Oklab(Util.lerp(L, other.L, t), Util.lerp(a, other.a, t), Util.lerp(b, other.b, t));
	}
}
