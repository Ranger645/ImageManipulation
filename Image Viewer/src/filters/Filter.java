package filters;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public abstract class Filter {
	
	private JPanel filter_config_panel = null;
	
	public Filter() {
		this.filter_config_panel = this.build_filter_edit_panel();
		this.filter_config_panel.setBorder(BorderFactory.createTitledBorder(this.getClass().getName()));
	}
	
	public abstract BufferedImage filter(BufferedImage in);
	public abstract Filter clone();
	
	protected JPanel build_filter_edit_panel() {
		return new JPanel();
	}
	
	public JPanel get_config_panel() {
		return this.filter_config_panel;
	}
	
	public String get_params() {
		return "";
	}
	
	public void set_params(String params) {
		
	}
	
	protected BufferedImage get_blank_image(BufferedImage b) {
		return new BufferedImage(b.getWidth(), b.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
	}
	
	protected Color get_color(BufferedImage b, int x, int y) {
		if (x < 0 || x >= b.getWidth() || y < 0 || y >= b.getHeight())
			return new Color(0,0,0,0);
		else 
			return new Color(b.getRGB(x, y));
	}
	
	protected int get_green(BufferedImage b, int x, int y) {
		Color c = this.get_color(b, x, y);
		return c.getGreen();
	}
	
	protected int get_red(BufferedImage b, int x, int y) {
		Color c = this.get_color(b, x, y);
		return c.getRed();
	}
	
	protected int get_blue(BufferedImage b, int x, int y) {
		Color c = this.get_color(b, x, y);
		return c.getBlue();
	}
	
	protected int get_alpha(BufferedImage b, int x, int y) {
		Color c = this.get_color(b, x, y);
		return c.getAlpha();
	}

}
