package com.fractalfripperies.fractal;

import com.fractalfripperies.CalculationThread;

public final class MandelbrotK implements Fractal {
	private final double k;
	private static final double CUTOFF = 4.0;
	private static final double REAL_START = 0;
	private static final double IMAGINARY_START = 0;
	
	public MandelbrotK(double k) {
		this.k = k;
	}
	
	public int iterate(final double realPos, final double imaginaryPos, final int maxIterations) {
		double angle;
		double radius;
		double real = REAL_START;
		double imaginary = IMAGINARY_START;
		int iterations = 0;
		while (iterations < maxIterations && (real*real + imaginary*imaginary) < CUTOFF) {
			// Convert to polar
			angle = getAngle(real, imaginary);
			radius = getRadius(real, imaginary);
			
			// Exponentiate
			angle *= k;
			radius = Math.pow(radius, k);
			
			// Convert to cartesian
			real = getReal(angle, radius);
			imaginary = getImaginary(angle, radius);
			
			// Add c
			real += realPos;
			imaginary += imaginaryPos;
			
			iterations++;
		}
		
		return iterations == maxIterations ? CalculationThread.IN_SET : iterations;
	}
	
	public static final double getAngle(double real, double imaginary) {
		return Math.atan2(imaginary, real);
		//return Math.atan(imaginary / real);
	}
	
	public static final double getRadius(double real, double imaginary) {
		return Math.hypot(real, imaginary);
	}
	
	public static final double getReal(double angle, double radius) {
		return radius * Math.cos(angle);
	}
	
	public static final double getImaginary(double angle, double radius) {
		return radius * Math.sin(angle);
	}
	
	public String toString() { return "Mandelbrot " + k; }
	public boolean equals(Object o) { return o instanceof MandelbrotK; }
	
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