package com.fractalfripperies;

import com.fractalfripperies.fractal.*;
import com.fractalfripperies.fastcolourscheme.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Color;

public final class CalculationThread extends Thread {
	private static final int INITIAL_MAX_ITERATIONS = 712;
	private static final int MAX_MAX_ITERATIONS = 16384;
	private static final int ZOOMS_PER_MAX_INCREASE = 5;
	private static final int MAX_ITERATIONS_INCREASE_FACTOR = 2;
	public static final int INITIAL_GRAIN = 16; //qqDPS was 32
	public static final int NO_VALUE = Integer.MIN_VALUE;
	public static final int IN_SET = Integer.MAX_VALUE;
	public static final int MAX_ZOOM = 42;
	
	private int width;
	private int height;
	private int[][] iterations;
	private int[][] oldIterations;
	private int maxIterations;
	private int zoom;
	
	private int imageWidth;
	private int imageHeight;
	private BufferedImage image;
	
	private Fractal fractal;
	private FastColourScheme colourScheme;
	
	private double realStart;
	private double imaginaryStart;
	private double realWidth;
	private double imaginaryHeight;
	private double realStep;
	private double imaginaryStep;
	private int grain;
	private boolean renderComplete;

	public FractalInfo getFractalInfo() {
		FractalInfo fi = new FractalInfo();
		fi.fractal = fractal;
		fi.colourScheme = colourScheme;
		fi.realStart = realStart;
		fi.imaginaryStart = imaginaryStart;
		fi.realWidth = realWidth;
		fi.imaginaryHeight = imaginaryHeight;
		return fi;
	}
	
	public CalculationThread(FractalInfo info, int imageWidth, int imageHeight) {
		this(info.fractal, info.colourScheme, imageWidth, imageHeight);
		
		if (imageWidth == imageHeight) {
			realStart = info.realStart;
			imaginaryStart = info.imaginaryStart;
			realWidth = info.realWidth;
			imaginaryHeight = info.imaginaryHeight;
		} else {
			double ratio = ((double) imageWidth) / ((double) imageHeight);
			realStart = info.realStart - (info.realWidth * ratio - info.realWidth) / 2;
			imaginaryStart = info.imaginaryStart;
			realWidth = info.realWidth * ratio;
			imaginaryHeight = info.imaginaryHeight;
		}
		
		realStep = realWidth / width;
		imaginaryStep = imaginaryHeight / height;
		
		/*
		realStart = info.realStart;
		imaginaryStart = info.imaginaryStart;
		realWidth = info.realWidth;
		imaginaryHeight = info.imaginaryHeight;
		realStep = realWidth / width;
		imaginaryStep = imaginaryHeight / height;
		*/
	}
	
	// CLEANUP
	public void shutdown() {
		setShutdownWanted();
		try {
			join();
		} catch (Exception e) { /* Never mind, let's just shut down. */ }
		iterations = null;
		oldIterations = null;
		image = null;
	}
	
	// EXECUTION
	public CalculationThread(Fractal fractal, FastColourScheme colourScheme, int imageWidth, int imageHeight) {
		setDaemon(true);
		this.fractal = fractal;
		this.colourScheme = colourScheme;
		width = imageWidth * 2;
		height = imageHeight * 2;
		iterations = new int[width][height];
		oldIterations = new int[width][height];
		clearIterations();
		
		if (imageWidth == imageHeight) {
			realStart = fractal.realStart();
			imaginaryStart = fractal.imaginaryStart();
			realWidth = fractal.realWidth();
			imaginaryHeight = fractal.imaginaryHeight();
		} else {
			double ratio = ((double) imageWidth) / ((double) imageHeight);
			realStart = fractal.realStart() - (fractal.realWidth() * ratio - fractal.realWidth()) / 2;
			imaginaryStart = fractal.imaginaryStart();
			realWidth = fractal.realWidth() * ratio;
			imaginaryHeight = fractal.imaginaryHeight();
		}
		
		
		realStep = realWidth / width;
		imaginaryStep = imaginaryHeight / height;
		maxIterations = INITIAL_MAX_ITERATIONS;
		grain = INITIAL_GRAIN;
		zoom = 0;
		
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
	}
	
	public BufferedImage fullRender(double realStart, double imaginaryStart, double realWidth, double imaginaryHeight, boolean antialias) {
		this.realStart = realStart;
		this.imaginaryStart = imaginaryStart;
		this.realWidth = realWidth;
		this.imaginaryHeight = imaginaryHeight;
		
		realStep = realWidth / width;
		imaginaryStep = imaginaryHeight / height;
		
		grain = antialias ? 1 : 2;
		maxIterations = MAX_MAX_ITERATIONS;
		
		// We can do a lot of fencing, since interim visual quality does not matter.
		if (256 < imageWidth) {
			fence(256);
		}
		if (64 < imageWidth) {
			fence(64);
		}
		if (16 < imageWidth) {
			fence(16);
		}
		if (8 < imageWidth) {
			fence(8);
		}
		
		// Now iterate and render uninterruptibly.
		iterate(false);
		render(false);
		
		return image;
	}
	
	public void run() {
		grain = INITIAL_GRAIN;
		iterate(/* interruptible */ true);
		try {
			Thread.sleep(150);
		}
		catch (Exception e) {}
		
		while (true) {
			//System.out.println("f");
			// Fence
			if (grain == INITIAL_GRAIN) {
				fence(grain);
			}
			if (assertShutdown) {
				return;
			}
			
			// Iterate.
			//System.out.println("i");
			iterate(/* interruptible */ true);
			if (assertShutdown) {
				return;
			}
			if (doChange()) {
				continue;
			}
			
			//System.out.println("r");
			// Render.
			render(/* interruptible */ true);
			if (assertShutdown) {
				return;
			}
			if (doChange()) {
				continue;
			}
			
			//System.out.println("rr");
			// Repaint.
			for (RepaintListener l : repaintListeners) { l.repaint(); }
			
			if (assertShutdown) {
				return;
			}
			
			// Wait.
			if (grain == 1) {
				//System.out.println("w");
				renderComplete = true;
				synchronized(this) {
					while (!changeWanted()) {
						try {
							wait(100);
						}
						catch (InterruptedException ex) { /* do nothing */ }
					}
				}
				
				//System.out.println("dc");
				if (assertShutdown) {
					return;
				}
				doChange();
			}
			
			//System.out.println("ip");
			// Increase Precision.
			//grain /= 2;
			if (grain > 4) {
				grain /= 4;
			} else {
				if (grain > 1) {
					grain /= 2;
				}
			}
			
			//System.out.println("g" + grain);
		}
	}
	
	private void iterate(boolean interruptible) {
		//System.out.println("iterating " + realStart + " " + imaginaryStart + " " + realWidth + " " + imaginaryHeight);
		
		for (int r = 0; r < width; r ++) {
			for (int i = 0; i < height; i ++) {
				// If the value is already known, skip it.
				if (iterations[r][i] > 0) {
					continue;
				}
				
				// Do an iteration if we're within the grain.
				if (r % grain == 0 && i % grain == 0) {
					iterations[r][i] = fractal.iterate(
							realStart + r * realStep,
							imaginaryStart + i * imaginaryStep,
							maxIterations);
				} else {
					// Set the iteration value to the current grain, so render knows where to get the iterations value.
					if (iterations[r][i] < -grain) {
						iterations[r][i] = -grain;
					}
				}
			}
			
			if (interruptible && changeWanted()) {
				return;
			}
		}
	}
	
	
	// RENDER
	private void render(boolean interruptible) {	
		for (int r = 0; r < width; r += 2) {
			for (int i = 0; i < height; i += 2) {
				int iters = iterations[r][i];
				
				// If this is a negative value, then it's a grain indicator of where to look for the value to render.
				if (iters < 0 && iters != NO_VALUE) {
					iters = -iters; // To preserve sanity with integer division, make positive.
					iters = iterations[(r / iters) * iters][(i / iters) * iters];
					
					 //Turn this on to detect negative grain indicator targets.
					/*
					if (iters < 0) { //qqDPS
						image.setRGB(r / 2, i / 2, 16711680);
						continue;
					}
					*/
					
					image.setRGB(r / 2, i / 2, colourScheme.getRGB(iters));
				} else {
					// Can we get supersampling?
					int iters2 = iterations[r  ][i+1];
					int iters3 = iterations[r+1][i  ];
					int iters4 = iterations[r+1][i+1];
					if (iters != NO_VALUE && iters2 > 0 && iters3 > 0 && iters4 > 0) {
						// Yes we can!
						image.setRGB(r / 2, i / 2, colourScheme.getSupersampledRGB(iters, iters2, iters3, iters4));
					} else {
						// Normal rendering.
						image.setRGB(r / 2, i / 2, colourScheme.getRGB(iters));
					}
				}
			}
			
			if (interruptible && changeWanted()) {
				drawMaxZoomReached();
				return;
			}
		}
		
		drawMaxZoomReached();
	}
	
	private void drawMaxZoomReached() {
		if (zoom == MAX_ZOOM) {
			Graphics2D g = image.createGraphics();
			FontMetrics fm = g.getFontMetrics();
		
			int w = fm.stringWidth("Maximum zoom reached.") + 10;
		
			g.setColor(Color.WHITE);
			g.fillRect((width / 2 - w) / 2, height / 4 - fm.getHeight() / 2 - 3, w, fm.getHeight() + 6);
			g.setColor(Color.BLACK);
			g.drawString("Maximum zoom reached.", (width / 2 - w) / 2 + 5, height / 4 - fm.getHeight() / 2 + fm.getAscent());
		}
	}
	
	
	// INTERNAL MESSAGING
	private synchronized boolean changeWanted() {
		return assertDrag ||
			assertZoomOut ||
			assertZoom ||
			assertColourSchemeChangeWanted ||
			assertFractalChangeWanted ||
			assertShutdown;
	}
	
	private synchronized boolean doChange() {
		if (assertZoom) {
			doZoom();
			return true;
		}
		if (assertZoomOut) {
			doZoomOut();
			return true;
		}
		if (assertDrag) {
			doDrag();
			return true;
		}
		if (assertColourSchemeChangeWanted) {
			doColourSchemeChange();
			return true;
		}
		if (assertFractalChangeWanted) {
			doFractalChange();
			return true;
		}
		
		return false;
	}
	
	
	private boolean assertZoom = false;
	private int zoomX;
	private int zoomY;
	
	private boolean assertZoomOut = false;
	private int zoomOutX;
	private int zoomOutY;
	
	private boolean assertDrag = false;
	private int dragStartX;
	private int dragStartY;
	private int dragX;
	private int dragY;
	
	private boolean assertColourSchemeChangeWanted = false;
	private FastColourScheme newColourScheme;
	
	private boolean assertFractalChangeWanted = false;
	private Fractal newFractal;
	
	private boolean assertShutdown = false;
	
	private synchronized void setShutdownWanted() {
		this.assertShutdown = true;
		notifyAll();
	}
	
	private synchronized void setZoomWanted(boolean assertZoom, int zoomX, int zoomY) {
		this.assertZoom = assertZoom;
		this.zoomX = zoomX;
		this.zoomY = zoomY;
		notifyAll();
	}
	
	private synchronized void setZoomOutWanted(boolean assertZoomOut, int zoomOutX, int zoomOutY) {
		this.assertZoomOut = assertZoomOut;
		this.zoomOutX = zoomOutX;
		this.zoomOutY = zoomOutY;
		notifyAll();
	}
	
	private synchronized void setColourSchemeChangeWanted(boolean assertColourSchemeChangeWanted, FastColourScheme newColourScheme) {
		this.assertColourSchemeChangeWanted = assertColourSchemeChangeWanted;
		this.newColourScheme = newColourScheme;
		notifyAll();
	}
	
	private synchronized void setFractalChangeWanted(boolean assertFractalChangeWanted, Fractal newFractal) {
		this.assertFractalChangeWanted = assertFractalChangeWanted;
		this.newFractal = newFractal;
		notifyAll();
	}
	
	private synchronized void setDragStart(int dragX, int dragY) {
		//System.out.println("setDragStart");
		assertDrag = false; // No actual dragging wanted yet.
		this.dragStartX = dragX;
		this.dragStartY = dragY;
		this.dragX = dragX;
		this.dragY = dragY;
		notifyAll();
	}
	
	private synchronized void setMoreDrag(int dragX, int dragY) {
		//System.out.println("setMoreDrag");
		assertDrag = true;
		this.dragX = dragX;
		this.dragY = dragY;
		notifyAll();
	}
	
	private synchronized void dragDone(int dragDoneToX, int dragDoneToY) {
		if (dragDoneToX == dragX && dragDoneToY == dragY) {
		//if (Math.abs(dragDoneToX - dragX) < INITIAL_GRAIN && Math.abs(dragDoneToY - dragY) < INITIAL_GRAIN) {
			dragStartX = dragDoneToX;
			dragStartY = dragDoneToY;
			assertDrag = false;
		} else {
			dragStartX = dragDoneToX;
			dragStartY = dragDoneToY;
			assertDrag = true;
		}
	}
	
	
	// GETTING GRAPHICS
	public HashSet<RepaintListener> repaintListeners = new HashSet<RepaintListener>();
	
	public BufferedImage getImage() {
		return image;
	}
	
	
	// NAVIGATION COMMANDS, ASSERTION
	private void waitTillNothingWanted() {
		synchronized(this) {
			while (changeWanted()) {
				try {
					wait(100);
				}
				catch (InterruptedException ex) { /* do nothing */ }
			}
		}
	}
	
	public synchronized void zoom(int x, int y) {
		waitTillNothingWanted();
		
		////System.out.println("Asserting zoom.");
		int grain2 = grain * 2;
		// Extra *2 due to supersampling.
		x = (x / grain2) * grain2 * 2;
		y = (y / grain2) * grain2 * 2;
		
		setZoomWanted(true, x, y);
	}
	
	public synchronized void zoomOut(int x, int y) {
		waitTillNothingWanted();
		setZoomOutWanted(true, x, y);
	}
	
	public synchronized void setColourScheme(FastColourScheme newColourScheme) {
		waitTillNothingWanted();
		setColourSchemeChangeWanted(true, newColourScheme);
	}
	
	public synchronized void setFractal(Fractal newFractal) {
		waitTillNothingWanted();
		setFractalChangeWanted(true, newFractal);
	}
	
	public synchronized void startDrag(int x, int y) {
		waitTillNothingWanted();
		setDragStart(x, y);
	}
	
	// Unthreadsafe?
	public synchronized void drag(int x, int y) {
		setMoreDrag(x, y);
	}
	
	
	// EXECUTION OF NAVIGATION COMMANDS
	private void doFractalChange() {
		
		fractal = newFractal;
		clearIterations();
		
		if (imageWidth == imageHeight) {
			realStart = fractal.realStart();
			imaginaryStart = fractal.imaginaryStart();
			realWidth = fractal.realWidth();
			imaginaryHeight = fractal.imaginaryHeight();
		} else {
			double ratio = ((double) imageWidth) / ((double) imageHeight);
			realStart = fractal.realStart() - (fractal.realWidth() * ratio - fractal.realWidth()) / 2;
			imaginaryStart = fractal.imaginaryStart();
			realWidth = fractal.realWidth() * ratio;
			imaginaryHeight = fractal.imaginaryHeight();
		}
		
		/*
		realStart = fractal.realStart();
		imaginaryStart = fractal.imaginaryStart();
		realWidth = fractal.realWidth();
		imaginaryHeight = fractal.imaginaryHeight();
		realStep = realWidth / width;
		imaginaryStep = imaginaryHeight / height;
		*/
		
		realStep = realWidth / width;
		imaginaryStep = imaginaryHeight / height;
		
		maxIterations = INITIAL_MAX_ITERATIONS;
		grain = INITIAL_GRAIN;
		zoom = 0;
		iterate(false);
		render(false);
		setFractalChangeWanted(false, null);
	}
	
	private void doZoom() {
		if (zoom == MAX_ZOOM) {
			System.out.println("Max zoom reached.");
			// Turn off zoom wanted.
			setZoomWanted(false, 0, 0);
			return;
		}
		
		
		// Zoom.
		zoom++;
		
		//System.out.println("Zoom: " + zoom);
				
		if (zoom % ZOOMS_PER_MAX_INCREASE == 0 && maxIterations < MAX_MAX_ITERATIONS) {
			maxIterations *= MAX_ITERATIONS_INCREASE_FACTOR;
			clearIterations();
		} else {
			zoomInIterations(zoomX - width / 4, zoomY - height / 4);
		}
		
		// Recalculate all the numbers.
		realStart += realWidth * zoomX / width - realWidth / 4;
		realWidth /= 2;
		imaginaryStart += imaginaryHeight * zoomY / height - imaginaryHeight / 4;
		imaginaryHeight /= 2;
		
		grain = INITIAL_GRAIN;
		
		realStep = realWidth / width;
		imaginaryStep = imaginaryHeight / height;
		
		//System.out.println("zoomiterandrender");
		
		iterate(false);
		
		// Render and repaint.
		render(/* interruptible */ false);
		for (RepaintListener l : repaintListeners) { l.repaint(); }
		
		// Fence.
		fence(16);
		
		// The render isn't complete anymore.
		renderComplete = false;
		
		//System.out.println("zoomiterandrender DONE");
		
		// Ready for next zoom.
		setZoomWanted(false, 0, 0);
	}
	
	private void doZoomOut() {
		// Zoom.
		zoom--;
		zoomOutIterations(width - zoomOutX * 2, height - zoomOutY * 2);
		
		// Recalculate all the numbers.
		realStart -= (width - zoomOutX * 2) * realWidth / width;
		realWidth *= 2;
		imaginaryStart -= (height - zoomOutY * 2) * imaginaryHeight / height;
		imaginaryHeight *= 2;
		
		grain = INITIAL_GRAIN;
		
		realStep = realWidth / width;
		imaginaryStep = imaginaryHeight / height;
		
		iterate(false);
		
		// Render and repaint.
		
		render(/* interruptible */ false);
		for (RepaintListener l : repaintListeners) { l.repaint(); }
		
		// Fence.
		fence(16);
		
		// The render isn't complete anymore.
		renderComplete = false;
		
		// Ready for next zoom.
		
		setZoomOutWanted(false, 0, 0);
	}
	
	private void doDrag() {
		//System.out.println("doDrag");
		
		// Capture how far we need to drag just now.
		int deltaX;
		int deltaY;
		int dragToX;
		int dragToY;
		
		synchronized(this) {
			// Experimental.
			dragToX = dragX;
			dragToY = dragY;
			deltaX = dragStartX - dragX;
			deltaY = dragStartY - dragY;
			
			/*
			deltaX = dragStartX - (dragX / INITIAL_GRAIN) * INITIAL_GRAIN;
			deltaY = dragStartY - (dragY / INITIAL_GRAIN) * INITIAL_GRAIN;
			dragToX = dragStartX - deltaX;
			dragToY = dragStartY - deltaY;
			*/
		}
		
		shiftIterations(deltaX * 2, deltaY * 2); // Factor of 2 due to supersampling.
		
		realStart += deltaX * realStep * 2;
		imaginaryStart += deltaY * realStep * 2;
		
		grain = INITIAL_GRAIN;
		
		renderComplete = false;
		
		iterate(/* interruptible */ false);
		render(/* interruptible */ false);
		for (RepaintListener l : repaintListeners) { l.repaint(); }
		
		dragDone(dragToX, dragToY);
		
		//System.out.println("doDrag DONE");
	}
	
	private void doColourSchemeChange() {
		colourScheme = newColourScheme;
		render(/* interruptible */ false);
		for (RepaintListener l : repaintListeners) { l.repaint(); }
		setColourSchemeChangeWanted(false, null);
	}
	
	
	// ITERATION ARRAY MANIPULATION
	/**
	 * Sets all iterations to "no value calculated".
	 */
	private void clearIterations() {
		for (int r = 0; r < width; r++) {
			for (int i = 0; i < height; i++) {
				iterations[r][i] = NO_VALUE;
				oldIterations[r][i] = NO_VALUE;
			}
		}
	}
	
	private void shiftIterations(int shiftReal, int shiftImaginary) {
		// Copy into old iterations.
		for (int r = 0; r < width; r++) {
			System.arraycopy(iterations[r], 0, oldIterations[r], 0, height);
		}
		
		int shiftedR;
		int shiftedI;
		
		for (int r = 0; r < width; r++) {
			for (int i = 0; i < height; i++) {
				shiftedR = r + shiftReal;
				shiftedI = i + shiftImaginary;
				if (shiftedR >= 0 &&
					shiftedR < width &&
					shiftedI >= 0 &&
					shiftedI < height)
				{
					int iters = oldIterations[shiftedR][shiftedI];
					if (iters < -2) { //qqDPS was 0
						iters = -INITIAL_GRAIN;
					}
					iterations[r][i] = iters;
				} else {
					iterations[r][i] = NO_VALUE;
				}
			}
		}
	}
	
	private void zoomOutIterations(int windowRealStart, int windowImaginaryStart) {
		for (int r = 0; r < width; r++) {
			System.arraycopy(iterations[r], 0, oldIterations[r], 0, height);
		}
		
		int oldRealPos;
		int oldImaginaryPos;
		int oldIters;
		
		for (int r = 0; r < width; r++) {
			for (int i = 0; i < height; i++) {
				oldRealPos = r * 2 - windowRealStart;
				oldImaginaryPos = i * 2 - windowImaginaryStart;
				if (oldRealPos > -1 && oldRealPos < width && oldImaginaryPos > -1 && oldImaginaryPos < height) {
					oldIters = oldIterations[oldRealPos][oldImaginaryPos];
					
					if (oldIters < 0) {
						if (oldIters == -2) {
							// Grain of 2. Use neighbouring value.
							if (oldRealPos > 0 && oldImaginaryPos > 0) {
								iterations[r][i] = oldIterations[oldRealPos - 1][oldImaginaryPos - 1];
							} else {
								iterations[r][i] = NO_VALUE;
							}
						} else {
							// Grain of more than 2. Can decrease. qqDPS
							// iterations[r][i] = oldIters / 2;
							iterations[r][i] = -INITIAL_GRAIN;
						}
					} else {
						// There is a value in the appropriate old cell.
						iterations[r][i] = oldIters;
					}
				} else {
					// The cell is outside the known area.
					iterations[r][i] = NO_VALUE;
				}
			}
		}
	}
	
	/**
	 * @param windowRealStart Where in the current iterations to start using values. Can be negative.
	 * @param windowImaginaryStart Where in the current iterations to start using values. Can be negative.
	*/
	private void zoomInIterations(int windowRealStart, int windowImaginaryStart) {
		for (int r = 0; r < width; r++) {
			System.arraycopy(iterations[r], 0, oldIterations[r], 0, height);
		}
		
		int oldRealPos;
		int oldImaginaryPos;
		
		for (int r = 0; r < width; r++) {
			for (int i = 0; i < height; i++) {
				if (r % 2 == 0 && i % 2 == 0) {
					// This could have a previous value.
					oldRealPos = r / 2 + windowRealStart;
					oldImaginaryPos = i / 2 + windowImaginaryStart;
					if (oldRealPos > -1 && oldRealPos < width && oldImaginaryPos > -1 && oldImaginaryPos < height) {
						int oldIters = oldIterations[oldRealPos][oldImaginaryPos];
						// Are the previous iterations an actual iteration value or a grain value?
						if (oldIters < 0 && oldIters != NO_VALUE) {
							// Grain value.
							iterations[r][i] = oldIters * 2;
						} else {
							// Real iterations.
							iterations[r][i] = oldIters;
						}
					} else {
						// Out of bounds of the previous rectangle, so we don't know.
						iterations[r][i] = NO_VALUE;
					}
				} else {
					// It's not a previous value. Let's cast about for a grain to peg this to.
					// Let's try the top-left of the zoom.
					oldRealPos = r / 2 + windowRealStart;
					oldImaginaryPos = i / 2 + windowImaginaryStart;
					int oldIters;
					if (oldRealPos > -1 && oldRealPos < width && oldImaginaryPos > -1 && oldImaginaryPos < height) {
						oldIters = oldIterations[oldRealPos][oldImaginaryPos];
						// If it's a grain, use it.
						if (oldIters < 0 && oldIters != NO_VALUE) {
							iterations[r][i] = oldIters * 2;
						} else {
							// It's not a grain, so we can say that the grain is 2 and render's going to find something.
							iterations[r][i] = -2;
						}
					} else {
						// Out of bounds of the previous rectangle, so we don't know.
						iterations[r][i] = NO_VALUE;
					}
				}
			}
		}
	}
	
	// FENCING
	/**
	 * Breaks when called with grid = 1!
	 */
	private void fence(int grid) {	
		int realGridSteps = width / grid - 1;
		
		boolean[] upperFence = new boolean[realGridSteps];
		boolean[] lowerFence = new boolean[realGridSteps];
		boolean leftFence;
		boolean rightFence;
		
		//Calculate initial upper fence.
		for (int r = 0; r < realGridSteps; r++) {
			upperFence[r] = fenceReal(r * grid, 0, grid);
		}
		
		for (int i = 0; i < height - grid; i += grid) {
			// Calculate initial left fence.
			leftFence = fenceImaginary(0, i, grid);
			
			// Calculate the bottom and right for each square.
			for (int r = 0; r < realGridSteps; r++) {
				// Lower.
				lowerFence[r] = fenceReal(r * grid, i + 1, grid);
				// Right.
				rightFence = fenceImaginary((r + 1) * grid, i, grid);
				if (leftFence && rightFence && upperFence[r] && lowerFence[r]) {
					////System.out.println("Hit: " + r * grid + "/" + i + " (" + grid + ")");
					fillFencedSquare(r * grid, i, grid);
				}
				// Exchange right into left.
				leftFence = rightFence;
			}
			
			// Lower into upper fence.
			upperFence = lowerFence;
			lowerFence = new boolean[realGridSteps];
		}
	}
	
	private void fillFencedSquare(int real, int imaginary, int grid) {
		int realEnd = real + grid - 1;
		int imaginaryEnd = imaginary + grid - 1;
		for (int r = real + 1; r < realEnd; r++) {
			for (int i = imaginary + 1; i < imaginaryEnd; i++) {
				iterations[r][i] = IN_SET;
			}
		}
	}
	
	private boolean fenceReal(int real, int imaginary, int grid) {
		int end = real + grid;
		for (int r = real; r < end; r++) {
			if (iterations[r][imaginary] != NO_VALUE) {
				// There is already a computed value.
				if (iterations[r][imaginary] != IN_SET) {
					// And it's not in the set, so the fencing fails.
					return false;
				} else {
					continue;
				}
			}
			
			int iters = fractal.iterate(
					realStart + r * realStep,
					imaginaryStart + imaginary * imaginaryStep,
					maxIterations);
			iterations[r][imaginary] = iters;
			
			if (iters != IN_SET) {
				// The value we've just computed is not in the set, so fencing fails.
				return false;
			}
		}
		
		return true;
	}
	
	private boolean fenceImaginary(int real, int imaginary, int grid) {
		int end = imaginary + grid;
		for (int i = imaginary; i < end; i++) {
			if (iterations[real][i] != NO_VALUE) {
				// There is already a computed value.
				if (iterations[real][i] != IN_SET) {
					// And it's not in the set, so the fencing fails.
					return false;
				} else {
					continue;
				}
			}
			
			int iters = fractal.iterate(
					realStart + real * realStep,
					imaginaryStart + i * imaginaryStep,
					maxIterations);
			iterations[real][i] = iters;
			
			if (iters != IN_SET) {
				// The value we've just computed is not in the set, so fencing fails.
				return false;
			}
		}
		
		return true;
	}
	
	// HELPERS
	public interface RepaintListener {
		public void repaint();
	}
} 