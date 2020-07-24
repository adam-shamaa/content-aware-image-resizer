import java.awt.Color;
import edu.princeton.cs.algs4.IndexMinPQ;
import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
	private final static double ONE_THOUSAND = 1000;	
	private double[] energy;
	private int[] edgeTo;
	private double[] distTo;
	private int[][] index;
	private Picture picture;
	private IndexMinPQ<Double> pq;

	public SeamCarver(Picture picture) {	
		if (picture == null) throw new IllegalArgumentException();
		initializeNewPicture(picture, null, null);
	}

	public Picture picture() { return new Picture(picture); } //return copy current picture
	public int width() { return picture.width(); }		//width of current picture (px)
	public int height() { return picture.height(); };	//height of current picture (px)
	public double energy(int x, int y) { 
		if (x < 0 || x >= width() || y < 0 || y >= height()) throw new IllegalArgumentException();
		return energy[index(x,y)]; 
	}

	//Helper Functions
	private void initializeNewPicture(Picture picture, String orientation, int[] seam) {
		this.picture = picture;	//current Picture
		int width = width(), height = height();	//width and height of the picture in pixels
		int pixels = width*height;	//total pixels of the picture

		pq = new IndexMinPQ<Double>(pixels);	//priority queue to implement Dijkstra's algorithim, using
		edgeTo = new int[pixels];		//location of the pixel whose edge points to this pixel
		distTo = new double [pixels];	//current minimum cost path to this pixel

		if (orientation == null) {	//calculate energy of all inner pixels
			initializeColRowConversion();
			intializeInnerPixels();
		}else if (orientation == "horizontal") {	//only recalculate energy of the pixels affected by a horizontal seam cut
			initializeHorizontalPixels(seam, energy);
			initializeColRowConversion();
		}else if (orientation == "vertical") {	//only recalculate energy of the pixels affected by a vertical seam cut
			initializeColRowConversion();
			initializeVerticalPixels(seam, energy);
		}

		initializeOuterPixels();
	}

	private int index(int x, int y) {
		return index[x][y];
	}

	private void initializeHorizontalPixels(int[] seam, double[] copyOfEnergy) { //re-initialize the energy of the pixels whose energy must have changed due to a horizontal seam cut
		int width = width(), height = height(), pixels = width*height;

		energy = new double[pixels];	//energy of each pixel
		for (int x = 1; x < width - 1; x++) {
			int offset = 0;
			for (int y = 1; y < height -1; y++) {
				if (seam[x]-1 == y) {
					calculateEnergy(x,y);
				}else if (seam[x] == y) {
					calculateEnergy(x,y);
					offset++;
				}else {
					energy[index(x,y)] = copyOfEnergy[index(x,y+offset)];
				}
			}
		}
	}

	private void initializeVerticalPixels(int[] seam, double[] copyOfEnergy) { //re-initialize the energy of the pixels whose energy must have changed due to a horizontal seam cut
		int width = width(), height = height(), pixels = width*height;
		energy = new double[pixels];	//energy of each pixel
		int offset = 1;
		for (int y = 1; y < height-1; y++) {
			for (int x = 1; x < width-1; x++) {
				if (seam[y]-1 == x) {
					calculateEnergy(x,y);
				}else if (seam[y] == x) {
					calculateEnergy(x,y);
					offset++;
				}else {
					energy[index(x,y)] = copyOfEnergy[index(x,y)+offset];
				}
			}
			if (seam[y] == width()-1) offset++;
		}
	}

	private void initializeColRowConversion() {
		int width = width(), height = height();
		index = new int[width][height];	//double array representation which returns the single index value of the pixel at (col, row). (Simplifies calculations & readability)
		int count = 0;
		for (int y = 0; y < height; y++) {	//initialize single index values for each (x,y) index (used to simplify calculations and readability). I.e. the single-index representation of a double array
			for (int x = 0; x < width; x++) {
				index[x][y] = count++;
			}
		}
	}

	private void intializeInnerPixels() {
		int width = width(), height = height();
		int pixels = width*height;
		energy = new double[pixels];	//energy of each pixel
		for (int x = 1; x < width - 1; x++) {	//initialize the energy of each pixel excluding top-row, bottom-row, left-most column & right-most column
			for (int y = 1; y < height -1; y++) {
				calculateEnergy(x,y);
			}
		}
	}

	private void initializeOuterPixels() {
		int width = width(), height = height();
		for (int x = 0; x < width; x++) {	//initialize top & bottom row pixel energies
			energy[index(x,0)] = ONE_THOUSAND;
			energy[index(x,height-1)] = ONE_THOUSAND;
		}

		for (int y = 0; y < height; y++) {	//initialize left-most column & right-most column pixel energies
			energy[index(0,y)] = ONE_THOUSAND;
			energy[index(width-1,y)] = ONE_THOUSAND;
		}
	}

	private void calculateEnergy(int x, int y) {	//'energy' according to the dual gradient function of a given pixel (col, row)
		Color left = picture.get(x-1, y), right = picture.get(x+1, y), up = picture.get(x, y-1), down = picture.get(x, y+1);	//retrieve colors of adjacent pixels
		double xGradient =	Math.pow(right.getBlue() - left.getBlue(), 2) + Math.pow(right.getRed() - left.getRed(), 2) + Math.pow(right.getGreen() - left.getGreen(), 2);	//x-gradiant function - differences squared of each RGB value among the right pixel to left pixel respectively 
		double yGradient =	Math.pow(down.getBlue() - up.getBlue(), 2) + Math.pow(down.getRed() - up.getRed(), 2) + Math.pow(down.getGreen() - up.getGreen(), 2); //y-gradiant function - differences squared of each RGB value among the pixel below to pixel above respectively 
		energy[index(x,y)] = Math.sqrt(xGradient+yGradient);	//energy of the pixel (*note can not avoid the sqrt to save some cost from the computation as the shortest-paths of the sqrt graph and non-sqrt graphs are completely unrelated.
	}

	private void resetDistTo() {	//reset weight of every path to each column *note- additional step must taken from the location of the source of this call. Must additionaly set the weight of each source value to 0.
		int pixels = width()*height();
		for (int i = 0; i < pixels; i++) {
			distTo[i] = Double.POSITIVE_INFINITY;
		}
	}

	private void relaxVertical(int x, int y) {
		if (y >= height()-1) return;
		int lowerBound = -1, upperBound = 1;
		if (x == 0) lowerBound = 0;
		if (x == width()-1) upperBound = 0;

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
		if (y == height()-1) upperBound = 0;

		int sourceIndex = index(x,y);
		for (int i = lowerBound; i <= upperBound; i++) {
			int index = index(x+1, i+y);
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
		if (seam == null || seam.length < width() || !(width() >= 1)) throw new IllegalArgumentException();
		for (int verticalIndice = 0; verticalIndice < seam.length; verticalIndice++) {
			if (seam[verticalIndice] < 0 || seam[verticalIndice] >= height()) throw new IllegalArgumentException();
			else if (verticalIndice < seam.length-1) {
				if (Math.abs(seam[verticalIndice] - seam[verticalIndice+1]) > 1) throw new IllegalArgumentException();
			}
		}
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
		initializeNewPicture(newPicture, "horizontal", seam);
	}

	public void removeVerticalSeam(int[] seam) {	//Remove the vertical seam and initialize the replacement picture
		if (seam == null || seam.length < height() || !(height() >= 1)) throw new IllegalArgumentException();
		for (int horizontalIndice = 0; horizontalIndice < seam.length; horizontalIndice++) {
			if (seam[horizontalIndice] < 0 || seam[horizontalIndice] >= width()) throw new IllegalArgumentException();
			else if (horizontalIndice < seam.length-1) {
				if (Math.abs(seam[horizontalIndice] - seam[horizontalIndice+1]) > 1) throw new IllegalArgumentException();
			}
		}
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
		initializeNewPicture(newPicture, "vertical", seam);
	}

	public static void main(String[] args) {
		Picture picture = new Picture("road.png");
		//seemIdentical seam = new seemIdentical(picture);
		
		/*
		long average = 0;
		for (int i = 0; i < 100; i++) {
			SeamCarver seam = new SeamCarver(picture);
			long currentTime = System.currentTimeMillis();
			for (int run = 0; run < 280; run++) {
				seam.removeVerticalSeam(seam.findVerticalSeam());
				average += System.currentTimeMillis() - currentTime;
			}
		}
		System.out.println("Average: " + (average/100));
		 */
		//seam.picture().save("test2.png");
		/*int[] seamAr = seam.findHorizontalSeam();
		for (int i : seamAr) {
				System.out.println(i);
		}*/
		//seam.removeHorizontalSeam(seamAr);
		/*
		seamIdentical seam = new seamIdentical(picture);
		SeamCarver seamC = new SeamCarver(picture);
		for (int run = 0; run < 100; run++) {
			int [] seamAr = seam.findHorizontalSeam();
			for (int i : seamAr) {
				System.out.print(i + " ");
			}
			System.out.println(" ");
			seam.removeHorizontalSeam(seamAr);
			seamAr = seamC.findHorizontalSeam();
			for (int i : seamAr) {
				System.out.print(i + " ");
			}
			seamC.removeHorizontalSeam(seamAr);
			System.out.println(" ");
		}
		seamC.picture().save("test2.png");
		seam.picture().save("test1.png");
		for (int h = 0 ; h < seam.height(); h++) {
			for (int w = 0; w < seam.width(); w++) {
				if (seam.energy(w, h) != seamC.energy(w, h)) {
					System.out.println(w + " " + h + " " + seam.energy(w, h) + " " + seamC.energy(w, h));
				}	
			}
		}*/

	}
}