package batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import filters.F_Combination;
import image_viewer.Utilites;
import image_viewer.WorkingBar;
import tools.ImageConverter;
import utilities.CountConfiguration;

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
	private File output;

	public BackgroundNd2Counter(File nd2, String config, File output) {
		this.config = config;
		if (nd2.isDirectory()) {
			List<File> files = Utilites.get_files_in_dir_by_type(nd2, "nd2");
			this.nd2_files = new File[files.size()];
			for (int i = 0; i < files.size(); i++)
				this.nd2_files[i] = files.get(i);
		} else {
			this.nd2_files = new File[1];
			this.nd2_files[0] = nd2;
		}
		this.output = output;
	}

	public void calculate() {
		WorkingBar.start_working();

		// Decoding the configuration string:
		String[] count_config_strings = this.config.split("#####\n");
		CountConfiguration[] count_configs = new CountConfiguration[count_config_strings.length - 1];
		for (int i = 1; i < count_config_strings.length; i++)
			count_configs[i - 1] = new CountConfiguration(count_config_strings[i]);
		int layer_number = count_configs.length;

		// Building the array of counts now that we know its dimensions:
		this.counts = new int[this.nd2_files.length][layer_number];

		for (int file_index = 0; file_index < this.nd2_files.length; file_index++) {
			File nd2_file = nd2_files[file_index];
			WorkingBar.set_text(nd2_file.getAbsolutePath());
			System.out.println("Counting: " + nd2_file.getAbsolutePath());

			// Splitting the nd2 file and loading the layers:
			ImageConverter.nd2_split(nd2_file);
			File temp_folder = new File("temp");
			File[] layer_image_files = temp_folder.listFiles();
			Arrays.sort(layer_image_files, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			// Checking for a layer number mismatch. This occurs when the number of layers
			// in the configuration file does not match the number of layers in the nd2
			// file.
			if (layer_number != layer_image_files.length) {
				System.err.println(
						"Layer number mismatch between configuration file and " + nd2_file.getAbsolutePath() + ".");
				for (int i = 0; i < layer_number; i++)
					this.counts[file_index][i] = -1;
				continue;
			}

			// Iterating over all the layers and counting the blobs with the specific
			// parameters for that layer.
			for (int i = 0; i < layer_number; i++) {
				BackgroundImage image = new BackgroundImage(layer_image_files[i]);
				image.filter(new F_Combination(count_configs[i].get_filters()));
				counts[file_index][i] = algos.BlobFinder.find_blob_centers(image.getImage(),
						count_configs[i].getGrey_thresh(), count_configs[i].getBlob_size(), 2).size();
				System.out.printf("\tCounts[%d] = %d\n", i + 1, counts[file_index][i]);
			}
		}
		
		// Saving the layers to an output file:
		try {
			System.out.println("Writing Counts to " + output.getAbsolutePath());
			PrintWriter printer = new PrintWriter(output);
			
			// Printing the headings:
			printer.print("Layers:");
			for (int i = 1; i <= layer_number; i++)
				printer.print("," + i);
			printer.print("\n");
			
			// Printing the data:
			for (int i = 0; i < nd2_files.length; i++) {
				printer.print(this.nd2_files[i].getName());
				for (int n = 0; n < layer_number; n++)
					printer.print("," + counts[i][n]);
				printer.print("\n");
			}
			
			printer.close();
		} catch (FileNotFoundException e) {
			System.err.println("Unable to print to file.");
			e.printStackTrace();
		}
		

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
