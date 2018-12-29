package counters;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import algos.Blob;
import algos.BlobFinder;
import algos.BlobListAdjuster;
import image_viewer.Utilites;
import utilities.ExclusiveSliderTextGroup;
import utilities.SliderTextCombination;

public class C_MinMult_MaxSizeSplit extends Counter {

	private SliderTextCombination minimum_percentage_control, minimum_multiplier, maximum_percentage_control;

	public C_MinMult_MaxSizeSplit() {
		super(new String[] { "Black/White" });
	}

	@Override
	public List<Blob> count(BufferedImage image) {
		// Getting the original blobs that were counted:
		List<Blob> blobs = BlobFinder.find_blobs(image, (int) this.grey_thresh_component.get_value(),
				(int) this.blob_size_component.getValue());

		BufferedImage black_white_image = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics2D g = black_white_image.createGraphics();
		g.drawImage(image, 0, 0, null);
		for (Blob blob : blobs)
			for (Point point : blob.points)
				black_white_image.setRGB(point.x, point.y, Color.GRAY.getRGB());
		this.display_modes.put("Black/White", black_white_image);

		// Computing the number of minimums and maximums:
		int minimum_number = (int) (blobs.size() * (minimum_percentage_control.get_value() / 100.0));
		int maximum__number = (int) (blobs.size() * ((100 - maximum_percentage_control.get_value()) / 100.0));

		// Adjusting minimum percentage:
		blobs = BlobListAdjuster.adjust_min_mult(blobs, image, minimum_number, minimum_multiplier.get_value(),
				(int) this.grey_thresh_component.get_value(), (int) this.blob_size_component.getValue());

		// Adjusting maximum percentage:
		BlobListAdjuster.adjust_max_size_split(blobs, minimum_number, maximum__number);

		return blobs;
	}

	@Override
	public JPanel create_control_panel() {
		JPanel panel = this.create_default_control_panel();

		ExclusiveSliderTextGroup min_max = new ExclusiveSliderTextGroup(2, 0, 100,
				new String[] { "Minimum Percent", "Maximum Percent" });
		SliderTextCombination[] sliders = min_max.get_sliders();
		minimum_percentage_control = sliders[0];
		minimum_percentage_control.set_draw_border(true);
		maximum_percentage_control = sliders[1];
		maximum_percentage_control.set_draw_border(true);
		minimum_percentage_control.set_value(10);
		maximum_percentage_control.set_value(90);
		minimum_multiplier = new SliderTextCombination("Minimum Multiplier", true, 5, 0, 500, 3, true, 25, 50, 100);

		Utilites.addGridComponent(panel, minimum_percentage_control, 0, 2, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL);
		Utilites.addGridComponent(panel, minimum_multiplier, 0, 3, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL);
		Utilites.addGridComponent(panel, maximum_percentage_control, 0, 4, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL);

		minimum_percentage_control.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				count_display_update();
			}
		});
		minimum_multiplier.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				count_display_update();
			}
		});
		maximum_percentage_control.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				count_display_update();
			}
		});

		return panel;
	}

	@Override
	public Counter clone() {
		return new C_MinMult_MaxSizeSplit();
	}

}
