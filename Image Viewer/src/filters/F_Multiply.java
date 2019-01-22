package filters;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import image_viewer.Utilites;
import utilities.SliderTextCombination;

public class F_Multiply extends Filter {

	private double red, green, blue;
	private SliderTextCombination s_red, s_green, s_blue;

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

				buffer.setRGB(i, n, new Color((int) Math.min((this.red * red), 255),
						(int) Math.min((this.green * green), 255), (int) Math.min((this.blue * blue), 255)).getRGB());
			}
		}
		return buffer;
	}

	@Override
	public String get_params() {
		String params = super.get_params();
		return params + this.red + "," + this.green + "," + this.blue;
	}

	@Override
	public void set_params(String params) {
		super.set_params(params);
		String[] param_array = params.split(",");
		this.red = Double.parseDouble(param_array[0]);
		this.s_red.set_value(red);
		this.green = Double.parseDouble(param_array[1]);
		this.s_green.set_value(green);
		this.blue = Double.parseDouble(param_array[2]);
		this.s_blue.set_value(blue);
	}

	@Override
	protected JPanel build_filter_edit_panel() {
		JPanel panel = super.build_filter_edit_panel();
		panel.setLayout(new GridBagLayout());

		s_red = new SliderTextCombination("Red", true, 5, 0, 200, 1, true, 5, 10, 100.0);
		s_red.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				red = s_red.get_value();
			}
		});
		s_green = new SliderTextCombination("Green", true, 5, 0, 200, 1, true, 5, 10, 100.0);
		s_green.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				green = s_green.get_value();
			}
		});
		s_blue = new SliderTextCombination("Blue", true, 5, 0, 200, 1, true, 5, 10, 100.0);
		s_blue.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				blue = s_blue.get_value();
			}
		});
		
		Utilites.addGridComponent(panel, s_red, 0, 0, 1, 1, 1.0, 1.0);
		Utilites.addGridComponent(panel, s_green, 0, 1, 1, 1, 1.0, 1.0);
		Utilites.addGridComponent(panel, s_blue, 0, 2, 1, 1, 1.0, 1.0);

		return panel;
	}

	@Override
	public Filter clone() {
		return new F_Multiply();
	}
	
	public void set_values(double red, double green, double blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.s_red.set_value(red);
		this.s_green.set_value(green);
		this.s_blue.set_value(blue);
	}

}
