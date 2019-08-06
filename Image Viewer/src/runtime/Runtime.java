package runtime;

import image_viewer.Window;

public class Runtime {

	public static void main(String[] args) {

		/*
		 * Building the window for this application.
		 * 
		 * This is an event driven application so there is no actual code inside of the
		 * main function. The event loop and handlers handle all of the user operation
		 * events.
		 */
		Window window = new Window();

	}

}
