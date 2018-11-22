package image_viewer;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import filters.F_Combination;
import filters.Filter;

public class ImageController extends JPanel {

	private Viewer image = null;
	private Window window = null;

	private DefaultListModel<String> model = null;
	private JList<String> filter_list = null;

	private JTabbedPane control_tabs = new JTabbedPane();
	private JPanel filter_control_panel = null;

	private static final String CONTROL = "Control";
	private static final String FILTER = "Filter";

	public ImageController(Viewer image) {
		this.image = image;
		this.setLayout(new GridBagLayout());

		Box control_box = Box.createVerticalBox();
		JButton btn_close_image = new JButton("Close Image");
		btn_close_image.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.close_current_image();
			}
		});
		control_box.add(btn_close_image);
		JPanel control_panel = new JPanel();
		control_panel.add(control_box);

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
			CardLayout cl = (CardLayout) filter_control_panel.getLayout();
			cl.show(filter_control_panel, "" + index);
			this.repaint();
		}
	}

	public void init_window() {
		this.window = (Window) image.getParent().getParent().getParent().getParent().getParent().getParent();
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
		this.repaint();
	}

	private String get_filter_name(Filter f) {
		String[] names = this.window.filter_manager.get_filter_names();
		for (String name : names)
			if (this.window.filter_manager.get_filter(name).getClass().getName() == f.getClass().getName())
				return name;
		return "NULL";
	}

}
