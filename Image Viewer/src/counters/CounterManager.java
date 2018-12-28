package counters;

import java.util.HashMap;
import java.util.Map;

public class CounterManager {
	
	private Map<String, Counter> counters = new HashMap<String, Counter>();
	
	public CounterManager() {
		counters.put("Default", new C_Default());
	}
	
	public Counter get_new_counter(String key) {
		return counters.get(key).clone();
	}

}
