package image_viewer;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

public class WorkingBar extends JComponent implements Runnable {
	/**
	 * Custom working bar animation.
	 */
	public static volatile boolean running = false;
	private static Thread running_thread = new Thread();
	private static WorkingBar bar = new WorkingBar();
	int x;
	private static String draw_string = "";
	float width_ratio = 0.2f;

	public void paintComponent(Graphics g) {
		if (running) {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			g.setColor(Color.ORANGE);
			g.fillRect(x % this.getWidth(), 0, (int) (this.getWidth() * this.width_ratio), this.getHeight());
			g.fillRect(x % this.getWidth() - this.getWidth(), 0, (int) (this.getWidth() * this.width_ratio),
					this.getHeight());
			g.setColor(Color.BLACK);
			this.x += 2;
		}
		
		synchronized (draw_string) {
			g.drawString(draw_string, 0, this.getHeight() - 3);
		}
	}
	
	public static void set_text(String new_text) {
		synchronized (draw_string) {
			draw_string = new_text;
		}
		bar.repaint();
	}

	public void run() {
		while (running) {
			this.repaint();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.x = 0;
	}

	public static void start_working() {
		if (!running) {
			running_thread = new Thread(bar);
			running = true;
			running_thread.start();
		}
	}

	public static void stop_working() {
		if (running) {
			running = false;
			running_thread = null;
		}
		set_text("");
	}

	public static WorkingBar get_bar() {
		return bar;
	}

}
