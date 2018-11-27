package filters;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import image_viewer.Utilites;

public class F_Multiply extends Filter {

	private double red, green, blue;

	public F_Multiply() {
		super();
		red = green = blue = 1.0;
	}

	@Override
	public BufferedImage filter(BufferedImage in) {
		BufferedImage buffer = this.get_blank_image(in);
		for (int i = 0; i < buffer.getWidth(); i++) {
			for (int n = 0; n < buffer.getHeight(); n++) {
				int red = this.get_red(in, i, n);
				int green = this.get_green(in, i, n);
				int blue = this.get_blue(in, i, n);

				buffer.setRGB(i, n,
						new Color((int) Math.min((this.red * red), 255), (int) Math.min((this.green * green), 255), (int) Math.min((this.blue * blue), 255)).getRGB());
			}
		}
		return buffer;
	}

	@Override
	protected JPanel build_filter_edit_panel() {
		JPanel panel = super.build_filter_edit_panel();
		panel.setLayout(new GridBagLayout());

		Box red_box = Box.createVerticalBox();
		JSlider red_slider = new JSlider(0, 200, 100);
		JTextField red_text = new JTextField("1.0", 5);
		red_text.setHorizontalAlignment(JTextField.CENTER);
		red_text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				red = Integer.parseInt(red_text.getText());
				red_slider.setValue((int) (red * 100));
			}
		});
		red_slider.setMajorTickSpacing(10);
		red_slider.setMinorTickSpacing(5);
		red_slider.setPaintTicks(true);
		red_slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				red = red_slider.getValue() / 100.0;
				red_text.setText("" + red);
			}
		});
		red_box.add(red_slider);
		red_box.add(red_text);
		red_box.setBorder(BorderFactory.createTitledBorder("Red Multiplier"));
		Utilites.addGridComponent(panel, red_box, 0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.VERTICAL);
		
		Box green_box = Box.createVerticalBox();
		JSlider green_slider = new JSlider(0, 200, 100);
		JTextField green_text = new JTextField("1.0", 5);
		green_text.setHorizontalAlignment(JTextField.CENTER);
		green_text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				green = Integer.parseInt(green_text.getText());
				green_slider.setValue((int) (green * 100));
			}
		});
		green_slider.setMajorTickSpacing(10);
		green_slider.setMinorTickSpacing(5);
		green_slider.setPaintTicks(true);
		green_slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				green = green_slider.getValue() / 100.0;
				green_text.setText("" + green);
			}
		});
		green_box.add(green_slider);
		green_box.add(green_text);
		green_box.setBorder(BorderFactory.createTitledBorder("Green Multiplier"));
		Utilites.addGridComponent(panel, green_box, 0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.VERTICAL);
		
		Box blue_box = Box.createVerticalBox();
		JSlider blue_slider = new JSlider(0, 200, 100);
		JTextField blue_text = new JTextField("1.0", 5);
		blue_text.setHorizontalAlignment(JTextField.CENTER);
		blue_text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				blue = Integer.parseInt(blue_text.getText());
				blue_slider.setValue((int) (blue * 100));
			}
		});
		blue_slider.setMajorTickSpacing(10);
		blue_slider.setMinorTickSpacing(5);
		blue_slider.setPaintTicks(true);
		blue_slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				blue = blue_slider.getValue() / 100.0;
				blue_text.setText("" + blue);
			}
		});
		blue_box.add(blue_slider);
		blue_box.add(blue_text);
		blue_box.setBorder(BorderFactory.createTitledBorder("Blue Multiplier"));
		Utilites.addGridComponent(panel, blue_box, 0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.VERTICAL);

		return panel;
	}

	@Override
	public Filter clone() {
		return new F_Multiply();
	}

}
