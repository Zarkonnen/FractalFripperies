package com.fractalfripperies.fastcolourscheme;

import com.fractalfripperies.CalculationThread;

public class FastStripyThreeColourScheme extends SimpleFastColourScheme {
	private static final int BLACK = 0;
	private static final int GRAY =  4210752;
	
	public final int getR(int iterations) {
		return iterations == CalculationThread.IN_SET ? 255 : Math.min(255, iterations / 32 * ((iterations * 5) % 255));
	}
	
	public final int getG(int iterations) {
		return iterations == CalculationThread.IN_SET ? 255 : (iterations * 5) % 255;
	}
	
	public int getB(int iterations) {
		return iterations == CalculationThread.IN_SET ? 255 : (iterations * 17) % 255;
	}
	
	public String toString() { return "Psychedelic Stripes"; }
	public boolean equals(Object o) { return o instanceof FastStripyThreeColourScheme; }
}