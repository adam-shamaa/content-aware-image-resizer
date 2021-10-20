import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.time.LocalTime;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import PrincetonResources.Picture;

public class GUI extends javax.swing.JFrame {

	File archivo;

	int width;

	int height;

	int ancho;

	int alto;

	private javax.swing.JTextField currentDimensionsJTextField;

	private javax.swing.JMenuBar jMenuBar1;

	private javax.swing.JLabel pictureJLabel;

	private javax.swing.JSlider widthtJSlider;

	private JMenuItem mntmNewMenuItem;

	private JMenuItem mntmNewMenuItem_1;

	private JMenuItem mntmNewMenuItem_2;

	private JSlider heightJSlider;

	private JMenu mnNewMenu;

	private JSeparator separator;

	public GUI() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(GUI.class.getResource("/images/crop.png")));

		setTitle("Smart Image Resizer");

		initComponents();

	}

	private void initComponents() {

		pictureJLabel = new javax.swing.JLabel();

		currentDimensionsJTextField = new javax.swing.JTextField();
		currentDimensionsJTextField.setEditable(false);
		currentDimensionsJTextField.setHorizontalAlignment(SwingConstants.CENTER);

		currentDimensionsJTextField.setFont(new Font("Tahoma", Font.PLAIN, 18));

		// Declare JMenus
		jMenuBar1 = new javax.swing.JMenuBar();

		widthtJSlider = new javax.swing.JSlider();
		/* End Component Declarations */

		/* Start Component Attributes Modifications */

		// JFrame Attributes
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		setBackground(new java.awt.Color(102, 102, 102));

		pictureJLabel.setForeground(new java.awt.Color(102, 102, 102));

		pictureJLabel.setAlignmentX(0.5F);

		currentDimensionsJTextField.setBackground(new java.awt.Color(238, 238, 238));

		currentDimensionsJTextField.setBorder(null);

		currentDimensionsJTextField.setOpaque(true);

		widthtJSlider.setMajorTickSpacing(50);

		widthtJSlider.setMinimum(1);

		widthtJSlider.setMinorTickSpacing(1);

		widthtJSlider.setPaintTicks(true);

		widthtJSlider.setSnapToTicks(true);

		widthtJSlider.setValue(0);

		widthtJSlider.setInverted(true);

		widthtJSlider.setMaximumSize(new java.awt.Dimension(282, 30));

		widthtJSlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

			public void mouseDragged(java.awt.event.MouseEvent evt) {
				widthHeightJSliderMouseDragged(evt);
			}

		});

		setJMenuBar(jMenuBar1);

		mnNewMenu = new JMenu("File");
		mnNewMenu.setForeground(Color.BLACK);
		mnNewMenu.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		mnNewMenu.setIcon(new ImageIcon(GUI.class.getResource("/images/file.png")));
		jMenuBar1.add(mnNewMenu);

		mntmNewMenuItem_1 = new JMenuItem("Open");
		mntmNewMenuItem_1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		mnNewMenu.add(mntmNewMenuItem_1);
		mntmNewMenuItem_1.setIcon(new ImageIcon(GUI.class.getResource("/images/abrir.png")));

		separator = new JSeparator();
		mnNewMenu.add(separator);

		mntmNewMenuItem = new JMenuItem("Save");
		mntmNewMenuItem.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		mnNewMenu.add(mntmNewMenuItem);
		mntmNewMenuItem.setIcon(new ImageIcon(GUI.class.getResource("/images/save.png")));

		mntmNewMenuItem.addMouseListener(new MouseAdapter() {

			@Override

			public void mousePressed(MouseEvent e) {

				exportPicture();

			}

		});

		mntmNewMenuItem_1.addMouseListener(new MouseAdapter() {

			@Override

			public void mousePressed(MouseEvent e) {

				importPicture();

			}

		});

		mntmNewMenuItem_2 = new JMenuItem("Reset");
		mntmNewMenuItem_2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		mntmNewMenuItem_2.setIcon(new ImageIcon(GUI.class.getResource("/images/actualizar.png")));

		mntmNewMenuItem_2.addMouseListener(new MouseAdapter() {

			@Override

			public void mousePressed(MouseEvent e) {

				updatePictureDimension(false, true);

				widthtJSlider.setValue(0);

				heightJSlider.setValue(0);

				initializeNewPicture(false, true);

				System.out.println(width + " x " + height);
				currentDimensionsJTextField.setText(width + " x " + height);

			}

		});

		jMenuBar1.add(mntmNewMenuItem_2);

		heightJSlider = new JSlider();
		heightJSlider.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				updatePictureDimension(false, false);
			}
		});
		heightJSlider.setValue(0);
		heightJSlider.setSnapToTicks(true);
		heightJSlider.setPaintTicks(true);
		heightJSlider.setMinorTickSpacing(1);
		heightJSlider.setMinimum(1);
		heightJSlider.setMaximumSize(new Dimension(282, 30));
		heightJSlider.setMajorTickSpacing(50);
		heightJSlider.setInverted(true);

		JLabel lblNewLabel = new JLabel("Width");
		lblNewLabel.setIcon(new ImageIcon(GUI.class.getResource("/images/width.png")));
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));

		JLabel lblHeight = new JLabel("Height");
		lblHeight.setIcon(new ImageIcon(GUI.class.getResource("/images/height.png")));
		lblHeight.setFont(new Font("Tahoma", Font.PLAIN, 16));

		// Mount Components to JFrame (automatically done by NetBeans, highly suggest
		// using NetBeans to modify the layout rather then modifying this code
		// directly.)
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(101).addComponent(lblNewLabel).addGap(18)
						.addComponent(widthtJSlider, GroupLayout.PREFERRED_SIZE, 282, GroupLayout.PREFERRED_SIZE)
						.addGap(37).addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
						.addGap(18)
						.addComponent(heightJSlider, GroupLayout.PREFERRED_SIZE, 282, GroupLayout.PREFERRED_SIZE)
						.addGap(49)
						.addComponent(currentDimensionsJTextField, GroupLayout.PREFERRED_SIZE, 221,
								GroupLayout.PREFERRED_SIZE)
						.addContainerGap(53, Short.MAX_VALUE))
				.addComponent(pictureJLabel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 1284, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
								.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
										.addGroup(layout.createSequentialGroup()
												.addGroup(layout.createParallelGroup(Alignment.LEADING)
														.addComponent(widthtJSlider, GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.addComponent(lblNewLabel))
												.addGap(16))
										.addGroup(layout.createSequentialGroup()
												.addComponent(heightJSlider, GroupLayout.PREFERRED_SIZE, 46,
														GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED, 18, Short.MAX_VALUE)))
								.addComponent(currentDimensionsJTextField, GroupLayout.PREFERRED_SIZE, 39,
										GroupLayout.PREFERRED_SIZE))
						.addGap(48).addComponent(pictureJLabel, GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)));
		getContentPane().setLayout(layout);

		pack();
	}

	private Picture currentPicture; // current picture displayed on the GUI
	private SeamCarver seam;
	private SeamCarver seam2;// Seam carver which handles the removal and processing of the image
	private Thread seamThread;
	private Thread seamThread2; // thread allowing the computation of seams in the background
	private double ratio; // ratio to reduce picture size without affecting dimensions
	private final JFileChooser filechooser = new JFileChooser(); // file chooser for the import/export of pictures

	private void widthHeightJSliderMouseDragged(java.awt.event.MouseEvent evt) {
		updatePictureDimension(true, false);
	}

	// Initiate file chooser to import picture
	private void importPicture() {
		// file-mode - files only
		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// Valid file-types Note* first input 'png/jpg' not valid, however required to
		// indicate to user which picture formats are eligible
		filechooser.setFileFilter(new FileNameExtensionFilter("png/jpg", "png", "jpg"));

		int result = -99;
		// open dialog & save result - if opened result = 0, if cancelled result = 1
		result = filechooser.showOpenDialog(this);
		if (result == 0) {
			// show selected file-name on GUI

			// replace & reinitialize variables with the new imported picture
			archivo = filechooser.getSelectedFile();

			currentPicture = new Picture(archivo);

			initializeNewPicture(true, false);

		}
	}

	private void exportPicture() {

		filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = -1;

		result = filechooser.showSaveDialog(this);

		if (result == 0) {

			LocalTime myObj = LocalTime.now();

			currentPicture
					.save(filechooser.getSelectedFile() + "//" + myObj.toString().replace(":", "_") + "_cropped.png");

		}

	}

	private void updatePictureDimension(boolean w, boolean reset) {

		if (w) {

			currentPicture = seam.getPicture(widthtJSlider.getValue());

		}

		else {

			currentPicture = seam.getPicture(heightJSlider.getValue());

		}

		if (reset) {

			ancho = width;

			alto = height;

			currentPicture = new Picture(archivo);

			initializeNewPicture(true, false);

		}

		else {

			try {

				ancho = currentPicture.height();

				alto = currentPicture.width();

			}

			catch (Exception e) {

				currentPicture = new Picture(archivo);

				initializeNewPicture(true, false);

			}

		}

		currentDimensionsJTextField.setText(ancho + " x " + alto);

		setPicture(w, reset);

	}

	private void initializeNewPicture(boolean guardarSize, boolean reset) {

		if (seamThread != null)
			seam.stopThread();

		seam = new SeamCarver(currentPicture);

		seam.insertJSlider(widthtJSlider);

		seamThread = new Thread(seam);

		seamThread.setPriority(seamThread.MAX_PRIORITY);

		seamThread.start();

		seam2 = new SeamCarver(currentPicture);

		seam2.insertJSlider(heightJSlider);

		seamThread2 = new Thread(seam2);

		seamThread2.setPriority(seamThread2.MAX_PRIORITY);

		seamThread2.start();

		ratio = currentPicture.height() / 330.0;

		if (guardarSize) {

			width = currentPicture.width();

			height = currentPicture.height();

		}

		if (reset) {

			ancho = width;

			alto = height;

		}

		else {

			ancho = currentPicture.width();

			alto = currentPicture.height();

		}

		currentDimensionsJTextField.setText(ancho + " x " + alto);

		setPicture(true, reset);

		widthtJSlider.setMaximum(currentPicture.width());

		widthtJSlider.setValue(0);

		heightJSlider.setMaximum(currentPicture.width());

		heightJSlider.setValue(0);

	}

	private void setPicture(boolean width, boolean reset) {

		redimensionar(width, reset);

		pictureJLabel.setVerticalAlignment(JLabel.CENTER);

		pictureJLabel.updateUI();

		pictureJLabel.validate();

	}

	public void redimensionar(boolean wi, boolean reset) {

		try {

			int ancho = 330;

			int w;

			if (reset) {
				w = width;
			}

			else {
				w = currentPicture.width();
			}

			int alto = (int) (w / ratio);

			if (wi) {

				ancho = alto;

				alto = 330;

			}

			pictureJLabel.setIcon(new ImageIcon(((ImageIcon) currentPicture.getJLabel().getIcon()).getImage()
					.getScaledInstance(ancho, alto, java.awt.Image.SCALE_SMOOTH)));

			pictureJLabel.setHorizontalAlignment(JLabel.CENTER);

		}

		catch (Exception e) {
			//
		}

	}

	public static void main(String args[]) {

		try {

			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {

				if ("Nimbus".equals(info.getName())) {

					javax.swing.UIManager.setLookAndFeel(info.getClassName());

					break;

				}

			}

		}

		catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		java.awt.EventQueue.invokeLater(new Runnable() {

			public void run() {

				GUI gui = new GUI();

				gui.setVisible(true);

			}

		});
	}
}