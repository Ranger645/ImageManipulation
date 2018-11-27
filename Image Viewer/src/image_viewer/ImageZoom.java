package image_viewer;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class ImageZoom implements MouseWheelListener {
	
	private Viewer image;
	
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

}
