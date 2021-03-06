package filters;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class F_Blue extends Filter {
	
	@Override
	public BufferedImage filter(BufferedImage in) {
		BufferedImage buffer = this.get_blank_image(in);
		for (int i = 0; i < buffer.getWidth(); i++) {
			for (int n = 0; n < buffer.getHeight(); n++) {
				int blue = this.get_blue(in, i, n);
				buffer.setRGB(i, n, new Color(0, 0, blue).getRGB());
			}
		}
		return buffer;
	}

	@Override
	public Filter clone() {
		return new F_Blue();
	}

}
