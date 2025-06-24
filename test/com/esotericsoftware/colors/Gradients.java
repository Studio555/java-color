
package com.esotericsoftware.colors;import static com.esotericsoftware.colors.Util.*;import static com.esotericsoftware.colors.Colors.*;import static com.esotericsoftware.colors.Colors.*;

import static com.esotericsoftware.colors.Colors.*;

import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.esotericsoftware.colors.Colors.CAM16;
import com.esotericsoftware.colors.Colors.CAM16UCS;
import com.esotericsoftware.colors.Colors.Lab;
import com.esotericsoftware.colors.Colors.Oklab;
import com.esotericsoftware.colors.Colors.RGB;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** @author Nathan Sweet <misc@n4te.com> */
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
		case "RGB" -> lerp(from, to, t);
		case "LinearRGB" -> {
			var c1 = LinearRGB(from);
			var c2 = LinearRGB(to);
			var interpolated = lerp(c1, c2, t);
			yield new RGB(sRGB(interpolated.r()), sRGB(interpolated.g()), sRGB(interpolated.b()));
		}
		case "LinearRGB+L" -> {
			// Get target L* in Lab space.
			Lab lab1 = Lab(from);
			Lab lab2 = Lab(to);
			float targetL = lerp(lab1.L(), lab2.L(), t);

			// Convert to Lab to adjust lightness while preserving hue/chroma direction.
			Lab currentLab = Lab(RGB(lerp(LinearRGB(from), LinearRGB(to), t)));

			// Use target L* but preserve a,b ratios (color direction).
			yield RGB(new Lab(targetL, currentLab.a(), currentLab.b()));
		}
		case "Oklab" -> RGB(lerp(Oklab(from), Oklab(to), t));
		case "Oklab+L" -> {
			// Get target L in Oklab space.
			Oklab ok1 = Oklab(from);
			Oklab ok2 = Oklab(to);
			float targetL = lerp(ok1.L(), ok2.L(), t);

			// Convert to Oklab to adjust lightness while preserving hue/chroma direction.
			Oklab currentOklab = Oklab(RGB(lerp(LinearRGB(from), LinearRGB(to), t)));

			// Use target L* but preserve a,b ratios (color direction).
			yield RGB(new Oklab(targetL, currentOklab.a(), currentOklab.b()));
		}
		case "Oklch" -> RGB(lerp(Oklch(from), Oklch(to), t));
		case "Okhsv" -> RGB(lerp(Okhsv(from), Okhsv(to), t));
		case "HSV" -> RGB(lerp(HSV(from), HSV(to), t));
		case "HSL" -> RGB(lerp(HSL(from), HSL(to), t));
		case "Lab" -> RGB(lerp(Lab(from), Lab(to), t));
		case "HCT" -> RGB(lerp(HCT(from), HCT(to), t));
		case "CAM16" -> RGB(lerp(CAM16(from), CAM16(to), t));
		case "CAM16UCS" -> {
			CAM16UCS c1 = CAM16UCS(CAM16(from));
			CAM16UCS c2 = CAM16UCS(CAM16(to));
			yield RGB(CAM16(lerp(c1, c2, t), CAM16.VC.sRGB));
		}
		case "HSLuv" -> RGB(lerp(HSLuv(from), HSLuv(to), t));
		case "Luv" -> RGB(lerp(Luv(from), Luv(to), t));
		case "LCh" -> RGB(lerp(LCh(from), LCh(to), t));
		case "XYZ" -> RGB(lerp(XYZ(from), XYZ(to), t));
		case "ITP" -> RGB(lerp(ITP(from), ITP(to), t));
		default -> throw new RuntimeException(colorSpace);
		};
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
		config.colorSpaces.add("Lab");
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
		config.colorSpaces.add("ITP"); // Intended for HDR.
		config.colorSpaces.add("LCh");
		config.colorSpaces.add("Okhsv");
		config.colorSpaces.add("HSV");
		config.colorSpaces.add("HSL");

		config.gradients.add(new Gradient(RGB(0xe93227), RGB(0x0422f3))); // Red to Blue
		config.gradients.add(new Gradient(RGB(0xe93824), RGB(0x80f54b))); // Orange to Light Green
		config.gradients.add(new Gradient(RGB(0x0239de), RGB(0xf9fc59))); // Blue to Yellow
		config.gradients.add(new Gradient(RGB(0x002fe8), RGB(0x75f656))); // Blue to Green
		config.gradients.add(new Gradient(RGB(0xffffff), RGB(0x000000))); // White to Black

		new Gradients(config);
	}
}
