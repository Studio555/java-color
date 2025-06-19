
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;

import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.esotericsoftware.colors.Colors.CAM16;
import com.esotericsoftware.colors.Colors.CAM16UCS;
import com.esotericsoftware.colors.Colors.HCT;
import com.esotericsoftware.colors.Colors.HSL;
import com.esotericsoftware.colors.Colors.HSLuv;
import com.esotericsoftware.colors.Colors.HSV;
import com.esotericsoftware.colors.Colors.ITP;
import com.esotericsoftware.colors.Colors.LCh;
import com.esotericsoftware.colors.Colors.Lab;
import com.esotericsoftware.colors.Colors.LinearRGB;
import com.esotericsoftware.colors.Colors.Luv;
import com.esotericsoftware.colors.Colors.Oklab;
import com.esotericsoftware.colors.Colors.Oklch;
import com.esotericsoftware.colors.Colors.RGB;
import com.esotericsoftware.colors.Colors.XYZ;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class Gradients {
	public Gradients (Table config) throws Throwable {
		int imageWidth = config.labelWidth + (config.gradients.size() * config.cellWidth)
			+ ((config.gradients.size() + 1) * config.padding);
		int imageHeight = (config.colorSpaces.size() * config.cellHeight) + ((config.colorSpaces.size() + 1) * config.padding);
		var image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		var gr = (Graphics2D)image.getGraphics();

		gr.setColor(config.backgroundColor);
		gr.fillRect(0, 0, imageWidth, imageHeight);

		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		gr.setFont(new Font(config.fontName, Font.BOLD, config.fontSize));

		for (int row = 0; row < config.colorSpaces.size(); row++) {
			String colorSpace = config.colorSpaces.get(row);
			int y = config.padding + (row * (config.cellHeight + config.padding));

			// Label.
			int textY = y + config.cellHeight / 2 + config.fontSize / 3;
			gr.setColor(config.textColor);
			int textWidth = gr.getFontMetrics().stringWidth(colorSpace);
			int textX = config.labelWidth - textWidth - config.padding;
			gr.drawString(colorSpace, textX, textY);

			// Gradients.
			for (int col = 0; col < config.gradients.size(); col++) {
				Gradient gradient = config.gradients.get(col);
				int x = config.labelWidth + config.padding + (col * (config.cellWidth + config.padding));
				drawGradient(gr, colorSpace, gradient.from, gradient.to, x, y, config.cellWidth, config.cellHeight);
			}
		}

		File file = new File(config.outputFile);
		ImageIO.write(image, "png", file);
		System.out.println(file.getAbsolutePath());
	}

	private void drawGradient (Graphics2D gr, String colorSpace, RGB from, RGB to, int x, int y, int width, int height) {
		int steps = width;
		for (int i = 0; i < steps; i++) {
			float t = i / (float)(steps - 1);
			RGB interpolated = interpolateInColorSpace(colorSpace, from, to, t);

			// Clamp values to valid range
			float r = Math.max(0, Math.min(1, interpolated.r()));
			float g = Math.max(0, Math.min(1, interpolated.g()));
			float b = Math.max(0, Math.min(1, interpolated.b()));

			gr.setColor(new Color(r, g, b));
			gr.fillRect(x + i, y, 1, height);
		}

		// Border
		// gr.setColor(Color.GRAY);
		// gr.drawRect(x, y, width - 1, height - 1);
	}

	private RGB interpolateInColorSpace (String colorSpace, RGB from, RGB to, float t) {
		return switch (colorSpace) {
		case "RGB" -> new RGB(lerp(from.r(), to.r(), t), lerp(from.g(), to.g(), t), lerp(from.b(), to.b(), t));
		case "LinearRGB" -> {
			var c1 = LinearRGB(from);
			var c2 = LinearRGB(to);
			float r = lerp(c1.r(), c2.r(), t);
			float g = lerp(c1.g(), c2.g(), t);
			float b = lerp(c1.b(), c2.b(), t);
			yield new RGB(sRGB(r), sRGB(g), sRGB(b));
		}
		case "LinearRGB+L" -> {
			// Get target L* in Lab space.
			Lab lab1 = Lab(from);
			Lab lab2 = Lab(to);
			float targetL = lerp(lab1.L(), lab2.L(), t);

			var c1 = LinearRGB(from);
			var c2 = LinearRGB(to);
			float r = lerp(c1.r(), c2.r(), t);
			float g = lerp(c1.g(), c2.g(), t);
			float b = lerp(c1.b(), c2.b(), t);

			// Convert to Lab to adjust lightness while preserving hue/chroma direction.
			Lab currentLab = Lab(RGB(new LinearRGB(r, g, b)));

			// Use target L* but preserve a,b ratios (color direction).
			yield RGB(new Lab(targetL, currentLab.a(), currentLab.b()));
		}
		case "Oklch" -> {
			// Convert to Oklch for perceptually uniform interpolation
			Oklch c1 = Oklch(from);
			Oklch c2 = Oklch(to);
			float L = lerp(c1.L(), c2.L(), t);
			float C = lerp(c1.C(), c2.C(), t);
			float h = lerpAngle(c1.h(), c2.h(), t);
			yield RGB(new Oklch(L, C, h));
		}
		case "Oklab+L" -> {
			// Get target L in Oklab space.
			Oklab ok1 = Oklab(from);
			Oklab ok2 = Oklab(to);
			float targetL = lerp(ok1.L(), ok2.L(), t);

			var lin1 = LinearRGB(from);
			var lin2 = LinearRGB(to);
			float r = lerp(lin1.r(), lin2.r(), t);
			float g = lerp(lin1.g(), lin2.g(), t);
			float b = lerp(lin1.b(), lin2.b(), t);

			// Convert to Oklab to adjust lightness while preserving hue/chroma direction.
			Oklab currentOklab = Oklab(RGB(new LinearRGB(r, g, b)));

			// Use target L* but preserve a,b ratios (color direction).
			yield RGB(new Oklab(targetL, currentOklab.a(), currentOklab.b()));
		}
		case "HSL" -> {
			HSL c1 = HSL(from);
			HSL c2 = HSL(to);
			float h = lerpAngle(c1.H(), c2.H(), t);
			float s = lerp(c1.S(), c2.S(), t);
			float l = lerp(c1.L(), c2.L(), t);
			yield RGB(new HSL(h, s, l));
		}
		case "HSV" -> {
			HSV c1 = HSV(from);
			HSV c2 = HSV(to);
			float h = lerpAngle(c1.H(), c2.H(), t);
			float s = lerp(c1.S(), c2.S(), t);
			float v = lerp(c1.V(), c2.V(), t);
			yield RGB(new HSV(h, s, v));
		}
		case "Lab" -> {
			Lab c1 = Lab(from);
			Lab c2 = Lab(to);
			float L = lerp(c1.L(), c2.L(), t);
			float a = lerp(c1.a(), c2.a(), t);
			float b = lerp(c1.b(), c2.b(), t);
			yield RGB(new Lab(L, a, b));
		}
		case "Oklab" -> {
			Oklab c1 = Oklab(from);
			Oklab c2 = Oklab(to);
			float L = lerp(c1.L(), c2.L(), t);
			float a = lerp(c1.a(), c2.a(), t);
			float b = lerp(c1.b(), c2.b(), t);
			yield RGB(new Oklab(L, a, b));
		}
		case "HCT" -> {
			HCT c1 = HCT(from);
			HCT c2 = HCT(to);
			float h = lerpAngle(c1.h(), c2.h(), t);
			float c = lerp(c1.C(), c2.C(), t);
			float tone = lerp(c1.T(), c2.T(), t);
			yield RGB(new HCT(h, c, tone));
		}
		case "CAM16" -> {
			CAM16 c1 = CAM16(from);
			CAM16 c2 = CAM16(to);
			float J = lerp(c1.J(), c2.J(), t);
			float C = lerp(c1.C(), c2.C(), t);
			float h = lerpAngle(c1.h(), c2.h(), t);
			yield RGB(new CAM16(J, C, h, 0, 0, 0));
		}
		case "CAM16UCS" -> {
			CAM16UCS c1 = CAM16UCS(CAM16(from));
			CAM16UCS c2 = CAM16UCS(CAM16(to));
			float J = lerp(c1.J(), c2.J(), t);
			float a = lerp(c1.a(), c2.a(), t);
			float b = lerp(c1.b(), c2.b(), t);
			yield RGB(CAM16(new CAM16UCS(J, a, b), CAM16.VC.sRGB));
		}
		case "HSLuv" -> {
			HSLuv c1 = HSLuv(from);
			HSLuv c2 = HSLuv(to);
			float h = lerpAngle(c1.H(), c2.H(), t);
			float s = lerp(c1.S(), c2.S(), t);
			float l = lerp(c1.L(), c2.L(), t);
			yield RGB(new HSLuv(h, s, l));
		}
		case "LCh" -> {
			LCh c1 = LCh(from);
			LCh c2 = LCh(to);
			float L = lerp(c1.L(), c2.L(), t);
			float C = lerp(c1.C(), c2.C(), t);
			float h = lerpAngle(c1.h(), c2.h(), t);
			yield RGB(new LCh(L, C, h));
		}
		case "XYZ" -> {
			XYZ c1 = XYZ(from);
			XYZ c2 = XYZ(to);
			float X = lerp(c1.X(), c2.X(), t);
			float Y = lerp(c1.Y(), c2.Y(), t);
			float Z = lerp(c1.Z(), c2.Z(), t);
			yield RGB(new XYZ(X, Y, Z));
		}
		case "Luv" -> {
			Luv c1 = Luv(from);
			Luv c2 = Luv(to);
			float u1 = Float.isNaN(c1.u()) ? 0 : c1.u();
			float v1 = Float.isNaN(c1.v()) ? 0 : c1.v();
			float u2 = Float.isNaN(c2.u()) ? 0 : c2.u();
			float v2 = Float.isNaN(c2.v()) ? 0 : c2.v();
			float L = lerp(c1.L(), c2.L(), t);
			float u = lerp(u1, u2, t);
			float v = lerp(v1, v2, t);
			yield RGB(new Luv(L, u, v));
		}
		case "ITP" -> {
			ITP c1 = ITP(from);
			ITP c2 = ITP(to);
			float I = lerp(c1.I(), c2.I(), t);
			float T = lerp(c1.Ct(), c2.Ct(), t);
			float P = lerp(c1.Cp(), c2.Cp(), t);
			yield RGB(new ITP(I, T, P));
		}
		default -> throw new RuntimeException(colorSpace);
		};
	}

	private float lerpAngle (float from, float to, float t) {
		// Handle NaN for achromatic colors
		if (Float.isNaN(from) && Float.isNaN(to)) return 0;
		if (Float.isNaN(from)) return to;
		if (Float.isNaN(to)) return from;

		float diff = to - from;
		if (diff > 180)
			diff -= 360;
		else if (diff < -180) //
			diff += 360;
		float result = from + diff * t;
		if (result < 0) result += 360;
		if (result >= 360) result -= 360;
		return result;
	}

	static class Gradient {
		RGB from, to;

		Gradient (RGB from, RGB to) {
			this.from = from;
			this.to = to;
		}
	}

	static class Table {
		int cellWidth = 276;
		int cellHeight = 151;
		int labelWidth = 118;
		int padding = 5;
		int fontSize = 16;
		Color backgroundColor = Color.WHITE;
		Color textColor = Color.BLACK;
		String fontName = "Arial";
		String outputFile = "gradients.png";
		ArrayList<String> colorSpaces = new ArrayList();
		ArrayList<Gradient> gradients = new ArrayList();
	}

	@SuppressWarnings("unused")
	static public void main (String[] args) throws Throwable {
		var config = new Table();

		config.colorSpaces.add("RGB");
		config.colorSpaces.add("XYZ");
		config.colorSpaces.add("LinearRGB");
		config.colorSpaces.add("LinearRGB+L");
		config.colorSpaces.add("Oklab+L");
		config.colorSpaces.add("Luv");
		config.colorSpaces.add("CAM16UCS");
		config.colorSpaces.add("Oklab");
		config.colorSpaces.add("HCT");
		config.colorSpaces.add("HSLuv");
		config.colorSpaces.add("CAM16");
		config.colorSpaces.add("Oklch");
		config.colorSpaces.add("LCh");
		config.colorSpaces.add("Lab");
		config.colorSpaces.add("ITP"); // Intended for HDR.
		config.colorSpaces.add("HSL");
		config.colorSpaces.add("HSV");

		config.gradients.add(new Gradient(RGB(0xe93227), RGB(0x0422f3))); // Red to Blue
		config.gradients.add(new Gradient(RGB(0xe93824), RGB(0x80f54b))); // Orange to Light Green
		config.gradients.add(new Gradient(RGB(0x0239de), RGB(0xf9fc59))); // Blue to Yellow
		config.gradients.add(new Gradient(RGB(0x002fe8), RGB(0x75f656))); // Blue to Green
		config.gradients.add(new Gradient(RGB(0xffffff), RGB(0x000000))); // White to Black

		new Gradients(config);
	}
}
