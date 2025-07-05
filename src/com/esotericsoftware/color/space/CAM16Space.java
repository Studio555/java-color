
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Util.*;

import com.esotericsoftware.color.Color;

public interface CAM16Space extends Color {
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

	/** Uses {@link CAM16.VC#sRGB}. */
	default public CAM16 CAM16 () {
		return CAM16(CAM16.VC.sRGB);
	}

	public CAM16 CAM16 (CAM16.VC vc);

	/** Uses {@link CAM16.VC#sRGB}. */
	default public Lab Lab () {
		return Lab(CAM16.VC.sRGB);
	}

	default public Lab Lab (CAM16.VC vc) {
		return CAM16(vc).Lab(vc);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	default public LinearRGB LinearRGB () {
		return LinearRGB(CAM16.VC.sRGB);
	}

	default public LinearRGB LinearRGB (CAM16.VC vc) {
		return CAM16(vc).LinearRGB(vc);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	default public RGB RGB () {
		return RGB(CAM16.VC.sRGB);
	}

	default public RGB RGB (CAM16.VC vc) {
		return CAM16(vc).RGB(vc);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	default public uv uv () {
		return uv(CAM16.VC.sRGB);
	}

	default public uv uv (CAM16.VC vc) {
		return CAM16(vc).uv(vc);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	default public xy xy () {
		return xy(CAM16.VC.sRGB);
	}

	default public xy xy (CAM16.VC vc) {
		return CAM16(vc).xy(vc);
	}

	/** Uses {@link CAM16.VC#sRGB}. */
	default public XYZ XYZ () {
		return XYZ(CAM16.VC.sRGB);
	}

	default public XYZ XYZ (CAM16.VC vc) {
		return CAM16(vc).XYZ(vc);
	}

	default public float len () {
		return (float)Math.sqrt(len2());
	}

	default public float len2 () {
		return J() * J() + a() * a() + b() * b();
	}
}
