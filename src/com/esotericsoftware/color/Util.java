
package com.esotericsoftware.color;

import java.lang.reflect.RecordComponent;

/** @author Nathan Sweet <misc@n4te.com> */
public class Util {
	static public final float PI = 3.1415927f, radDeg = 180 / PI, degRad = PI / 180;
	static public final float EPSILON = 1e-6f;

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

	/** Calculate angle difference in degrees, handling wraparound. */
	static public float angleDifference (float a1, float a2) {
		float diff = a2 - a1;
		while (diff > 180)
			diff -= 360;
		while (diff < -180)
			diff += 360;
		return diff;
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

	static public float[] matrixMultiply (float row0, float row1, float row2, float[][] matrix) {
		return new float[] { //
			row0 * matrix[0][0] + row1 * matrix[0][1] + row2 * matrix[0][2],
			row0 * matrix[1][0] + row1 * matrix[1][1] + row2 * matrix[1][2],
			row0 * matrix[2][0] + row1 * matrix[2][1] + row2 * matrix[2][2]};
	}

	static public float[] matrixMultiply (float row0, float row1, float row2, float[] matrix) {
		return new float[] { //
			row0 * matrix[0] + row1 * matrix[1] + row2 * matrix[2], //
			row0 * matrix[3] + row1 * matrix[4] + row2 * matrix[5], //
			row0 * matrix[6] + row1 * matrix[7] + row2 * matrix[8]};
	}

	/** Solves Ax = b for x using Cramer's rule for 3x3. */
	static public float[] matrixSolve (float[][] A, float b0, float b1, float b2) {
		float det = A[0][0] * (A[1][1] * A[2][2] - A[2][1] * A[1][2]) //
			- A[0][1] * (A[1][0] * A[2][2] - A[1][2] * A[2][0]) //
			+ A[0][2] * (A[1][0] * A[2][1] - A[1][1] * A[2][0]);
		float det1 = b0 * (A[1][1] * A[2][2] - A[2][1] * A[1][2]) //
			- A[0][1] * (b1 * A[2][2] - A[1][2] * b2) //
			+ A[0][2] * (b1 * A[2][1] - A[1][1] * b2);
		float det2 = A[0][0] * (b1 * A[2][2] - b2 * A[1][2]) //
			- b0 * (A[1][0] * A[2][2] - A[1][2] * A[2][0]) //
			+ A[0][2] * (A[1][0] * b2 - A[2][0] * b1);
		float det3 = A[0][0] * (A[1][1] * b2 - A[2][1] * b1) //
			- A[0][1] * (A[1][0] * b2 - A[2][0] * b1) //
			+ b0 * (A[1][0] * A[2][1] - A[2][0] * A[1][1]);
		return new float[] {det1 / det, det2 / det, det3 / det};
	}

	static public float[][] invert3x3 (float[][] m) {
		float det = m[0][0] * (m[1][1] * m[2][2] - m[2][1] * m[1][2]) //
			- m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) //
			+ m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
		float invdet = 1 / det;
		float[][] inv = new float[3][3];
		inv[0][0] = (m[1][1] * m[2][2] - m[2][1] * m[1][2]) * invdet;
		inv[0][1] = (m[0][2] * m[2][1] - m[0][1] * m[2][2]) * invdet;
		inv[0][2] = (m[0][1] * m[1][2] - m[0][2] * m[1][1]) * invdet;
		inv[1][0] = (m[1][2] * m[2][0] - m[1][0] * m[2][2]) * invdet;
		inv[1][1] = (m[0][0] * m[2][2] - m[0][2] * m[2][0]) * invdet;
		inv[1][2] = (m[1][0] * m[0][2] - m[0][0] * m[1][2]) * invdet;
		inv[2][0] = (m[1][0] * m[2][1] - m[2][0] * m[1][1]) * invdet;
		inv[2][1] = (m[2][0] * m[0][1] - m[0][0] * m[2][1]) * invdet;
		inv[2][2] = (m[0][0] * m[1][1] - m[1][0] * m[0][1]) * invdet;
		return inv;
	}
}
