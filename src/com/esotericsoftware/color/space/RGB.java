
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Gamut;
import com.esotericsoftware.color.Illuminant;
import com.esotericsoftware.color.Illuminant.CIE2;
import com.esotericsoftware.color.Util;
import com.esotericsoftware.color.space.LMS.CAT;
import com.esotericsoftware.color.space.YCbCr.YCbCrColorSpace;

/** Standard RGB with sRGB gamma encoding. Values are clamped [0..1], use {@link LinearRGB} or {@link XYZ} for interchange to
 * preserve wide-gamut colors. */
public record RGB (
	/** Red [0..1]. */
	float r,
	/** Green [0..1]. */
	float g,
	/** Blue [0..1]. */
	float b) {

	public RGB {
		r = clamp(r);
		g = clamp(g);
		b = clamp(b);
	}

	public RGB (int rgb) {
		this( //
			((rgb & 0xff0000) >>> 16) / 255f, //
			((rgb & 0x00ff00) >>> 8) / 255f, //
			((rgb & 0x0000ff)) / 255f);
	}

	public float get (int index) {
		return switch (index) {
		case 0 -> r;
		case 1 -> g;
		case 2 -> b;
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public RGB set (int index, float value) {
		return switch (index) {
		case 0 -> new RGB(clamp(value), g, b);
		case 1 -> new RGB(r, clamp(value), b);
		case 2 -> new RGB(r, g, clamp(value));
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public ACES2065_1 ACES2065_1 () {
		float r = linear(this.r), g = linear(this.g), b = linear(this.b);
		return new ACES2065_1( //
			0.43953127f * r + 0.38391885f * g + 0.17654988f * b, // To AP0.
			0.08959387f * r + 0.81347942f * g + 0.09692672f * b, //
			0.01738063f * r + 0.11176223f * g + 0.87085713f * b);
	}

	public ACEScg ACEScg () {
		float r = linear(this.r), g = linear(this.g), b = linear(this.b);
		return new ACEScg( //
			0.61309741f * r + 0.33952315f * g + 0.04737945f * b, // To AP1.
			0.07019486f * r + 0.91635524f * g + 0.0134499f * b, //
			0.0206156f * r + 0.10956263f * g + 0.86982177f * b);
	}

	public ACEScc ACEScc () {
		ACEScg cg = ACEScg();
		return new ACEScc(ACEScc.encode(cg.r()), ACEScc.encode(cg.g()), ACEScc.encode(cg.b()));
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public CAM16 CAM16 () {
		return CAM16(CAM16.VC.sRGB);
	}

	public CAM16 CAM16 (CAM16.VC vc) {
		float r = linear(this.r) * 100, g = linear(this.g) * 100, b = linear(this.b) * 100;
		return new XYZ( //
			0.41233895f * r + 0.35762064f * g + 0.18051042f * b, //
			0.2126f * r + 0.7152f * g + 0.0722f * b, //
			0.01932141f * r + 0.11916382f * g + 0.95034478f * b).CAM16(vc);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public CAM16UCS CAM16UCS () {
		return CAM16().CAM16UCS();
	}

	public CAM16UCS CAM16UCS (CAM16.VC vc) {
		return CAM16(vc).CAM16UCS();
	}

	public C1C2C3 C1C2C3 () {
		return new C1C2C3((float)Math.atan(r / Math.max(g, b)), //
			(float)Math.atan(g / Math.max(r, b)), //
			(float)Math.atan(b / Math.max(r, g)));
	}

	/** @return [1000..100000K] or NaN out of range.
	 * @see uv#CCT(CCT.Method) */
	public CCT CCT (CCT.Method method) {
		return uv().CCT(method);
	}

	/** Uses {@link CCT.Method#Robertson}.
	 * @return [1000..100000K] or NaN out of range. */
	public CCT CCT () {
		return uv().CCT();
	}

	public CMYK CMYK () {
		float K = 1 - max();
		if (1 - K < EPSILON) return new CMYK(0, 0, 0, K); // Black
		return new CMYK( //
			(1 - r - K) / (1 - K), //
			(1 - g - K) / (1 - K), //
			(1 - b - K) / (1 - K), K);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	public HCT HCT () {
		return HCT(CAM16.VC.sRGB);
	}

	public HCT HCT (CAM16.VC vc) {
		CAM16 cam16 = CAM16(vc);
		return new HCT(cam16.h(), cam16.C(), Lab.YtoLstar(Y()));
	}

	public HSI HSI () {
		float I = (r + g + b) / 3;
		float min = min(), S = I < EPSILON ? 0 : 1 - min / I, H = Float.NaN;
		if (S != 0 && I != 0) {
			float alpha = 0.5f * (2 * r - g - b);
			float beta = 0.8660254f * (g - b); // sqrt(3) / 2
			H = (float)Math.atan2(beta, alpha);
			if (H < 0) H += 2 * PI;
			H = H * radDeg;
		}
		return new HSI(H, S, I);
	}

	public HSL HSL () {
		float min = min(), max = max(), delta = max - min, L = (max + min) / 2;
		if (delta < EPSILON) return new HSL(Float.NaN, 0, L); // Gray.
		float S = L <= 0.5f ? delta / (max + min) : delta / (2 - max - min), H;
		if (r == max)
			H = (g - b) / 6 / delta;
		else if (g == max)
			H = 1 / 3f + (b - r) / 6 / delta;
		else
			H = 2 / 3f + (r - g) / 6 / delta;
		if (H < 0) H += 1;
		if (H > 1) H -= 1;
		H *= 360;
		return new HSL(H, S, L);
	}

	public HSLuv HSLuv () {
		LCHuv lch = LChuv();
		float L = lch.L(), C = lch.C(), H = lch.H();
		if (L > 100 - EPSILON) return new HSLuv(H, 0, 100);
		if (L < EPSILON) return new HSLuv(H, 0, 0);
		float maxChroma = HSLuv.maxChromaForLH(L, H);
		return new HSLuv(H, maxChroma < EPSILON ? 0 : Math.min(100, (C / maxChroma) * 100), L);
	}

	public HSV HSV () {
		float min = min(), max = max(), delta = max - min, H = 0;
		if (max == min)
			H = Float.NaN;
		else if (max == r) {
			H = (g - b) / delta * 60;
			if (H < 0) H += 360;
		} else if (max == g)
			H = ((b - r) / delta + 2) * 60;
		else if (max == b) //
			H = ((r - g) / delta + 4) * 60;
		return new HSV(H, delta < EPSILON ? 0 : delta / max, max);
	}

	/** @return NaN if invalid. */
	public HunterLab HunterLab () {
		return XYZ().HunterLab();
	}

	/** @return IHS color space normalized or NaN if invalid. */
	public IHS IHS () {
		float I = r + g + b;
		if (I < EPSILON) return new IHS(I, Float.NaN, Float.NaN);
		float H, S, min = min();
		if (b == min) {
			float denom = I - 3 * b;
			H = Math.abs(denom) < EPSILON ? Float.NaN : (g - b) / denom;
		} else if (r == min) {
			float denom = I - 3 * r;
			H = Math.abs(denom) < EPSILON ? Float.NaN : (b - r) / denom + 1;
		} else {
			float denom = I - 3 * g;
			H = Math.abs(denom) < EPSILON ? Float.NaN : (r - g) / denom + 2;
		}
		if (H >= 0 && H <= 1)
			S = (I - 3 * b) / I;
		else if (H >= 1 && H <= 2)
			S = (I - 3 * r) / I;
		else
			S = (I - 3 * g) / I;
		return new IHS(I, H, S);
	}

	public ITP ITP () {
		float r = linear(this.r), g = linear(this.g), b = linear(this.b);
		float r2020 = 0.627404f * r + 0.329282f * g + 0.0433136f * b; // To BT.2020.
		float g2020 = 0.069097f * r + 0.91954f * g + 0.0113612f * b;
		float b2020 = 0.0163916f * r + 0.0880132f * g + 0.895595f * b;
		float L = ITP.PQ_EOTF_inverse((1688 / 4096f) * r2020 + (2146 / 4096f) * g2020 + (262 / 4096f) * b2020);
		float M = ITP.PQ_EOTF_inverse((683 / 4096f) * r2020 + (2951 / 4096f) * g2020 + (462 / 4096f) * b2020);
		float S = ITP.PQ_EOTF_inverse((99 / 4096f) * r2020 + (309 / 4096f) * g2020 + (3688 / 4096f) * b2020);
		return new ITP( //
			0.5f * L + 0.5f * M, // L'M'S' to ITP.
			1.613769531f * L + -3.323486328f * M + 1.709716797f * S, //
			4.378173828f * L + -4.245605469f * M + -0.132568359f * S);
	}

	/** Uses {@link CIE2#D65}. */
	public Lab Lab () {
		return Lab(CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	public Lab Lab (XYZ tristimulus) {
		return XYZ().Lab(tristimulus);
	}

	/** Uses {@link CIE2#D65}. */
	public LCh LCh () {
		return LCh(CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	public LCh LCh (XYZ tristimulus) {
		return Lab(tristimulus).LCh();
	}

	/** Uses {@link CIE2#D65}.
	 * @return NaN if invalid. */
	public LCHuv LChuv () {
		return Luv().LCHuv();
	}

	/** Uses {@link CIE2#D65}.
	 * @return NaN if invalid. */
	public Luv Luv () {
		return XYZ().Luv(CIE2.D65);
	}

	/** @return NaN if invalid. */
	public Luv Luv (XYZ tristimulus) {
		return XYZ().Luv(tristimulus);
	}

	public LinearRGB LinearRGB () {
		return new LinearRGB(linear(r), linear(g), linear(b));
	}

	/** Uses the LMS CIECAM02 transformation matrix. */
	public LMS LMS () {
		return LMS(CAT.CAT02);
	}

	public LMS LMS (CAT matrix) {
		return XYZ().LMS(matrix);
	}

	/** Compares perceptual chromaticity.
	 * @return NaN if invalid. */
	public float MacAdamSteps (RGB other) {
		return uv().MacAdamSteps(other.uv());
	}

	/** O1O2 version 2. */
	public O1O2 O1O2 () {
		float O1 = (r - g) / 2;
		float O2 = (r + g) / 4 - b / 2;
		return new O1O2(O1, O2);
	}

	public Oklab Oklab () {
		float r = linear(this.r), g = linear(this.g), b = linear(this.b);
		float l = (float)Math.cbrt(0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b);
		float m = (float)Math.cbrt(0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b);
		float s = (float)Math.cbrt(0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b);
		return new Oklab( //
			0.2104542553f * l + 0.793617785f * m - 0.0040720468f * s, //
			1.9779984951f * l - 2.428592205f * m + 0.4505937099f * s, //
			0.0259040371f * l + 0.7827717662f * m - 0.808675766f * s);
	}

	public Oklch Oklch () {
		return Oklab().Oklch();
	}

	public Okhsl Okhsl () {
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

	public Okhsv Okhsv () {
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
	public rg rg () {
		float sum = r + g + b;
		if (sum < EPSILON) return new rg(Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
		float rNorm = r / sum, gNorm = g / sum, bNorm = 1 - rNorm - gNorm;
		float rS = rNorm - 0.333f, gS = gNorm - 0.333f;
		float s = (float)Math.sqrt(rS * rS + gS * gS);
		float h = s < EPSILON ? Float.NaN : ((float)Math.atan2(rS, gS) * radDeg + 360) % 360;
		return new rg(rNorm, gNorm, bNorm, s, h);
	}

	public TSL TSL () {
		float sum = r + g + b;
		if (sum < EPSILON) return new TSL(0, 0, 0); // Black
		float L = 0.299f * r + 0.587f * g + 0.114f * b;
		float r1 = r / sum - 1 / 3f, g1 = g / sum - 1 / 3f;
		float S = (float)Math.sqrt(9 / 5f * (r1 * r1 + g1 * g1));
		float T = 0;
		if (Math.abs(g1 - r1) > EPSILON || Math.abs(2 * g1 + r1) > EPSILON) {
			T = (float)Math.atan2(g1 - r1, 2 * g1 + r1) * radDeg;
			if (T < 0) T += 360;
			T = T / 360;
		}
		return new TSL(T, S, L);
	}

	/** @return NaN if invalid. */
	public uv uv () {
		return xy().uv();
	}

	/** Uses {@link Gamut#sRGB}.
	 * @return NaN if invalid. */
	public xy xy () {
		return Gamut.sRGB.xy(this);
	}

	public XYZ XYZ () {
		float r = linear(this.r), g = linear(this.g), b = linear(this.b);
		return new XYZ( //
			(0.4124564f * r + 0.3575761f * g + 0.1804375f * b) * 100, //
			(0.2126729f * r + 0.7151522f * g + 0.072175f * b) * 100, //
			(0.0193339f * r + 0.119192f * g + 0.9503041f * b) * 100);
	}

	/** @return [0..100]. */
	public float Y () {
		float r = linear(this.r), g = linear(this.g), b = linear(this.b);
		return (0.2126729f * r + 0.7151522f * g + 0.072175f * b) * 100;
	}

	/** @return Relative luminance [0..1]. */
	public float luminance () {
		float r = linear(this.r), g = linear(this.g), b = linear(this.b);
		return 0.2126729f * r + 0.7151522f * g + 0.072175f * b;
	}

	public YCbCr YCbCr (YCbCrColorSpace colorSpace) {
		float Y, Cb, Cr;
		if (colorSpace == YCbCrColorSpace.ITU_BT_601) {
			Y = 0.299f * r + 0.587f * g + 0.114f * b;
			Cb = -0.168735892f * r - 0.331264108f * g + 0.5f * b;
			Cr = 0.5f * r - 0.418687589f * g - 0.081312411f * b;
		} else {
			Y = 0.2126f * r + 0.7152f * g + 0.0722f * b;
			Cb = -0.114572f * r - 0.385428f * g + 0.5f * b;
			Cr = 0.5f * r - 0.454153f * g - 0.045847f * b;
		}
		return new YCbCr(Y, Cb, Cr);
	}

	public YCC YCC () {
		return new YCC( //
			0.213f * r + 0.419f * g + 0.081f * b, //
			-0.131f * r - 0.256f * g + 0.387f * b + 0.612f, //
			0.373f * r - 0.312f * g - 0.061f * b + 0.537f);
	}

	public YCoCg YCoCg () {
		return new YCoCg( //
			r / 4 + g / 2 + b / 4, //
			r / 2 - b / 2, //
			-r / 4 + g / 2 - b / 4);
	}

	public YES YES () {
		return new YES( //
			r * 0.253f + g * 0.684f + b * 0.063f, //
			r * 0.5f + g * -0.5f, //
			r * 0.25f + g * 0.25f + b * -0.5f);
	}

	public YIQ YIQ () {
		return new YIQ( //
			0.299f * r + 0.587f * g + 0.114f * b, //
			0.595716f * r - 0.274453f * g - 0.321263f * b, //
			0.211456f * r - 0.522591f * g + 0.311135f * b);
	}

	public YUV YUV () {
		return new YUV( //
			0.299f * r + 0.587f * g + 0.114f * b, //
			-0.147141f * r - 0.288869f * g + 0.43601f * b, //
			0.614975f * r - 0.514965f * g - 0.10001f * b);
	}

	public RGB add (float value) {
		return new RGB(clamp(r + value), clamp(g + value), clamp(b + value));
	}

	public RGB add (int index, float value) {
		return switch (index) {
		case 0 -> new RGB(clamp(r + value), g, b);
		case 1 -> new RGB(r, clamp(g + value), b);
		case 2 -> new RGB(r, g, clamp(b + value));
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public RGB add (float r, float g, float b) {
		return new RGB(clamp(this.r + r), clamp(this.g + g), clamp(this.b + b));
	}

	public RGB lerp (RGB other, float t) {
		return new RGB(clamp(Util.lerp(r, other.r(), t)), clamp(Util.lerp(g, other.g(), t)), clamp(Util.lerp(b, other.b(), t)));
	}

	public float max () {
		return Util.max(r, g, b);
	}

	public float min () {
		return Util.min(r, g, b);
	}

	public RGB nor () {
		float max = max();
		return max < EPSILON ? this : new RGB(r / max, g / max, b / max);
	}

	public RGB sub (float value) {
		return new RGB(clamp(r - value), clamp(g - value), clamp(b - value));
	}

	public RGB sub (int index, float value) {
		return switch (index) {
		case 0 -> new RGB(clamp(r - value), g, b);
		case 1 -> new RGB(r, clamp(g - value), b);
		case 2 -> new RGB(r, g, clamp(b - value));
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public RGB sub (float r, float g, float b) {
		return new RGB(clamp(this.r - r), clamp(this.g - g), clamp(this.b - b));
	}

	public RGB scl (float value) {
		return new RGB(clamp(r * value), clamp(g * value), clamp(b * value));
	}

	public RGB scl (int index, float value) {
		return switch (index) {
		case 0 -> new RGB(clamp(r * value), g, b);
		case 1 -> new RGB(r, clamp(g * value), b);
		case 2 -> new RGB(r, g, clamp(b * value));
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public RGB scl (float r, float g, float b) {
		return new RGB(clamp(this.r * r), clamp(this.g * g), clamp(this.b * b));
	}

	public float dst (RGB other) {
		return (float)Math.sqrt(dst2(other));
	}

	public float dst2 (RGB other) {
		float dr = r - other.r, dg = g - other.g, db = b - other.b;
		return dr * dr + dg * dg + db * db;
	}

	public float len () {
		return (float)Math.sqrt(len2());
	}

	public float len2 () {
		return r * r + g * g + b * b;
	}

	/** {@link Lab#deltaE2000(Lab, float, float, float)} with 1 for lightness, chroma, and hue. */
	public float deltaE2000 (RGB other) {
		return Lab().deltaE2000(other.Lab(), 1, 1, 1);
	}

	public float grayscale () {
		return r * 0.2125f + g * 0.7154f + b * 0.0721f;
	}

	public boolean achromatic () {
		return max() - min() < EPSILON;
	}

	/** Returns colors opposite on color wheel. */
	public RGB complementary () {
		HSL hsl = HSL();
		float h = hsl.H() + 180;
		if (h >= 360) h -= 360;
		return new HSL(h, hsl.S(), hsl.L()).RGB();
	}

	/** Returns 3 colors evenly spaced on color wheel. */
	public RGB[] triadic () {
		HSL hsl = HSL();
		float h1 = hsl.H() + 120;
		float h2 = hsl.H() + 240;
		if (h1 >= 360) h1 -= 360;
		if (h2 >= 360) h2 -= 360;
		return new RGB[] {this, new HSL(h1, hsl.S(), hsl.L()).RGB(), new HSL(h2, hsl.S(), hsl.L()).RGB()};
	}

	/** Returns 3 colors adjacent on color wheel.
	 * @param angle [0..360] */
	public RGB[] analogous (float angle) {
		HSL hsl = HSL();
		float h1 = hsl.H() + angle;
		float h2 = hsl.H() - angle;
		if (h1 >= 360) h1 -= 360;
		if (h2 < 0) h2 += 360;
		return new RGB[] {new HSL(h2, hsl.S(), hsl.L()).RGB(), this, new HSL(h1, hsl.S(), hsl.L()).RGB()};
	}

	/** Returns a split-complementary color scheme. */
	public RGB[] splitComplementary () {
		HSL hsl = HSL();
		float h1 = hsl.H() + 150;
		float h2 = hsl.H() + 210;
		if (h1 >= 360) h1 -= 360;
		if (h2 >= 360) h2 -= 360;
		return new RGB[] {this, new HSL(h1, hsl.S(), hsl.L()).RGB(), new HSL(h2, hsl.S(), hsl.L()).RGB()};
	}

	/** Returns the WCAG contrast ratio between foreground and background colors.
	 * @return Contrast ratio, 1:1 to 21:1. */
	public float contrastRatio (RGB bg) {
		float fgLum = Y() / 100;
		float bgLum = bg.Y() / 100;
		float L1 = Math.max(fgLum, bgLum);
		float L2 = Math.min(fgLum, bgLum);
		return (L1 + 0.05f) / (L2 + 0.05f);
	}

	/** Returns true if the colors meet the WCAG AA contrast accessibility standard.
	 * @param largeText true for 18pt+ normal or 14pt+ bold text */
	public boolean WCAG_AA (RGB bg, boolean largeText) {
		return contrastRatio(bg) >= (largeText ? 3 : 4.5f);
	}

	/** Returns true if the colors meet the WCAG AAA contrast accessibility standard.
	 * @param largeText true for 18pt+ normal or 14pt+ bold text */
	public boolean WCAG_AAA (RGB bg, boolean largeText) {
		return contrastRatio(bg) >= (largeText ? 4.5f : 7);
	}
}
