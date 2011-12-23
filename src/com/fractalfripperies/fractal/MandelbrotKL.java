package com.fractalfripperies.fractal;

import com.fractalfripperies.CalculationThread;

public final class MandelbrotKL implements Fractal {
	private static final double CUTOFF = 4.0;
	private static final double REAL_START = 0;
	private static final double IMAGINARY_START = 0;
	
	private double k;
	private double l;
	
	public MandelbrotKL(double k, double l) {
		this.k = k;
		this.l = l;
	}
	
	public int iterate(final double realPos, final double imaginaryPos, final int maxIterations) {
		double real = realPos;
		double imaginary = imaginaryPos;
		int iterations = 0;
		while (iterations < maxIterations && (real * real + imaginary * imaginary) < CUTOFF) {
			double atan = Math.atan(imaginary / real);
			
			if (real < 0 && imaginary < 0) {
				atan -= Math.PI;
			}
			
			if (real > 0 && imaginary > 0) {
				atan += Math.PI;
			}
						
			double radius = Math.pow(real*real + imaginary*imaginary, k / 2) *
				Math.pow(Math.E, -l * atan);
						
			double angle = k * atan + 0.5 * l * Math.log(real * real + imaginary * imaginary);
			
			real = radius * Math.cos(angle) + realPos;
			imaginary = radius * Math.sin(angle) + imaginaryPos;
			
			iterations++;
		}
		
		return iterations == maxIterations ? CalculationThread.IN_SET : iterations;
	}
	
	public String toString() { return l < 0 ? ("Mandelbrot " + k + " - " + (-l) + "i") : ("Mandelbrot " + k + " + " + l + "i"); }
	public boolean equals(Object o) { return o instanceof MandelbrotKL; }
	
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