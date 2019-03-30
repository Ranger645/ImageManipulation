package files;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import image_viewer.Viewer;
import image_viewer.WorkingBar;
import tools.ImageConverter;

public class ImageLoader {
	/**
	 * This class stores the file types that this program can open and handles
	 * opening them.
	 */

	private static final String[] VALID_TYPES = { "png", "jpg", "jpeg", "nd2" };

	/**
	 * Loads an abstract image and converts it to an array of viewers. If the given
	 * file cannot be opened, returns null.
	 * 
	 * @param to_load the image file to load.
	 * @return an array of viewers
	 */
	public static Viewer[] load_image(File to_load) {

		// Making sure we can load the file:
		if (!ImageLoader.is_valid_file(to_load))
			return null;

		// Now that we know we can load the file, we need to determine how to load it:
		String type = to_load.getAbsolutePath().substring(to_load.getAbsolutePath().lastIndexOf("."));
		if (type.endsWith("nd2")) {
			WorkingBar.set_text("Loading image: " + to_load.getAbsolutePath());
			WorkingBar.start_working();
			
			// Splitting the nd2 into layers and putting them into temp/
			ImageConverter.nd2_split(to_load);

			// Getting the number of files in temp:
			File temp_folder = new File("temp");
			int layer_number = temp_folder.listFiles().length;

			// Creating the array of viewers to allocate:
			Viewer[] viewers = new Viewer[layer_number];

			// Adding the viewers to the array:
			File[] files = temp_folder.listFiles();
			String[] file_names = new String[files.length];
			for (int i = 0; i < files.length; i++)
				file_names[i] = files[i].getName();
			Arrays.sort(file_names);
			
			for (int i = 0; i < file_names.length; i++) {
				viewers[i] = new Viewer(file_names[i].substring(0, file_names[i].indexOf(".")));
				try {
					viewers[i].set_image(temp_folder.listFiles()[i]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			WorkingBar.set_text("");
			WorkingBar.stop_working();

			return viewers;
		} else {
			// Default file opening:
			Viewer[] viewers = new Viewer[1];
			String name = to_load.getName();
			viewers[0] = new Viewer(name.substring(0, name.indexOf(".")));

			try {
				viewers[0].set_image(to_load);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return viewers;
		}
	}

	/**
	 * Tests to see if the given file is able to be opened by this program.
	 * 
	 * @param f the file to test
	 * @return true if it is valid or false otherwise.
	 */
	public static boolean is_valid_file(File f) {
		for (String type : VALID_TYPES)
			if (f.getName().endsWith("." + type))
				return true;
		return false;
	}

	public static File[] get_all_valid_files_in_dir(File directory) {
		List<File> valid_files = new LinkedList<File>();
		for (File f : directory.listFiles())
			if (ImageLoader.is_valid_file(f))
				valid_files.add(f);

		return valid_files.toArray(new File[valid_files.size()]);
	}

}
