package counters;

import java.util.HashMap;
import java.util.Map;

public class CounterManager {
	
	private Map<String, Counter> counters = new HashMap<String, Counter>();
	
	public CounterManager() {
		counters.put("Default", new C_Default());
		counters.put("Min-Mult Max-SizeSplit", new C_MinMult_MaxSizeSplit());
		counters.put("Min-Mult Max-Threshold", new C_MinMult_MaxThresh());
	}
	
	public Counter get_new_counter(String key) {
		return counters.get(key).clone();
	}
	
	public Map<String, Counter> getCounters() {
		return counters;
	}

}
