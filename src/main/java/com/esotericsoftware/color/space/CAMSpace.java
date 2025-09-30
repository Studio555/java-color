
package com.esotericsoftware.color.space;

import static com.esotericsoftware.color.Colors.*;

public interface CAMSpace extends Color {
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

	default public float len () {
		return (float)Math.sqrt(len2());
	}

	default public float len2 () {
		return J() * J() + a() * a() + b() * b();
	}

	public interface CAM02Space extends CAMSpace {
		default public Lab Lab (CAM02.VC vc) {
			return CAM02(vc).Lab(vc);
		}

		default public LRGB LRGB (CAM02.VC vc) {
			return CAM02(vc).LRGB(vc);
		}

		default public RGB RGB (CAM02.VC vc) {
			return CAM02(vc).RGB(vc);
		}

		default public uv uv (CAM02.VC vc) {
			return CAM02(vc).uv(vc);
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

		default public float Y (CAM02.VC vc) {
			return CAM02(vc).XYZ(vc).Y();
		}
	}

	public interface CAM16Space extends CAMSpace {
		default public Lab Lab (CAM16.VC vc) {
			return CAM16(vc).Lab(vc);
		}

		default public LRGB LRGB (CAM16.VC vc) {
			return CAM16(vc).LRGB(vc);
		}

		default public RGB RGB (CAM16.VC vc) {
			return CAM16(vc).RGB(vc);
		}

		default public uv uv (CAM16.VC vc) {
			return CAM16(vc).uv(vc);
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

		default public float Y (CAM16.VC vc) {
			return CAM16(vc).XYZ(vc).Y();
		}
	}
}
