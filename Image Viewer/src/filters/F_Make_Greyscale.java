package filters;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class F_Make_Greyscale extends Filter {

	@Override
	public BufferedImage filter(BufferedImage in) {
		BufferedImage buffer = this.get_blank_image(in);
		for (int i = 0; i < buffer.getWidth(); i++) {
			for (int n = 0; n < buffer.getHeight(); n++) {
				int color = this.get_red(in, i, n) + this.get_green(in, i, n) + this.get_blue(in, i, n);
				color /= 3;
				buffer.setRGB(i, n, new Color(color, color, color).getRGB());
			}
		}
		return buffer;
	}

	@Override
	public Filter clone() {
		return new F_Make_Greyscale();
	}

}
