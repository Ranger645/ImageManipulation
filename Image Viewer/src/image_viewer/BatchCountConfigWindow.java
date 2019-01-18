package image_viewer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import batch.BackgroundNd2Counter;
import files.FileUtilities;

public class BatchCountConfigWindow extends JFrame implements ActionListener {

	private final String TITLE = "Nd2 Batch Count Configuration";
	private static String folderDefault = Window.DOCUMENTS.getAbsolutePath();
	private static String configFileDefault = "/Users/gregfoss/git/ImageManipulation/Image Viewer/res/test.batch";
	private static String outputFileDefault = Window.DOCUMENTS.getAbsolutePath() + File.separator + "batch_counts.csv";
	private static BatchCountConfigWindow staticWindow = null;

	private JFrame parent = null;
	private JPanel main_panel = null;
	private JTextField text_folder, text_config_file, text_output_file;
	private JButton btn_folder_select, btn_config_file_select, btn_output_file_select, btn_approve, btn_cancel;
	private JLabel lbl_folder_select, lbl_config_file_select, lbl_output_file_select, lbl_title;

	private int status = -1;

	public static BackgroundNd2Counter show_config_dialog(JFrame parent) {
		if (staticWindow == null) {
			// Building the window:
			staticWindow = new BatchCountConfigWindow(parent);
		}
		staticWindow.show_window(parent);

		int status = 1;
		while ((status = staticWindow.get_status()) == 1)
			try {
				Thread.sleep(1);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

		if (status == 0) {
			File count_folder = new File(staticWindow.get_folder_name());
			File config_file = new File(staticWindow.get_config_file_name());
			File output_file = new File(staticWindow.get_output_file_name());

			System.out.print("Got parameters, testing...");
			if (count_folder.exists() && count_folder.isDirectory() && config_file.exists()
					&& config_file.getName().endsWith(".batch")) {
				String config = "";
				System.out.println("Paramamters passed tests.");

				// Saving new defaults:
				folderDefault = count_folder.getAbsolutePath();
				configFileDefault = config_file.getAbsolutePath();
				outputFileDefault = output_file.getAbsolutePath();

				try {
					config = new String(Files.readAllBytes(config_file.toPath()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new BackgroundNd2Counter(count_folder, config, output_file);
			} else {
				System.out.println("Parameters failed tests.");
			}
		}
		return null;
	}

	public BatchCountConfigWindow(JFrame parent) {
		this.parent = parent;
		this.setTitle(this.TITLE);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				hide_window(-1);
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
			}
			
			@Override
			public void windowActivated(WindowEvent e) {	
			}
		});

		this.main_panel = new JPanel();
		this.main_panel.setLayout(new GridBagLayout());

		this.text_folder = new JTextField(folderDefault, 40);
		this.text_config_file = new JTextField(configFileDefault, 40);
		this.text_output_file = new JTextField(outputFileDefault, 40);

		this.btn_folder_select = new JButton("...");
		this.btn_config_file_select = new JButton("...");
		this.btn_output_file_select = new JButton("...");
		this.btn_approve = new JButton("  Ok  ");
		this.btn_cancel = new JButton("Cancel");

		this.btn_folder_select.addActionListener(this);
		this.btn_config_file_select.addActionListener(this);
		this.btn_output_file_select.addActionListener(this);
		this.btn_approve.addActionListener(this);
		this.getRootPane().setDefaultButton(this.btn_approve);
		this.btn_approve.requestFocus();
		this.btn_cancel.addActionListener(this);

		this.lbl_folder_select = new JLabel("Batch Folder:");
		this.lbl_config_file_select = new JLabel("Config File:");
		this.lbl_output_file_select = new JLabel("Output File:");
		this.lbl_title = new JLabel(this.TITLE);

		Utilites.addGridComponent(this.main_panel, this.lbl_title, 2, 0, 1, 1, 1, 1, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(this.main_panel, this.lbl_folder_select, 0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(this.main_panel, this.lbl_config_file_select, 0, 2, 1, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH);
		Utilites.addGridComponent(this.main_panel, this.lbl_output_file_select, 0, 3, 1, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH);
		Utilites.addGridComponent(this.main_panel, this.text_folder, 1, 1, 3, 1, 1, 1, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(this.main_panel, this.text_config_file, 1, 2, 3, 1, 1, 1, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(this.main_panel, this.text_output_file, 1, 3, 3, 1, 1, 1, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(this.main_panel, this.btn_folder_select, 4, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(this.main_panel, this.btn_config_file_select, 4, 2, 1, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH);
		Utilites.addGridComponent(this.main_panel, this.btn_output_file_select, 4, 3, 1, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH);
		Utilites.addGridComponent(this.main_panel, this.btn_approve, 1, 5, 1, 1, 1, 1, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
		Utilites.addGridComponent(this.main_panel, this.btn_cancel, 3, 5, 1, 1, 1, 1, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);

		this.add(this.main_panel);
		this.pack();
		this.setVisible(false);
	}

	public String get_folder_name() {
		return this.text_folder.getText();
	}

	public String get_config_file_name() {
		return this.text_config_file.getText();
	}

	public String get_output_file_name() {
		return this.text_output_file.getText();
	}

	public void show_window(JFrame new_parent) {
		this.status = 1;
		
		this.setVisible(true);
		if (new_parent != null && new_parent.isVisible()) {
			Point p_loc = new_parent.getLocation();
			this.setLocation(p_loc.x + 300, p_loc.y + 200);
		}
	}

	public void hide_window(int new_status) {
		this.status = new_status;
		this.setVisible(false);
	}

	public int get_status() {
		return status;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.btn_approve) {
			this.hide_window(0);
		} else if (e.getSource() == this.btn_cancel) {
			this.hide_window(-1);
		} else if (e.getSource() == this.btn_folder_select) {
			File current = new File(this.text_folder.getText());
			current = current.exists() ? current : new File(BatchCountConfigWindow.folderDefault);
			File result = FileUtilities.showFolderOpenDialog(current, (Window) this.parent);
			if (result != null)
				this.text_folder.setText(result.getAbsolutePath());
			this.requestFocus();
		} else if (e.getSource() == this.btn_config_file_select) {
			File current = new File(this.text_config_file.getText());
			current = current.exists() ? current : new File(BatchCountConfigWindow.configFileDefault);
			File result = FileUtilities.showFileOpenDialog(current, "batch", (Window) this.parent);
			if (result != null)
				this.text_config_file.setText(result.getAbsolutePath());
			this.requestFocus();
		} else if (e.getSource() == this.btn_output_file_select) {
			File current = new File(this.text_output_file.getText());
			current = current.exists() ? current : new File(BatchCountConfigWindow.outputFileDefault);
			File result = FileUtilities.showFileSaveDialog(current, "counts", "csv", (Window) this.parent);
			if (result != null)
				this.text_output_file.setText(result.getAbsolutePath());
			this.requestFocus();
		} 
	}

}
