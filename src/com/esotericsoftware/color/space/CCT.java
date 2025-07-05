
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Illuminant;
import com.esotericsoftware.color.Spectrum;

public record CCT ( //
	float K,
	float Duv) implements Color {

	static float[] KPlanckian, uvPlanckian;

	public CCT {
		if (!Float.isFinite(K) || !Float.isFinite(Duv)) {
			K = Float.NaN;
			Duv = Float.NaN;
		}
	}

	public CCT (float K) {
		this(K, 0);
	}

	public boolean invalid () {
		return Float.isNaN(K);
	}

	/** @return Requires [1000K+] else returns NaN. */
	public LinearRGB LinearRGB () {
		return uv1960().LinearRGB();
	}

	/** @return Requires [1000K+] else returns NaN. */
	public RGB RGB () {
		return uv1960().RGB();
	}

	/** Convert to RGBW using one calibrated white LED color. Brightness is maximized.
	 * @param brightness [0..1]
	 * @param w White LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @return Requires [1000K+] else returns NaN. */
	public RGBW RGBW (float brightness, LinearRGB w) {
		LinearRGB target = xy().LinearRGB();
		float W = 1;
		float r = Math.max(0, target.r() - W * w.r());
		float g = Math.max(0, target.g() - W * w.g());
		float b = Math.max(0, target.b() - W * w.b());
		float total = r + g + b + W;
		if (total > brightness) {
			float excess = total - brightness;
			// Reduce RGB proportionally.
			float sum = r + g + b;
			if (sum > 0 && excess <= sum) { // Achieve target by only reducing RGB.
				float scale = (sum - excess) / sum;
				r *= scale;
				g *= scale;
				b *= scale;
			} else { // Need to also reduce white.
				r = g = b = 0;
				W = brightness;
			}
		} else {
			float scale = brightness / total;
			r *= scale;
			g *= scale;
			b *= scale;
			W *= scale;
		}
		if (r > 1) r = 1;
		if (g > 1) g = 1;
		if (b > 1) b = 1;
		if (W > 1) W = 1;
		return new RGBW(r, g, b, W);
	}

	/** Convert to RGBWW using two calibrated white LED colors. Brightness is maximized.
	 * @param brightness [0..1]
	 * @param w1 First white LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @param w2 Second white LED color.
	 * @return NaN if invalid. */
	public RGBWW RGBWW (float brightness, LinearRGB w1, LinearRGB w2) {
		float K1 = w1.CCT().K;
		float K2 = w2.CCT().K;
		float W1, W2;
		if (Math.abs(K2 - K1) < EPSILON) // Both whites have same CCT.
			W1 = W2 = 0.5f;
		else {
			float ratio = clamp((K - K1) / (K2 - K1));
			W1 = 1 - ratio;
			W2 = ratio;
		}
		RGB target = RGB();
		float r = Math.max(0, target.r() - (W1 * w1.r() + W2 * w2.r()));
		float g = Math.max(0, target.g() - (W1 * w1.g() + W2 * w2.g()));
		float b = Math.max(0, target.b() - (W1 * w1.b() + W2 * w2.b()));
		float total = r + g + b + W1 + W2;
		if (total > brightness) {
			float excess = total - brightness;
			// Reduce RGB proportionally.
			float sum = r + g + b;
			if (sum > 0 && excess <= sum) { // Achieve target by only reducing RGB.
				float scale = (sum - excess) / sum;
				r *= scale;
				g *= scale;
				b *= scale;
			} else { // Need to also reduce white.
				r = g = b = 0;
				float scale = brightness / (W1 + W2);
				W1 *= scale;
				W2 *= scale;
			}
		} else {
			float scale = brightness / total;
			r *= scale;
			g *= scale;
			b *= scale;
			W1 *= scale;
			W2 *= scale;
		}
		if (r > 1) r = 1;
		if (g > 1) g = 1;
		if (b > 1) b = 1;
		if (W1 > 1) W1 = 1;
		if (W2 > 1) W2 = 1;
		return new RGBWW(r, g, b, W1, W2);
	}

	/** @return NaN if invalid. */
	public uv uv () {
		return uv1960().uv();
	}

	/** @return Requires [1000K+] else returns NaN. */
	public uv1960 uv1960 () {
		if (K < 1000) return new uv1960(Float.NaN, Float.NaN);
		float[] Robertson = CCT.RobertsonImproved;
		float pr = Robertson[0], mired = 1e6f / K;
		for (int i = 5; i < 650; i += 5) {
			float cr = Robertson[i];
			if (mired >= pr && mired <= cr) {
				float t = (mired - pr) / (cr - pr), u = Robertson[i - 4], v = Robertson[i - 3];
				u += (Robertson[i + 1] - u) * t;
				v += (Robertson[i + 2] - v) * t;
				if (Duv != 0) {
					float du = Robertson[i - 2], dv = Robertson[i - 1];
					u -= (du + t * (Robertson[i + 3] - du)) * Duv;
					v -= (dv + t * (Robertson[i + 4] - dv)) * Duv;
				}
				return new uv1960(u, v);
			}
			pr = cr;
		}
		if (mired < Robertson[0]) return new uv1960(Robertson[1] - Robertson[4] * Duv, Robertson[2] + Robertson[3] * Duv);
		return new uv1960(Robertson[651] - Robertson[654] * Duv, Robertson[652] + Robertson[653] * Duv);
	}

	/** Returns Planckian locus coordinates for {@link #K()} and {@link #Duv()}.
	 * @return Requires [1000K+] else returns NaN. */
	public xy xy () {
		return uv1960().xy();
	}

	/** Returns CIE Illuminant D Series daylight locus coordinates for {@link #K()}.
	 * @return Requires [4000.25000K+] else returns NaN. */
	public xy xyDaylight () {
		if (K < 4000 || K > 25000) return new xy(Float.NaN, Float.NaN);
		float K3 = K * K * K;
		float K2 = K * K;
		float x;
		if (K <= 7000)
			x = -4.607e9f / K3 + 2.9678e6f / K2 + 0.09911e3f / K + 0.244063f;
		else
			x = -2.0064e9f / K3 + 1.9018e6f / K2 + 0.24748e3f / K + 0.23704f;
		return new xy(x, 2.87f * x - 3.0f * x * x - 0.275f);
	}

	/** @return Normalized with Y=100. Requires [1000K+] else returns NaN. */
	public XYZ XYZ () {
		return uv1960().XYZ();
	}

	/** Uses exact Planck's law for spectral power distribution.
	 * @return Normalized with Y=100. Requires [26.3K+] else returns NaN. */
	public XYZ PlanckianXYZ () {
		double X = 0, Y = 0, Z = 0;
		for (int i = 0; i < 81; i++) {
			double lambda = (380 + i * 5) * 1e-9; // nm to meters.
			double exponent = XYZ.c2 / (lambda * K);
			double B = exponent > 700 ? 0 : XYZ.c1 / (lambda * lambda * lambda * lambda * lambda * (Math.exp(exponent) - 1));
			X += B * XYZ.Xbar[i];
			Y += B * XYZ.Ybar[i];
			Z += B * XYZ.Zbar[i];
		}
		if (Y == 0) return new XYZ(Float.NaN, Float.NaN, Float.NaN);
		double scale = 100 / Y;
		return new XYZ((float)(X * scale), 100, (float)(Z * scale));
	}

	/** @return Normalized to Y=100. */
	public Spectrum blackbody (int start, int end, int step) {
		int length = ((end - start) / step) + 1;
		float[] values = new float[length];
		for (int i = 0; i < length; i++) {
			double lambda = (start + i * step) * 1e-9; // nm to meters.
			double exponent = XYZ.c2 / (lambda * K);
			values[i] = (float)(exponent > 700 ? 0
				: XYZ.c1 / (lambda * lambda * lambda * lambda * lambda * (Math.exp(exponent) - 1)));
		}
		return new Spectrum(values, step, start).normalize();
	}

	/** Returns a reference illuminant spectrum for this CCT. Uses Planckian radiator for CCT < 5000K, CIE daylight for >= 5000K.
	 * @return 380-780nm @ 5nm, 81 values normalized to Y=100. Requires [1000K+] else returns NaN. */
	public Spectrum reference () {
		return K < 5000 ? blackbody(380, 780, 5) : Illuminant.D(xyDaylight());
	}

	@SuppressWarnings("all")
	public CCT CCT () {
		return this;
	}

	/** Ohno (2013) with 1.0134 spacing. */
	static void PlanckianTable () {
		if (KPlanckian == null) {
			synchronized (uv.class) {
				if (KPlanckian == null) {
					float[] kTable = new float[515];
					float[] uvTable = new float[1030];
					kTable[0] = 1000;
					kTable[1] = 1001;
					float K = 1001, next = 1.0134f;
					for (int i = 2; i < 513; i++) {
						K *= next;
						kTable[i] = K;
						float D = clamp((K - 1000) / 99000);
						next = 1.0134f * (1 - D) + (1 + (1.0134f - 1) / 10) * D;
					}
					kTable[513] = 99999;
					kTable[514] = 100000;
					for (int i = 0; i < 515; i++) {
						uv uv = new CCT(kTable[i]).PlanckianXYZ().uv();
						uvTable[i * 2] = uv.u();
						uvTable[i * 2 + 1] = uv.v();
					}
					KPlanckian = kTable;
					uvPlanckian = uvTable;
				}
			}
		}
	}

	/** Original Robertson isotemperature lines with precomputed direction. */
	static public final float[] Robertson1968 = { // mired, u, v, du, dv
		0, 0.18006f, 0.26352f, 0.9716304f, -0.23650457f, // Infinity K
		10, 0.18066f, 0.26589f, 0.9690406f, -0.24690185f, // 100000.0 K
		20, 0.18133f, 0.26846f, 0.9657298f, -0.25954953f, // 50000.0 K
		30, 0.18208f, 0.27119f, 0.96160626f, -0.2744328f, // 33333.332 K
		40, 0.18293f, 0.27407f, 0.95658004f, -0.29146993f, // 25000.0 K
		50, 0.18388f, 0.27709f, 0.95054394f, -0.31059024f, // 20000.0 K
		60, 0.18494f, 0.28021f, 0.9433986f, -0.33166122f, // 16666.666 K
		70, 0.18611f, 0.28342f, 0.93504727f, -0.35452315f, // 14285.714 K
		80, 0.1874f, 0.28668f, 0.925398f, -0.37899676f, // 12500.0 K
		90, 0.1888f, 0.28997f, 0.9143755f, -0.40486717f, // 11111.111 K
		100, 0.19032f, 0.29326f, 0.9019168f, -0.4319099f, // 10000.0 K
		125, 0.19462f, 0.30141f, 0.864265f, -0.5030368f, // 8000.0 K
		150, 0.19962f, 0.30921f, 0.81741905f, -0.5760434f, // 6666.6665 K
		175, 0.20525f, 0.31647f, 0.7623116f, -0.6472102f, // 5714.2856 K
		200, 0.21142f, 0.32312f, 0.70070165f, -0.7134544f, // 5000.0 K
		225, 0.21807f, 0.32909f, 0.6349235f, -0.77257496f, // 4444.4443 K
		250, 0.22511f, 0.33439f, 0.5674147f, -0.8234322f, // 4000.0 K
		275, 0.23247f, 0.33904f, 0.5004877f, -0.86574364f, // 3636.3635 K
		300, 0.2401f, 0.34308f, 0.43606806f, -0.8999136f, // 3333.3333 K
		325, 0.24792f, 0.34655f, 0.3755177f, -0.9268153f, // 3076.923 K
		350, 0.25591f, 0.34951f, 0.31966853f, -0.94752944f, // 2857.1428 K
		375, 0.264f, 0.352f, 0.2689336f, -0.9631587f, // 2666.6667 K
		400, 0.27218f, 0.35407f, 0.22339252f, -0.9747285f, // 2500.0 K
		425, 0.28039f, 0.35577f, 0.18286845f, -0.98313737f, // 2352.9412 K
		450, 0.28863f, 0.35714f, 0.14705601f, -0.9891282f, // 2222.2222 K
		475, 0.29685f, 0.35823f, 0.11556052f, -0.9933004f, // 2105.2632 K
		500, 0.30505f, 0.35907f, 0.08796569f, -0.9961235f, // 2000.0 K
		525, 0.3132f, 0.35968f, 0.063857116f, -0.997959f, // 1904.762 K
		550, 0.32129f, 0.36011f, 0.042833105f, -0.9990822f, // 1818.1818 K
		575, 0.32931f, 0.36038f, 0.024520462f, -0.9996993f, // 1739.1305 K
		600, 0.33724f, 0.36051f, 0.0085870605f, -0.9999631f, // 1666.6666 K
	};

	/** Improved Robertson isotemperature lines: larger LUT (131*5) with adaptive increments [1000..100000K], precomputed
	 * direction. */
	static public final float[] RobertsonImproved = { // mired, u, v, du, dv
		0, 0.18006f, 0.26352f, 0.9716304f, -0.23650457f, // infinity K
		10, 0.18063825f, 0.2659503f, 0.9688685f, -0.24757574f, // 100000 K
		10.15797f, 0.18064822f, 0.26598927f, 0.96882194f, -0.24775791f, // 98444.86 K
		10.328407f, 0.180659f, 0.26603138f, 0.9687715f, -0.24795505f, // 96820.35 K
		10.512272f, 0.18067065f, 0.2660769f, 0.9687169f, -0.24816848f, // 95126.914 K
		10.710599f, 0.18068324f, 0.26612604f, 0.9686576f, -0.24839951f, // 93365.46 K
		10.924499f, 0.18069686f, 0.2661791f, 0.9685934f, -0.24864972f, // 91537.38 K
		11.155165f, 0.1807116f, 0.26623645f, 0.96852386f, -0.24892068f, // 89644.58 K
		11.403879f, 0.18072753f, 0.26629838f, 0.9684484f, -0.24921417f, // 87689.46 K
		11.67202f, 0.18074475f, 0.2663653f, 0.96836656f, -0.24953216f, // 85674.98 K
		11.961068f, 0.18076338f, 0.26643756f, 0.9682777f, -0.24987675f, // 83604.57 K
		12.2726145f, 0.18078354f, 0.26651564f, 0.96818125f, -0.25025025f, // 81482.23 K
		12.608365f, 0.18080537f, 0.26659995f, 0.96807647f, -0.2506552f, // 79312.42 K
		12.970156f, 0.18082897f, 0.26669106f, 0.9679626f, -0.25109443f, // 77100.08 K
		13.359958f, 0.18085453f, 0.2667895f, 0.9678389f, -0.2515709f, // 74850.54 K
		13.77989f, 0.1808822f, 0.26689583f, 0.96770436f, -0.2520881f, // 72569.516 K
		14.232226f, 0.18091217f, 0.26701075f, 0.9675578f, -0.25264955f, // 70263.08 K
		14.71941f, 0.18094464f, 0.2671349f, 0.96739846f, -0.25325945f, // 67937.51 K
		15.244065f, 0.18097982f, 0.26726913f, 0.96722466f, -0.2539222f, // 65599.3 K
		15.809016f, 0.18101797f, 0.26741418f, 0.96703523f, -0.25464275f, // 63255.043 K
		16.417294f, 0.18105933f, 0.26757103f, 0.96682847f, -0.25542656f, // 60911.38 K
		17.06349f, 0.18110362f, 0.26773834f, 0.96660566f, -0.2562683f, // 58604.66 K
		17.744291f, 0.18115066f, 0.26791543f, 0.9663675f, -0.2571652f, // 56356.152 K
		18.461544f, 0.18120064f, 0.26810288f, 0.9661125f, -0.25812134f, // 54166.65 K
		19.21719f, 0.18125378f, 0.26830134f, 0.9658395f, -0.2591411f, // 52036.746 K
		20.013271f, 0.18131028f, 0.2685115f, 0.96554685f, -0.26022914f, // 49966.844 K
		20.851944f, 0.1813704f, 0.26873407f, 0.9652331f, -0.2613907f, // 47957.16 K
		21.735481f, 0.18143442f, 0.26896986f, 0.9648962f, -0.26263127f, // 46007.723 K
		22.666271f, 0.18150261f, 0.26921967f, 0.9645344f, -0.26395696f, // 44118.418 K
		23.646833f, 0.18157527f, 0.2694844f, 0.9641454f, -0.26537427f, // 42288.96 K
		24.67982f, 0.18165274f, 0.26976502f, 0.9637268f, -0.26689035f, // 40518.938 K
		25.867485f, 0.18174301f, 0.2700898f, 0.9632344f, -0.26866248f, // 38658.57 K
		27.106613f, 0.18183856f, 0.2704311f, 0.9627075f, -0.2705444f, // 36891.367 K
		28.399431f, 0.18193975f, 0.27078977f, 0.9621434f, -0.27254373f, // 35211.973 K
		29.748266f, 0.182047f, 0.2711668f, 0.96153903f, -0.27466848f, // 33615.406 K
		31.155539f, 0.1821607f, 0.2715631f, 0.9608909f, -0.27692738f, // 32097.021 K
		32.62378f, 0.18228136f, 0.27197978f, 0.9601952f, -0.27932972f, // 30652.488 K
		34.155632f, 0.18240944f, 0.27241787f, 0.9594481f, -0.2818855f, // 29277.748 K
		35.753845f, 0.1825455f, 0.2728786f, 0.9586448f, -0.28460532f, // 27969.02 K
		37.421295f, 0.18269013f, 0.27336305f, 0.9577804f, -0.2875007f, // 26722.752 K
		39.16098f, 0.18284397f, 0.27387258f, 0.9568495f, -0.29058382f, // 25535.623 K
		40.97603f, 0.18300772f, 0.27440846f, 0.955846f, -0.29386777f, // 24404.512 K
		42.869698f, 0.18318212f, 0.27497205f, 0.9547635f, -0.2973665f, // 23326.5 K
		44.845398f, 0.183368f, 0.27556482f, 0.9535942f, -0.3010948f, // 22298.832 K
		46.90668f, 0.18356627f, 0.27618822f, 0.9523304f, -0.30506864f, // 21318.924 K
		49.057247f, 0.18377788f, 0.27684382f, 0.95096296f, -0.30930483f, // 20384.348 K
		51.29458f, 0.18400325f, 0.27753118f, 0.9494863f, -0.31380832f, // 19495.238 K
		53.617355f, 0.18424292f, 0.27825028f, 0.9478939f, -0.31858608f, // 18650.678 K
		56.028828f, 0.18449795f, 0.2790024f, 0.94617534f, -0.32365453f, // 17847.955 K
		58.53237f, 0.18476947f, 0.27978885f, 0.9443191f, -0.3290309f, // 17084.562 K
		61.131493f, 0.1850587f, 0.28061098f, 0.942313f, -0.33473307f, // 16358.181 K
		63.829834f, 0.18536696f, 0.28147006f, 0.94014317f, -0.34077963f, // 15666.655 K
		66.63117f, 0.18569571f, 0.28236744f, 0.93779474f, -0.34718996f, // 15007.99 K
		69.53944f, 0.18604645f, 0.2833044f, 0.9352516f, -0.35398394f, // 14380.329 K
		72.5587f, 0.18642084f, 0.2842822f, 0.93249536f, -0.36118186f, // 13781.944 K
		75.693184f, 0.18682066f, 0.28530207f, 0.92950684f, -0.3688047f, // 13211.2295 K
		78.94729f, 0.18724781f, 0.28636518f, 0.9262647f, -0.37687352f, // 12666.68 K
		82.32558f, 0.18770435f, 0.28747258f, 0.9227456f, -0.38540962f, // 12146.894 K
		85.83277f, 0.18819244f, 0.28862533f, 0.9189242f, -0.3944342f, // 11650.562 K
		89.473785f, 0.18871446f, 0.28982425f, 0.91477305f, -0.40396816f, // 11176.458 K
		93.253716f, 0.18927287f, 0.29107016f, 0.91026235f, -0.4140319f, // 10723.434 K
		97.17787f, 0.18987034f, 0.29236367f, 0.9053599f, -0.42464492f, // 10290.408 K
		101.25174f, 0.19050972f, 0.29370525f, 0.90003127f, -0.43582553f, // 9876.373 K
		105.48105f, 0.19119401f, 0.29509515f, 0.89423877f, -0.44759023f, // 9480.376 K
		109.87169f, 0.19192639f, 0.2965334f, 0.8879429f, -0.45995364f, // 9101.525 K
		114.42984f, 0.19271024f, 0.29801992f, 0.8811013f, -0.47292763f, // 8738.9795 K
		119.16188f, 0.19354908f, 0.29955417f, 0.8736691f, -0.4865207f, // 8391.945 K
		124.07443f, 0.19444668f, 0.30113557f, 0.8655991f, -0.5007377f, // 8059.678 K
		129.1744f, 0.19540694f, 0.30276307f, 0.85684216f, -0.5155788f, // 7741.4727 K
		134.46889f, 0.19643395f, 0.3044354f, 0.8473473f, -0.53103906f, // 7436.6646 K
		139.96536f, 0.19753198f, 0.30615097f, 0.83706254f, -0.5471073f, // 7144.625 K
		145.6488f, 0.19870077f, 0.3079009f, 0.82597995f, -0.56369954f, // 6865.8306 K
		151.50148f, 0.19993897f, 0.30967546f, 0.81410563f, -0.5807168f, // 6600.5957 K
		157.52837f, 0.20124976f, 0.31147188f, 0.80140644f, -0.59812015f, // 6348.063 K
		163.73459f, 0.20263633f, 0.31328714f, 0.78785217f, -0.61586446f, // 6107.445 K
		170.12547f, 0.2041019f, 0.31511804f, 0.7734166f, -0.6338981f, // 5878.0146 K
		176.70645f, 0.2056497f, 0.31696126f, 0.7580784f, -0.6521633f, // 5659.103 K
		183.48314f, 0.20728296f, 0.31881326f, 0.7418222f, -0.6705967f, // 5450.0923 K
		190.46132f, 0.2090049f, 0.3206704f, 0.724639f, -0.68912864f, // 5250.41 K
		197.64697f, 0.21081874f, 0.32252884f, 0.706528f, -0.70768505f, // 5059.526 K
		205.0462f, 0.21272767f, 0.32438472f, 0.68749696f, -0.7261872f, // 4876.9497 K
		212.66536f, 0.21473484f, 0.32623395f, 0.6675629f, -0.7445534f, // 4702.223 K
		220.51093f, 0.21684337f, 0.32807252f, 0.6467533f, -0.7626992f, // 4534.923 K
		228.5896f, 0.21905631f, 0.32989624f, 0.62510616f, -0.7805398f, // 4374.6523 K
		236.90828f, 0.2213767f, 0.33170092f, 0.60267013f, -0.7979904f, // 4221.043 K
		245.47409f, 0.22380745f, 0.33348235f, 0.57950526f, -0.8149685f, // 4073.7498 K
		254.29434f, 0.22635145f, 0.33523628f, 0.5556817f, -0.83139515f, // 3932.451 K
		263.37656f, 0.22901145f, 0.33695856f, 0.53128f, -0.84719634f, // 3796.8452 K
		272.7285f, 0.23179011f, 0.33864498f, 0.50638974f, -0.8623047f, // 3666.6504 K
		282.35815f, 0.23469001f, 0.34029147f, 0.48110828f, -0.8766611f, // 3541.6013 K
		292.27377f, 0.23771359f, 0.341894f, 0.45553976f, -0.8902155f, // 3421.4497 K
		302.4838f, 0.24086313f, 0.3434487f, 0.42979273f, -0.9029276f, // 3305.9622 K
		312.99698f, 0.24414082f, 0.3449518f, 0.403979f, -0.9147682f, // 3194.919 K
		323.82227f, 0.24754864f, 0.3463997f, 0.37821144f, -0.9257192f, // 3088.1138 K
		334.9689f, 0.25108844f, 0.347789f, 0.3526019f, -0.93577343f, // 2985.3518 K
		346.4465f, 0.25476184f, 0.34911644f, 0.32725945f, -0.9449345f, // 2886.4485 K
		358.26477f, 0.2585703f, 0.35037908f, 0.30228895f, -0.9532164f, // 2791.2317 K
		370.43387f, 0.26251513f, 0.3515742f, 0.27778906f, -0.9606421f, // 2699.537 K
		382.96417f, 0.2665973f, 0.3526994f, 0.25385094f, -0.9672433f, // 2611.2102 K
		395.86633f, 0.27081764f, 0.35375246f, 0.23055771f, -0.9730587f, // 2526.1052 K
		409.15143f, 0.2751767f, 0.35473162f, 0.20798287f, -0.9781324f, // 2444.0828 K
		422.83084f, 0.27967486f, 0.35563534f, 0.18619043f, -0.98251367f, // 2365.012 K
		436.91626f, 0.28431216f, 0.3564625f, 0.16523431f, -0.98625433f, // 2288.768 K
		451.41968f, 0.2890884f, 0.3572123f, 0.14515851f, -0.98940843f, // 2215.2334 K
		466.3535f, 0.29400313f, 0.35788426f, 0.12599719f, -0.9920306f, // 2144.2961 K
		481.73047f, 0.2990556f, 0.35847828f, 0.107775085f, -0.9941753f, // 2075.8496 K
		497.56375f, 0.30424476f, 0.3589947f, 0.090507925f, -0.99589574f, // 2009.7927 K
		513.8164f, 0.30955285f, 0.35943288f, 0.07425155f, -0.99723953f, // 1946.2205 K
		530.49036f, 0.31497455f, 0.35979432f, 0.05900308f, -0.99825776f, // 1885.0485 K
		547.5965f, 0.3205072f, 0.36008114f, 0.04474415f, -0.9989985f, // 1826.1621 K
		565.146f, 0.32614788f, 0.36029562f, 0.031450834f, -0.9995053f, // 1769.4542 K
		583.1503f, 0.33189338f, 0.36044034f, 0.019094573f, -0.99981767f, // 1714.8237 K
		601.62115f, 0.33774036f, 0.36051798f, 0.007643232f, -0.99997085f, // 1662.1755 K
		620.5706f, 0.34368506f, 0.36053145f, 0.0029384517f, -0.9999957f, // 1611.4202 K
		640.01105f, 0.34972364f, 0.3604838f, 0.012688006f, -0.99991953f, // 1562.473 K
		659.95514f, 0.3558518f, 0.36037812f, 0.02164478f, -0.99976575f, // 1515.2545 K
		680.4159f, 0.36206508f, 0.36021766f, 0.029849315f, -0.99955446f, // 1469.6893 K
		701.40674f, 0.36835867f, 0.36000568f, 0.037342936f, -0.99930257f, // 1425.7063 K
		722.9412f, 0.3747275f, 0.3597455f, 0.044167276f, -0.99902415f, // 1383.2383 K
		745.03357f, 0.38116613f, 0.35944048f, 0.05036389f, -0.99873096f, // 1342.2214 K
		767.69806f, 0.3876688f, 0.3590939f, 0.055973966f, -0.9984322f, // 1302.5955 K
		790.9496f, 0.39422947f, 0.3587091f, 0.06103805f, -0.9981354f, // 1264.3031 K
		814.8032f, 0.40084177f, 0.3582893f, 0.065595716f, -0.9978463f, // 1227.2902 K
		839.2747f, 0.40749896f, 0.3578377f, 0.06968539f, -0.997569f, // 1191.505 K
		864.3799f, 0.4141939f, 0.35735744f, 0.073344156f, -0.9973067f, // 1156.8988 K
		890.13513f, 0.42091924f, 0.35685158f, 0.07660761f, -0.9970613f, // 1123.4249 K
		916.5573f, 0.42766723f, 0.356323f, 0.07950973f, -0.9968341f, // 1091.0393 K
		943.66364f, 0.4344298f, 0.35577464f, 0.08208277f, -0.9966255f, // 1059.6996 K
		971.4718f, 0.4411986f, 0.35520923f, 0.084357195f, -0.9964355f, // 1029.366 K
		1000, 0.44796494f, 0.3546294f, 0.086361654f, -0.99626386f, // 1000 K
		1029.2668f, 0.45471993f, 0.3540377f, 0.08812292f, -0.9961096f, // 971.56537 K
	};

	public enum Method {
		/** Nearest isotemperature line, greatly improved accuracy. */
		RobertsonImproved,
		/** Nearest isotemperature line, poor accuracy. */
		Robertson1968,
		/** Nearest point on Planckian locus. */
		Ohno2013
	}
}
