package algos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import filters.F_Color_Average_Square;
import filters.F_Multiply;
import filters.F_Threshold;
import filters.Filter;

public class BlobFinder {

	private static BufferedImage small_pre_filter, small_post_filter, large_pre_filter, large_post_filter;

	public static List<Blob> find_blobs_min_max(BufferedImage image, int grey_thresh, int min_blob_size, int min,
			int max, double min_mult, int min_blur, int max_thresh) {

		final int width = image.getWidth();
		final int height = image.getHeight();

		// Getting grey scale values and storing them in a single dimensional array:
		int[] pixels = new int[width * height];
		int[] pixels_copy = new int[width * height];
		for (int i = 0; i < pixels.length; i++) {
			Color c = new Color(image.getRGB(i % width, i / width));
			int color = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
			pixels[i] = color;
			pixels_copy[i] = color;
		}

		List<Blob> blobs = new ArrayList<Blob>();
		// Getting the blobs themselves.
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] >= grey_thresh) {
				Blob blob = get_blob(pixels, i, grey_thresh, min_blob_size, width, height);
				if (blob != null) {
					blobs.add(blob);
				}
			}
		}

		if (max < 0 || min < 0)
			return blobs;

		// Now we have to take our calculated list of blobs and separate them into
		// small, average, and large. We then apply a contrast increase to the large
		// ones and a blur to the small ones and recount them.
		Collections.sort(blobs, new BlobSortBySize());
		int small_count = (int) (blobs.size() * (min / 100.0));
		int large_start = blobs.size() - (int) (blobs.size() * ((100.0 - max) / 100.0));

		// Getting the average pixel count per blob:
		double average = 0;
		int count = 0;
		for (int i = small_count; i < large_start; i++) {
			average += blobs.get(small_count).points.size();
			count++;
		}
		if (count != 0)
			average = average / count;
		System.out.println("Average = " + average);

		// Flooring the size of each blob with the average size of the middle blobs.
		for (int i = large_start; i < blobs.size(); i++) {
			blobs.get(i).set_count((int) (blobs.get(i).points.size() / average));
			blobs.get(i).setType(1);
		}

		// Now we have to rerun the big ones through the algorithm. Before we do that,
		// they have to have their contrast increased by a user configurable value.
//		BufferedImage large_blob_image = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
//		Graphics2D g = large_blob_image.createGraphics();
//		g.setPaint(Color.BLACK);
//		g.fillRect(0, 0, large_blob_image.getWidth(), large_blob_image.getHeight());
//		while (large_start < blobs.size()) {
//			blobs.get(large_start).setType(1);
//			for (Point p : blobs.get(large_start).points) {
//				large_blob_image.setRGB(p.x, p.y, image.getRGB(p.x, p.y));
//			}
//			blobs.remove(large_start);
//		}
//		large_pre_filter = large_blob_image.getSubimage(0, 0, large_blob_image.getWidth(),
//				large_blob_image.getHeight());
//		Filter large_filter = new F_Threshold(max_thresh);
//		large_blob_image = large_filter.filter(large_blob_image);
//		List<Blob> large_blobs = BlobFinder.find_blobs_min_max(large_blob_image, Math.max(grey_thresh, max_thresh),
//				min_blob_size, -1, -1, min_mult, min_blur, max_thresh);
//		for (Blob b : large_blobs)
//			b.setType(1);
//		blobs.addAll(large_blobs);
//		large_post_filter = large_blob_image.getSubimage(0, 0, large_blob_image.getWidth(),
//				large_blob_image.getHeight());

		// So we have to rerun these through the algorithm along with the middle size
		// points to see if any can be merged. This will entail a multiply and then a
		// blur. Both of these filters will be customizable.
		BufferedImage small_blob_image = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics2D g = small_blob_image.createGraphics();
		g.setPaint(Color.BLACK);
		g.fillRect(0, 0, small_blob_image.getWidth(), small_blob_image.getHeight());
		for (int i = 0; i < small_count; i++) {
			blobs.get(0).setType(-1);
			for (Point p : blobs.get(0).points) {
				small_blob_image.setRGB(p.x, p.y, image.getRGB(p.x, p.y));
			}
			blobs.remove(0);
		}
		small_pre_filter = small_blob_image.getSubimage(0, 0, small_blob_image.getWidth(),
				small_blob_image.getHeight());
		F_Multiply small_filter_mult = new F_Multiply();
		F_Color_Average_Square small_filter_avg = new F_Color_Average_Square();
		small_filter_avg.set_radius(min_blur);
		small_filter_mult.set_values(min_mult, min_mult, min_mult);
		small_blob_image = small_filter_mult.filter(small_blob_image);
		// small_blob_image = small_filter_avg.filter(small_blob_image);
		List<Blob> small_blobs = BlobFinder.find_blobs_min_max(small_blob_image, grey_thresh, min_blob_size, -1, -1,
				min_mult, min_blur, max_thresh);
		for (Blob b : small_blobs)
			b.setType(-1);
		blobs.addAll(small_blobs);
		small_post_filter = small_blob_image.getSubimage(0, 0, small_blob_image.getWidth(),
				small_blob_image.getHeight());

		return blobs;
	}

	/**
	 * Finds the centers of all the blobs in the given image. The blobs are returned
	 * as a list of points.
	 * 
	 * @param image         - the buffered image, as grey-scale as possible
	 * @param grey_thresh   - the minimum grey value to be considered a part of a
	 *                      blob
	 * @param min_blob_size - the minimum number of pixels that can make up a blob
	 *                      to make it a blob.
	 * @return - the list of points that are the centers of the blobs.
	 */
	public static List<Blob> find_blobs_iterations(BufferedImage image, int grey_thresh, int min_blob_size,
			int iterations) {

		final int width = image.getWidth();
		final int height = image.getHeight();

		// Getting grey scale values and storing them in a single dimensional array:
		int[] pixels = new int[width * height];
		int[] pixels_copy = new int[width * height];
		for (int i = 0; i < pixels.length; i++) {
			Color c = new Color(image.getRGB(i % width, i / width));
			int color = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
			pixels[i] = color;
			pixels_copy[i] = color;
		}

		List<Blob> blobs = new ArrayList<Blob>();
		int current_iteration = 0;
		for (; current_iteration < iterations; current_iteration++) {

			// Getting the blobs themselves.
			for (int i = 0; i < pixels.length; i++) {
				if (pixels[i] >= grey_thresh) {
					Blob blob = get_blob(pixels, i, grey_thresh, min_blob_size, width, height);
					if (blob != null) {
						blobs.add(blob);
					}
				}
			}

			if (blobs.size() == 0)
				break;

			/*
			 * Now that the list of blobs is created, we can start to mess with it. We want
			 * to get the centralized average of all the blob sizes to get the median based
			 * average. We can then use this to break up the bigger blobs into multiple.
			 * Breaking blobs can be done by first identifying how many pieces they have to
			 * be broken into, and then using an expensive algorithm to determine which
			 * points are in which blob and split them.
			 */
			Collections.sort(blobs, new BlobSortBySize());

			double cutoff_fraction = 0.21 / (current_iteration + 1);
			int median_average_blob_size = 0;
			int start = (int) (blobs.size() * cutoff_fraction);
			int end = (int) (blobs.size() * (1 - cutoff_fraction));
			for (int i = start; i <= end; i++)
				median_average_blob_size += blobs.get(i).points.size();
			median_average_blob_size /= (end - start + 1);

			int index = 0;
			if (current_iteration != iterations - 1) {
				int count = 0;
				int new_grey_thresh = 0;
				while (index < blobs.size()) {
					Blob b = blobs.get(index);
					if (b.points.size() <= median_average_blob_size * 2) {
						b.setType(current_iteration);
						index++;
					} else {
						// RIGHT HERE IS WHERE THE MAGIC HAPPENS. All the blobs that are too big are fed
						// back into the algorithm with the multiplier * grey_thresh threshold.
						// All this does is resets the points that were a part of the big blobs to their
						// original pixel values.
						for (Point p : b.points) {
							int color = get_val(p, pixels_copy, width);
							count++;
							new_grey_thresh += color;
							set_val(color, p, pixels, width);
						}
						blobs.remove(index);
					}
				}
				if (count != 0)
					grey_thresh = new_grey_thresh / count;
			}
		}
		return blobs;
	}

	public static List<Point> find_blob_centers(BufferedImage image, int grey_thresh, int min_blob_size,
			int iterations) {
		List<Blob> blobs = BlobFinder.find_blobs_iterations(image, grey_thresh, min_blob_size, iterations);
		List<Point> points = new ArrayList<Point>();
		for (Blob b : blobs)
			points.add(b.compute_average_point());
		return points;
	}

	/**
	 * Gets the blob object of the blob containing the point (i % width, i / width).
	 * Also sets the spots in the blob to be 0s so they can't be counted again.
	 * 
	 * @param pixels        - a single dimensional representation of the image
	 * @param i             - the index of the pixel that is in the blob.
	 * @param grey_thresh   - the minimum value in the pixels array to be a part of
	 *                      the blob.
	 * @param min_blob_size - the minimum size of the blob for it to be a real blob
	 * @param width         - the width of the image.
	 * @param height        - the height of the image.
	 * @return - the average of all the points in the blob.
	 */
	private static Blob get_blob(int[] pixels, int i, int grey_thresh, int min_blob_size, int width, int height) {
		int x = i % width;
		int y = i / width;

		List<Point> points_to_test = new ArrayList<Point>();
		List<Point> tested_points = new ArrayList<Point>();
		List<Point> edge_points = new ArrayList<Point>();

		// Adding the first value to the array to test.
		points_to_test.add(new Point(x, y));

		while (points_to_test.size() > 0) {

			// Getting the next pixel to test the sides of, adding it to the list of tested,
			// and then setting its value in the pixel array to 0 so it doesn't get counted
			// twice.
			Point current = points_to_test.remove(0);
			tested_points.add(current);

			Point test = null;
			int surrounding_count = 0;

			// top
			test = new Point(current.x, current.y - 1);
			if (get_val(test, pixels, width) >= grey_thresh) {
				points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);
			// bottom
			test = new Point(current.x, current.y + 1);
			if (get_val(test, pixels, width) >= grey_thresh) {
				points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);

			// right
			test = new Point(current.x + 1, current.y);
			if (get_val(test, pixels, width) >= grey_thresh) {
				points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);
			// left
			test = new Point(current.x - 1, current.y);
			if (get_val(test, pixels, width) >= grey_thresh) {
				points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);

			// top right
			test = new Point(current.x + 1, current.y - 1);
			if (get_val(test, pixels, width) >= grey_thresh) {
				points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);
			// bottom right
			test = new Point(current.x + 1, current.y + 1);
			if (get_val(test, pixels, width) >= grey_thresh) {
				points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);

			// top left
			test = new Point(current.x - 1, current.y - 1);
			if (get_val(test, pixels, width) >= grey_thresh) {
				points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);

			// bottom left
			test = new Point(current.x - 1, current.y + 1);
			if (get_val(test, pixels, width) >= grey_thresh) {
				points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);

			if (surrounding_count != 8)
				edge_points.add(current);
		}
		return tested_points.size() >= min_blob_size ? new Blob(tested_points, edge_points) : null;
	}

	private static int get_val(Point p, int[] pixels, int width) {
		if (p.x < 0 || p.y < 0 || p.x >= width || p.y >= pixels.length / width)
			return 0;
		return pixels[p.y * width + p.x];
	}

	private static void set_val(int val, Point p, int[] pixels, int width) {
		if (p.x < 0 || p.y < 0 || p.x >= width || p.y >= pixels.length / width)
			return;
		pixels[p.y * width + p.x] = val;
	}

	public static BufferedImage getSmall_pre_filter() {
		return small_pre_filter;
	}

	public static BufferedImage getSmall_post_filter() {
		return small_post_filter;
	}

	public static BufferedImage getLarge_pre_filter() {
		return large_pre_filter;
	}

	public static BufferedImage getLarge_post_filter() {
		return large_post_filter;
	}

}
