
package com.esotericsoftware.color.space;

/** Opponent 2 color channels for image processing. */
public record O1O2 (
	/** Yellow-blue opponent [-1..1]. */
	float O1,
	/** Red-green opponent [-1..1]. */
	float O2) {}
