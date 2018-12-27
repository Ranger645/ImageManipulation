package filters;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import image_viewer.Utilites;

public class F_Color_Average_Square extends Filter {
	/**
	 * Averages the colors around each pixel. The radius of the average is given by
	 * the pixel average radius parameter passed to the constructor higher radiuses
	 * will take more time so be wary.
	 */

	private int RADIUS;
	private JSpinner control;

	public F_Color_Average_Square() {
		super();
		RADIUS = 1;
	}

	public BufferedImage filter_slow(BufferedImage in) {
		BufferedImage buffer = this.get_blank_image(in);
		for (int i = 0; i < buffer.getWidth(); i++) {
			for (int n = 0; n < buffer.getHeight(); n++) {
				int red = 0, green = 0, blue = 0;
				for (int x = -RADIUS; x <= RADIUS; x++)
					for (int y = -RADIUS; y <= RADIUS; y++) {
						red += this.get_red(in, i + x, n + y);
						green += this.get_green(in, i + x, n + y);
						blue += this.get_blue(in, i + x, n + y);
					}
				int divisor = (RADIUS * 2 + 1) * (RADIUS * 2 + 1);
				buffer.setRGB(i, n, new Color(red / divisor, green / divisor, blue / divisor).getRGB());
			}
		}
		return buffer;
	}

	public BufferedImage filter(BufferedImage in) {
		BufferedImage buffer = this.get_blank_image(in);
		int red = 0, green = 0, blue = 0;
		int divisor = (RADIUS * 2 + 1) * (RADIUS * 2 + 1);

		int x = 0;
		int y = 0;
		int dy = 1;

		for (int i = -RADIUS; i <= RADIUS; i++) {
			for (int n = -RADIUS; n <= RADIUS; n++) {
				red += this.get_red(in, x + i, y + n);
				green += this.get_green(in, x + i, y + n);
				blue += this.get_blue(in, x + i, y + n);
			}
		}

		y = dy == 1 ? 0 : in.getHeight();
		for (;; y += dy) {
			buffer.setRGB(x, y, new Color(red / divisor, green / divisor, blue / divisor).getRGB());

			// Adding the new row and subtracting the old
			if ((y == 0 && dy == -1) || (y == in.getHeight() - 1 && dy == 1)) {
				// Moving Right:
				for (int i = -RADIUS; i <= RADIUS; i++) {
					Color add = this.get_color(in, x + RADIUS + 1, y + i);
					Color sub = this.get_color(in, x - RADIUS, y + i);
					red = red - sub.getRed() + add.getRed();
					green = green - sub.getGreen() + add.getGreen();
					blue = blue - sub.getBlue() + add.getBlue();
				}
				x++;
				dy *= -1;
				y -= dy;
				if (x == buffer.getWidth())
					break;
			} else if (dy == 1)
				// Moving Down
				for (int i = -RADIUS; i <= RADIUS; i++) {
					Color add = this.get_color(in, x + i, y + RADIUS + 1);
					Color sub = this.get_color(in, x + i, y - RADIUS);
					red = red - sub.getRed() + add.getRed();
					green = green - sub.getGreen() + add.getGreen();
					blue = blue - sub.getBlue() + add.getBlue();
				}
			else
				// Moving Up
				for (int i = -RADIUS; i <= RADIUS; i++) {
					Color add = this.get_color(in, x + i, y - RADIUS - 1);
					Color sub = this.get_color(in, x + i, y + RADIUS);
					red = red - sub.getRed() + add.getRed();
					green = green - sub.getGreen() + add.getGreen();
					blue = blue - sub.getBlue() + add.getBlue();
				}
		}
		return buffer;
	}

	@Override
	protected JPanel build_filter_edit_panel() {
		JPanel panel = super.build_filter_edit_panel();
		panel.setLayout(new GridBagLayout());
		control = new JSpinner();
		control.setValue(1);
		control.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				RADIUS = (int) control.getValue();
			}
		});
		control.setBorder(BorderFactory.createTitledBorder("Average Radius"));
		Utilites.addGridComponent(panel, control, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL);
		return panel;
	}

	@Override
	public Filter clone() {
		return new F_Color_Average_Square();
	}

	@Override
	public String get_params() {
		String params = super.get_params();
		return params + this.RADIUS;
	}

	@Override
	public void set_params(String params) {
		super.set_params(params);
		String[] param_array = params.split(",");
		this.RADIUS = Integer.parseInt(param_array[0]);
		this.control.setValue(this.RADIUS);
	}

	public void set_radius(int radius) {
		this.RADIUS = radius;
	}

}
