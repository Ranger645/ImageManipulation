package counters;

import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;

import algos.Blob;

public abstract class Counter {
	
	
	
	public abstract List<Blob> count();
	public abstract JPanel create_control_panel();
	
	public JPanel create_default_control_panel() {
		JPanel panel = new JPanel(new GridBagLayout());
		return panel;
	}

}
