
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Util.*;

import java.lang.reflect.RecordComponent;

import com.esotericsoftware.colors.Colors.CAM16;
import com.esotericsoftware.colors.Colors.Illuminant;
import com.esotericsoftware.colors.Util.ACESccUtil;
import com.esotericsoftware.colors.Util.CCTUtil;
import com.esotericsoftware.colors.Util.HCTUtil;
import com.esotericsoftware.colors.Util.HSLUtil;
import com.esotericsoftware.colors.Util.HSLuvUtil;
import com.esotericsoftware.colors.Util.ITPUtil;
import com.esotericsoftware.colors.Util.LMSUtil;
import com.esotericsoftware.colors.Util.LabUtil;
import com.esotericsoftware.colors.Util.OkhsvUtil;

/** @author Nathan Sweet <misc@n4te.com> */
public class Colors {
	static public ACES2065_1 ACES2065_1 (RGB rgb) {
		float r = linear(rgb.r), g = linear(rgb.g), b = linear(rgb.b);
		return new ACES2065_1( //
			0.43953127f * r + 0.38391885f * g + 0.17654988f * b, // To AP0.
			0.08959387f * r + 0.81347942f * g + 0.09692672f * b, //
			0.01738063f * r + 0.11176223f * g + 0.87085713f * b);
	}

	static public ACEScg ACEScg (RGB rgb) {
		float r = linear(rgb.r), g = linear(rgb.g), b = linear(rgb.b);
		return new ACEScg( //
			0.61309741f * r + 0.33952315f * g + 0.04737945f * b, // To AP1.
			0.07019486f * r + 0.91635524f * g + 0.01344990f * b, //
			0.02061560f * r + 0.10956263f * g + 0.86982177f * b);
	}

	static public ACEScc ACEScc (RGB rgb) {
		ACEScg cg = ACEScg(rgb);
		return new ACEScc(ACESccUtil.encode(cg.r), ACESccUtil.encode(cg.g), ACESccUtil.encode(cg.b));
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	static public CAM16 CAM16 (CAM16UCS ucs) {
		return CAM16(ucs, CAM16.VC.sRGB);
	}

	static public CAM16 CAM16 (CAM16UCS ucs, CAM16.VC vc) { // Based on Copyright 2021 Google LLC (Apache 2.0).
		float jstar = ucs.J, astar = ucs.a, bstar = ucs.b;
		float C = (float)(Math.expm1(Math.hypot(astar, bstar) * 0.0228) / 0.0228) / vc.FLRoot;
		float h = (float)Math.atan2(bstar, astar) * radDeg;
		if (h < 0) h += 360;
		float J = jstar / (1 - (jstar - 100) * 0.007f), sqrtJ = (float)Math.sqrt(J / 100);
		return new CAM16(J, C, h, 4 / vc.c * sqrtJ * (vc.Aw + 4) * vc.FLRoot, C * vc.FLRoot,
			50 * (float)Math.sqrt((C / sqrtJ * vc.c) / (vc.Aw + 4)));
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	static public CAM16 CAM16 (RGB rgb) {
		return CAM16(rgb, CAM16.VC.sRGB);
	}

	static public CAM16 CAM16 (RGB rgb, CAM16.VC vc) {
		float r = linear(rgb.r) * 100, g = linear(rgb.g) * 100, b = linear(rgb.b) * 100;
		return CAM16(new XYZ( //
			0.41233895f * r + 0.35762064f * g + 0.18051042f * b, //
			0.2126f * r + 0.7152f * g + 0.0722f * b, //
			0.01932141f * r + 0.11916382f * g + 0.95034478f * b), vc);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	static public CAM16 CAM16 (XYZ XYZ) {
		return CAM16(XYZ, CAM16.VC.sRGB);
	}

	static public CAM16 CAM16 (XYZ XYZ, CAM16.VC vc) { // Based on Copyright 2021 Google LLC (Apache 2.0).
		float rT = (XYZ.X * 0.401288f) + (XYZ.Y * 0.650173f) + (XYZ.Z * -0.051461f); // To cone/RGB responses.
		float gT = (XYZ.X * -0.250268f) + (XYZ.Y * 1.204414f) + (XYZ.Z * 0.045854f);
		float bT = (XYZ.X * -0.002079f) + (XYZ.Y * 0.048952f) + (XYZ.Z * 0.953127f);
		float rD = vc.rgbD[0] * rT; // Discount illuminant.
		float gD = vc.rgbD[1] * gT;
		float bD = vc.rgbD[2] * bT;
		float rAF = (float)Math.pow(vc.FL * Math.abs(rD) / 100, 0.42); // Chromatic adaptation.
		float gAF = (float)Math.pow(vc.FL * Math.abs(gD) / 100, 0.42);
		float bAF = (float)Math.pow(vc.FL * Math.abs(bD) / 100, 0.42);
		float rA = Math.signum(rD) * 400 * rAF / (rAF + 27.13f);
		float gA = Math.signum(gD) * 400 * gAF / (gAF + 27.13f);
		float bA = Math.signum(bD) * 400 * bAF / (bAF + 27.13f);
		float a = (11 * rA + -12 * gA + bA) / 11; // Redness-greenness.
		float b = (rA + gA - 2 * bA) / 9; // Yellowness-blueness.
		float u = (20 * rA + 20 * gA + 21 * bA) / 20; // Auxiliary components.
		float p2 = (40 * rA + 20 * gA + bA) / 20;
		float hDeg = (float)Math.atan2(b, a) * radDeg; // Hue.
		float h = hDeg < 0 ? hDeg + 360 : hDeg >= 360 ? hDeg - 360 : hDeg;
		float ac = p2 * vc.Nbb; // Achromatic response to color.
		float J = 100 * (float)Math.pow(ac / vc.Aw, vc.c * vc.z); // CAM16 lightness and brightness.
		float huePrime = (h < 20.14f) ? h + 360 : h; // CAM16 chroma, colorfulness, and saturation.
		float eHue = 0.25f * ((float)Math.cos(huePrime * degRad + 2) + 3.8f);
		float p1 = 50000 / 13f * eHue * vc.Nc * vc.Ncb;
		float t = p1 * (float)Math.hypot(a, b) / (u + 0.305f);
		float alpha = (float)Math.pow(1.64 - Math.pow(0.29, vc.n), 0.73) * (float)Math.pow(t, 0.9);
		float C = alpha * (float)Math.sqrt(J / 100); // CAM16 chroma, colorfulness, saturation.
		return new CAM16(J, C, h, 4 / vc.c * (float)Math.sqrt(J / 100) * (vc.Aw + 4) * vc.FLRoot, C * vc.FLRoot,
			50 * (float)Math.sqrt((alpha * vc.c) / (vc.Aw + 4)));
	}

	static public CAM16UCS CAM16UCS (CAM16 cam16) { // Based on Copyright 2021 Google LLC (Apache 2.0).
		float J = cam16.J, M = cam16.M, h = cam16.h * degRad;
		float Jstar = 1.7f * J / (1 + 0.007f * J);
		float Mstar = (1 / 0.0228f) * (float)Math.log1p(0.0228f * M);
		return new CAM16UCS(Jstar, Mstar * (float)Math.cos(h), Mstar * (float)Math.sin(h));
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	static public CAM16UCS CAM16UCS (RGB rgb) {
		return CAM16UCS(CAM16(rgb));
	}

	static public CAM16UCS CAM16UCS (RGB rgb, CAM16.VC vc) {
		return CAM16UCS(CAM16(rgb, vc));
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	static public CAM16UCS CAM16UCS (XYZ XYZ) {
		return CAM16UCS(CAM16(XYZ));
	}

	static public CAM16UCS CAM16UCS (XYZ XYZ, CAM16.VC vc) {
		return CAM16UCS(CAM16(XYZ, vc));
	}

	static public C1C2C3 C1C2C3 (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		return new C1C2C3((float)Math.atan(r / Math.max(g, b)), //
			(float)Math.atan(g / Math.max(r, b)), //
			(float)Math.atan(b / Math.max(r, g)));
	}

	/** @return CCT [1667..25000K] or NaN if invalid. */
	static public float CCT (RGB rgb) {
		return CCT(xy(rgb));
	}

	/** @return CCT [1667..25000K] or NaN if invalid. */
	static public float CCT (uv uv) {
		return CCT(xy(uv));
	}

	/** <0.5K error, 0.021K average at <7000K. <1.1K error, 0.065K average at <14000K. <4.9K error, 0.3K average at <25000K.
	 * @return CCT [1667..25000K] or NaN if outside valid range. */
	static public float CCT (xy xy) {
		float x = xy.x, y = xy.y;
		if (x < 0.25f || x > 0.565f || y < 0.20f || y > 0.45f) return Float.NaN;
		float n = (x - 0.3320f) / (0.1858f - y);
		float CCT = 449 * n * n * n + 3525 * n * n + 6823.3f * n + 5520.33f; // McCamy initial guess.
		if (CCT < 1667 || CCT > 25000) return Float.NaN;
		float adjust = CCT < 7000 ? 0.000489f : (CCT < 15000 ? 0.0024f : 0.00095f);
		for (int i = 0; i < 3; i++) {
			xy current = xy(CCT, 0);
			float ex = x - current.x, ey = y - current.y;
			if (ex * ex + ey * ey < 1e-10f) break;
			float h = CCT * adjust;
			xy next = xy(CCT + h, 0);
			float tx = (next.x - current.x) / h, ty = (next.y - current.y) / h;
			CCT += (ex * tx + ey * ty) / (tx * tx + ty * ty);
		}
		return CCT;
	}

	static public CMYK CMYK (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		float K = 1 - max(r, g, b);
		if (1 - K < EPSILON) return new CMYK(0, 0, 0, K); // Black
		return new CMYK( //
			(1 - r - K) / (1 - K), //
			(1 - g - K) / (1 - K), //
			(1 - b - K) / (1 - K), K);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	static public HCT HCT (RGB rgb) {
		return HCT(rgb, CAM16.VC.sRGB);
	}

	static public HCT HCT (RGB rgb, CAM16.VC vc) {
		CAM16 cam16 = CAM16(rgb, vc);
		return new HCT(cam16.h, cam16.C, LabUtil.YtoLstar(XYZ(rgb).Y));
	}

	static public HSI HSI (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		float I = (r + g + b) / 3;
		float min = min(r, g, b);
		float S = I < EPSILON ? 0 : 1 - min / I, H = Float.NaN;
		if (S != 0 && I != 0) {
			float alpha = 0.5f * (2 * r - g - b);
			float beta = 0.8660254f * (g - b); // sqrt(3) / 2
			H = (float)Math.atan2(beta, alpha);
			if (H < 0) H += 2 * PI;
			H = H * radDeg;
		}
		return new HSI(H, S, I);
	}

	static public HSL HSL (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		float min = min(r, g, b), max = max(r, g, b), delta = max - min, L = (max + min) / 2;
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

	static public HSLuv HSLuv (RGB rgb) {
		LCHuv lch = LCHuv(Luv(rgb));
		float L = lch.L, C = lch.C, H = lch.H;
		if (L > 100 - EPSILON) return new HSLuv(H, 0, 100);
		if (L < EPSILON) return new HSLuv(H, 0, 0);
		float maxChroma = HSLuvUtil.maxChromaForLH(L, H);
		return new HSLuv(H, maxChroma < EPSILON ? 0 : Math.min(100, (C / maxChroma) * 100), L);
	}

	static public HSV HSV (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		float min = min(r, g, b), max = max(r, g, b), delta = max - min, H = 0;
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
	static public HunterLab HunterLab (RGB rgb) {
		return HunterLab(XYZ(rgb));
	}

	/** @return NaN if invalid. */
	static public HunterLab HunterLab (XYZ xyz) {
		float X = xyz.X, Y = xyz.Y, Z = xyz.Z;
		if (Y < EPSILON) return new HunterLab(0, 0, 0);
		float sqrt = (float)Math.sqrt(Y);
		return new HunterLab(10 * sqrt, 17.5f * ((1.02f * X - Y) / sqrt), 7 * ((Y - 0.847f * Z) / sqrt));
	}

	/** @return IHS color space normalized or NaN if invalid. */
	static public IHS IHS (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		float I = r + g + b;
		if (I < EPSILON) return new IHS(I, Float.NaN, Float.NaN);
		float H, S, min = min(r, g, b);
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

	static public ITP ITP (RGB rgb) {
		float r = linear(rgb.r), g = linear(rgb.g), b = linear(rgb.b);
		float r2020 = 0.6274040f * r + 0.3292820f * g + 0.0433136f * b; // To BT.2020.
		float g2020 = 0.0690970f * r + 0.9195400f * g + 0.0113612f * b;
		float b2020 = 0.0163916f * r + 0.0880132f * g + 0.8955950f * b;
		float L = ITPUtil.PQ_EOTF_inverse((1688f / 4096f) * r2020 + (2146f / 4096f) * g2020 + (262f / 4096f) * b2020);
		float M = ITPUtil.PQ_EOTF_inverse((683f / 4096f) * r2020 + (2951f / 4096f) * g2020 + (462f / 4096f) * b2020);
		float S = ITPUtil.PQ_EOTF_inverse((99f / 4096f) * r2020 + (309f / 4096f) * g2020 + (3688f / 4096f) * b2020);
		return new ITP( //
			0.5f * L + 0.5f * M + 0f * S, // L'M'S' to ITP.
			1.613769531f * L + -3.323486328f * M + 1.709716797f * S, //
			4.378173828f * L + -4.245605469f * M + -0.132568359f * S);
	}

	static public Lab Lab (LCh lch) {
		float L = lch.L, C = lch.C, h = lch.h;
		if (C < EPSILON || Float.isNaN(h)) return new Lab(L, 0, 0);
		return new Lab(L, C * (float)Math.cos(h * degRad), C * (float)Math.sin(h * degRad));
	}

	/** Uses {@link Illuminant.CIE2#D65}. */
	static public Lab Lab (uv uv) {
		return Lab(uv, Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public Lab Lab (uv uv, XYZ tristimulus) {
		return Lab(XYZ(uv), tristimulus);
	}

	/** Uses {@link Illuminant.CIE2#D65}. */
	static public Lab Lab (RGB rgb) {
		return Lab(rgb, Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public Lab Lab (RGB rgb, XYZ tristimulus) {
		return Lab(XYZ(rgb), tristimulus);
	}

	/** Uses {@link Illuminant.CIE2#D65}. */
	static public Lab Lab (XYZ XYZ) {
		return Lab(XYZ, Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public Lab Lab (XYZ XYZ, XYZ tristimulus) {
		float X = XYZ.X / tristimulus.X, Y = XYZ.Y / tristimulus.Y, Z = XYZ.Z / tristimulus.Z;
		X = X > LabUtil.e ? (float)Math.pow(X, 1 / 3d) : (LabUtil.k * X + 16) / 116;
		Y = Y > LabUtil.e ? (float)Math.pow(Y, 1 / 3d) : (LabUtil.k * Y + 16) / 116;
		Z = Z > LabUtil.e ? (float)Math.pow(Z, 1 / 3d) : (LabUtil.k * Z + 16) / 116;
		return new Lab(116 * Y - 16, 500 * (X - Y), 200 * (Y - Z));
	}

	/** Uses {@link Illuminant.CIE2#D65}.
	 * @return NaN if invalid. */
	static public Luv Luv (RGB rgb) {
		return Luv(XYZ(rgb), Illuminant.CIE2.D65);
	}

	/** @return NaN if invalid. */
	static public Luv Luv (RGB rgb, XYZ tristimulus) {
		return Luv(XYZ(rgb), tristimulus);
	}

	/** Uses {@link Illuminant.CIE2#D65}.
	 * @return NaN if invalid. */
	static public Luv Luv (XYZ XYZ) {
		return Luv(XYZ, Illuminant.CIE2.D65);
	}

	/** @return NaN if invalid. */
	static public Luv Luv (XYZ XYZ, XYZ tristimulus) {
		float X = XYZ.X, Y = XYZ.Y, Z = XYZ.Z;
		float Xn = tristimulus.X, Yn = tristimulus.Y, Zn = tristimulus.Z;
		float yr = Y / Yn, L = yr > LabUtil.e ? 116 * (float)Math.cbrt(yr) - 16 : LabUtil.k * yr;
		float divisor = X + 15 * Y + 3 * Z, divisorN = Xn + 15 * Yn + 3 * Zn;
		if (divisor < EPSILON || divisorN < EPSILON) return new Luv(L, Float.NaN, Float.NaN);
		float u_prime = 4 * X / divisor, v_prime = 9 * Y / divisor;
		float un_prime = 4 * Xn / divisorN, vn_prime = 9 * Yn / divisorN;
		return new Luv(L, 13 * L * (u_prime - un_prime), 13 * L * (v_prime - vn_prime));
	}

	static public Luv Luv (LCHuv lch) {
		float L = lch.L, C = lch.C, H = lch.H;
		if (C < EPSILON || Float.isNaN(H)) return new Luv(L, 0, 0);
		float rad = H * degRad;
		return new Luv(L, C * (float)Math.cos(rad), C * (float)Math.sin(rad));
	}

	static public LCHuv LCHuv (Luv luv) {
		float L = luv.L, u = luv.u, v = luv.v;
		float C = (float)Math.sqrt(u * u + v * v);
		float H = C < EPSILON ? Float.NaN : (float)Math.atan2(v, u) * radDeg;
		return new LCHuv(L, C, H < 0 ? H + 360 : H);
	}

	static public LCh LCh (Lab Lab) {
		float L = Lab.L, a = Lab.a, b = Lab.b;
		float C = (float)Math.sqrt(a * a + b * b);
		float h = C < EPSILON ? Float.NaN : (float)Math.atan2(b, a) * radDeg;
		if (h < 0) h += 360;
		return new LCh(L, C, h);
	}

	/** Uses {@link Illuminant.CIE2#D65}. */
	static public LCh LCh (RGB rgb) {
		return LCh(rgb, Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public LCh LCh (RGB rgb, XYZ tristimulus) {
		return LCh(Lab(rgb, tristimulus));
	}

	static public LinearRGB LinearRGB (RGB rgb) {
		return new LinearRGB(linear(rgb.r), linear(rgb.g), linear(rgb.b));
	}

	static public LinearRGB LinearRGB (XYZ xyz) {
		float X = xyz.X / 100, Y = xyz.Y / 100, Z = xyz.Z / 100;
		return new LinearRGB( //
			3.2404542f * X - 1.5371385f * Y - 0.4985314f * Z, //
			-0.9692660f * X + 1.8760108f * Y + 0.0415560f * Z, //
			0.0556434f * X - 0.2040259f * Y + 1.0572252f * Z);
	}

	static public LinearRGB LinearRGB (Oklab Oklab) {
		float L = Oklab.L, a = Oklab.a, b = Oklab.b;
		float l = L + 0.3963377774f * a + 0.2158037573f * b;
		float m = L - 0.1055613458f * a - 0.0638541728f * b;
		float s = L - 0.0894841775f * a - 1.2914855480f * b;
		l *= l * l;
		m *= m * m;
		s *= s * s;
		return new LinearRGB( //
			(+4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s), //
			(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s), //
			(-0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s));
	}

	/** Uses the LMS CIECAM02 transformation matrix. */
	static public LMS LMS (RGB rgb) {
		return LMS(rgb, CAT.CAT02);
	}

	static public LMS LMS (RGB rgb, CAT matrix) {
		return LMS(XYZ(rgb), matrix);
	}

	/** Uses the LMS CIECAM02 transformation matrix. */
	static public LMS LMS (XYZ XYZ) {
		return LMS(XYZ, CAT.CAT02);
	}

	static public LMS LMS (XYZ XYZ, CAT matrix) {
		float[] lms = matrixMultiply(XYZ.X, XYZ.Y, XYZ.Z, switch (matrix) {
		case HPE -> LMSUtil.HPE_forward;
		case Bradford -> LMSUtil.Bradford_forward;
		case VonKries -> LMSUtil.vonKries_forward;
		case CAT97 -> LMSUtil.CAT97_forward;
		default -> LMSUtil.CAT02_forward;
		});
		return new LMS(lms[0], lms[1], lms[2]);
	}

	/** O1O2 version 2. */
	static public O1O2 O1O2 (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		float O1 = (r - g) / 2;
		float O2 = (r + g) / 4 - b / 2;
		return new O1O2(O1, O2);
	}

	static public Oklab Oklab (Oklch Oklch) {
		float L = Oklch.L, C = Oklch.C, h = Oklch.h * degRad;
		if (C < EPSILON || Float.isNaN(h)) return new Oklab(L, 0, 0);
		return new Oklab(L, C * (float)Math.cos(h), C * (float)Math.sin(h));
	}

	static public Oklab Oklab (RGB rgb) {
		float r = linear(rgb.r), g = linear(rgb.g), b = linear(rgb.b);
		float l = (float)Math.cbrt(0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b);
		float m = (float)Math.cbrt(0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b);
		float s = (float)Math.cbrt(0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b);
		return new Oklab( //
			0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s, //
			1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s, //
			0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s);
	}

	static public Oklab Oklab (XYZ xyz) {
		float X = xyz.X / 100f, Y = xyz.Y / 100f, Z = xyz.Z / 100f;
		float r = 3.2404542f * X - 1.5371385f * Y - 0.4985314f * Z; // To linear RGB without clamp, D65.
		float g = -0.9692660f * X + 1.8760108f * Y + 0.0415560f * Z;
		float b = 0.0556434f * X - 0.2040259f * Y + 1.0572252f * Z;
		float l = (float)Math.cbrt(0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b);
		float m = (float)Math.cbrt(0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b);
		float s = (float)Math.cbrt(0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b);
		return new Oklab( //
			0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s, //
			1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s, //
			0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s);
	}

	static public Oklch Oklch (Oklab Oklab) {
		float L = Oklab.L, a = Oklab.a, b = Oklab.b;
		float C = (float)Math.sqrt(a * a + b * b);
		float h = C < EPSILON ? Float.NaN : (float)Math.atan2(b, a) * radDeg;
		if (h < 0) h += 360;
		return new Oklch(L, C, h);
	}

	static public Oklch Oklch (RGB rgb) {
		return Oklch(Oklab(rgb));
	}

	static public Okhsl Okhsl (RGB rgb) {
		Oklab lab = Oklab(rgb);
		float L = lab.L;
		if (L >= 1 - EPSILON) return new Okhsl(Float.NaN, 0, 1); // White.
		if (L <= EPSILON) return new Okhsl(Float.NaN, 0, 0); // Black.
		float C = (float)Math.sqrt(lab.a * lab.a + lab.b * lab.b);
		if (C < EPSILON) return new Okhsl(Float.NaN, 0, OkhsvUtil.toe(L)); // Gray.
		float h = 0.5f + 0.5f * (float)Math.atan2(-lab.b, -lab.a) / PI;
		float a_ = lab.a / C, b_ = lab.b / C;
		float[] Cs = OkhsvUtil.Cs(L, a_, b_);
		float C_0 = Cs[0], C_mid = Cs[1], C_max = Cs[2];
		float mid = 0.8f, s;
		if (C < C_mid) {
			float k_1 = mid * C_0, k_2 = (1.f - k_1 / C_mid), t = C / (k_1 + k_2 * C);
			s = t * mid;
		} else {
			float mid_inv = 1.25f;
			float k_0 = C_mid, k_1 = (1.f - mid) * C_mid * C_mid * mid_inv * mid_inv / C_0, k_2 = (1.f - (k_1) / (C_max - C_mid));
			float t = (C - k_0) / (k_1 + k_2 * (C - k_0));
			s = mid + (1.f - mid) * t;
		}
		return new Okhsl(h * 360, s, OkhsvUtil.toe(L));
	}

	static public Okhsv Okhsv (RGB rgb) {
		Oklab lab = Oklab(rgb);
		float L = lab.L;
		if (L >= 1 - EPSILON) return new Okhsv(Float.NaN, 0, 1); // White.
		if (L <= EPSILON) return new Okhsv(Float.NaN, 0, 0); // Black.
		float C = (float)Math.sqrt(lab.a * lab.a + lab.b * lab.b);
		if (C < EPSILON) return new Okhsv(Float.NaN, 0, L); // Gray.
		float h = (float)Math.atan2(lab.b, lab.a) * radDeg;
		if (h < 0) h += 360;
		float a_ = lab.a / C, b_ = lab.b / C;
		float[] ST_max = OkhsvUtil.cuspST(a_, b_);
		float T_max = ST_max[1], S_0 = 0.5f, k = 1 - S_0 / ST_max[0], t = T_max / (C + L * T_max);
		float L_v = t * L, C_v = t * C, L_vt = OkhsvUtil.toeInv(L_v), C_vt = C_v * L_vt / L_v;
		var l_r = LinearRGB(new Oklab(L_vt, a_ * C_vt, b_ * C_vt));
		L /= (float)Math.cbrt(1.f / Math.max(0, max(l_r.r, l_r.g, l_r.b)));
		float Lt = OkhsvUtil.toe(L);
		return new Okhsv(h, clamp((S_0 + T_max) * C_v / (T_max * S_0 + T_max * k * C_v)), clamp(Lt / L_v));
	}

	static public RGB RGB (int rgb) {
		return new RGB( //
			((rgb & 0xff0000) >>> 16) / 255f, //
			((rgb & 0x00ff00) >>> 8) / 255f, //
			((rgb & 0x0000ff)) / 255f);
	}

	static public RGB RGB (ACES2065_1 aces) {
		float r = aces.r, g = aces.g, b = aces.b;
		float rLin = 2.52140088f * r + -1.13389840f * g + -0.38750249f * b; // From AP0.
		float gLin = -0.27621892f * r + 1.37270743f * g + -0.09648852f * b;
		float bLin = -0.01538264f * r + -0.15297240f * g + 1.16835505f * b;
		return new RGB(sRGB(rLin), sRGB(gLin), sRGB(bLin));
	}

	static public RGB RGB (ACEScc aces) {
		return RGB(new ACEScg(ACESccUtil.decode(aces.r), ACESccUtil.decode(aces.g), ACESccUtil.decode(aces.b)));
	}

	static public RGB RGB (ACEScg aces) {
		float r = aces.r, g = aces.g, b = aces.b;
		float rLinear = 1.70482663f * r + -0.62151743f * g + -0.08330920f * b; // From AP1.
		float gLinear = -0.13028185f * r + 1.14085365f * g + -0.01057180f * b;
		float bLinear = -0.02400720f * r + -0.12895973f * g + 1.15296693f * b;
		return new RGB(sRGB(rLinear), sRGB(gLinear), sRGB(bLinear));
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	static public RGB RGB (CAM16 cam16) {
		return RGB(XYZ(cam16, CAM16.VC.sRGB));
	}

	static public RGB RGB (CAM16 cam16, CAM16.VC vc) {
		return RGB(XYZ(cam16, vc));
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	static public RGB RGB (CAM16UCS ucs) {
		return RGB(ucs, CAM16.VC.sRGB);
	}

	static public RGB RGB (CAM16UCS ucs, CAM16.VC vc) {
		return RGB(CAM16(ucs, vc), vc);
	}

	/** @param CCT [1667..25000K]
	 * @return NaN if invalid. */
	static public RGB RGB (float CCT, float Duv) {
		return RGB(xy(CCT, Duv));
	}

	static public RGB RGB (CMYK CMYK) {
		float C = CMYK.C, M = CMYK.M, Y = CMYK.Y, K = CMYK.K;
		return new RGB( //
			(1 - C) * (1 - K), //
			(1 - M) * (1 - K), //
			(1 - Y) * (1 - K));
	}

	static public RGB RGB (HSI HSI) {
		float H = HSI.H * degRad, S = HSI.S, I = HSI.I;
		float r, g, b;
		if (S < EPSILON) // Gray.
			r = g = b = I;
		else if (H >= 0 && H < 2 * PI / 3) {
			b = I * (1 - S);
			r = I * (1 + S * (float)Math.cos(H) / (float)Math.cos(PI / 3 - H));
			g = 3 * I - r - b;
		} else if (H >= 2 * PI / 3 && H < 4 * PI / 3) {
			H = H - 2 * PI / 3;
			r = I * (1 - S);
			g = I * (1 + S * (float)Math.cos(H) / (float)Math.cos(PI / 3 - H));
			b = 3 * I - r - g;
		} else {
			H = H - 4 * PI / 3;
			g = I * (1 - S);
			b = I * (1 + S * (float)Math.cos(H) / (float)Math.cos(PI / 3 - H));
			r = 3 * I - g - b;
		}
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	static public RGB RGB (HSL HSL) {
		float H = HSL.H / 360, S = HSL.S, L = HSL.L;
		float r = 0, g = 0, b = 0;
		if (S < EPSILON) // Gray.
			r = g = b = L;
		else {
			float v2 = L < 0.5f ? L * (1 + S) : L + S - L * S, v1 = 2 * L - v2;
			r = HSLUtil.hueToRGB(v1, v2, H + 1 / 3f);
			g = HSLUtil.hueToRGB(v1, v2, H);
			b = HSLUtil.hueToRGB(v1, v2, H - 1 / 3f);
		}
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	static public RGB RGB (LinearRGB LinearRGB) {
		return new RGB(sRGB(clamp(LinearRGB.r)), sRGB(clamp(LinearRGB.g)), sRGB(clamp(LinearRGB.b)));
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	static public RGB RGB (HCT HCT) {
		return RGB(HCT, CAM16.VC.sRGB);
	}

	static public RGB RGB (HCT HCT, CAM16.VC vc) {
		float T = HCT.T, C = HCT.C, h = HCT.h * degRad;
		if (T < 0.0001f) return new RGB(0, 0, 0); // Black.
		if (T > 99.9999f) return new RGB(1, 1, 1); // White.
		if (C < 0.0001f) { // Gray.
			float gray = sRGB(LabUtil.LstarToYn(T));
			return new RGB(gray, gray, gray);
		}
		float Y = LabUtil.LstarToY(T);
		RGB rgb = HCTUtil.findRGB(h, C, Y, vc);
		return rgb != null ? rgb : HCTUtil.bisectToLimit(Y, h);
	}

	/** @return NaN if invalid. */
	static public RGB RGB (HSLuv HSLuv) {
		float H = HSLuv.H, S = HSLuv.S, L = HSLuv.L;
		if (L > 100 - EPSILON) return new RGB(1, 1, 1);
		if (L < EPSILON) return new RGB(0, 0, 0);
		return RGB(Luv(new LCHuv(L, HSLuvUtil.maxChromaForLH(L, H) * S / 100, H)));
	}

	static public RGB RGB (HSV HSV) {
		float hue = HSV.H, saturation = HSV.S, value = HSV.V;
		if (Float.isNaN(hue) || saturation < EPSILON) return new RGB(value, value, value);
		float f = hue / 60 - (float)Math.floor(hue / 60);
		float p = value * (1 - saturation);
		float q = value * (1 - f * saturation);
		float t = value * (1 - (1 - f) * saturation);
		float r, g, b;
		switch ((int)Math.floor(hue / 60) % 6) {
		case 0 -> {
			r = value;
			g = t;
			b = p;
		}
		case 1 -> {
			r = q;
			g = value;
			b = p;
		}
		case 2 -> {
			r = p;
			g = value;
			b = t;
		}
		case 3 -> {
			r = p;
			g = q;
			b = value;
		}
		case 4 -> {
			r = t;
			g = p;
			b = value;
		}
		default -> {
			r = value;
			g = p;
			b = q;
		}
		}
		return new RGB(r, g, b);
	}

	static public RGB RGB (HunterLab lab) {
		return RGB(XYZ(lab));
	}

	static public RGB RGB (IHS IHS) {
		float I = IHS.I, H = IHS.H, S = IHS.S;
		float r, g, b;
		if (H >= 0 && H <= 1) {
			r = I * (1 + 2 * S - 3 * S * H) / 3;
			g = I * (1 - S + 3 * S * H) / 3;
			b = I * (1 - S) / 3;
		} else if (H >= 1 && H <= 2) {
			r = I * (1 - S) / 3;
			g = I * (1 + 2 * S - 3 * S * (H - 1)) / 3;
			b = I * (1 - S + 3 * S * (H - 1)) / 3;
		} else {
			r = I * (1 - S + 3 * S * (H - 2)) / 3;
			g = I * (1 - S) / 3;
			b = I * (1 + 2 * S - 3 * S * (H - 2)) / 3;
		}
		return new RGB(r, g, b);
	}

	static public RGB RGB (ITP ITP) {
		float I = ITP.I, Ct = ITP.Ct, Cp = ITP.Cp;
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

	/** Uses {@link Illuminant.CIE2#D65}. */
	static public RGB RGB (Lab Lab) {
		return RGB(XYZ(Lab, Illuminant.CIE2.D65));
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public RGB RGB (Lab Lab, XYZ tristimulus) {
		return RGB(XYZ(Lab, tristimulus));
	}

	/** Uses {@link Illuminant.CIE2#D65}. */
	static public RGB RGB (LCh LCh) {
		return RGB(Lab(LCh), Illuminant.CIE2.D65);
	}

	/** Uses the LMS CIECAM02 transformation matrix. */
	static public RGB RGB (LMS LMS) {
		return RGB(LMS, CAT.CAT02);
	}

	static public RGB RGB (LMS LMS, CAT matrix) {
		return RGB(XYZ(LMS, matrix));
	}

	/** @return NaN if invalid. */
	static public RGB RGB (Luv luv) {
		return RGB(luv, Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}.
	 * @return NaN if invalid. */
	static public RGB RGB (Luv luv, XYZ tristimulus) {
		return RGB(XYZ(luv, tristimulus));
	}

	static public RGB RGB (Oklab Oklab) {
		float L = Oklab.L, a = Oklab.a, b = Oklab.b;
		float l = L + 0.3963377774f * a + 0.2158037573f * b;
		float m = L - 0.1055613458f * a - 0.0638541728f * b;
		float s = L - 0.0894841775f * a - 1.2914855480f * b;
		l *= l * l;
		m *= m * m;
		s *= s * s;
		return new RGB( //
			sRGB(clamp(4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s)), //
			sRGB(clamp(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s)), //
			sRGB(clamp(-0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s)));
	}

	static public RGB RGB (Oklch Oklch) {
		return RGB(Oklab(Oklch));
	}

	static public RGB RGB (Okhsl Okhsl) {
		float l = Okhsl.l, s = Okhsl.s, L = OkhsvUtil.toeInv(l), h = Okhsl.h * degRad;
		if (l >= 1 - EPSILON) return new RGB(1, 1, 1); // White.
		if (l <= EPSILON) return new RGB(0, 0, 0); // Black.
		if (s < EPSILON) return RGB(new Oklab(L, 0, 0)); // Gray.
		float a_ = (float)Math.cos(h), b_ = (float)Math.sin(h);
		float[] Cs = OkhsvUtil.Cs(L, a_, b_);
		float C_0 = Cs[0], C_mid = Cs[1], C_max = Cs[2], C;
		if (s < 0.8f) {
			float t = 1.25f * s, k_1 = 0.8f * C_0, k_2 = (1 - k_1 / C_mid);
			C = t * k_1 / (1 - k_2 * t);
		} else {
			float t = 5 * (s - 0.8f);
			float k_0 = C_mid, k_1 = 0.2f * C_mid * C_mid * 1.25f * 1.25f / C_0, k_2 = 1 - (k_1) / (C_max - C_mid);
			C = k_0 + t * k_1 / (1 - k_2 * t);
		}
		return RGB(new Oklab(L, C * a_, C * b_));
	}

	static public RGB RGB (Okhsv Okhsv) {
		float v = Okhsv.v, s = Okhsv.s, h = Okhsv.h * degRad;
		if (v < EPSILON) return new RGB(0, 0, 0); // Black.
		if (s < EPSILON) return RGB(new Oklab(v, 0, 0)); // Gray.
		float a_ = (float)Math.cos(h), b_ = (float)Math.sin(h);
		float[] ST_max = OkhsvUtil.cuspST(a_, b_);
		float T_max = ST_max[1], S_0 = 0.5f, k = 1 - S_0 / ST_max[0];
		float L_v = 1 - s * S_0 / (S_0 + T_max - T_max * k * s);
		float C_v = s * T_max * S_0 / (S_0 + T_max - T_max * k * s);
		float L = v * L_v, C = v * C_v;
		float L_vt = OkhsvUtil.toeInv(L_v);
		float C_vt = C_v * L_vt / L_v;
		float L_new = OkhsvUtil.toeInv(L);
		C *= L_new / L;
		var l_r = LinearRGB(new Oklab(L_vt, a_ * C_vt, b_ * C_vt));
		float scale = (float)Math.cbrt(1 / Math.max(0, max(l_r.r, l_r.g, l_r.b)));
		C *= scale;
		return RGB(new Oklab(L_new * scale, C * a_, C * b_));
	}

	static public RGB RGB (rg rg, float luminance) {
		return new RGB(clamp(rg.r * luminance), clamp(rg.g * luminance), clamp(rg.b * luminance));
	}

	static public RGB RGB (TSL TSL) {
		float L = TSL.L, S = TSL.S, T = TSL.T;
		if (L < EPSILON) return new RGB(0, 0, 0); // Black.
		if (S < EPSILON) return new RGB(L, L, L); // Gray.
		float a = T * 360 * degRad, r1, g1;
		if (Math.abs(T) < EPSILON) {
			r1 = g1 = (float)Math.sqrt(5 * S * S / 18);
			if (Float.floatToIntBits(T) == 0x80000000) r1 = g1 = -r1; // -0f preserves the sign.
		} else {
			float tan = (float)Math.tan(a), x = (1 - 2 * tan) / (1 + tan);
			g1 = (float)Math.sqrt(5 / 9f * S * S / (x * x + 1));
			if (a >= PI) g1 = -g1;
			r1 = x * g1;
		}
		float k = L / (0.185f * r1 + 0.473f * g1 + 1 / 3f), r = k * (r1 + 1 / 3f), g = k * (g1 + 1 / 3f);
		return new RGB(clamp(r), clamp(g), clamp(k - r - g));
	}

	/** @return Normalized. */
	static public RGB RGB (uv uv) {
		xy xy = xy(uv);
		XYZ XYZ = XYZ(new xyY(xy.x, xy.y, 1));
		RGB rgb = RGB(XYZ);
		float r = rgb.r, g = rgb.g, b = rgb.b;
		float max = max(r, g, b);
		if (max > 0) {
			r /= max;
			g /= max;
			b /= max;
		}
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	/** Uses {@link Gamut#sRGB}.
	 * @return Normalized or NaN if invalid. */
	static public RGB RGB (xy xy) {
		return RGB(xy, Gamut.sRGB);
	}

	/** @return NaN if invalid. */
	static public RGB RGB (xy xy, Gamut gamut) {
		xy = gamut.clamp(xy);
		if (xy.y < EPSILON) return new RGB(Float.NaN, Float.NaN, Float.NaN);
		float X = xy.x / xy.y;
		float Z = (1 - xy.x - xy.y) / xy.y;
		float[][] xyzToRGB = gamut.XYZ_RGB;
		float r = xyzToRGB[0][0] * X + xyzToRGB[0][1] + xyzToRGB[0][2] * Z; // Y=1.
		float g = xyzToRGB[1][0] * X + xyzToRGB[1][1] + xyzToRGB[1][2] * Z;
		float b = xyzToRGB[2][0] * X + xyzToRGB[2][1] + xyzToRGB[2][2] * Z;
		float max = max(r, g, b);
		if (max > 0) {
			r /= max;
			g /= max;
			b /= max;
		}
		return new RGB(sRGB(Math.max(0, r)), sRGB(Math.max(0, g)), sRGB(Math.max(0, b)));
	}

	static public RGB RGB (XYZ XYZ) {
		float X = XYZ.X / 100, Y = XYZ.Y / 100, Z = XYZ.Z / 100;
		return new RGB(sRGB(clamp(3.2404542f * X - 1.5371385f * Y - 0.4985314f * Z)),
			sRGB(clamp(-0.9692660f * X + 1.8760108f * Y + 0.0415560f * Z)),
			sRGB(clamp(0.0556434f * X - 0.2040259f * Y + 1.0572252f * Z)));
	}

	static public RGB RGB (YCbCr YCbCr, YCbCrColorSpace colorSpace) {
		float Y = YCbCr.Y, Cb = YCbCr.Cb, Cr = YCbCr.Cr;
		float r, g, b;
		if (colorSpace == YCbCrColorSpace.ITU_BT_601) {
			r = Y + 0.00000000f * Cb + 1.40200000f * Cr;
			g = Y - 0.34413629f * Cb - 0.71413629f * Cr;
			b = Y + 1.77200000f * Cb + 0.00000000f * Cr;
		} else {
			r = Y - 0.000000295f * Cb + 1.574799932f * Cr;
			g = Y - 0.187324182f * Cb - 0.468124212f * Cr;
			b = Y + 1.855599963f * Cb - 0.000000402f * Cr;
		}
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	static public RGB RGB (YCC YCC) {
		float Y = YCC.Y, C1 = YCC.C1, C2 = YCC.C2;
		float r = 1.402525f * Y + 0.002952f * (C1 - 0.612f) + 1.881096f * (C2 - 0.537f);
		float g = 1.402525f * Y - 0.444393f * (C1 - 0.612f) - 0.956979f * (C2 - 0.537f);
		float b = 1.402525f * Y + 2.291013f * (C1 - 0.612f) + 0.003713f * (C2 - 0.537f);
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	static public RGB RGB (YCoCg YCoCg) {
		float Y = YCoCg.Y, Co = YCoCg.Co, Cg = YCoCg.Cg;
		float r = Y + Co - Cg;
		float g = Y + Cg;
		float b = Y - Co - Cg;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	static public RGB RGB (YES YES) {
		float Y = YES.Y, E = YES.E, S = YES.S;
		float r = Y + E * 1.431f + S * 0.126f;
		float g = Y + E * -0.569f + S * 0.126f;
		float b = Y + E * 0.431f + S * -1.874f;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	static public RGB RGB (YIQ YIQ) {
		float Y = YIQ.Y, I = YIQ.I, Q = YIQ.Q;
		float r = 1 * Y + 0.95629572f * I + 0.62102442f * Q;
		float g = 1 * Y - 0.27212210f * I - 0.64738060f * Q;
		float b = 1 * Y - 1.10698902f * I + 1.70461500f * Q;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	static public RGB RGB (YUV YUV) {
		float Y = YUV.Y, U = YUV.U, V = YUV.V;
		float r = Y - 0.00000055f * U + 1.13988360f * V;
		float g = Y - 0.39464236f * U - 0.58062209f * V;
		float b = Y + 2.03206343f * U - 0.00000025f * V;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	static public RGBW RGBW (int rgb) {
		return new RGBW( //
			((rgb & 0xff0000) >>> 24) / 255f, //
			((rgb & 0xff0000) >>> 16) / 255f, //
			((rgb & 0x00ff00) >>> 8) / 255f, //
			((rgb & 0x0000ff)) / 255f);
	}

	/** Convert RGB to RGBW using one calibrated white LED color. Brightness of {@code rgb} paramter is preserved.
	 * @param rgb Target color, including brightness.
	 * @param w White LED color scaled by relative luminance (may exceed 1). Eg: wr *= wlux / rlux */
	static public RGBW RGBW (RGB rgb, RGB w) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		// How much of each channel the white LED can provide.
		float ratioR = r / w.r, ratioG = g / w.g, ratioB = b / w.b;
		// The white level is limited by the channel that needs the least white contribution.
		float W = min(ratioR, ratioG, ratioB);
		W = Math.min(W, 1);
		// Subtract the white contribution from each channel.
		return new RGBW(Math.max(0, r - W * w.r), Math.max(0, g - W * w.g), Math.max(0, b - W * w.b), W);
	}

	/** Convert CCT to RGBW using one calibrated white LED color. Brightness is maximized.
	 * @param CCT [1667..25000K]
	 * @param brightness [0..1]
	 * @param w White LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @return NaN if invalid. */
	static public RGBW RGBW (float CCT, float brightness, RGB w) {
		RGB target = RGB(CCT, 0);
		float W = 1;
		float r = Math.max(0, target.r - W * w.r);
		float g = Math.max(0, target.g - W * w.g);
		float b = Math.max(0, target.b - W * w.b);
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

	/** Convert RGB to RGBWW using two calibrated white LED colors. Brightness of {@code rgb} paramter is preserved.
	 * @param rgb Target color, including brightness.
	 * @param w1 First white LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @param w2 Second white LED color. */
	static public RGBWW RGBWW (RGB rgb, RGB w1, RGB w2) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		// How much of each channel the white LED can provide.
		float ratioR1 = r / w1.r, ratioG1 = g / w1.g, ratioB1 = b / w1.b;
		float ratioR2 = r / w2.r, ratioG2 = g / w2.g, ratioB2 = b / w2.b;
		// The white level is limited by the channel that needs the least white contribution.
		float W1 = min(ratioR1, ratioG1, ratioB1);
		float W2 = min(ratioR2, ratioG2, ratioB2);
		// Subtract the white contribution from each channel.
		if (W1 > W2) {
			r = Math.max(0, r - W1 * w1.r);
			g = Math.max(0, g - W1 * w1.g);
			b = Math.max(0, b - W1 * w1.b);
			return new RGBWW(r, g, b, W1, 0);
		}
		return new RGBWW(Math.max(0, r - W2 * w2.r), Math.max(0, g - W2 * w2.g), Math.max(0, b - W2 * w2.b), 0, W2);
	}

	/** Convert CCT to RGBWW using two calibrated white LED colors. Brightness is maximized.
	 * @param CCT [1667..25000K]
	 * @param brightness [0..1]
	 * @param w1 First white LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @param w2 Second white LED color.
	 * @return NaN if invalid. */
	static public RGBWW RGBWW (float CCT, float brightness, RGB w1, RGB w2) {
		float cct1 = CCT(uv(w1));
		float cct2 = CCT(uv(w2));
		float W1, W2;
		if (Math.abs(cct2 - cct1) < EPSILON) // Both whites have same CCT.
			W1 = W2 = 0.5f;
		else {
			float ratio = clamp((CCT - cct1) / (cct2 - cct1));
			W1 = 1 - ratio;
			W2 = ratio;
		}
		RGB target = RGB(CCT, 0);
		float r = Math.max(0, target.r - (W1 * w1.r + W2 * w2.r));
		float g = Math.max(0, target.g - (W1 * w1.g + W2 * w2.g));
		float b = Math.max(0, target.b - (W1 * w1.b + W2 * w2.b));
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
	static public rg rg (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		float sum = r + g + b;
		if (sum < EPSILON) return new rg(Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
		float rNorm = r / sum, gNorm = g / sum, bNorm = 1 - rNorm - gNorm;
		float rS = rNorm - 0.333f, gS = gNorm - 0.333f;
		float s = (float)Math.sqrt(rS * rS + gS * gS);
		float h = s < EPSILON ? Float.NaN : ((float)Math.atan2(rS, gS) * radDeg + 360) % 360;
		return new rg(rNorm, gNorm, bNorm, s, h);
	}

	static public TSL TSL (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b, sum = r + g + b;
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
	static public uv uv (RGB rgb) {
		xyY xyY = xyY(XYZ(rgb));
		return uv(new xy(xyY.x, xyY.y));
	}

	static public uv uv (uv1960 uv) {
		return new uv(uv.u, 1.5f * uv.v);
	}

	/** @return NaN if invalid. */
	static public uv uv (xy xy) {
		float x = xy.x, y = xy.y;
		float denom = -2 * x + 12 * y + 3;
		if (Math.abs(denom) < EPSILON) return new uv(Float.NaN, Float.NaN);
		return new uv(4 * x / denom, 9 * y / denom);
	}

	/** @param CCT [1667..25000K]
	 * @return NaN if invalid. */
	static public uv uv (float CCT, float Duv) {
		return uv(xy(CCT, Duv));
	}

	/** CIEDE2000 color difference. Compares colors considering lightness, chromaticity, and/or hue.
	 * @param kL Lightness weight.
	 * @param kC Chroma weight.
	 * @param kH Hue weight.
	 * @return <1: imperceptible to the human eye, 1..2 just noticeable difference (JND), 2..10 clearly visible difference, >50
	 *         very different colors. */
	static public float deltaE2000 (Lab lab1, Lab lab2, float kL, float kC, float kH) {
		float L1 = lab1.L(), a1 = lab1.a(), b1 = lab1.b();
		float L2 = lab2.L(), a2 = lab2.a(), b2 = lab2.b();
		float C1 = (float)Math.sqrt(a1 * a1 + b1 * b1), C2 = (float)Math.sqrt(a2 * a2 + b2 * b2); // Chroma.
		float Cab = (C1 + C2) / 2, Cab7 = (float)Math.pow(Cab, 7), G = 0.5f * (1 - (float)Math.sqrt(Cab7 / (Cab7 + 6103515625f)));
		float a1p = (1 + G) * a1, a2p = (1 + G) * a2;
		float C1p = (float)Math.sqrt(a1p * a1p + b1 * b1), C2p = (float)Math.sqrt(a2p * a2p + b2 * b2);
		float h1p = (float)Math.atan2(b1, a1p) * radDeg, h2p = (float)Math.atan2(b2, a2p) * radDeg; // Hue angle.
		if (h1p < 0) h1p += 360;
		if (h2p < 0) h2p += 360;
		float dLp = L2 - L1, dCp = C2p - C1p, dhp = h2p - h1p; // Delta L'C'h'
		if (dhp > 180)
			dhp -= 360;
		else if (dhp < -180) //
			dhp += 360;
		float dHp = 2 * (float)Math.sqrt(C1p * C2p) * (float)Math.sin(dhp * degRad / 2);
		float Lp = (L1 + L2) / 2, Cp = (C1p + C2p) / 2, hp = h1p + h2p; // Average.
		if (Math.abs(h1p - h2p) > 180) hp += hp < 360 ? 360 : -360;
		hp /= 2;
		float hpRad = hp * degRad;
		float T = 1 - 0.17f * (float)Math.cos(hpRad - 30 * degRad) + 0.24f * (float)Math.cos(2 * hpRad)
			+ 0.32f * (float)Math.cos(3 * hpRad + 6 * degRad) - 0.20f * (float)Math.cos(4 * hpRad - 63 * degRad);
		float SL = 1 + 0.015f * (Lp - 50) * (Lp - 50) / (float)Math.sqrt(20 + (Lp - 50) * (Lp - 50));
		float SC = 1 + 0.045f * Cp;
		float SH = 1 + 0.015f * Cp * T;
		float dTheta = 30 * (float)Math.exp(-((hp - 275) / 25) * ((hp - 275) / 25));
		float Cp7 = (float)Math.pow(Cp, 7), RC = 2 * (float)Math.sqrt(Cp7 / (Cp7 + 6103515625f)); // 25^7
		float RT = -RC * (float)Math.sin(2 * dTheta * degRad);
		float dLpKlSl = kL == 0 ? 0 : dLp / (kL * SL);
		float dCpKcSc = kC == 0 ? 0 : dCp / (kC * SC);
		float dHpKhSh = kH == 0 ? 0 : dHp / (kH * SH);
		float deltaE2 = dLpKlSl * dLpKlSl + dCpKcSc * dCpKcSc + dHpKhSh * dHpKhSh + RT * dCpKcSc * dHpKhSh;
		return deltaE2 == 0 ? 0 : (float)Math.sqrt(deltaE2);
	}

	/** {@link #deltaE2000(Lab, Lab, float, float, float)} with 1 for lightness, chroma, and hue. */
	static public float deltaE2000 (Lab lab1, Lab lab2) {
		return deltaE2000(lab1, lab2, 1, 1, 1);
	}

	/** {@link #deltaE2000(Lab, Lab, float, float, float)} with 1 for lightness, chroma, and hue. */
	static public float deltaE2000 (RGB rgb1, RGB rgb2) {
		return deltaE2000(Lab(rgb1), Lab(rgb2), 1, 1, 1);
	}

	/** {@link #deltaE2000(Lab, Lab, float, float, float)} with 1 for lightness, chroma, and hue. */
	static public float deltaE2000 (uv uv1, uv uv2) {
		return deltaE2000(Lab(XYZ(uv1)), Lab(XYZ(uv2)), 1, 1, 1);
	}

	/** Compares perceptual chromaticity. */
	static public float MacAdamSteps (uv color1, uv color2) {
		float du = color1.u - color2.u, dv = color1.v - color2.v;
		return (float)Math.sqrt(du * du + dv * dv) / 0.0011f;
	}

	/** Compares perceptual chromaticity.
	 * @return NaN if invalid. */
	static public float MacAdamSteps (xy color1, xy color2) {
		return MacAdamSteps(uv(color1), uv(color2));
	}

	/** Compares perceptual chromaticity.
	 * @return NaN if invalid. */
	static public float MacAdamSteps (RGB color1, RGB color2) {
		return MacAdamSteps(uv(color1), uv(color2));
	}

	/** @return NaN if invalid. */
	static public float Duv (uv uv) {
		return Duv(xy(uv));
	}

	/** @return NaN if invalid. */
	static public float Duv (xy xy) {
		float CCT = CCT(xy);
		xy xyBB = xy(CCT, 0);
		uv1960 perp = CCTUtil.perpendicular(CCT, xyBB), uvBB = uv1960(xyBB), uv = uv1960(xy);
		return (uv.u - uvBB.u) * perp.u + (uv.v - uvBB.v) * perp.v;
	}

	static public uv1960 uv1960 (uv uv) {
		return new uv1960(uv.u, uv.v / 1.5f);
	}

	/** @return NaN if invalid. */
	static public uv1960 uv1960 (xy xy) {
		float x = xy.x, y = xy.y;
		float denom = -2 * x + 12 * y + 3;
		if (Math.abs(denom) < EPSILON) return new uv1960(Float.NaN, Float.NaN);
		return new uv1960(4 * x / denom, 6 * y / denom);
	}

	/** @param CCT [1667..25000K]
	 * @return NaN if invalid. */
	static public xy xy (float CCT, float Duv) {
		if (CCT < 1667 || CCT > 25000) return new xy(Float.NaN, Float.NaN);
		float x, t2 = CCT * CCT; // Krystek's approximation.
		if (CCT >= 1667 && CCT <= 4000)
			x = -0.2661239f * 1e9f / (t2 * CCT) - 0.2343589f * 1e6f / t2 + 0.8776956f * 1e3f / CCT + 0.179910f;
		else // CCT > 4000 && CCT <= 25000
			x = -3.0258469f * 1e9f / (t2 * CCT) + 2.1070379f * 1e6f / t2 + 0.2226347f * 1e3f / CCT + 0.240390f;
		float y, xx = x * x;
		if (CCT >= 1667 && CCT <= 2222)
			y = -1.1063814f * xx * x - 1.34811020f * xx + 2.18555832f * x - 0.20219683f;
		else if (CCT > 2222 && CCT <= 4000)
			y = -0.9549476f * xx * x - 1.37418593f * xx + 2.09137015f * x - 0.16748867f;
		else // CCT > 4000 && CCT <= 25000
			y = 3.0817580f * xx * x - 5.87338670f * xx + 3.75112997f * x - 0.37001483f;
		if (Duv != 0) {
			xy xyBB = new xy(x, y);
			uv1960 perp = CCTUtil.perpendicular(CCT, xyBB), uvBB = uv1960(xyBB);
			return xy(new uv1960(uvBB.u + perp.u * Duv, uvBB.v + perp.v * Duv));
		}
		return new xy(x, y);
	}

	/** @return NaN if invalid. */
	static public xy xy (uv uv) {
		float u = uv.u, v = uv.v;
		float denom = 6 * u - 16 * v + 12;
		if (Math.abs(denom) < EPSILON) return new xy(Float.NaN, Float.NaN);
		return new xy(9 * u / denom, 4 * v / denom);
	}

	/** @return NaN if invalid. */
	static public xy xy (uv1960 uv) {
		float u = uv.u, v = uv.v;
		float denom = 2 + u - 4 * v;
		if (Math.abs(denom) < EPSILON) return new xy(Float.NaN, Float.NaN);
		return new xy(u * 1.5f / denom, v / denom);
	}

	/** @return NaN if invalid. */
	static public xy xy (XYZ xyz) {
		float sum = xyz.X + xyz.Y + xyz.Z;
		if (sum < EPSILON) return new xy(Float.NaN, Float.NaN);
		return new xy(xyz.X / sum, xyz.Y / sum);
	}

	/** Uses {@link Gamut#sRGB}.
	 * @return NaN if invalid. */
	static public xy xy (RGB rgb) {
		return xy(rgb, Gamut.sRGB);
	}

	/** @return NaN if invalid. */
	static public xy xy (RGB rgb, Gamut gamut) {
		float r = linear(rgb.r), g = linear(rgb.g), b = linear(rgb.b);
		float[][] rgbToXYZ = gamut.RGB_XYZ;
		float X = rgbToXYZ[0][0] * r + rgbToXYZ[0][1] * g + rgbToXYZ[0][2] * b;
		float Y = rgbToXYZ[1][0] * r + rgbToXYZ[1][1] * g + rgbToXYZ[1][2] * b;
		float Z = rgbToXYZ[2][0] * r + rgbToXYZ[2][1] * g + rgbToXYZ[2][2] * b;
		float sum = X + Y + Z;
		if (Math.abs(sum) < EPSILON) return new xy(Float.NaN, Float.NaN);
		return new xy(X / sum, Y / sum);
	}

	/** @return NaN if invalid. */
	static public xyY xyY (XYZ xyz) {
		float sum = xyz.X + xyz.Y + xyz.Z;
		if (sum < EPSILON) return new xyY(Float.NaN, Float.NaN, Float.NaN);
		return new xyY(xyz.X / sum, xyz.Y / sum, xyz.Y);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	static public XYZ XYZ (CAM16 cam16) {
		return XYZ(cam16, CAM16.VC.sRGB);
	}

	static public XYZ XYZ (CAM16 cam16, CAM16.VC vc) { // Based on Copyright 2021 Google LLC (Apache 2.0).
		float J = cam16.J, C = cam16.C, h = cam16.h * degRad;
		float alpha = (C == 0 || J == 0) ? 0 : C / (float)Math.sqrt(J / 100);
		float t = (float)Math.pow(alpha / Math.pow(1.64 - Math.pow(0.29, vc.n), 0.73), 1 / 0.9);
		float ac = vc.Aw * (float)Math.pow(J / 100.0, 1.0 / vc.c / vc.z);
		float p1 = 0.25f * ((float)Math.cos(h + 2.0) + 3.8f) * (50000 / 13f) * vc.Nc * vc.Ncb, p2 = (ac / vc.Nbb);
		float hSin = (float)Math.sin(h), hCos = (float)Math.cos(h);
		float gamma = 23 * (p2 + 0.305f) * t / (23 * p1 + 11 * t * hCos + 108 * t * hSin);
		float a = gamma * hCos, b = gamma * hSin;
		float rA = (460 * p2 + 451 * a + 288 * b) / 1403;
		float gA = (460 * p2 - 891 * a - 261 * b) / 1403;
		float bA = (460 * p2 - 220 * a - 6300 * b) / 1403;
		float rCBase = Math.max(0, (27.13f * Math.abs(rA)) / (400 - Math.abs(rA)));
		float gCBase = Math.max(0, (27.13f * Math.abs(gA)) / (400 - Math.abs(gA)));
		float bCBase = Math.max(0, (27.13f * Math.abs(bA)) / (400 - Math.abs(bA)));
		float rC = Math.signum(rA) * (100 / vc.FL) * (float)Math.pow(rCBase, 1.0 / 0.42);
		float gC = Math.signum(gA) * (100 / vc.FL) * (float)Math.pow(gCBase, 1.0 / 0.42);
		float bC = Math.signum(bA) * (100 / vc.FL) * (float)Math.pow(bCBase, 1.0 / 0.42);
		float rF = rC / vc.rgbD[0], gF = gC / vc.rgbD[1], bF = bC / vc.rgbD[2];
		return new XYZ((rF * 1.8620678f) + (gF * -1.0112547f) + (bF * 0.14918678f),
			(rF * 0.38752654f) + (gF * 0.62144744f) + (bF * -0.00897398f),
			(rF * -0.01584150f) + (gF * -0.03412294f) + (bF * 1.0499644f));
	}

	static public XYZ XYZ (HunterLab lab) {
		float L = lab.L, a = lab.a, b = lab.b;
		float tempY = L / 10;
		float tempX = a / 17.5f * L / 10;
		float tempZ = b / 7 * L / 10;
		float Y = tempY * tempY;
		return new XYZ((tempX + Y) / 1.02f, Y, -(tempZ - Y) / 0.847f);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public XYZ XYZ (Lab lab, XYZ tristimulus) {
		float L = lab.L, a = lab.a, b = lab.b;
		float Y = (L + 16) / 116;
		float X = a / 500 + Y;
		float Z = Y - b / 200;
		float X3 = X * X * X;
		X = X3 > LabUtil.e ? X3 : (116 * X - 16) / LabUtil.k;
		Y = LabUtil.LstarToYn(L);
		float Z3 = Z * Z * Z;
		Z = Z3 > LabUtil.e ? Z3 : (116 * Z - 16) / LabUtil.k;
		return new XYZ(X * tristimulus.X, Y * tristimulus.Y, Z * tristimulus.Z);
	}

	static public XYZ XYZ (LinearRGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		return new XYZ( //
			(0.4124564f * r + 0.3575761f * g + 0.1804375f * b) * 100, //
			(0.2126729f * r + 0.7151522f * g + 0.0721750f * b) * 100, //
			(0.0193339f * r + 0.1191920f * g + 0.9503041f * b) * 100);
	}

	static public XYZ XYZ (LinearRGB rgb, Gamut gamut) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		float[][] rgbToXYZ = gamut.RGB_XYZ;
		float X = rgbToXYZ[0][0] * r + rgbToXYZ[0][1] * g + rgbToXYZ[0][2] * b;
		float Y = rgbToXYZ[1][0] * r + rgbToXYZ[1][1] * g + rgbToXYZ[1][2] * b;
		float Z = rgbToXYZ[2][0] * r + rgbToXYZ[2][1] * g + rgbToXYZ[2][2] * b;
		return new XYZ(X * 100, Y * 100, Z * 100);
	}

	static public XYZ XYZ (LMS lms, CAT matrix) {
		float[] xyz = matrixMultiply(lms.L, lms.M, lms.S, switch (matrix) {
		case HPE -> LMSUtil.HPE_backward;
		case Bradford -> LMSUtil.Bradford_backward;
		case VonKries -> LMSUtil.vonKries_backward;
		case CAT97 -> LMSUtil.CAT97_backward;
		default -> LMSUtil.CAT02_backward;
		});
		return new XYZ(xyz[0], xyz[1], xyz[2]);
	}

	/** Uses {@link Illuminant.CIE2#D65}.
	 * @return NaN if invalid. */
	static public XYZ XYZ (Luv luv) {
		return XYZ(luv, Illuminant.CIE2.D65);
	}

	static public XYZ XYZ (Oklab Oklab) {
		float L = Oklab.L, a = Oklab.a, b = Oklab.b;
		float l = L + 0.3963377774f * a + 0.2158037573f * b;
		float m = L - 0.1055613458f * a - 0.0638541728f * b;
		float s = L - 0.0894841775f * a - 1.2914855480f * b;
		l *= l * l;
		m *= m * m;
		s *= s * s;
		float r = 4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s;
		float g = -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s;
		b = -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s;
		return new XYZ( //
			(0.4124564f * r + 0.3575761f * g + 0.1804375f * b) * 100, // Linear RGB to XYZ, D65.
			(0.2126729f * r + 0.7151522f * g + 0.0721750f * b) * 100, //
			(0.0193339f * r + 0.1191920f * g + 0.9503041f * b) * 100);
	}

	/** @param tristimulus See {@link Illuminant}.
	 * @return NaN if invalid. */
	static public XYZ XYZ (Luv luv, XYZ tristimulus) {
		float L = luv.L, u = luv.u, v = luv.v;
		if (L < EPSILON) return new XYZ(0, 0, 0);
		float Xn = tristimulus.X, Yn = tristimulus.Y, Zn = tristimulus.Z;
		float divisorN = Xn + 15 * Yn + 3 * Zn;
		if (divisorN < EPSILON) return new XYZ(Float.NaN, Float.NaN, Float.NaN);
		float un_prime = 4 * Xn / divisorN;
		float vn_prime = 9 * Yn / divisorN;
		float u_prime = u / (13 * L) + un_prime;
		float v_prime = v / (13 * L) + vn_prime;
		if (v_prime < EPSILON) return new XYZ(Float.NaN, Float.NaN, Float.NaN);
		float Y = LabUtil.LstarToYn(L) * Yn;
		float X = Y * 9 * u_prime / (4 * v_prime);
		float Z = Y * (12 - 3 * u_prime - 20 * v_prime) / (4 * v_prime);
		return new XYZ(X, Y, Z);
	}

	static public XYZ XYZ (RGB rgb) {
		float r = linear(rgb.r), g = linear(rgb.g), b = linear(rgb.b);
		return new XYZ( //
			(0.4124564f * r + 0.3575761f * g + 0.1804375f * b) * 100, //
			(0.2126729f * r + 0.7151522f * g + 0.0721750f * b) * 100, //
			(0.0193339f * r + 0.1191920f * g + 0.9503041f * b) * 100);
	}

	static public XYZ XYZ (RGB rgb, Gamut gamut) {
		float r = linear(rgb.r), g = linear(rgb.g), b = linear(rgb.b);
		float[][] rgbToXYZ = gamut.RGB_XYZ;
		float X = rgbToXYZ[0][0] * r + rgbToXYZ[0][1] * g + rgbToXYZ[0][2] * b;
		float Y = rgbToXYZ[1][0] * r + rgbToXYZ[1][1] * g + rgbToXYZ[1][2] * b;
		float Z = rgbToXYZ[2][0] * r + rgbToXYZ[2][1] * g + rgbToXYZ[2][2] * b;
		return new XYZ(X * 100, Y * 100, Z * 100);
	}

	/** Uses Y=100. */
	static public XYZ XYZ (uv uv) {
		return XYZ(xy(uv));
	}

	/** Uses Y=100. */
	static public XYZ XYZ (xy xy) {
		return XYZ(xy, 100);
	}

	static public XYZ XYZ (xy xy, float Y) {
		return XYZ(new xyY(xy.x(), xy.y(), Y));
	}

	/** @return NaN X and Z if y is 0. */
	static public XYZ XYZ (xyY xyY) {
		if (xyY.y < EPSILON) return new XYZ(Float.NaN, xyY.Y, Float.NaN);
		return new XYZ( //
			xyY.x * xyY.Y / xyY.y, //
			xyY.Y, //
			(1 - xyY.x - xyY.y) * xyY.Y / xyY.y);
	}

	static public YCbCr YCbCr (RGB rgb, YCbCrColorSpace colorSpace) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
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

	static public YCC YCC (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		return new YCC( //
			0.213f * r + 0.419f * g + 0.081f * b, //
			-0.131f * r - 0.256f * g + 0.387f * b + 0.612f, //
			0.373f * r - 0.312f * g - 0.061f * b + 0.537f);
	}

	static public YCoCg YCoCg (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		return new YCoCg( //
			r / 4 + g / 2 + b / 4, //
			r / 2 - b / 2, //
			-r / 4 + g / 2 - b / 4);
	}

	static public YES YES (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		return new YES( //
			r * 0.253f + g * 0.684f + b * 0.063f, //
			r * 0.500f + g * -0.500f, //
			r * 0.250f + g * 0.250f + b * -0.5f);
	}

	static public YIQ YIQ (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		return new YIQ( //
			0.299f * r + 0.587f * g + 0.114f * b, //
			0.595716f * r - 0.274453f * g - 0.321263f * b, //
			0.211456f * r - 0.522591f * g + 0.311135f * b);
	}

	static public YUV YUV (RGB rgb) {
		float r = rgb.r, g = rgb.g, b = rgb.b;
		return new YUV( //
			0.299f * r + 0.587f * g + 0.114f * b, //
			-0.147141f * r - 0.288869f * g + 0.436010f * b, //
			0.614975f * r - 0.514965f * g - 0.100010f * b);
	}

	static public float max (float a, float b, float c) {
		return Math.max(a, Math.max(b, c));
	}

	static public float min (float a, float b, float c) {
		return Math.min(a, Math.min(b, c));
	}

	/** @return [0..1]. */
	static public float clamp (float value) {
		return Math.max(0, Math.min(1, value));
	}

	static public float lerp (float from, float to, float t) {
		return from + (to - from) * t;
	}

	static public float lerpAngle (float from, float to, float t) {
		if (Float.isNaN(from) && Float.isNaN(to)) return 0;
		if (Float.isNaN(from)) return to;
		if (Float.isNaN(to)) return from;
		float diff = to - from;
		if (diff > 180)
			diff -= 360;
		else if (diff < -180) //
			diff += 360;
		float result = from + diff * t;
		if (result < 0)
			result += 360;
		else if (result >= 360) //
			result -= 360;
		return result;
	}

	static public CAM16 lerp (CAM16 a, CAM16 b, float t) {
		return new CAM16(lerp(a.J, b.J, t), lerp(a.C, b.C, t), lerpAngle(a.h, b.h, t), 0, 0, 0);
	}

	static public CAM16UCS lerp (CAM16UCS a, CAM16UCS b, float t) {
		return new CAM16UCS(lerp(a.J, b.J, t), lerp(a.a, b.a, t), lerp(a.b, b.b, t));
	}

	static public HCT lerp (HCT a, HCT b, float t) {
		return new HCT(lerpAngle(a.h, b.h, t), lerp(a.C, b.C, t), lerp(a.T, b.T, t));
	}

	static public HSL lerp (HSL a, HSL b, float t) {
		return new HSL(lerpAngle(a.H, b.H, t), lerp(a.S, b.S, t), lerp(a.L, b.L, t));
	}

	static public HSLuv lerp (HSLuv a, HSLuv b, float t) {
		return new HSLuv(lerpAngle(a.H, b.H, t), lerp(a.S, b.S, t), lerp(a.L, b.L, t));
	}

	static public HSV lerp (HSV a, HSV b, float t) {
		return new HSV(lerpAngle(a.H, b.H, t), lerp(a.S, b.S, t), lerp(a.V, b.V, t));
	}

	static public ITP lerp (ITP a, ITP b, float t) {
		return new ITP(lerp(a.I, b.I, t), lerp(a.Ct, b.Ct, t), lerp(a.Cp, b.Cp, t));
	}

	static public Lab lerp (Lab a, Lab b, float t) {
		return new Lab(lerp(a.L, b.L, t), lerp(a.a, b.a, t), lerp(a.b, b.b, t));
	}

	static public LinearRGB lerp (LinearRGB a, LinearRGB b, float t) {
		return new LinearRGB(lerp(a.r, b.r, t), lerp(a.g, b.g, t), lerp(a.b, b.b, t));
	}

	static public LCh lerp (LCh a, LCh b, float t) {
		return new LCh(lerp(a.L, b.L, t), lerp(a.C, b.C, t), lerpAngle(a.h, b.h, t));
	}

	static public Luv lerp (Luv a, Luv b, float t) {
		float u, v;
		if (Float.isNaN(a.u) && Float.isNaN(b.u))
			u = 0;
		else if (Float.isNaN(a.u))
			u = b.u;
		else if (Float.isNaN(b.u))
			u = a.u;
		else
			u = lerp(a.u, b.u, t);
		if (Float.isNaN(a.v) && Float.isNaN(b.v)) {
			v = 0;
		} else if (Float.isNaN(a.v)) {
			v = b.v;
		} else if (Float.isNaN(b.v)) {
			v = a.v;
		} else {
			v = lerp(a.v, b.v, t);
		}
		return new Luv(lerp(a.L, b.L, t), u, v);
	}

	static public Oklab lerp (Oklab a, Oklab b, float t) {
		return new Oklab(lerp(a.L, b.L, t), lerp(a.a, b.a, t), lerp(a.b, b.b, t));
	}

	static public Oklch lerp (Oklch a, Oklch b, float t) {
		return new Oklch(lerp(a.L, b.L, t), lerp(a.C, b.C, t), lerpAngle(a.h, b.h, t));
	}

	static public Okhsv lerp (Okhsv a, Okhsv b, float t) {
		return new Okhsv(lerpAngle(a.h, b.h, t), lerp(a.s, b.s, t), lerp(a.v, b.v, t));
	}

	static public RGB lerp (RGB a, RGB b, float t) {
		return new RGB(clamp(lerp(a.r, b.r, t)), clamp(lerp(a.g, b.g, t)), clamp(lerp(a.b, b.b, t)));
	}

	static public XYZ lerp (XYZ a, XYZ b, float t) {
		return new XYZ(lerp(a.X, b.X, t), lerp(a.Y, b.Y, t), lerp(a.Z, b.Z, t));
	}

	/** @param linear [0..1]. */
	static public float gammaEncode (float linear, float gamma) {
		if (linear <= 0) return 0;
		if (linear >= 1) return 1;
		return (float)Math.pow(linear, 1 / gamma);
	}

	static public float gammaDecode (float encoded, float gamma) {
		if (encoded <= 0) return 0;
		if (encoded >= 1) return 1;
		return (float)Math.pow(encoded, gamma);
	}

	/** Linear to sRGB gamma correction. */
	static public float sRGB (float linear) {
		if (linear <= 0.0031308f) return 12.92f * linear;
		return (float)(1.055f * Math.pow(linear, 1 / 2.4) - 0.055);
	}

	/** sRGB to linear inverse gamma correction. */
	static public float linear (float srgb) {
		if (srgb <= 0.040449936f) return srgb / 12.92f;
		return (float)Math.pow((srgb + 0.055) / 1.055, 2.4);
	}

	/** @return [0..255] */
	static public int dmx8 (float value) {
		return Math.round(value * 255);
	}

	/** @return [0..65535] */
	static public int dmx16 (float value) {
		return (int)(value * 65535);
	}

	static public float[] floats (Record record) {
		RecordComponent[] components = record.getClass().getRecordComponents();
		float[] values = new float[components.length];
		try {
			for (int i = 0; i < components.length; i++)
				values[i] = (float)components[i].getAccessor().invoke(record);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return values;
	}

	static public String hex (Record record) {
		return hex(floats(record));
	}

	static public String hex (float... values) {
		StringBuilder buffer = new StringBuilder(values.length << 1);
		for (float value : values) {
			String hex = Integer.toHexString(Math.round(value * 255));
			if (hex.length() == 1) buffer.append('0');
			buffer.append(hex);
		}
		return buffer.toString();
	}

	static public String toString (Record record) {
		return toString(floats(record));
	}

	static public String toString (float... values) {
		StringBuilder buffer = new StringBuilder(values.length * 5);
		for (float value : values) {
			buffer.append(value);
			buffer.append(", ");
		}
		buffer.setLength(buffer.length() - 2);
		return buffer.toString();
	}

	static public String toString255 (Record record) {
		return toString255(floats(record));
	}

	static public String toString255 (float... values) {
		StringBuilder buffer = new StringBuilder(values.length * 5);
		for (float value : values) {
			buffer.append(Math.round(value * 255));
			buffer.append(", ");
		}
		buffer.setLength(buffer.length() - 2);
		return buffer.toString();
	}

	public enum YCbCrColorSpace {
		ITU_BT_601, ITU_BT_709_HDTV
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

	/** Tristimulus values [0..100]. */
	static public class Illuminant {
		/** 2-degree Observer (CIE 1931) */
		static public class CIE2 {
			/** Incandescent. */
			static public final XYZ A = new XYZ(109.850f, 100, 35.585f);
			static public final XYZ C = new XYZ(98.074f, 100, 118.232f);
			static public final XYZ D50 = new XYZ(96.422f, 100, 82.521f);
			static public final XYZ D55 = new XYZ(95.682f, 100, 92.149f);
			/** Daylight. */
			static public final XYZ D65 = new XYZ(95.047f, 100, 108.883f);
			static public final XYZ D75 = new XYZ(94.972f, 100, 122.638f);
			/** Fluorescent. */
			static public final XYZ F2 = new XYZ(99.187f, 100, 67.395f);
			static public final XYZ F7 = new XYZ(95.044f, 100, 108.755f);
			static public final XYZ F11 = new XYZ(100.966f, 100, 64.370f);
		}

		/** 10-degree Observer (CIE 1964) */
		static public class CIE10 {
			/** Incandescent. */
			static public final XYZ A = new XYZ(111.144f, 100, 35.200f);
			static public final XYZ C = new XYZ(97.285f, 100, 116.145f);
			static public final XYZ D50 = new XYZ(96.720f, 100, 81.427f);
			static public final XYZ D55 = new XYZ(95.799f, 100, 90.926f);
			/** Daylight. */
			static public final XYZ D65 = new XYZ(94.811f, 100, 107.304f);
			static public final XYZ D75 = new XYZ(94.416f, 100, 120.641f);
			/** Fluorescent. */
			static public final XYZ F2 = new XYZ(103.280f, 100, 69.026f);
			static public final XYZ F7 = new XYZ(95.792f, 100, 107.687f);
			static public final XYZ F11 = new XYZ(103.866f, 100, 65.627f);
		}
	}

	/** Academy Color Encoding System ACES2065-1 archival format (linear, AP0 primaries). */
	public record ACES2065_1 (
		/** Red [0..1]. */
		float r,
		/** Green [0..1]. */
		float g,
		/** Blue [0..1]. */
		float b) {}

	/** Academy Color Encoding System working space for CGI (linear, AP1 primaries). */
	public record ACEScg (
		/** Red [0..1]. */
		float r,
		/** Green [0..1]. */
		float g,
		/** Blue [0..1]. */
		float b) {}

	/** Academy Color Encoding System for color grading (logarithmic, AP1 primaries). */
	public record ACEScc (
		/** Red [0..1]. */
		float r,
		/** Green [0..1]. */
		float g,
		/** Blue [0..1]. */
		float b) {}

	/** Opponent 3 color space. */
	public record C1C2C3 (
		/** Achromatic channel [0..pi/2]. */
		float C1,
		/** Red-cyan opponent [0..pi/2]. */
		float C2,
		/** Yellow-violet opponent [0..pi/2]. */
		float C3) {}

	/** Color Appearance Model 2016. Predicts color appearance under viewing conditions. */
	public record CAM16 (
		/** Lightness [0..100]. */
		float J,
		/** Chroma [0+]. */
		float C,
		/** Hue angle [0..360]. */
		float h,
		/** Brightness [0+]. */
		float Q,
		/** Colorfulness [0+]. */
		float M,
		/** Saturation [0+]. */
		float s) {

		/** {@link CAM16} viewing conditions. */
		public record VC (
			float Aw,
			float Nbb,
			float Ncb,
			float c,
			float Nc,
			float n,
			float[] rgbD,
			float FL,
			float FLRoot,
			float z) {

			/** @param La Adapting luminance in cd/m², typically 20% of white luminance.
			 * @param bgL Background L* value (typically 50).
			 * @param surround Surround factor, typically: 0=dark (0% surround), 1=dim (0-20% surround), 2=average (>20% surround).
			 * @param discounting True when the eye is assumed to be fully adapted. False for most applications (incomplete chromatic
			 *           adaptation). */
			static public VC with (XYZ wp, float La, float bgL, float surround, boolean discounting) {
				// Based on Copyright 2021 Google LLC (Apache 2.0).
				bgL = Math.max(0.1f, bgL); // Avoid non-physical black infinities.
				float rW = (wp.X * 0.401288f) + (wp.Y * 0.650173f) + (wp.Z * -0.051461f); // To cone/RGB responses.
				float gW = (wp.X * -0.250268f) + (wp.Y * 1.204414f) + (wp.Z * 0.045854f);
				float bW = (wp.X * -0.002079f) + (wp.Y * 0.048952f) + (wp.Z * 0.953127f);
				float f = 0.8f + (surround / 10);
				float c = (f >= 0.9f) ? lerp(0.59f, 0.69f, ((f - 0.9f) * 10)) : lerp(0.525f, 0.59f, ((f - 0.8f) * 10));
				float d = clamp(discounting ? 1 : f * (1 - ((1 / 3.6f) * (float)Math.exp((-La - 42) / 92))));
				float[] rgbD = new float[] {d * (100 / rW) + 1 - d, d * (100 / gW) + 1 - d, d * (100 / bW) + 1 - d};
				float k = 1 / (5 * La + 1), k4 = k * k * k * k, k4F = 1 - k4;
				float fl = (k4 * La) + (0.1f * k4F * k4F * (float)Math.cbrt(5 * La));
				float n = LabUtil.LstarToY(bgL) / wp.Y, z = 1.48f + (float)Math.sqrt(n), nbb = 0.725f / (float)Math.pow(n, 0.2);
				float rAF = (float)Math.pow(fl * rgbD[0] * rW / 100, 0.42);
				float gAF = (float)Math.pow(fl * rgbD[1] * gW / 100, 0.42);
				float bAF = (float)Math.pow(fl * rgbD[2] * bW / 100, 0.42);
				float rA = (400 * rAF) / (rAF + 27.13f);
				float gA = (400 * gAF) / (gAF + 27.13f);
				float bA = (400 * bAF) / (bAF + 27.13f);
				float aw = ((2 * rA) + gA + 0.05f * bA) * nbb;
				return new VC(aw, nbb, nbb, c, f, n, rgbD, fl, (float)Math.pow(fl, 0.25), z);
			}

			static public final VC sRGB = VC.with(Illuminant.CIE2.D65, 200 / PI * LabUtil.LstarToYn(50), 50, 2, false);
		}
	}

	/** Uniform Color Space based on CAM16. For color difference calculations. */
	public record CAM16UCS (
		/** Lightness (J*) [0..100]. */
		float J,
		/** Red-green component (a*) [-50..50]. */
		float a,
		/** Yellow-blue component (b*) [-50..50]. */
		float b) {}

	/** Subtractive color model for printing. */
	public record CMYK (
		/** Cyan [0..1]. */
		float C,
		/** Magenta [0..1]. */
		float M,
		/** Yellow [0..1]. */
		float Y,
		/** Key (black) [0..1]. */
		float K) {}

	/** Material color system. {@link CAM16} hue/chroma with {@link Lab} L* tone. */
	public record HCT (
		/** Hue angle [0..360]. */
		float h,
		/** Chroma [0+]. */
		float C,
		/** Tone (L*) [0..100]. */
		float T) {}

	/** Hue, Saturation, Intensity. */
	public record HSI (
		/** Hue [0..360] or NaN if achromatic. */
		float H,
		/** Saturation [0..1]. */
		float S,
		/** Intensity [0..1]. */
		float I) {}

	/** Hue, Saturation, Lightness. Cylindrical RGB. */
	public record HSL (
		/** Hue [0..360] or NaN if achromatic. */
		float H,
		/** Saturation [0..1]. */
		float S,
		/** Lightness [0..1]. */
		float L) {}

	/** Human-friendly {@link HSL}. Perceptually uniform saturation and lightness. */
	public record HSLuv (
		/** Hue [0..360] or NaN if achromatic. */
		float H,
		/** Saturation [0..100]. */
		float S,
		/** Lightness [0..100]. */
		float L) {}

	/** Hue, Saturation, Value. Also known as HSB. */
	public record HSV (
		/** Hue [0..360] or NaN if achromatic. */
		float H,
		/** Saturation [0..1]. */
		float S,
		/** Value/Brightness [0..1]. */
		float V) {}

	/** Predecessor to CIELAB. More perceptually uniform than XYZ. */
	public record HunterLab (
		/** Lightness [0..100]. */
		float L,
		/** Red-green axis [-100..100]. */
		float a,
		/** Yellow-blue axis [-100..100]. */
		float b) {}

	/** Intensity, Hue, Saturation. Alternative to HSI with different hue calculation. */
	public record IHS (
		/** Intensity [0..1]. */
		float I,
		/** Hue [0..3] RGB sector or NaN if achromatic. */
		float H,
		/** Saturation [0..1]. */
		float S) {}

	/** ITU-R BT.2100 for HDR and wide color gamut. Also known as ICtCp. */
	public record ITP (
		/** Intensity [0..1]. */
		float I,
		/** Blue-yellow axis [-0.5..0.5]. */
		float Ct,
		/** Red-green axis [-0.5..0.5]. */
		float Cp) {}

	/** CIELAB perceptually uniform color space. */
	public record Lab (
		/** Lightness (L*) [0..100]. */
		float L,
		/** Red-green axis (a*) [-100..100]. */
		float a,
		/** Yellow-blue axis (b*) [-100..100]. */
		float b) {}

	/** Cylindrical CIELAB. */
	public record LCh (
		/** Lightness (L*) [0..100]. */
		float L,
		/** Chroma (C*) [0+]. */
		float C,
		/** Hue [0..360] or NaN if achromatic. */
		float h) {}

	/** Cylindrical CIELUV. */
	public record LCHuv (
		/** Lightness (L*) [0..100]. */
		float L,
		/** Chroma (C*) [0+]. */
		float C,
		/** Hue [0..360] or NaN if achromatic. */
		float H) {}

	/** RGB without gamma correction. Values are not clamped. */
	public record LinearRGB (
		/** Red [0..1]. */
		float r,
		/** Green [0..1]. */
		float g,
		/** Blue [0..1]. */
		float b) {}

	/** Human cone cell responses. */
	public record LMS (
		/** Long wavelength (red) cone response [0+]. */
		float L,
		/** Medium wavelength (green) cone response [0+]. */
		float M,
		/** Short wavelength (blue) cone response [0+]. */
		float S) {}

	/** CIELUV perceptually uniform color space. */
	public record Luv (
		/** Lightness (L*) [0..100]. */
		float L,
		/** Red-green chromaticity (u*) [-100..100]. */
		float u,
		/** Yellow-blue chromaticity (v*) [-100..100]. */
		float v) {}

	/** Opponent 2 color channels for image processing. */
	public record O1O2 (
		/** Yellow-blue opponent [-1..1]. */
		float O1,
		/** Red-green opponent [-1..1]. */
		float O2) {}

	/** Perceptually uniform color space. Based on CAM16 and IPT. */
	public record Oklab (
		/** Lightness [0..1]. */
		float L,
		/** Red-green axis [-0.5..0.5]. */
		float a,
		/** Yellow-blue axis [-0.5..0.5]. */
		float b) {}

	/** Cylindrical Oklab. */
	public record Oklch (
		/** Lightness [0..1]. */
		float L,
		/** Chroma [0+]. */
		float C,
		/** Hue [0..360] or NaN if achromatic. */
		float h) {}

	/** Oklab-based {@link HSL}. More perceptually uniform than HSL. */
	public record Okhsl (
		/** Hue [0..360] or NaN if achromatic. */
		float h,
		/** Saturation [0..1]. */
		float s,
		/** Lightness [0..1]. */
		float l) {}

	/** Oklab-based {@link HSV}. More perceptually uniform than HSV. */
	public record Okhsv (
		/** Hue [0..360] or NaN if achromatic. */
		float h,
		/** Saturation [0..1]. */
		float s,
		/** Value [0..1]. */
		float v) {}

	/** Standard RGB with sRGB gamma encoding. Values are clamped [0..1], use {@link XYZ} for interchange to preserve wide-gamut
	 * colors. */
	public record RGB (
		/** Red [0..1]. */
		float r,
		/** Green [0..1]. */
		float g,
		/** Blue [0..1]. */
		float b) {}

	/** RGB with 1 white channel for LEDs. */
	public record RGBW (
		/** Red [0..1]. */
		float r,
		/** Green [0..1]. */
		float g,
		/** Blue [0..1]. */
		float b,
		/** White [0..1]. */
		float w) {}

	/** RGB with 2 white channels for LEDs. */
	public record RGBWW (
		/** Red [0..1]. */
		float r,
		/** Green [0..1]. */
		float g,
		/** Blue [0..1]. */
		float b,
		/** White 1 [0..1]. */
		float w1,
		/** White 2 [0..1]. */
		float w2) {}

	/** Normalized red-green color space. */
	public record rg (
		/** Red chromaticity [0..1]. */
		float r,
		/** Green chromaticity [0..1]. */
		float g,
		/** Blue chromaticity [0..1]. */
		float b,
		/** Saturation [0..1]. */
		float s,
		/** Hue [0..360] or NaN if achromatic. */
		float h) {}

	/** Tint, Saturation, Lightness. For skin tone detection and analysis. */
	public record TSL (
		/** Tint [0..1]. */
		float T,
		/** Saturation [0..1]. */
		float S,
		/** Lightness [0..1]. */
		float L) {}

	/** CIE 1976 u'v' chromaticity coordinates. */
	public record uv (
		/** u' chromaticity [0..1]. */
		float u,
		/** v' chromaticity [0..1]. */
		float v) {}

	/** CIE 1960 UCS chromaticity coordinates. */
	public record uv1960 (
		/** u chromaticity [0..1]. */
		float u,
		/** v chromaticity [0..1]. */
		float v) {}

	/** CIE 1931 chromaticity coordinates. */
	public record xy (
		/** x chromaticity [0..1]. */
		float x,
		/** y chromaticity [0..1]. */
		float y) {}

	/** CIE xyY combining chromaticity with luminance. */
	public record xyY (
		/** x chromaticity [0..1]. */
		float x,
		/** y chromaticity [0..1]. */
		float y,
		/** Luminance Y [0+]. */
		float Y) {}

	/** Digital video color encoding. */
	public record YCbCr (
		/** Luma (Y') [0..1]. */
		float Y,
		/** Blue chroma [-0.5..0.5]. */
		float Cb,
		/** Red chroma [-0.5..0.5]. */
		float Cr) {}

	/** Photo YCC for Kodak PhotoCD. */
	public record YCC (
		/** Luma [0..1]. */
		float Y,
		/** Chroma 1 [-0.5..0.5]. */
		float C1,
		/** Chroma 2 [-0.5..0.5]. */
		float C2) {}

	/** Luma with orange and green chroma. Simple reversible transform. */
	public record YCoCg (
		/** Luma [0..1]. */
		float Y,
		/** Orange chroma [-0.5..0.5]. */
		float Co,
		/** Green chroma [-0.5..0.5]. */
		float Cg) {}

	/** Xerox YES color space. */
	public record YES (
		/** Luminance [0..1]. */
		float Y,
		/** Red-green chrominance [-0.5..0.5]. */
		float E,
		/** Yellow-blue chrominance [-0.5..0.5]. */
		float S) {}

	/** NTSC analog TV color encoding. */
	public record YIQ (
		/** Luma (Y') [0..1]. */
		float Y,
		/** In-phase (orange-blue) [-0.5..0.5]. */
		float I,
		/** Quadrature (purple-green) [-0.5..0.5]. */
		float Q) {}

	/** PAL analog TV color encoding. */
	public record YUV (
		/** Luma (Y') [0..1]. */
		float Y,
		/** Blue chrominance [-0.5..0.5]. */
		float U,
		/** Red chrominance [-0.5..0.5]. */
		float V) {}

	/** CIE 1931 tristimulus values. Foundation of colorimetry. */
	public record XYZ (
		/** X tristimulus [0+]. */
		float X,
		/** Y tristimulus (luminance) [0+]. */
		float Y,
		/** Z tristimulus [0+]. */
		float Z) {}
}
