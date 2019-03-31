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
	private boolean edit_mode = false;
	public int zoom_percentage = 100;

	public List<Blob> last_blobs;
	public ImageController gui_controller = null;
	public String KEY = "";

	private int last_blob_count = -1;

	public Viewer(String key) {
		super();

		// COUNTER INITIALIZATION:
		this.counter = new C_Default();
		this.counter.add_display_update_listener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				point_out_blobs();
				repaint();
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
			else {
				// Display the image provided by the counting function:
				to_draw = this.counter.get_display_image(this.gui_controller.get_selected_display_mode_key());

				// If the image that the counter supplies is null, then draw the filtered image.
				if (to_draw == null)
					to_draw = image_steps.get(image_steps.size() - 1);
			}

//			this.zoom_percentage += zoom_diff;
//			double offset_multiplier = this.zoom_percentage / (this.zoom_percentage - zoom_diff);
//			this.image_transform.apply_offset_multiplier(offset_multiplier);

			int[] draw_point = this.image_transform.update(this.getWidth(), this.getHeight());
			double zoom_percentage = this.image_transform.get_zoom_percentage();
			int width = (int) (to_draw.getWidth() * (zoom_percentage / 100.0));
			int height = (int) (to_draw.getHeight() * (zoom_percentage / 100.0));

			// Drawing the image to draw on the graphics:
			g.drawImage(to_draw, draw_point[0], draw_point[1], width, height, null);
			this.last_blob_count = Utilites.paint_blob_centers(this.last_blobs, g, draw_point[0], draw_point[1],
					(int) zoom_percentage);

			// Updating the text display with the count:
			WorkingBar.set_text(String.format("Counted %d blobs.\n", this.last_blob_count));
		}
	}

	/**
	 * Adds or removes a blob at the given spot. There are basically four cases that
	 * can happen: ADDING Blob count of existing blob is increased by 1 New blob is
	 * added REMOVING Nearest blob is removed or has 1 subtracted from it Nothing
	 * happens
	 * 
	 * @param x            - the x value to start at
	 * @param y            - the y value to start at
	 * @param max_distance - the max distance from the x and y that the (x, y) point
	 *                     can be.
	 * @param add          - the option to add a blob or subtract one.
	 */
	public void edit_closest_blob(int x, int y, double max_distance, boolean add) {
		if (this.last_blobs == null)
			return;

		int closest_index = -1;
		BufferedImage original = image_steps.get(image_steps.size() - 1);

		if (this.last_blobs.size() != 0) {

			double closest_point = this.last_blobs.get(0).compute_average_point().distanceSq(new Point(x, y));

			max_distance *= max_distance;
			for (int i = 0; i < this.last_blobs.size(); i++) {
				Blob b = this.last_blobs.get(i);
				double dist = b.compute_average_point().distanceSq((double) x, (double) y);
				if (dist <= max_distance && dist <= closest_point)
					closest_index = i;
			}

		}

		if (closest_index == -1 && (x < 0 || y < 0 || x > original.getWidth() || y > original.getHeight())) {
			return;
		}

		if (add) {
			// Adding a blob:
			if (closest_index == -1) {
				// Adding a new blob:
				List<Point> point = new ArrayList<Point>();
				point.add(new Point(x, y));
				this.last_blobs.add(new Blob(point));
			} else {
				// Adding to an existing blob:
				this.last_blobs.get(closest_index).set_count(this.last_blobs.get(closest_index).get_count() + 1);
			}
		} else {
			// Removing a blob:
			if (closest_index != -1) {
				// Removing from an existing blob:
				if (this.last_blobs.get(closest_index).get_count() == 1)
					this.last_blobs.remove(closest_index);
				else
					this.last_blobs.get(closest_index).set_count(this.last_blobs.get(closest_index).get_count() - 1);
			}
		}
	}

	/**
	 * Uses the current counter to count the blobs in the current image. Also
	 * repaints this component.
	 */
	public void point_out_blobs() {
		if (!this.edit_mode) {
			this.last_blobs = counter.count(this.image_steps.get(image_steps.size() - 1));
			this.compute_blob_count();
		}
	}

	/**
	 * Computes the blob count from the last counted blobs
	 * 
	 * @return the count of the blobs or -1 if the list of calculated blobs is null.
	 */
	public int compute_blob_count() {
		if (this.last_blobs != null) {
			this.last_blob_count = 0;
			for (Blob b : this.last_blobs)
				this.last_blob_count += b.get_count();
			return this.last_blob_count;
		}
		return this.last_blob_count = -1;
	}

	/**
	 * Manually counts the blobs without a repaint. Mainly used for when this viewer
	 * is added to a window and needs to get an initial blob count.
	 * 
	 * @return the number of blobs.
	 */
	public int count_blobs() {
		this.last_blobs = counter.count(this.image_steps.get(image_steps.size() - 1));
		int total_count = 0;
		for (Blob b : this.last_blobs)
			total_count += b.get_count();
		return total_count;
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

	public void add_filters(List<Filter> filters) {
		this.filters.addAll(filters);
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
	}

	public int get_blob_count() {
		return this.last_blob_count;
	}

	public void set_edit_mode(boolean edit_mode) {
		this.edit_mode = edit_mode;
	}
	
	public int get_image_width() {
		return this.image_steps.get(0).getWidth();
	}
	
	public int get_image_height() {
		return this.image_steps.get(0).getHeight();
	}

}
