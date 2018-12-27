package utilities;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import image_viewer.Utilites;

public class SliderTextCombination extends JPanel {

	public static final int DEFAULT_TEXT_WIDTH = 5, DEFAULT_SMALL_INTERVAL = 5, DEFAULT_LARGE_INTERVAL = 10;
	public static final boolean DEFAULT_ENABLE_BORDER = false, DEFAULT_PAINT_TICKS = true;

	// The value that will be used to convert the slider value to the text field
	// double.
	private double slider_to_text_multiplier;

	private JSlider slider;
	private JTextField text;
	private JLabel label;

	private List<ActionListener> listeners;

	public SliderTextCombination(String title, int min, int max, int value) {
		this(title, DEFAULT_ENABLE_BORDER, DEFAULT_TEXT_WIDTH, min, max, value, DEFAULT_PAINT_TICKS,
				DEFAULT_SMALL_INTERVAL, DEFAULT_LARGE_INTERVAL, 1);
	}

	public SliderTextCombination(String title, boolean enable_border, int text_width, int min, int max, double value,
			boolean paint_ticks, int small_interval, int large_interval, double slider_to_text_multiplier) {
		this.setLayout(new GridBagLayout());
		this.slider_to_text_multiplier = slider_to_text_multiplier;

		this.slider = new JSlider(min, max, (int) (value * this.slider_to_text_multiplier));
		this.slider.setPaintTicks(paint_ticks);
		this.slider.setMajorTickSpacing(large_interval);
		this.slider.setMinorTickSpacing(small_interval);
		this.slider.setToolTipText(title);
		this.slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				text.setText("" + (slider.getValue() / slider_to_text_multiplier));
				fire_listeners();
			}
		});
		Utilites.addGridComponent(this, this.slider, 0, 0, 2, 1, 1.0, 1.0);

		this.text = new JTextField("" + value, text_width);
		this.text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// Getting the number and checking to make sure its a number. If it is not a
				// number, then it sets the slider to the minimum.
				String str_value = text.getText();
				if (!Mathematics.isNumeric(str_value)) {
					slider.setValue(slider.getMinimum());
					return;
				}

				// Parsing the value and making sure it is in the slider bounds.
				double value = Double.parseDouble(str_value);
				if (value < slider.getMinimum() || value > slider.getMaximum()) {
					slider.setValue(slider.getMinimum());
					return;
				}

				// Setting the slider to the proper position:
				slider.setValue((int) (value * slider_to_text_multiplier));
				fire_listeners();
			}
		});
		Utilites.addGridComponent(this, this.text, 1, 1, 1, 1, 1.0, 1.0);

		this.label = new JLabel(title + ": ");
		Utilites.addGridComponent(this, this.label, 0, 1, 1, 1, 1.0, 1.0);

		if (enable_border)
			this.setBorder(BorderFactory.createTitledBorder(title));
	}

	protected void fire_listeners() {
		int size = this.listeners.size();
		for (int i = 0; i < size; i++)
			this.listeners.get(i)
					.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Slider_Text_Combo"));
	}

	public void addActionListener(ActionListener action) {
		if (this.listeners == null)
			this.listeners = new LinkedList<ActionListener>();
		this.listeners.add(action);
	}

	public double get_value() {
		return Double.parseDouble(this.text.getText());
	}
	
	public void set_value(double value) {
		this.slider.setValue((int) (value * this.slider_to_text_multiplier));
	}

}
