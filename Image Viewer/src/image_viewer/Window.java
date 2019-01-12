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
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
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

import batch.BackgroundNd2Counter;
import batch.BatchCountExecution;
import counters.Counter;
import counters.CounterManager;
import files.FileUtilities;
import files.IMFFile;
import filters.F_Combination;
import filters.Filter;
import filters.FilterManager;
import tools.ImageConverter;

public class Window extends JFrame {

	private static final String TITLE = "IMAGE VIEWER v1.1";

	private List<Viewer> image_viewers = new ArrayList<>();
	private JTabbedPane tabs = new JTabbedPane();
	private JPanel gui_filter_managers = null;
	private Window self = null;

	public FilterManager filter_manager = null;
	public CounterManager counter_manager = null;

	public static final File DOCUMENTS = new File(System.getProperty("user.home") + File.separator + "Documents");
	public static final File DOWNLOADS = new File(System.getProperty("user.home") + File.separator + "Downloads");

	public Window() {
		super();
		System.out.println("Default Path: " + DOCUMENTS.getAbsolutePath());
		this.self = this;
		this.setSize(1000, 700);
		this.setTitle(TITLE);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		filter_manager = new FilterManager();
		counter_manager = new CounterManager();
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
		JMenuItem opennd2option = new JMenuItem("Open nd2");
		opennd2option.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread() {
					public void run() {
						import_nd2();
					}
				};
				t.start();
			}
		});
		JMenuItem closeoption = new JMenuItem("Close Current");
		closeoption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close_current_image();
			}
		});
		JMenuItem save_imf_file = new JMenuItem("Save as .imf");
		save_imf_file.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IMFFile file;
				try {
					file = new IMFFile(self, false);
				} catch (ParserConfigurationException e1) {
					e1.printStackTrace();
					return;
				}
				File to_save = FileUtilities.showFileSaveDialog(Window.DOCUMENTS, "default", "imf", self);
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
		JMenuItem open_imf_file = new JMenuItem("Open .imf");
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
		file_menu.add(opennd2option);
		file_menu.add(open_imf_file);
		file_menu.add(save_imf_file);
		file_menu.addSeparator();
		file_menu.add(closeoption);

		JMenu filter_menu = new JMenu("Filter");
		String[] filter_names = filter_manager.get_filter_names();
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
		JMenuItem batch_count_res = new JMenuItem("Batch Count res/");
		batch_count_res.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BatchCountExecution exec = new BatchCountExecution("res/", "temp/out.csv");
				Viewer open_viewer = image_viewers.get(tabs.getSelectedIndex());
				Filter all = new F_Combination(open_viewer.get_filters());
				exec.execute(all, 75, 10);
			}
		});
		JMenuItem batch_count_gen = new JMenuItem("Batch Count ...");
		batch_count_gen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String target_folder = "res";
				String output_file = "res/out.csv";

				JFileChooser saver = new JFileChooser();
				saver.setCurrentDirectory(new File(target_folder));
				saver.setSelectedFile(new File(output_file));
				int result = saver.showSaveDialog(self);
				File f = saver.getSelectedFile();
				if (result != saver.CANCEL_OPTION) {
					target_folder = f.getParentFile().getAbsolutePath();
					output_file = target_folder + "/" + f.getName();

					BatchCountExecution exec = new BatchCountExecution(target_folder, output_file);
					Viewer open_viewer = image_viewers.get(tabs.getSelectedIndex());
					Filter all = new F_Combination(open_viewer.get_filters());
					exec.execute(all, 75, 10);
				}
			}
		});
		JMenuItem batch_count_nd2 = new JMenuItem("Batch Count nd2...");
		batch_count_nd2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread() {
					public void run() {
						BackgroundNd2Counter counter = BatchCountConfigWindow.show_config_dialog(self);
						if (counter == null)
							return;
						counter.calculate();
					}
				};
				t.start();
			}
		});
		batch_menu.add(batch_count_res);
		batch_menu.add(batch_count_gen);
		batch_menu.add(batch_count_nd2);

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

	public void import_nd2() {
		String path = "res/";
		String name = JOptionPane.showInputDialog("Enter a file name in the res folder.", "header_test1.nd2");
		if (name == null)
			return;

		this.close_all_images();
		ImageConverter.nd2_split(new File(path + name));
		open_all_files("temp");
	}

	/**
	 * Opens unique viewers in this window for all the valid image files in the
	 * given directory path.
	 * 
	 * @param path - the string representation of the directory to open all the
	 *             images inside of.
	 */
	public void open_all_files(String path) {
		File directory = new File(path);
		File[] to_open = directory.listFiles();
		for (int i = to_open.length - 1; i >= 0; i--) {
			String file_name = to_open[i].getName();
			if (file_name.contains(".jpg") || file_name.contains(".png")) {
				Viewer new_viewer = new Viewer(file_name.substring(0, file_name.indexOf(".")));
				this.add_viewer(new_viewer, to_open[i]);
			}
		}
	}

	protected void add_viewer(Viewer viewer, File image_file) {
		try {
			if (!viewer.set_image(image_file))
				throw new IOException();
			image_viewers.add(viewer);
			tabs.addTab(viewer.KEY, viewer);
			viewer.gui_controller.init_window();

			// Adding the GUI controller to the card layout
			gui_filter_managers.add(viewer.gui_controller, viewer.KEY);

		} catch (IOException e) {
			e.printStackTrace();
		}
		this.repaint();
	}

	public void open_image() {
		String name = JOptionPane.showInputDialog("Enter a file name in the res folder.", "cells_image_5.png");

		this.open_image(name);
	}

	public void open_image(String name) {
		String path = "res/";
		String[] name_split = name.split("\\.");

		Viewer image_viewer = new Viewer(name_split[0] + "_" + tabs.getTabCount());

		path += name;
		this.add_viewer(image_viewer, new File(path));
	}

	public void close_all_images() {
		while (tabs.getTabCount() > 0)
			this.close_current_image();
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
		Viewer first = viewers[0];
		viewers[0] = this.get_selected_viewer();
		viewers[this.image_viewers.indexOf(this.get_selected_viewer())] = first;
		return viewers;
	}

	public void add_filter(String name) {
		this.get_selected_viewer().add_filter(filter_manager.get_filter(name));
		this.repaint();
	}

}
