
package com.esotericsoftware.color;

import com.esotericsoftware.color.space.XYZ;

/** Tristimulus values [0..100]. */
public class Illuminant {
	/** 2-degree Observer (CIE 1931) */
	static public class CIE2 {
		/** Incandescent. */
		static public final XYZ A = new XYZ(109.85f, 100, 35.585f);
		static public final XYZ C = new XYZ(98.074f, 100, 118.232f);
		static public final XYZ D50 = new XYZ(96.422f, 100, 82.521f);
		static public final XYZ D55 = new XYZ(95.682f, 100, 92.149f);
		/** Daylight. */
		static public final XYZ D65 = new XYZ(95.047f, 100, 108.883f);
		static public final XYZ D75 = new XYZ(94.972f, 100, 122.638f);
		/** Equal energy. */
		static public final XYZ E = new XYZ(100, 100, 100);
		/** Fluorescent. */
		static public final XYZ F2 = new XYZ(99.187f, 100, 67.395f);
		static public final XYZ F7 = new XYZ(95.044f, 100, 108.755f);
		static public final XYZ F11 = new XYZ(100.966f, 100, 64.37f);
	}

	/** 10-degree Observer (CIE 1964) */
	static public class CIE10 {
		/** Incandescent. */
		static public final XYZ A = new XYZ(111.144f, 100, 35.2f);
		static public final XYZ C = new XYZ(97.285f, 100, 116.145f);
		static public final XYZ D50 = new XYZ(96.72f, 100, 81.427f);
		static public final XYZ D55 = new XYZ(95.799f, 100, 90.926f);
		/** Daylight. */
		static public final XYZ D65 = new XYZ(94.811f, 100, 107.304f);
		static public final XYZ D75 = new XYZ(94.416f, 100, 120.641f);
		/** Equal energy. */
		static public final XYZ E = new XYZ(100, 100, 100);
		/** Fluorescent. */
		static public final XYZ F2 = new XYZ(103.28f, 100, 69.026f);
		static public final XYZ F7 = new XYZ(95.792f, 100, 107.687f);
		static public final XYZ F11 = new XYZ(103.866f, 100, 65.627f);
	}
}
