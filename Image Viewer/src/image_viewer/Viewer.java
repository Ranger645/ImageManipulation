package image_viewer;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import algos.BlobFinder;
import filters.Filter;
import filters.FilterManager;

public class Viewer extends JPanel {

	// The list of images that are the different steps in the filter process.
	private List<BufferedImage> image_steps = new ArrayList<BufferedImage>();

	// The list of filters that get applied to the original image.
	private List<Filter> filters = new ArrayList<Filter>();

	private boolean continuous_blob_finding = true;
	public int zoom_percentage = 100;

	public PointManager points = null;
	public ImageController gui_controller = null;
	public String KEY = "";

	public Viewer(String key) {
		super();
		points = new PointManager();
		gui_controller = new ImageController(this);
		this.addMouseWheelListener(new ImageZoom(this));
		this.KEY = key;
	}

	public void paint(Graphics g) {
		if (image_steps.size() > 0) {
			BufferedImage to_draw = image_steps.get(image_steps.size() - 1);

			int width = (int) (to_draw.getWidth() * (this.zoom_percentage / 100.0));
			int height = (int) (to_draw.getHeight() * (this.zoom_percentage / 100.0));

			int x = this.getWidth() / 2 - width / 2;
			int y = this.getHeight() / 2 - height / 2;
			g.drawImage(to_draw, x, y, width, height, null);
			points.paint_points(g, x, y, zoom_percentage);
		}
	}

	public void point_out_blobs() {
		points.clear_points();
		List<Point> blobs = BlobFinder.find_blob_centers(image_steps.get(image_steps.size() - 1),
				this.gui_controller.get_grey_thresh(), this.gui_controller.get_blob_size(), 10);
		System.out.printf("Counted %d blobs with a threshhold of %d and a size of %d.\n", blobs.size(),
				this.gui_controller.get_grey_thresh(), this.gui_controller.get_blob_size());
		for (Point blob : blobs) {
			points.addPoint(blob);
		}
		this.gui_controller.update_count(blobs.size());
		this.repaint();
	}

	public boolean set_image(String path) throws IOException {
		File image_file = new File(path);
		return this.set_image(image_file);
	}

	public boolean set_image(File image_file) throws IOException {
		if (image_file.exists()) {
			// Loading the image:
			BufferedImage image = ImageIO.read(image_file);
			image_steps.clear();
			image_steps.add(image);
			this.compute_filters();

			return true;
		}
		return false;
	}

	public void clear_filters() {
		clear_filtered_images();
		filters.clear();
		this.gui_controller.update_filter_list();
		this.repaint();
	}

	private void clear_filtered_images() {
		while (image_steps.size() > 1)
			image_steps.remove(image_steps.size() - 1);
	}

	public void compute_next_filtered_image() {
		image_steps.add(filters.get(filters.size() - 1).filter(image_steps.get(image_steps.size() - 1)));
		this.gui_controller.update_filter_list();
		this.repaint();
	}

	/**
	 * Re-calculates the image_steps array list based on the first image in the list
	 * and the list of filters.
	 */
	public void compute_filters() {
		this.clear_filtered_images();
		if (image_steps.size() > 0)
			for (int i = 0; i < filters.size(); i++) {
				image_steps.add(filters.get(i).filter(image_steps.get(i)));
			}
		this.gui_controller.update_filter_list();
		if (continuous_blob_finding)
			this.point_out_blobs();
		this.repaint();
	}

	public Filter get_filter(int index) {
		return filters.get(index);
	}

	public void add_filter(Filter f) {
		filters.add(f);
		this.compute_filters();
	}

	public void add_filter(Filter f, int index) {
		filters.add(index, f);
		this.compute_filters();
	}

	public void remove_filter(int index) {
		this.filters.remove(index);
		this.compute_filters();
	}

	public void close() {
		filters.clear();
		image_steps.clear();
		points.clear_points();
		points = null;
		filters = null;
		image_steps = null;
	}

	public List<Filter> get_filters() {
		return this.filters;
	}

	public void set_continuous_blob_finding(boolean finding) {
		this.continuous_blob_finding = finding;
	}

}
