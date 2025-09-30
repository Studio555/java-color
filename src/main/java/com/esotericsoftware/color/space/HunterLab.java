
package com.esotericsoftware.color.space;

/** Predecessor to CIELAB. More perceptually uniform than XYZ. */
public record HunterLab (
	/** Lightness [0..100]. */
	float L,
	/** Red-green axis [-100..100]. */
	float a,
	/** Yellow-blue axis [-100..100]. */
	float b) implements Color {

	public XYZ XYZ () {
		float tempY = L / 10;
		float tempX = a / 17.5f * L / 10;
		float tempZ = b / 7 * L / 10;
		float Y = tempY * tempY;
		return new XYZ((tempX + Y) / 1.02f, Y, -(tempZ - Y) / 0.847f);
	}

	public float Y () {
		float tempY = L / 10;
		return tempY * tempY;
	}

	@SuppressWarnings("all")
	public HunterLab HunterLab () {
		return this;
	}
}
