package com.fractalfripperies.fastcolourscheme;

import com.fractalfripperies.CalculationThread;

public class Flame extends SimpleFastColourScheme {
	public final int getR(int iterations) {
		return iterations == CalculationThread.IN_SET ? 255 : Math.min(255, iterations * 8);
	}
	
	public final int getG(int iterations) {
		return iterations == CalculationThread.IN_SET ? 255 : Math.min(255, iterations * 2);
	}
	
	public int getB(int iterations) {
		return iterations == CalculationThread.IN_SET ? 255 : Math.min(255, iterations / 32);
	}
	
	public String toString() { return "Flame"; }
	public boolean equals(Object o) { return o instanceof Flame; }
}