package filters;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import image_viewer.Utilites;

public class F_Threshold extends Filter {

	private int threshold;

	public F_Threshold(int threshold) {
		super();
		this.threshold = threshold;
	}

	@Override
	public BufferedImage filter(BufferedImage in) {
		BufferedImage buffer = this.get_blank_image(in);
		int black_rgb = Color.BLACK.getRGB();
		for (int i = 0; i < buffer.getWidth(); i++) {
			for (int n = 0; n < buffer.getHeight(); n++) {
				int red = this.get_red(in, i, n);
				int green = this.get_green(in, i, n);
				int blue = this.get_blue(in, i, n);
				if ((red + green + blue) / 3 >= this.threshold)
					buffer.setRGB(i, n, in.getRGB(i, n));
				else 
					buffer.setRGB(i, n, black_rgb);
			}
		}
		return buffer;
	}

	@Override
	public Filter clone() {
		return new F_Threshold(this.threshold);
	}

	@Override
	protected JPanel build_filter_edit_panel() {
		JPanel panel = super.build_filter_edit_panel();
		panel.setLayout(new GridBagLayout());

		JSlider slider = new JSlider(0, 255, 10);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(30);
		slider.setMinorTickSpacing(10);
		JTextField text = new JTextField(this.threshold + "", 5);

		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				text.setText("" + slider.getValue());
				threshold = slider.getValue();
			}
		});
		text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int value = Integer.parseInt(text.getText());
				slider.setValue(value);
			}
		});
		Utilites.addGridComponent(panel, slider, 0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL);
		Utilites.addGridComponent(panel, text, 0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL);

		return panel;
	}

}
