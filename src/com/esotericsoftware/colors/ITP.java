
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;

import com.esotericsoftware.colors.Util.ITPUtil;

/** ITU-R BT.2100 for HDR and wide color gamut. Also known as ICtCp. */
public record ITP (
	/** Intensity [0..1]. */
	float I,
	/** Blue-yellow axis [-0.5..0.5]. */
	float Ct,
	/** Red-green axis [-0.5..0.5]. */
	float Cp) {

	public RGB RGB () {
		float L = ITPUtil.PQ_EOTF(I + 0.00860514f * Ct + 0.11103f * Cp); // PQ to linear.
		float M = ITPUtil.PQ_EOTF(I + -0.00860514f * Ct + -0.11103f * Cp);
		float S = ITPUtil.PQ_EOTF(I + 0.56003125f * Ct + -0.32062717f * Cp);
		float r2020 = 3.4366088f * L + -2.5064522f * M + 0.0698454f * S; // To BT.2020 RGB
		float g2020 = -0.7913296f * L + 1.9836005f * M + -0.1922709f * S;
		float b2020 = -0.0259499f * L + -0.0989138f * M + 1.1248637f * S;
		return new RGB( //
			sRGB(1.6604910f * r2020 + -0.5876411f * g2020 + -0.0728499f * b2020), // BT.2020 to linear sRGB.
			sRGB(-0.1245505f * r2020 + 1.1328999f * g2020 + -0.0083494f * b2020), //
			sRGB(-0.0181508f * r2020 + -0.1005789f * g2020 + 1.1187297f * b2020));
	}

	public ITP lerp (ITP other, float t) {
		return new ITP(Colors.lerp(I, other.I, t), Colors.lerp(Ct, other.Ct, t), Colors.lerp(Cp, other.Cp, t));
	}
}
