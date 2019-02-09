package image_viewer;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;

import batch.BatchCountManager;
import counters.Counter;
import counters.CounterManager;
import files.FileUtilities;
import files.IMFFile;
import files.ImageLoader;
import filters.FilterManager;

public class Window extends JFrame {

	private static final String TITLE = "IMAGE VIEWER v1.1";

	private List<Viewer> image_viewers = new ArrayList<>();
	private JTabbedPane tabs = new JTabbedPane();
	private JPanel gui_filter_managers = null;
	private Window self = null;

	public CounterManager counter_manager = null;

	private BatchCountManager batch_manager = null;

	public static final File DOCUMENTS = new File(System.getProperty("user.home") + File.separator + "Documents");
	public static final File DOWNLOADS = new File(System.getProperty("user.home") + File.separator + "Downloads");

	public Window() {
		super();
		System.out.println("Default Path: " + DOCUMENTS.getAbsolutePath());
		this.self = this;
		this.setSize(1000, 700);
		this.setTitle(TITLE);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		FilterManager.initialize();
		this.counter_manager = new CounterManager();
		tabs.setSize(800, 650);

		// Creating the menu bar:
		JMenuBar menu = new JMenuBar();

		JMenu file_menu = new JMenu("File");
		JMenuItem openoption = new JMenuItem("Open");
		openoption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				open_image();
			}
		});
		JMenuItem closeoption = new JMenuItem("Close Current");
		closeoption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (image_viewers.size() > 0)
					close_current_image();
				else
					System.err.println("No images are currently open.");
			}
		});
		JMenuItem save_imf_file = new JMenuItem("Save as .imf");
		save_imf_file.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File to_save = FileUtilities.showFileSaveDialog(Window.DOCUMENTS, "default", "imf", self);
				int result = JOptionPane.showConfirmDialog(self,
						"Would you like to lock in these image names? This will only allow your new .imf file to mapped saved configurations to images of the same name.",
						"Lock Names", JOptionPane.YES_NO_CANCEL_OPTION);
				if (result == JOptionPane.CANCEL_OPTION)
					return;

				IMFFile file;
				try {
					file = new IMFFile(self, result == JOptionPane.YES_OPTION);
				} catch (ParserConfigurationException e1) {
					e1.printStackTrace();
					return;
				}

				if (to_save != null) {
					int status = file.save_file(to_save.getAbsolutePath(), false);

					switch (status) {
					case 0:
						JOptionPane.showMessageDialog(self, "File successfully saved to " + to_save.getAbsolutePath(),
								"File Write", JOptionPane.OK_OPTION);
						break;
					case 1:
						status = JOptionPane.showConfirmDialog(self,
								"File at " + to_save.getAbsolutePath() + "exists already, overwrite?");
						if (status == JOptionPane.YES_OPTION)
							file.save_file(to_save.getAbsolutePath(), true);
						else
							break;
					case 2:
						if (status == 2)
							JOptionPane.showMessageDialog(self, "Error writing file.", "Error", JOptionPane.OK_OPTION);
					}
				}
			}
		});
		JMenuItem open_imf_file = new JMenuItem("Apply .imf");
		open_imf_file.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File open_file = FileUtilities.showFileOpenDialog(Window.DOCUMENTS, "imf", self);
				if (open_file == null)
					return;

				// Resolving file to open because it is not null:
				IMFFile encoded_file;
				try {
					encoded_file = new IMFFile(open_file);
				} catch (IOException e1) {
					e1.printStackTrace();
					return;
				}
				encoded_file.apply_to_window(self);
			}
		});
		file_menu.add(openoption);
		file_menu.add(open_imf_file);
		file_menu.add(save_imf_file);
		file_menu.addSeparator();
		file_menu.add(closeoption);

		JMenu filter_menu = new JMenu("Filter");
		String[] filter_names = FilterManager.get_filter_names();
		for (String name : filter_names) {
			JMenuItem item = new JMenuItem(name);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					add_filter(((JMenuItem) e.getSource()).getText());
				}
			});
			filter_menu.add(item);
		}
		filter_menu.addSeparator();
		JMenuItem clear_filters_item = new JMenuItem("Clear Filters");
		clear_filters_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image_viewers.get(tabs.getSelectedIndex()).clear_filters();
			}
		});
		filter_menu.add(clear_filters_item);

		JMenu image_menu = new JMenu("Image");
		JMenuItem duplicate_image_option = new JMenuItem("Duplicate Image");
		JMenuItem split_rgb_option = new JMenuItem("Split RGB");
		JMenuItem join_images_option = new JMenuItem("Create New Overlay");
		JMenuItem merge_images_option = new JMenuItem("Merge Images");
		image_menu.add(duplicate_image_option);
		image_menu.add(split_rgb_option); // splits image into 3 images for RGB
		image_menu.add(join_images_option); // creates new image of selected
		image_menu.add(merge_images_option); // merges images into one new image.

		JMenu algo_menu = new JMenu("Counting");
		JMenuItem blob_count = new JMenuItem("Count Blobs");
		blob_count.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image_viewers.get(tabs.getSelectedIndex()).point_out_blobs();
			}
		});
		JMenuItem clear_count = new JMenuItem("Clear Count");
		clear_count.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image_viewers.get(tabs.getSelectedIndex()).last_blobs.clear();
				image_viewers.get(tabs.getSelectedIndex()).repaint();
			}
		});
		Map<String, Counter> all_counters = this.counter_manager.getCounters();
		ButtonGroup counter_group = new ButtonGroup();
		for (String s : all_counters.keySet()) {
			JRadioButtonMenuItem current_counter_menu_item = new JRadioButtonMenuItem(s);
			current_counter_menu_item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					get_selected_viewer().set_counter(counter_manager.get_new_counter(s));
					repaint();
				}
			});
			counter_group.add(current_counter_menu_item);
			if (s.equals("Default"))
				counter_group.setSelected(current_counter_menu_item.getModel(), true);

			algo_menu.add(current_counter_menu_item);
		}
		algo_menu.addSeparator();
		algo_menu.add(blob_count);
		algo_menu.add(clear_count);

		JMenu batch_menu = new JMenu("Batch");
		JMenuItem batch_start = new JMenuItem("Start");
		batch_start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// Getting information about which files/directories to use:
				Thread batch_start_thread = new Thread() {
					public void run() {
						File[] output = BatchCountConfigWindow.show_config_dialog(self);
						if (output == null)
							return;

						batch_manager = new BatchCountManager(output[0], output[1], output[2]);
						batch_manager.start();

						if (self.close_all_images())
							self.add_viewers(batch_manager.get_next_viewers());
						else {
							batch_manager.stop_process();
							batch_manager = null;
						}
					}
				};
				batch_start_thread.start();
			}
		});
		JMenuItem batch_next = new JMenuItem("Next");
		batch_next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Viewer[] viewers = get_viewers();
				if (batch_manager != null && (batch_manager.in_progress() || batch_manager.is_on_last())
						&& self.close_all_images()) {
					int[] counts = new int[viewers.length];
					for (int i = 0; i < counts.length; i++)
						counts[i] = viewers[i].get_blob_count();
					batch_manager.write_file_line(batch_manager.get_file_name(), counts);
					self.add_viewers(batch_manager.get_next_viewers());
					if (batch_manager.is_on_last()) {
						WorkingBar.set_text(
								"Batch count completed, output saved to " + batch_manager.get_output_path_string());
						batch_manager.stop_process();
						repaint();
					}
				} else if (batch_manager == null || !batch_manager.in_progress())
					System.err.println("No batch process running.");
			}
		});
		JMenuItem batch_stop = new JMenuItem("Stop");
		batch_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (batch_manager != null && batch_manager.in_progress())
					batch_manager.stop_process();
				else
					System.err.println("No batch process running.");
			}
		});
		batch_menu.add(batch_start);
		batch_menu.add(batch_next);
		batch_menu.add(batch_stop);

		menu.add(file_menu);
		menu.add(filter_menu);
		menu.add(image_menu);
		menu.add(algo_menu);
		menu.add(batch_menu);
		this.setJMenuBar(menu);

		gui_filter_managers = new JPanel();
		gui_filter_managers.setLayout(new CardLayout());
		gui_filter_managers.setMaximumSize(new Dimension(800, 100000));

		tabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (tabs.getSelectedComponent() != null)
					((CardLayout) gui_filter_managers.getLayout()).show(gui_filter_managers,
							((Viewer) tabs.getSelectedComponent()).KEY);
			}
		});

		JPanel view_panel = new JPanel();
		view_panel.setLayout(new GridBagLayout());
		Utilites.addGridComponent(view_panel, gui_filter_managers, 0, 0, 1, 6, 0.15, 1, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(view_panel, tabs, 1, 0, 1, 5, 1, 1, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH);
		WorkingBar.get_bar().setPreferredSize(new Dimension(100000, 25));
		Utilites.addGridComponent(view_panel, WorkingBar.get_bar(), 1, 5, 1, 1, 0, 0.025, GridBagConstraints.SOUTH,
				GridBagConstraints.BOTH);

		this.add(view_panel);
		this.setVisible(true);
	}

	protected void add_viewers(Viewer[] viewers) {
		for (Viewer viewer : viewers)
			this.add_viewer(viewer);
	}

	protected void add_viewer(Viewer viewer) {
		if (viewer == null)
			return;

		image_viewers.add(viewer);
		tabs.addTab(viewer.KEY, viewer);

		// Adding the GUI controller to the card layout
		gui_filter_managers.add(viewer.gui_controller, viewer.KEY);
	}

	/**
	 * This takes in a file pointing to an image to open
	 * 
	 * @param image the file that points to the image.
	 */
	public void open_image(File image) {
		if (image == null || !ImageLoader.is_valid_file(image))
			JOptionPane.showMessageDialog(this, "Error opening file.", "ERROR", JOptionPane.ERROR_MESSAGE);

		Viewer[] viewers = ImageLoader.load_image(image);
		this.add_viewers(viewers);

		this.repaint();
	}

	public void open_image() {
		File to_open = FileUtilities.showFileOpenDialog(new File("res"), this);
		if (to_open == null) {
			System.out.println("No file to open selected.");
		}

		this.open_image(to_open);
	}

	public boolean close_all_images() {
		if (JOptionPane.showConfirmDialog(this, "That operation will close all currently open images. Continue?",
				"Closing Viewers", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return false;
		while (tabs.getTabCount() > 0)
			this.close_current_image();
		return true;
	}

	public void close_current_image() {
		Viewer to_close = image_viewers.get(tabs.getSelectedIndex());
		to_close.close();
		gui_filter_managers.remove(to_close.gui_controller);
		((CardLayout) gui_filter_managers.getLayout()).removeLayoutComponent(to_close.gui_controller);
		image_viewers.remove(tabs.getSelectedIndex());
		tabs.remove(tabs.getSelectedIndex());
		this.repaint();
	}

	public Viewer get_selected_viewer() {
		return image_viewers.get(tabs.getSelectedIndex());
	}

	/**
	 * Returns an array of the viewers in this window. The selected viewer is the
	 * first one in the list.
	 * 
	 * @return an array of references to Viewer objects.
	 */
	public Viewer[] get_viewers() {
		Viewer[] viewers = new Viewer[this.image_viewers.size()];
		for (int i = 0; i < viewers.length; i++)
			viewers[i] = this.image_viewers.get(i);
		if (viewers.length == 0)
			return viewers;
		Viewer first = viewers[0];
		viewers[0] = this.get_selected_viewer();
		viewers[this.image_viewers.indexOf(this.get_selected_viewer())] = first;
		return viewers;
	}

	public void add_filter(String name) {
		this.get_selected_viewer().add_filter(FilterManager.get_filter(name));
		this.repaint();
	}

}
