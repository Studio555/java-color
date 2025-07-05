
package com.esotericsoftware.color.tools;

import static com.esotericsoftware.color.Util.*;

import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.color.Observer;
import com.esotericsoftware.color.Util;
import com.esotericsoftware.color.space.XYZ;

/** @author Nathan Sweet <misc@n4te.com> */
public class CCTRobertsonLUT {
	static public void main (String[] args) throws Exception {
		List<float[]> entries = new ArrayList();

		// 131 entries, 655 length:
		// 1000.0..2000.0: 0.09698486 @ 1014.59644
		// 2000.0..7000.0: 0.10644531 @ 5353.7134
		// 7000.0..20000.0: 1.0742188 @ 19944.176
		// 20000.0..60000.0: 2.0195312 @ 59854.04
		// 60000.0..100000.0: 2.1953125 @ 94087.12
		for (double K = 971.56535f;;) {
			double mired = 1e6f / K;
			float[] entry = entry(K, mired);
			entries.add(entry);
			if (K < 2000)
				K *= Util.lerp(1.03f, 1.037f, (float)K / 2000);
			else if (K < 7000)
				K *= Util.lerp(1.037f, 1.045f, (float)(K - 2000) / (7000 - 2000));
			else if (K < 20000)
				K *= Util.lerp(1.045f, 1.05f, (float)(K - 7000) / (20000 - 7000));
			else if (K < 40000)
				K *= Util.lerp(1.05f, 1.0525f, (float)(K - 20000) / (40000 - 20000));
			else if (K < 60000)
				K *= Util.lerp(1.048f, 1.0432f, (float)(K - 40000) / (60000 - 40000));
			else if (K < 100000)
				K *= Util.lerp(1.0432f, 1.03106657f, (float)(K - 60000) / (80000 - 60000));
			else
				K *= 1.025f;
			K *= 0.996f;
			if (K > 100000) break;
		}

		entries.sort( (a, b) -> Float.compare(a[0], b[0]));

		System.out.println("static private final float[] ROBERTSON_DATA = {");
		System.out.println("// mired, u, v, du_norm, dv_norm");

		float slope0 = -0.24341f;
		float length0 = (float)Math.sqrt(1 + slope0 * slope0);
		float du0 = 1 / length0;
		float dv0 = slope0 / length0;
		System.out.println("0, 0.18006f, 0.26352f, " + du0 + "f, " + dv0 + "f, // infinity K");

		int count = 1;
		for (float[] entry : entries) {
			float mired = entry[0];
			float u = entry[1];
			float v = entry[2];
			float slope = entry[3];
			float length = (float)Math.sqrt(1 + slope * slope);
			float du = 1 / length;
			float dv = slope / length;
			float K = 1e6f / mired;
			System.out.println(mired + "f, " + u + "f, " + v + "f, " + du + "f, " + dv + "f, // " + K + " K");
			count++;
		}
		System.out.println("};");
		System.out.println();
		System.out.println(count + " entries");
	}

	static private float[] entry (double K, double mired) {
		double[] uv = uv1960(K);
		float u = (float)uv[0], v = (float)uv[1];
		double slope = calculateSlope(K);
		return new float[] {(float)mired, u, v, (float)slope};
	}

	static private double calculateSlope (double K) {
		double deltaK;
		if (K >= 1620 && K <= 1700)
			deltaK = 0.001;
		else if (K >= 1550 && K <= 1750)
			deltaK = 0.01;
		else if (K >= 1400 && K <= 1800)
			deltaK = 0.1;
		else if (K >= 1000 && K <= 2000)
			deltaK = 1;
		else if (K >= 50000)
			deltaK = K * 0.00001;
		else if (K >= 20000)
			deltaK = K * 0.0001;
		else
			deltaK = Math.min(10, K * 0.001);
		double slope = calculateSlopeWithStep(K, deltaK, 0);
		if (Math.abs(slope) > 100) {
			deltaK *= 0.01;
			slope = calculateSlopeWithStep(K, deltaK, 0);
		}
		return slope;
	}

	static private double calculateSlopeWithStep (double K, double deltaK, int depth) {
		if (depth > 3) throw new RuntimeException();
		if (K - deltaK < 427) deltaK = K - 427;
		double[] uv1 = uv1960(K - deltaK);
		double[] uv2 = uv1960(K + deltaK);
		double du_dK = (uv2[0] - uv1[0]) / (2 * deltaK);
		double dv_dK = (uv2[1] - uv1[1]) / (2 * deltaK);
		if (Math.abs(dv_dK) < 1e-10) throw new RuntimeException();
		return -du_dK / dv_dK;
	}

	static private double[] uv1960 (double K) {
		if (K < 427) throw new RuntimeException();
		double X = 0, Y = 0, Z = 0;
		for (int i = 0; i < 81; i++) {
			double lambda = (380 + i * 5) * 1e-9; // nm to meters.
			double exponent = XYZ.c2 / (lambda * K);
			double B = exponent > 700 ? 0 : XYZ.c1 / (lambda * lambda * lambda * lambda * lambda * (Math.exp(exponent) - 1));
			X += B * Observer.CIE2.xbar[i];
			Y += B * Observer.CIE2.ybar[i];
			Z += B * Observer.CIE2.zbar[i];
		}
		if (Y > 0) {
			double scale = 100 / Y;
			X *= scale;
			Z *= scale;
		}
		// XYZ to xy
		double sum = X + 100 + Z;
		double x = X / sum, y = 100 / sum;
		// xy to u'v'
		double denom = -2 * x + 12 * y + 3;
		if (Math.abs(denom) < EPSILON) return new double[] {Double.NaN, Double.NaN};
		return new double[] {4 * x / denom, 6 * y / denom};
	}
}
