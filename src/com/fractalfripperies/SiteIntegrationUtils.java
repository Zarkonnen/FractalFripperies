package com.fractalfripperies;

import java.net.URL;

public class SiteIntegrationUtils {
	public static final String MAKE_SHIRT_URL = "http://www.fractalfripperies.com/uk/selectshirt.php";
	//"http://localhost/fractalfripperies/selectshirt.php";
	public static final String FRACTAL_URL = "http://www.fractalfripperies.com/uk/";
	//"http://localhost/fractalfripperies/";
	
	public static final String PRICE = "\u20a418 + \u20a45 postage";//"$29 + $7 postage";
	
	public static URL getMakeShirtURL(FractalInfo fi) {
		try {
			return new URL
			(
				MAKE_SHIRT_URL
				+ "?fractal=" + fi.fractal.getClass().getName()
				+ "&colourScheme=" + fi.colourScheme.getClass().getName()
				+ "&realStart=" + fi.realStart
				+ "&imaginaryStart=" + fi.imaginaryStart
				+ "&realWidth=" + fi.realWidth
				+ "&imaginaryHeight=" + fi.imaginaryHeight
			);
		}
		catch (java.net.MalformedURLException e) { return null; }
	}
	
	public static URL getFractalURL(FractalInfo fi) {
		try {
			return new URL
			(
				FRACTAL_URL
				+ "?fractal=" + fi.fractal.getClass().getName()
				+ "&colourScheme=" + fi.colourScheme.getClass().getName()
				+ "&realStart=" + fi.realStart
				+ "&imaginaryStart=" + fi.imaginaryStart
				+ "&realWidth=" + fi.realWidth
				+ "&imaginaryHeight=" + fi.imaginaryHeight
			);
		}
		catch (java.net.MalformedURLException e) { return null; }
	}
}