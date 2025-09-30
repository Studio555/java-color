
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Colors.*;

/** Human cone cell responses. */
public record LMS (
	/** Long wavelength (red) cone response [0+]. */
	float L,
	/** Medium wavelength (green) cone response [0+]. */
	float M,
	/** Short wavelength (blue) cone response [0+]. */
	float S) implements Color {

	public RGB RGB (CAT matrix) {
		return XYZ(matrix).RGB();
	}

	/** Uses {@link CAT#Bradford}. */
	public XYZ XYZ () {
		return XYZ(CAT.Bradford);
	}

	public XYZ XYZ (CAT matrix) {
		float[] xyz = matrixMultiply(L, M, S, matrix.backward);
		return new XYZ(xyz[0], xyz[1], xyz[2]);
	}

	@SuppressWarnings("all")
	public LMS LMS () {
		return this;
	}

	/** Chromatic adaptation transforms. */
	public enum CAT {
		Bradford(new float[] {0.8951f, 0.2664f, -0.1614f, -0.7502f, 1.7135f, 0.0367f, 0.0389f, -0.0685f, 1.0296f},
			new float[] {0.9869929f, -0.14705427f, 0.15996265f, 0.43230528f, 0.51836026f, 0.049291223f, -0.0085286675f, 0.04004282f,
				0.96848667f}),
		Bianco2010(new float[] {0.8752f, 0.2787f, -0.1539f, -0.8904f, 1.8709f, 0.0195f, -0.0061f, 0.0162f, 0.9899f},
			new float[] {0.99197084f, -0.14913051f, 0.15715967f, 0.47211623f, 0.46361646f, 0.06426726f, -0.0016135583f,
				-0.008506196f, 1.0101198f}),
		BiancoPC2010(new float[] {0.6489f, 0.3915f, -0.0404f, -0.3775f, 1.3055f, 0.072f, -0.0271f, 0.0888f, 0.9383f},
			new float[] {1.3123151f, -0.39947215f, 0.087157115f, 0.37936038f, 0.6545309f, -0.03389114f, 0.0019999386f, -0.073481865f,
				1.071482f}),
		/** CIECAM02. */
		CAT02(new float[] {0.7328f, 0.4296f, -0.1624f, -0.7036f, 1.6975f, 0.0061f, 0.003f, 0.0136f, 0.9834f},
			new float[] {1.0961238f, -0.27886903f, 0.18274519f, 0.45436907f, 0.4735332f, 0.072097816f, -0.00962761f, -0.0056980313f,
				1.0153257f}),
		CAT02_Brill2008(new float[] {0.7328f, 0.4296f, -0.1624f, -0.7036f, 1.6975f, 0.0061f, 0, 0, 1},
			new float[] {1.0978566f, -0.27784342f, 0.17998677f, 0.45505267f, 0.47393778f, 0.07100954f, -0, 0, 1}),
		CAT16(new float[] {0.401288f, 0.650173f, -0.051461f, -0.250268f, 1.204414f, 0.045854f, -0.002079f, 0.048952f, 0.953127f},
			new float[] {1.8620679f, -1.0112545f, 0.14918676f, 0.38752654f, 0.62144744f, -0.008973984f, -0.015841497f, -0.034122933f,
				1.0499644f}),
		CMC2000(new float[] {0.7982f, 0.3389f, -0.1371f, -0.5918f, 1.5512f, 0.0406f, 0.0008f, 0.0239f, 0.9753f},
			new float[] {1.0764501f, -0.23766239f, 0.16121235f, 0.41096434f, 0.5543418f, 0.034693863f, -0.010953765f, -0.013389357f,
				1.0243433f}),
		Fairchild(new float[] {0.8951f, -0.7502f, 0.0389f, 0.2664f, 1.7135f, 0.0685f, -0.1614f, 0.0367f, 1.0296f},
			new float[] {0.9775825f, 0.42940554f, -0.06550334f, -0.15833788f, 0.51488334f, -0.028273273f, 0.1588897f, 0.0489606f,
				0.9619905f}),
		/** Hunt-Pointer-Estevez. */
		HPE(new float[] {0.38971f, 0.68898f, -0.07868f, -0.22981f, 1.1834f, 0.04641f, 0, 0, 1},
			new float[] {1.9101968f, -1.1121238f, 0.20190795f, 0.37095007f, 0.62905425f, -8.0577065E-6f, -0, 0, 1}),
		Sharp(new float[] {1.2694f, -0.0988f, -0.1706f, -0.8364f, 1.8006f, 0.0357f, 0.0297f, -0.0315f, 1.0018f},
			new float[] {0.81563324f, 0.04715478f, 0.13721663f, 0.3791144f, 0.57694244f, 0.044000868f, -0.012260138f, 0.016743053f,
				0.9955188f}),
		VonKries(new float[] {0.40024f, 0.7076f, -0.08081f, -0.2263f, 1.16532f, 0.0457f, 0, 0, 0.91822f},
			new float[] {1.8599365f, -1.1293817f, 0.21989742f, 0.36119142f, 0.6388125f, -6.3685507E-6f, -0, 0, 1.0890636f});

		final float[] forward, backward;

		CAT (float[] forward, float[] backward) {
			this.forward = forward;
			this.backward = backward;
		}
	}
}
