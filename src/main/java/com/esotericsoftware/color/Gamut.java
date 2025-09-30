
package com.esotericsoftware.color;

import static com.esotericsoftware.color.Gamut.*;
import static com.esotericsoftware.color.Colors.*;

import com.esotericsoftware.color.space.LRGB;
import com.esotericsoftware.color.space.RGB;
import com.esotericsoftware.color.space.XYZ;
import com.esotericsoftware.color.space.uv;
import com.esotericsoftware.color.space.xy;

/** @author Nathan Sweet <misc@n4te.com> */
public interface Gamut {
	static public final RGBGamut //
	sRGB = new RGBGamut(new xy(0.64f, 0.33f), new xy(0.3f, 0.6f), new xy(0.15f, 0.06f)), //
		DisplayP3 = new RGBGamut(new xy(0.68f, 0.32f), new xy(0.265f, 0.69f), new xy(0.15f, 0.06f)), //
		Rec2020 = new RGBGamut(new xy(0.708f, 0.292f), new xy(0.17f, 0.797f), new xy(0.131f, 0.046f)), //
		all = new RGBGamut(new xy(1, 0), new xy(0, 1), new xy(0, 0)); //

	public boolean contains (xy xy);

	public xy nearest (xy xy);

	public xy raycast (xy xy);

	public uv nearest (uv uv);

	public uv raycast (uv uv);

	public XYZ XYZ (LRGB rgb);

	public LRGB LRGB (XYZ XYZ);

	public XYZ whitePoint ();

	default public boolean contains (uv uv) {
		return contains(uv.xy());
	}

	default public XYZ XYZ (RGB rgb) {
		return XYZ(rgb.LRGB());
	}

	default public xy xy (LRGB rgb) {
		return XYZ(rgb).xy();
	}

	default public xy xy (RGB rgb) {
		return XYZ(rgb.LRGB()).xy();
	}

	default public uv uv (LRGB rgb) {
		return XYZ(rgb).uv();
	}

	default public uv uv (RGB rgb) {
		return XYZ(rgb.LRGB()).uv();
	}

	default public LRGB LRGB (xy xy) {
		return LRGB(xy.XYZ(1));
	}

	default public LRGB LRGB (uv uv) {
		return LRGB(uv.xy().XYZ(1));
	}

	default public RGB RGB (xy xy) {
		return LRGB(xy.XYZ(1)).RGB();
	}

	default public RGB RGB (uv uv) {
		return LRGB(uv.xy().XYZ(1)).RGB();
	}

	static private xy closestPointOnSegment (xy xy, GamutVertex av, GamutVertex bv) {
		xy a = av.xy, b = bv.xy;
		float xDiff = b.x() - a.x(), yDiff = b.y() - a.y(), length2 = xDiff * xDiff + yDiff * yDiff;
		if (length2 == 0) return a;
		float t = ((xy.x() - a.x()) * xDiff + (xy.y() - a.y()) * yDiff) / length2;
		if (t <= 0) return a;
		if (t >= 1) return b;
		return new xy(a.x() + t * xDiff, a.y() + t * yDiff);
	}

	static private uv closestPointOnSegment (uv uv, GamutVertex av, GamutVertex bv) {
		uv a = av.uv, b = bv.uv;
		float xDiff = b.u() - a.u(), yDiff = b.v() - a.v(), length2 = xDiff * xDiff + yDiff * yDiff;
		if (length2 == 0) return a;
		float t = ((uv.u() - a.u()) * xDiff + (uv.v() - a.v()) * yDiff) / length2;
		if (t <= 0) return a;
		if (t >= 1) return b;
		return new uv(a.u() + t * xDiff, a.v() + t * yDiff);
	}

	static private xy lineSegmentIntersect (xy origin, float dx, float dy, xy a, xy b) {
		float ex = b.x() - a.x(), ey = b.y() - a.y();
		float denom = dx * ey - dy * ex;
		if (Math.abs(denom) < EPSILON) return null; // Parallel.
		float t = ((a.x() - origin.x()) * ey - (a.y() - origin.y()) * ex) / denom;
		float s = ((a.x() - origin.x()) * dy - (a.y() - origin.y()) * dx) / denom;
		if (t > EPSILON && s >= 0 && s <= 1) return new xy(origin.x() + t * dx, origin.y() + t * dy);
		return null;
	}

	static private uv lineSegmentIntersect (uv origin, float dx, float dy, uv a, uv b) {
		float ex = b.u() - a.u(), ey = b.v() - a.v();
		float denom = dx * ey - dy * ex;
		if (Math.abs(denom) < EPSILON) return null; // Parallel.
		float t = ((a.u() - origin.u()) * ey - (a.v() - origin.v()) * ex) / denom;
		float s = ((a.u() - origin.u()) * dy - (a.v() - origin.v()) * dx) / denom;
		if (t > EPSILON && s >= 0 && s <= 1) return new uv(origin.u() + t * dx, origin.v() + t * dy);
		return null;
	}

	public record GamutVertex (xy xy, uv uv) {
		public GamutVertex (xy xy) {
			this(xy, xy.uv());
		}

		public GamutVertex (uv uv) {
			this(uv.xy(), uv);
		}
	}

	static public class RGBGamut implements Gamut {
		public final GamutVertex red, green, blue;
		private final XYZ wpXYZ;
		private final xy wpxy;
		private final uv wpuv;
		public final float[][] RGB_XYZ, XYZ_RGB;

		/** Uses {@link Observer#Default} D65. */
		public RGBGamut (uv red, uv green, uv blue) {
			this(red.xy(), green.xy(), blue.xy(), Observer.Default.D65);
		}

		/** Uses {@link Observer#Default} D65. */
		public RGBGamut (xy red, xy green, xy blue) {
			this(red, green, blue, Observer.Default.D65);
		}

		public RGBGamut (uv red, uv green, uv blue, XYZ whitePoint) {
			this(red.xy(), green.xy(), blue.xy(), whitePoint);
		}

		public RGBGamut (xy red, xy green, xy blue, XYZ whitePoint) {
			if (red == null) throw new IllegalArgumentException("red cannot be null.");
			if (green == null) throw new IllegalArgumentException("green cannot be null.");
			if (blue == null) throw new IllegalArgumentException("blue cannot be null.");
			if (whitePoint == null) throw new IllegalArgumentException("whitePoint cannot be null.");
			this.red = new GamutVertex(red);
			this.green = new GamutVertex(green);
			this.blue = new GamutVertex(blue);
			wpXYZ = whitePoint;
			wpxy = whitePoint.xy();
			wpuv = whitePoint.uv();
			RGB_XYZ = RGB_XYZ();
			XYZ_RGB = invert3x3(RGB_XYZ);
			for (int row = 0; row < 3; row++)
				for (int column = 0; column < 3; column++)
					RGB_XYZ[row][column] *= 100;
		}

		public XYZ whitePoint () {
			return wpXYZ;
		}

		public boolean contains (xy xy) {
			// Check inside.
			// Check on vertex.
			if ((below(xy, blue, green) && below(xy, green, red) && above(xy, red, blue)) || (red.xy.dst2(xy) < EPSILON * EPSILON))
				return true;
			if (green.xy.dst2(xy) < EPSILON * EPSILON) return true;
			if (blue.xy.dst2(xy) < EPSILON * EPSILON) return true;
			// Check on edge.
			if (segment(xy, red, green)) return true;
			if (segment(xy, green, blue)) return true;
			if (segment(xy, blue, red)) return true;
			return false;
		}

		public xy nearest (xy xy) {
			if (contains(xy)) return xy;
			xy pAB = closestPointOnSegment(xy, red, green);
			xy pAC = closestPointOnSegment(xy, red, blue);
			xy pBC = closestPointOnSegment(xy, green, blue);
			float dAB = xy.dst2(pAB), dAC = xy.dst2(pAC), dBC = xy.dst2(pBC), lowest = dAB;
			xy closestPoint = pAB;
			if (dAC < lowest) {
				lowest = dAC;
				closestPoint = pAC;
			}
			return dBC < lowest ? pBC : closestPoint;
		}

		public uv nearest (uv uv) {
			if (contains(uv.xy())) return uv;
			uv pAB = closestPointOnSegment(uv, red, green);
			uv pAC = closestPointOnSegment(uv, red, blue);
			uv pBC = closestPointOnSegment(uv, green, blue);
			float dAB = uv.dst2(pAB), dAC = uv.dst2(pAC), dBC = uv.dst2(pBC), lowest = dAB;
			uv closestPoint = pAB;
			if (dAC < lowest) {
				lowest = dAC;
				closestPoint = pAC;
			}
			return dBC < lowest ? pBC : closestPoint;
		}

		public xy raycast (xy xy) {
			if (contains(xy)) return xy;
			float dx = xy.x() - wpxy.x(), dy = xy.y() - wpxy.y();
			xy intersection = null;
			float minT = Float.MAX_VALUE;
			xy p = lineSegmentIntersect(wpxy, dx, dy, red.xy, green.xy); // Red-green.
			if (p != null) {
				float t = dx != 0 ? (p.x() - wpxy.x()) / dx : (p.y() - wpxy.y()) / dy;
				if (t > EPSILON && t < minT) {
					minT = t;
					intersection = p;
				}
			}
			p = lineSegmentIntersect(wpxy, dx, dy, green.xy, blue.xy); // Green-blue.
			if (p != null) {
				float t = dx != 0 ? (p.x() - wpxy.x()) / dx : (p.y() - wpxy.y()) / dy;
				if (t > EPSILON && t < minT) {
					minT = t;
					intersection = p;
				}
			}
			p = lineSegmentIntersect(wpxy, dx, dy, blue.xy, red.xy); // Blue-red.
			if (p != null) {
				float t = dx != 0 ? (p.x() - wpxy.x()) / dx : (p.y() - wpxy.y()) / dy;
				if (t > EPSILON && t < minT) {
					minT = t;
					intersection = p;
				}
			}
			return intersection != null ? intersection : xy;
		}

		public uv raycast (uv uv) {
			if (contains(uv)) return uv;
			float dx = uv.u() - wpuv.u(), dy = uv.v() - wpuv.v();
			uv intersection = null;
			float minT = Float.MAX_VALUE;
			uv p = lineSegmentIntersect(wpuv, dx, dy, red.uv, green.uv); // Red-green.
			if (p != null) {
				float t = dx != 0 ? (p.u() - wpuv.u()) / dx : (p.v() - wpuv.v()) / dy;
				if (t > EPSILON && t < minT) {
					minT = t;
					intersection = p;
				}
			}
			p = lineSegmentIntersect(wpuv, dx, dy, green.uv, blue.uv); // Green-blue.
			if (p != null) {
				float t = dx != 0 ? (p.u() - wpuv.u()) / dx : (p.v() - wpuv.v()) / dy;
				if (t > EPSILON && t < minT) {
					minT = t;
					intersection = p;
				}
			}
			p = lineSegmentIntersect(wpuv, dx, dy, blue.uv, red.uv); // Blue-red.
			if (p != null) {
				float t = dx != 0 ? (p.u() - wpuv.u()) / dx : (p.v() - wpuv.v()) / dy;
				if (t > EPSILON && t < minT) {
					minT = t;
					intersection = p;
				}
			}
			return intersection != null ? intersection : uv;
		}

		public XYZ XYZ (LRGB rgb) {
			float r = rgb.r(), g = rgb.g(), b = rgb.b();
			float X = RGB_XYZ[0][0] * r + RGB_XYZ[0][1] * g + RGB_XYZ[0][2] * b;
			float Y = RGB_XYZ[1][0] * r + RGB_XYZ[1][1] * g + RGB_XYZ[1][2] * b;
			float Z = RGB_XYZ[2][0] * r + RGB_XYZ[2][1] * g + RGB_XYZ[2][2] * b;
			return new XYZ(X * 100, Y * 100, Z * 100);
		}

		public LRGB LRGB (XYZ XYZ) {
			float X = XYZ.X() / 100, Y = XYZ.Y() / 100, Z = XYZ.Z() / 100;
			float r = XYZ_RGB[0][0] * X + XYZ_RGB[0][1] * Y + XYZ_RGB[0][2] * Z;
			float g = XYZ_RGB[1][0] * X + XYZ_RGB[1][1] * Y + XYZ_RGB[1][2] * Z;
			float b = XYZ_RGB[2][0] * X + XYZ_RGB[2][1] * Y + XYZ_RGB[2][2] * Z;
			float max = max(r, g, b);
			if (max > 0) {
				r /= max;
				g /= max;
				b /= max;
			}
			return new LRGB(r, g, b);
		}

		static private boolean below (xy xy, GamutVertex av, GamutVertex bv) {
			xy a = av.xy, b = bv.xy;
			float xDiff = a.x() - b.x();
			if (Math.abs(xDiff) < EPSILON) return true;
			float slope = (a.y() - b.y()) / xDiff;
			return xy.y() <= xy.x() * slope + a.y() - slope * a.x();
		}

		static private boolean above (xy xy, GamutVertex av, GamutVertex bv) {
			xy a = av.xy, b = bv.xy;
			float xDiff = a.x() - b.x();
			if (Math.abs(xDiff) < EPSILON) return true;
			float slope = (a.y() - b.y()) / xDiff;
			return xy.y() >= xy.x() * slope + a.y() - slope * a.x();
		}

		static private boolean segment (xy p, GamutVertex av, GamutVertex bv) {
			xy a = av.xy, b = bv.xy;
			float dx = b.x() - a.x(), dy = b.y() - a.y();
			float length = dx * dx + dy * dy;
			if (length < EPSILON * EPSILON) return false;
			float t = ((p.x() - a.x()) * dx + (p.y() - a.y()) * dy) / length;
			if (t < 0 || t > 1) return false;
			dx = p.x() - (a.x() + t * dx);
			dy = p.y() - (a.y() + t * dy);
			return dx * dx + dy * dy < EPSILON * EPSILON;
		}

		private float[][] RGB_XYZ () {
			xy red = this.red.xy, green = this.green.xy, blue = this.blue.xy;
			float Xr = red.x() / red.y();
			float Yr = 1;
			float Zr = (1 - red.x() - red.y()) / red.y();
			float Xg = green.x() / green.y();
			float Yg = 1;
			float Zg = (1 - green.x() - green.y()) / green.y();
			float Xb = blue.x() / blue.y();
			float Yb = 1;
			float Zb = (1 - blue.x() - blue.y()) / blue.y();
			float[][] M = { //
				{Xr, Xg, Xb}, //
				{Yr, Yg, Yb}, //
				{Zr, Zg, Zb}};
			float[] S = matrixSolve(M, wpXYZ.X() / wpXYZ.Y(), 1, wpXYZ.Z() / wpXYZ.Y());
			return new float[][] { //
				{Xr * S[0], Xg * S[1], Xb * S[2]}, //
				{Yr * S[0], Yg * S[1], Yb * S[2]}, //
				{Zr * S[0], Zg * S[1], Zb * S[2]}};
		}

		static public class PhilipsHue {
			static public final RGBGamut //
			wide = new RGBGamut(new xy(0.700607f, 0.299301f), new xy(0.172416f, 0.746797f), new xy(0.135503f, 0.039879f)),
				A = new RGBGamut(new xy(0.704f, 0.296f), new xy(0.2151f, 0.7106f), new xy(0.138f, 0.08f)), //
				B = new RGBGamut(new xy(0.675f, 0.322f), new xy(0.409f, 0.518f), new xy(0.167f, 0.04f)), //
				C = new RGBGamut(new xy(0.692f, 0.308f), new xy(0.17f, 0.7f), new xy(0.153f, 0.048f));

			/** Returns the gamut for the model identifier, or null.
			 * <p>
			 * This model list is no longer used by Philips. Use the gamut from the light. */
			static public Gamut forModel (String model) {
				return switch (model) {
				case "LLC001", // Monet, Renoir, Mondriaan (gen II)
					"LLC005", // Bloom (gen II)
					"LLC006", // Iris (gen III)
					"LLC007", // Bloom, Aura (gen III)
					"LLC010", // Iris
					"LLC011", // Hue Bloom
					"LLC012", // Hue Bloom
					"LLC013", // Storylight
					"LST001", // Light Strips
					"LLC014" // Bloom, Aura (gen III)
					-> A;
				case "LCT001", // Hue A19
					"LCT002", // Hue BR30
					"LCT003", // Hue GU10
					"LCT007", // Hue A19
					"LLM001" // Color Light Module
					-> B;
				case "LLC020", // Hue Go
					"LST002", // Hue LightStrips Plus
					"LCT010", // Hue A19 gen 3
					"LCT011", // Hue BR30
					"LCT012", // Hue color candle
					"LCT014", // Hue A19 gen 3
					"LCT015", // Hue A19 gen 3
					"LCT016" // Hue A19 gen 3
					-> C;
				default -> null;
				};
			}
		}
	}

	static public class PolygonGamut implements Gamut {
		public final GamutVertex[] vertices;
		/** xy coordinate pairs. */
		public final float[] xys;
		private XYZ wpXYZ;
		private xy wpxy;
		private uv wpuv;

		public PolygonGamut (xy... polygon) {
			if (polygon == null) throw new IllegalArgumentException("polygon cannot be null.");
			int n = polygon.length;
			if (n < 3) throw new IllegalArgumentException("polygon must have >= 3 points: " + n);
			vertices = new GamutVertex[n];
			xys = new float[n << 1];
			for (int i = 0, f = 0; i < n; i++, f += 2) {
				xy xy = polygon[i];
				vertices[i] = new GamutVertex(xy);
				xys[f] = xy.x();
				xys[f + 1] = xy.y();
			}
		}

		public PolygonGamut (uv... polygon) {
			if (polygon == null) throw new IllegalArgumentException("polygon cannot be null.");
			int n = polygon.length;
			if (n < 3) throw new IllegalArgumentException("polygon must have >= 3 points: " + n);
			vertices = new GamutVertex[n];
			xys = new float[n << 1];
			for (int i = 0, f = 0; i < n; i++, f += 2) {
				uv uv = polygon[i];
				var vertex = new GamutVertex(uv);
				vertices[i] = vertex;
				xys[f] = vertex.xy.x();
				xys[f + 1] = vertex.xy.y();
			}
		}

		public boolean contains (xy xy) {
			float x = xy.x(), y = xy.y();
			// Check inside.
			boolean odd = false;
			int n = xys.length;
			float xj = xys[n - 2], yj = xys[n - 1];
			for (int i = 0; i < n; i += 2) {
				float xi = xys[i], yi = xys[i + 1];
				if (yi < y == yj >= y) {
					float xint = xi + (y - yi) * (xj - xi) / (yj - yi);
					if (xint < x - EPSILON)
						odd = !odd;
					else if (Math.abs(xint - x) < EPSILON) //
						return true;
				}
				xj = xi;
				yj = yi;
			}
			if (odd) return true;
			xj = xys[n - 2];
			yj = xys[n - 1];
			for (int i = 0; i < n; i += 2) {
				// Check on vertex.
				float xi = xys[i], yi = xys[i + 1], dx = x - xi, dy = y - yi;
				if (dx * dx + dy * dy < EPSILON * EPSILON) return true;
				// Check on edge.
				float ex = xj - xi, ey = yj - yi, length = ex * ex + ey * ey;
				if (length > EPSILON * EPSILON) {
					float t = (dx * ex + dy * ey) / length;
					if (t >= 0 && t <= 1) {
						float px = dx - t * ex, py = dy - t * ey;
						if (px * px + py * py < EPSILON * EPSILON) return true;
					}
				}
				xj = xi;
				yj = yi;
			}
			return false;
		}

		public xy nearest (xy xy) {
			if (contains(xy)) return xy;
			float minDist = Float.MAX_VALUE;
			xy closest = null;
			GamutVertex a = vertices[vertices.length - 1];
			for (GamutVertex b : vertices) {
				xy pointOnEdge = closestPointOnSegment(xy, a, b);
				float dist = xy.dst2(pointOnEdge);
				if (dist < minDist) {
					minDist = dist;
					closest = pointOnEdge;
				}
				a = b;
			}
			return closest;
		}

		public uv nearest (uv uv) {
			if (contains(uv.xy())) return uv;
			float minDist = Float.MAX_VALUE;
			uv closest = null;
			GamutVertex a = vertices[vertices.length - 1];
			for (GamutVertex b : vertices) {
				uv pointOnEdge = closestPointOnSegment(uv, a, b);
				float dist = uv.dst2(pointOnEdge);
				if (dist < minDist) {
					minDist = dist;
					closest = pointOnEdge;
				}
				a = b;
			}
			return closest;
		}

		/** Requires white point to be set. */
		public xy raycast (xy xy) {
			if (contains(xy)) return xy;
			float dx = xy.x() - wpxy.x(), dy = xy.y() - wpxy.y();
			xy intersection = null;
			float minT = Float.MAX_VALUE;
			GamutVertex a = vertices[vertices.length - 1];
			for (GamutVertex b : vertices) {
				xy p = lineSegmentIntersect(wpxy, dx, dy, a.xy, b.xy);
				if (p != null) {
					float t = dx != 0 ? (p.x() - wpxy.x()) / dx : (p.y() - wpxy.y()) / dy;
					if (t > EPSILON && t < minT) {
						minT = t;
						intersection = p;
					}
				}
				a = b;
			}
			return intersection != null ? intersection : xy;
		}

		/** Requires white point to be set. */
		public uv raycast (uv uv) {
			if (contains(uv)) return uv;
			float dx = uv.u() - wpuv.u(), dy = uv.v() - wpuv.v();
			uv intersection = null;
			float minT = Float.MAX_VALUE;
			GamutVertex a = vertices[vertices.length - 1];
			for (GamutVertex b : vertices) {
				uv p = lineSegmentIntersect(wpuv, dx, dy, a.uv, b.uv);
				if (p != null) {
					float t = dx != 0 ? (p.u() - wpuv.u()) / dx : (p.v() - wpuv.v()) / dy;
					if (t > EPSILON && t < minT) {
						minT = t;
						intersection = p;
					}
				}
				a = b;
			}
			return intersection != null ? intersection : uv;
		}

		public void setWhitePoint (XYZ whitePoint) {
			wpXYZ = whitePoint;
			wpxy = whitePoint.xy();
			wpuv = whitePoint.uv();
		}

		/** Returns the white point, or null if not set. */
		public XYZ whitePoint () {
			return wpXYZ;
		}

		/** Unsupported by default. Override and implement if needed. */
		public XYZ XYZ (LRGB rgb) {
			throw new UnsupportedOperationException();
		}

		/** Unsupported by default. Override and implement if needed. */
		public LRGB LRGB (XYZ XYZ) {
			throw new UnsupportedOperationException();
		}
	}
}
