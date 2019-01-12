package image_viewer;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import algos.Blob;
import counters.C_Default;
import counters.Counter;
import filters.Filter;

public class Viewer extends JPanel {

	private Counter counter;

	// The list of images that are the different steps in the filter process.
	private List<BufferedImage> image_steps = new ArrayList<BufferedImage>();
	// The images that store the counting steps:

	// The list of filters that get applied to the original image.
	private List<Filter> filters = new ArrayList<Filter>();

	private ImageZoom image_transform;
	private boolean continuous_blob_finding = true;
	public int zoom_percentage = 100;

	public List<Blob> last_blobs;
	public ImageController gui_controller = null;
	public String KEY = "";

	public Viewer(String key) {
		super();

		// COUNTER INITIALIZATION:
		this.counter = new C_Default();
		this.counter.add_display_update_listener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				point_out_blobs();
			}
		});

		gui_controller = new ImageController(this);
		this.addMouseWheelListener(image_transform = new ImageZoom(this));
		this.addMouseListener(image_transform);
		this.addMouseMotionListener(image_transform);
		this.KEY = key;
	}

	public void paint(Graphics g) {
		if (image_steps.size() > 0) {
			// Constructing the image to draw:
			BufferedImage to_draw = null;
			int display_mode = this.gui_controller.get_selected_display_mode();
			if (display_mode == 0) // Display the filtered image:
				to_draw = image_steps.get(image_steps.size() - 1);
			else if (display_mode == 1) // Display the original image:
				to_draw = image_steps.get(0);
			else { // Display the image provided by the counting function:
				to_draw = this.counter.get_display_image(this.gui_controller.get_selected_display_mode_key());

				// If the image that the counter supplies is null, then draw the filtered image.
				if (to_draw == null)
					to_draw = image_steps.get(image_steps.size() - 1);
			}

			// Doing the math on the zoom and offsets:
			int width = (int) (to_draw.getWidth() * (this.zoom_percentage / 100.0));
			int height = (int) (to_draw.getHeight() * (this.zoom_percentage / 100.0));
			Point offset_point = image_transform.get_image_position();
			int x = this.getWidth() / 2 - width / 2 + offset_point.x;
			int y = this.getHeight() / 2 - height / 2 + offset_point.y;

			// Drawing the image to draw on the graphics:
			g.drawImage(to_draw, x, y, width, height, null);
			int blob_count = Utilites.paint_blob_centers(this.last_blobs, g, x, y, zoom_percentage);

			// Updating the text display with the count:
			WorkingBar.set_text(String.format("Counted %d blobs.\n", blob_count));
		}
	}

	/**
	 * Uses the current counter to count the blobs in the current image. Also
	 * repaints this component.
	 */
	public void point_out_blobs() {
		last_blobs = counter.count(this.image_steps.get(image_steps.size() - 1));
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
		last_blobs.clear();
		last_blobs = null;
		filters = null;
		image_steps = null;
	}

	public List<Filter> get_filters() {
		return this.filters;
	}

	public void set_continuous_blob_finding(boolean finding) {
		this.continuous_blob_finding = finding;
	}

	public void recenter_display() {
		this.image_transform.recenter();
	}

	public Counter get_counter() {
		return this.counter;
	}

	public void set_counter(Counter c) {
		this.counter = c;
		this.counter.add_display_update_listener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				point_out_blobs();
			}
		});
		this.gui_controller.update_counter();
		this.point_out_blobs();
	}

}
