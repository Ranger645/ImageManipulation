package image_viewer;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
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

import batch.BatchCountExecution;
import filters.F_Combination;
import filters.Filter;
import filters.FilterManager;
import tools.ImageConverter;

public class Window extends JFrame {

	private static final String TITLE = "IMAGE VIEWER v1.0";

	private List<Viewer> image_viewers = new ArrayList<>();
	private JTabbedPane tabs = new JTabbedPane();
	private JPanel gui_filter_managers = null;
	private Window self = null;

	public FilterManager filter_manager = null;

	public Window() {
		super();
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
				import_nd2();
			}
		});
		JMenuItem closeoption = new JMenuItem("Close");
		closeoption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close_current_image();
			}
		});
		file_menu.add(openoption);
		file_menu.add(opennd2option);
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
		JMenuItem split_rgb_option = new JMenuItem("Split RGB");
		JMenuItem join_images_option = new JMenuItem("Create New Overlay");
		JMenuItem merge_images_option = new JMenuItem("Merge Images");
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
		algo_menu.add(blob_count);
		algo_menu.addSeparator();
		algo_menu.add(clear_count);
		algo_menu.addSeparator();
		algo_menu.add(batch_count_res);
		algo_menu.add(batch_count_gen);

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

		Box container = Box.createHorizontalBox();
		container.add(gui_filter_managers);
		container.add(tabs);
		this.add(container);

		this.setVisible(true);
	}

	public void import_nd2() {
		String path = "res/";
		String name = JOptionPane.showInputDialog("Enter a file name in the res folder.", "header_test1.nd2");

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

			// Adding the GUI controller to the card layout and setting the card layout to
			// show the image controller that was just added.
			gui_filter_managers.add(viewer.gui_controller, viewer.KEY);
			((CardLayout) gui_filter_managers.getLayout()).show(gui_filter_managers, viewer.KEY);

		} catch (IOException e) {
			e.printStackTrace();
		}
		this.repaint();
	}

	public void open_image() {
		String path = "res/";

		String name = JOptionPane.showInputDialog("Enter a file name in the res folder.", "cells_image_4.png");
		Viewer image_viewer = new Viewer(name + tabs.getTabCount());

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
