package filters;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Queue;

public class F_Color_Average_Square extends Filter {
	/**
	 * Averages the colors around each pixel. The radius of the average is
	 * given by the pixel average radius parameter passed to the constructor
	 * higher radiuses will take more time so be wary.
	 */

	private final int RADIUS;

	public F_Color_Average_Square(int pixel_average_radius) {
		RADIUS = pixel_average_radius;
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

}
