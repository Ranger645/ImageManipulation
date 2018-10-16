package filters;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public  class FilterManager {
	
	private Map<String, Filter> filters = new HashMap<String, Filter>();
	
	/**
	 * Stores a hash-map of string representations of each filter to
	 * the associated class for that filter.
	 */
	public FilterManager() {
		filters.put("Blue-to-Grey", new F_Blue());
		filters.put("Red-to-Grey", new F_Red());
		filters.put("Green-to-Grey", new F_Green());
		for (int i = 1; i <= 5; i++)
			filters.put("Square-Average-" + i, new F_Color_Average_Square(i));
		filters.put("Contrast Increase", new F_Contrast_Increase(128));
		filters.put("Color Inversion", new F_Invert());
	}
	
	public Filter get_filter(String name) {
		return filters.get(name);
	}
	
	public String[] get_filter_names() {
		return filters.keySet().toArray(new String[filters.size()]);
	}
	
	public void add_filter(String name, Filter f) {
		filters.put(name, f);
	}
	
}
