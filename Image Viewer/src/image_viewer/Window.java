package image_viewer;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import filters.FilterManager;

public class Window extends JFrame {

	private static final String TITLE = "IMAGE VIEWER v1.0";

	private List<Viewer> image_viewers = new ArrayList<>();
	private JTabbedPane tabs = new JTabbedPane();
	private JPanel gui_filter_managers = null;

	public FilterManager filter_manager = null;

	public Window() {
		super();
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
				add_image();
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
		algo_menu.add(blob_count);
		algo_menu.addSeparator();
		algo_menu.add(clear_count);

		menu.add(file_menu);
		menu.add(filter_menu);
		menu.add(algo_menu);
		this.setJMenuBar(menu);

		gui_filter_managers = new JPanel();
		gui_filter_managers.setLayout(new CardLayout());
		gui_filter_managers.setMaximumSize(new Dimension(300, 100000));

		Box container = Box.createHorizontalBox();
		container.add(gui_filter_managers);
		container.add(tabs);
		this.add(container);

		this.setVisible(true);
	}

	public void add_image() {
		String path = "res/";

		String name = JOptionPane.showInputDialog("Enter a file name in the res folder.", "cells_image_4.png");
		Viewer image_viewer = new Viewer(name + tabs.getTabCount());

		path += name;
		try {
			if (!image_viewer.set_image(path))
				throw new IOException();
			image_viewers.add(image_viewer);
			tabs.addTab(name, image_viewer);
			image_viewer.gui_controller.init_window();

			// Adding the GUI controller to the card layout and setting the card layout to
			// show the image controller that was just added.
			gui_filter_managers.add(image_viewer.gui_controller, image_viewer.KEY);
			((CardLayout) gui_filter_managers.getLayout()).show(gui_filter_managers, image_viewer.KEY);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.repaint();
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

	public void add_filter(String name) {
		image_viewers.get(tabs.getSelectedIndex()).add_filter(filter_manager.get_filter(name));
		this.repaint();
	}

}
