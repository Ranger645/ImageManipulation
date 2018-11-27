package algos;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Blob {

	List<Point> points = null;
	List<Point> edge_points = null;

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
		Point average = new Point(0, 0);
		for (Point p : this.points) {
			average.x += p.x;
			average.y += p.y;
		}
		average.x /= points.size();
		average.y /= points.size();
		return average;
	}

	public String toString() {
		Point average = this.compute_average_point();
		return String.format("[Blob={Size=%d;Average=(%f,%f)}]", this.points.size(), average.x, average.y);
	}

	public List<Blob> split(int pieces, int thresh, int size) {
		// This works by returning subset blobs of this blob that are derived from the
		// threshold and minimum pixel counts.
		List<Blob> blob_list = new ArrayList<Blob>();
		
		
		
		return blob_list;
	}

	public static double point_triangle_area(Point a, Point b, Point c) {
		return (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y)) / 2.0;
	}

}
