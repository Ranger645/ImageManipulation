package filters;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import image_viewer.Utilites;

public class F_Contrast_Increase extends Filter {

	double contrast = 128; // range of -255 to 255.
	private JSlider slider;

	/**
	 * Constructs a contrast altering filter.
	 * 
	 * @param contrast - the contrast alter coefficient. This should be in [-255,
	 *                 255].
	 */
	public F_Contrast_Increase(double contrast) {
		super();
		this.contrast = contrast;
	}

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
		this.slider.setValue((int) (this.contrast * 100));
	}

	@Override
	protected JPanel build_filter_edit_panel() {
		JPanel panel = super.build_filter_edit_panel();
		slider = new JSlider();
		JTextField text = new JTextField();
		panel.setLayout(new GridBagLayout());

		text.setColumns(5);
		text.setText("50");
		text.setHorizontalAlignment(JTextField.CENTER);
		text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int value = Integer.parseInt(text.getText());
				slider.setValue(value);
				contrast = value;
			}
		});

		slider.setMaximum(100);
		slider.setMinimum(0);
		slider.setValue(50);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int num_values = 255 * 2 + 1;
				double ratio = slider.getValue() / 100.0 * num_values - 255;
				contrast = (int) ratio;
				contrast = Math.max(-255, contrast);
				contrast = Math.min(255, contrast);
				text.setText("" + slider.getValue());
			}
		});

		Utilites.addGridComponent(panel, slider, 0, 1, 1, 1, 0, 0, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(panel, text, 0, 2, 1, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH);
		
		return panel;
	}

	@Override
	public Filter clone() {
		return new F_Contrast_Increase(128);
	}

}
