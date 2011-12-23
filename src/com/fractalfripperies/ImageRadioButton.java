package com.fractalfripperies;

import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.HashMap;
import java.awt.RenderingHints;
import java.util.Collection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class ImageRadioButton extends JPanel {
	private BufferedImage selectedImage;
	private BufferedImage normalImage;
	private boolean selected = false;
	private boolean highlighted = false;
	private Runnable callback;
	private Collection<ImageRadioButton> otherButtons = null;
	private int shift;
	
	public ImageRadioButton(String imageURL) {
		try {
			normalImage = cleanImageEdges(ImageIO.read(getClass().getResource(imageURL)));
			shift = normalImage.getWidth() / 20;
			shift = shift < 2 ? 2 : shift;
			selectedImage = addSelectionBorder(normalImage, shift);
			setSize(selectedImage.getWidth(), selectedImage.getHeight());
		} catch (Exception e) { e.printStackTrace(); }
		
		final ImageRadioButton self = this;
		
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				setSelected(true);
				if (otherButtons != null) {
					for (ImageRadioButton b : otherButtons) {
						if (b != self) {
							b.setSelected(false);
						}
					}
				}
				if (callback != null) {
					callback.run();
				}
			}
		});
	}
	
	public void setCallback(Runnable callback) {
		this.callback = callback;
	}
	
	public void setOtherButtons(Collection<ImageRadioButton> otherButtons) {
		this.otherButtons = otherButtons;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
		repaint();
	}
	
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
		repaint();
	}
	
	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, selectedImage.getWidth(), selectedImage.getHeight());
		if (selected) {
			g.drawImage(selectedImage, 0, 0, null);
		} else {
			g.drawImage(normalImage, shift, shift, null);
		}
		
		if (highlighted) {
			g.setColor(EnhancedFractalPanel.HELP_BG_COLOR_TRANSLUCENT);
			g.fillOval(shift, shift, normalImage.getWidth(), normalImage.getHeight());
		}
	}
	
	// IMAGE MANIPULATION
	private static final HashMap<RenderingHints.Key, Object> HINTS = new HashMap<RenderingHints.Key, Object>();
	static {
		HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	public static BufferedImage cleanImageEdges(BufferedImage i) {
		BufferedImage i2 = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = i2.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, i2.getWidth(), i2.getHeight());
		
		g.clip(new java.awt.geom.Ellipse2D.Double(-1, -1, i2.getWidth() + 2, i2.getHeight() + 2));
		g.drawImage(i, 0, 0, null);
		
		return i2;
	}
	
	public static BufferedImage addSelectionBorder(BufferedImage i, int shift) {
		BufferedImage i2 = new BufferedImage(i.getWidth() + shift * 2, i.getHeight() + shift * 2, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = i2.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, i2.getWidth(), i2.getHeight());
		g.drawImage(i, shift, shift, null);
		g.setColor(new Color(160, 160, 160));
		g.setStroke(new BasicStroke((int) (shift * 1.5)));
		g.addRenderingHints(HINTS);
		g.drawOval(shift, shift, i.getWidth(), i.getHeight());
		return i2;
	}
}