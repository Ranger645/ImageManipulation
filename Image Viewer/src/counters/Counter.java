package counters;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import algos.Blob;
import image_viewer.Utilites;
import utilities.SliderTextCombination;

public abstract class Counter {

	private List<ActionListener> display_update_listeners = null;

	protected SliderTextCombination grey_thresh_component;
	protected JSpinner blob_size_component;

	/**
	 * Uses the count GUI in combination with the buffered image and this count
	 * method to count the number of blobs. This will be called every time
	 * count_display_update() is called.
	 * 
	 * @param image - the image to count
	 * @return an array-list of blobs that represent the counted blobs.
	 */
	public abstract List<Blob> count(BufferedImage image);

	/**
	 * Must be implemented to return a control panel for this count method. GUI
	 * components that need to update the count in the display must include
	 * count_display_update() in their action-listeners.
	 * 
	 * @return the constructed J-panel.
	 */
	public abstract JPanel create_control_panel();

	/**
	 * Creates the default J-panel with just the grey threshold control and blob
	 * size control mechanisms.
	 * 
	 * @return the constructed panel.
	 */
	protected JPanel create_default_control_panel() {
		JPanel panel = new JPanel(new GridBagLayout());

		grey_thresh_component = new SliderTextCombination("Threshold", true, 5, 0, 255, 100, true, 5, 10, 1);
		grey_thresh_component.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				count_display_update();
			}
		});
		JLabel blob_size_label = new JLabel("Min Blob Size: ");
		blob_size_component = new JSpinner();
		blob_size_component.setValue(15);
		blob_size_component.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				count_display_update();
			}
		});

		Utilites.addGridComponent(panel, grey_thresh_component, 0, 0, 2, 1, 1.0, 1.0);
		Utilites.addGridComponent(panel, blob_size_label, 0, 1, 1, 1, 1.0, 1.0);
		Utilites.addGridComponent(panel, blob_size_component, 1, 1, 1, 1, 1.0, 1.0);

		return panel;
	}

	/**
	 * Adds a listener to the listener chain that will be called whenever the
	 * display should be updated. Listeners containing <parent>.repaint() should be
	 * added to this.
	 * 
	 * @param action
	 */
	public void add_display_update_listener(ActionListener action) {
		if (display_update_listeners == null)
			display_update_listeners = new LinkedList<ActionListener>();
		display_update_listeners.add(action);
	}

	/**
	 * When this is called, it calls any display update listeners that have been
	 * added to this counter and fires those events:
	 */
	protected void count_display_update() {
		if (display_update_listeners == null)
			return;
		Iterator<ActionListener> iter = display_update_listeners.iterator();
		while (iter.hasNext())
			iter.next().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Count_Update"));
	}

}
