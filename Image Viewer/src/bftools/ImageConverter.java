package bftools;

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
	 * General purpose function for converting images to a different format. Uses
	 * the bftools package as a child process to convert everything.
	 * 
	 * @param source        - the file to start with
	 * @param new_file_type - the file extension to convert to (ex, "jpg", "tiff",
	 *                      etc.)
	 * @param target_dir    - the directory that the converted file should be placed
	 *                      in.
	 * @param file_name     - the name of the new file without the extension.
	 */
	public static int convert_image(File source, String new_file_type, File target_dir, String file_name) {
		String command = "tools/bftools/bfconvert";

		// Testing if the proper executable exists:
		File executable = new File(command);
		if (!executable.exists()) {
			System.out.println("bftools executable not found.");
			return -1;
		}

		// Building the command:
		String target_file_path = target_dir + File.pathSeparator + file_name + "." + new_file_type;
		command += " " + source.getAbsolutePath() + " " + target_file_path;

		Runtime run = Runtime.getRuntime();
		try {
			System.out.println("Running Command: " + command);
			Process proc = run.exec(command);

			BufferedReader stdoutput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader stderror = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			
			System.out.print("Process Running...");
			while (proc.isAlive());
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
