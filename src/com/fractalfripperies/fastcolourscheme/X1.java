package com.fractalfripperies.fastcolourscheme;

import com.fractalfripperies.CalculationThread;

public class X1 extends SimpleFastColourScheme {
	private static final int BLACK = 0;
	private static final int GRAY =  4210752;
	
	public final int getR(int iterations) {
		return iterations == CalculationThread.IN_SET
			? 0
			: iterations % 16 == 0
			? 0
			: Math.max(0, (int) (Math.sin((iterations + 10)  / 32) * (255 + 64)) - 64);
	}
	
	public final int getG(int iterations) {
		return 0; //return iterations == CalculationThread.IN_SET ? 0 : (iterations % 16 == 0 ? 255 : 0);
	}
	
	public int getB(int iterations) {
		return iterations == CalculationThread.IN_SET ? 0 : (iterations % 16 == 0 ? 255 : 0);
	}
	
	public String toString() { return "Electric Coral"; }
	public boolean equals(Object o) { return o instanceof X1; }
}