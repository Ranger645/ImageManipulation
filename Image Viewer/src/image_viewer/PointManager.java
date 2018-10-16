package image_viewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class PointManager {
	
	private List<Point> points = new ArrayList<Point>();
	
	public PointManager() {
		
	}
	
	public void paint_points(Graphics g, int dx, int dy) {
		// Draws the list of points on the given graphics object with the given x and y 
		// as the origin of all the points in the points array.
		int radius = 3;
		g.setColor(Color.CYAN);
		for (Point p : points) {
			int x = dx + (int) (p.getX());
			int y = dy + (int) (p.getY());
			g.drawLine(x - radius, y, x + radius, y);
			g.drawLine(x, y - radius, x , y + radius);
		}
	}
	
	public void addPoint(Point p) {
		points.add(p);
	}
	
	public void removePoint(Point p) {
		points.remove(p);
	}
	
	public void clear_points() {
		points.clear();
	}
	
	public Point getPointNear(int x, int y, int radius) {
		for (Point p : points) {
			if (p.distanceSq(x, y) < radius * radius)
				return p;
		}
		return null;
	}

}
