package com.fractalfripperies.fastcolourscheme;

import com.fractalfripperies.CalculationThread;

public abstract class SimpleFastColourScheme implements FastColourScheme {
	public abstract int getR(int iterations);
	
	public abstract int getG(int iterations);
	
	public abstract int getB(int iterations);
	
	public final int getRGB(int iterations) {
		return getB(iterations) + 256 * getG(iterations) + 65536 * getR(iterations);
	}
	
	public final int getSupersampledRGB(int i1, int i2, int i3, int i4) {
		return
			((getB(i1) + getB(i2) + getB(i3) + getB(i4)) / 4) +
			((getG(i1) + getG(i2) + getG(i3) + getG(i4)) / 4) * 256 +
			((getR(i1) + getR(i2) + getR(i3) + getR(i4)) / 4) * 65536;
	}
}