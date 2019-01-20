package runtime;

import java.io.File;

import filters.F_Blue;
import image_viewer.Window;

public class Runtime {

	public static void main(String[] args) {

		Window window = new Window();
		window.open_image(new File("res/cells_image_5.png"));
		window.get_selected_viewer().add_filter(new F_Blue());
		window.repaint();

	}

}
