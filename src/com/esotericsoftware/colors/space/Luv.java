
package com.esotericsoftware.colors.space;

import static com.esotericsoftware.colors.Util.*;

import com.esotericsoftware.colors.Illuminant;
import com.esotericsoftware.colors.Illuminant.CIE2;
import com.esotericsoftware.colors.Util;

/** CIELUV perceptually uniform color space. */
public record Luv (
	/** Lightness (L*) [0..100]. */
	float L,
	/** Red-green chromaticity (u*) [-100..100]. */
	float u,
	/** Yellow-blue chromaticity (v*) [-100..100]. */
	float v) {

	public LCHuv LCHuv () {
		float C = (float)Math.sqrt(u * u + v * v);
		float H = C < EPSILON ? Float.NaN : (float)Math.atan2(v, u) * radDeg;
		return new LCHuv(L, C, H < 0 ? H + 360 : H);
	}

	public float get (int index) {
		return switch (index) {
		case 0 -> L;
		case 1 -> u;
		case 2 -> v;
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public Luv set (int index, float value) {
		return switch (index) {
		case 0 -> new Luv(value, u, v);
		case 1 -> new Luv(L, value, v);
		case 2 -> new Luv(L, u, value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	/** @return NaN if invalid. */
	public RGB RGB () {
		return RGB(CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}.
	 * @return NaN if invalid. */
	public RGB RGB (XYZ tristimulus) {
		return XYZ(tristimulus).RGB();
	}

	/** Uses {@link CIE2#D65}.
	 * @return NaN if invalid. */
	public XYZ XYZ () {
		return XYZ(CIE2.D65);
	}

	/** @param tristimulus See {@link Illuminant}.
	 * @return NaN if invalid. */
	public XYZ XYZ (XYZ tristimulus) {
		if (L < EPSILON) return new XYZ(0, 0, 0);
		float Xn = tristimulus.X(), Yn = tristimulus.Y(), Zn = tristimulus.Z();
		float divisorN = Xn + 15 * Yn + 3 * Zn;
		if (divisorN < EPSILON) return new XYZ(Float.NaN, Float.NaN, Float.NaN);
		float un_prime = 4 * Xn / divisorN;
		float vn_prime = 9 * Yn / divisorN;
		float u_prime = u / (13 * L) + un_prime;
		float v_prime = v / (13 * L) + vn_prime;
		if (v_prime < EPSILON) return new XYZ(Float.NaN, Float.NaN, Float.NaN);
		float Y = Lab.LstarToYn(L) * Yn;
		float X = Y * 9 * u_prime / (4 * v_prime);
		float Z = Y * (12 - 3 * u_prime - 20 * v_prime) / (4 * v_prime);
		return new XYZ(X, Y, Z);
	}

	public Luv add (float value) {
		return new Luv(L + value, u + value, v + value);
	}

	public Luv add (int index, float value) {
		return switch (index) {
		case 0 -> new Luv(L + value, u, v);
		case 1 -> new Luv(L, u + value, v);
		case 2 -> new Luv(L, u, v + value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public Luv add (float L, float u, float v) {
		return new Luv(this.L + L, this.u + u, this.v + v);
	}

	public Luv lerp (Luv other, float t) {
		float u, v;
		if (Float.isNaN(this.u) && Float.isNaN(other.u))
			u = 0;
		else if (Float.isNaN(this.u))
			u = other.u;
		else if (Float.isNaN(other.u))
			u = this.u;
		else
			u = Util.lerp(this.u, other.u, t);
		if (Float.isNaN(this.v) && Float.isNaN(other.v))
			v = 0;
		else if (Float.isNaN(this.v))
			v = other.v;
		else if (Float.isNaN(other.v))
			v = this.v;
		else
			v = Util.lerp(this.v, other.v, t);
		return new Luv(Util.lerp(L, other.L, t), u, v);
	}

	public Luv sub (float value) {
		return new Luv(L - value, u - value, v - value);
	}

	public Luv sub (int index, float value) {
		return switch (index) {
		case 0 -> new Luv(L - value, u, v);
		case 1 -> new Luv(L, u - value, v);
		case 2 -> new Luv(L, u, v - value);
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	public Luv sub (float L, float u, float v) {
		return new Luv(this.L - L, this.u - u, this.v - v);
	}

	public float dst (Luv other) {
		float dL = L - other.L, du = u - other.u, dv = v - other.v;
		return (float)Math.sqrt(dL * dL + du * du + dv * dv);
	}

	public float dst2 (Luv other) {
		float dL = L - other.L, du = u - other.u, dv = v - other.v;
		return dL * dL + du * du + dv * dv;
	}

	public Luv withL (float L) {
		return new Luv(L, u, v);
	}
}
