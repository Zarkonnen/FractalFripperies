package com.fractalfripperies;

import com.fractalfripperies.fastcolourscheme.*;
import com.fractalfripperies.fractal.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.Image;
import java.awt.Color;
import java.awt.Font;
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
import java.awt.Point;
import java.awt.Dimension;
import java.util.prefs.Preferences;
import java.awt.FlowLayout;
import com.fractalfripperies.CalculationThread.RepaintListener;


public class FractalApplication extends JFrame {
	protected EnhancedFractalPanel p;
	protected SettingsPanel settingsP;
	protected boolean fullscreen = false;
	protected boolean checkFullscreen = false;
	protected JButton checkFullscreenButton;
	protected Timer checkFullscreenTimer;
	protected TimerTask checkFullscreenTimerTask;
	public static final int FULLSCREEN_CHECK_WAIT = 6000;
	public static final int SETTINGS_PANEL_WIDTH = 120;
	protected int width;
	protected int height;
	protected FractalInfo fractalInfo;
	
	public FractalApplication(int width, int height, boolean fullscreen, boolean checkFullscreen,
			FractalInfo fractalInfo)
	{
		this.width = width;
		this.height = height;
		this.fullscreen = fullscreen;
		this.checkFullscreen = checkFullscreen;
		this.fractalInfo = fractalInfo;
		initialise();
	}
		
	protected void initialise() {
		if (fullscreen) {
			setUndecorated(true);
		}
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("FractalFripperies Fractal Viewer");
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				FractalAppMain.saveFractalInfoToPrefs(p.calculationThread.getFractalInfo());
				System.exit(0);
			}
		});
		if (fullscreen) {
			setSize(width, height);
			if (checkFullscreen) {
				initialiseFullscreenCheck();
			} else {
				initialiseFractalViewer();
			}
		} else {
			initialiseFractalViewer();
			pack();
		}
	}
	
	protected void initialiseFullscreenCheck() {	
		checkFullscreenTimer = new Timer();
		checkFullscreenTimerTask = new TimerTask() {
			public void run() {
				Preferences userRoot = Preferences.userRoot();
				Preferences prefs = userRoot.node("com/fractalfripperies/FractalApplication");
				prefs.putInt("width", FractalAppMain.DEFAULT_RESOLUTION.width);
				prefs.putInt("height", FractalAppMain.DEFAULT_RESOLUTION.height);
				prefs.putBoolean("fullscreen", FractalAppMain.DEFAULT_RESOLUTION.fullscreen);
				prefs.putBoolean("dontAskForResolution", false);
				try { prefs.flush(); } catch (Exception e) {}
				System.exit(0);
			}
		};
		checkFullscreenTimer.schedule(checkFullscreenTimerTask, FULLSCREEN_CHECK_WAIT);
		
		setLayout(null);
		add(checkFullscreenButton = new JButton("Click here to confirm fullscreen mode"), BorderLayout.CENTER);
			checkFullscreenButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
				checkFullscreenTimerTask.cancel();
				remove(checkFullscreenButton);
				initialiseFractalViewer();
				validate();
			}});
			checkFullscreenButton.setBounds(width / 2 - 200, height / 2 - 60, 400, 120);
	}
	
	protected void initialiseFractalViewer() {
		setLayout(new BorderLayout());
		add(p = new EnhancedFractalPanel(width - SETTINGS_PANEL_WIDTH, height, fractalInfo, /*showHelp*/true), BorderLayout.CENTER);
		p.setHelpTextForStandalone(true);
		p.setPreferredSize(new Dimension(width - SETTINGS_PANEL_WIDTH, height));
		add(settingsP = new SettingsPanel(height, p, new Mandelbrot(), new FastStripyColourScheme(), fullscreen), BorderLayout.EAST);
		settingsP.setPreferredSize(new Dimension(SETTINGS_PANEL_WIDTH, height));
		addListeners();
		p.calculationThread.repaintListeners.add(new RepaintListener() { public void repaint() {
			FractalAppMain.saveFractalInfoToPrefs(p.calculationThread.getFractalInfo());
		}});
	}
	
	protected void addListeners() {
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
}