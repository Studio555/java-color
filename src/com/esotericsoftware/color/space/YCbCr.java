
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

/** Digital video color encoding. */
public record YCbCr (
	/** Luma (Y') [0..1]. */
	float Y,
	/** Blue chroma [-0.5..0.5]. */
	float Cb,
	/** Red chroma [-0.5..0.5]. */
	float Cr) {

	public LinearRGB LinearRGB (YCbCrColorSpace colorSpace) {
		float r, g, b;
		if (colorSpace == YCbCrColorSpace.ITU_BT_601) {
			r = Y + 1.402f * Cr;
			g = Y - 0.34413629f * Cb - 0.71413629f * Cr;
			b = Y + 1.772f * Cb;
		} else {
			r = Y - 0.000000295f * Cb + 1.574799932f * Cr;
			g = Y - 0.187324182f * Cb - 0.468124212f * Cr;
			b = Y + 1.855599963f * Cb - 0.000000402f * Cr;
		}
		return new LinearRGB(linear(r), linear(g), linear(b));
	}

	public RGB RGB (YCbCrColorSpace colorSpace) {
		float r, g, b;
		if (colorSpace == YCbCrColorSpace.ITU_BT_601) {
			r = Y + 1.402f * Cr;
			g = Y - 0.34413629f * Cb - 0.71413629f * Cr;
			b = Y + 1.772f * Cb;
		} else {
			r = Y - 0.000000295f * Cb + 1.574799932f * Cr;
			g = Y - 0.187324182f * Cb - 0.468124212f * Cr;
			b = Y + 1.855599963f * Cb - 0.000000402f * Cr;
		}
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	public XYZ XYZ (YCbCrColorSpace colorSpace) {
		return RGB(colorSpace).XYZ();
	}

	public enum YCbCrColorSpace {
		ITU_BT_601, ITU_BT_709_HDTV
	}
}
