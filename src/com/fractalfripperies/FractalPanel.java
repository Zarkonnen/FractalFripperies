package com.fractalfripperies;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Image;
import java.awt.Graphics;
import com.fractalfripperies.fastcolourscheme.*;
import com.fractalfripperies.fractal.*;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.DisplayMode;

public class FractalPanel extends JPanel implements CalculationThread.RepaintListener {
	public CalculationThread calculationThread;
	
	public int width;
	public int height;
	
	public FractalPanel(int width, int height, Fractal fractal, FastColourScheme colourScheme) {
		this.width = width;
		this.height = height;
		setSize(width, height);
		// qqDPS Rounding width/height to CalculationThread.INITIAL_GRAIN.
		int grainNeeded = CalculationThread.INITIAL_GRAIN * 2;
		width = width % grainNeeded == 0
			? width
			: (width / grainNeeded + 1) * grainNeeded;
		height = height % grainNeeded == 0
			? height
			: (height / grainNeeded + 1) * grainNeeded;
		calculationThread = new CalculationThread(fractal, colourScheme, width, height);
		calculationThread.repaintListeners.add(this);
		calculationThread.start();
	}
	
	public FractalPanel(int width, int height, FractalInfo info) {
		this.width = width;
		this.height = height;
		setSize(width, height);
		// qqDPS Rounding width/height to CalculationThread.INITIAL_GRAIN.
		int grainNeeded = CalculationThread.INITIAL_GRAIN * 2;
		width = width % grainNeeded == 0
			? width
			: (width / grainNeeded + 1) * grainNeeded;
		height = height % grainNeeded == 0
			? height
			: (height / grainNeeded + 1) * grainNeeded;
		calculationThread = new CalculationThread(info, width, height);
		calculationThread.repaintListeners.add(this);
		calculationThread.start();
	}
	
	public void paint(Graphics g) {
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(calculationThread.getImage(), (getWidth() - width) / 2, 0, null);
	}
	
	public void destroy() {
		calculationThread.shutdown();
		calculationThread = null;
	}
}