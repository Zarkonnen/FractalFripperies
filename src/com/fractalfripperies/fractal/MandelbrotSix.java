package com.fractalfripperies.fractal;

import com.fractalfripperies.CalculationThread;

public final class MandelbrotSix implements Fractal {
	private static final double CUTOFF = 4.0;
	private static final double REAL_START = 0;
	private static final double IMAGINARY_START = 0;
	
	public int iterate(final double realPos, final double imaginaryPos, final int maxIterations) {
		double tempReal;
		double real = REAL_START;
		double imaginary = IMAGINARY_START;
		int iterations = 0;
		while (iterations < maxIterations && (real*real + imaginary*imaginary) < CUTOFF) {
			tempReal =
				real * real * real * real * real * real
				- 15 * real * real * real * real * imaginary * imaginary
				+ 15 * real * real * imaginary * imaginary * imaginary * imaginary
				- imaginary * imaginary * imaginary * imaginary * imaginary * imaginary
				+ realPos;
			imaginary = 
				6 * real * real * real * real * real * imaginary
				- 20 * real * real * real * imaginary * imaginary * imaginary
				+ 6 * real * imaginary * imaginary * imaginary * imaginary * imaginary
				+ imaginaryPos;
			real = tempReal;
			iterations++;
		}
		
		return iterations == maxIterations ? CalculationThread.IN_SET : iterations;
	}
	
	public String toString() { return "Mandelbrot Six"; }
	public boolean equals(Object o) { return o instanceof MandelbrotSix; }
	
	public double realStart() {
		return -2.0;
	}
	public double imaginaryStart() {
		return -2.0;
	}
	public double realWidth() {
		return 4.0;
	}
	public double imaginaryHeight() {
		return 4.0;
	}
}