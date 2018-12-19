package filters;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class F_Combination extends Filter {

	/**
	 * This class is the filter for combining filters. It stores a list of all
	 * filters that it computes in order and can be stored as a single filter.
	 */
	List<Filter> filters = new ArrayList<Filter>();
	
	public F_Combination(List<Filter> filters) {
		super();
		this.filters.addAll(filters);
	}
	
	public F_Combination(Filter[] filters) {
		super();
		for (Filter f : filters)
			this.filters.add(f);
	}
	
	@Override
	public BufferedImage filter(BufferedImage in) {
		BufferedImage image = get_blank_image(in);
		Graphics g = image.getGraphics();
		g.drawImage(in, 0, 0, null);
		for (Filter filter : this.filters)
			image = filter.filter(image);
		return image;
	}

	@Override
	public Filter clone() {
		List<Filter> filter_copies = new ArrayList<Filter>();
		for (Filter f : filters)
			filter_copies.add(f.clone());
		return new F_Combination(filter_copies);
	}

}
