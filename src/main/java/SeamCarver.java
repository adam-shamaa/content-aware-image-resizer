import java.awt.Color;
import PrincetonResources.Picture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Adam Shamaa
 */

public class SeamCarver implements Runnable {
  private final static double ONE_THOUSAND = 1000;
  private ArrayList < Double > [] energy;
  private int[] edgeTo;
  private double[][] distTo;
  private Picture picture;
  private Queue < Integer > queue;
  private boolean[] onQueue;
  private Picture[] pictures;
  private javax.swing.JSlider widthHeightJSlider;
  private boolean stopThread;

  public SeamCarver(Picture picture) {
    if (picture == null) throw new IllegalArgumentException();
    initializeNewPicture(picture, null, null);
  }

  //One-time initialization
  private void newPicture() {
    int width = width(),
    height = height();

    //cache to retrieve the pictures whos dimensions which have already been computed 
    pictures = new Picture[width];

    //energy of each pixel according to the dual-gradient function. Note* One time ArrayList initialization for easy removal of pixels. 
    energy = (ArrayList < Double > []) new ArrayList[height];
    for (int h = 0; h < height; h++) {
      energy[h] = new ArrayList < Double > ();
    }

    //initialize the energy of each pixel 
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (y == 0 || y == height - 1) { //corner case (top row & bottom row) - set to maximum energy to encourage the removal of inner pixels only
          energy[y].add(x, ONE_THOUSAND);
        } else if (x == 0 || x == width - 1) { //corner case (left-most column & right-most column) - set to maximum energy to encourage the removal of inner pixels only
          energy[y].add(x, ONE_THOUSAND);
        } else addEnergy(x, y); //calculate & add each pixel's energy according to the dual-gradient function - see called method for more detail on calculation specifics 
      }
    }
  }

  //Copy of the current picture
  public Picture picture() {
    return new Picture(picture);
  }

  //width of current picture (px)
  public int width() {
    return picture.width();
  }

  //height of current picture (px)
  public int height() {
    return picture.height();
  };

  //energy of the pixel at (column, row)
  public double energy(int column, int row) {
    if (column < 0 || column >= width() || row < 0 || row >= height()) throw new IllegalArgumentException();
    return energy[row].get(column);
  }

  //Helper Functions
  //General Initialization
  private void initializeNewPicture(Picture picture, String orientation, int[] seam) {
    this.picture = picture;

    //width and height of the picture in pixels 
    int width = width(),
    height = height();
    int pixels = width * height;

    //head of the edge to this pixel
    edgeTo = new int[pixels];

    //current minimum-weight path to this pixel
    distTo = new double[width][height];

    //queue for Bellman Ford's min-path implementation
    queue = new LinkedList < Integer > ();
    onQueue = new boolean[pixels];

    //Initialization for new pictures / seam-removals
    if (orientation == null) { //calculate energy of all inner pixels
      newPicture();
    } else if (orientation == "horizontal") { //only recalculate the energy of the pixels affected by a horizontal seam cut
      initializeHorizontalPixels(seam);
    } else if (orientation == "vertical") { //only recalculate the energy of the pixels affected by a vertical seam cut
      initializeVerticalPixels(seam);
    }
  }

  private int index(int column, int row) {
    //single-array index representation of the double array representation at (col,row)
    return (column + (row * width()));
  }

  //calculate the energy according to the dual gradient function of a given pixel at (col, row)
  private double calculateEnergy(int column, int row) {
    //retrieve colors of adjacent pixels
    Color left = picture.get(column - 1, row),
    right = picture.get(column + 1, row),
    up = picture.get(column, row - 1),
    down = picture.get(column, row + 1);

    //x-gradient function - differences squared of each RGB value among the right pixel to left pixel respectively 
    double xGradient = Math.pow(right.getBlue() - left.getBlue(), 2) + Math.pow(right.getRed() - left.getRed(), 2) + Math.pow(right.getGreen() - left.getGreen(), 2);

    //y-gradient function - differences squared of each RGB value among the pixel below to pixel above respectively 
    double yGradient = Math.pow(down.getBlue() - up.getBlue(), 2) + Math.pow(down.getRed() - up.getRed(), 2) + Math.pow(down.getGreen() - up.getGreen(), 2);

    return xGradient + yGradient;
  }

  //add energy of pixel (x,y) to list
  private void addEnergy(int x, int y) {
    energy[y].add(calculateEnergy(x, y));
  }

  //replace energy of pixel (x,y) on list
  private void replaceEnergy(int x, int y) {
    energy[y].set(x, calculateEnergy(x, y));
  }

  //reset weight of every path to each column *Note. additional step must be taken from the location of the source of this call - additionally set the weight of each source value to 0.
  private void resetDistTo() {
    int width = width(),
    height = height();
    distTo = new double[width][height];
    for (int column = 0; column < width; column++) {
      Arrays.fill(distTo[column], Double.POSITIVE_INFINITY);
    }
  }

  /* Helper functions associated with vertical seams */
  //find a path of min-weight from the top row to bottom row. Returns x-values (where each indice represents the row). I.e. seam[row] = col 
  public int[] findVerticalSeam() {
    int width = width(),
    height = height();
    resetDistTo();

    //Bellman Ford's implementation to compute min-energy path to bottom row from top row
    for (int w = 0; w < width; w++) { //insert sources (top row)
      distTo[w][0] = 0.0;
      queue.add(index(w, 0));
    }
    while (!queue.isEmpty()) {
      int index = queue.remove();
      relaxVertical(index % width, index / width);
    }

    //Retrieve lowest-weight path to bottom row
    double minDistance = Double.POSITIVE_INFINITY; //minimum total energy-path
    int minDistanceIndex = 0; //index of bottom-row pixel belonging to min-energy path
    for (int i = 0; i < width; i++) {
      if (minDistance > distTo[i][height - 1]) {
        minDistance = distTo[i][height - 1];
        minDistanceIndex = index(i, height - 1);
      }
    }

    //copy min-energy path to array
    int[] horizontalVals = new int[height];
    int arrayIndex = height;
    for (int i = minDistanceIndex; arrayIndex > 0; i = edgeTo[i]) {
      horizontalVals[--arrayIndex] = i % width;
    }
    return horizontalVals;
  }

  private void relaxVertical(int column, int y) {
    if (y >= height() - 1) return; //pixel at the end of the graph (bottom-row)
    //Corner cases
    int lowerBound = -1,
    upperBound = 1;
    if (column == 0) lowerBound = 0; //pixel at left-most column
    if (column == width() - 1) upperBound = 0; //pixel at right-most column
    int sourceIndex = index(column, y);

    //check up to three of the adjacent pixels below source
    for (int i = lowerBound; i <= upperBound; i++) {
      int neighborIndex = index(i + column, y + 1);
      if (distTo[i + column][y + 1] > energy[y + 1].get(i + column) + distTo[column][y]) {
        distTo[i + column][y + 1] = energy[y + 1].get(i + column) + distTo[column][y];
        edgeTo[neighborIndex] = sourceIndex;
        if (!onQueue[neighborIndex]) {
          onQueue[neighborIndex] = true;
          queue.add(neighborIndex);
        }
      }
    }
  }

  //Remove the given vertical seam and initialize new picture with width reduced by 1
  public void removeVerticalSeam(int[] seam) {
    if (seam == null || seam.length < height() || !(height() >= 1)) throw new IllegalArgumentException();
    for (int horizontalIndice = 0; horizontalIndice < seam.length; horizontalIndice++) {
      if (seam[horizontalIndice] < 0 || seam[horizontalIndice] >= width()) throw new IllegalArgumentException();
      else if (horizontalIndice < seam.length - 1) {
        if (Math.abs(seam[horizontalIndice] - seam[horizontalIndice + 1]) > 1) throw new IllegalArgumentException();
      }
    }

    //new picture to copy eligible pixels
    Picture newPicture = new Picture(width() - 1, height());
    int height = height(),
    width = width() - 1;

    //copy all eligible pixels to new picture
    for (int h = 0; h < height; h++) {
      //offset variable to skip an ineligible pixel
      int offset = 0;

      for (int column = 0; column < width; column++) {

        //check if the pixel is eligible, otherwise skip
        if (seam[h] == column) {
          offset++;
        }

        newPicture.setRGB(column, h, picture.getRGB(column + offset, h));
      }
    }
    //reinitialize variables & only recalculate energy of pixels adjacent to the removed pixels
    initializeNewPicture(newPicture, "vertical", seam);
  }

  //re-initialize the energy of the pixels whose energy may have changed due to a horizontal seam cut
  private void initializeVerticalPixels(int[] seam) {
    int height = height() - 1;
    int width = width() - 1;

    //remove deleted pixel energies from list
    for (int column = height; column >= 0; column--) {
      energy[column].remove(seam[column]);
    }

    //recalculate energy of adjacent pixels affected by pixel deletion
    for (int column = 1; column < height - 1; column++) {
      if (seam[column] < width && seam[column] > 0) {
        replaceEnergy(seam[column], column);
        if (column < height - 1) replaceEnergy(seam[column], column + 1);
        if (column > 1) replaceEnergy(seam[column], column - 1);
        if (seam[column] > 1) replaceEnergy(seam[column] - 1, column);
        if (seam[column] < width - 1) replaceEnergy(seam[column] + 1, column);
      }
    }
  }

  //Helper functions associated with horizontal Seams
  public int[] findHorizontalSeam() { //find a path of min-weight from the left-most column to the right-most column. Returns y-values (where each indice represents the column). I.e. seam[col] = row 
    int width = width(),
    height = height();
    resetDistTo();

    //Bellman Ford's implementation to compute min-energy path to bottom row from top row
    for (int h = 0; h < height; h++) { //insert sources (top row)
      distTo[0][h] = 0.0;
      queue.add(index(0, h));
    }

    while (!queue.isEmpty()) {
      int index = queue.remove();
      relaxHorizontal(index % width, index / width);
    }

    //Retrieve lowest-weight path to right-most column
    double minDistance = Double.POSITIVE_INFINITY; //minimum total energy-path
    int minDistanceIndex = 0; //index of bottom-row pixel belonging to min-energy path
    for (int i = 0; i < height; i++) {
      if (minDistance > distTo[width - 1][i]) {
        minDistance = distTo[width - 1][i];
        minDistanceIndex = index(width - 1, i);
      }
    }

    //copy min-energy path to array
    int[] verticalVals = new int[width];
    int arrayIndex = width;
    for (int i = minDistanceIndex; arrayIndex > 0; i = edgeTo[i]) {
      verticalVals[--arrayIndex] = i / width;
    }

    return verticalVals;
  }

  private void relaxHorizontal(int x, int y) {
    if (x >= width() - 1) return; //pixel at end of graph (right-most column)
    //Corner cases
    int lowerBound = -1,
    upperBound = 1;

    if (y == 0) lowerBound = 0; //pixel at top-row
    if (y == height() - 1) upperBound = 0; //pixel at bottom-row
    int sourceIndex = index(x, y);

    //check up to three of the adjacent pixels to the right of the source (directly to right, directly to right one pixel up and/or directly to the right one pixel down)
    for (int i = lowerBound; i <= upperBound; i++) {
      int index = index(x + 1, i + y);
      if (distTo[x + 1][y + i] > energy[y + i].get(x + 1) + distTo[x][y]) {
        distTo[x + 1][y + i] = energy[y + i].get(x + 1) + distTo[x][y];
        edgeTo[index] = sourceIndex;
        if (!onQueue[index]) {
          onQueue[index] = true;
          queue.add(index);
        }
      }
    }
  }

  //Remove the given horizontal seam and initialize new picture with height reduced by 1
  public void removeHorizontalSeam(int[] seam) {
    if (seam == null || seam.length < width() || !(width() >= 1)) throw new IllegalArgumentException();
    for (int verticalIndice = 0; verticalIndice < seam.length; verticalIndice++) {
      if (seam[verticalIndice] < 0 || seam[verticalIndice] >= height()) throw new IllegalArgumentException();
      else if (verticalIndice < seam.length - 1) {
        if (Math.abs(seam[verticalIndice] - seam[verticalIndice + 1]) > 1) throw new IllegalArgumentException();
      }
    }

    //new picture to copy eligible pixels
    Picture newPicture = new Picture(width(), height() - 1);
    //copy all eligible pixels to new picture	
    int height = height() - 1,
    width = width();
    for (int x = 0; x < width; x++) {
      int offset = 0;
      for (int h = 0; h < height; h++) {
        //offset variable to skip ineligible pixels
        if (seam[x] == h) {
          offset++;
        }
        newPicture.setRGB(x, h, picture.getRGB(x, h + offset));
      }
    }
    //reinitialize variables & only recalculate energy of pixels adjacent to the removed pixels
    initializeNewPicture(newPicture, "horizontal", seam);
  }

  //re-initialize the energy of the pixels whose energy must have changed due to a vertical seam cut
  private void initializeHorizontalPixels(int[] seam) {
    int height = height() - 1;
    int width = width() - 1;

    //remove all deleted pixel energies from list
    for (int column = 0; column < seam.length; column++) {
      //Shift implementation instead of .delete() due to nature of the array. 
      //The array will not shift correctly by using .delete due to the nature of the orientation of the array.
      shiftStackUp(column, seam[column]);
    }

    //recalculate energy of adjacent pixels affected by pixel deletion
    for (int column = 1; column < seam.length - 1; column++) {
      if (seam[column] > 1 && seam[column] < height - 1) {
        replaceEnergy(column, seam[column]);
        if (column > 1) replaceEnergy(column - 1, seam[column]);
        if (column < width - 1) replaceEnergy(column + 1, seam[column]);
        if (seam[column] < height - 1) replaceEnergy(column, seam[column] + 1);
        if (seam[column] > 1) replaceEnergy(column, seam[column] - 1);
      }
    }
  }

  //'deletion' method to shift all pixels below a delete pixel upwards
  public void shiftStackUp(int x, int y) {
    for (int i = y; i < height(); i++) {
      energy[i].set(x, energy[i + 1].get(x));
    }
  }

  //GUI helper functions
  public void run() {
    int width = width() - 1;
    for (int i = 0; i < width; i++) {
      if (stopThread) return;
      removeVerticalSeam(findVerticalSeam());
      pictures[i] = picture();
      widthHeightJSlider.setMaximum(i);
    }
  }

  public void insertJSlider(javax.swing.JSlider JSlider) {
    this.widthHeightJSlider = JSlider;
  }

  public Picture getPicture(int pixelsRemoved) {
    return pictures[pixelsRemoved];
  }

  public void stopThread() {
    stopThread = true;
  }
}