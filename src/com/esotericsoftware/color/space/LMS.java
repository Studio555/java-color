
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

/** Human cone cell responses. */
public record LMS (
	/** Long wavelength (red) cone response [0+]. */
	float L,
	/** Medium wavelength (green) cone response [0+]. */
	float M,
	/** Short wavelength (blue) cone response [0+]. */
	float S) {

	/** Uses the LMS CIECAM02 transformation matrix. */
	public RGB RGB () {
		return RGB(CAT.CAT02);
	}

	public RGB RGB (CAT matrix) {
		return XYZ(matrix).RGB();
	}

	public XYZ XYZ (CAT matrix) {
		float[] xyz = matrixMultiply(L, M, S, switch (matrix) {
		case HPE -> CAT.HPE_backward;
		case Bradford -> CAT.Bradford_backward;
		case VonKries -> CAT.vonKries_backward;
		case CAT97 -> CAT.CAT97_backward;
		default -> CAT.CAT02_backward;
		});
		return new XYZ(xyz[0], xyz[1], xyz[2]);
	}

	/** Chromatic adaptation transforms. */
	public enum CAT {
		/** Hunt-Pointer-Estevez. */
		HPE,
		/** Bradford. */
		Bradford,
		/** Von Kries. */
		VonKries,
		/** CIECAM97s. */
		CAT97,
		/** CIECAM02. */
		CAT02;

		static final float[][] HPE_forward = {{0.38971f, 0.68898f, -0.07868f}, {-0.22981f, 1.18340f, 0.04641f},
			{0.00000f, 0.00000f, 1.00000f}};
		static final float[][] HPE_backward = {{1.91019683f, -1.11212389f, 0.20190796f}, {0.37095009f, 0.62905426f, -0.00000806f},
			{0.00000f, 0.00000f, 1.00000f}};
		static final float[][] Bradford_forward = {{0.8951000f, 0.2664000f, -0.1614000f}, {-0.7502000f, 1.7135000f, 0.0367000f},
			{0.0389000f, -0.0685000f, 1.0296000f}};
		static final float[][] Bradford_backward = {{0.9869929f, -0.1470543f, 0.1599627f}, {0.4323053f, 0.5183603f, 0.0492912f},
			{-0.0085287f, 0.0400428f, 0.9684867f}};
		static final float[][] vonKries_forward = {{0.4002f, 0.7076f, -0.0808f}, {-0.2263f, 1.1653f, 0.0457f}, {0f, 0f, 0.9182f}};
		static final float[][] vonKries_backward = {{1.86006661f, -1.12948008f, 0.21989830f},
			{0.36122292f, 0.63880431f, -0.00000713f}, {0.00000f, 0.00000f, 1.08908734f}};
		static final float[][] CAT97_forward = {{0.8562f, 0.3372f, -0.1934f}, {-0.8360f, 1.8327f, 0.0033f},
			{0.0357f, -0.00469f, 1.0112f}};
		static final float[][] CAT97_backward = {{0.9838112f, -0.1805292f, 0.1887508f}, {0.4488317f, 0.4632779f, 0.0843307f},
			{-0.0326513f, 0.0085222f, 0.9826514f}};
		static final float[][] CAT02_forward = {{0.7328f, 0.4296f, -0.1624f}, {-0.7036f, 1.6975f, 0.0061f},
			{0.0030f, 0.0136f, 0.9834f}};
		static final float[][] CAT02_backward = {{1.0961238f, -0.2788690f, 0.1827452f}, {0.4543690f, 0.4735332f, 0.0720978f},
			{-0.0096276f, -0.0056980f, 1.0153256f}};
	}
}
