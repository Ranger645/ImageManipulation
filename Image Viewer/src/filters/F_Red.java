package filters;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class F_Red extends Filter{
	
	@Override
	public BufferedImage filter(BufferedImage in) {
		BufferedImage buffer = this.get_blank_image(in);
		for (int i = 0; i < buffer.getWidth(); i++) {
			for (int n = 0; n < buffer.getHeight(); n++) {
				int red = this.get_red(in, i, n);
				buffer.setRGB(i, n, new Color(red, 0, 0).getRGB());
			}
		}
		return buffer;
	}

}
