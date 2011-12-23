package com.fractalfripperies;

import com.fractalfripperies.fractal.*;
import com.fractalfripperies.fastcolourscheme.*;

public class FractalInfo {
	public Fractal fractal;
	public FastColourScheme colourScheme;
	public double realStart;
	public double imaginaryStart;
	public double realWidth;
	public double imaginaryHeight;
	
	public FractalInfo() {}
	public FractalInfo(Fractal fractal, FastColourScheme colourScheme, double realStart,
			double imaginaryStart, double realWidth, double imaginaryHeight)
	{
		this.fractal = fractal; this.colourScheme = colourScheme; this.realStart = realStart;
		this.imaginaryStart = imaginaryStart; this.realWidth = realWidth;
		this.imaginaryHeight = imaginaryHeight;
	}
}