package com.fractalfripperies.fastcolourscheme;

import com.fractalfripperies.CalculationThread;

public class OffsetStripes extends SimpleFastColourScheme {
	private static final int BLACK = 0;
	private static final int GRAY =  4210752;
	
	public final int getR(int iterations) {
		return iterations == CalculationThread.IN_SET ? 0 : (iterations / 13) % 2 * 255;
	}
	
	public final int getG(int iterations) {
		return iterations == CalculationThread.IN_SET ? 0 : (iterations / 17) % 2 * 255;
	}
	
	public int getB(int iterations) {
		return 0;//return iterations == CalculationThread.IN_SET ? 0 : (iterations / 23) % 2 * 255;
	}
	
	public String toString() { return "Offset Stripes"; }
	public boolean equals(Object o) { return o instanceof OffsetStripes; }
}