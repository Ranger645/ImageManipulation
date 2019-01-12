package files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import image_viewer.Window;

public class FileUtilities {
	
	/**
	 * Shows an open dialog that contains error checking and options for a starting
	 * directory and extension.
	 * 
	 * @param starting_directory
	 * @return
	 */
	public static File showFolderOpenDialog(File starting_directory, Window parent) {
		JFileChooser chooser = new JFileChooser(starting_directory);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int result = chooser.showOpenDialog(parent);
		if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION)
			return null;
		
		File selected = chooser.getSelectedFile();
		if (selected == null || !selected.exists())
			return null;
		else
			return selected;
	}
	
	/**
	 * Shows an open dialog that contains error checking and options for a starting
	 * directory and extension.
	 * 
	 * @param starting_directory
	 * @return
	 */
	public static File showFileOpenDialog(File starting_directory, Window parent) {
		JFileChooser chooser = new JFileChooser(starting_directory);
		
		int result = chooser.showOpenDialog(parent);
		if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION)
			return null;
		
		File selected = chooser.getSelectedFile();
		if (selected == null || !selected.exists())
			return null;
		else
			return selected;
	}

	/**
	 * Shows an open dialog that contains error checking and options for a starting
	 * directory and extension.
	 * 
	 * @param starting_directory
	 * @param extension
	 * @return
	 */
	public static File showFileOpenDialog(File starting_directory, String extension, Window parent) {
		JFileChooser chooser = new JFileChooser(starting_directory);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(extension + " File", extension);
		chooser.setFileFilter(filter);
		
		int result = chooser.showOpenDialog(parent);
		if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION)
			return null;
		
		File selected = chooser.getSelectedFile();
		if (selected == null || !selected.exists())
			return null;
		else
			return selected;
	}
	
	/**
	 * Shows a save dialog that contains error checking and options for a starting
	 * directory, default file name, and extension.
	 * 
	 * @param starting_directory
	 * @param default_name
	 * @param extension
	 * @return
	 */
	public static File showFileSaveDialog(File starting_directory, String default_name, String extension, Window parent) {
		JFileChooser chooser = new JFileChooser(starting_directory);
		File default_file = new File(starting_directory, default_name + "." + extension);
		chooser.setSelectedFile(default_file);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(extension + " File", extension);
		chooser.setFileFilter(filter);
		int result = chooser.showSaveDialog(parent);
		
		if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION)
			return null;
		
		return chooser.getSelectedFile();
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
