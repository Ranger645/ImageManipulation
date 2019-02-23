package batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import files.IMFFile;
import files.ImageLoader;
import image_viewer.Viewer;
import image_viewer.WorkingBar;

public class BatchCountManager extends Thread {
	/*
	 * Class that keeps track of the batch counting. It takes in a folder to count
	 * all of the valid files in. It also manages loading things into memory while
	 * the user is counting the previous one.
	 */

	private File directory = null, config_file = null, output_file = null;
	private IMFFile config;
	private boolean viewers_available = false;
	private Viewer[] next_viewers = null;

	private boolean keep_going = false;
	private int current_index = -1;
	private int total_files = -1;
	private String current_file = "";

	public BatchCountManager(File folder_to_count, File config_file, File output_file) {
		this.directory = folder_to_count;
		this.config_file = config_file;
		this.output_file = output_file;
	}

	public void run() {

		// Initializing the Batch Count Manager.
		try {
			this.initialize();
		} catch (IOException e) {
			System.err.println("Error initializing BatchCounter");
			return;
		}

		File[] files = ImageLoader.get_all_valid_files_in_dir(this.directory);
		this.total_files = files.length;
		for (int i = 0; i < files.length; i++) {
			this.current_file = files[i].getName();
			this.current_index = i;
			synchronized (this) {
				// This is where we do all of the work in splitting the image file and loading
				// it.
				this.next_viewers = ImageLoader.load_image(files[i]);
				this.config.apply_to_viewers(this.next_viewers);
				this.viewers_available = true;
			}

			// Waiting for the user to count every time except the last time.
			boolean waiting = true;
			while (waiting && keep_going)
				synchronized (this) {
					waiting = this.viewers_available;
				}

			if (!keep_going)
				return;
		}
		this.stop_process();
	}

	/**
	 * Initializes the output files by creating it if it does not exist.
	 * 
	 * @throws IOException
	 */
	public void initialize() throws IOException {
		this.config = new IMFFile(config_file);

		if (!this.output_file.exists()) {
			this.output_file.createNewFile();
			return;
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.output_file, false));
		writer.write("");
		writer.close();

		this.keep_going = true;
		this.total_files = -1;
		this.current_index = -1;
		this.current_file = "";
	}

	/**
	 * Function to append an output line to the file. It will append to a line like:
	 * <file_Name>,<counts...>
	 * 
	 * @param file_name the string identifies of the count line
	 * @param count     the array of counts to append to this line. The output is a
	 *                  csv format.
	 * @return true if write is successful and false if it fails.
	 */
	public boolean write_file_line(String file_name, int[] counts) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(this.output_file, true));
			writer.write(file_name);
			for (int count : counts)
				writer.write("," + count);
			writer.write("\n");
			writer.close();
		} catch (IOException e) {
			System.err.println("Error appending line to output file.");
			return false;
		}
		return true;
	}

	/**
	 * Gets the next set of viewers to count.
	 * 
	 * @return an array of Viewer objects.
	 */
	public Viewer[] get_next_viewers() {
		// Waiting for viewers to be available:
		boolean waiting = true;
		
		// Writing current viewer counts to file:
		int[] counts = new int[this.next_viewers.length];
		for (int i = 0; i < counts.length; i++)
			counts[i] = this.next_viewers[i].get_blob_count();
		// Writing the blob counts to the file
		this.write_file_line(this.get_file_name(), counts);

		// Waiting for the run method of this thread to load the viewers:
		WorkingBar.set_text("Waiting for image to load.");
		WorkingBar.start_working();
		while (waiting && keep_going)
			synchronized (this) {
				waiting = !this.viewers_available;
				if (waiting == false)
					this.viewers_available = false;
			}
		WorkingBar.set_text("Loaded Image.");
		WorkingBar.stop_working();

		if (!keep_going)
			return null;

		System.out.println("Returning next viewers");
		return this.next_viewers;
	}

	public void stop_process() {
		System.out.println("Stopping batch load process.");
		this.keep_going = false;
		this.current_index = this.total_files;
	}

	public boolean in_progress() {
		return this.keep_going;
	}

	public boolean is_on_last() {
		return this.current_index == this.total_files - 1;
	}

	public String get_output_path_string() {
		return this.output_file.getAbsolutePath();
	}

	public String get_file_name() {
		return this.current_file;
	}

}
