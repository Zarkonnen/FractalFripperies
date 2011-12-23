package com.fractalfripperies;

import com.fractalfripperies.fastcolourscheme.*;
import com.fractalfripperies.fractal.*;
import javax.swing.*;
import java.awt.BorderLayout;

public class FractalApplet extends JApplet {
	private FractalViewer viewer;
	
	public void init() {		
		setLayout(new BorderLayout());
		
		if (getParameter("fractal") == null) {
			add(viewer = new FractalViewer(this), BorderLayout.CENTER);
		} else {
			try {
				FractalInfo i = new FractalInfo();
				i.fractal = (Fractal) Class.forName(getParameter("fractal")).newInstance();
				i.colourScheme = (FastColourScheme) Class.forName(getParameter("colourScheme")).newInstance();
				i.realStart = Double.parseDouble(getParameter("realStart"));
				i.imaginaryStart = Double.parseDouble(getParameter("imaginaryStart"));
				i.realWidth = Double.parseDouble(getParameter("realWidth"));
				i.imaginaryHeight = Double.parseDouble(getParameter("imaginaryHeight"));
				add(viewer = new FractalViewer(i, this), BorderLayout.CENTER);
			}
			catch (Exception e) {
				e.printStackTrace();
				add(viewer = new FractalViewer(this), BorderLayout.CENTER);
			}
		}
	}
	
	public void destroy() {
		viewer.destroy();
		viewer = null;
		System.gc();
	}
}