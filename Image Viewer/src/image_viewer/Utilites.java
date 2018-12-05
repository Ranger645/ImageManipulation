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
	
	public static List<File> get_files_in_dir_by_type(File directory, String extension) {
		List<File> embedded_files = new ArrayList<File>();
		File[] to_search = directory.listFiles();
		if (to_search == null)
			return new ArrayList<File>();
		for (int i = 0; i < to_search.length; i++) {
			if (to_search[i].isDirectory())
				embedded_files.addAll(get_files_in_dir_by_type(to_search[i], extension));
			else {
				String name = to_search[i].getName();
				if (name.endsWith("." + extension))
					embedded_files.add(to_search[i]);
			}
		}
		return embedded_files;
	}

}
