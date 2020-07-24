import java.awt.Color;
import java.util.Stack;

import edu.princeton.cs.algs4.IndexMinPQ;
import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
	private final static double ONE_THOUSAND = 10000;
	private double[] energy;
	private int[] edgeTo;
	private double[] distTo;
	private int[][] index;
	Picture picture;
	IndexMinPQ<Double> pq;

	public SeamCarver(Picture picture) {
		initializeNewPicture(picture);
	}

	public Picture picture() { return picture; }
	public int width() { return picture.width(); }
	public int height() { return picture.height(); };
	public double energy(int x, int y) { return Math.sqrt(energy[index(x,y)]); }

	private void initializeNewPicture(Picture picture) {
		this.picture = picture;
		int width = width(), height = height();
		int pixels = width*height;
		pq = new IndexMinPQ<Double>(pixels);

		energy = new double[pixels];
		edgeTo = new int[pixels];
		distTo = new double [pixels];
		index = new int[width][height];

		int count = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				index[x][y] = count++;
			}
		}

		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height -1; y++) {
				calculateEnergy(x,y);
			}
		}

		for (int x = 0; x < width; x++) {
			energy[index(x,0)] = ONE_THOUSAND;
			energy[index(x,height-1)] = ONE_THOUSAND;
		}

		for (int y = 0; y < height; y++) {
			energy[index(0,y)] = ONE_THOUSAND;
			energy[index(width-1,y)] = ONE_THOUSAND;
		}
	}

	private int index(int x, int y) {
		return index[x][y];
	}

	private void calculateEnergy(int x, int y) {
		Color left = picture.get(x-1, y), right = picture.get(x+1, y), up = picture.get(x, y-1), down = picture.get(x, y+1);
		double xGradient =	Math.pow(right.getBlue() - left.getBlue(), 2) + Math.pow(right.getRed() - left.getRed(), 2) + Math.pow(right.getGreen() - left.getGreen(), 2);
		double yGradient =	Math.pow(down.getBlue() - up.getBlue(), 2) + Math.pow(down.getRed() - up.getRed(), 2) + Math.pow(down.getGreen() - up.getGreen(), 2);
		energy[index(x,y)] = Math.sqrt(xGradient+yGradient);
	}

	private void resetDistTo() {
		int pixels = width()*height();
		for (int i = 0; i < pixels; i++) {
			distTo[i] = Double.POSITIVE_INFINITY;
		}
	}

	private void relaxVertical(int x, int y) {
		if (y >= height()-1) return;
		int lowerBound = -1, upperBound = 1;
		if (x == 0) lowerBound = 0;
		else if (x == width()-1) upperBound = 0;

		int sourceIndex = index(x, y);
		for (int i = lowerBound; i <= upperBound; i++) {
			int index = index(i+x, y+1);
			if (distTo[index] > energy[index] + distTo[sourceIndex]) {
				distTo[index] = energy[index] + distTo[sourceIndex];
				edgeTo[index] = sourceIndex;
				if (pq.contains(index)) pq.changeKey(index, distTo[index]);
				else pq.insert(index, distTo[index]);
			}
		}
	}

	private void relaxHorizontal(int x, int y) {
		if (x >= width()-1) return;
		int lowerBound = -1, upperBound = 1;
		if (y == 0) lowerBound = 0;
		else if (y == height()-1) upperBound = 0;

		int sourceIndex = index(x,y);
		for (int i = lowerBound; i <= upperBound; i++) {
			int index = index(x+1, i+y);
			//System.out.println("source: " + x + " " + y + " Neighbor: " + index%width() + " " + index/width() + " Source:" + distTo[sourceIndex] + " Neighbor: "+ distTo[index] + " energy: " + energy[index]);
			if (distTo[index] > energy[index] + distTo[sourceIndex]) {
				distTo[index] = energy[index] + distTo[sourceIndex];
				edgeTo[index] = sourceIndex;
				if (pq.contains(index)) pq.changeKey(index, distTo[index]);
				else pq.insert(index, distTo[index]);
			}
		}
	}

	public int[] findVerticalSeam() {
		resetDistTo();
		int width = width(), height = height();
		for (int w = 0; w < width; w++) {
			distTo[index(w,0)] = 0;
			pq.insert(w, 0.0);
		}

		while (!pq.isEmpty()) {
			int index = pq.delMin();
			relaxVertical(index%width, index/width);
		}

		double minDistance = Double.POSITIVE_INFINITY;
		int minDistanceIndex = 0;
		for (int i = 0; i < width; i++) {
			if (minDistance > distTo[index(i,height-1)]) {
				minDistance = distTo[index(i,height-1)];
				minDistanceIndex = index(i,height-1);
			}
		}
		int[] horizontalVals = new int[height];
		int arrayIndex = height;
		for (int i = minDistanceIndex; arrayIndex > 0; i = edgeTo[i]) {
			horizontalVals[--arrayIndex] = i%width;
		}
		return horizontalVals;
	}

	public int[] findHorizontalSeam() {
		resetDistTo();
		int width = width(), height = height();
		for (int h = 0; h < height; h++) {
			distTo[index(0,h)] = 0;
			pq.insert(index(0,h), 0.0);
		}

		while (!pq.isEmpty()) {
			int index = pq.delMin();
			relaxHorizontal(index%width, index/width);
		}

		double minDistance = Double.POSITIVE_INFINITY;
		int minDistanceIndex = 0;
		for (int i = 0; i < height; i++) {
			if (minDistance > distTo[index(width-1,i)]) {
				minDistance = distTo[index(width-1,i)];
				minDistanceIndex = index(width-1,i);
			}
		}

		int[] verticalVals = new int[width];
		int arrayIndex = width;
		for (int i = minDistanceIndex; arrayIndex > 0; i = edgeTo[i]) {
			verticalVals[--arrayIndex] = i/width;
		}
		return verticalVals;
	}

	public void removeHorizontalSeam(int[] seam) {
		Picture newPicture = new Picture(width(), height()-1);
		int height = height() - 1, width = width();
		for (int x = 0; x < width; x++) {
			int offset = 0;
			for (int h = 0; h < height; h++) {
				if (seam[x] != h) {
					newPicture.set(x, h, picture.get(x, h + offset));
				}else {
					offset++;
					newPicture.set(x, h, picture.get(x, h + offset));
				}
			}
		}
		initializeNewPicture(newPicture);
	}

	public void removeVerticalSeam(int[] seam) {
		Picture newPicture = new Picture(width()-1, height());
		int height = height(), width = width()-1;
		for (int h = 0; h < height; h++) {
			int offset = 0;
			for (int x = 0; x < width; x++) {
				if (seam[h] != x) {
					newPicture.set(x, h, picture.get(x + offset, h));
				}else {
					offset++;
					newPicture.set(x, h, picture.get(x + offset, h));
				}
			}
		}
		initializeNewPicture(newPicture);
	}

	public static void main(String[] args) {
		Picture picture = new Picture("ocean.png");
		SeamCarver seam = new SeamCarver(picture);
		for (int i = 0; i < 180; i++) {
			seam.removeHorizontalSeam(seam.findHorizontalSeam());
		}
		/*int[] seamAr = seam.findHorizontalSeam();
		for (int i : seamAr) {
				System.out.println(i);
		}*/
		//seam.removeHorizontalSeam(seamAr);
		seam.picture.save("test.png");
	}






}