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

public class Viewer extends JPanel {

	// The list of images that are the different steps in the filter process.
	private List<BufferedImage> image_steps = new ArrayList<BufferedImage>();

	// The list of filters that get applied to the original image.
	private List<Filter> filters = new ArrayList<Filter>();
	
	public PointManager points = null;
	public ImageController gui_controller = null;
	public String KEY = "";

	public Viewer(String key) {
		super();
		points = new PointManager();
		gui_controller = new ImageController(this);
		this.KEY = key;
	}

	public void paint(Graphics g) {
		if (image_steps.size() > 0) {
			BufferedImage to_draw = image_steps.get(image_steps.size() - 1);
			int x = this.getWidth() / 2 - to_draw.getWidth() / 2;
			int y = this.getHeight() / 2 - to_draw.getHeight() / 2;
			g.drawImage(to_draw, x, y, null);
			points.paint_points(g, x, y);
		}
	}
	
	public void point_out_blobs() {
		List<Point> blobs = BlobFinder.find_blob_centers(image_steps.get(image_steps.size() - 1), 75, 10);
		for (Point blob : blobs) {
			points.addPoint(blob);
		}
		this.repaint();
	}

	public boolean set_image(String path) throws IOException {
		File image_file = new File(path);
		if (image_file.exists()) {
			System.out.println("Setting image to " + path);

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
	private void compute_filters() {
		this.clear_filtered_images();
		if (image_steps.size() > 0)
			for (int i = 0; i < filters.size(); i++) {
				image_steps.add(filters.get(i).filter(image_steps.get(i)));
			}
		this.gui_controller.update_filter_list();
		this.repaint();
	}
	
	public Filter get_filter(int index) {
		return filters.get(index);
	}

	public void add_filter(Filter f) {
		filters.add(f);
		this.compute_next_filtered_image();
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

}
