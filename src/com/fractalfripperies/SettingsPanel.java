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
import edu.stanford.ejalbert.BrowserLauncher;

import static com.fractalfripperies.FractalViewer.FRACTALS;
import static com.fractalfripperies.FractalViewer.FRACTAL_IMAGES;
import static com.fractalfripperies.FractalViewer.COLOUR_SCHEMES;
import static com.fractalfripperies.FractalViewer.COLOUR_SCHEME_IMAGES;

public class SettingsPanel extends JPanel {
	private int width = FractalApplication.SETTINGS_PANEL_WIDTH;
	private int height;
	private final EnhancedFractalPanel p;
	private final boolean fullscreen;
	
	public SettingsPanel(int height, final EnhancedFractalPanel p, final Fractal startFractal, final FastColourScheme startColourScheme, final boolean fullscreen) {
		setLayout(null);
		setBackground(Color.WHITE);
		this.height = height;
		this.p = p;
		this.fullscreen = fullscreen;
		
		// Create fractal radio buttons
		ArrayList<ImageRadioButton> fButtons = new ArrayList<ImageRadioButton>();
		
		int offset = 10;
		for (int i = 0; i < FRACTALS.length; i++) {
			ImageRadioButton rb = new ImageRadioButton("images/" + FRACTAL_IMAGES[i]);
			add(rb);
			rb.setLocation(10, offset);
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
		
		offset = 10;
		for (int i = 0; i < COLOUR_SCHEMES.length; i++) {
			ImageRadioButton rb = new ImageRadioButton("images/" + COLOUR_SCHEME_IMAGES[i]);
			add(rb);
			rb.setLocation(65, offset);
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
		
		offset += 45;
		
		// Exit button.
		ImageRadioButton exitB = new ImageRadioButton("images/Exit.jpg");
		add(exitB);
		exitB.setLocation(10, offset);
		exitB.setCallback(new Runnable() { public void run() {
			FractalAppMain.saveFractalInfoToPrefs(p.calculationThread.getFractalInfo());
			System.exit(0);
		}});
		exitB.setToolTipText("Exit fractal viewer");
		
		// Link Button
		final ImageRadioButton linkB = new ImageRadioButton("images/Link.jpg");
		add(linkB);
		linkB.setLocation(65, offset);
		linkB.setCallback(new Runnable() { public void run() {
			try {
				FractalAppMain.saveFractalInfoToPrefs(p.calculationThread.getFractalInfo());
				new BrowserLauncher().openURLinBrowser("http://www.fractalfripperies.com");				
				if (fullscreen) {
					try { Thread.sleep(1000); } catch (Exception e) {}
					System.exit(0);
				}
			} catch (Exception e) {}
			linkB.setSelected(false);
		}});
		linkB.setToolTipText("Go to FractalFripperies.com");
		
		offset += 45;
		
		
		// Make Button
		final ImageRadioButton makeButton = new ImageRadioButton("images/MakeTShirt.jpg");
		add(makeButton);
		makeButton.setLocation((width - makeButton.getWidth()) / 2, height - 115);
		makeButton.setCallback(new Runnable() { public void run() {
			try {
				FractalInfo info = p.calculationThread.getFractalInfo();
				// Note: also squares the info, which is a good thing.
				FractalAppMain.saveFractalInfoToPrefs(info);
				
				if (fullscreen) {
					try {
						GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
						dev.setFullScreenWindow(null);
					} catch (Exception e) {}
				}
				new BrowserLauncher().openURLinBrowser(SiteIntegrationUtils.getMakeShirtURL(info).toString());				
				if (fullscreen) {
					try { Thread.sleep(1000); } catch (Exception e) {}
					System.exit(0);
				}
				makeButton.setSelected(false);
			} catch (Exception e) {}
		}});
		makeButton.setToolTipText("Make T-Shirt");
		
		// Show a rectangle indicating the T-shirt area.
		makeButton.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				p.setShowTShirtRect(true);
			}
			public void mouseExited(MouseEvent e) {
				p.setShowTShirtRect(false);
			}
		});
	}
}