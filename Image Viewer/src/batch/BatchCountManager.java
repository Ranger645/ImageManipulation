package batch;

import java.io.File;

import files.ImageLoader;
import image_viewer.Viewer;
import image_viewer.WorkingBar;

public class BatchCountManager extends Thread {
	/*
	 * Class that keeps track of the batch counting. It takes in a folder to count
	 * all of the valid files in. It also manages loading things into memory while
	 * the user is counting the previous one.
	 */

	private File directory = null;
	private boolean viewers_available = false;
	private Viewer[] next_viewers = null;
	
	private boolean keep_going = false;

	public BatchCountManager(File folder_to_count) {
		this.directory = folder_to_count;
	}

	public void run() {
		this.keep_going = true;
		File[] files = ImageLoader.get_all_valid_files_in_dir(this.directory);
		System.out.println("Starting batch.");
		for (File file : files) {
			synchronized (this) {
				// This is where we do all of the work in splitting the image file and loading
				// it.
				next_viewers = ImageLoader.load_image(file);
				this.viewers_available = true;
			}

			boolean waiting = true;
			while (waiting && keep_going)
				synchronized (this) {
					waiting = this.viewers_available;
				}
			
			if (!keep_going) {
				System.out.println("Ending batch.");
				return;
			}
		}
		System.out.println("Finishing counts.");
		this.keep_going = false;
	}

	/**
	 * Gets the next set of viewers to count.
	 * 
	 * @return an array of Viewer objects.
	 */
	public Viewer[] get_next_viewers() {
		// Waiting for viewers to be available:
		System.out.println("Waiting for next batch...");
		boolean waiting = true;
		
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
		
		System.out.println("Getting next batch.");
		
		if (!keep_going)
			return null;

		return this.next_viewers;
	}

	public void stop_process() {
		this.keep_going = false;
	}
	
	public boolean in_progress() {
		return this.keep_going;
	}

}
