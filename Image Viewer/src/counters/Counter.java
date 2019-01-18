package counters;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
	protected JPanel counter_control_panel = null;

	protected Map<String, BufferedImage> display_modes = null;

	protected Counter(String[] mode_names) {
		this.display_modes = new HashMap<String, BufferedImage>();
		for (String s : mode_names)
			this.display_modes.put(s, null);
		counter_control_panel = this.create_control_panel();
	}

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
	protected abstract JPanel create_control_panel();

	public abstract String[] encode();

	public abstract void decode(String[] to_decode);

	/**
	 * Returns a clone of this counter
	 */
	public abstract Counter clone();
	
	public JPanel get_control_panel() {
		return this.counter_control_panel;
	}

	/**
	 * Creates the default J-panel with just the grey threshold control and blob
	 * size control mechanisms. This is a panel using a grid bag layout. There is a
	 * 2 x 2 square at the top occupied by the default components.
	 * 
	 * @return the constructed panel.
	 */
	protected JPanel create_default_control_panel() {
		JPanel panel = new JPanel(new GridBagLayout());

		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		grey_thresh_component = new SliderTextCombination("Threshold", true, 5, 3, 255, 100, true, 5, 10, 1);
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

	public Set<String> get_display_mode_keys() {
		return this.display_modes.keySet();
	}

	public BufferedImage get_display_image(String key) {
		return this.display_modes.get(key);
	}

	protected void add_blob_cover_display_mode(BufferedImage original, String key, List<Blob> blobs, int start, int end,
			Color main_color, Color edge_color) {
		if (start > end)
			return;

		int rgb = main_color.getRGB();
		int rgb_secondary = edge_color.getRGB();

		BufferedImage display = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
		display.createGraphics().drawImage(original, 0, 0, null);
		for (int i = start; i < end; i++) {
			for (Point p : blobs.get(i).points)
				display.setRGB(p.x, p.y, rgb);
			for (Point p : blobs.get(i).edge_points)
				display.setRGB(p.x, p.y, rgb_secondary);
		}

		this.display_modes.put(key, display);
	}

	protected void add_blob_cover_display_mode(BufferedImage original, String key, List<Blob> blobs, int start, int end,
			Color main_color) {
		this.add_blob_cover_display_mode(original, key, blobs, start, end, main_color, Color.RED);
	}

	protected void add_blob_cover_display_mode(BufferedImage original, String key, List<Blob> blobs, int start,
			int end) {
		this.add_blob_cover_display_mode(original, key, blobs, start, end, Color.GRAY, Color.RED);
	}

}
