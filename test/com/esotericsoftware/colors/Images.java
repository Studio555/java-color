
package com.esotericsoftware.colors;

import static com.esotericsoftware.colors.Colors.*;

import java.io.File;

import javax.imageio.ImageIO;

import com.esotericsoftware.colors.Colors.RGB;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Images {
	public Images () throws Throwable {
		var image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
		var gr = (Graphics2D)image.getGraphics();
		gr.clearRect(0, 0, image.getWidth(), image.getHeight());
		RGB from = new RGB(1, 0, 0);
		RGB to = new RGB(0, 0, 1);
		for (int x = 0; x < 100; x++) {
			float a = x / 100f;
			float r = lerp(from.r(), to.r(), a);
			float g = lerp(from.g(), to.g(), a);
			float b = lerp(from.b(), to.b(), a);
			var color = new Color(r, g, b);
			gr.setColor(color);
			gr.drawRect(x, 0, 1, 100);
		}
		ImageIO.write(image, "png", new File("gradients.png"));
	}

	@SuppressWarnings("unused")
	static public void main (String[] args) throws Throwable {
		new Images();
	}
}
