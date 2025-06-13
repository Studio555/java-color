
package com.esotericsoftware.colors;

/** @author Nathan Sweet */
public class Colors {
	static private final float PI = 3.1415927f;
	static private final float radDeg = 180 / PI;
	static private final float degRad = PI / 180;
	static private final float k = 903.2963f; // 24389/27
	static private final float e = 0.008856452f; // 216/24389
	static private final float[][] hpe_forward = {{0.38971f, 0.68898f, -0.07868f}, {-0.22981f, 1.18340f, 0.04641f},
		{0.00000f, 0.00000f, 1.00000f}};
	static private final float[][] hpe_backward = {{1.91020f, -1.11212f, 0.20191f}, {0.37095f, 0.62905f, -0.00001f},
		{0.00000f, 0.00000f, 1.00000f}};
	static private final float[][] bradford_forward = {{0.8951000f, 0.2664000f, -0.1614000f},
		{-0.7502000f, 1.7135000f, 0.0367000f}, {0.0389000f, -0.0685000f, 1.0296000f}};
	static private final float[][] bradford_backward = {{0.9869929f, -0.1470543f, 0.1599627f},
		{0.4323053f, 0.5183603f, 0.0492912f}, {-0.0085287f, 0.0400428f, 0.9684867f}};
	static private final float[][] vonkries_forward = {{0.4002f, 0.7076f, -0.0808f}, {-0.2263f, 1.1653f, 0.0457f},
		{0f, 0f, 0.9182f}};
	static private final float[][] vonkries_backward = {{1.86007f, -1.12948f, 0.21990f}, {0.36122f, 0.63880f, -0.00001f},
		{0.00000f, 0.00000f, 1.08909f}};
	static private final float[][] cat97_forward = {{0.8562f, 0.3372f, -0.1934f}, {-0.8360f, 1.8327f, 0.0033f},
		{0.0357f, -0.00469f, 1.0112f}};
	static private final float[][] cat97_backward = {{0.9838112f, -0.1805292f, 0.1887508f}, {0.4488317f, 0.4632779f, 0.0843307f},
		{-0.0326513f, 0.0085222f, 0.9826514f}};
	static private final float[][] cat02_forward = {{0.7328f, 0.4296f, -0.1624f}, {-0.7036f, 1.6975f, 0.0061f},
		{0.0030f, 0.0136f, 0.9834f}};
	static private final float[][] cat02_backward = {{1.0961238f, -0.2788690f, 0.1827452f}, {0.4543690f, 0.4735332f, 0.0720978f},
		{-0.0096276f, -0.0056980f, 1.0153256f}};

	static private final float[][] HSLuv_XYZ_RGB = {{3.2404542f, -1.5371385f, -0.4985314f}, {-0.9692660f, 1.8760108f, 0.0415560f},
		{0.0556434f, -0.2040259f, 1.0572252f}};

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

	/** Calculate CCT from xy chromaticity using McCamy's approximation.
	 * @return CCT in Kelvin [1667-25000], or NaN if outside valid range. */
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
		float L = lch.L();
		float C = lch.C();
		float H = lch.H();
		if (L > 99.9999999f) return new HSLuv(H, 0, 100);
		if (L < 0.00000001f) return new HSLuv(H, 0, 0);
		float maxChroma = maxChromaForLH(L, H);
		float S = maxChroma < 1e-10f ? 0 : Math.min(100, (C / maxChroma) * 100);
		return new HSLuv(H, S, L);
	}

	static private float maxChromaForLH (float L, float H) {
		float hRad = H * degRad;
		float minChroma = Float.MAX_VALUE;
		for (float[] bound : getBounds(L)) {
			float m1 = bound[0], b1 = bound[1];
			if (Float.isNaN(m1) || Float.isNaN(b1)) continue;
			float x, y;
			if (Math.abs(Math.sin(hRad)) < 1e-10f) { // Hue is 0 or 180 degrees (vertical line).
				x = 0;
				y = b1;
			} else { // Calculate intersection based on hue angle.
				float m2 = -1 / (float)Math.tan(hRad);
				x = intersectLineLine(m1, b1, m2, 0);
				if (Float.isNaN(x)) continue; // Lines are parallel.
				y = m2 * x;
			}
			float chroma = (float)Math.sqrt(x * x + y * y);
			if (chroma >= 0 && chroma < minChroma) minChroma = chroma;
		}
		return minChroma == Float.MAX_VALUE ? 0 : minChroma;
	}

	static private float[][] getBounds (float L) {
		float[][] bounds = new float[6][2];
		float sub1 = (float)Math.pow(L + 16, 3) / 1560896;
		float sub2 = sub1 > e ? sub1 : L / k;
		for (int c = 0, index = 0; c < 3; c++) {
			float m1 = HSLuv_XYZ_RGB[c][0];
			float m2 = HSLuv_XYZ_RGB[c][1];
			float m3 = HSLuv_XYZ_RGB[c][2];
			for (int t = 0; t < 2; t++) {
				float top1 = (284517 * m1 - 94839 * m3) * sub2;
				float top2 = (838422 * m3 + 769860 * m2 + 731718 * m1) * L * sub2 - 769860 * t * L;
				float bottom = (632260 * m3 - 126452 * m2) * sub2 + 126452 * t;
				if (Math.abs(bottom) < 1e-10f) {
					bounds[index][0] = Float.NaN;
					bounds[index][1] = Float.NaN;
				} else {
					bounds[index][0] = top1 / bottom;
					bounds[index][1] = top2 / bottom;
				}
				index++;
			}
		}
		return bounds;
	}

	static private float intersectLineLine (float m1, float b1, float m2, float b2) {
		float denom = m1 - m2;
		if (Math.abs(denom) < 1e-10f) return Float.NaN; // Parallel lines
		return (b2 - b1) / denom;
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

	/** Default: CIE 2-degree D65 Tristimulus. */
	static public Lab Lab (RGB rgb) {
		return Lab(rgb, Illuminant.CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}. */
	static public Lab Lab (RGB rgb, XYZ tristimulus) {
		return Lab(XYZ(rgb), tristimulus);
	}

	/** Default: CIE 2-degree D65 Tristimulus. */
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

	/** Default: CIE 2-degree D65 Tristimulus. */
	static public Luv Luv (RGB rgb, XYZ tristimulus) {
		return Luv(XYZ(rgb), tristimulus);
	}

	/** Default: CIE 2-degree D65 Tristimulus. */
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

	/** Default: CIE 2-degree D65 Tristimulus. */
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

	/** Default: LMS CIECAM02 transformation matrix. */
	static public LMS LMS (RGB rgb) {
		return LMS(rgb, CAT.CAT02);
	}

	static public LMS LMS (RGB rgb, CAT matrix) {
		return LMS(XYZ(rgb), matrix);
	}

	/** Default: LMS CIECAM02 transformation matrix. */
	static public LMS LMS (XYZ XYZ) {
		return LMS(XYZ, CAT.CAT02);
	}

	static public LMS LMS (XYZ XYZ, CAT matrix) {
		float[] array = {XYZ.X(), XYZ.Y(), XYZ.Z()};
		float[] lms = switch (matrix) {
		case HPE -> matrixMultiply(array, hpe_forward);
		case Bradford -> matrixMultiply(array, bradford_forward);
		case VonKries -> matrixMultiply(array, vonkries_forward);
		case CAT97 -> matrixMultiply(array, cat97_forward);
		default -> matrixMultiply(array, cat02_forward);
		};
		return new LMS(lms[0], lms[1], lms[2]);
	}

	static public float[] matrixMultiply (float[] a, float[][] b) {
		float[] result = new float[b[0].length];
		for (int i = 0; i < b[0].length; i++)
			for (int ii = 0; ii < b.length; ii++)
				result[i] += a[ii] * b[ii][i];
		return result;
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

	static public Oklch Oklch (Oklab oklab) {
		float L = oklab.L(), a = oklab.a(), b = oklab.b();
		float C = (float)Math.sqrt(a * a + b * b);
		float h = (float)Math.atan2(b, a) * radDeg;
		if (h < 0) h += 360;
		return new Oklch(L, C, h);
	}

	static public Oklch Oklch (RGB rgb) {
		return Oklch(Oklab(rgb));
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

	/** Default: CIE 2-degree D65 Tristimulus. */
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
		float maxChroma = maxChromaForLH(L, H);
		float C = maxChroma * S / 100;
		return RGB(Luv(new LCHuv(L, C, H)));
	}

	/** Default: CIE 2-degree D65 Tristimulus. */
	static public RGB RGB (LCh LCh) {
		return RGB(Lab(LCh), Illuminant.CIE2.D65);
	}

	/** Default: LMS CIECAM02 transformation matrix. */
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
		float r = +4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s;
		float g = -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s;
		float bLinear = -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s;
		return new RGB(sRGB(clamp(r)), sRGB(clamp(g)), sRGB(clamp(bLinear)));
	}

	static public RGB RGB (Oklch Oklch) {
		Oklab oklab = Oklab(Oklch);
		return RGB(oklab);
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
			r = Y + 0.000f * Cb + 1.403f * Cr;
			g = Y - 0.344f * Cb - 0.714f * Cr;
			b = Y + 1.773f * Cb + 0.000f * Cr;
		} else {
			r = Y + 0.0000f * Cb + 1.5701f * Cr;
			g = Y - 0.1870f * Cb - 0.4664f * Cr;
			b = Y + 1.8556f * Cb + 0.0000f * Cr;
		}
		return new RGB(clamp(r), clamp(g), clamp(b));
	}

	static public RGB RGB (YCC YCC) {
		float Y = YCC.Y(), C1 = YCC.C1(), C2 = YCC.C2();
		float r = 0.981f * Y + 1.315f * (C2 - 0.537f);
		float g = 0.981f * Y - 0.311f * (C1 - 0.612f) - 0.669f * (C2 - 0.537f);
		float b = 0.981f * Y + 1.601f * (C1 - 0.612f);
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
		float r = clamp(Y + 0.956f * I + 0.621f * Q);
		float g = clamp(Y - 0.272f * I - 0.647f * Q);
		float b = clamp(Y - 1.105f * I + 1.702f * Q);
		return new RGB(r, g, b);
	}

	/** Y in the range [0..1].<br>
	 * U chrominance in the range [-0.5..0.5].<br>
	 * V chrominance in the range [-0.5..0.5]. */
	static public RGB RGB (YUV YUV) {
		float Y = YUV.Y(), U = YUV.U(), V = YUV.V();
		float r = Y + 0.000f * U + 1.140f * V;
		float g = Y - 0.396f * U - 0.581f * V;
		float b = Y + 2.029f * U + 0.000f * V;
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

	/** Convert RGB to RGBW using two calibrated white LED colors. Brightness of {@code rgb} paramter is preserved.
	 * @param rgb Target color, including brightness.
	 * @param w1 First white LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @param w2 Second white LED color.
	 * @return RGBW in the range [0,1] */
	static public RGBTW RGBTW (RGB rgb, RGB w1, RGB w2) {
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
			return new RGBTW(r, g, b, W1, 0);
		}
		r -= W2 * w2.r();
		g -= W2 * w2.g();
		b -= W2 * w2.b();
		r = Math.max(0, r);
		g = Math.max(0, g);
		b = Math.max(0, b);
		return new RGBTW(r, g, b, 0, W2);
	}

	/** Convert CCT to RGBTW using two calibrated white LED colors. Brightness is maximized.
	 * @param CCT [1667-25000K]
	 * @param brightness [0-1]
	 * @param w1 First white LED color scaled by relative luminance (may exceed 1). Eg: wr * wlux / rlux
	 * @param w2 Second white LED color.
	 * @return RGBTW values [0,1] */
	static public RGBTW RGBTW (float CCT, float brightness, RGB w1, RGB w2) {
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
		return new RGBTW(r, g, b, W1, W2);
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

	/** Default: CIE 2-degree D65 Tristimulus. */
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
		case HPE -> matrixMultiply(array, hpe_backward);
		case Bradford -> matrixMultiply(array, bradford_backward);
		case VonKries -> matrixMultiply(array, vonkries_backward);
		case CAT97 -> matrixMultiply(array, cat97_backward);
		default -> matrixMultiply(array, cat02_backward);
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
			Cb = -0.169f * r - 0.331f * g + 0.500f * b;
			Cr = 0.500f * r - 0.419f * g - 0.081f * b;
		} else {
			Y = 0.2215f * r + 0.7154f * g + 0.0721f * b;
			Cb = -0.1145f * r - 0.3855f * g + 0.5000f * b;
			Cr = 0.5016f * r - 0.4556f * g - 0.0459f * b;
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
		float I = 0.596f * r - 0.275f * g - 0.322f * b;
		float Q = 0.212f * r - 0.523f * g + 0.311f * b;
		return new YIQ(Y, I, Q);
	}

	/** @return Y in the range [0..1].<br>
	 *         U in the range [-0.5..0.5].<br>
	 *         V in the range [-0.5..0.5]. */
	static public YUV YUV (RGB rgb) {
		float r = rgb.r(), g = rgb.g(), b = rgb.b();
		float Y = 0.299f * r + 0.587f * g + 0.114f * b;
		float U = -0.14713f * r - 0.28886f * g + 0.436f * b;
		float V = 0.615f * r - 0.51499f * g - 0.10001f * b;
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

	public record RGBTW (float r, float g, float b, float t, float w) {
		public String hex () {
			return Colors.hex(r(), g(), b(), t(), w());
		}

		public String toString255 () {
			return Colors.toString255(r(), g(), b(), t(), w());
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
