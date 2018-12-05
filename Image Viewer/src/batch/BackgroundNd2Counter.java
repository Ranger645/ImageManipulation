package batch;

import java.io.File;
import java.util.List;

import image_viewer.Utilites;
import image_viewer.WorkingBar;

/**
 * Counts the number of blobs given an nd2 file or folder containing nd2 files.
 * 
 * @author gregfoss
 */
public class BackgroundNd2Counter {

	// Layer counts arrays for every nd2 file
	private int[][] counts;

	private String config;
	private File[] nd2_files;

	public BackgroundNd2Counter(File nd2, String config) {
		this.config = config;
		if (nd2.isDirectory()) {
			List<File> files = Utilites.get_files_in_dir_by_type(nd2, "nd2");
			this.nd2_files = new File[files.size()];
			this.counts = new int[files.size()][];
			for (int i = 0; i < files.size(); i++)
				this.nd2_files[i] = files.get(i);
		} else {
			this.nd2_files = new File[1];
			this.counts = new int[1][];
			this.nd2_files[0] = nd2;
		}
	}

	public void calculate() {
		WorkingBar.start_working();

		WorkingBar.stop_working();
	}

	public String get_calculated_string() {
		String str = "";
		for (int file_index = 0; file_index < this.nd2_files.length; file_index++) {
			str += this.nd2_files[file_index].getName() + ",";
			for (int i = 0; i < this.counts[file_index].length; i++) {
				str += counts[file_index][i];
				if (i != counts[file_index].length - 1)
					str += ",";
			}
			str += "\n";
		}
		return str;
	}

}
