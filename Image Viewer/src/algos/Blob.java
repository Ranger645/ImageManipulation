package algos;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Blob {

	public List<Point> points = null;
	public List<Point> edge_points = null;
	private int count = 1;
	private Point computed_center = null;

	// The variable that will be used for coloring. Smaller points will be negative
	// and bigger points will be positive.
	private int type = 0;

	public Blob() {
		points = new ArrayList<Point>();
		edge_points = new ArrayList<Point>();
	}

	public Blob(List<Point> points) {
		this.points = new ArrayList<Point>(points);
		edge_points = this.compute_edge_points();
	}

	private List<Point> compute_edge_points() {
		return new ArrayList<Point>();
	}

	public Blob(List<Point> points, List<Point> edge_points) {
		this.points = new ArrayList<Point>(points);
		this.edge_points = new ArrayList<Point>(edge_points);
	}

	public Point compute_average_point() {
		if (this.computed_center != null)
			return this.computed_center;
		Point average = new Point(0, 0);
		for (Point p : this.points) {
			average.x += p.x;
			average.y += p.y;
		}
		average.x /= points.size();
		average.y /= points.size();
		this.computed_center = average;
		return this.computed_center;
	}

	public String toString() {
		Point average = this.compute_average_point();
		return String.format("[Blob={Size=%d;Average=(%f,%f)}]", this.points.size(), average.x, average.y);
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public int getType() {
		return this.type;
	}
	
	public void set_count(int count) {
		this.count = count;
	}
	
	public int get_count() {
		return this.count;
	}

	public Color get_color() {
		return type == 0 ? Color.CYAN : (type < 0 ? Color.YELLOW : Color.RED);
	}

}
