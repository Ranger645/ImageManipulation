package algos;

import java.util.Comparator;

public class BlobSortBySize implements Comparator<Blob> {

	@Override
	public int compare(Blob a, Blob b) {
		return a.points.size() - b.points.size();
	}

}
