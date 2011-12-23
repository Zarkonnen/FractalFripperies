package com.fractalfripperies.fastcolourscheme;

import com.fractalfripperies.CalculationThread;

public class BrownAndGreen extends SimpleFastColourScheme {
	public final int getR(int iterations) {
		return iterations == CalculationThread.IN_SET ? 0 : Math.min(127, (iterations % 15) * 16 + 83);
	}
	
	public final int getG(int iterations) {
		return iterations == CalculationThread.IN_SET ? 31 : Math.min(255, Math.max(0, ((iterations / 16 % 127) + 127 - (iterations % 16) * 4) + 83));
	}
	
	public int getB(int iterations) {
		return iterations == CalculationThread.IN_SET ? 0 : 115;
	}
	
	public String toString() { return "Sea Creature"; }
	public boolean equals(Object o) { return o instanceof BrownAndGreen; }
}