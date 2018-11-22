package filters;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class F_Invert extends Filter {

	@Override
	public BufferedImage filter(BufferedImage in) {
		BufferedImage buffer = this.get_blank_image(in);
		for (int i = 0; i < buffer.getWidth(); i++) {
			for (int n = 0; n < buffer.getHeight(); n++) {
				Color c = this.get_color(in, i, n);
				buffer.setRGB(i, n, new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()).getRGB());
			}
		}
		return buffer;
	}

	@Override
	public Filter clone() {
		return new F_Invert();
	}

}
