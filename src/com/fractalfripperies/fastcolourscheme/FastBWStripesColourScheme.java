package com.fractalfripperies.fastcolourscheme;

import com.fractalfripperies.CalculationThread;

public final class FastBWStripesColourScheme implements FastColourScheme {
	private static final int BLACK = 0;
	private static final int GRAY =  4210752;
	private static final int WHITE = 255 * 1 + 255 * 256 + 255 * 65536;
	private static final int PERIOD = 16;
	
	public int getRGB(int iterations) {
		return (iterations / PERIOD) % 2 * WHITE;
	}
	
	public int getSupersampledRGB(int i1, int i2, int i3, int i4) {
		return
			((
				((i1 / PERIOD) % 2) * 255 +
				((i2 / PERIOD) % 2) * 255 +
				((i3 / PERIOD) % 2) * 255 +
				((i4 / PERIOD) % 2) * 255
			) / 4) * (1 + 256 + 65536);
	}
	
	public String toString() { return "Black & White Stripes"; }
	public boolean equals(Object o) { return o instanceof FastBWStripesColourScheme; }
}