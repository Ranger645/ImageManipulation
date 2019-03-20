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
			System.err.println("Error initializing Batch Counter");
			return;
		}

		File[] files = ImageLoader.get_all_valid_files_in_dir(this.directory);
		this.total_files = files.length;
		for (int i = 0; i < files.length; i++) {

			// Setting object fields for this image to count:
			this.current_file = files[i].getName();
			this.current_index = i;

			// Loading the files into memory in the worker thread:
			synchronized (this) {
				// This is where we do all of the work in splitting the image file and loading
				// it.
				this.next_viewers = ImageLoader.load_image(files[i]);
				this.config.apply_to_viewers(this.next_viewers);
				this.viewers_available = true;
			}

			// Breaking out if the process is stopped:
			if (!keep_going)
				break;

			// Waiting for an outside process to call get_next_viewers()
			boolean waiting = true;
			while (waiting && keep_going)
				synchronized (this) {
					waiting = this.viewers_available;
				}

			// Breaking out if the process is stopped:
			if (!keep_going)
				break;
		}
		this.stop_process();
	}

	/**
	 * Saves the counts of this.current_file to the output file.
	 */
	public void save_previous_counts(Viewer[] prev_viewers) {
		// Writing the previous viewer's counts to the file:
		int[] counts = new int[prev_viewers.length];
		for (int n = 0; n < counts.length; n++) {
			counts[n] = prev_viewers[n].get_blob_count();
		}
		this.sort_counts(prev_viewers, counts);
		this.write_file_line(this.current_file, counts);
	}
	
	void sort_counts(Viewer keys[], int counts[]) 
    { 
        int n = counts.length; 
  
        // One by one move boundary of unsorted sub-array 
        for (int i = 0; i < n-1; i++) 
        { 
            // Find the minimum element in unsorted array 
            int min_idx = i; 
            for (int j = i+1; j < n; j++) 
                if (keys[j].KEY.compareTo(keys[min_idx].KEY) < 0) 
                    min_idx = j; 
  
            // Swap the found minimum element with the first 
            // element 
            int temp = counts[min_idx]; 
            counts[min_idx] = counts[i]; 
            counts[i] = temp; 
        } 
    } 

	/**
	 * Gets the next set of viewers to count. If the next set is not ready yet,
	 * hangs until it is ready.
	 * 
	 * @return an array of Viewer objects.
	 */
	public Viewer[] get_next_viewers() {

		// Waiting for the next set of viewers to be available:
		boolean waiting = false;
		while (!waiting && keep_going)
			synchronized (this) {
				waiting = this.viewers_available;
			}

		// If the process is stopped before the next viewers are loaded:
		if (!this.keep_going)
			return null;

		// Telling the batch counter to start loading the next viewers:
		synchronized (this) {
			this.viewers_available = false;
		}

		// Returning the next viewers:
		return this.next_viewers;
	}

	/**
	 * Initializes the output files by creating it if it does not exist.
	 * 
	 * @throws IOException
	 */
	public void initialize() throws IOException {
		this.config = new IMFFile(config_file);

		// Creating the output file if it has to be created:
		if (!this.output_file.exists()) {
			this.output_file.createNewFile();
			return;
		}
		// Making sure the contents of the output file is nothing
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.output_file, false));
		writer.write("");
		writer.close();

		// Initializing state variables:
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
	 * Gets the status of the batch count manager.
	 * 
	 * @return the number of files left to count. If not counting, returns -1.
	 */
	public int get_status() {
		if (!this.keep_going)
			return -1;
		return this.total_files - this.current_index;
	}

	public int get_total_files() {
		return this.total_files;
	}

	public void stop_process() {
		System.out.println("All done loading files, counts still may need to be recorded. Output counts at: "
				+ output_file.getAbsolutePath());
		this.keep_going = false;
		this.current_index = this.total_files;
	}

	/**
	 * TODO: Make this test the input parameters
	 * 
	 * @return
	 */
	public boolean test_parameters() {
		return true;
	}

}
