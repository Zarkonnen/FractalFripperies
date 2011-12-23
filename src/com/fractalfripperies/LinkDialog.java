package com.fractalfripperies;

import javax.swing.*;
import java.applet.AppletContext;
import java.net.URL;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.Font;
import java.awt.Color;

public class LinkDialog extends JDialog {
	private static final int WIDTH = 600;
	private static final int SPACING = 8;
	private static final int H_SPACING = 12;
	private static final int SINGLE_H = 24;
	private static final int FIELD_H = 32;
	private final URL link;
	private final JApplet applet;
	
	public LinkDialog(FractalInfo info, JApplet a) {
		super((JFrame) null, "Link", true);
		
		link = SiteIntegrationUtils.getFractalURL(info);
		
		this.applet = a;
		
		setLayout(null);
		
		// Instructive label
		JPanel labelPanel = new JPanel(new FlowLayout());
		add(labelPanel);
		labelPanel.setBounds(0, SPACING, WIDTH, SINGLE_H);
		labelPanel.add(new JLabel("Copy this link to send it to others, or go to its page to bookmark it."));
		
		// URL field
		JTextArea urlField = new JTextArea(link.toString());
		urlField.setLineWrap(true);
		urlField.setFont(new JLabel().getFont().deriveFont(9f));
		//urlField.setBackground(getBackground());
		urlField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		add(urlField);
		urlField.setEditable(false);
		urlField.setBounds(H_SPACING, SINGLE_H + SPACING * 2, WIDTH - 2 * H_SPACING, FIELD_H);
		urlField.selectAll();
		
		// Close and Go To buttons
		JPanel buttonPanel = new JPanel(new FlowLayout());
		add(buttonPanel);
		buttonPanel.setBounds(0, SINGLE_H + FIELD_H + SPACING * 2, WIDTH, FIELD_H);
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}});
		buttonPanel.add(closeButton);
		getRootPane().setDefaultButton(closeButton);
		urlField.addKeyListener(new KeyAdapter() { public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				setVisible(false);
			}
		}});
		
		JButton goToButton = new JButton("Go To");
		goToButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
			applet.getAppletContext().showDocument(link);
			setVisible(false);
		}});
		buttonPanel.add(goToButton);
		
		// Set size
		setSize(WIDTH, SINGLE_H + FIELD_H + FIELD_H + SPACING * 5 + /* Menubar Extra */ 25); 
		setResizable(false);
	}
}