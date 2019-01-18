package image_viewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class PointManager {
	
	private List<Point> points = new ArrayList<Point>();
	private List<Color> point_colors = new ArrayList<Color>();
	
	public PointManager() {
		
	}
	
	public void paint_points(Graphics g, int dx, int dy, int zoom) {
		// Draws the list of points on the given graphics object with the given x and y 
		// as the origin of all the points in the points array.
		int radius = 3;
		double multiplier = zoom / 100.0;
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			g.setColor(point_colors.get(i));
			int x = dx + (int) (p.getX() * multiplier);
			int y = dy + (int) (p.getY() * multiplier);
			g.drawLine(x - radius, y, x + radius, y);
			g.drawLine(x, y - radius, x , y + radius);
		}
	}
	
	public void addPoint(Point p) {
		points.add(p);
		this.point_colors.add(Color.CYAN);
	}
	
	public void addPoint(Point p, Color c) {
		points.add(p);
		this.point_colors.add(c);
	}
	
	public void removePoint(Point p) {
		int index = points.indexOf(p);
		points.remove(index);
		point_colors.remove(index);
	}
	
	public void clear_points() {
		points.clear();
		point_colors.clear();
	}
	
	public Point getPointNear(int x, int y, int radius) {
		for (Point p : points) {
			if (p.distanceSq(x, y) < radius * radius)
				return p;
		}
		return null;
	}

}
