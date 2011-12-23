package com.fractalfripperies;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Image;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Graphics;
import com.fractalfripperies.fastcolourscheme.*;
import com.fractalfripperies.fractal.*;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class EnhancedFractalPanel extends FractalPanel {	
	public static final Color HELP_BG_COLOR = new Color(255, 255, 163, 220);
	public static final Color HELP_BG_COLOR_TRANSLUCENT = new Color(255, 255, 163, 127);
	public static final int TEXT_TOP_INSET = 27;
	public static final int HELP_ROUNDING = 15;
	public static final int TEXT_INSET = 10;
	public static final int LINE_HEIGHT = 14;
	public int helpSide;
	public int xInset;
	public int yInset;
	public int textWidth;
	public int textHeight;
	public boolean showHelp = true;

	public static final Font HELP_FONT = new Font("Verdana", Font.PLAIN, 12);
	public static final Font HELP_TITLE_FONT = new Font("Verdana", Font.BOLD, 12);
	protected Image upButton;
	protected Image downButton;
	protected Image leftButton;
	protected Image rightButton;
	protected Image zoomInButton;
	protected Image zoomOutButton;
	protected Image helpButton;
	protected Image closeButton;
	protected boolean firstTime = true;
	public Timer getTimer() { return vanishHelpTimer; } protected Timer vanishHelpTimer = new Timer();
	
	public void setShowTShirtRect(boolean showTShirtRect) { this.showTShirtRect = showTShirtRect; repaint(); }
	protected boolean showTShirtRect;
	
	public static final int BUTTON_SIDE = 15;
	public static final int BUTTON_MARGIN = 3;
	/** Which page of the help to show, or -1 for none. */
	protected int helpPage = -1;
	protected boolean helpTranslucent = false;
	protected boolean helpReminder = false;
	protected boolean meaningfulInteractionHasHappened = false;
	public void setHelpTextForStandalone(boolean b) {
		helpText = b ? STANDALONE_HELP_TEXT : HELP_TEXT;
	}
	
	private HashMap<String, Rectangle> buttonLocations = new HashMap<String, Rectangle>();
	
	protected static final String[][] HELP_TEXT = new String[][] {
		new String[] {
			"Explore the fractal, then press",
			"\"Make T-Shirt\" to buy.",
			"",
			" - Double-click to zoom in.",
			" - Right- or Alt-click to zoom out.",
			" - Drag to move.",
			" - Use the buttons on the left to",
			"   select different fractals.",
			" - Use the buttons on the right to",
			"   select different colour schemes.",
			" - Press the link icon on the bottom",
			"   left to get a link to the current",
			"   fractal image.",
			" - Press the ? button to show this."
		}
	};
	
	protected static final String[][] STANDALONE_HELP_TEXT = new String[][] {
		new String[] {
			"Explore the fractal, then press",
			"\"Make T-Shirt\" to buy.",
			"",
			" - Double-click to zoom in.",
			" - Right- or Alt-click to zoom out.",
			" - Drag to move.",
			" - Use the buttons on the right to",
			"   select different fractals and",
			"   colour schemes.",
			" - Press the ? button to show this."
		}
	};
	
	protected String[][] helpText = HELP_TEXT;
	
	public EnhancedFractalPanel(int width, int height, Fractal fractal, FastColourScheme colourScheme, boolean showHelp) {
		super(width, height, fractal, colourScheme);
		this.showHelp = showHelp;
		initImages();
		initTimer();
		initSizes();
	}
	
	public EnhancedFractalPanel(int width, int height, FractalInfo info, boolean showHelp) {
		super(width, height, info);
		this.showHelp = showHelp;
		initImages();
		initTimer();
		initSizes();
	}
	
	public EnhancedFractalPanel(Fractal fractal, FastColourScheme colourScheme) {
		super(FractalViewer.FRACTAL_WIDTH, FractalViewer.FRACTAL_HEIGHT, fractal, colourScheme);
		initImages();
		initTimer();
		initSizes();
	}
	
	public EnhancedFractalPanel(FractalInfo info) {
		super(FractalViewer.FRACTAL_WIDTH, FractalViewer.FRACTAL_HEIGHT, info);
		initImages();
		initTimer();
		initSizes();
	}
	
	protected void initSizes() {
		helpSide = 260;
		xInset = (width - helpSide) / 2 + TEXT_INSET;
		yInset = (height - helpSide) / 2 + TEXT_INSET + TEXT_TOP_INSET;
		textWidth = helpSide - 2 * TEXT_INSET;
		textHeight = helpSide - 2 * TEXT_INSET - TEXT_TOP_INSET;
		
		buttonLocations.put("up", new Rectangle(width / 2 - BUTTON_SIDE / 2, BUTTON_MARGIN, BUTTON_SIDE, BUTTON_SIDE));
		buttonLocations.put("down", new Rectangle(width / 2 - BUTTON_SIDE / 2, height - BUTTON_SIDE - BUTTON_MARGIN, BUTTON_SIDE, BUTTON_SIDE));
		buttonLocations.put("left", new Rectangle(BUTTON_MARGIN, height / 2 - BUTTON_SIDE / 2, BUTTON_SIDE, BUTTON_SIDE));
		buttonLocations.put("right", new Rectangle(width - BUTTON_SIDE - BUTTON_MARGIN, height / 2 - BUTTON_SIDE / 2, BUTTON_SIDE, BUTTON_SIDE));
		buttonLocations.put("zoomIn", new Rectangle(BUTTON_MARGIN, height - BUTTON_SIDE - BUTTON_MARGIN, BUTTON_SIDE, BUTTON_SIDE));
		buttonLocations.put("zoomOut", new Rectangle(BUTTON_MARGIN + BUTTON_SIDE + BUTTON_MARGIN, height - BUTTON_SIDE - BUTTON_MARGIN, BUTTON_SIDE, BUTTON_SIDE));
		buttonLocations.put("help", new Rectangle(width - BUTTON_SIDE - BUTTON_MARGIN, height - BUTTON_SIDE - BUTTON_MARGIN, BUTTON_SIDE, BUTTON_SIDE));
		buttonLocations.put("close", new Rectangle(33, 33, HELP_ROUNDING, HELP_ROUNDING));
		buttonLocations.put("next", new Rectangle(270, 270, HELP_ROUNDING, HELP_ROUNDING));
		buttonLocations.put("previous", new Rectangle(33, 270, HELP_ROUNDING, HELP_ROUNDING));
	}
	
	/** The user has managed to do something meaningful to the applet. Assume they know how to use it. */
	public void meaningfulInteractionHasHappened() {
		meaningfulInteractionHasHappened = true;
	}
	
	protected void initTimer() {
		if (!showHelp) { return; }
		
		// Fade in help.
		vanishHelpTimer.schedule(new TimerTask() { public void run() {
			if (helpPage == -1 && !meaningfulInteractionHasHappened) {
				helpPage = 0;
				repaint();
			}
		}}, 42000);
		// Fade out help.
		vanishHelpTimer.schedule(new TimerTask() { public void run() {
			if (helpPage == 0 && firstTime && !meaningfulInteractionHasHappened) {
				helpTranslucent = true;
				firstTime = false;
				repaint();
			}
		}}, 102000);
		vanishHelpTimer.schedule(new TimerTask() { public void run() {
			if (firstTime && !meaningfulInteractionHasHappened) {
				helpPage = -1;
				helpTranslucent = false;
				firstTime = false;
				repaint();
			}
		}}, 103000);
		vanishHelpTimer.schedule(new TimerTask() { public void run() {
			if (helpPage == -1) {
				helpReminder = true;
				repaint();
			}
		}}, 10000, 60000);
		vanishHelpTimer.schedule(new TimerTask() { public void run() {
			helpReminder = false;
			repaint();
		}}, 20000, 60000);
	}
	
	protected void initImages() {
		try {
			upButton = ImageIO.read(getClass().getResource("images/up.png"));
			downButton = ImageIO.read(getClass().getResource("images/down.png"));
			leftButton = ImageIO.read(getClass().getResource("images/left.png"));
			rightButton = ImageIO.read(getClass().getResource("images/right.png"));
			zoomInButton = ImageIO.read(getClass().getResource("images/zoomin.png"));
			zoomOutButton = ImageIO.read(getClass().getResource("images/zoomout.png"));
			helpButton = ImageIO.read(getClass().getResource("images/help.png"));
			closeButton = ImageIO.read(getClass().getResource("images/close.png"));
			
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/** @return Whether a click hit something. */
	public boolean click(Point p) {
		if (helpPage > -1) {
			if (firstTime) {
				// The help page, it is ethereal. Note that this breaks paging etc.
				helpPage = -1;
				repaint();
				return false;
			}
			
			if (buttonLocations.get("close").contains(p)) {
				helpPage = -1;
				helpTranslucent = false;
				//firstTime = false;
				repaint();
				meaningfulInteractionHasHappened = true;
				return true;
			}
			if (buttonLocations.get("next").contains(p) && helpPage < helpText.length - 1) {
				helpPage++;
				repaint();
				//firstTime = false;
				return true;
			}
			if (buttonLocations.get("previous").contains(p) && helpPage > 0) {
				helpPage--;
				repaint();
				//firstTime = false;
				return true;
			}
			return true;
		}
		
		if (buttonLocations.get("up").contains(p)) {
			calculationThread.startDrag(p.x, p.y);
			calculationThread.drag(p.x, p.y + height / 4);
			meaningfulInteractionHasHappened = true;
			return true;
		}
		if (buttonLocations.get("down").contains(p)) {
			calculationThread.startDrag(p.x, p.y);
			calculationThread.drag(p.x, p.y - height / 4);
			meaningfulInteractionHasHappened = true;
			return true;
		}
		if (buttonLocations.get("left").contains(p)) {
			calculationThread.startDrag(p.x, p.y);
			calculationThread.drag(p.x + height / 4, p.y);
			meaningfulInteractionHasHappened = true;
			return true;
		}
		if (buttonLocations.get("right").contains(p)) {
			calculationThread.startDrag(p.x, p.y);
			calculationThread.drag(p.x - height / 4, p.y);
			meaningfulInteractionHasHappened = true;
			return true;
		}
		if (buttonLocations.get("zoomIn").contains(p)) {
			calculationThread.zoom(width / 2, height / 2);
			meaningfulInteractionHasHappened = true;
			return true;
		}
		if (buttonLocations.get("zoomOut").contains(p)) {
			calculationThread.zoomOut(width / 2, height / 2);
			meaningfulInteractionHasHappened = true;
			return true;
		}
		if (buttonLocations.get("help").contains(p) && showHelp) {
			helpPage = 0;
			helpReminder = false;
			repaint();
			meaningfulInteractionHasHappened = true;
			return true;
		}
		
		return false;
	}
	
	/** @return Whether a click hit something. */
	public boolean isCaught(Point p) {
		for (Rectangle r : buttonLocations.values()) {
			if (r.contains(p)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		
		if (showTShirtRect) {
			g.setFont(new Font("Verdana", Font.PLAIN, 13));
			if (width >= height) {
				g.setColor(new Color(255, 255, 255, 31));
				g.fillRect((width - height) / 2, 0, height - 1, height - 1);
				g.setColor(new Color(255, 255, 255));
				g.drawRect((width - height) / 2, 0, height - 1, height - 1);
				g.drawString("printed area", (width - height) / 2 + 3, 13);
			} else {
				g.setColor(new Color(255, 255, 255, 31));
				g.fillRect(0, (height - width) / 2, width - 1, width - 1);
				g.setColor(new Color(255, 255, 255));
				g.fillRect(0, (height - width) / 2, width - 1, width - 1);
				g.drawString("printed area", 3, (height - width) / 2 + 13);
			}
		}
		
		g.drawImage(upButton, width / 2 - BUTTON_SIDE / 2, BUTTON_MARGIN, null);
		g.drawImage(downButton, width / 2 - BUTTON_SIDE / 2, height - BUTTON_SIDE - BUTTON_MARGIN, null);
		g.drawImage(leftButton, BUTTON_MARGIN, height / 2 - BUTTON_SIDE / 2, null);
		g.drawImage(rightButton, width - BUTTON_SIDE - BUTTON_MARGIN, height / 2 - BUTTON_SIDE / 2, null);
		g.drawImage(zoomInButton, BUTTON_MARGIN, height - BUTTON_SIDE - BUTTON_MARGIN, null);
		g.drawImage(zoomOutButton, BUTTON_MARGIN + BUTTON_SIDE + BUTTON_MARGIN, height - BUTTON_SIDE - BUTTON_MARGIN, null);
		if (showHelp) {
			g.drawImage(helpButton, width - BUTTON_SIDE - BUTTON_MARGIN, height - BUTTON_SIDE - BUTTON_MARGIN, null);
		}
		
		// Help text.
		if (helpPage > -1) {
			g.setColor(helpTranslucent ? HELP_BG_COLOR_TRANSLUCENT : HELP_BG_COLOR);
			g.fillRoundRect((width - helpSide) / 2, (height - helpSide) / 2, helpSide, helpSide, HELP_ROUNDING, HELP_ROUNDING);
		}
		
		if (helpPage == 0) {
			g.setColor(Color.BLACK);
			g.setFont(HELP_TITLE_FONT);
			g.drawString("Instructions", (width - helpSide) / 2 + 30, (height - helpSide) / 2 + 16);
			g.setFont(HELP_FONT);
			for (int i = 0; i < helpText[helpPage].length; i++) {
				g.drawString(helpText[helpPage][i], xInset, yInset + i * LINE_HEIGHT);
			}
			
			if (helpPage > 0) {
				g.drawImage(leftButton, 33, 270, null);
			}
			if (helpPage < helpText.length - 1) {
				g.drawImage(rightButton, 270, 270, null);
			}
			g.drawImage(closeButton, (width - helpSide) / 2 + 4, (height - helpSide) / 2 + 4, null); 
		} else {
			if (helpReminder) {
				g.setColor(HELP_BG_COLOR);
				g.fillRoundRect((width - helpSide) / 2, height - 24, helpSide, 18, HELP_ROUNDING, HELP_ROUNDING);
				g.setFont(HELP_FONT);
				g.setColor(Color.BLACK);
				g.drawString("Click the ? button for instructions.", xInset, height - 11);
				/*
				g.drawLine(270, 305, 285, 305);
				g.drawLine(280, 300, 285, 305);
				g.drawLine(280, 310, 285, 305);
				*/
				g.drawLine((width + helpSide) / 2 - 20, height - 15, (width + helpSide) / 2 - 5, height - 15);
				g.drawLine((width + helpSide) / 2 - 10, height - 20, (width + helpSide) / 2 - 5, height - 15);
				g.drawLine((width + helpSide) / 2 - 10, height - 10, (width + helpSide) / 2 - 5, height - 15);
			}
		}
	}
}