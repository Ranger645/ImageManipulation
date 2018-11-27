package batch;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import filters.Filter;

public class BatchCountExecution {

	private File folder = null;
	private File output_file = null;

	/**
	 * Constructor that establishes a batch execution profile.
	 * 
	 * @param source_folder - the string path to the source folder with the images
	 *                      to be counted.
	 * @param target_folder - the string path to the target CSV file that will
	 *                      result from the counts
	 */
	public BatchCountExecution(String source_folder, String target_blob_counts_file) {
		folder = new File(source_folder);
		if (!folder.exists() || !folder.isDirectory())
			System.err.println("[ERROR] Directory not valid " + source_folder);
		output_file = new File(target_blob_counts_file);
		if (!output_file.getName().contains(".csv"))
			System.err.println("[WARNING] Output file is not named with csv, you may have trouble opening it.");
	}

	/**
	 * Function that executes the batch process on the files in the source folder.
	 * 
	 * @param operation - the filter that needs to be applied to all the images
	 *                  before their blobs are counted.
	 * @return - true if successful and false if unsuccessful.
	 */
	public boolean execute(Filter operation, int grey_thresh, int blob_pixel_size) {
		/*
		 * Preparation Steps: 1) Build a hash-map for the filename:cellcount
		 *
		 * Counting Steps: 1) While there is a file left in the folder 2) open the file
		 * and store it as a background image 3) filter the image 4) Count the cells
		 * with the proper parameters 5) Store the count in the hash-map with the key
		 * That is the file name.
		 * 
		 * Finishing Steps: 1) Open an input stream to the output file 2) Print out
		 * hash-map in CSV format
		 */
		Map<String, Integer> counts = new HashMap<String, Integer>();
		File[] child_files = this.folder.listFiles();

		for (File file : child_files) {
			System.out.println("Count Blobs in: " + file.getName());
			BackgroundImage image = new BackgroundImage(file);
			image.filter(operation);
			List<Point> count_points = algos.BlobFinder.find_blob_centers(image.getImage(), grey_thresh,
					blob_pixel_size, 2);
			counts.put(file.getName(), count_points.size());
		}
		
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(this.output_file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		for (String key : counts.keySet()) {
			printer.println(key + "," + counts.get(key).toString());
		}
		printer.close();
		System.out.println("Blob counts saved to " + this.output_file.getPath());

		return true;
	}

}
