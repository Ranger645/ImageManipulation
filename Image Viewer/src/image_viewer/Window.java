package image_viewer;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import batch.BackgroundNd2Counter;
import batch.BatchCountExecution;
import filters.F_Combination;
import filters.Filter;
import filters.FilterManager;
import tools.ImageConverter;
import utilities.FileUtilities;

public class Window extends JFrame {

	private static final String TITLE = "IMAGE VIEWER v1.0";

	private List<Viewer> image_viewers = new ArrayList<>();
	private JTabbedPane tabs = new JTabbedPane();
	private JPanel gui_filter_managers = null;
	private Window self = null;

	public FilterManager filter_manager = null;
	
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
		JMenuItem closeoption = new JMenuItem("Close");
		closeoption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close_current_image();
			}
		});
		JMenuItem save_filter_list = new JMenuItem("Save Filter File");
		save_filter_list.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Viewer selected = get_selected_viewer();
				File filter_file = FileUtilities.showFileSaveDialog(DOCUMENTS, selected.KEY, "filter", self);
				if (filter_file == null)
					return;
				
				try {
					PrintWriter file_write = new PrintWriter(filter_file);
					file_write.print(filter_manager.encode_filters(get_selected_viewer().get_filters()));
					file_write.close();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		});
		JMenuItem save_count_file = new JMenuItem("Save Count File");
		save_count_file.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Viewer selected = get_selected_viewer();
				File filter_file = FileUtilities.showFileSaveDialog(DOCUMENTS, selected.KEY, "count", self);
				if (filter_file == null)
					return;
				
				try {
					PrintWriter file_write = new PrintWriter(filter_file);
					file_write.println(selected.KEY + "," + selected.gui_controller.get_grey_thresh() + ","
							+ selected.gui_controller.get_blob_size());
					file_write.print(filter_manager.encode_filters(selected.get_filters()));
					file_write.close();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		});
		JMenuItem save_image_batch_file = new JMenuItem("Save Batch Settings File");
		save_image_batch_file.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File batch_file = FileUtilities.showFileSaveDialog(DOCUMENTS, "batch", "batch", self);
				if (batch_file == null)
					return;
				
				try {
					PrintWriter file_write = new PrintWriter(batch_file);
					
					// Printing the header:
					file_write.println(batch_file.getName() + "," + image_viewers.size());
					file_write.println("#####");
					
					// Printing the Viewers each separated by Lines with #####
					for (Viewer v : image_viewers) {
						file_write.println(v.KEY + "," + v.gui_controller.get_grey_thresh() + ","
								+ v.gui_controller.get_blob_size());
						file_write.print(filter_manager.encode_filters(v.get_filters()));
						file_write.println("#####");
					}
					file_write.close();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		});
		JMenuItem open_filter_list = new JMenuItem("Apply Filter File");
		open_filter_list.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File filter_file = FileUtilities.showFileOpenDialog(DOCUMENTS, "filter", self);
				if (filter_file == null)
					return;
				
				try {
					FileInputStream file_read = new FileInputStream(filter_file);
					byte[] bytes = file_read.readAllBytes();
					String values = new String(bytes);

					get_selected_viewer().clear_filters();
					List<Filter> filters = filter_manager.decode_filters(values);

					for (Filter f : filters)
						get_selected_viewer().add_filter(f);

					file_read.close();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		JMenuItem open_count_file = new JMenuItem("Apply Count File");
		open_count_file.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File filter_file = FileUtilities.showFileOpenDialog(DOCUMENTS, "count", self);
				if (filter_file == null)
					return;
				
				try {
					FileInputStream file_read = new FileInputStream(filter_file);
					byte[] bytes = file_read.readAllBytes();
					String values = new String(bytes);

					String header = values.substring(0, values.indexOf("\n"));
					String[] header_values = header.split(",");
					values = values.substring(values.indexOf("\n") + 1);

					int index = -1;
					for (int i = 0; i < tabs.getTabCount(); i++)
						if (tabs.getTitleAt(i).equals(header_values[0]))
							index = i;

					Viewer to_apply_to = null;
					if (index != -1) {
						to_apply_to = (Viewer) tabs.getComponentAt(index);
					} else {
						to_apply_to = get_selected_viewer();
					}

					to_apply_to.clear_filters();
					to_apply_to.gui_controller.set_grey_thresh(Integer.parseInt(header_values[1]));
					to_apply_to.gui_controller.set_blob_size(Integer.parseInt(header_values[2]));
					List<Filter> filters = filter_manager.decode_filters(values);

					for (Filter f : filters)
						to_apply_to.add_filter(f);

					file_read.close();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		file_menu.add(openoption);
		file_menu.add(opennd2option);
		file_menu.add(open_filter_list);
		file_menu.add(open_count_file);
		file_menu.add(save_filter_list);
		file_menu.add(save_count_file);
		file_menu.add(save_image_batch_file);
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
				image_viewers.get(tabs.getSelectedIndex()).points.clear_points();
				image_viewers.get(tabs.getSelectedIndex()).repaint();
			}
		});
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
		algo_menu.add(blob_count);
		algo_menu.addSeparator();
		algo_menu.add(clear_count);
		algo_menu.addSeparator();
		algo_menu.add(batch_count_res);
		algo_menu.add(batch_count_gen);
		algo_menu.add(batch_count_nd2);

		menu.add(file_menu);
		menu.add(filter_menu);
		menu.add(image_menu);
		menu.add(algo_menu);
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
		String path = "res/";

		String name = JOptionPane.showInputDialog("Enter a file name in the res folder.", "cells_image_5.png");
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

	public void add_filter(String name) {
		this.get_selected_viewer().add_filter(filter_manager.get_filter(name));
		this.repaint();
	}

}
