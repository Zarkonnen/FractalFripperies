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
import java.util.HashMap;


public class FullFractalRenderer {
	/**
	 * Expected args:
	 * 0: image file path, .jpg
	 * 1: image width
	 * 2: image height
	 * 3: full classname of fractal
	 * 4: full classname of colour scheme
	 * 5: realStart
	 * 6: imaginaryStart
	 * 7: realWidth
	 * 8: imaginaryHeight
	 * 9: black background (true/false)
	*/
	public static void main(String[] args) {
		if (args.length < 10) {
			System.out.println("Usage:\n" +
			 "0: image file path, .jpg\n" +
			 "1: image width\n" +
			 "2: image height\n" +
			 "3: full classname of fractal\n" +
			 "4: full classname of colour scheme\n" +
			 "5: realStart\n" +
			 "6: imaginaryStart\n" +
			 "7: realWidth\n" +
			 "8: imaginaryHeight\n" +
			 "9: black background (true/false/thumb)"
			);
			return;
		}
		
		try {
			String filePath = args[0];
			int imageWidth = Integer.parseInt(args[1]);
			int imageHeight = Integer.parseInt(args[2]);
			Fractal fractal = (Fractal) Class.forName(args[3]).newInstance();
			FastColourScheme colourScheme = (FastColourScheme) Class.forName(args[4]).newInstance();
			double realStart = Double.parseDouble(args[5]);
			double imaginaryStart = Double.parseDouble(args[6]);
			double realWidth = Double.parseDouble(args[7]);
			double imaginaryHeight = Double.parseDouble(args[8]);
			boolean blackBackground = args[9].equals("true");
			boolean thumb = args[9].equals("thumb");
		
			CalculationThread t = new CalculationThread(fractal, colourScheme, imageWidth, imageHeight);
			BufferedImage mainImage = t.fullRender(realStart, imaginaryStart, realWidth, imaginaryHeight, thumb);
			
			// Free up memory.
			t = null;
			if (blackBackground) {
				deriveBlackImage(mainImage, new File(filePath));
			} else {
				writeImage(mainImage, new File(filePath));
			
				if (!thumb) {
					writeBackImage(new File(filePath + ".back.jpg"), fractal, realStart, imaginaryStart, realWidth, imaginaryHeight, blackBackground);
				}
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
	
	public static void writePng(BufferedImage image, File file) throws Exception {
		IIOImage iioImage = new IIOImage(image, null, null);
		FileImageOutputStream s = new FileImageOutputStream(file);
		ImageWriter iw = ImageIO.getImageWritersByFormatName("png").next();
		iw.setOutput(s);
		iw.write(iioImage);
		s.close();
	}
	
	private static final HashMap<Class, File> IMAGE_LOCATIONS = new HashMap<Class, File>();
	
	static {
		IMAGE_LOCATIONS.put(Mandelbrot.class, new File("backs/mandelbrot.jpg"));
		IMAGE_LOCATIONS.put(MandelbrotThree.class, new File("backs/mandelbrot3.jpg"));
		IMAGE_LOCATIONS.put(MandelbrotFour.class, new File("backs/mandelbrot4.jpg"));
		IMAGE_LOCATIONS.put(MandelbrotFive.class, new File("backs/mandelbrot5.jpg"));
		IMAGE_LOCATIONS.put(MandelbrotSix.class, new File("backs/mandelbrot6.jpg"));
		IMAGE_LOCATIONS.put(Mandelbar.class, new File("backs/mandelbar.jpg"));
		IMAGE_LOCATIONS.put(BurningShip.class, new File("backs/burningship.jpg"));
	}
	
	private static final HashMap<Class, File> BLACK_IMAGE_LOCATIONS = new HashMap<Class, File>();
	
	static {
		BLACK_IMAGE_LOCATIONS.put(Mandelbrot.class, new File("backs/mandelbrot-black.jpg"));
		BLACK_IMAGE_LOCATIONS.put(MandelbrotThree.class, new File("backs/mandelbrot3-black.jpg"));
		BLACK_IMAGE_LOCATIONS.put(MandelbrotFour.class, new File("backs/mandelbrot4-black.jpg"));
		BLACK_IMAGE_LOCATIONS.put(MandelbrotFive.class, new File("backs/mandelbrot5-black.jpg"));
		BLACK_IMAGE_LOCATIONS.put(MandelbrotSix.class, new File("backs/mandelbrot6-black.jpg"));
		BLACK_IMAGE_LOCATIONS.put(Mandelbar.class, new File("backs/mandelbar-black.jpg"));
		BLACK_IMAGE_LOCATIONS.put(BurningShip.class, new File("backs/burningship-black.jpg"));
	}
	
	public static void deriveBlackImage(BufferedImage image, File file) throws Exception {
		BufferedImage extraImage = new BufferedImage(image.getWidth(), image.getHeight() + 210, BufferedImage.TYPE_INT_ARGB);
		Graphics g = extraImage.getGraphics();
		
		g.drawImage(image, 0, 0, null);
		
		g.setFont(new Font("Times", Font.PLAIN, 192));
		FontMetrics fm = g.getFontMetrics();
		
		g.setColor(Color.WHITE);
		g.drawString("www.fractalfripperies.com", image.getWidth() / 2 - fm.stringWidth("www.fractalfripperies.com") / 2, extraImage.getHeight() - 50);
		
		file = new File(file.getPath().substring(0, file.getPath().lastIndexOf('.')) + ".png");
		
		writePng(extraImage, file);
	}
	
	public static void writeBackImage(	File f,
										Fractal fractal,
										double realStart,
										double imaginaryStart,
										double realWidth,
										double imaginaryHeight,
										boolean blackBackground)
	{
		try {
			BufferedImage textImage = ImageIO.read(
					(blackBackground ? BLACK_IMAGE_LOCATIONS : IMAGE_LOCATIONS).get(fractal.getClass()));
			
			Graphics g = textImage.getGraphics();
			g.setFont(new Font("Times", Font.PLAIN, realWidth < 0.0000001 ? 32 : 64));
			g.setColor(blackBackground ? Color.WHITE : Color.BLACK);
			FontMetrics fm = g.getFontMetrics();
			double realEnd = realStart + realWidth;
			double imaginaryEnd = imaginaryStart + imaginaryHeight;
			
			// Now figure out how to display the four coordinates.
			int numberOfDigits = Double.toString(realWidth).length() + 3;
			
			String realStartStr = Double.toString(realStart);
			realStartStr = realStartStr.substring(0, Math.min(realStartStr.length(), numberOfDigits));
			String imaginaryStartStr = Double.toString(imaginaryStart);
			imaginaryStartStr = imaginaryStartStr.substring(0, Math.min(imaginaryStartStr.length(), numberOfDigits));
			imaginaryStartStr = imaginaryStart >= 0 ? "+" + imaginaryStartStr : imaginaryStartStr;
			
			String realEndStr = Double.toString(realEnd);
			realEndStr = realEndStr.substring(0, Math.min(realEndStr.length(), numberOfDigits));
			String imaginaryEndStr = Double.toString(imaginaryEnd);
			imaginaryEndStr = imaginaryEndStr.substring(0, Math.min(imaginaryEndStr.length(), numberOfDigits));
			imaginaryEndStr = imaginaryEnd >= 0 ? "+" + imaginaryEndStr : imaginaryEndStr;
			
			String coords = "(" + realStartStr + imaginaryStartStr + "i to " + realEndStr + imaginaryEndStr + "i)";
			g.drawString(coords, 1200 - fm.stringWidth(coords) / 2, 1350);
			
			writeImage(textImage, f);
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}