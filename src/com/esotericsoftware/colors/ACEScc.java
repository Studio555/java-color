
package com.esotericsoftware.colors;

import com.esotericsoftware.colors.Util.ACESccUtil;

/** Academy Color Encoding System for color grading (logarithmic, AP1 primaries). */
public record ACEScc (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) {

	public RGB RGB () {
		return new ACEScg(ACESccUtil.decode(r), ACESccUtil.decode(g), ACESccUtil.decode(b)).RGB();
	}
}
