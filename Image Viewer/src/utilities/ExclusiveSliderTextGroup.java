package utilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExclusiveSliderTextGroup {

	private SliderTextCombination[] sliders;
	
	private int interval = 0;

	/**
	 * Creates from scratch n slider text combinations that are exclusive.
	 * 
	 * @param n - the number of exclusive sliders to generate.
	 * @return an array of n sliders. The order is the order that they are separated
	 *         in. Returns null if there are invalid arguments. Max must be greater
	 *         than min and (max - min) / n must be an integer.
	 */
	public ExclusiveSliderTextGroup(int n, int min, int max, String[] interval_names) {
		// Defining the sliders array:
		sliders = new SliderTextCombination[n];

		// Checking to make sure it is valid:
		if (min > max || (max - min) % n != 0 || max - min < n || interval_names.length < n) {
			sliders = null;
			return;
		}
		this.interval = (max - min) / n;
		
		if (interval_names == null) {
			interval_names = new String[n];
			for (int i = 0; i < interval_names.length; i++)
				interval_names[i] = "Interval " + i;
		}

		// Initializing the array of sliders:
		for (int i = 0; i < n; i++) {
			sliders[i] = new SliderTextCombination(interval_names[i], i * interval + min, Math.min((i + 2) * interval + min, max),
					(i + 1) * interval);
			sliders[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int index = get_slider_index((SliderTextCombination) e.getSource());
					
					// Adjusting the slider below this slider's max
					if (index != 0) {
						sliders[index - 1].set_max((int) sliders[index].get_value());
					}
					
					// Adjusting the slider above this slider's min
					if (index != sliders.length - 1) {
						sliders[index + 1].set_min((int) sliders[index].get_value());
					}
				}
			});
		}
	}
	
	public SliderTextCombination[] get_sliders() {
		return this.sliders;
	}

	private int get_slider_index(SliderTextCombination slider) {
		for (int i = 0; i < this.sliders.length; i++)
			if (slider == this.sliders[i])
				return i;
		return -1;
	}

}
