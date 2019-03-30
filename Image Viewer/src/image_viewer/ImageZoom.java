package image_viewer;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class ImageZoom implements MouseWheelListener, MouseListener, MouseMotionListener {

	private Viewer image;
	private int image_x = 0, image_y = 0, mouse_x = -1, mouse_y = -1, floating_mouse_x = 0, floating_mouse_y = 0, dzoom = 0;
	private boolean left_clicked, right_clicked;

	public ImageZoom(Viewer image) {
		this.image = image;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		dzoom += -e.getWheelRotation();
		this.mouse_x = e.getX();
		this.mouse_y = e.getY();
		this.image.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		left_clicked = e.getButton() == MouseEvent.BUTTON1;
		right_clicked = e.getButton() == MouseEvent.BUTTON3;
		if (left_clicked == !right_clicked) {
			this.image.edit_closest_blob(e.getX(), e.getY(), 5, left_clicked);
			this.image.repaint();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.mouse_x = -1;
		this.mouse_y = -1;
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
			int offset_x = e.getX() - this.mouse_x;
			int offset_y = e.getY() - this.mouse_y;

			this.mouse_x = e.getX();
			this.mouse_y = e.getY();

			this.image_x += offset_x;
			this.image_y += offset_y;
		}
		image.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		this.mouse_x = e.getX();
		this.mouse_y = e.getY();
		floating_mouse_x = e.getX();
		floating_mouse_y = e.getY();
	}
	
	public int get_zoom_differential() {
		int zoom = this.dzoom;
		this.dzoom = 0;
		return zoom;
	}
	
	public void apply_offset_multiplier(double multiplier) {
		this.mouse_x *= multiplier;
		this.mouse_y *= multiplier;
	}

	public Point get_image_position() {
		return new Point(this.image_x, this.image_y);
	}

	public void recenter() {
		this.image_x = 0;
		this.image_y = 0;
		this.mouse_x = -1;
		this.mouse_y = -1;
		this.image.zoom_percentage = 100;
		this.image.repaint();
	}

}
