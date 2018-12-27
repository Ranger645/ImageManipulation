package image_viewer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import algos.Blob;

public class Utilites {

	public static void addGridComponent(JPanel panel, JComponent c, int x, int y, int width, int height, double weightx,
			double weighty, int loc, int fill) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.anchor = loc;
		gbc.fill = fill;
		panel.add(c, gbc);
	}

	public static void addGridComponent(JPanel panel, JComponent c, int x, int y, int width, int height, double weightx,
			double weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(c, gbc);
	}

	public static int paint_blob_centers(List<Blob> blobs, Graphics g, int dx, int dy, int zoom) {
		int radius = 3;
		double multiplier = zoom / 100.0;
		int count = 0;
		for (int i = 0; i < blobs.size(); i++) {
			Point p = blobs.get(i).compute_average_point();
			g.setColor(blobs.get(i).get_color());
			int x = dx + (int) (p.getX() * multiplier);
			int y = dy + (int) (p.getY() * multiplier);
			g.drawLine(x - radius, y, x + radius, y);
			g.drawLine(x, y - radius, x, y + radius);

			// If the counts of this blob is greater than 1, than it needs to have a number:
			if (blobs.get(i).get_count() > 1) {
				g.drawString("" + blobs.get(i).get_count(), x, y);
			}
			
			count += blobs.get(i).get_count();
		}
		return count;
	}

}
