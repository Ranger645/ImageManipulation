package batch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import filters.Filter;

/**
 * The class that will store an image file that needs to be operated on in the
 * background (not in the GUI) by the batch process.
 * 
 * @author gregfoss
 */
public class BackgroundImage {

	private String folder = "";
	private String filename = "";
	private BufferedImage image = null;
	
	public BackgroundImage(File file) {
		try {
			this.image = ImageIO.read(file);
			this.folder = file.getParent();
			this.filename = file.getName();
		} catch (IOException e) {
			System.err.println("[ERROR] opening " + file);
			e.printStackTrace();
		}
	}
	
	public void filter(Filter f) {
		this.image = f.filter(this.image);
	}

	public String getFolder() {
		return folder;
	}

	public String getFilename() {
		return filename;
	}

	public BufferedImage getImage() {
		return image;
	}
	
}
