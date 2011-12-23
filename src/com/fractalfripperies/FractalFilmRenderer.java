package com.fractalfripperies;

import com.fractalfripperies.fractal.*;
import com.fractalfripperies.fastcolourscheme.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import java.io.File;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Color;


public class FractalFilmRenderer {
	/**
	 * Expected args:
	 * 0: image file path without .jpg
	 * 1: image width
	 * 2: image height
	 * 3: from
	 * 4: to
	 * 5: step
	*/
	public static void main(String[] args) {		
		try {
			String filePath = args[0];
			int imageWidth = Integer.parseInt(args[1]);
			int imageHeight = Integer.parseInt(args[2]);
			double from = Double.parseDouble(args[3]);
			double to = Double.parseDouble(args[4]);
			double step = Double.parseDouble(args[5]);
			FastColourScheme colourScheme = new FastStripyColourScheme();
		
			double k = from;
			while (k < (to + step)) {
				CalculationThread t = new CalculationThread(new MandelbrotK(k), colourScheme, imageWidth, imageHeight);
				BufferedImage mainImage = t.fullRender(-2.0, -2.0, 4.0, 4.0, false);

				// Free up memory.
				t = null;

				int imageNumber = (int) (k * 10000);

				writeImage(mainImage, new File(filePath + " " + imageNumber + ".jpg"));
				
				k += step;
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.out); //qqDPS For seeing what's going wrong in PHP.
		}
	}
	
	public static void writeImage(BufferedImage image, File file) throws Exception {
		IIOImage iioImage = new IIOImage(image, null, null);
		FileImageOutputStream s = new FileImageOutputStream(file);
		ImageWriter iw = ImageIO.getImageWritersByFormatName("jpeg").next();
		JPEGImageWriteParam p = (JPEGImageWriteParam) iw.getDefaultWriteParam();
		p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		for (float f : p.getCompressionQualityValues()) {
			if (p.getCompressionQuality() < f) {
				p.setCompressionQuality(f);
			}
		}
		iw.setOutput(s);
		iw.write(null, iioImage, p);
		s.close();
	}
}