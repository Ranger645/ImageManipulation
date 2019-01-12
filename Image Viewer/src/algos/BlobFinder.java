package algos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import filters.F_Color_Average_Square;
import filters.F_Multiply;

public class BlobFinder {

	private static BufferedImage small_pre_filter, small_post_filter, large_pre_filter, large_post_filter;

	public static List<Blob> find_blobs(BufferedImage image, int grey_thresh, int min_blob_size) {
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

		int[] original_pixels = Arrays.copyOf(pixels, pixels.length);

		List<Blob> blobs = new ArrayList<Blob>();
		// Getting the blobs themselves.
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] >= grey_thresh) {
				Blob blob = get_blob(pixels, original_pixels, i, grey_thresh, min_blob_size, width, height);
				if (blob != null) {
					blobs.add(blob);
				}
			}
		}

		return blobs;
	}

	public static List<Point> find_blob_centers(BufferedImage image, int grey_thresh, int min_blob_size) {
		List<Blob> blobs = BlobFinder.find_blobs(image, grey_thresh, min_blob_size);
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
	private static Blob get_blob(int[] pixels, int[] original, int i, int grey_thresh, int min_blob_size, int width,
			int height) {
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
			if (get_val(test, original, width) >= grey_thresh) {
				if (get_val(test, pixels, width) >= grey_thresh)
					points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);
			// bottom
			test = new Point(current.x, current.y + 1);
			if (get_val(test, original, width) >= grey_thresh) {
				if (get_val(test, pixels, width) >= grey_thresh)
					points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);

			// right
			test = new Point(current.x + 1, current.y);
			if (get_val(test, original, width) >= grey_thresh) {
				if (get_val(test, pixels, width) >= grey_thresh)
					points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);
			// left
			test = new Point(current.x - 1, current.y);
			if (get_val(test, original, width) >= grey_thresh) {
				if (get_val(test, pixels, width) >= grey_thresh)
					points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);

			// top right
			test = new Point(current.x + 1, current.y - 1);
			if (get_val(test, original, width) >= grey_thresh) {
				if (get_val(test, pixels, width) >= grey_thresh)
					points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);
			// bottom right
			test = new Point(current.x + 1, current.y + 1);
			if (get_val(test, original, width) >= grey_thresh) {
				if (get_val(test, pixels, width) >= grey_thresh)
					points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);

			// top left
			test = new Point(current.x - 1, current.y - 1);
			if (get_val(test, original, width) >= grey_thresh) {
				if (get_val(test, pixels, width) >= grey_thresh)
					points_to_test.add(test);
				surrounding_count++;
			}
			set_val(0, test, pixels, width);
			
			// bottom left
			test = new Point(current.x - 1, current.y + 1);
			if (get_val(test, original, width) >= grey_thresh) {
				if (get_val(test, pixels, width) >= grey_thresh)
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
