
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Gamut;
import com.esotericsoftware.color.Util;

/** Standard RGB with sRGB gamma encoding. Values are clamped [0..1], use {@link LinearRGB} or {@link XYZ} for interchange to
 * preserve wide-gamut colors. */
public record RGB (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) implements Color {

	public RGB {
		r = clamp(r);
		g = clamp(g);
		b = clamp(b);
	}

	public RGB (int rgb) {
		this( //
			((rgb & 0xff0000) >>> 16) / 255f, //
			((rgb & 0x00ff00) >>> 8) / 255f, //
			((rgb & 0x0000ff)) / 255f);
	}

	public RGB (String hex) {
		this(Integer.parseInt(hex.substring(0, 2), 16) / 255f, //
			Integer.parseInt(hex.substring(2, 4), 16) / 255f, //
			Integer.parseInt(hex.substring(4, 6), 16) / 255f);
	}

	public float get (int index) {
		return switch (index) {
		case 0 -> r;
		case 1 -> g;
		case 2 -> b;
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public RGB set (int index, float value) {
		return switch (index) {
		case 0 -> new RGB(clamp(value), g, b);
		case 1 -> new RGB(r, clamp(value), b);
		case 2 -> new RGB(r, g, clamp(value));
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public LinearRGB LinearRGB () {
		return new LinearRGB(linear(r), linear(g), linear(b));
	}

	/** @return NaN if invalid. */
	public uv uv () {
		return xy().uv();
	}

	/** Uses {@link Gamut#sRGB}.
	 * @return NaN if invalid. */
	public xy xy () {
		return Gamut.sRGB.xy(this);
	}

	public XYZ XYZ () {
		float r = linear(this.r), g = linear(this.g), b = linear(this.b);
		return new XYZ( //
			41.24564f * r + 35.75761f * g + 18.04375f * b, //
			21.26729f * r + 71.51522f * g + 7.2175f * b, //
			1.93339f * r + 11.9192f * g + 95.03041f * b);
	}

	public float Y () {
		return 21.26729f * linear(r) + 71.51522f * linear(g) + 7.2175f * linear(b);
	}

	public RGB add (float value) {
		return new RGB(clamp(r + value), clamp(g + value), clamp(b + value));
	}

	public RGB add (int index, float value) {
		return switch (index) {
		case 0 -> new RGB(clamp(r + value), g, b);
		case 1 -> new RGB(r, clamp(g + value), b);
		case 2 -> new RGB(r, g, clamp(b + value));
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public RGB add (float r, float g, float b) {
		return new RGB(clamp(this.r + r), clamp(this.g + g), clamp(this.b + b));
	}

	public RGB lerp (RGB other, float t) {
		return new RGB(clamp(Util.lerp(r, other.r(), t)), clamp(Util.lerp(g, other.g(), t)), clamp(Util.lerp(b, other.b(), t)));
	}

	public float max () {
		return Util.max(r, g, b);
	}

	public float min () {
		return Util.min(r, g, b);
	}

	public RGB nor () {
		float max = max();
		return max < EPSILON ? this : new RGB(r / max, g / max, b / max);
	}

	public RGB sub (float value) {
		return new RGB(clamp(r - value), clamp(g - value), clamp(b - value));
	}

	public RGB sub (int index, float value) {
		return switch (index) {
		case 0 -> new RGB(clamp(r - value), g, b);
		case 1 -> new RGB(r, clamp(g - value), b);
		case 2 -> new RGB(r, g, clamp(b - value));
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public RGB sub (float r, float g, float b) {
		return new RGB(clamp(this.r - r), clamp(this.g - g), clamp(this.b - b));
	}

	public RGB scl (float value) {
		return new RGB(clamp(r * value), clamp(g * value), clamp(b * value));
	}

	public RGB scl (int index, float value) {
		return switch (index) {
		case 0 -> new RGB(clamp(r * value), g, b);
		case 1 -> new RGB(r, clamp(g * value), b);
		case 2 -> new RGB(r, g, clamp(b * value));
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public RGB scl (float r, float g, float b) {
		return new RGB(clamp(this.r * r), clamp(this.g * g), clamp(this.b * b));
	}

	public float dst (RGB other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (RGB other) {
		float dr = r - other.r, dg = g - other.g, db = b - other.b;
		return dr * dr + dg * dg + db * db;
	}

	public float len () {
		return (float)Math.sqrt(len2());
	}

	public float len2 () {
		return r * r + g * g + b * b;
	}

	public float grayscale () {
		return r * 0.2125f + g * 0.7154f + b * 0.0721f;
	}

	public boolean achromatic () {
		return max() - min() < EPSILON;
	}

	/** Returns the color opposite on color wheel. */
	public RGB complementary () {
		HSL hsl = HSL();
		float h = hsl.H() + 180;
		if (h >= 360) h -= 360;
		return new HSL(h, hsl.S(), hsl.L()).RGB();
	}

	/** Returns 3 colors evenly spaced on color wheel. */
	public RGB[] triadic () {
		HSL hsl = HSL();
		float h1 = hsl.H() + 120;
		float h2 = hsl.H() + 240;
		if (h1 >= 360) h1 -= 360;
		if (h2 >= 360) h2 -= 360;
		return new RGB[] {this, new HSL(h1, hsl.S(), hsl.L()).RGB(), new HSL(h2, hsl.S(), hsl.L()).RGB()};
	}

	/** Returns 3 colors adjacent on color wheel.
	 * @param angle [0..360] */
	public RGB[] analogous (float angle) {
		HSL hsl = HSL();
		float h1 = hsl.H() + angle;
		float h2 = hsl.H() - angle;
		if (h1 >= 360) h1 -= 360;
		if (h2 < 0) h2 += 360;
		return new RGB[] {new HSL(h2, hsl.S(), hsl.L()).RGB(), this, new HSL(h1, hsl.S(), hsl.L()).RGB()};
	}

	/** Returns 3 colors in a split-complementary color scheme. */
	public RGB[] splitComplementary () {
		HSL hsl = HSL();
		float h1 = hsl.H() + 150;
		float h2 = hsl.H() + 210;
		if (h1 >= 360) h1 -= 360;
		if (h2 >= 360) h2 -= 360;
		return new RGB[] {this, new HSL(h1, hsl.S(), hsl.L()).RGB(), new HSL(h2, hsl.S(), hsl.L()).RGB()};
	}

	/** Returns the WCAG contrast ratio between foreground and background colors.
	 * @return Contrast ratio, 1:1 to 21:1. */
	public float contrastRatio (RGB bg) {
		float fgLum = Y() / 100;
		float bgLum = bg.Y() / 100;
		float L1 = Math.max(fgLum, bgLum);
		float L2 = Math.min(fgLum, bgLum);
		return (L1 + 0.05f) / (L2 + 0.05f);
	}

	/** Returns true if the colors meet the WCAG AA contrast accessibility standard.
	 * @param largeText true for 18pt+ normal or 14pt+ bold text */
	public boolean WCAG_AA (RGB bg, boolean largeText) {
		return contrastRatio(bg) >= (largeText ? 3 : 4.5f);
	}

	/** Returns true if the colors meet the WCAG AAA contrast accessibility standard.
	 * @param largeText true for 18pt+ normal or 14pt+ bold text */
	public boolean WCAG_AAA (RGB bg, boolean largeText) {
		return contrastRatio(bg) >= (largeText ? 4.5f : 7);
	}

	@SuppressWarnings("all")
	public RGB RGB () {
		return this;
	}
}
