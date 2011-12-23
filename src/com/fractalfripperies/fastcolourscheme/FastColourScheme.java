package com.fractalfripperies.fastcolourscheme;

public interface FastColourScheme {
	public int getRGB(int iterations);
	public int getSupersampledRGB(int i1, int i2, int i3, int i4);
}