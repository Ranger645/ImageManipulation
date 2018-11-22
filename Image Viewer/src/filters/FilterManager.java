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
		filters.put("Blue Only", new F_Blue());
		filters.put("Red Only", new F_Red());
		filters.put("Green Only", new F_Green());
		filters.put("Square-Average", new F_Color_Average_Square());
		filters.put("Contrast Increase", new F_Contrast_Increase(128));
		filters.put("Color Inversion", new F_Invert());
		filters.put("Multiply", new F_Multiply());
	}
	
	public Filter get_filter(String name) {
		return (Filter) filters.get(name).clone();
	}
	
	public String[] get_filter_names() {
		return filters.keySet().toArray(new String[filters.size()]);
	}
	
	public void add_filter(String name, Filter f) {
		filters.put(name, f);
	}
	
}
