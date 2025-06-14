
package com.esotericsoftware.colors;

/** @author Nathan Sweet <misc@n4te.com> */
public class Colors {
	static final float PI = 3.1415927f, radDeg = 180 / PI, degRad = PI / 180;
	static final float k = 903.2963f; // 24389/27
	static final float e = 0.008856452f; // 216/24389
	static private final float[][] HPE_forward = {{0.38971f, 0.68898f, -0.07868f}, {-0.22981f, 1.18340f, 0.04641f},
		{0.00000f, 0.00000f, 1.00000f}};
	static private final float[][] HPE_backward = {{1.91019683f, -1.11212389f, 0.20190796f},
		{0.37095009f, 0.62905426f, -0.00000806f}, {0.00000f, 0.00000f, 1.00000f}};
	static private final float[][] Badford_forward = {{0.8951000f, 0.2664000f, -0.1614000f}, {-0.7502000f, 1.7135000f, 0.0367000f},
		{0.0389000f, -0.0685000f, 1.0296000f}};
	static private final float[][] Bradford_backward = {{0.9869929f, -0.1470543f, 0.1599627f},
		{0.4323053f, 0.5183603f, 0.0492912f}, {-0.0085287f, 0.0400428f, 0.9684867f}};
	static private final float[][] vonKries_forward = {{0.4002f, 0.7076f, -0.0808f}, {-0.2263f, 1.1653f, 0.0457f},
		{0f, 0f, 0.9182f}};
	static private final float[][] vonKries_backward = {{1.86006661f, -1.12948008f, 0.21989830f},
		{0.36122292f, 0.63880431f, -0.00000713f}, {0.00000f, 0.00000f, 1.08908734f}};
	static private final float[][] CAT97_forward = {{0.8562f, 0.3372f, -0.1934f}, {-0.8360f, 1.8327f, 0.0033f},
		{0.0357f, -0.00469f, 1.0112f}};
	static private final float[][] cat97_backward = {{0.9838112f, -0.1805292f, 0.1887508f}, {0.4488317f, 0.4632779f, 0.0843307f},
		{-0.0326513f, 0.0085222f, 0.9826514f}};
	static private final float[][] CAT02_forward = {{0.7328f, 0.4296f, -0.1624f}, {-0.7036f, 1.6975f, 0.0061f},
		{0.0030f, 0.0136f, 0.9834f}};
	static private final float[][] CAT02_backward = {{1.0961238f, -0.2788690f, 0.1827452f}, {0.4543690f, 0.4735332f, 0.0720978f},
		{-0.0096276f, -0.0056980f, 1.0153256f}};

	private Colors () {
	}

	static public C1C2C3 C1C2C3 (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float C1 = (float)Math.atan(r / Math.max(g, b));
		float C2 = (float)Math.atan(g / Math.max(r, b));
		float C3 = (float)Math.atan(b / Math.max(r, g));
		return new C1C2C3(C1, C2, C3);
	}

	static public float CCT (uv uv) {
		return CCT(xy(uv));
	}

	/** @return CCT in Kelvin [1667-25000], or NaN if outside valid range. */
	static public float CCT (xy xy) {
		float x = xy.x(), y = xy.y();
		if (x < 0.25f || x > 0.565f || y < 0.20f || y > 0.45f) return Float.NaN;
		// McCamy's approximation.
		float n = (x - 0.3320f) / (0.1858f - y);
		float CCT = 449 * n * n * n + 3525 * n * n + 6823.3f * n + 5520.33f;
		if (CCT < 1667 || CCT > 25000) return Float.NaN;
		return CCT;
	}

	/** @return CMYK color space. Normalized. */
	static public CMYK CMYK (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float K = 1 - Math.max(r, Math.max(g, b));
		if (1 - K < 1e-10f) return new CMYK(0, 0, 0, K); // Black
		float C = (1 - r - K) / (1 - K);
		float M = (1 - g - K) / (1 - K);
		float Y = (1 - b - K) / (1 - K);
		return new CMYK(C, M, Y, K);
	}

	/** @return Color difference value (0 = identical colors, larger values = more different) */
	static public float deltaE2000 (Lab lab1, Lab lab2) {
		return deltaE2000(lab1, lab2, 1, 1, 1);
	}

	/** @param kL Weight for lightness (default 1)
	 * @param kC Weight for chroma (default 1)
	 * @param kH Weight for hue (default 1)
	 * @return Color difference value (0 = identical colors, larger values = more different) */
	static public float deltaE2000 (Lab lab1, Lab lab2, float kL, float kC, float kH) {
		float L1 = lab1.L(), a1 = lab1.a(), b1 = lab1.b();
		float L2 = lab2.L(), a2 = lab2.a(), b2 = lab2.b();
		float C1 = (float)Math.sqrt(a1 * a1 + b1 * b1); // Chroma.
		float C2 = (float)Math.sqrt(a2 * a2 + b2 * b2);
		float Cab = (C1 + C2) / 2; // Average chroma.
		float Cab7 = (float)Math.pow(Cab, 7);
		float G = 0.5f * (1 - (float)Math.sqrt(Cab7 / (Cab7 + 6103515625f))); // 25^7
		float a1p = (1 + G) * a1;
		float a2p = (1 + G) * a2;
		float C1p = (float)Math.sqrt(a1p * a1p + b1 * b1);
		float C2p = (float)Math.sqrt(a2p * a2p + b2 * b2);
		float h1p = (float)Math.atan2(b1, a1p) * radDeg; // Hue angle.
		float h2p = (float)Math.atan2(b2, a2p) * radDeg;
		if (h1p < 0) h1p += 360;
		if (h2p < 0) h2p += 360;
		float dLp = L2 - L1; // Delta L'C'h'
		float dCp = C2p - C1p;
		float dhp = h2p - h1p;
		if (dhp > 180)
			dhp -= 360;
		else if (dhp < -180) //
			dhp += 360;
		float dHp = 2 * (float)Math.sqrt(C1p * C2p) * (float)Math.sin((dhp * degRad) / 2);
		float Lp = (L1 + L2) / 2; // Average.
		float Cp = (C1p + C2p) / 2;
		float hp;
		if (Math.abs(h1p - h2p) > 180) {
			if (h1p + h2p < 360)
				hp = (h1p + h2p + 360) / 2;
			else
				hp = (h1p + h2p - 360) / 2;
		} else
			hp = (h1p + h2p) / 2;
		float hpRad = hp * degRad;
		float T = 1 - 0.17f * (float)Math.cos(hpRad - 30 * degRad) + 0.24f * (float)Math.cos(2 * hpRad)
			+ 0.32f * (float)Math.cos(3 * hpRad + 6 * degRad) - 0.20f * (float)Math.cos(4 * hpRad - 63 * degRad);
		float SL = 1 + (0.015f * (Lp - 50) * (Lp - 50)) / (float)Math.sqrt(20 + (Lp - 50) * (Lp - 50));
		float SC = 1 + 0.045f * Cp;
		float SH = 1 + 0.015f * Cp * T;
		float dTheta = 30 * (float)Math.exp(-((hp - 275) / 25) * ((hp - 275) / 25));
		float Cp7 = (float)Math.pow(Cp, 7);
		float RC = 2 * (float)Math.sqrt(Cp7 / (Cp7 + 6103515625f)); // 25^7
		float RT = -RC * (float)Math.sin(2 * dTheta * degRad);
		float dLpKlSl = dLp / (kL * SL);
		float dCpKcSc = dCp / (kC * SC);
		float dHpKhSh = dHp / (kH * SH);
		return (float)Math.sqrt(dLpKlSl * dLpKlSl + dCpKcSc * dCpKcSc + dHpKhSh * dHpKhSh + RT * dCpKcSc * dHpKhSh);
	}

	/** Uses the CIE 2-degree D65 tristimulus.
	 * @param rgb1 First RGB color
	 * @param rgb2 Second RGB color
	 * @return Color difference value (0 = identical colors, larger values = more different) */
	static public float deltaE2000 (RGB rgb1, RGB rgb2) {
		return deltaE2000(Lab(rgb1), Lab(rgb2));
	}

	/** Calculates the CIEDE2000 color difference between two RGB colors with custom weights and D65 illuminant.
	 * @param rgb1 First RGB color
	 * @param rgb2 Second RGB color
	 * @param kL Weight for lightness (default 1)
	 * @param kC Weight for chroma (default 1)
	 * @param kH Weight for hue (default 1)
	 * @return Color difference value (0 = identical colors, larger values = more different) */
	static public float deltaE2000 (RGB rgb1, RGB rgb2, float kL, float kC, float kH) {
		return deltaE2000(Lab(rgb1), Lab(rgb2), kL, kC, kH);
	}

	static public float grayscale (RGB rgb) {
		return rgb.r() * 0.2125f + rgb.g() * 0.7154f + rgb.b() * 0.0721f;
	}

	static public HSI HSI (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float I = (r + g + b) / 3;
		float min = Math.min(r, Math.min(g, b));
		float S = I < 1e-10f ? 0 : 1 - min / I, H = 0;
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
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float delta = max - min;
		float H = 0, S = 0, L = (max + min) / 2;
		if (delta < 1e-10f) { // Gray.
			H = 0;
			S = 0;
		} else {
			S = L <= 0.5f ? delta / (max + min) : delta / (2 - max - min);
			float hue;
			if (r == max)
				hue = (g - b) / 6 / delta;
			else if (g == max)
				hue = 1 / 3f + (b - r) / 6 / delta;
			else
				hue = 2 / 3f + (r - g) / 6 / delta;
			if (hue < 0) hue += 1;
			if (hue > 1) hue -= 1;
			H = (int)(hue * 360);
		}
		return new HSL(H, S, L);
	}

	/** Adds (hue + 360) % 360 to represent hue in the range [0..359]. */
	static public HSV HSV (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float delta = max - min;
		float H = 0;
		if (max == min)
			H = 0;
		else if (max == r) {
			H = (g - b) / delta * 60;
			if (H < 0) H += 360;
		} else if (max == g)
			H = ((b - r) / delta + 2) * 60;
		else if (max == b) //
			H = ((r - g) / delta + 4) * 60;
		float S = delta < 1e-10f ? 0 : delta / max;
		return new HSV(H, S, max);
	}

	static public HSLuv HSLuv (RGB rgb) {
		Luv luv = Luv(rgb);
		LCHuv lch = LCHuv(luv);
		float L = lch.L(), C = lch.C(), H = lch.H();
		if (L > 99.9999999f) return new HSLuv(H, 0, 100);
		if (L < 0.00000001f) return new HSLuv(H, 0, 0);
		float maxChroma = Util.HSLuv.maxChromaForLH(L, H);
		float S = maxChroma < 1e-10f ? 0 : Math.min(100, (C / maxChroma) * 100);
		return new HSLuv(H, S, L);
	}

	static public HunterLab HunterLab (RGB rgb) {
		return HunterLab(XYZ(rgb));
	}

	static public HunterLab HunterLab (XYZ xyz) {
		float X = xyz.X(), Y = xyz.Y(), Z = xyz.Z();
		if (Y < 1e-10f) return new HunterLab(0, Float.NaN, Float.NaN);
		float sqrt = (float)Math.sqrt(Y);
		float L = 10 * sqrt;
		float a = 17.5f * ((1.02f * X - Y) / sqrt);
		float b = 7 * ((Y - 0.847f * Z) / sqrt);
		return new HunterLab(L, a, b);
	}

	/** @return IHS color space. Normalized. */
	static public IHS IHS (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float I = r + g + b;
		if (I < 1e-10f) return new IHS(I, Float.NaN, Float.NaN);
		float H;
		if (b == Math.min(Math.min(r, g), b)) {
			float denom = I - 3 * b;
			H = Math.abs(denom) < 1e-10f ? Float.NaN : (g - b) / denom;
		} else if (r == Math.min(Math.min(r, g), b)) {
			float denom = I - 3 * r;
			H = Math.abs(denom) < 1e-10f ? Float.NaN : (b - r) / denom + 1;
		} else {
			float denom = I - 3 * g;
			H = Math.abs(denom) < 1e-10f ? Float.NaN : (r - g) / denom + 2;
		}
		float S;
		if (H >= 0 && H <= 1)
			S = (I - 3 * b) / I;
		else if (H >= 1 && H <= 2)
			S = (I - 3 * r) / I;
		else
			S = (I - 3 * g) / I;
		return new IHS(I, H, S);
	}

	static public Lab Lab (LCh lch) {
		float L = lch.L(), C = lch.C(), h = lch.h();
		float a = C * (float)Math.cos(h * degRad);
		float b = C * (float)Math.sin(h * degRad);
		return new Lab(L, a, b);
	}

	/** Uses the CIE 2-degree D65 tristimulus. */
	static public Lab Lab (RGB rgb) {
		return Lab(rgb, Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public Lab Lab (RGB rgb, XYZ tristimulus) {
		return Lab(XYZ(rgb), tristimulus);
	}

	/** Uses the CIE 2-degree D65 tristimulus. */
	static public Lab Lab (XYZ XYZ) {
		return Lab(XYZ, Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public Lab Lab (XYZ XYZ, XYZ tristimulus) {
		float X = XYZ.X(), Y = XYZ.Y(), Z = XYZ.Z();
		X /= tristimulus.X();
		Y /= tristimulus.Y();
		Z /= tristimulus.Z();
		if (X > 0.008856f)
			X = (float)Math.pow(X, 1 / 3f);
		else
			X = 7.787036f * X + 0.13793103f;
		if (Y > 0.008856f)
			Y = (float)Math.pow(Y, 1 / 3f);
		else
			Y = 7.787036f * Y + 0.13793103f;
		if (Z > 0.008856f)
			Z = (float)Math.pow(Z, 1 / 3f);
		else
			Z = 7.787036f * Z + 0.13793103f;
		float L = 116 * Y - 16;
		float a = 500 * (X - Y);
		float b = 200 * (Y - Z);
		return new Lab(L, a, b);
	}

	static public Luv Luv (RGB rgb) {
		return Luv(XYZ(rgb));
	}

	/** Uses the CIE 2-degree D65 tristimulus. */
	static public Luv Luv (RGB rgb, XYZ tristimulus) {
		return Luv(XYZ(rgb), tristimulus);
	}

	/** Uses the CIE 2-degree D65 tristimulus. */
	static public Luv Luv (XYZ XYZ) {
		return Luv(XYZ, Illuminant.CIE2.D65);
	}

	static public Luv Luv (XYZ XYZ, XYZ tristimulus) {
		float X = XYZ.X(), Y = XYZ.Y(), Z = XYZ.Z();
		float Xn = tristimulus.X(), Yn = tristimulus.Y(), Zn = tristimulus.Z();
		float yr = Y / Yn;
		float L = yr > e ? 116 * (float)Math.cbrt(yr) - 16 : k * yr;
		float divisor = X + 15 * Y + 3 * Z;
		float divisorN = Xn + 15 * Yn + 3 * Zn;
		if (divisor < 1e-10f || divisorN < 1e-10f) return new Luv(L, Float.NaN, Float.NaN);
		float u_prime = 4 * X / divisor;
		float v_prime = 9 * Y / divisor;
		float un_prime = 4 * Xn / divisorN;
		float vn_prime = 9 * Yn / divisorN;
		float u = 13 * L * (u_prime - un_prime);
		float v = 13 * L * (v_prime - vn_prime);
		return new Luv(L, u, v);
	}

	static public LCHuv LCHuv (Luv luv) {
		float L = luv.L(), u = luv.u(), v = luv.v();
		float C = (float)Math.sqrt(u * u + v * v);
		float H = (float)Math.atan2(v, u) * radDeg;
		if (H < 0) H += 360;
		return new LCHuv(L, C, H);
	}

	static public Luv Luv (LCHuv lch) {
		float L = lch.L(), C = lch.C(), H = lch.H();
		float rad = H * degRad;
		float u = C * (float)Math.cos(rad);
		float v = C * (float)Math.sin(rad);
		return new Luv(L, u, v);
	}

	static public LCh LCh (Lab Lab) {
		float L = Lab.L(), a = Lab.a(), b = Lab.b();
		float h = (float)Math.atan2(b, a) * radDeg;
		if (h < 0) h += 360;
		float C = (float)Math.sqrt(a * a + b * b);
		return new LCh(L, C, h);
	}

	/** Uses the CIE 2-degree D65 tristimulus. */
	static public LCh LCh (RGB rgb) {
		return LCh(rgb, Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public LCh LCh (RGB rgb, XYZ tristimulus) {
		return LCh(Lab(rgb, tristimulus));
	}

	static public LinearRGB LinearRGB (XYZ xyz) {
		float X = xyz.X() / 100, Y = xyz.Y() / 100, Z = xyz.Z() / 100;
		float r = 3.2404542f * X - 1.5371385f * Y - 0.4985314f * Z;
		float g = -0.9692660f * X + 1.8760108f * Y + 0.0415560f * Z;
		float b = 0.0556434f * X - 0.2040259f * Y + 1.0572252f * Z;
		return new LinearRGB(r, g, b);
	}

	static public LinearRGB LinearRGB (Oklab Oklab) {
		float L = Oklab.L(), a = Oklab.a(), b = Oklab.b();
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
		float[] array = {XYZ.X(), XYZ.Y(), XYZ.Z()};
		float[] lms = switch (matrix) {
		case HPE -> Util.matrixMultiply(array, HPE_forward);
		case Bradford -> Util.matrixMultiply(array, Badford_forward);
		case VonKries -> Util.matrixMultiply(array, vonKries_forward);
		case CAT97 -> Util.matrixMultiply(array, CAT97_forward);
		default -> Util.matrixMultiply(array, CAT02_forward);
		};
		return new LMS(lms[0], lms[1], lms[2]);
	}

	/** O1O2 version 2. */
	static public O1O2 O1O2 (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float O1 = (r - g) / 2;
		float O2 = (r + g) / 4 - b / 2;
		return new O1O2(O1, O2);
	}

	static public Oklab Oklab (Oklch Oklch) {
		float L = Oklch.L(), C = Oklch.C(), h = Oklch.h() * degRad;
		float a = C * (float)Math.cos(h);
		float b = C * (float)Math.sin(h);
		return new Oklab(L, a, b);
	}

	static public Oklab Oklab (RGB rgb) {
		float r = linear(rgb.r()), g = linear(rgb.g()), b = linear(rgb.b());
		// Convert to Oklab via XYZ.
		float l = (float)Math.cbrt(0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b);
		float m = (float)Math.cbrt(0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b);
		float s = (float)Math.cbrt(0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b);
		float L = 0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s;
		float a = 1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s;
		float bLab = 0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s;
		return new Oklab(L, a, bLab);
	}

	static public Oklab lerp (Oklab Oklab1, Oklab Oklab2, float t) {
		float L = (1 - t) * Oklab1.L() + t * Oklab2.L();
		float a = (1 - t) * Oklab1.a() + t * Oklab2.a();
		float b = (1 - t) * Oklab1.b() + t * Oklab2.b();
		return new Oklab(L, a, b);
	}

	static public Oklch Oklch (Oklab Oklab) {
		float L = Oklab.L(), a = Oklab.a(), b = Oklab.b();
		float C = (float)Math.sqrt(a * a + b * b);
		float h = (float)Math.atan2(b, a) * radDeg;
		if (h < 0) h += 360;
		return new Oklch(L, C, h);
	}

	static public Oklch Oklch (RGB rgb) {
		return Oklch(Oklab(rgb));
	}

	static public Okhsl Okhsl (RGB rgb) {
		Oklab lab = Oklab(rgb);
		float L = lab.L();
		if (L >= 0.9999999f) return new Okhsl(0, 0, 1); // White.
		if (L <= 0.0000001f) return new Okhsl(0, 0, 0); // Black.
		float C = (float)Math.sqrt(lab.a() * lab.a() + lab.b() * lab.b());
		if (C < 1e-10f) return new Okhsl(0, 0, Util.Okhsv.toe(L)); // Gray.
		float h = 0.5f + 0.5f * (float)Math.atan2(-lab.b(), -lab.a()) / PI;
		float a_ = lab.a() / C, b_ = lab.b() / C;
		float[] Cs = Util.Okhsv.Cs(L, a_, b_);
		float C_0 = Cs[0], C_mid = Cs[1], C_max = Cs[2];
		float mid = 0.8f, s;
		if (C < C_mid) {
			float k_1 = mid * C_0;
			float k_2 = (1.f - k_1 / C_mid);
			float t = C / (k_1 + k_2 * C);
			s = t * mid;
		} else {
			float mid_inv = 1.25f;
			float k_0 = C_mid;
			float k_1 = (1.f - mid) * C_mid * C_mid * mid_inv * mid_inv / C_0;
			float k_2 = (1.f - (k_1) / (C_max - C_mid));
			float t = (C - k_0) / (k_1 + k_2 * (C - k_0));
			s = mid + (1.f - mid) * t;
		}
		float l = Util.Okhsv.toe(L);
		return new Okhsl(h * 360, s, l);
	}

	static public Okhsv Okhsv (RGB rgb) {
		Oklab lab = Oklab(rgb);
		float L = lab.L();
		if (L >= 0.9999999f) return new Okhsv(0, 0, 1); // White.
		if (L <= 0.0000001f) return new Okhsv(0, 0, 0); // Black.
		float C = (float)Math.sqrt(lab.a() * lab.a() + lab.b() * lab.b());
		if (C < 1e-10f) return new Okhsv(0, 0, L); // Gray.
		float h = (float)Math.atan2(lab.b(), lab.a()) * radDeg;
		if (h < 0) h += 360;
		float a_ = lab.a() / C, b_ = lab.b() / C;
		float[] ST_max = Util.Okhsv.cuspST(a_, b_);
		float S_max = ST_max[0], T_max = ST_max[1], S_0 = 0.5f;
		float k = 1 - S_0 / S_max;
		float t = T_max / (C + L * T_max);
		float L_v = t * L, C_v = t * C;
		float L_vt = Util.Okhsv.toeInv(L_v);
		float C_vt = C_v * L_vt / L_v;
		var l_r = LinearRGB(new Oklab(L_vt, a_ * C_vt, b_ * C_vt));
		float scale_L = (float)Math.cbrt(1.f / Math.max(Math.max(l_r.r(), l_r.g()), Math.max(l_r.b(), 0.f)));
		L = L / scale_L;
		C = C / scale_L;
		C = C * Util.Okhsv.toe(L) / L;
		L = Util.Okhsv.toe(L);
		float v = L / L_v;
		float s = (S_0 + T_max) * C_v / ((T_max * S_0) + T_max * k * C_v);
		return new Okhsv(h, clamp(s), clamp(v));
	}

	static public RGB RGB (CMYK cmyk) {
		float C = cmyk.C(), M = cmyk.M(), Y = cmyk.Y(), K = cmyk.K();
		return new RGB((1 - C) * (1 - K), (1 - M) * (1 - K), (1 - Y) * (1 - K));
	}

	/** CCT for Y=33. */
	static public RGB RGB (float CCT, float Duv) {
		return RGB(CCT, Duv, 33);
	}

	/** @param Y > 33 clips R. */
	static public RGB RGB (float CCT, float Duv, float Y) {
		xy xy;
		if (Math.abs(Duv) < 1e-10f)
			xy = xy(CCT);
		else
			xy = xy(uv1960(CCT, Duv));
		XYZ XYZ = XYZ(new xyY(xy.x(), xy.y(), Y));
		RGB rgb = RGB(XYZ);
		float max = Math.max(rgb.r(), Math.max(rgb.g(), rgb.b()));
		if (max > 1) rgb = new RGB(rgb.r() / max, rgb.g() / max, rgb.b() / max);
		return new RGB(Math.max(0, rgb.r()), Math.max(0, rgb.g()), Math.max(0, rgb.b()));
	}

	static public RGB RGB (HSI HSI) {
		float H = HSI.H() * degRad, S = HSI.S(), I = HSI.I();
		float r, g, b;
		if (S < 1e-10f) // Gray.
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
		float hue = HSL.H(), saturation = HSL.S(), luminance = HSL.L();
		float r = 0, g = 0, b = 0;
		if (saturation < 1e-10f) // Gray.
			r = g = b = luminance;
		else {
			float v1, v2;
			float h = hue / 360;
			v2 = luminance < 0.5f ? luminance * (1 + saturation) : luminance + saturation - luminance * saturation;
			v1 = 2 * luminance - v2;
			r = hueToRGB(v1, v2, h + 1 / 3f);
			g = hueToRGB(v1, v2, h);
			b = hueToRGB(v1, v2, h - 1 / 3f);
		}
		return new RGB(r, g, b);
	}

	static private float hueToRGB (float v1, float v2, float vH) {
		if (vH < 0) vH += 1;
		if (vH > 1) vH -= 1;
		if (6 * vH < 1) return v1 + (v2 - v1) * 6 * vH;
		if (2 * vH < 1) return v2;
		if (3 * vH < 2) return v1 + (v2 - v1) * (2 / 3f - vH) * 6;
		return v1;
	}

	/** saturation Saturation. In the range[0..1].<br>
	 * value Value. In the range[0..1].
	 * @return RGB color space. In the range[0..1]. */
	static public RGB RGB (HSV HSV) {
		float hue = HSV.H(), saturation = HSV.S(), value = HSV.V();
		int hi = (int)Math.floor(hue / 60) % 6;
		float f = hue / 60 - (float)Math.floor(hue / 60);
		float p = value * (1 - saturation);
		float q = value * (1 - f * saturation);
		float t = value * (1 - (1 - f) * saturation);
		float r, g, b;
		if (hi == 0) {
			r = value;
			g = t;
			b = p;
		} else if (hi == 1) {
			r = q;
			g = value;
			b = p;
		} else if (hi == 2) {
			r = p;
			g = value;
			b = t;
		} else if (hi == 3) {
			r = p;
			g = q;
			b = value;
		} else if (hi == 4) {
			r = t;
			g = p;
			b = value;
		} else {
			r = value;
			g = p;
			b = q;
		}
		return new RGB(r, g, b);
	}

	static public RGB RGB (HunterLab lab) {
		return RGB(XYZ(lab));
	}

	static public RGB RGB (IHS IHS) {
		float I = IHS.I(), H = IHS.H(), S = IHS.S();
		if (H >= 0 && H <= 1) {
			float r = I * (1 + 2 * S - 3 * S * H) / 3;
			float g = I * (1 - S + 3 * S * H) / 3;
			float b = I * (1 - S) / 3;
			return new RGB(r, g, b);
		}
		if (H >= 1 && H <= 2) {
			float r = I * (1 - S) / 3;
			float g = I * (1 + 2 * S - 3 * S * (H - 1)) / 3;
			float b = I * (1 - S + 3 * S * (H - 1)) / 3;
			return new RGB(r, g, b);
		}
		float r = I * (1 - S + 3 * S * (H - 2)) / 3;
		float g = I * (1 - S) / 3;
		float b = I * (1 + 2 * S - 3 * S * (H - 2)) / 3;
		return new RGB(r, g, b);
	}

	/** Uses the CIE 2-degree D65 tristimulus. */
	static public RGB RGB (Lab Lab) {
		return RGB(Lab, Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public RGB RGB (Lab Lab, XYZ tristimulus) {
		return RGB(XYZ(Lab, tristimulus));
	}

	static public RGB RGB (Luv luv) {
		return RGB(luv, Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public RGB RGB (Luv luv, XYZ tristimulus) {
		return RGB(XYZ(luv, tristimulus));
	}

	static public RGB RGB (HSLuv hsluv) {
		float H = hsluv.H(), S = hsluv.S(), L = hsluv.L();
		if (L > 99.99999f) return new RGB(1, 1, 1);
		if (L < 0.00001f) return new RGB(0, 0, 0);
		float C = Util.HSLuv.maxChromaForLH(L, H) * S / 100;
		return RGB(Luv(new LCHuv(L, C, H)));
	}

	/** Uses the CIE 2-degree D65 tristimulus. */
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

	static public RGB RGB (Oklab Oklab) {
		float L = Oklab.L(), a = Oklab.a(), b = Oklab.b();
		float l = L + 0.3963377774f * a + 0.2158037573f * b;
		float m = L - 0.1055613458f * a - 0.0638541728f * b;
		float s = L - 0.0894841775f * a - 1.2914855480f * b;
		l *= l * l;
		m *= m * m;
		s *= s * s;
		return new RGB( //
			sRGB(clamp(+4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s)), //
			sRGB(clamp(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s)), //
			sRGB(clamp(-0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s)));
	}

	static public RGB RGB (Oklch Oklch) {
		return RGB(Oklab(Oklch));
	}

	static public RGB RGB (Okhsl hsl) {
		float l = hsl.l();
		if (l >= 0.9999999f) return new RGB(1, 1, 1); // White.
		if (l <= 0.0000001f) return new RGB(0, 0, 0); // Black.
		float s = hsl.s();
		if (s == 0.0f) return RGB(new Oklab(Util.Okhsv.toeInv(l), 0, 0)); // Gray.
		float h = hsl.h() * degRad;
		float L = Util.Okhsv.toeInv(l);
		float a_ = (float)Math.cos(h), b_ = (float)Math.sin(h);
		float[] Cs = Util.Okhsv.Cs(L, a_, b_);
		float C_0 = Cs[0], C_mid = Cs[1], C_max = Cs[2], C;
		if (s < 0.8f) {
			float t = 1.25f * s;
			float k_1 = 0.8f * C_0;
			float k_2 = (1 - k_1 / C_mid);
			C = t * k_1 / (1 - k_2 * t);
		} else {
			float t = 5 * (s - 0.8f);
			float k_0 = C_mid;
			float k_1 = 0.2f * C_mid * C_mid * 1.25f * 1.25f / C_0;
			float k_2 = 1 - (k_1) / (C_max - C_mid);
			C = k_0 + t * k_1 / (1 - k_2 * t);
		}
		return RGB(new Oklab(L, C * a_, C * b_));
	}

	static public RGB RGB (Okhsv hsv) {
		float v = hsv.v();
		if (v == 0) return new RGB(0, 0, 0); // Black.
		float s = hsv.s();
		if (s == 0) return RGB(new Oklab(v, 0, 0)); // Gray.
		float h = hsv.h() * degRad;
		float a_ = (float)Math.cos(h), b_ = (float)Math.sin(h);
		float[] ST_max = Util.Okhsv.cuspST(a_, b_);
		float S_max = ST_max[0], T_max = ST_max[1], S_0 = 0.5f;
		float k = 1 - S_0 / S_max;
		float L_v = 1 - s * S_0 / (S_0 + T_max - T_max * k * s);
		float C_v = s * T_max * S_0 / (S_0 + T_max - T_max * k * s);
		float L = v * L_v, C = v * C_v;
		float L_vt = Util.Okhsv.toeInv(L_v);
		float C_vt = C_v * L_vt / L_v;
		float L_new = Util.Okhsv.toeInv(L);
		C = C * L_new / L;
		L = L_new;
		var l_r = LinearRGB(new Oklab(L_vt, a_ * C_vt, b_ * C_vt));
		float scale = (float)Math.cbrt(1 / Math.max(Math.max(l_r.r(), l_r.g()), Math.max(l_r.b(), 0)));
		L = L * scale;
		C = C * scale;
		return RGB(new Oklab(L, C * a_, C * b_));
	}

	static public RGB RGB (uv uv) {
		xy xy = xy(uv);
		XYZ xyz = XYZ(new xyY(xy.x(), xy.y(), 100));
		RGB rgb = RGB(xyz);
		float max = Math.max(rgb.r(), Math.max(rgb.g(), rgb.b()));
		if (max > 1) rgb = new RGB(rgb.r() / max, rgb.g() / max, rgb.b() / max);
		return new RGB(Math.max(0, rgb.r()), Math.max(0, rgb.g()), Math.max(0, rgb.b()));
	}

	static public RGB RGB (xy xy, Gamut gamut) {
		xy = gamut.clamp(xy);
		if (xy.y() < 1e-10f) return new RGB(Float.NaN, Float.NaN, Float.NaN);
		float X = xy.x() / xy.y();
		float Y = 1.0f;
		float Z = (1 - xy.x() - xy.y()) / xy.y();
		float[][] xyzToRGB = gamut.XYZ_RGB;
		float r = xyzToRGB[0][0] * X + xyzToRGB[0][1] * Y + xyzToRGB[0][2] * Z;
		float g = xyzToRGB[1][0] * X + xyzToRGB[1][1] * Y + xyzToRGB[1][2] * Z;
		float b = xyzToRGB[2][0] * X + xyzToRGB[2][1] * Y + xyzToRGB[2][2] * Z;
		float max = Math.max(r, Math.max(g, b));
		if (max > 1) {
			r /= max;
			g /= max;
			b /= max;
		}
		r = sRGB(Math.max(0, r));
		g = sRGB(Math.max(0, g));
		b = sRGB(Math.max(0, b));
		return new RGB(r, g, b);
	}

	static public RGB RGB (XYZ XYZ) {
		float X = XYZ.X() / 100, Y = XYZ.Y() / 100, Z = XYZ.Z() / 100;
		float r = 3.2404542f * X - 1.5371385f * Y - 0.4985314f * Z;
		float g = -0.9692660f * X + 1.8760108f * Y + 0.0415560f * Z;
		float b = 0.0556434f * X - 0.2040259f * Y + 1.0572252f * Z;
		r = sRGB(clamp(r));
		g = sRGB(clamp(g));
		b = sRGB(clamp(b));
		return new RGB(r, g, b);
	}

	static public RGB RGB (YCbCr YCbCr, YCbCrColorSpace colorSpace) {
		float Y = YCbCr.Y(), Cb = YCbCr.Cb(), Cr = YCbCr.Cr();
		float r, g, b;
		if (colorSpace == YCbCrColorSpace.ITU_BT_601) {
			r = 1.00000000f * Y + 0.00000000f * Cb + 1.40200000f * Cr;
			g = 1.00000000f * Y - 0.34413629f * Cb - 0.71413629f * Cr;
			b = 1.00000000f * Y + 1.77200000f * Cb + 0.00000000f * Cr;
		} else {
			r = 1.000000f * Y - 0.000000295f * Cb + 1.574799932f * Cr;
			g = 1.000000f * Y - 0.187324182f * Cb - 0.468124212f * Cr;
			b = 1.000000f * Y + 1.855599963f * Cb - 0.000000402f * Cr;
		}
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	static public RGB RGB (YCC YCC) {
		float Y = YCC.Y(), C1 = YCC.C1(), C2 = YCC.C2();
		float r = 1.402525f * Y + 0.002952f * (C1 - 0.612f) + 1.881096f * (C2 - 0.537f);
		float g = 1.402525f * Y - 0.444393f * (C1 - 0.612f) - 0.956979f * (C2 - 0.537f);
		float b = 1.402525f * Y + 2.291013f * (C1 - 0.612f) + 0.003713f * (C2 - 0.537f);
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	/** Y pseudo luminance, or intensity.<br>
	 * Co orange chrominance.<br>
	 * Cg green chrominance. */
	static public RGB RGB (YCoCg YCoCg) {
		float Y = YCoCg.Y(), Co = YCoCg.Co(), Cg = YCoCg.Cg();
		float r = Y + Co - Cg;
		float g = Y + Cg;
		float b = Y - Co - Cg;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	/** Y Luminance component. [0..1]<br>
	 * E Chrominance factor. Difference of red and green channels. [-0.5..0.5]<br>
	 * S Chrominance factor. Difference of yellow and blue. [-0.5..0.5] */
	static public RGB RGB (YES YES) {
		float Y = YES.Y(), E = YES.E(), S = YES.S();
		float r = Y + E * 1.431f + S * 0.126f;
		float g = Y + E * -0.569f + S * 0.126f;
		float b = Y + E * 0.431f + S * -1.874f;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	/** Y in the range [0..1].<br>
	 * I in-phase in the range [-0.5..0.5].<br>
	 * Q quadrature in the range [-0.5..0.5]. */
	static public RGB RGB (YIQ YIQ) {
		float Y = YIQ.Y(), I = YIQ.I(), Q = YIQ.Q();
		float r = clamp(1.00000000f * Y + 0.95629572f * I + 0.62102442f * Q);
		float g = clamp(1.00000000f * Y - 0.27212210f * I - 0.64738060f * Q);
		float b = clamp(1.00000000f * Y - 1.10698902f * I + 1.70461500f * Q);
		return new RGB(r, g, b);
	}

	/** Y in the range [0..1].<br>
	 * U chrominance in the range [-0.5..0.5].<br>
	 * V chrominance in the range [-0.5..0.5]. */
	static public RGB RGB (YUV YUV) {
		float Y = YUV.Y(), U = YUV.U(), V = YUV.V();
		float r = 1.00000000f * Y - 0.00000055f * U + 1.13988360f * V;
		float g = 1.00000000f * Y - 0.39464236f * U - 0.58062209f * V;
		float b = 1.00000000f * Y + 2.03206343f * U - 0.00000025f * V;
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	/** Convert RGB to RGBW using one calibrated white LED color. Brightness of {@code rgb} paramter is preserved.
	 * @param rgb Target color, including brightness.
	 * @param w White LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @return RGBW values [0,1] */
	static public RGBW RGBW (RGB rgb, RGB w) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		// Calculate how much of each channel the white LED can provide.
		float ratioR = r / w.r(), ratioG = g / w.g(), ratioB = b / w.b();
		// The white level is limited by the channel that needs the least white contribution.
		float W = Math.min(ratioR, Math.min(ratioG, ratioB));
		W = Math.min(W, 1);
		// Subtract the white contribution from each channel.
		r -= W * w.r();
		g -= W * w.g();
		b -= W * w.b();
		r = Math.max(0, r);
		g = Math.max(0, g);
		b = Math.max(0, b);
		return new RGBW(r, g, b, W);
	}

	/** Convert CCT to RGBW using one calibrated white LED color. Brightness is maximized.
	 * @param CCT [1667-25000K]
	 * @param brightness [0-1]
	 * @param w White LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @return RGBW values [0,1] */
	static public RGBW RGBW (float CCT, float brightness, RGB w) {
		RGB target = RGB(CCT, 0);
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
		return new RGBW(r, g, b, W);
	}

	/** Convert RGB to RGBWW using two calibrated white LED colors. Brightness of {@code rgb} paramter is preserved.
	 * @param rgb Target color, including brightness.
	 * @param w1 First white LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @param w2 Second white LED color.
	 * @return RGBW in the range [0,1] */
	static public RGBWW RGBWW (RGB rgb, RGB w1, RGB w2) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		// Calculate how much of each channel the white LED can provide.
		float ratioR1 = r / w1.r(), ratioG1 = g / w1.g(), ratioB1 = b / w1.b();
		float ratioR2 = r / w2.r(), ratioG2 = g / w2.g(), ratioB2 = b / w2.b();
		// The white level is limited by the channel that needs the least white contribution.
		float W1 = Math.min(ratioR1, Math.min(ratioG1, ratioB1));
		float W2 = Math.min(ratioR2, Math.min(ratioG2, ratioB2));
		// Subtract the white contribution from each channel.
		if (W1 > W2) {
			r -= W1 * w1.r();
			g -= W1 * w1.g();
			b -= W1 * w1.b();
			r = Math.max(0, r);
			g = Math.max(0, g);
			b = Math.max(0, b);
			return new RGBWW(r, g, b, W1, 0);
		}
		r -= W2 * w2.r();
		g -= W2 * w2.g();
		b -= W2 * w2.b();
		r = Math.max(0, r);
		g = Math.max(0, g);
		b = Math.max(0, b);
		return new RGBWW(r, g, b, 0, W2);
	}

	/** Convert CCT to RGBWW using two calibrated white LED colors. Brightness is maximized.
	 * @param CCT [1667-25000K]
	 * @param brightness [0-1]
	 * @param w1 First white LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @param w2 Second white LED color.
	 * @return RGBWW values [0,1] */
	static public RGBWW RGBWW (float CCT, float brightness, RGB w1, RGB w2) {
		float cct1 = CCT(uv(w1));
		float cct2 = CCT(uv(w2));
		float W1, W2;
		if (Math.abs(cct2 - cct1) < 1e-10f) // Both whites have same CCT.
			W1 = W2 = 0.5f;
		else {
			float ratio = Math.max(0, Math.min(1, (CCT - cct1) / (cct2 - cct1)));
			W1 = 1 - ratio;
			W2 = ratio;
		}
		RGB target = RGB(CCT, 0);
		float r = Math.max(0, target.r() - (W1 * w1.r() + W2 * w2.r()));
		float g = Math.max(0, target.g() - (W1 * w1.g() + W2 * w2.g()));
		float b = Math.max(0, target.b() - (W1 * w1.b() + W2 * w2.b()));
		float total = r + g + b + W1 + W2;
		if (total > brightness) {
			float excess = total - brightness;
			// Reduce RGB proportionally.
			float sum = r + g + b;
			if (sum > 0 && excess <= sum) {
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
		return new RGBWW(r, g, b, W1, W2);
	}

	/** Returns the complementary color (opposite on color wheel). */
	static public RGB complementary (RGB base) {
		HSL hsl = HSL(base);
		float h = hsl.H() + 180;
		if (h >= 360) h -= 360;
		return RGB(new HSL(h, hsl.S(), hsl.L()));
	}

	/** Returns a triadic color scheme (3 colors evenly spaced on color wheel). */
	static public RGB[] triadic (RGB base) {
		HSL hsl = HSL(base);
		float h1 = hsl.H() + 120;
		float h2 = hsl.H() + 240;
		if (h1 >= 360) h1 -= 360;
		if (h2 >= 360) h2 -= 360;
		return new RGB[] {base, RGB(new HSL(h1, hsl.S(), hsl.L())), RGB(new HSL(h2, hsl.S(), hsl.L()))};
	}

	/** Returns an analogous color scheme (colors adjacent on color wheel). */
	static public RGB[] analogous (RGB base, float angle) {
		HSL hsl = HSL(base);
		float h1 = hsl.H() + angle;
		float h2 = hsl.H() - angle;
		if (h1 >= 360) h1 -= 360;
		if (h2 < 0) h2 += 360;
		return new RGB[] {RGB(new HSL(h2, hsl.S(), hsl.L())), base, RGB(new HSL(h1, hsl.S(), hsl.L()))};
	}

	/** Returns a split-complementary color scheme. */
	static public RGB[] splitComplementary (RGB base) {
		HSL hsl = HSL(base);
		float h1 = hsl.H() + 150;
		float h2 = hsl.H() + 210;
		if (h1 >= 360) h1 -= 360;
		if (h2 >= 360) h2 -= 360;
		return new RGB[] {base, RGB(new HSL(h1, hsl.S(), hsl.L())), RGB(new HSL(h2, hsl.S(), hsl.L()))};
	}

	/** Returns the WCAG contrast ratio between foreground and background colors.
	 * @return Contrast ratio, 1:1 to 21:1. */
	static public float contrastRatio (RGB foreground, RGB background) {
		float fgLum = XYZ(foreground).Y() / 100;
		float bgLum = XYZ(background).Y() / 100;
		float L1 = Math.max(fgLum, bgLum);
		float L2 = Math.min(fgLum, bgLum);
		return (L1 + 0.05f) / (L2 + 0.05f);
	}

	/** Returns true if the colors meet the WCAG AA contrast accessibility standard.
	 * @param largeText true for 18pt+ normal or 14pt+ bold text */
	static public boolean WCAG_AA (RGB fg, RGB bg, boolean largeText) {
		return contrastRatio(fg, bg) >= (largeText ? 3 : 4.5f);
	}

	/** Returns true if the colors meet the WCAG AAA contrast accessibility standard.
	 * @param largeText true for 18pt+ normal or 14pt+ bold text */
	static public boolean WCAG_AAA (RGB fg, RGB bg, boolean largeText) {
		return contrastRatio(fg, bg) >= (largeText ? 4.5f : 7);
	}

	/** Rg-Chromaticity space, illumination and pose invariant.
	 * @return Normalized RGChromaticity: RGB, saturation, hue. Range[0..1]. */
	static public RGChromaticity rgChromaticity (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float sum = r + g + b;
		if (sum < 1e-10f) return new RGChromaticity(Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
		float rNorm = r / sum;
		float gNorm = g / sum;
		float bNorm = 1 - rNorm - gNorm;
		float rS = rNorm - 0.333f;
		float gS = gNorm - 0.333f;
		float saturation = (float)Math.sqrt(rS * rS + gS * gS);
		float hue = (float)Math.atan2(rS, gS);
		return new RGChromaticity(rNorm, gNorm, bNorm, saturation, hue);
	}

	static public uv uv (RGB rgb) {
		XYZ xyz = XYZ(rgb);
		xyY xyY = xyY(xyz);
		return uv(new xy(xyY.x(), xyY.y()));
	}

	static public uv uv (uv1960 uv) {
		return new uv(uv.u(), 1.5f * uv.v());
	}

	/** @return NaN if invalid. */
	static public uv uv (xy xy) {
		float x = xy.x(), y = xy.y();
		float denominator = -2 * x + 12 * y + 3;
		if (Math.abs(denominator) < 1e-10f) return new uv(Float.NaN, Float.NaN);
		float u = 4 * x / denominator;
		float v = 9 * y / denominator;
		return new uv(u, v);
	}

	static public float Duv (xy color) {
		uv1960 uv = uv1960(color);
		float cct = CCT(uv(color));
		float delta = Math.min(50, cct * 0.01f);
		xy bb1 = xy(Math.max(1667, cct - delta));
		xy bb2 = xy(Math.min(25000, cct + delta));
		uv1960 uv1 = uv1960(bb1), uv2 = uv1960(bb2);
		float du = uv2.u() - uv1.u(), dv = uv2.v() - uv1.v();
		float length = (float)Math.sqrt(du * du + dv * dv);
		if (length > 0) {
			du /= length;
			dv /= length;
		}
		float perpU = -dv, perpV = du;
		uv1960 uvbb = uv1960(xy(cct));
		du = uv.u() - uvbb.u();
		dv = uv.v() - uvbb.v();
		return du * perpU + dv * perpV;
	}

	static public float MacAdamSteps (xy color1, xy color2) {
		uv1960 uv1 = uv1960(color1), uv2 = uv1960(color2);
		float du = uv1.u() - uv2.u(), dv = uv1.v() - uv2.v();
		return (float)Math.sqrt(du * du + dv * dv) / 0.0011f;
	}

	static public uv1960 uv1960 (float CCT) {
		return uv1960(xy(CCT));
	}

	static public uv1960 uv1960 (float CCT, float Duv) {
		// The isothermal lines in CIE 1960 are approximately perpendicular to the locus.
		// Find the slope of the locus at this CCT using forward difference to get the perpendicular.
		uv1960 uv = uv1960(CCT);
		uv1960 uvNext = uv1960(CCT + 10); // Small temperature change for derivative.
		// Perpendicular vector (rotate 90 degrees).
		float perpU = uv.v() - uvNext.v();
		float perpV = uvNext.u() - uv.u();
		// Normalize the perpendicular vector.
		float length = (float)Math.sqrt(perpU * perpU + perpV * perpV);
		if (length < 1e-10f) return new uv1960(Float.NaN, Float.NaN); // Cannot determine perpendicular.
		perpU /= length;
		perpV /= length;
		return new uv1960(uv.u() + perpU * Duv, uv.v() + perpV * Duv);
	}

	static public uv1960 uv1960 (uv uv) {
		return new uv1960(uv.u(), uv.v() / 1.5f);
	}

	/** @return NaN if invalid. */
	static public uv1960 uv1960 (xy xy) {
		float x = xy.x(), y = xy.y();
		float denominator = -2 * x + 12 * y + 3;
		if (Math.abs(denominator) < 1e-10f) return new uv1960(Float.NaN, Float.NaN);
		float u = 4 * x / denominator;
		float v = 6 * y / denominator;
		return new uv1960(u, v);
	}

	/** @param CCT 1667-25000K
	 * @return xy chromaticity, or NaN if CCT is outside valid range. */
	static public xy xy (float CCT) {
		if (CCT < 1667 || CCT > 25000) return new xy(Float.NaN, Float.NaN);

		float x;
		if (CCT >= 1667 && CCT <= 4000)
			x = -0.2661239f * 1e9f / (CCT * CCT * CCT) - 0.2343589f * 1e6f / (CCT * CCT) + 0.8776956f * 1e3f / CCT + 0.179910f;
		else // CCT > 4000 && CCT <= 25000
			x = -3.0258469f * 1e9f / (CCT * CCT * CCT) + 2.1070379f * 1e6f / (CCT * CCT) + 0.2226347f * 1e3f / CCT + 0.240390f;

		float y;
		if (CCT >= 1667 && CCT <= 2222)
			y = -1.1063814f * x * x * x - 1.34811020f * x * x + 2.18555832f * x - 0.20219683f;
		else if (CCT > 2222 && CCT <= 4000)
			y = -0.9549476f * x * x * x - 1.37418593f * x * x + 2.09137015f * x - 0.16748867f;
		else // CCT > 4000 && CCT <= 25000
			y = 3.0817580f * x * x * x - 5.87338670f * x * x + 3.75112997f * x - 0.37001483f;

		return new xy(x, y);
	}

	static public xy xy (uv uv) {
		float u = uv.u(), v = uv.v();
		float denominator = 6 * u - 16 * v + 12;
		if (Math.abs(denominator) < 1e-10f) return new xy(Float.NaN, Float.NaN);
		float x = 9 * u / denominator;
		float y = 4 * v / denominator;
		return new xy(x, y);
	}

	static public xy xy (uv1960 uv) {
		float u = uv.u(), v = uv.v();
		float denominator = 2 + u - 4 * v;
		if (Math.abs(denominator) < 1e-10f) return new xy(Float.NaN, Float.NaN);
		float D = 6 / denominator;
		float x = u * D / 4;
		float y = v * D / 6;
		return new xy(x, y);
	}

	static public xy xy (XYZ xyz) {
		float sum = xyz.X() + xyz.Y() + xyz.Z();
		if (sum < 1e-10f) return new xy(Float.NaN, Float.NaN);
		return new xy(xyz.X() / sum, xyz.Y() / sum);
	}

	static public xy xy (RGB rgb, Gamut gamut) {
		float r = linear(rgb.r()), g = linear(rgb.g()), b = linear(rgb.b());
		float[][] rgbToXYZ = gamut.RGB_XYZ;
		float X = rgbToXYZ[0][0] * r + rgbToXYZ[0][1] * g + rgbToXYZ[0][2] * b;
		float Y = rgbToXYZ[1][0] * r + rgbToXYZ[1][1] * g + rgbToXYZ[1][2] * b;
		float Z = rgbToXYZ[2][0] * r + rgbToXYZ[2][1] * g + rgbToXYZ[2][2] * b;
		float sum = X + Y + Z;
		if (Math.abs(sum) < 1e-10f) return new xy(Float.NaN, Float.NaN);
		return new xy(X / sum, Y / sum);
	}

	static public xyY xyY (XYZ xyz) {
		float sum = xyz.X() + xyz.Y() + xyz.Z();
		if (sum < 1e-10f) return new xyY(Float.NaN, Float.NaN, Float.NaN);
		return new xyY(xyz.X() / sum, xyz.Y() / sum, xyz.Y());
	}

	static public XYZ XYZ (HunterLab lab) {
		float L = lab.L(), a = lab.a(), b = lab.b();
		float tempY = L / 10;
		float tempX = a / 17.5f * L / 10;
		float tempZ = b / 7 * L / 10;
		float Y = tempY * tempY;
		float X = (tempX + Y) / 1.02f;
		float Z = -(tempZ - Y) / 0.847f;
		return new XYZ(X, Y, Z);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public XYZ XYZ (Lab lab, XYZ tristimulus) {
		float L = lab.L(), a = lab.a(), b = lab.b();
		float Y = (L + 16) / 116;
		float X = a / 500 + Y;
		float Z = Y - b / 200;
		float X3 = X * X * X;
		if (X3 > e)
			X = X3;
		else
			X = (116 * X - 16) / k;
		if (L > 8)
			Y = (float)Math.pow((L + 16) / 116, 3);
		else
			Y = L / k;
		float Z3 = Z * Z * Z;
		if (Z3 > e)
			Z = Z3;
		else
			Z = (116 * Z - 16) / k;
		return new XYZ(X * tristimulus.X(), Y * tristimulus.Y(), Z * tristimulus.Z());
	}

	/** Uses the CIE 2-degree D65 tristimulus. */
	static public XYZ XYZ (Luv luv) {
		return XYZ(luv, Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public XYZ XYZ (Luv luv, XYZ tristimulus) {
		float L = luv.L(), u = luv.u(), v = luv.v();
		float Xn = tristimulus.X(), Yn = tristimulus.Y(), Zn = tristimulus.Z();
		float Y;
		if (L > 8)
			Y = (float)Math.pow((L + 16) / 116, 3) * Yn;
		else
			Y = L / k * Yn;
		if (L < 1e-10f) return new XYZ(0, 0, 0);
		float divisorN = Xn + 15 * Yn + 3 * Zn;
		if (divisorN < 1e-10f) return new XYZ(Float.NaN, Float.NaN, Float.NaN);
		float un_prime = 4 * Xn / divisorN;
		float vn_prime = 9 * Yn / divisorN;
		float u_prime = u / (13 * L) + un_prime;
		float v_prime = v / (13 * L) + vn_prime;
		if (v_prime < 1e-10f) return new XYZ(Float.NaN, Float.NaN, Float.NaN);
		float X = Y * 9 * u_prime / (4 * v_prime);
		float Z = Y * (12 - 3 * u_prime - 20 * v_prime) / (4 * v_prime);
		return new XYZ(X, Y, Z);
	}

	static public XYZ XYZ (LinearRGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		return new XYZ( //
			(0.4124564f * r + 0.3575761f * g + 0.1804375f * b) * 100, //
			(0.2126729f * r + 0.7151522f * g + 0.0721750f * b) * 100, //
			(0.0193339f * r + 0.1191920f * g + 0.9503041f * b) * 100);
	}

	static public XYZ XYZ (LMS lms, CAT matrix) {
		float[] array = {lms.L(), lms.M(), lms.S()};
		float[] xyz = switch (matrix) {
		case HPE -> Util.matrixMultiply(array, HPE_backward);
		case Bradford -> Util.matrixMultiply(array, Bradford_backward);
		case VonKries -> Util.matrixMultiply(array, vonKries_backward);
		case CAT97 -> Util.matrixMultiply(array, cat97_backward);
		default -> Util.matrixMultiply(array, CAT02_backward);
		};
		return new XYZ(xyz[0], xyz[1], xyz[2]);
	}

	static public XYZ XYZ (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		if (r > 0.04045f)
			r = (float)Math.pow((r + 0.055f) / 1.055f, 2.4f);
		else
			r /= 12.92f;
		if (g > 0.04045f)
			g = (float)Math.pow((g + 0.055f) / 1.055f, 2.4f);
		else
			g /= 12.92f;
		if (b > 0.04045f)
			b = (float)Math.pow((b + 0.055f) / 1.055f, 2.4f);
		else
			b /= 12.92f;
		r *= 100;
		g *= 100;
		b *= 100;
		float X = 0.4124564f * r + 0.3575761f * g + 0.1804375f * b;
		float Y = 0.2126729f * r + 0.7151522f * g + 0.0721750f * b;
		float Z = 0.0193339f * r + 0.1191920f * g + 0.9503041f * b;
		return new XYZ(X, Y, Z);
	}

	static public XYZ XYZ (uv uv) {
		return XYZ(xy(uv));
	}

	/** Convert using Y-100. */
	static public XYZ XYZ (xy xy) {
		return XYZ(new xyY(xy.x(), xy.y(), 100));
	}

	static public XYZ XYZ (xyY xyY) {
		if (xyY.y() < 1e-10f) return new XYZ(Float.NaN, xyY.Y(), Float.NaN);
		float X = xyY.x() * xyY.Y() / xyY.y();
		float Z = (1 - xyY.x() - xyY.y()) * xyY.Y() / xyY.y();
		return new XYZ(X, xyY.Y(), Z);
	}

	static public YCbCr YCbCr (RGB rgb, YCbCrColorSpace colorSpace) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
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

	/** @return YCC color space. In the range [0..1]. */
	static public YCC YCC (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float Y = 0.213f * r + 0.419f * g + 0.081f * b;
		float C1 = -0.131f * r - 0.256f * g + 0.387f * b + 0.612f;
		float C2 = 0.373f * r - 0.312f * g - 0.061f * b + 0.537f;
		return new YCC(Y, C1, C2);
	}

	static public YCoCg YCoCg (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float Y = r / 4 + g / 2 + b / 4;
		float Co = r / 2 - b / 2;
		float Cg = -r / 4 + g / 2 - b / 4;
		return new YCoCg(Y, Co, Cg);
	}

	static public YES YES (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float Y = r * 0.253f + g * 0.684f + b * 0.063f;
		float E = r * 0.500f + g * -0.500f;
		float S = r * 0.250f + g * 0.250f + b * -0.5f;
		return new YES(Y, E, S);
	}

	static public YIQ YIQ (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float Y = 0.299f * r + 0.587f * g + 0.114f * b;
		float I = 0.595716f * r - 0.274453f * g - 0.321263f * b;
		float Q = 0.211456f * r - 0.522591f * g + 0.311135f * b;
		return new YIQ(Y, I, Q);
	}

	/** @return Y in the range [0..1].<br>
	 *         U in the range [-0.5..0.5].<br>
	 *         V in the range [-0.5..0.5]. */
	static public YUV YUV (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float Y = 0.299f * r + 0.587f * g + 0.114f * b;
		float U = -0.147141f * r - 0.288869f * g + 0.436010f * b;
		float V = 0.614975f * r - 0.514965f * g - 0.100010f * b;
		return new YUV(Y, U, V);
	}

	/** Clamps a value to the range [0, 1]. */
	static public float clamp (float value) {
		return Math.max(0, Math.min(1, value));
	}

	/** @param linear In the range [0,1]. */
	static public float gammaEncode (float linear, float gamma) {
		if (linear <= 0) return 0;
		if (linear >= 1) return 1;
		return (float)Math.pow(linear, 1 / gamma);
	}

	/** @param encoded In the range [0,1]. */
	static public float gammaDecode (float encoded, float gamma) {
		if (encoded <= 0) return 0;
		if (encoded >= 1) return 1;
		return (float)Math.pow(encoded, gamma);
	}

	/** Linear to sRGB gamma correction. */
	static public float sRGB (float linear) {
		if (linear <= 0.0031308f) return 12.92f * linear;
		return 1.055f * (float)Math.pow(linear, 1 / 2.4f) - 0.055f;
	}

	/** sRGB to linear inverse gamma correction. */
	static public float linear (float srgb) {
		if (srgb <= 0.04045f) return srgb / 12.92f;
		return (float)Math.pow((srgb + 0.055f) / 1.055f, 2.4f);
	}

	/** @param value In the range [0,1].
	 * @return 0-255 */
	static public int dmx8 (float value) {
		return (int)(value * 255);
	}

	/** @param value In the range [0,1].
	 * @return 0-65535 */
	static public int dmx16 (float value) {
		return (int)(value * 65535);
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

	static public String toString255 (float... values) {
		StringBuilder buffer = new StringBuilder(values.length * 5);
		for (float value : values) {
			buffer.append((int)(value * 255));
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

	/** Tristimulus values using the scale 0-100. */
	static public class Illuminant {
		/** 10-degree Observer (CIE 1964) */
		static public class CIE10 {
			static public final XYZ A = new XYZ(111.144f, 100, 35.200f); // Incandescent.
			static public final XYZ C = new XYZ(97.285f, 100, 116.145f);
			static public final XYZ D50 = new XYZ(96.720f, 100, 81.427f);
			static public final XYZ D55 = new XYZ(95.799f, 100, 90.926f);
			static public final XYZ D65 = new XYZ(94.811f, 100, 107.304f); // Daylight.
			static public final XYZ D75 = new XYZ(94.416f, 100, 120.641f);
			static public final XYZ F2 = new XYZ(103.280f, 100, 69.026f); // Fluorescent.
			static public final XYZ F7 = new XYZ(95.792f, 100, 107.687f);
			static public final XYZ F11 = new XYZ(103.866f, 100, 65.627f);
		}

		/** 2-degree Observer (CIE 1931) */
		static public class CIE2 {
			static public final XYZ A = new XYZ(109.850f, 100, 35.585f); // Incandescent.
			static public final XYZ C = new XYZ(98.074f, 100, 118.232f);
			static public final XYZ D50 = new XYZ(96.422f, 100, 82.521f);
			static public final XYZ D55 = new XYZ(95.682f, 100, 92.149f);
			static public final XYZ D65 = new XYZ(95.047f, 100, 108.883f); // Daylight.
			static public final XYZ D75 = new XYZ(94.972f, 100, 122.638f);
			static public final XYZ F2 = new XYZ(99.187f, 100, 67.395f); // Fluorescent.
			static public final XYZ F7 = new XYZ(95.044f, 100, 108.755f);
			static public final XYZ F11 = new XYZ(100.966f, 100, 64.370f);
		}
	}

	/** C1C2C3, 3 channel opponent color space (achromatic/intensity, red-blue, green vs red+blue) */
	public record C1C2C3 (float C1, float C2, float C3) {}

	/** CMYK */
	public record CMYK (float C, float M, float Y, float K) {}

	/** HSI (Hue, Saturation, Intensity) */
	public record HSI (float H, float S, float I) {}

	/** HSL (Hue, Saturation, Lightness) */
	public record HSL (float H, float S, float L) {}

	/** HSLuv (Human-friendly HSL) */
	public record HSLuv (float H, float S, float L) {}

	/** HSV (Hue, Saturation, Value) */
	public record HSV (float H, float S, float V) {}

	/** Hunter Lab */
	public record HunterLab (float L, float a, float b) {}

	/** IHS (Intensity, Hue, Saturation) */
	public record IHS (float I, float H, float S) {}

	/** CIE Lab */
	public record Lab (float L, float a, float b) {}

	/** CIE LCh (cylindrical Lab) */
	public record LCh (float L, float C, float h) {}

	/** CIE Luv */
	public record Luv (float L, float u, float v) {}

	/** CIE LCHuv (cylindrical Luv) */
	public record LCHuv (float L, float C, float H) {}

	/** Linear RGB (no gamma correction) */
	public record LinearRGB (float r, float g, float b) {}

	/** LMS cone response */
	public record LMS (float L, float M, float S) {}

	/** O1O2, 2 channel opponent colors */
	public record O1O2 (float O1, float O2) {}

	/** Oklab perceptual color space */
	public record Oklab (float L, float a, float b) {}

	/** Oklch (cylindrical Oklab) */
	public record Oklch (float L, float C, float h) {}

	/** Okhsl (Oklab-based HSL) */
	public record Okhsl (float h, float s, float l) {}

	/** Okhsv (Oklab-based HSV) */
	public record Okhsv (float h, float s, float v) {}

	/** sRGB */
	public record RGB (float r, float g, float b) {
		public String hex () {
			return Colors.hex(r(), g(), b());
		}

		public String toString255 () {
			return Colors.toString255(r(), g(), b());
		}
	}

	public record RGBW (float r, float g, float b, float w) {
		public String hex () {
			return Colors.hex(r(), g(), b(), w());
		}

		public String toString255 () {
			return Colors.toString255(r(), g(), b(), w());
		}
	}

	public record RGBWW (float r, float g, float b, float w1, float w2) {
		public String hex () {
			return Colors.hex(r(), g(), b(), w1(), w2());
		}

		public String toString255 () {
			return Colors.toString255(r(), g(), b(), w1(), w2());
		}
	}

	/** RG Chromaticity (5 values) */
	public record RGChromaticity (float r, float g, float b, float saturation, float hue) {}

	/** CIE 1976 u'v' */
	public record uv (float u, float v) {}

	/** CIE 1960 uv */
	public record uv1960 (float u, float v) {}

	/** Chromaticity xy coordinates */
	public record xy (float x, float y) {}

	/** CIE xyY */
	public record xyY (float x, float y, float Y) {}

	/** YCbCr */
	public record YCbCr (float Y, float Cb, float Cr) {}

	/** YCC (PhotoYCC) */
	public record YCC (float Y, float C1, float C2) {}

	/** YCoCg */
	public record YCoCg (float Y, float Co, float Cg) {}

	/** YES */
	public record YES (float Y, float E, float S) {}

	/** YIQ */
	public record YIQ (float Y, float I, float Q) {}

	/** YUV */
	public record YUV (float Y, float U, float V) {}

	/** CIE XYZ [0,100] */
	public record XYZ (float X, float Y, float Z) {}
}
