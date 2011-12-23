package com.fractalfripperies;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Image;
import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Graphics;
import com.fractalfripperies.fastcolourscheme.*;
import com.fractalfripperies.fractal.*;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.DisplayMode;
import java.applet.AppletContext;
import java.util.ArrayList;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.util.Timer;
import java.util.TimerTask;

public class FractalViewer extends JPanel implements CalculationThread.RepaintListener {
	public static final int VIEWER_WIDTH = 420;
	public static final int VIEWER_HEIGHT = 435;
	public static final int FRACTAL_WIDTH = 320;
	public static final int FRACTAL_HEIGHT = 320;
	
	public static final FastColourScheme[] COLOUR_SCHEMES = { new FastStripyColourScheme(), new FastGlitteringLightsColourScheme(), 
		new FastBWStripesColourScheme(), new FastStripyThreeColourScheme(), new X1(), new Flame(),
		new BrownAndGreen() };
	public static final String[] COLOUR_SCHEME_IMAGES = { "Stripes.jpg", "GlitteringLights.jpg", "BlackAndWhite.jpg",
		"Psychedelic.jpg", "Electric.jpg", "Flame.jpg", "SeaCreature.jpg" };
	public static final Fractal[] FRACTALS = { new Mandelbrot(), new MandelbrotThree(), new MandelbrotFour(), new MandelbrotFive(),
		new MandelbrotSix(), new BurningShip(), new Mandelbar() };
	public static final String[] FRACTAL_IMAGES = { "Mandelbrot.jpg", "Mandelbrot3.jpg", "Mandelbrot4.jpg", "Mandelbrot5.jpg",
		"Mandelbrot6.jpg", "BurningShip.jpg", "Mandelbar.jpg" };
	
	private EnhancedFractalPanel p;
	private JApplet applet;
	
	public FractalViewer(JApplet a) {
		p = new EnhancedFractalPanel(FRACTALS[0], COLOUR_SCHEMES[0]);
		init(a, FRACTALS[0], COLOUR_SCHEMES[0]);
	}
	
	public FractalViewer(FractalInfo info, JApplet a) {
		p = new EnhancedFractalPanel(info);
		init(a, info.fractal, info.colourScheme);
	}
	
	public void init(JApplet a, Fractal startFractal, FastColourScheme startColourScheme) {
		this.applet = a;
		setSize(VIEWER_WIDTH, VIEWER_HEIGHT);
		setLayout(null);
		setBackground(Color.WHITE);
		
		// Fractal panel.
		add(p);
		p.setBounds((VIEWER_WIDTH - FRACTAL_WIDTH) / 2, 0, FRACTAL_WIDTH, FRACTAL_HEIGHT);
		
		// Create fractal radio buttons
		ArrayList<ImageRadioButton> fButtons = new ArrayList<ImageRadioButton>();
		
		int offset = 0;
		for (int i = 0; i < FRACTALS.length; i++) {
			ImageRadioButton rb = new ImageRadioButton("images/" + FRACTAL_IMAGES[i]);
			add(rb);
			rb.setLocation(0, offset);
			offset += 45;
			fButtons.add(rb);
			rb.setOtherButtons(fButtons);
			final Fractal fractal = FRACTALS[i];
			rb.setCallback(new Runnable() { public void run() {
				p.calculationThread.setFractal(fractal);
			}});
			rb.setToolTipText(fractal.toString());
			
			if (fractal.getClass().equals(startFractal.getClass())) {
				rb.setSelected(true);
			}
		}
		
		// Create colour scheme radio buttons
		ArrayList<ImageRadioButton> cButtons = new ArrayList<ImageRadioButton>();
		
		offset = 0;
		for (int i = 0; i < COLOUR_SCHEMES.length; i++) {
			ImageRadioButton rb = new ImageRadioButton("images/" + COLOUR_SCHEME_IMAGES[i]);
			add(rb);
			rb.setLocation(VIEWER_WIDTH - rb.getWidth(), offset);
			offset += 45;
			cButtons.add(rb);
			rb.setOtherButtons(cButtons);
			final FastColourScheme scheme = COLOUR_SCHEMES[i];
			rb.setCallback(new Runnable() { public void run() {
				p.calculationThread.setColourScheme(scheme);
			}});
			rb.setToolTipText(scheme.toString());
			
			if (scheme.getClass().equals(startColourScheme.getClass())) {
				rb.setSelected(true);
			}
		}
		
		// Make Button
		final ImageRadioButton makeButton = new ImageRadioButton("images/MakeTShirt.jpg");
		add(makeButton);
		makeButton.setLocation((VIEWER_WIDTH - makeButton.getWidth()) / 2, FRACTAL_HEIGHT + 5);
		makeButton.setCallback(new Runnable() { public void run() {
			applet.getAppletContext().showDocument(SiteIntegrationUtils.getMakeShirtURL(p.calculationThread.getFractalInfo()));
		}});
		makeButton.setToolTipText("Make T-Shirt");
		
		// Throb the button
		p.getTimer().schedule(new TimerTask() { public void run() {
			makeButton.setHighlighted(true);
		}}, 5000, 20000);
		
		p.getTimer().schedule(new TimerTask() { public void run() {
			makeButton.setHighlighted(false);
		}}, 5200, 20000);
		
		// Link Button
		final ImageRadioButton linkButton = new ImageRadioButton("images/linkButton.jpg");
		add(linkButton);
		linkButton.setLocation(8, FRACTAL_HEIGHT + 5 + makeButton.getHeight() / 2 - linkButton.getHeight() / 2);
		linkButton.setCallback(new Runnable() { public void run() {
			linkButton.setSelected(false);
			LinkDialog ld = new LinkDialog(p.calculationThread.getFractalInfo(), applet);
			ld.setLocationRelativeTo(null); // Center on screen.
			ld.setVisible(true);
		}});
		linkButton.setToolTipText("Link to this fractal");
		
		// Price
		JLabel priceLabel = new JLabel(SiteIntegrationUtils.PRICE);
		priceLabel.setFont(new Font("Times", Font.PLAIN, 16));
		add(priceLabel);
			
		//priceLabel.setBounds(VIEWER_WIDTH - 16, FRACTAL_HEIGHT + 5 + makeButton.getHeight() / 2, 100, 32);
		priceLabel.setBounds(VIEWER_WIDTH - 150, FRACTAL_HEIGHT + makeButton.getHeight() / 2 - 8 + 5 + 2, 140, 16);
		
		// MOUSE LISTENERS
		p.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2 && p.click(e.getPoint())) {
					return;
				}
				
				if (e.getClickCount() >= 2 && !e.isMetaDown() && !e.isAltDown()) {
					p.calculationThread.zoom(e.getX(), e.getY());
					// User managed to zoom in.
					p.meaningfulInteractionHasHappened();
					return;
				}
				
				if (e.getButton() == MouseEvent.BUTTON2 || e.isMetaDown() || e.isAltDown()) {
					p.calculationThread.zoomOut(e.getX(), e.getY());
					// User managed to zoom out.
					p.meaningfulInteractionHasHappened();
					return;
				}
			}
			public void mousePressed(MouseEvent e) {
				// Give the click to the handler for pressing buttons, then check if anything hit.
				/*if (p.click(e.getPoint())) {
					return;
				}*/
				p.click(e.getPoint());
				p.calculationThread.startDrag(e.getX(), e.getY());
			}
		});
		p.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				/*if (p.isCaught(e.getPoint())) {
					return;
				}*/
				p.calculationThread.drag(e.getX(), e.getY());
			}
		});
		p.addMouseWheelListener(new MouseWheelListener() {
			private int offset = 0;
			
			public void mouseWheelMoved(MouseWheelEvent e) {
				offset += e.getWheelRotation();
				if (offset <= -6) {
					p.calculationThread.zoom(e.getX(), e.getY());
					offset += 6;
				}
				
				if (offset >= 6) {
					p.calculationThread.zoomOut(e.getX(), e.getY());
					offset -= 6;
				}
			}
		});
	}
	
	public void destroy() {
		p.destroy();
		p = null;
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setLayout(new BorderLayout());
		f.add((new FractalViewer(null)), BorderLayout.CENTER);
		f.setSize(VIEWER_WIDTH, VIEWER_HEIGHT);
		f.setUndecorated(true);
		f.setVisible(true);
	}
}