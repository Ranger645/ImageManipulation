package image_viewer;

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

import filters.F_Combination;
import filters.Filter;

public class ImageController extends JPanel {

	private Viewer image = null;
	private Window window = null;

	private DefaultListModel<String> model = null;
	private JList<String> filter_list = null;

	private JTabbedPane control_tabs = new JTabbedPane();

	private static final String CONTROL = "CONTROL";
	private static final String FILTER = "FILTER";

	public ImageController(Viewer image) {
		this.image = image;

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
		filter_list.setVisibleRowCount(30);
		filter_list.setFixedCellWidth(200);
		JScrollPane list_scroller = new JScrollPane(filter_list);

		Utilites.addGridComponent(filter_panel, list_scroller, 0, 0, 1, 4, 0, 0, GridBagConstraints.CENTER,
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
						if (index >= 0)
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
		JButton btn_edit_filter = new JButton("Edit Filter");
		Utilites.addGridComponent(filter_panel, btn_edit_filter, 0, 4, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(filter_panel, btn_combine_filters, 0, 5, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(filter_panel, btn_remove_filter, 0, 6, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);

		control_tabs.addTab(CONTROL, control_panel);
		control_tabs.addTab(FILTER, filter_panel);
		this.add(control_tabs);
	}

	public void init_window() {
		this.window = (Window) image.getParent().getParent().getParent().getParent().getParent().getParent();
	}

	public void update_filter_list() {
		List<Filter> fil = image.get_filters();
		model.clear();
		for (Filter f : fil)
			model.addElement(this.get_filter_name(f));
	}

	private String get_filter_name(Filter f) {
		String[] names = this.window.filter_manager.get_filter_names();
		for (String name : names)
			if (this.window.filter_manager.get_filter(name) == f)
				return name;
		return "NULL";
	}

}
