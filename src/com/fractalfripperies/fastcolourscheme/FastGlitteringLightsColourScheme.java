package com.fractalfripperies.fastcolourscheme;

import com.fractalfripperies.CalculationThread;

public final class FastGlitteringLightsColourScheme implements FastColourScheme {
	private static final int BLACK = 0;
	private static final int GRAY =  4210752;
	
	public int getRGB(int iterations) {
		switch (iterations) {
			case CalculationThread.IN_SET: return BLACK;
			case CalculationThread.NO_VALUE: return GRAY;
			default: {
				return	Math.min(255, 768 / (iterations % 32 + 1)) +
						256 * Math.min(255, 384 / (iterations % 128 + 1)) +
						65536 * Math.min(255, 192 / (iterations % 64 + 1));
			}
		}
	}
	
	public int getSupersampledRGB(int i1, int i2, int i3, int i4) {
		return
			((
				(i1 == CalculationThread.IN_SET ? 0 : (i1 == CalculationThread.NO_VALUE ? 64 : Math.min(255, 768 / (i1 % 32 + 1)))) +
				(i2 == CalculationThread.IN_SET ? 0 : (i2 == CalculationThread.NO_VALUE ? 64 : Math.min(255, 768 / (i2 % 32 + 1)))) +
				(i3 == CalculationThread.IN_SET ? 0 : (i3 == CalculationThread.NO_VALUE ? 64 : Math.min(255, 768 / (i3 % 32 + 1)))) +
				(i4 == CalculationThread.IN_SET ? 0 : (i4 == CalculationThread.NO_VALUE ? 64 : Math.min(255, 768 / (i4 % 32 + 1))))
			) / 4) +
			((
				(i1 == CalculationThread.IN_SET ? 0 : (i1 == CalculationThread.NO_VALUE ? 64 : Math.min(255, 384 / (i1 % 128 + 1)))) +
				(i2 == CalculationThread.IN_SET ? 0 : (i2 == CalculationThread.NO_VALUE ? 64 : Math.min(255, 384 / (i2 % 128 + 1)))) +
				(i3 == CalculationThread.IN_SET ? 0 : (i3 == CalculationThread.NO_VALUE ? 64 : Math.min(255, 384 / (i3 % 128 + 1)))) +
				(i4 == CalculationThread.IN_SET ? 0 : (i4 == CalculationThread.NO_VALUE ? 64 : Math.min(255, 384 / (i4 % 128 + 1))))
			) / 4) * 256 +
			((
				(i1 == CalculationThread.IN_SET ? 0 : (i1 == CalculationThread.NO_VALUE ? 64 : Math.min(255, 192 / (i1 % 64 + 1)))) +
				(i2 == CalculationThread.IN_SET ? 0 : (i2 == CalculationThread.NO_VALUE ? 64 : Math.min(255, 192 / (i2 % 64 + 1)))) +
				(i3 == CalculationThread.IN_SET ? 0 : (i3 == CalculationThread.NO_VALUE ? 64 : Math.min(255, 192 / (i3 % 64 + 1)))) +
				(i4 == CalculationThread.IN_SET ? 0 : (i4 == CalculationThread.NO_VALUE ? 64 : Math.min(255, 192 / (i4 % 64 + 1))))
			) / 4) * 65536
		;
	}
	
	public String toString() { return "Glittering Lights"; }
	public boolean equals(Object o) { return o instanceof FastGlitteringLightsColourScheme; }
}