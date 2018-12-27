package image_viewer;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import counters.C_Default;
import counters.Counter;
import filters.F_Combination;
import filters.Filter;

public class ImageController extends JPanel {

	private Viewer image = null;
	private Window window = null;
	private Counter counter = null;

	private DefaultListModel<String> model = null;
	private JList<String> filter_list = null;

	private JTabbedPane control_tabs = new JTabbedPane();
	private JPanel filter_control_panel = null;
	private int last_config_index = 0;

	private static final String CONTROL = "Control";
	private static final String FILTER = "Filter";

	public ImageController(Viewer image) {
		this.image = image;
		this.setLayout(new GridBagLayout());
		
		JPanel count_control_panel = this.image.get_counter().create_control_panel();

		JPanel control_panel = new JPanel();
		control_panel.setLayout(new GridBagLayout());
		JButton btn_close_image = new JButton("Close Image");
		btn_close_image.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.close_current_image();
			}
		});
		JButton btn_recenter_image = new JButton("Recenter Image");
		btn_recenter_image.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image.recenter_display();
			}
		});

		JPanel image_select_panel = new JPanel();
		image_select_panel.setLayout(new GridBagLayout());
		JRadioButton original_image_draw_button = new JRadioButton("Original Image");
		original_image_draw_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image.set_index_to_draw(0);
				image.set_display_mode(0);
			}
		});
		JRadioButton filtered_image_draw_button = new JRadioButton("Filtered Image");
		filtered_image_draw_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image.set_index_to_draw(-1);
				image.set_display_mode(1);
			}
		});
		JRadioButton small_count_button = new JRadioButton("Small Counts");
		small_count_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image.set_display_mode(2);
			}
		});
		JRadioButton large_count_button = new JRadioButton("Large Counts");
		large_count_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image.set_display_mode(3);
			}
		});
		ButtonGroup group = new ButtonGroup();
		group.add(original_image_draw_button);
		group.add(filtered_image_draw_button);
		group.add(small_count_button);
		group.add(large_count_button);
		group.setSelected(filtered_image_draw_button.getModel(), true);

		Utilites.addGridComponent(image_select_panel, original_image_draw_button, 0, 0, 1, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
		Utilites.addGridComponent(image_select_panel, filtered_image_draw_button, 0, 1, 1, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
		Utilites.addGridComponent(image_select_panel, small_count_button, 0, 2, 1, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
		Utilites.addGridComponent(image_select_panel, large_count_button, 0, 3, 1, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
		image_select_panel.setBorder(BorderFactory.createTitledBorder("Image Draw Control"));

		Utilites.addGridComponent(control_panel, count_control_panel, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL);
		Utilites.addGridComponent(control_panel, image_select_panel, 0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL);
		Utilites.addGridComponent(control_panel, btn_recenter_image, 0, 2, 1, 1, 1.0, 0.1, GridBagConstraints.SOUTH,
				GridBagConstraints.HORIZONTAL);
		Utilites.addGridComponent(control_panel, btn_close_image, 0, 3, 1, 1, 1.0, 0.1, GridBagConstraints.SOUTH,
				GridBagConstraints.HORIZONTAL);

		JPanel filter_panel = new JPanel();
		filter_panel.setLayout(new GridBagLayout());

		model = new DefaultListModel<String>();
		filter_list = new JList<String>(model);
		filter_list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		filter_list.setLayoutOrientation(JList.VERTICAL);
		filter_list.setPrototypeCellValue("#############################################################");
		filter_list.setVisibleRowCount(10);
		filter_list.setFixedCellWidth(200);
		filter_list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Whenever a new filter is selected this event will fire.
				// For whatever reason it fires twice though...
				change_filter_config_panel(filter_list.getSelectedIndex());
			}
		});
		JScrollPane list_scroller = new JScrollPane(filter_list);
		Utilites.addGridComponent(filter_panel, list_scroller, 0, 0, 1, 4, 0, 0, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH);

		JButton btn_remove_filter = new JButton("Remove Filter");
		btn_remove_filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (filter_list.getSelectedIndex() >= 0) {
					int index = filter_list.getSelectedIndex();
					image.remove_filter(index);
					if (model.size() > 0) {
						index -= 1;
						if (index > 0)
							filter_list.setSelectedIndex(index);
						else
							filter_list.setSelectedIndex(0);
					}
				}
			}
		});
		JButton btn_combine_filters = new JButton("Combine Filters");
		btn_combine_filters.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selected = filter_list.getSelectedIndices();
				if (selected.length > 0) {
					String name = JOptionPane.showInputDialog(window, "Enter a name for the new filter");
					if (name.length() > 0) {
						List<Filter> fil = new ArrayList<Filter>();
						for (int i = 0; i < selected.length; i++)
							fil.add(image.get_filter(selected[i]));
						for (int i = selected.length - 1; i >= 0; i--)
							image.remove_filter(selected[i]);
						Filter new_fil = new F_Combination(fil);
						window.filter_manager.add_filter(name, new_fil);
						image.add_filter(window.filter_manager.get_filter(name), selected[0]);
					}
				}
			}
		});
		JButton btn_update_filters = new JButton("Recalculate");
		btn_update_filters.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image.compute_filters();
			}
		});
		Utilites.addGridComponent(filter_panel, btn_combine_filters, 0, 5, 1, 1, 0, 0, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(filter_panel, btn_remove_filter, 0, 6, 1, 1, 0, 0, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(filter_panel, btn_update_filters, 0, 7, 1, 1, 0, 0, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH);

		filter_control_panel = new JPanel(new CardLayout());
		Utilites.addGridComponent(filter_panel, filter_control_panel, 0, 8, 1, 7, 1.0, 1.0, GridBagConstraints.SOUTH,
				GridBagConstraints.BOTH);

		control_tabs.addTab(FILTER, filter_panel);
		control_tabs.addTab(CONTROL, control_panel);
		Utilites.addGridComponent(this, control_tabs, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
	}

	public void change_filter_config_panel(int index) {
		if (index >= 0) {
			this.last_config_index = index;
			CardLayout cl = (CardLayout) filter_control_panel.getLayout();
			cl.show(filter_control_panel, "" + index);
			this.repaint();
		}
	}

	public void init_window() {
		this.window = (Window) image.getParent().getParent().getParent().getParent().getParent().getParent();
	}

	public Window get_window() {
		return this.window;
	}

	public void update_filter_list() {
		List<Filter> fil = image.get_filters();
		model.clear();
		filter_control_panel.removeAll();
		filter_control_panel.setLayout(new CardLayout());
		for (int i = 0; i < fil.size(); i++) {
			model.addElement(this.get_filter_name(fil.get(i)));
			filter_control_panel.add(fil.get(i).get_config_panel(), "" + i);
		}
		if (model.size() > 0 && last_config_index < model.size()) {
			CardLayout cl = (CardLayout) filter_control_panel.getLayout();
			cl.show(filter_control_panel, "" + last_config_index);
		}
		this.repaint();
	}

	private String get_filter_name(Filter f) {
		String[] names = this.window.filter_manager.get_filter_names();
		for (String name : names)
			if (this.window.filter_manager.get_filter(name).getClass().getName() == f.getClass().getName())
				return name;
		return "NULL";
	}

	public void update_counter() {
		
	}

}
