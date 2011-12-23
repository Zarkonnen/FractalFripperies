package com.fractalfripperies;

import com.fractalfripperies.fastcolourscheme.*;
import com.fractalfripperies.fractal.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;


public class SimpleFractalViewApplet extends JApplet {
	private FractalPanel p;
	public static final int WIDTH = 320;
	public static final int HEIGHT = 320;
	public static final int SMALL_WIDTH = 84;
	public static final int SMALL_HEIGHT = 84;
	private boolean blackBG;
	
	private static final HashMap<Object, Object> RENDERING_HINTS = new HashMap<Object, Object>();
	static {
		RENDERING_HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	
	public void init() {
		
		setLayout(null);
				
		if (getParameter("fractal") == null) {
			add(p = new FractalPanel(blackBG ? SMALL_WIDTH : WIDTH, blackBG ? SMALL_HEIGHT : HEIGHT, new Mandelbrot(), new FastStripyColourScheme()));
		} else {
			try {
				FractalInfo i = new FractalInfo();
				i.fractal = (Fractal) Class.forName(getParameter("fractal")).newInstance();
				i.colourScheme = (FastColourScheme) Class.forName(getParameter("colourScheme")).newInstance();
				i.realStart = Double.parseDouble(getParameter("realStart"));
				i.imaginaryStart = Double.parseDouble(getParameter("imaginaryStart"));
				i.realWidth = Double.parseDouble(getParameter("realWidth"));
				i.imaginaryHeight = Double.parseDouble(getParameter("imaginaryHeight"));
				blackBG = Boolean.valueOf(getParameter("black"));
				
				javax.swing.JOptionPane.showMessageDialog(null, "<html>realStart: " + i.realStart + "<br>" +
						"imaginaryStart: " + i.imaginaryStart + "<br>realWidth: " + i.realWidth + "<br>" + 
						"imaginaryHeight: " + i.imaginaryHeight + "</html>");
				
				add(p = new FractalPanel(blackBG ? SMALL_WIDTH : WIDTH, blackBG ? SMALL_HEIGHT : HEIGHT, i));
			}
			catch (Exception e) {
				e.printStackTrace();
				add(p = new FractalPanel(blackBG ? SMALL_WIDTH : WIDTH, blackBG ? SMALL_HEIGHT : HEIGHT, new Mandelbrot(), new FastStripyColourScheme()));
			}
		}
		
		if (blackBG) {
			p.setBounds(8, 0, SMALL_WIDTH, SMALL_HEIGHT);
		} else {
			p.setBounds(0, 0, WIDTH, HEIGHT);
		}
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		
		if (blackBG) {
			if (g instanceof Graphics2D) {
				((Graphics2D) g).addRenderingHints(RENDERING_HINTS);
			}
			
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, 8, HEIGHT);
			g.fillRect(92, 0, 8, HEIGHT);
			g.fillRect(0, 84, WIDTH, 16);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Times", Font.PLAIN, 9));
			FontMetrics fm = g.getFontMetrics();
			int offset = (WIDTH - fm.stringWidth("www.fractalfripperies.com")) / 2;
			g.drawString("www.fractalfripperies.com", offset, 95);
		}
	}
	
	public void destroy() {
		p.destroy();
		p = null;
		System.gc();
	}
}