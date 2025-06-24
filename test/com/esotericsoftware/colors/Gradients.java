
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Util.*;

import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.esotericsoftware.colors.space.CAM16;
import com.esotericsoftware.colors.space.CAM16UCS;
import com.esotericsoftware.colors.space.Lab;
import com.esotericsoftware.colors.space.LinearRGB;
import com.esotericsoftware.colors.space.Oklab;
import com.esotericsoftware.colors.space.RGB;

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
		case "RGB" -> from.lerp(to, t);
		case "LinearRGB" -> {
			LinearRGB c1 = from.LinearRGB();
			LinearRGB c2 = to.LinearRGB();
			yield c1.lerp(c2, t).RGB();
		}
		case "LinearRGB+L" -> {
			// Get target L* in Lab space.
			Lab lab1 = from.Lab();
			Lab lab2 = to.Lab();
			float targetL = lerp(lab1.L(), lab2.L(), t);

			// Convert to Lab to adjust lightness while preserving hue/chroma direction.
			Lab currentLab = from.LinearRGB().lerp(to.LinearRGB(), t).RGB().Lab();

			// Use target L* but preserve a,b ratios (color direction).
			yield new Lab(targetL, currentLab.a(), currentLab.b()).RGB();
		}
		case "Oklab" -> from.Oklab().lerp(to.Oklab(), t).RGB();
		case "Oklab+L" -> {
			// Get target L in Oklab space.
			Oklab ok1 = from.Oklab();
			Oklab ok2 = to.Oklab();
			float targetL = lerp(ok1.L(), ok2.L(), t);

			// Convert to Oklab to adjust lightness while preserving hue/chroma direction.
			Oklab currentOklab = from.LinearRGB().lerp(to.LinearRGB(), t).RGB().Oklab();

			// Use target L* but preserve a,b ratios (color direction).
			yield new Oklab(targetL, currentOklab.a(), currentOklab.b()).RGB();
		}
		case "Oklch" -> from.Oklch().lerp(to.Oklch(), t).RGB();
		case "Okhsv" -> from.Okhsv().lerp(to.Okhsv(), t).RGB();
		case "HSV" -> from.HSV().lerp(to.HSV(), t).RGB();
		case "HSL" -> from.HSL().lerp(to.HSL(), t).RGB();
		case "Lab" -> from.Lab().lerp(to.Lab(), t).RGB();
		case "HCT" -> from.HCT().lerp(to.HCT(), t).RGB();
		case "CAM16" -> from.CAM16().lerp(to.CAM16(), t).RGB();
		case "CAM16UCS" -> {
			CAM16UCS c1 = from.CAM16().CAM16UCS();
			CAM16UCS c2 = to.CAM16().CAM16UCS();
			yield c1.lerp(c2, t).CAM16(CAM16.VC.sRGB).RGB();
		}
		case "HSLuv" -> from.HSLuv().lerp(to.HSLuv(), t).RGB();
		case "Luv" -> from.Luv().lerp(to.Luv(), t).RGB();
		case "LCh" -> from.LCh().lerp(to.LCh(), t).RGB();
		case "XYZ" -> from.XYZ().lerp(to.XYZ(), t).RGB();
		case "ITP" -> from.ITP().lerp(to.ITP(), t).RGB();
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

		config.gradients.add(new Gradient(new RGB(0xe93227), new RGB(0x0422f3))); // Red to Blue
		config.gradients.add(new Gradient(new RGB(0xe93824), new RGB(0x80f54b))); // Orange to Light Green
		config.gradients.add(new Gradient(new RGB(0x0239de), new RGB(0xf9fc59))); // Blue to Yellow
		config.gradients.add(new Gradient(new RGB(0x002fe8), new RGB(0x75f656))); // Blue to Green
		config.gradients.add(new Gradient(new RGB(0xffffff), new RGB(0x000000))); // White to Black

		new Gradients(config);
	}
}
