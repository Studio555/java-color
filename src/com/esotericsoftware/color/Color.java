
package com.esotericsoftware.color;

import com.esotericsoftware.color.space.LinearRGB;
import com.esotericsoftware.color.space.RGB;
import com.esotericsoftware.color.space.XYZ;
import com.esotericsoftware.color.space.uv;
import com.esotericsoftware.color.space.xy;

public interface Color {
	default public LinearRGB LinearRGB () {
		return XYZ().LinearRGB();
	}

	default public RGB RGB () {
		return XYZ().RGB();
	}

	default public uv uv () {
		return XYZ().uv();
	}

	default public xy xy () {
		return XYZ().xy();
	}

	public XYZ XYZ ();
}
