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
import java.util.prefs.BackingStoreException;
import com.fractalfripperies.ResolutionDialog.Resolution;
import com.fractalfripperies.ResolutionDialog.ResolutionResult;


public class FractalAppMain {
	public static final Resolution DEFAULT_RESOLUTION = new Resolution(800, 600, false);
	public static final FractalInfo DEFAULT_FRACTAL_INFO = new FractalInfo(new Mandelbrot(),
			new FastStripyColourScheme(), -2.0, -2.0, 4.0, 4.0);
	
	public static void main(String[] args) {
		Resolution resolution = DEFAULT_RESOLUTION;
		boolean dontAskForResolution = false;
		boolean checkFullscreen = false;
		FractalInfo fractalInfo = getFractalInfoFromPrefs();
		
		// Get prefs.
		Preferences userRoot = Preferences.userRoot();
		boolean hadPrefs = false;
		try {
			if (userRoot.nodeExists("com/fractalfripperies/FractalApplication")) {
				hadPrefs = true;
				Preferences prefs = userRoot.node("com/fractalfripperies/FractalApplication");
				resolution = new Resolution(
					prefs.getInt("width", resolution.width),
					prefs.getInt("height", resolution.height),
					prefs.getBoolean("fullscreen", resolution.fullscreen));
				dontAskForResolution = prefs.getBoolean("dontAskForResolution", false);
			}
		} catch (BackingStoreException e) { /* ignore */ }
		
		// Ask for resolution, unless it's the first time.
		if (hadPrefs) {
			ResolutionResult r = ResolutionDialog.getResolution(resolution, dontAskForResolution);
			checkFullscreen = !resolution.fullscreen && r.resolution.fullscreen;
			resolution = r.resolution;
			dontAskForResolution = r.dontAskAgain;
		}
		
		// Save prefs.
		Preferences prefs = userRoot.node("com/fractalfripperies/FractalApplication");
		prefs.putInt("width", resolution.width);
		prefs.putInt("height", resolution.height);
		prefs.putBoolean("fullscreen", resolution.fullscreen);
		prefs.putBoolean("dontAskForResolution", dontAskForResolution);
		try { prefs.flush(); } catch (Exception e) {}
		saveFractalInfoToPrefs(fractalInfo);
		
		// Display program.
		FractalApplication app = new FractalApplication(resolution.width, resolution.height,
				resolution.fullscreen, checkFullscreen, fractalInfo);
		
		if (resolution.fullscreen) {
			GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			dev.setFullScreenWindow(app);
		} else {
			app.setVisible(true);
		}
	}
	
	public static FractalInfo getFractalInfoFromPrefs() {
		Preferences userRoot = Preferences.userRoot();
		try {
			if (userRoot.nodeExists("com/fractalfripperies/FractalApplication")) {
				Preferences prefs = userRoot.node("com/fractalfripperies/FractalApplication");
				return new FractalInfo(
					(Fractal) Class.forName(prefs.get("fractal", "com.fractalfripperies.fractal.Mandelbrot")).newInstance(),
					(FastColourScheme) Class.forName(prefs.get("colourScheme", "com.fractalfripperies.fastcolourscheme.FastStripyColourScheme")).newInstance(),
					prefs.getDouble("realStart", -2.0),
					prefs.getDouble("imaginaryStart", -2.0),
					prefs.getDouble("realWidth", 4.0),
					prefs.getDouble("imaginaryHeight", 4.0)
				);
			} else {
				return DEFAULT_FRACTAL_INFO;
			}
		} catch (Exception e) {
			return DEFAULT_FRACTAL_INFO;
		}
	}
	
	public static void saveFractalInfoToPrefs(FractalInfo info) {
		squareFractalInfo(info);
		Preferences userRoot = Preferences.userRoot();
		try {
			Preferences prefs = userRoot.node("com/fractalfripperies/FractalApplication");
			prefs.put("fractal", info.fractal.getClass().getName());
			prefs.put("colourScheme", info.colourScheme.getClass().getName());
			prefs.putDouble("realStart", info.realStart);
			prefs.putDouble("imaginaryStart", info.imaginaryStart);
			prefs.putDouble("realWidth", info.realWidth);
			prefs.putDouble("imaginaryHeight", info.imaginaryHeight);
		} catch (Exception e) {}
	}
	
	/** Fixes up the given fractalInfo to have a square area. qqDPS HACKY */
	public static void squareFractalInfo(FractalInfo info) {
		if (info.imaginaryHeight > info.realWidth) {
			double difference = info.imaginaryHeight - info.realWidth;
			info.imaginaryStart += difference / 2;
			info.imaginaryHeight = info.realWidth;
		} else {
			if (info.realWidth > info.imaginaryHeight) {
				double difference = info.realWidth - info.imaginaryHeight;
				info.realStart += difference / 2;
				info.realWidth = info.imaginaryHeight;
			}
		}
	}
}