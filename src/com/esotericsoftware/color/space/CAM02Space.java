
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Color;

public interface CAM02Space extends Color {
	public float J ();

	public float a ();

	public float b ();

	default public float C () {
		return (float)Math.sqrt(a() * a() + b() * b());
	}

	default public float h () {
		float h = (float)Math.atan2(b(), a()) * radDeg;
		return h < 0 ? h + 360 : h;
	}

	default public float get (int index) {
		return switch (index) {
		case 0 -> J();
		case 1 -> a();
		case 2 -> b();
		default -> throw new IndexOutOfBoundsException(index);
		};
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	default public CAM02 CAM02 () {
		return CAM02(CAM02.VC.sRGB);
	}

	public CAM02 CAM02 (CAM02.VC vc);

	/** Uses {@link CAM02.VC#sRGB}. */
	default public Lab Lab () {
		return Lab(CAM02.VC.sRGB);
	}

	default public Lab Lab (CAM02.VC vc) {
		return CAM02(vc).Lab(vc);
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	default public LinearRGB LinearRGB () {
		return LinearRGB(CAM02.VC.sRGB);
	}

	default public LinearRGB LinearRGB (CAM02.VC vc) {
		return CAM02(vc).LinearRGB(vc);
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	default public RGB RGB () {
		return RGB(CAM02.VC.sRGB);
	}

	default public RGB RGB (CAM02.VC vc) {
		return CAM02(vc).RGB(vc);
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	default public uv uv () {
		return uv(CAM02.VC.sRGB);
	}

	default public uv uv (CAM02.VC vc) {
		return CAM02(vc).uv(vc);
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	default public xy xy () {
		return xy(CAM02.VC.sRGB);
	}

	default public xy xy (CAM02.VC vc) {
		return CAM02(vc).xy(vc);
	}

	/** Uses {@link CAM02.VC#sRGB}. */
	default public XYZ XYZ () {
		return XYZ(CAM02.VC.sRGB);
	}

	default public XYZ XYZ (CAM02.VC vc) {
		return CAM02(vc).XYZ(vc);
	}

	default public float len () {
		return (float)Math.sqrt(len2());
	}

	default public float len2 () {
		return J() * J() + a() * a() + b() * b();
	}
}
