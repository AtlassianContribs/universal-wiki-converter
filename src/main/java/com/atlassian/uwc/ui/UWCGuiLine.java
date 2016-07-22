package com.atlassian.uwc.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

import org.apache.log4j.Logger;


/**
 *  decorative line used with the headers
 */
public class UWCGuiLine extends JPanel {

	private Line2D line;
	Logger log = Logger.getLogger(this.getClass());
	private Dimension size;
	
	public UWCGuiLine(int x1, int x2, int y1, int y2) {
		
		this.line = new Line2D.Double(x1, x2, y1, y2);
		int width = (x1 > y1)?(x1-y1):(y1-x1);
		int height = 10;
		size = new Dimension(width,height);
		this.setForeground(getFontColor());
		this.setVisible(true);
	}
	
	public void paintComponent(Graphics g) {
		clear(g);
		Graphics2D g2d = (Graphics2D)g;
		this.setSize(size);//have to do this here, or it gets overwritten 
		g2d.draw(this.line);
	}
	
	protected void clear(Graphics g) {
		super.paintComponent(g);
	}

	public static Color getFontColor() {
		return Color.BLACK; 
	}
}
