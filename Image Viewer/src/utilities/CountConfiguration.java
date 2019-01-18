package utilities;

import java.util.List;

import filters.Filter;
import filters.FilterManager;

public class CountConfiguration {

	private int grey_thresh, blob_size;
	private List<Filter> filters;

	public CountConfiguration(String config) {
		String[] lines = config.split("\n");
		String count_parameters = lines[0];
		
		String[] params = count_parameters.split(",");
		this.grey_thresh = Integer.parseInt(params[1]);
		this.blob_size = Integer.parseInt(params[2]);

		String[] filter_configurations = new String[lines.length - 1];
		for (int i = 0; i < filter_configurations.length; i++)
			filter_configurations[i] = lines[i + 1];
		String f_config = String.join("\n", filter_configurations);
		
		FilterManager manager = new FilterManager();
		this.filters = manager.decode_filters(f_config);
	}
	
	public int getGrey_thresh() {
		return grey_thresh;
	}

	public int getBlob_size() {
		return blob_size;
	}

	public List<Filter> get_filters() {
		return this.filters;
	}

}
