package filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import counters.Counter;

public class FilterManager {

	private static Map<String, Filter> filters = new HashMap<String, Filter>();

	/**
	 * Stores a hash-map of string representations of each filter to the associated
	 * class for that filter.
	 */
	public static void initialize() {
		filters.put("Blue Only", new F_Blue());
		filters.put("Red Only", new F_Red());
		filters.put("Green Only", new F_Green());
		filters.put("Square-Average", new F_Color_Average_Square());
		filters.put("Contrast Increase", new F_Contrast_Increase());
		filters.put("Color Inversion", new F_Invert());
		filters.put("Multiply", new F_Multiply());
		filters.put("Greyscale", new F_Make_Greyscale());
		filters.put("Threshold", new F_Threshold(10));
	}

	public static String encode_filters(List<Filter> to_encode) {
		String encoded = "";
		for (Filter f : to_encode) {
			String key = "";
			for (String k : filters.keySet())
				if (filters.get(k).getClass().getName().equals(f.getClass().getName())) {
					key = k;
					break;
				}
			encoded += key + "," + f.get_params() + "\n";
		}
		return encoded;
	}

	public static List<Filter> decode_filters(String to_decode) {
		String[] filter_lines = to_decode.split("\n");
		List<Filter> filters = new ArrayList<Filter>();
		for (String line : filter_lines) {
			String filter_name = line.substring(0, line.indexOf(","));
			Filter f = FilterManager.filters.get(filter_name).clone();

			String filter_params = "";
			if (line.indexOf(",") != line.length() - 1)
				filter_params += line.substring(line.indexOf(",") + 1);
			f.set_params(filter_params);
			filters.add(f);
		}
		return filters;
	}

	public static Filter get_filter(String name) {
		return (Filter) filters.get(name).clone();
	}

	public static String[] get_filter_names() {
		return filters.keySet().toArray(new String[filters.size()]);
	}

	public static void add_filter(String name, Filter f) {
		filters.put(name, f);
	}

	public static String get_filter_key(Filter f) {
		for (String key : filters.keySet())
			if (f.getClass().getName().equals(filters.get(key).getClass().getName()))
				return key;
		return null;

	}

}
