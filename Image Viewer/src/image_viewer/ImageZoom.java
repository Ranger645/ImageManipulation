package image_viewer;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class ImageZoom implements MouseWheelListener, MouseListener, MouseMotionListener {

	private Viewer image;
	private int image_x = 0, image_y = 0, mouse_x = -1, mouse_y = -1;
	private boolean left_clicked, right_clicked;

	public ImageZoom(Viewer image) {
		this.image = image;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		this.image.zoom_percentage += e.getWheelRotation();
		this.image.zoom_percentage = Math.max(this.image.zoom_percentage, 5);
		this.image.zoom_percentage = Math.min(this.image.zoom_percentage, 500);
		this.image.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		left_clicked = e.getButton() == MouseEvent.BUTTON1;
		right_clicked = e.getButton() == MouseEvent.BUTTON2;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		left_clicked = e.getButton() == MouseEvent.BUTTON1 ? false : left_clicked;
		right_clicked = e.getButton() == MouseEvent.BUTTON2 ? false : left_clicked;
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
