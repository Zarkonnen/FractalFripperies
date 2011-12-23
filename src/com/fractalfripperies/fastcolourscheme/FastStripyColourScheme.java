package com.fractalfripperies.fastcolourscheme;

import com.fractalfripperies.CalculationThread;

public final class FastStripyColourScheme implements FastColourScheme {
	private static final int WHITE = 16777215;
	private static final int GRAY =  12632256;
	
	public int getRGB(int iterations) {
		switch (iterations) {
			case CalculationThread.IN_SET: return WHITE;
			case CalculationThread.NO_VALUE: return GRAY;
			default: {
				return	((iterations * 16) % 255) +
						256 * (iterations % 255) +
						65536 * ((iterations / 16) % 255);
			}
		}
	}
	
	public int getSupersampledRGB(int i1, int i2, int i3, int i4) {
		return
			((
				(i1 == CalculationThread.IN_SET ? 255 : (i1 == CalculationThread.NO_VALUE ? 192 : ((i1 * 16) % 255))) +
				(i2 == CalculationThread.IN_SET ? 255 : (i2 == CalculationThread.NO_VALUE ? 192 : ((i2 * 16) % 255))) +
				(i3 == CalculationThread.IN_SET ? 255 : (i3 == CalculationThread.NO_VALUE ? 192 : ((i3 * 16) % 255))) +
				(i4 == CalculationThread.IN_SET ? 255 : (i4 == CalculationThread.NO_VALUE ? 192 : ((i4 * 16) % 255)))
			) / 4) +
			((
				(i1 == CalculationThread.IN_SET ? 255 : (i1 == CalculationThread.NO_VALUE ? 192 : (i1 % 255))) +
				(i2 == CalculationThread.IN_SET ? 255 : (i2 == CalculationThread.NO_VALUE ? 192 : (i2 % 255))) +
				(i3 == CalculationThread.IN_SET ? 255 : (i3 == CalculationThread.NO_VALUE ? 192 : (i3 % 255))) +
				(i4 == CalculationThread.IN_SET ? 255 : (i4 == CalculationThread.NO_VALUE ? 192 : (i4 % 255)))
			) / 4) * 256 +
			((
				(i1 == CalculationThread.IN_SET ? 255 : (i1 == CalculationThread.NO_VALUE ? 192 : ((i1 / 16) % 255))) +
				(i2 == CalculationThread.IN_SET ? 255 : (i2 == CalculationThread.NO_VALUE ? 192 : ((i2 / 16) % 255))) +
				(i3 == CalculationThread.IN_SET ? 255 : (i3 == CalculationThread.NO_VALUE ? 192 : ((i3 / 16) % 255))) +
				(i4 == CalculationThread.IN_SET ? 255 : (i4 == CalculationThread.NO_VALUE ? 192 : ((i4 / 16) % 255)))
			) / 4) * 65536
		;
	}
	
	public String toString() { return "Stripes"; }
	public boolean equals(Object o) { return o instanceof FastStripyColourScheme; }
}