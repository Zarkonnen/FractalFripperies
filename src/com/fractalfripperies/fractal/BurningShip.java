package com.fractalfripperies.fractal;

import com.fractalfripperies.CalculationThread;

public final class BurningShip implements Fractal {
	private static final double CUTOFF = 4.0;
	private static final double REAL_START = 0;
	private static final double IMAGINARY_START = 0;
	
	public int iterate(final double realPos, final double imaginaryPos, final int maxIterations) {
		double tempReal;
		double real = REAL_START;
		double imaginary = IMAGINARY_START;
		int iterations = 0;
		while (iterations < maxIterations && (real*real + imaginary*imaginary) < CUTOFF) {
			tempReal = real*real - imaginary*imaginary - realPos;
			imaginary = Math.abs(2 * real * imaginary) + imaginaryPos;
			real = tempReal;
			iterations++;
		}
		
		return iterations == maxIterations ? CalculationThread.IN_SET : iterations;
	}
	
	public String toString() { return "Burning Ship"; }
	public boolean equals(Object o) { return o instanceof BurningShip; }
	
	public double realStart() {
		return 1.698046875;
	}
	public double imaginaryStart() {
		return -0.08984375;
	}
	public double realWidth() {
		return 0.125;
	}
	public double imaginaryHeight() {
		return 0.125;
	}
}