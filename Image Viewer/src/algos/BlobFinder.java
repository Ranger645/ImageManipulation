package algos;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
	public static List<Point> find_blob_centers(BufferedImage image, int grey_thresh, int min_blob_size) {
		List<Point> points = new ArrayList<Point>();

		final int width = image.getWidth();
		final int height = image.getHeight();

		// Getting grey scale values and storing them in a single dimensional array:
		int[] pixels = new int[width * height];
		for (int i = 0; i < pixels.length; i++) {
			Color c = new Color(image.getRGB(i % width, i / width));
			pixels[i] = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
		}

		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] >= grey_thresh) {
				Point p = test_blob(pixels, i, grey_thresh, min_blob_size, width, height);
				if (p != null)
					points.add(p);
			}
		}

		return points;
	}

	/**
	 * Gets the center point of the blob containing the point (i % width, i /
	 * width). Also sets the spots in the blob to be 0s so they can't be counted
	 * again.
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
	private static Point test_blob(int[] pixels, int i, int grey_thresh, int min_blob_size, int width, int height) {
		int x = i % width;
		int y = i / width;

		List<Point> points_to_test = new ArrayList<Point>();
		List<Point> tested_points = new ArrayList<Point>();

		// Adding the first value to the array to test.
		points_to_test.add(new Point(x, y));

		while (points_to_test.size() > 0) {

			// Getting the next pixel to test the sides of, adding it to the list of tested,
			// and then setting its value in the pixel array to 0 so it doesn't get counted
			// twice.
			Point current = points_to_test.remove(0);
			tested_points.add(current);

			Point test = null;

			// top
			test = new Point(current.x, current.y - 1);
			if (get_val(test, pixels, width) >= grey_thresh)
				points_to_test.add(test);
			set_val(0, test, pixels, width);
			// bottom
			test = new Point(current.x, current.y + 1);
			if (get_val(test, pixels, width) >= grey_thresh)
				points_to_test.add(test);
			set_val(0, test, pixels, width);

			// right
			test = new Point(current.x + 1, current.y);
			if (get_val(test, pixels, width) >= grey_thresh)
				points_to_test.add(test);
			set_val(0, test, pixels, width);
			// left
			test = new Point(current.x - 1, current.y);
			if (get_val(test, pixels, width) >= grey_thresh)
				points_to_test.add(test);
			set_val(0, test, pixels, width);

			// top right
			test = new Point(current.x + 1, current.y - 1);
			if (get_val(test, pixels, width) >= grey_thresh)
				points_to_test.add(test);
			set_val(0, test, pixels, width);
			// bottom right
			test = new Point(current.x + 1, current.y + 1);
			if (get_val(test, pixels, width) >= grey_thresh)
				points_to_test.add(test);
			set_val(0, test, pixels, width);

			// top left
			test = new Point(current.x - 1, current.y - 1);
			if (get_val(test, pixels, width) >= grey_thresh)
				points_to_test.add(test);
			set_val(0, test, pixels, width);
			// bottom left
			test = new Point(current.x - 1, current.y + 1);
			if (get_val(test, pixels, width) >= grey_thresh)
				points_to_test.add(test);
			set_val(0, test, pixels, width);
		}

		Point center = new Point(0, 0);
		for (Point p : tested_points) {
			center.x += p.x;
			center.y += p.y;
		}
		center.x /= tested_points.size();
		center.y /= tested_points.size();

		return tested_points.size() >= min_blob_size ? center : null;
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
