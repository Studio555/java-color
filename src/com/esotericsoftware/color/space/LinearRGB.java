
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Gamut;
import com.esotericsoftware.color.Util;

/** RGB without gamma correction. Values are not clamped. */
public record LinearRGB (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) {

	public LinearRGB (int rgb) {
		this( //
			((rgb & 0xff0000) >>> 16) / 255f, //
			((rgb & 0x00ff00) >>> 8) / 255f, //
			((rgb & 0x0000ff)) / 255f);
	}

	public float get (int index) {
		return switch (index) {
		case 0 -> r;
		case 1 -> g;
		case 2 -> b;
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LinearRGB set (int index, float value) {
		return switch (index) {
		case 0 -> new LinearRGB(value, g, b);
		case 1 -> new LinearRGB(r, value, b);
		case 2 -> new LinearRGB(r, g, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public RGB RGB () {
		return new RGB(sRGB(clamp(r)), sRGB(clamp(g)), sRGB(clamp(b)));
	}

	public XYZ XYZ () {
		return new XYZ( //
			(0.4124564f * r + 0.3575761f * g + 0.1804375f * b) * 100, //
			(0.2126729f * r + 0.7151522f * g + 0.0721750f * b) * 100, //
			(0.0193339f * r + 0.1191920f * g + 0.9503041f * b) * 100);
	}

	public XYZ XYZ (Gamut gamut) {
		float[][] rgbToXYZ = gamut.RGB_XYZ;
		float X = rgbToXYZ[0][0] * r + rgbToXYZ[0][1] * g + rgbToXYZ[0][2] * b;
		float Y = rgbToXYZ[1][0] * r + rgbToXYZ[1][1] * g + rgbToXYZ[1][2] * b;
		float Z = rgbToXYZ[2][0] * r + rgbToXYZ[2][1] * g + rgbToXYZ[2][2] * b;
		return new XYZ(X * 100, Y * 100, Z * 100);
	}

	public LinearRGB add (float value) {
		return new LinearRGB(r + value, g + value, b + value);
	}

	public LinearRGB add (int index, float value) {
		return switch (index) {
		case 0 -> new LinearRGB(r + value, g, b);
		case 1 -> new LinearRGB(r, g + value, b);
		case 2 -> new LinearRGB(r, g, b + value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LinearRGB add (float r, float g, float b) {
		return new LinearRGB(this.r + r, this.g + g, this.b + b);
	}

	public LinearRGB lerp (LinearRGB other, float t) {
		return new LinearRGB(Util.lerp(r, other.r, t), Util.lerp(g, other.g, t), Util.lerp(b, other.b, t));
	}

	public float max () {
		return Util.max(r, g, b);
	}

	public float min () {
		return Util.min(r, g, b);
	}

	public LinearRGB nor () {
		float max = max();
		return max < EPSILON ? this : new LinearRGB(r / max, g / max, b / max);
	}

	public LinearRGB sub (float value) {
		return new LinearRGB(r - value, g - value, b - value);
	}

	public LinearRGB sub (int index, float value) {
		return switch (index) {
		case 0 -> new LinearRGB(r - value, g, b);
		case 1 -> new LinearRGB(r, g - value, b);
		case 2 -> new LinearRGB(r, g, b - value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LinearRGB sub (float r, float g, float b) {
		return new LinearRGB(this.r - r, this.g - g, this.b - b);
	}

	public LinearRGB scl (float value) {
		return new LinearRGB(r * value, g * value, b * value);
	}

	public LinearRGB scl (int index, float value) {
		return switch (index) {
		case 0 -> new LinearRGB(r * value, g, b);
		case 1 -> new LinearRGB(r, g * value, b);
		case 2 -> new LinearRGB(r, g, b * value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LinearRGB scl (float r, float g, float b) {
		return new LinearRGB(this.r * r, this.g * g, this.b * b);
	}

	public float dst (LinearRGB other) {
		float dr = r - other.r, dg = g - other.g, db = b - other.b;
		return (float)Math.sqrt(dr * dr + dg * dg + db * db);
	}

	public float dst2 (LinearRGB other) {
		float dr = r - other.r, dg = g - other.g, db = b - other.b;
		return dr * dr + dg * dg + db * db;
	}
}
