import java.awt.Color;
import edu.princeton.cs.algs4.Picture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 * @author Adam Shamaa
 *
 */
public class SeamCarver {
	private final static double ONE_THOUSAND = 1000;
	private ArrayList<Double>[] energy;
	private int[] edgeTo;
	private double[][] distTo;
	private Picture picture;
	private Queue<Integer> queue;
	private boolean[] onQ;
	private Picture[] pictures;
	private javax.swing.JSlider widthHeightJSlider;

	public SeamCarver(Picture picture) {	//Initialization
		if (picture == null) throw new IllegalArgumentException();
		initializeNewPicture(picture, null, null);
	}
	private void newPicture() {									//One-time initialization
		int width = width(), height = height(); int pixels = width*height;
		pictures = new Picture[width];							//cache to retrieve already calculated dimensions
		energy = (ArrayList<Double>[]) new ArrayList[height];	//energy of each pixel according to the dual-gradient function. Note* One time ArrayList initialization for easy removal of pixels. 
		for (int h = 0; h < height; h++) {	
			energy[h] = new ArrayList<Double>();
		}
		for (int y = 0; y < height ; y++) {				//initialize the energy of each pixel 
			for (int x = 0; x < width ; x++) {	
				if (y == 0 || y == height-1){			//corner case (top row & bottom row) - set to maximum energy to encourage the removal of inner pixels only
					energy[y].add(x, ONE_THOUSAND);
				}else if (x == 0 || x == width-1) {		//corner case (left-most column & right-most column) - set to maximum energy to encourage the removal of inner pixels only
					energy[y].add(x, ONE_THOUSAND);
				}else addEnergy(x,y);					//calculate & add each pixel's energy according to the dual-gradient function - see called method for more detail on calculation specifics 
			}
		}
	}
	public Picture picture() { return new Picture(picture); } //return copy current picture
	public int width() { return picture.width(); }			//width of current picture (px)
	public int height() { return picture.height(); };		//height of current picture (px)
	public double energy(int x, int y) { 					//energy of pixel(x,y)
		if (x < 0 || x >= width() || y < 0 || y >= height()) throw new IllegalArgumentException();
		return energy[y].get(x); 
	}

	//Helper Functions
	//General Initialization
	private void initializeNewPicture(Picture picture, String orientation, int[] seam) {
		this.picture = picture;						//current Picture
		int width = width(), height = height();		//width and height of the picture(px) 
		int pixels = width*height;	
		edgeTo = new int[pixels];					//head of edge to this pixel
		distTo = new double[width][height];			//current minimum-weight path to this pixel
		queue = new LinkedList<Integer>();			//queue to implement Bellman Ford's algorithm
		onQ = new boolean[pixels];

		//Initialization for new pictures / seam-removals
		if (orientation == null) {					//calculate energy of all inner pixels
			newPicture();
		}else if (orientation == "horizontal") {	//only recalculate energy of the pixels affected by a horizontal seam cut
			initializeHorizontalPixels(seam);
		}else if (orientation == "vertical") {		//only recalculate energy of the pixels affected by a vertical seam cut
			initializeVerticalPixels(seam);
		}
	}
	private int index(int x, int y) { return (x+(y*width())); }		//single-array index representation of double array index(col,row)

	private double calculateEnergy(int x, int y) { 	//calculate the energy according to the dual gradient function of a given pixel (col, row)
		Color left = picture.get(x-1, y), right = picture.get(x+1, y), up = picture.get(x, y-1), down = picture.get(x, y+1);	//retrieve colors of adjacent pixels
		double xGradient =	Math.pow(right.getBlue() - left.getBlue(), 2) + Math.pow(right.getRed() - left.getRed(), 2) + Math.pow(right.getGreen() - left.getGreen(), 2);	//x-gradient function - differences squared of each RGB value among the right pixel to left pixel respectively 
		double yGradient =	Math.pow(down.getBlue() - up.getBlue(), 2) + Math.pow(down.getRed() - up.getRed(), 2) + Math.pow(down.getGreen() - up.getGreen(), 2); //y-gradient function - differences squared of each RGB value among the pixel below to pixel above respectively 
		return Math.sqrt(xGradient+yGradient);
	}

	private void addEnergy(int x, int y) { 			//add energy of pixel (x,y) to list
		energy[y].add(calculateEnergy(x,y)); 
	}	

	private void replaceEnergy(int x, int y) {		//replace energy of pixel (x,y) on list
		energy[y].set(x, calculateEnergy(x,y)); 
	}

	private void resetDistTo() {	//reset weight of every path to each column *Note. additional step must be taken from the location of the source of this call - additionally set the weight of each source value to 0.
		int width = width(), height = height();
		distTo = new double[width][height];
		for (int x = 0; x < width; x++) {
			Arrays.fill(distTo[x], Double.POSITIVE_INFINITY);
		}
	}

	//Helper functions associated with vertical seams
	public int[] findVerticalSeam() {	//find a path of min-weight from the top row to bottom row. Returns x-values (where each indice represents the row). I.e. seam[row] = col 
		int width = width(), height = height();
		resetDistTo();							

		//Bellman Ford's implementation to compute min-energy path to bottom row from top row
		for (int w = 0; w < width; w++) {	//insert sources (top row)
			distTo[w][0] = 0.0;
			queue.add(index(w,0));
		}
		while (!queue.isEmpty()) {
			int index = queue.remove();
			relaxVertical(index%width, index/width);
		} 

		//Retrieve lowest-weight path to bottom row
		double minDistance = Double.POSITIVE_INFINITY;	//minimum total energy-path
		int minDistanceIndex = 0;						//index of bottom-row pixel belonging to min-energy path
		for (int i = 0; i < width; i++) {
			if (minDistance > distTo[i][height-1]) {
				minDistance = distTo[i][height-1];
				minDistanceIndex = index(i,height-1);
			}
		}

		//copy min-energy path to array
		int[] horizontalVals = new int[height];
		int arrayIndex = height;
		for (int i = minDistanceIndex; arrayIndex > 0; i = edgeTo[i]) {
			horizontalVals[--arrayIndex] = i%width;
		}

		return horizontalVals;
	}

	private void relaxVertical(int x, int y) {	
		if (y >= height()-1) return;	//pixel at the end of the graph (bottom-row)

		//Corner cases
		int lowerBound = -1, upperBound = 1;
		if (x == 0) lowerBound = 0;			//pixel at left-most column
		if (x == width()-1) upperBound = 0;	//pixel at right-most column

		int sourceIndex = index(x, y);						
		for (int i = lowerBound; i <= upperBound; i++) {	//check up to three of the adjacent pixels below source
			int neighborIndex = index(i+x, y+1);
			if (distTo[i+x][y+1] > energy[y+1].get(i+x) + distTo[x][y]) {
				distTo[i+x][y+1] = energy[y+1].get(i+x) + distTo[x][y];
				edgeTo[neighborIndex] = sourceIndex;
				if (!onQ[neighborIndex]) {
					onQ[neighborIndex] = true;
					queue.add(neighborIndex);
				}
			}
		}
	}

	public void removeVerticalSeam(int[] seam) {	//Remove the given vertical seam and initialize new picture with width reduced by 1
		if (seam == null || seam.length < height() || !(height() >= 1)) throw new IllegalArgumentException();
		for (int horizontalIndice = 0; horizontalIndice < seam.length; horizontalIndice++) {
			if (seam[horizontalIndice] < 0 || seam[horizontalIndice] >= width()) throw new IllegalArgumentException();
			else if (horizontalIndice < seam.length-1) {
				if (Math.abs(seam[horizontalIndice] - seam[horizontalIndice+1]) > 1) throw new IllegalArgumentException();
			}
		}

		Picture newPicture = new Picture(width()-1, height());	//new picture to copy eligible pixels
		int height = height(), width = width()-1;
		for (int h = 0; h < height; h++) {						//copy all eligible pixels to new picture
			int offset = 0;										//offset variable to skip an ineligible pixel
			for (int x = 0; x < width; x++) {
				if (seam[h] == x) {								//check if the pixel is eligible, otherwise skip
					offset++;
				}
				newPicture.setRGB(x, h, picture.getRGB(x+offset, h));		
			}
		}
		initializeNewPicture(newPicture, "vertical", seam);		//reinitialize variables & only recalculate energy of pixels adjacent to the removed pixels
	}


	private void initializeVerticalPixels(int[] seam) { //re-initialize the energy of the pixels whose energy may have changed due to a horizontal seam cut
		int height = height() - 1; int width = width() - 1;

		for (int x = height; x >= 0; x--) {		//remove deleted pixel energies from list
			energy[x].remove(seam[x]);
		}

		for (int x = 1; x < height-1; x++) {	//recalculate energy of adjacent pixels affected by pixel deletion
			if (seam[x] < width && seam[x] > 0) {
				replaceEnergy(seam[x],x);
				if (x < height-1) replaceEnergy(seam[x], x+1);
				if (x > 1) replaceEnergy(seam[x], x-1);
				if (seam[x] > 1) replaceEnergy(seam[x]-1, x); 
				if (seam[x] < width-1) replaceEnergy(seam[x]+1, x);
			}
		}
	}

	//Helper functions associated with horizontal Seams
	public int[] findHorizontalSeam() { //find a path of min-weight from the left-most column to the right-most column. Returns y-values (where each indice represents the column). I.e. seam[col] = row 
		int width = width(), height = height();
		resetDistTo();

		//Bellman Ford's implementation to compute min-energy path to bottom row from top row
		for (int h = 0; h < height; h++) {	//insert sources (top row)
			distTo[0][h] = 0.0;
			queue.add(index(0,h));
		}

		while (!queue.isEmpty()) {
			int index = queue.remove();
			relaxHorizontal(index%width, index/width);
		}

		//Retrieve lowest-weight path to right-most column
		double minDistance = Double.POSITIVE_INFINITY; 	//minimum total energy-path
		int minDistanceIndex = 0;						//index of bottom-row pixel belonging to min-energy path
		for (int i = 0; i < height; i++) {
			if (minDistance > distTo[width-1][i]) {
				minDistance = distTo[width-1][i];
				minDistanceIndex = index(width-1,i);
			}
		}

		//copy min-energy path to array
		int[] verticalVals = new int[width];
		int arrayIndex = width;
		for (int i = minDistanceIndex; arrayIndex > 0; i = edgeTo[i]) {
			verticalVals[--arrayIndex] = i/width;
		}

		return verticalVals;
	}

	private void relaxHorizontal(int x, int y) {
		if (x >= width()-1) return;			//pixel at end of graph (right-most column)

		//Corner cases
		int lowerBound = -1, upperBound = 1;
		if (y == 0) lowerBound = 0;				//pixel at top-row
		if (y == height()-1) upperBound = 0;	//pixel at bottom-row

		int sourceIndex = index(x,y);
		for (int i = lowerBound; i <= upperBound; i++) {	//check up to three of the adjacent pixels to the right source (directly to right, directly to right one pixel up and/or directly to the right one pixel down)
			int index = index(x+1, i+y);
			if (distTo[x+1][y+i] > energy[y+i].get(x+1) + distTo[x][y]) {
				distTo[x+1][y+i] = energy[y+i].get(x+1) + distTo[x][y];
				edgeTo[index] = sourceIndex;
				if (!onQ[index]) {
					onQ[index] = true;
					queue.add(index);
				}
			}
		}
	}

	public void removeHorizontalSeam(int[] seam) {				//Remove the given horizontal seam and initialize new picture with height reduced by 1
		if (seam == null || seam.length < width() || !(width() >= 1)) throw new IllegalArgumentException();
		for (int verticalIndice = 0; verticalIndice < seam.length; verticalIndice++) {
			if (seam[verticalIndice] < 0 || seam[verticalIndice] >= height()) throw new IllegalArgumentException();
			else if (verticalIndice < seam.length-1) {
				if (Math.abs(seam[verticalIndice] - seam[verticalIndice+1]) > 1) throw new IllegalArgumentException();
			}
		}

		Picture newPicture = new Picture(width(), height()-1);	//new picture to copy eligible pixels
		int height = height() - 1, width = width();				//copy all eligible pixels to new picture	
		for (int x = 0; x < width; x++) {						
			int offset = 0;
			for (int h = 0; h < height; h++) {
				if (seam[x] == h) {								//offset variable to skip ineligible pixels
					offset++;
				}
				newPicture.setRGB(x, h, picture.getRGB(x, h+offset));		
			}
		}
		initializeNewPicture(newPicture, "horizontal", seam);	//reinitialize variables & only recalculate energy of pixels adjacent to the removed pixels
	}

	private void initializeHorizontalPixels(int[] seam) { 	//re-initialize the energy of the pixels whose energy must have changed due to a vertical seam cut
		int height = height() - 1; int width = width() - 1;

		for (int w = 0; w < seam.length; w++) {				//remove all deleted pixel energies from list
			shiftStackUp(w,  seam[w]);						//shift implementation instead of .delete() due to nature of the array. The array will not shift correctlyby using .delete due to the nature of the orientation of the array.
		}

		for (int w = 1; w < seam.length-1; w++) {			//recalculate energy of adjacent pixels affected by pixel deletion
			if (seam[w] > 1 && seam[w] < height-1) {
				replaceEnergy(w, seam[w]);
				if (w > 1) replaceEnergy(w-1, seam[w]);
				if (w < width-1) replaceEnergy(w+1, seam[w]);
				if (seam[w] < height-1) replaceEnergy(w, seam[w]+1);
				if (seam[w] > 1) replaceEnergy(w, seam[w]-1);
			} 
		}
	}
	
	public void shiftStackUp(int x, int y) {			//'deletion' method to shift all pixels below a delete pixel upwards
		for (int i = y; i < height(); i++) {
			energy[i].set(x, energy[i+1].get(x));
		}
	}

	public static void main(String[] args) {
		Picture picture = new Picture("road.png");
		SeamCarver seamC = new SeamCarver(picture);
		/*long current = System.currentTimeMillis();
		test2 seam = new test2(picture);
		System.out.println(System.currentTimeMillis() - current);

		//seam.removeHorizontalSeam(seam.findHorizontalSeam());

		//System.out.println(seamC.width + " " + seamC.height + " " + seamC.energy.size());
		//System.out.println(seam.picture().width() + " " + seam.picture().height() + " " + seam.energy.length);

		//int[] ar = seamC.findVerticalSeam();
		//seamC.removeVerticalSeam(ar);
		//10324, 9963
		//seamC.findVerticalSeam();
		//System.out.println(System.currentTimeMillis() - current);
		/*current = System.currentTimeMillis();
		 * 
		 */

		/*for (int i : seamC.findHorizontalSeam()) {
			//System.out.println(i);
		}*/
		for (int run = 0; run < 200; run++) {
			seamC.removeVerticalSeam(seamC.findVerticalSeam());
			//seamC.removeHorizontalSeam(seamC.findHorizontalSeam());
		}
		seamC.picture.save("test.png");
		//System.out.println(System.currentTimeMillis() - current);
		//seamC.picture().save("saved2.png");
		//seam.picture().save("saved3.png");
		//seam.picture().save("saved2.png");
		//System.out.println(seamC.width + " " + seamC.height + " " + seamC.energy.size());
		//System.out.println(seam.picture().width() + " " + seam.picture().height() + " " + seam.energy.length);

		//s
		/*for (int h = 0 ; h < seam.height(); h++) {
			for (int w = 0; w < seam.width(); w++) {
				if (seam.energy(w, h) != seamC.energy(w, h)) {
					System.out.println(w + " " + h + " " + seam.energy(w, h) + " " + seamC.energy(w, h));
				} 
				//System.out.println(w + " :)" + h + " " + seam.energy(w, h) + " " + seamC.energy(w, h));

			}
			System.out.println(" ");
		}*/
		/*int[] seamA = seam.findHorizontalSeam();
		double sumA = 0;
		double sumB = 0;
		int[] seamB = seamC.findHorizontalSeam();
		for (int i = 0; i < seamA.length; i++) {
			sumA += seam.energy(i, seamA[i]);
			sumB += seamC.energy(i, seamB[i]);
			if (seamA[i] != seamB[i]) {
				System.out.println(seamA[i] + " " + seamB[i] + " " + i);
			}
		}
		System.out.println(sumA + " " + sumB);
		seam.picture().save("a.png");
		seamC.picture().save("b.png");
		/*for (int w = 0; w < seam.width(); w++) {
			for (int h = 0 ; h < seam.height(); h++) {
				if (seam.energy(w, h) == seamC.energy(w, h)) {
					System.out.println(w + " " + h + " " + seam.energy(w, h) + " " + seamC.energy(w, h));
				}	
			}
			System.out.println(" ");
		}*/
		/*
		//test2 test2 = new test2(picture);
		/*long current = System.currentTimeMillis();

		for (int run = 0; run < 100; run++) {
			seam.removeHorizontalSeam(seam.findHorizontalSeam());
		}
		System.out.println(System.currentTimeMillis() - current);
		seam.picture().save("test1.png");(/
		/*for (int h = 0; h < picture.height(); h++) {
			for (int w = 0; w < picture.width(); w++) {
				//System.out.print(seam.distTo.get(seam.index(w, h)) + " ");
			}
			//System.out.println(" ");
		}*/


		/*
		for (int i : se) {
			System.out.println(i);
		}*/
		//System.out.println(se[0]);
		//seam.removeVerticalSeam(se);

		//System.out.println(System.currentTimeMillis() - current);
		//seam.picture().save("test2.png");
		/*seamIdentical seamC = new seamIdentical(picture);
		long currentTime = System.currentTimeMillis();
		for (int run = 0; run < 15; run++) {
			seamC.removeVerticalSeam(seamC.findVerticalSeam());
			seam.removeVerticalSeam(seam.findVerticalSeam());
			//average += System.currentTimeMillis() - currentTime;
		}
		for (int h = 0 ; h < seam.height(); h++) {
			for (int w = 0; w < seam.width(); w++) {
				if (seam.energy(w, h) != seamC.energy(w, h)) {
					System.out.println(w + " " + h + " " + seam.energy(w, h) + " " + seamC.energy(w, h));
				}	
			}
		}
		System.out.println(seam.picture.equals(seamC.picture()));
		System.out.println(seam.width() + " " + seamC.width() + " " + picture.width());
		seamC.picture().save("test2.png");
		seam.picture().save("test1.png");*/
		//a.add(8, 5);
		//System.out.println(a.get(1));


		//seamC.picture().save("test3.png");
		//seemIdentical seam = new seemIdentical(picture);

		/*
		long average = 0;
		for (int i = 0; i < 100; i++) {

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

		for (int h = 0 ; h < seam.height(); h++) {
			for (int w = 0; w < seam.width(); w++) {
				if (seam.energy(w, h) != seamC.energy(w, h)) {
					System.out.println(w + " " + h + " " + seam.energy(w, h) + " " + seamC.energy(w, h));
				}	
			}
		}*/
	}
}