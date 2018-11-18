package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ImageConverter {
	/**
	 * This class is responsible for converting any given image file type into any
	 * other given image file type. The amount of data that is lost, the user is
	 * responsible for. There are several output options for the functions in this
	 * class. Either the images can be outputed to the res/ folder or can be
	 * outputed to a specified file.
	 */

	/**
	 * Splits the channels of a given nd2 source file into jpg files and deposits
	 * the results in the temp folder as jpg files numbered to what layer number
	 * they are. For example, 1.jpg, 2.jpg, and 3.jpg will be added to temp/ for an
	 * nd2 file with three layers.
	 * 
	 * @param source        - the file to start with
	 */
	public static int nd2_split(File source) {
		File root = new File("tools/imagej/scripts/nd2_to_jpg_split.ijm");
		
		String[] command = new String[3];
		command[0] = "tools/imagej/scripts/run_script.sh";
		
		String[] path_parts = root.getAbsolutePath().split(" ");
		String reconstructed_path = "";
		for (int i = 0; i < path_parts.length - 1; i++)
			reconstructed_path += path_parts[i] + " ";
		reconstructed_path += path_parts[path_parts.length - 1];
		command[1] = new String(reconstructed_path);
		
		path_parts = source.getAbsolutePath().split(" ");
		reconstructed_path = "";
		for (int i = 0; i < path_parts.length - 1; i++)
			reconstructed_path += path_parts[i] + " ";
		reconstructed_path += path_parts[path_parts.length - 1];
		command[2] = reconstructed_path;

		Runtime run = Runtime.getRuntime();
		try {
			System.out.println("Running Command: " + "".join(" ", command));
			Process proc = run.exec(command);

			BufferedReader stdoutput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader stderror = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

			System.out.print("Process Running...");
			while (proc.isAlive())
				;
			System.out.println("Completed.");

			String s = null;
			while ((s = stdoutput.readLine()) != null) {
				System.out.println(s);
			}
			while ((s = stderror.readLine()) != null) {
				System.out.println(s);
			}

		} catch (IOException e) {
			e.printStackTrace();
			return -2;
		}

		return 1;
	}

}
