package filters;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import image_viewer.Utilites;
import utilities.SliderTextCombination;

public class F_Contrast_Increase extends Filter {

	private double contrast = 0; // range of -255 to 255.
	private SliderTextCombination contrast_controller;

	@Override
	public BufferedImage filter(BufferedImage in) {
		BufferedImage buffer = this.get_blank_image(in);
		double factor = (259 * (contrast + 255)) / (255 * (259 - contrast));
		for (int i = 0; i < buffer.getWidth(); i++) {
			for (int n = 0; n < buffer.getHeight(); n++) {
				Color c = this.get_color(in, i, n);
				double red = c.getRed();
				double green = c.getGreen();
				double blue = c.getBlue();

				red = factor * (red - 128) + 128;
				green = factor * (green - 128) + 128;
				blue = factor * (blue - 128) + 128;

				red = Math.max(red, 0);
				green = Math.max(green, 0);
				blue = Math.max(blue, 0);
				red = Math.min(red, 255);
				green = Math.min(green, 255);
				blue = Math.min(blue, 255);

				buffer.setRGB(i, n, new Color((int) red, (int) green, (int) blue).getRGB());
			}
		}
		return buffer;
	}

	@Override
	public String get_params() {
		String params = super.get_params();
		return params + this.contrast;
	}

	@Override
	public void set_params(String params) {
		super.set_params(params);
		this.contrast = Double.parseDouble(params);
		this.contrast_controller.set_value(this.contrast);
	}

	@Override
	protected JPanel build_filter_edit_panel() {
		JPanel panel = super.build_filter_edit_panel();
		panel.setLayout(new GridBagLayout());

		this.contrast_controller = new SliderTextCombination("Contrast", true, 5, -255, 255, 0, true,
				10, 50, 1);
		this.contrast_controller.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				contrast = contrast_controller.get_value();
			}
		});
		Utilites.addGridComponent(panel, this.contrast_controller, 0, 0, 1, 1, 0, 0, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH);

		return panel;
	}

	@Override
	public Filter clone() {
		return new F_Contrast_Increase();
	}

}
