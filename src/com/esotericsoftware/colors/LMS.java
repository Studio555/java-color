
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.Util.LMSUtil;

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
		case HPE -> LMSUtil.HPE_backward;
		case Bradford -> LMSUtil.Bradford_backward;
		case VonKries -> LMSUtil.vonKries_backward;
		case CAT97 -> LMSUtil.CAT97_backward;
		default -> LMSUtil.CAT02_backward;
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
		CAT02
	}
}
