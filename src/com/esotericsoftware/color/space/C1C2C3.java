
package com.esotericsoftware.color.space;

/** Opponent 3 color space. */
public record C1C2C3 (
	/** Achromatic channel [0..pi/2]. */
	float C1,
	/** Red-cyan opponent [0..pi/2]. */
	float C2,
	/** Yellow-violet opponent [0..pi/2]. */
	float C3) {}
