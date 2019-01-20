package batch;

import java.io.File;

import files.ImageLoader;
import image_viewer.Viewer;

public class BatchCountManager extends Thread {
	/*
	 * Class that keeps track of the batch counting. It takes in a folder to count
	 * all of the valid files in. It also manages loading things into memory while
	 * the user is counting the previous one.
	 */

	private File directory = null;
	private boolean viewers_available = false;
	private Viewer[] next_viewers = null;

	public BatchCountManager(File folder_to_count) {
		this.directory = folder_to_count;
	}

	public void run() {
		File[] files = ImageLoader.get_all_valid_files_in_dir(this.directory);
		for (File file : files) {
			synchronized (this) {
				// This is where we do all of the work in splitting the image file and loading
				// it.
				next_viewers = ImageLoader.load_image(file);
				this.viewers_available = true;
			}
			
			boolean waiting = true;
			while (waiting)
				synchronized (this) {
					waiting = this.viewers_available;
				}
		}
	}

	public Viewer[] get_next_viewers() {
		// Waiting for viewers to be available:
		boolean waiting = true;
		while (waiting)
			synchronized (this) {
				waiting = !this.viewers_available;
				if (waiting == false)
					this.viewers_available = false;
			}

		return this.next_viewers;
	}

}
