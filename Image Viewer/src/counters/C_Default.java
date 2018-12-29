package counters;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import algos.Blob;
import algos.BlobFinder;

public class C_Default extends Counter {

	public C_Default() {
		super(new String[] {});
	}

	@Override
	public List<Blob> count(BufferedImage image) {
		return BlobFinder.find_blobs(image, (int) this.grey_thresh_component.get_value(),
				(int) this.blob_size_component.getValue());
	}

	@Override
	public JPanel create_control_panel() {
		return this.create_default_control_panel();
	}

	@Override
	public Counter clone() {
		return new C_Default();
	}

}
