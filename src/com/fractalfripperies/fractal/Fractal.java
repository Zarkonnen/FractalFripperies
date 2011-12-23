package com.fractalfripperies.fractal;

public interface Fractal {
	public int iterate(double realPos, double imaginaryPos, int maxIterations);
	public double realStart();
	public double imaginaryStart();
	public double realWidth();
	public double imaginaryHeight();
}