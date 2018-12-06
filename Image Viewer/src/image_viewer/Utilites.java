package image_viewer;

import java.awt.GridBagConstraints;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

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

}
