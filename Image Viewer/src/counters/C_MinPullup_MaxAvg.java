package counters;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import algos.Blob;
import algos.BlobFinder;
import algos.BlobListAdjuster;
import algos.BlobSortBySize;
import image_viewer.Utilites;
import utilities.ExclusiveSliderTextGroup;
import utilities.SliderTextCombination;

public class C_MinPullup_MaxAvg extends Counter {

	private SliderTextCombination minimum_percentage_control, maximum_percentage_control;
	private JSpinner max_blur_control;

	protected C_MinPullup_MaxAvg() {
		super(new String[] { "Max-Pre-Blur", "Max-Post-Blur" });
	}

	@Override
	public List<Blob> count(BufferedImage image) {
		// Getting the original blobs that were counted:
		List<Blob> blobs = BlobFinder.find_blobs(image, (int) this.grey_thresh_component.get_value(),
				(int) this.blob_size_component.getValue());
		Collections.sort(blobs, new BlobSortBySize());

		// Computing the number of minimums and maximums:
		int minimum_number = (int) (blobs.size() * (minimum_percentage_control.get_value() / 100.0));
		int maximum__number = (int) (blobs.size() * ((100 - maximum_percentage_control.get_value()) / 100.0));
		int old_length = blobs.size();

		this.add_blob_cover_display_mode(image, "Max-Pre-Blur", blobs, old_length - maximum__number, blobs.size(),
				Color.GRAY);

		// Adjusting maximum percentage:
		blobs = BlobListAdjuster.adjust_max_avg_sqr(blobs, image, maximum__number,
				Math.max((int) this.max_blur_control.getValue(), 0), (int) this.grey_thresh_component.get_value(),
				(int) this.blob_size_component.getValue());
		this.add_blob_cover_display_mode(image, "Max-Post-Blur", blobs, old_length - maximum__number, blobs.size(),
				Color.GRAY);

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

		max_blur_control = new JSpinner();
		max_blur_control.setValue(3);
		JPanel blur_control_panel = new JPanel(new GridBagLayout());
		Utilites.addGridComponent(blur_control_panel, new JLabel("Max Blur Control:"), 0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
		Utilites.addGridComponent(blur_control_panel, max_blur_control, 1, 0, 1, 1, 1.5, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL);
		blur_control_panel.setBorder(BorderFactory.createTitledBorder("Max Blur Control"));

		Utilites.addGridComponent(panel, minimum_percentage_control, 0, 2, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL);
		Utilites.addGridComponent(panel, maximum_percentage_control, 0, 3, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL);
		Utilites.addGridComponent(panel, blur_control_panel, 0, 4, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL);

		minimum_percentage_control.addActionListener(new ActionListener() {
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
		max_blur_control.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				count_display_update();
			}
		});

		return panel;
	}

	@Override
	public String[] encode() {
		return null;
	}

	@Override
	public void decode(String[] to_decode) {

	}

	@Override
	public Counter clone() {
		return new C_MinPullup_MaxAvg();
	}

}
