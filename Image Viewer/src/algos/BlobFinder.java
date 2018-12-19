package algos;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlobFinder {

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
	public static List<Blob> find_blobs(BufferedImage image, int grey_thresh, int min_blob_size,
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
		List<Blob> blobs = BlobFinder.find_blobs(image, grey_thresh, min_blob_size, iterations);
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

}
