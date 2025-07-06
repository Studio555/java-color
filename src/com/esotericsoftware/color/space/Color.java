
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Illuminant;
import com.esotericsoftware.color.Observer;
import com.esotericsoftware.color.space.LMS.CAT;
import com.esotericsoftware.color.space.YCbCr.YCbCrColorSpace;

public interface Color {
	default public ACES2065_1 ACES2065_1 () {
		LinearRGB rgb = LinearRGB();
		return new ACES2065_1( //
			0.43953127f * rgb.r() + 0.38391885f * rgb.g() + 0.17654988f * rgb.b(), // To AP0.
			0.08959387f * rgb.r() + 0.81347942f * rgb.g() + 0.09692672f * rgb.b(), //
			0.01738063f * rgb.r() + 0.11176223f * rgb.g() + 0.87085713f * rgb.b());
	}

	default public ACEScg ACEScg () {
		LinearRGB rgb = LinearRGB();
		return new ACEScg( //
			0.61309741f * rgb.r() + 0.33952315f * rgb.g() + 0.04737945f * rgb.b(), // To AP1.
			0.07019486f * rgb.r() + 0.91635524f * rgb.g() + 0.0134499f * rgb.b(), //
			0.0206156f * rgb.r() + 0.10956263f * rgb.g() + 0.86982177f * rgb.b());
	}

	default public ACEScc ACEScc () {
		ACEScg cg = ACEScg();
		return new ACEScc(ACEScc.encode(cg.r()), ACEScc.encode(cg.g()), ACEScc.encode(cg.b()));
	}

	default public C1C2C3 C1C2C3 () {
		RGB rgb = RGB();
		return new C1C2C3((float)Math.atan(rgb.r() / Math.max(rgb.g(), rgb.b())), //
			(float)Math.atan(rgb.g() / Math.max(rgb.r(), rgb.b())), //
			(float)Math.atan(rgb.b() / Math.max(rgb.r(), rgb.g())));
	}

	/** Uses {@link com.esotericsoftware.color.space.CAM02.VC#sRGB}. */
	default public CAM02 CAM02 () {
		return CAM02(CAM02.VC.sRGB);
	}

	default public CAM02 CAM02 (CAM02.VC vc) {
		return XYZ().CAM02(vc);
	}

	/** Uses {@link com.esotericsoftware.color.space.CAM02.VC#sRGB}. */
	default public CAM02UCS CAM02UCS () {
		return CAM02().CAM02UCS();
	}

	default public CAM02UCS CAM02UCS (CAM02.VC vc) {
		return CAM02(vc).CAM02UCS();
	}

	/** Uses {@link com.esotericsoftware.color.space.CAM16.VC#sRGB}. */
	default public CAM16 CAM16 () {
		return CAM16(CAM16.VC.sRGB);
	}

	default public CAM16 CAM16 (CAM16.VC vc) {
		return XYZ().CAM16(vc);
	}

	/** Uses {@link com.esotericsoftware.color.space.CAM16.VC#sRGB}. */
	default public CAM16UCS CAM16UCS () {
		return CAM16().CAM16UCS();
	}

	default public CAM16UCS CAM16UCS (CAM16.VC vc) {
		return CAM16(vc).CAM16UCS();
	}

	/** @return [1000..infinity] or NaN out of range.
	 * @see uv#CCT(CCT.Method) */
	default public CCT CCT (CCT.Method method) {
		return uv().CCT(method);
	}

	/** Uses {@link com.esotericsoftware.color.space.CCT.Method#RobertsonImproved}.
	 * @return [1000..infinity] or NaN out of range. */
	default public CCT CCT () {
		return uv().CCT();
	}

	default public CMYK CMYK () {
		RGB rgb = RGB();
		float K = 1 - rgb.max();
		if (1 - K < EPSILON) return new CMYK(0, 0, 0, K); // Black
		return new CMYK( //
			(1 - rgb.r() - K) / (1 - K), //
			(1 - rgb.g() - K) / (1 - K), //
			(1 - rgb.b() - K) / (1 - K), K);
	}

	/** Uses {@link com.esotericsoftware.color.space.CAM16.VC#HCT}. */
	default public HCT HCT () {
		return HCT(CAM16.VC.HCT);
	}

	default public HCT HCT (CAM16.VC vc) {
		CAM16 cam16 = CAM16(vc);
		return new HCT(cam16.h(), cam16.C(), Lab.YtoLstar(Y()));
	}

	default public HSI HSI () {
		RGB rgb = RGB();
		float I = (rgb.r() + rgb.g() + rgb.b()) / 3;
		float min = rgb.min(), S = I < EPSILON ? 0 : 1 - min / I, H = Float.NaN;
		if (S != 0 && I != 0) {
			float alpha = 0.5f * (2 * rgb.r() - rgb.g() - rgb.b());
			float beta = 0.8660254f * (rgb.g() - rgb.b()); // sqrt(3) / 2
			H = (float)Math.atan2(beta, alpha);
			if (H < 0) H += 2 * PI;
			H = H * radDeg;
		}
		return new HSI(H, S, I);
	}

	default public HSL HSL () {
		RGB rgb = RGB();
		float min = rgb.min(), max = rgb.max(), delta = max - min, L = (max + min) / 2;
		if (delta < EPSILON) return new HSL(Float.NaN, 0, L); // Gray.
		float S = L <= 0.5f ? delta / (max + min) : delta / (2 - max - min), H;
		if (rgb.r() == max)
			H = (rgb.g() - rgb.b()) / 6 / delta;
		else if (rgb.g() == max)
			H = 1 / 3f + (rgb.b() - rgb.r()) / 6 / delta;
		else
			H = 2 / 3f + (rgb.r() - rgb.g()) / 6 / delta;
		if (H < 0) H += 1;
		if (H > 1) H -= 1;
		H *= 360;
		return new HSL(H, S, L);
	}

	default public HSLuv HSLuv () {
		LCHuv lch = LChuv();
		float L = lch.L(), C = lch.C(), H = lch.H();
		if (L > 100 - EPSILON) return new HSLuv(H, 0, 100);
		if (L < EPSILON) return new HSLuv(H, 0, 0);
		float maxChroma = HSLuv.maxChromaForLH(L, H);
		return new HSLuv(H, maxChroma < EPSILON ? 0 : Math.min(100, (C / maxChroma) * 100), L);
	}

	default public HSV HSV () {
		RGB rgb = RGB();
		float min = rgb.min(), max = rgb.max(), delta = max - min, H = 0;
		if (max == min)
			H = Float.NaN;
		else if (max == rgb.r()) {
			H = (rgb.g() - rgb.b()) / delta * 60;
			if (H < 0) H += 360;
		} else if (max == rgb.g())
			H = ((rgb.b() - rgb.r()) / delta + 2) * 60;
		else if (max == rgb.b()) //
			H = ((rgb.r() - rgb.g()) / delta + 4) * 60;
		return new HSV(H, delta < EPSILON ? 0 : delta / max, max);
	}

	/** @return NaN if invalid. */
	default public HunterLab HunterLab () {
		return XYZ().HunterLab();
	}

	/** @return IHS color space normalized or NaN if invalid. */
	default public IHS IHS () {
		RGB rgb = RGB();
		float I = rgb.r() + rgb.g() + rgb.b();
		if (I < EPSILON) return new IHS(I, Float.NaN, Float.NaN);
		float H, S, min = rgb.min();
		if (rgb.b() == min) {
			float denom = I - 3 * rgb.b();
			H = Math.abs(denom) < EPSILON ? Float.NaN : (rgb.g() - rgb.b()) / denom;
		} else if (rgb.r() == min) {
			float denom = I - 3 * rgb.r();
			H = Math.abs(denom) < EPSILON ? Float.NaN : (rgb.b() - rgb.r()) / denom + 1;
		} else {
			float denom = I - 3 * rgb.g();
			H = Math.abs(denom) < EPSILON ? Float.NaN : (rgb.r() - rgb.g()) / denom + 2;
		}
		if (H >= 0 && H <= 1)
			S = (I - 3 * rgb.b()) / I;
		else if (H >= 1 && H <= 2)
			S = (I - 3 * rgb.r()) / I;
		else
			S = (I - 3 * rgb.g()) / I;
		return new IHS(I, H, S);
	}

	default public ITP ITP () {
		LinearRGB rgb = LinearRGB();
		float r2020 = 0.627404f * rgb.r() + 0.329282f * rgb.g() + 0.0433136f * rgb.b(); // To BT.2020.
		float g2020 = 0.069097f * rgb.r() + 0.91954f * rgb.g() + 0.0113612f * rgb.b();
		float b2020 = 0.0163916f * rgb.r() + 0.0880132f * rgb.g() + 0.895595f * rgb.b();
		float L = ITP.PQ_EOTF_inverse((1688 / 4096f) * r2020 + (2146 / 4096f) * g2020 + (262 / 4096f) * b2020);
		float M = ITP.PQ_EOTF_inverse((683 / 4096f) * r2020 + (2951 / 4096f) * g2020 + (462 / 4096f) * b2020);
		float S = ITP.PQ_EOTF_inverse((99 / 4096f) * r2020 + (309 / 4096f) * g2020 + (3688 / 4096f) * b2020);
		return new ITP( //
			0.5f * L + 0.5f * M, // L'M'S' to ITP.
			1.613769531f * L + -3.323486328f * M + 1.709716797f * S, //
			4.378173828f * L + -4.245605469f * M + -0.132568359f * S);
	}

	/** Uses {@link Observer#Default} D65. */
	default public Lab Lab () {
		return Lab(Observer.Default.D65);
	}

	/** @param whitePoint See {@link Illuminant}. */
	default public Lab Lab (XYZ whitePoint) {
		return XYZ().Lab(whitePoint);
	}

	/** Uses {@link Observer#Default} D65. */
	default public LCh LCh () {
		return LCh(Observer.Default.D65);
	}

	/** @param whitePoint See {@link Illuminant}. */
	default public LCh LCh (XYZ whitePoint) {
		return Lab(whitePoint).LCh();
	}

	/** Uses {@link Observer#Default} D65.
	 * @return NaN if invalid. */
	default public LCHuv LChuv () {
		return Luv().LCHuv();
	}

	default public LinearRGB LinearRGB () {
		return XYZ().LinearRGB();
	}

	/** Uses {@link com.esotericsoftware.color.space.LMS.CAT#CAT16}. */
	default public LMS LMS () {
		return LMS(CAT.CAT16);
	}

	default public LMS LMS (CAT matrix) {
		return XYZ().LMS(matrix);
	}

	/** Uses {@link Observer#Default} D65.
	 * @return NaN if invalid. */
	default public Luv Luv () {
		return XYZ().Luv(Observer.Default.D65);
	}

	/** @return NaN if invalid. */
	default public Luv Luv (XYZ whitePoint) {
		return XYZ().Luv(whitePoint);
	}

	/** O1O2 version 2. */
	default public O1O2 O1O2 () {
		RGB rgb = RGB();
		float O1 = (rgb.r() - rgb.g()) / 2;
		float O2 = (rgb.r() + rgb.g()) / 4 - rgb.b() / 2;
		return new O1O2(O1, O2);
	}

	default public Oklab Oklab () {
		LinearRGB rgb = LinearRGB();
		float l = (float)Math.cbrt(0.4122214708f * rgb.r() + 0.5363325363f * rgb.g() + 0.0514459929f * rgb.b());
		float m = (float)Math.cbrt(0.2119034982f * rgb.r() + 0.6806995451f * rgb.g() + 0.1073969566f * rgb.b());
		float s = (float)Math.cbrt(0.0883024619f * rgb.r() + 0.2817188376f * rgb.g() + 0.6299787005f * rgb.b());
		return new Oklab( //
			0.2104542553f * l + 0.793617785f * m - 0.0040720468f * s, //
			1.9779984951f * l - 2.428592205f * m + 0.4505937099f * s, //
			0.0259040371f * l + 0.7827717662f * m - 0.808675766f * s);
	}

	default public Oklch Oklch () {
		return Oklab().Oklch();
	}

	default public Okhsl Okhsl () {
		Oklab lab = Oklab();
		float L = lab.L();
		if (L >= 1 - EPSILON) return new Okhsl(Float.NaN, 0, 1); // White.
		if (L <= EPSILON) return new Okhsl(Float.NaN, 0, 0); // Black.
		float C = (float)Math.sqrt(lab.a() * lab.a() + lab.b() * lab.b());
		if (C < EPSILON) return new Okhsl(Float.NaN, 0, Okhsv.toe(L)); // Gray.
		float h = 0.5f + 0.5f * (float)Math.atan2(-lab.b(), -lab.a()) / PI;
		float a_ = lab.a() / C, b_ = lab.b() / C;
		float[] Cs = Okhsv.Cs(L, a_, b_);
		float C_0 = Cs[0], C_mid = Cs[1], C_max = Cs[2];
		float mid = 0.8f, s;
		if (C < C_mid) {
			float k_1 = mid * C_0, k_2 = (1 - k_1 / C_mid), t = C / (k_1 + k_2 * C);
			s = t * mid;
		} else {
			float mid_inv = 1.25f;
			float k_0 = C_mid, k_1 = (1 - mid) * C_mid * C_mid * mid_inv * mid_inv / C_0, k_2 = (1 - (k_1) / (C_max - C_mid));
			float t = (C - k_0) / (k_1 + k_2 * (C - k_0));
			s = mid + (1 - mid) * t;
		}
		return new Okhsl(h * 360, s, Okhsv.toe(L));
	}

	default public Okhsv Okhsv () {
		Oklab lab = Oklab();
		float L = lab.L();
		if (L >= 1 - EPSILON) return new Okhsv(Float.NaN, 0, 1); // White.
		if (L <= EPSILON) return new Okhsv(Float.NaN, 0, 0); // Black.
		float C = (float)Math.sqrt(lab.a() * lab.a() + lab.b() * lab.b());
		if (C < EPSILON) return new Okhsv(Float.NaN, 0, L); // Gray.
		float h = (float)Math.atan2(lab.b(), lab.a()) * radDeg;
		if (h < 0) h += 360;
		float a_ = lab.a() / C, b_ = lab.b() / C;
		float[] ST_max = Okhsv.cuspST(a_, b_);
		float T_max = ST_max[1], S_0 = 0.5f, k = 1 - S_0 / ST_max[0], t = T_max / (C + L * T_max);
		float L_v = t * L, C_v = t * C, L_vt = Okhsv.toeInv(L_v), C_vt = C_v * L_vt / L_v;
		LinearRGB l_r = new Oklab(L_vt, a_ * C_vt, b_ * C_vt).LinearRGB();
		L /= (float)Math.cbrt(1 / Math.max(0, l_r.max()));
		float Lt = Okhsv.toe(L);
		return new Okhsv(h, clamp((S_0 + T_max) * C_v / (T_max * S_0 + T_max * k * C_v)), clamp(Lt / L_v));
	}

	/** @return NaN if invalid. */
	default public rg rg () {
		RGB rgb = RGB();
		float sum = rgb.r() + rgb.g() + rgb.b();
		if (sum < EPSILON) return new rg(Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
		float rNorm = rgb.r() / sum, gNorm = rgb.g() / sum, bNorm = 1 - rNorm - gNorm;
		float rS = rNorm - 0.333f, gS = gNorm - 0.333f;
		float s = (float)Math.sqrt(rS * rS + gS * gS);
		float h = s < EPSILON ? Float.NaN : ((float)Math.atan2(rS, gS) * radDeg + 360) % 360;
		return new rg(rNorm, gNorm, bNorm, s, h);
	}

	default public RGB RGB () {
		return XYZ().RGB();
	}

	default public TSL TSL () {
		RGB rgb = RGB();
		float sum = rgb.r() + rgb.g() + rgb.b();
		if (sum < EPSILON) return new TSL(0, 0, 0); // Black
		float L = 0.299f * rgb.r() + 0.587f * rgb.g() + 0.114f * rgb.b();
		float r1 = rgb.r() / sum - 1 / 3f, g1 = rgb.g() / sum - 1 / 3f;
		float S = (float)Math.sqrt(9 / 5f * (r1 * r1 + g1 * g1));
		float T = 0;
		if (Math.abs(g1 - r1) > EPSILON || Math.abs(2 * g1 + r1) > EPSILON) {
			T = (float)Math.atan2(g1 - r1, 2 * g1 + r1) * radDeg;
			if (T < 0) T += 360;
			T = T / 360;
		}
		return new TSL(T, S, L);
	}

	default public uv uv () {
		return XYZ().uv();
	}

	default public uv1960 uv1960 () {
		return XYZ().uv1960();
	}

	default public xy xy () {
		return XYZ().xy();
	}

	public XYZ XYZ ();

	/** @return [0..100]. */
	default public float Y () {
		return XYZ().Y();
	}

	/** @return Relative luminance [0..1]. */
	default public float luminance () {
		return XYZ().Y() / 100;
	}

	default public YCbCr YCbCr (YCbCrColorSpace colorSpace) {
		RGB rgb = RGB();
		float Y, Cb, Cr;
		if (colorSpace == YCbCrColorSpace.ITU_BT_601) {
			Y = 0.299f * rgb.r() + 0.587f * rgb.g() + 0.114f * rgb.b();
			Cb = -0.168735892f * rgb.r() - 0.331264108f * rgb.g() + 0.5f * rgb.b();
			Cr = 0.5f * rgb.r() - 0.418687589f * rgb.g() - 0.081312411f * rgb.b();
		} else {
			Y = 0.2126f * rgb.r() + 0.7152f * rgb.g() + 0.0722f * rgb.b();
			Cb = -0.114572f * rgb.r() - 0.385428f * rgb.g() + 0.5f * rgb.b();
			Cr = 0.5f * rgb.r() - 0.454153f * rgb.g() - 0.045847f * rgb.b();
		}
		return new YCbCr(Y, Cb, Cr);
	}

	default public YCC YCC () {
		RGB rgb = RGB();
		return new YCC( //
			0.213f * rgb.r() + 0.419f * rgb.g() + 0.081f * rgb.b(), //
			-0.131f * rgb.r() - 0.256f * rgb.g() + 0.387f * rgb.b() + 0.612f, //
			0.373f * rgb.r() - 0.312f * rgb.g() - 0.061f * rgb.b() + 0.537f);
	}

	default public YCoCg YCoCg () {
		RGB rgb = RGB();
		return new YCoCg( //
			rgb.r() / 4 + rgb.g() / 2 + rgb.b() / 4, //
			rgb.r() / 2 - rgb.b() / 2, //
			-rgb.r() / 4 + rgb.g() / 2 - rgb.b() / 4);
	}

	default public YES YES () {
		RGB rgb = RGB();
		return new YES( //
			rgb.r() * 0.253f + rgb.g() * 0.684f + rgb.b() * 0.063f, //
			rgb.r() * 0.5f + rgb.g() * -0.5f, //
			rgb.r() * 0.25f + rgb.g() * 0.25f + rgb.b() * -0.5f);
	}

	default public YIQ YIQ () {
		RGB rgb = RGB();
		return new YIQ( //
			0.299f * rgb.r() + 0.587f * rgb.g() + 0.114f * rgb.b(), //
			0.595716f * rgb.r() - 0.274453f * rgb.g() - 0.321263f * rgb.b(), //
			0.211456f * rgb.r() - 0.522591f * rgb.g() + 0.311135f * rgb.b());
	}

	default public YUV YUV () {
		RGB rgb = RGB();
		return new YUV( //
			0.299f * rgb.r() + 0.587f * rgb.g() + 0.114f * rgb.b(), //
			-0.147141f * rgb.r() - 0.288869f * rgb.g() + 0.43601f * rgb.b(), //
			0.614975f * rgb.r() - 0.514965f * rgb.g() - 0.10001f * rgb.b());
	}

	/** {@link Lab#deltaE2000(Lab, float, float, float)} with 1 for lightness, chroma, and hue. */
	default public float deltaE2000 (Color other) {
		return Lab().deltaE2000(other.Lab(), 1, 1, 1);
	}

	/** Compares perceptual chromaticity.
	 * @return NaN if invalid. */
	default public float MacAdamSteps (Color other) {
		return uv().MacAdamSteps(other.uv());
	}
}
