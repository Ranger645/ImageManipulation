package filters;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class F_Contrast_Increase extends Filter {
	
	double contrast = 128; // range of -255 to 255.
	
	/**
	 * Constructs a contrast altering filter.
	 * @param contrast - the contrast alter coefficient. This should be in [-255, 255].
	 */
	public F_Contrast_Increase(double contrast) {
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
				
				buffer.setRGB(i, n, new Color((int)red, (int)green, (int)blue).getRGB());
			}
		}
		return buffer;
	}

}
