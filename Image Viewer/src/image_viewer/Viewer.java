package image_viewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import algos.Blob;
import algos.BlobFinder;
import filters.Filter;
import filters.FilterManager;

public class Viewer extends JPanel {

	// The list of images that are the different steps in the filter process.
	private List<BufferedImage> image_steps = new ArrayList<BufferedImage>();
	// The images that store the counting steps:
	private BufferedImage small_blob_image = null, large_blob_image = null;

	// The list of filters that get applied to the original image.
	private List<Filter> filters = new ArrayList<Filter>();

	private boolean continuous_blob_finding = true;
	private int display_index = -1;
	private int display_mode = 1;
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
			BufferedImage to_draw = null;
			if (this.display_mode == 2) {
				// Render mode that causes the different count levels to be displayed.
				BufferedImage original = image_steps.get(image_steps.size() - 1);
				to_draw = new BufferedImage(original.getWidth() * 2 + 10, original.getHeight() * 2 + 10,
						BufferedImage.TYPE_4BYTE_ABGR);

				Graphics draw_graphics = to_draw.getGraphics();
				draw_graphics.drawImage(image_steps.get(0), 0, 0, null);
				draw_graphics.drawImage(original, original.getWidth() + 10, 0, null);

				if (small_blob_image != null)
					draw_graphics.drawImage(small_blob_image, 0, original.getHeight() + 10, null);
				if (large_blob_image != null)
					draw_graphics.drawImage(large_blob_image, original.getWidth() + 10, original.getHeight() + 10, null);

			} else // Normal rendering
			if (display_index < 0 || display_index >= image_steps.size())
				to_draw = image_steps.get(image_steps.size() - 1);
			else
				to_draw = image_steps.get(display_index);

			int width = (int) (to_draw.getWidth() * (this.zoom_percentage / 100.0));
			int height = (int) (to_draw.getHeight() * (this.zoom_percentage / 100.0));

			int x = this.getWidth() / 2 - width / 2;
			int y = this.getHeight() / 2 - height / 2;
			g.drawImage(to_draw, x, y, width, height, null);
			if (display_mode == 2)
				x += (width / 2 + 10);
			points.paint_points(g, x, y, zoom_percentage);
		}
	}

	public void point_out_blobs() {
		points.clear_points();
		List<Blob> blobs = BlobFinder.find_blobs(image_steps.get(image_steps.size() - 1),
				this.gui_controller.get_grey_thresh(), this.gui_controller.get_blob_size(), 3);

		BufferedImage original = image_steps.get(image_steps.size() - 1);
		int width = original.getWidth();
		int height = original.getHeight();
		large_blob_image = new BufferedImage(width, height, original.getType());
		Graphics large_graphics = large_blob_image.getGraphics();
		large_graphics.setColor(Color.BLACK);
		large_graphics.fillRect(0, 0, width, height);
		small_blob_image = new BufferedImage(width, height, original.getType());
		Graphics small_graphics = small_blob_image.getGraphics();
		small_graphics.setColor(Color.BLACK);
		small_graphics.fillRect(0, 0, width, height);
		
		int white_rgb = Color.WHITE.getRGB();
		
		for (Blob b : blobs) {
			points.addPoint(b.compute_average_point(), b.getType() == 0 ? Color.CYAN : Color.RED);

			if (display_mode == 2) {
				// Painting the large and small blob images if need be.
				if (b.getType() > 0) {
					// Painting the large image blob
					for (Point p : b.points) {
						large_blob_image.setRGB(p.x, p.y, white_rgb);
					}
				}
				
				if (b.getType() < 0) {
					// Painting the small image
					for (Point p : b.points) {
						small_blob_image.setRGB(p.x, p.y, white_rgb);
					}
				}
			}
		}

		WorkingBar.set_text(String.format("Counted %d blobs with a threshhold of %d and a size of %d.\n", blobs.size(),
				this.gui_controller.get_grey_thresh(), this.gui_controller.get_blob_size()));

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

	public void set_index_to_draw(int index) {
		this.display_index = index;
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

	public void set_display_mode(int mode) {
		this.display_mode = mode;
		this.repaint();
	}

}
