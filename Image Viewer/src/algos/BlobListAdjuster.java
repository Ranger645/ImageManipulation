package algos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

import filters.F_Multiply;

public class BlobListAdjuster {

	////////////////////// SOFT ADJUSTMENTS //////////////////////

	/**
	 * Adds blob counts to each blob based on the average blob size of the center
	 * set of blobs.
	 * 
	 * @param blobs
	 * @param min_number
	 * @param max_number
	 */
	public static void adjust_max_size_split(List<Blob> blobs, int min_number, int max_number) {
		Collections.sort(blobs, new BlobSortBySize());

		int sum = 0;
		double summed = blobs.size() - min_number - max_number;
		for (int i = 0; i < summed; i++)
			sum += blobs.get(i + min_number).points.size();

		int average = (int) (sum / summed);
		for (int i = blobs.size() - max_number; i < blobs.size(); i++) {
			blobs.get(i).setType(1);
			blobs.get(i).set_count(blobs.get(i).points.size() / average);
		}
	}

	////////////////////// HARD ADJUSTMENTS //////////////////////

	/**
	 * Multiplies the pixels in the mid range of blobs and then re-finds the blobs
	 * in that multiplied image.
	 * 
	 * @param blobs
	 * @param original_image
	 * @param min_number
	 * @param min_mult
	 * @param grey_thresh
	 * @param min_blob_size
	 * @return
	 */
	public static List<Blob> adjust_min_mult(List<Blob> blobs, BufferedImage original_image, int min_number,
			double min_mult, int grey_thresh, int min_blob_size) {
		// Sorting the blobs and getting the number of small ones:
		Collections.sort(blobs, new BlobSortBySize());

		// Setting up the blank image to filter:
		BufferedImage small_blob_image = new BufferedImage(original_image.getWidth(), original_image.getHeight(),
				original_image.getType());
		Graphics2D g = small_blob_image.createGraphics();
		g.setPaint(Color.BLACK);
		g.fillRect(0, 0, small_blob_image.getWidth(), small_blob_image.getHeight());

		// Adding the blobs we need to filter to the image:
		for (int i = 0; i < min_number; i++) {
			blobs.get(0).setType(-1);
			for (Point p : blobs.get(0).points) {
				small_blob_image.setRGB(p.x, p.y, original_image.getRGB(p.x, p.y));
			}
			blobs.remove(0);
		}

		// Filtering the small blob image:
		F_Multiply small_filter_mult = new F_Multiply();
		small_filter_mult.set_values(min_mult, min_mult, min_mult);
		small_blob_image = small_filter_mult.filter(small_blob_image);

		// Getting the small blobs from the new small blob image:
		List<Blob> small_blobs = BlobFinder.find_blobs(small_blob_image, grey_thresh, min_blob_size);
		for (Blob b : small_blobs)
			b.setType(-1);
		blobs.addAll(small_blobs);

		return blobs;
	}

}
