package image_viewer;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class ImageZoom implements MouseWheelListener, MouseListener, MouseMotionListener {

	private static final int MOVE_MINIMUM = 1;

	private Viewer image;
	private double image_x = 0, image_y = 0, mouse_x = -1, mouse_y = -1, oimage_x = 0, oimage_y = 0, zoom = -1,
			dzoom = 0;
	private double WIDTH, HEIGHT;
	private boolean left_clicked, right_clicked;
	private int window_width, window_height;

	public ImageZoom(Viewer image) {
		this.image = image;
	}

	public int[] update(int window_width, int window_height) {

		// Getting the default width and height of the image:
		this.WIDTH = this.image.get_image_width();
		this.HEIGHT = this.image.get_image_height();

		this.window_width = window_width;
		this.window_height = window_height;

		if (this.zoom < 0) {
			// Setting the zoom to initially be perfect:
			this.zoom = (int) ((window_width / ((double) this.WIDTH)) * 100);
			this.zoom = Math.min(this.zoom, (int) ((window_height / ((double) this.HEIGHT)) * 100));
		}

		// Changing the zoom:
		if (this.dzoom != 0 && !(this.zoom + this.dzoom < 10) && !(this.zoom + this.dzoom > 500)) {

			// This is the point on the image that stays stationary:
			double image_mouse_x = this.mouse_x - this.image_x;
			double image_mouse_y = this.mouse_y - this.image_y;

			// Taking down the percentages of pixel lengths to the top right quadrant of the
			// clicked point on the image.
			double x_percentage = image_mouse_x / (WIDTH * (this.zoom / 100.0));
			double y_percentage = image_mouse_y / (HEIGHT * (this.zoom / 100.0));

			// Changing the actual zoom:
			this.zoom += this.dzoom;

			// Applying the percentages to the new width and height:
			double next_length_x = (int) (x_percentage * WIDTH * (this.zoom / 100.0));
			double next_length_y = (int) (y_percentage * HEIGHT * (this.zoom / 100.0));

			// Adding the offsets to the image x and y value:
			this.image_x += image_mouse_x - next_length_x;
			this.image_y += image_mouse_y - next_length_y;
		}

		if (this.oimage_x > ImageZoom.MOVE_MINIMUM || this.oimage_y > ImageZoom.MOVE_MINIMUM
				|| this.oimage_x < -ImageZoom.MOVE_MINIMUM || this.oimage_y < -ImageZoom.MOVE_MINIMUM) {
			// Changing the offset:
			this.image_x += this.oimage_x;
			this.image_y += this.oimage_y;
		}

		this.oimage_x = this.oimage_y = this.dzoom = 0;
		return new int[] { (int) this.image_x, (int) this.image_y };
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		this.dzoom += -e.getWheelRotation();
		this.mouse_x = e.getX();
		this.mouse_y = e.getY();
		this.image.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// This has to do with clicking the mouse to add and subtract counts. That is
		// why it doesn't use update().
//		left_clicked = e.getButton() == MouseEvent.BUTTON1;
//		right_clicked = e.getButton() == MouseEvent.BUTTON3;
//		if (left_clicked == !right_clicked) {
//			double multiplier = 100.0 / this.zoom;
//			this.image.edit_closest_blob((int) (multiplier * (e.getX() - this.image_x)),
//					(int) (multiplier * (e.getY() - this.image_y)), 8, left_clicked);
//			this.image.repaint();
//		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.mouse_x = e.getX();
		this.mouse_y = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.mouse_x = -1;
		this.mouse_y = -1;

		// Mouse click was judged as a move, so correct:
		if (this.oimage_x <= ImageZoom.MOVE_MINIMUM && this.oimage_y <= ImageZoom.MOVE_MINIMUM
				&& this.oimage_x >= -ImageZoom.MOVE_MINIMUM && this.oimage_y >= -ImageZoom.MOVE_MINIMUM) {
			this.oimage_x = this.oimage_y = 0.0;
			left_clicked = e.getButton() == MouseEvent.BUTTON1;
			right_clicked = e.getButton() == MouseEvent.BUTTON3;
			if (left_clicked == !right_clicked) {
				double multiplier = 100.0 / this.zoom;
				this.image.edit_closest_blob((int) (multiplier * (e.getX() - this.image_x)),
						(int) (multiplier * (e.getY() - this.image_y)), 8, left_clicked);
				this.image.repaint();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// First time setup:
		if (this.mouse_x < 0 || this.mouse_y < 0) {
			this.mouse_x = e.getX();
			this.mouse_y = e.getY();
			return;
		}

		// Every other time:
		if (e.getButton() == MouseEvent.BUTTON1) {
			this.oimage_x += e.getX() - this.mouse_x;
			this.oimage_y += e.getY() - this.mouse_y;

			this.mouse_x = e.getX();
			this.mouse_y = e.getY();
		}

		// Calling update to update the image_x and image_y variables
		this.image.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	public void recenter() {
		this.image_x = 0;
		this.image_y = 0;
		this.zoom = (int) ((window_width / ((double) this.WIDTH)) * 100);
		this.zoom = Math.min(this.zoom, (int) ((window_height / ((double) this.HEIGHT)) * 100));
		this.mouse_x = -1;
		this.mouse_y = -1;
		this.image.repaint();
	}

	public double get_zoom_percentage() {
		return this.zoom;
	}

	public int get_mouse_x() {
		return (int) this.mouse_x;
	}

	public int get_mouse_y() {
		return (int) this.mouse_y;
	}

}
