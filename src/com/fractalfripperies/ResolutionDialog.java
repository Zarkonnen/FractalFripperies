package com.fractalfripperies;

import javax.swing.*;
import java.util.Vector;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.ActionEvent;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;

public class ResolutionDialog extends JDialog {
	public static final Dimension[] RESOLUTIONS = new Dimension[] {
		new Dimension(800,600),
		new Dimension(1024, 768),
		new Dimension(1280, 960),
		new Dimension(1600, 1200)
	};
	public static final int VERTICAL_MARGIN_FOR_NON_FULLSCREEN = 80;
	
	protected JComboBox resolutionChooser;
	protected JLabel infoLabel;
	protected JCheckBox dontAskAgainCheckBox;
	protected JButton okButton;
	
	protected Resolution resolution;
	protected boolean dontAskAgain;
	protected boolean initialResolutionHasFullscreen;
	
	public static ResolutionResult getResolution(Resolution defaultResolution, boolean dontAskAgain) {
		if (dontAskAgain &&
			!Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK))
		{
			return new ResolutionResult(defaultResolution, dontAskAgain);
		}
		
		ResolutionDialog d = new ResolutionDialog(defaultResolution, dontAskAgain);
		d.setVisible(true);
		return new ResolutionResult(d.resolution, d.dontAskAgain);
	}
	
	protected ResolutionDialog(Resolution defaultResolution, boolean dontAskAgain) {
		initialResolutionHasFullscreen = defaultResolution.fullscreen;
		this.resolution = defaultResolution;
		this.dontAskAgain = dontAskAgain;
		initialise();
	}
	
	protected void initialise() {
		setLocationRelativeTo(null); 
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setTitle("Choose Resolution");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		setModal(true);
		
		add(resolutionChooser = new JComboBox(getAvailableResolutions()));
				if (resolution != null) { resolutionChooser.setSelectedItem(resolution); }
				resolutionChooser.setAlignmentX(Component.CENTER_ALIGNMENT);
				resolutionChooser.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
					if (((Resolution) resolutionChooser.getSelectedItem()).fullscreen &&
						!initialResolutionHasFullscreen)
					{
						infoLabel.setText("Program will close if fullscreen not confirmed.");
					} else {
						infoLabel.setText("");
					}
				}});
		add(infoLabel = new JLabel(""));
				infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
				infoLabel.setFont(infoLabel.getFont().deriveFont(11f));
				infoLabel.setPreferredSize(new Dimension(200, 18));
		add(dontAskAgainCheckBox = new JCheckBox("Don't show this again (use caps lock to re-show)"));
				dontAskAgainCheckBox.setFont(dontAskAgainCheckBox.getFont().deriveFont(11f));
				dontAskAgainCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
				dontAskAgainCheckBox.setSelected(dontAskAgain);
		add(okButton = new JButton("OK"));
				getRootPane().setDefaultButton(okButton);
				okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
				okButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
					resolution = (Resolution) resolutionChooser.getSelectedItem();
					dontAskAgain = dontAskAgainCheckBox.isSelected();
					dispose();
				}});
			
		pack();
	}
	
	protected static Vector<Resolution> getAvailableResolutions() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		
		Vector<Resolution> l = new Vector<Resolution>();
		
		for (Dimension d : RESOLUTIONS) {
			if (d.width <= width && d.height <= height - VERTICAL_MARGIN_FOR_NON_FULLSCREEN) {
				l.add(new Resolution(d.width, d.height, /*fullscreen*/false));
			}
		}
		
		l.add(new Resolution(width, height, /*fullscreen*/true));
		
		return l;
	}
	
	public static final class ResolutionResult {
		public final Resolution resolution;
		public final boolean dontAskAgain;
		public ResolutionResult(Resolution resolution, boolean dontAskAgain) {
			this.resolution = resolution; this.dontAskAgain = dontAskAgain;
		}
	}
	
	public static final class Resolution {
		public final int width;
		public final int height;
		public final boolean fullscreen;
		
		public Resolution(int width, int height, boolean fullscreen) {
			this.width = width; this.height = height; this.fullscreen = fullscreen;
		}
		
		public String toString() {
			if (fullscreen) {
				return "Fullscreen (" + width + "x" + height + ")";
			} else {
				return width + "x" + height;
			}
		}
		
		public boolean equals(Object o) {
			if (!(o instanceof Resolution)) { return false; }
			Resolution r2 = (Resolution) o;
			return width == r2.width && height == r2.height && fullscreen == r2.fullscreen;
		}
		
		public int hashCode() {
			return 123901134 ^ width ^ height ^ (fullscreen ? 399082110 : 0);
		}
	}
}