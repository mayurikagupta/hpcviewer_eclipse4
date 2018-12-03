package edu.rice.cs.hpc.traceviewer.data.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.common.util.ProcedureClassData;
import edu.rice.cs.hpc.data.util.IProcedureTable;
import edu.rice.cs.hpc.traceviewer.data.util.ProcedureClassMap;

/**************************************************************
 * A data structure designed to hold all the name-color pairs
 * needed for the actual drawing.
 **************************************************************/
public class ColorTable implements IProcedureTable
{
	static final public int COLOR_ICON_SIZE = 8;
	
	static private final int cmin = 16;
	static private final int cmax = 200 - cmin;
	static private final long RANDOM_SEED = 612543231;
	
	/**All of the function names stored in this colorTable.*/
	final private ArrayList<String> procNames;
	
	/**The display this ColorTable uses to generate the random colors.*/
	final private Display display;

	/** user defined color */
	final private ProcedureClassMap classMap;
	
	final private Random random_generator;

	// data members

	private ColorImagePair IMAGE_WHITE;
	final private	HashMap<String, ColorImagePair> colorMatcher;

	/**Creates a new ColorTable with Display _display.*/
	public ColorTable()
	{
		procNames = new ArrayList<String>();
		// Initializes the CSS that represents time values outside of the
		// time-line.
		procNames.add(CallPath.NULL_FUNCTION);
		
		display = Util.getActiveShell().getDisplay();
		
		// rework the color assignment to use a single random number stream
		random_generator = new Random((long)RANDOM_SEED);

		// initialize the procedure-color map (user-defined color)
		classMap = new ProcedureClassMap(display);
		
		colorMatcher = new HashMap<String, ColorTable.ColorImagePair>();
		
		initializeWhiteColor();
	}
	
	/**
	 * Dispose the allocated resources
	 */
	public void dispose() {
		for (ColorImagePair pair: colorMatcher.values()) {
			pair.dispose();
		}
		
		colorMatcher.clear();
		classMap.clear();
	}
	
	/**
	 * Returns the color in the colorMatcher that corresponds to the name's class
	 * @param name
	 * @return
	 */
	public Color getColor(String name)
	{
		final ColorImagePair cipair = colorMatcher.get(name);
		if (cipair != null) {
			return cipair.getColor();
		} else {
			return createColorImagePair(name).getColor();
		}
	}
	
	/**
	 * returns the image that corresponds to the name's class
	 * @param name
	 * @return
	 */
	public Image getImage(String name) 
	{
		final ColorImagePair cipair = colorMatcher.get(name);
		if (cipair != null) {
			return cipair.getImage();
		} else {
			return createColorImagePair(name).getImage();
		}
	}
	
	private ColorImagePair createColorImagePair(String procName)
	{
		RGB rgb = getProcedureColor( procName, cmin, cmax, random_generator );
		Color c = new Color(display, rgb);
		Image i = createImage(display, rgb);
		ColorImagePair cip = new ColorImagePair(c, i);
		
		colorMatcher.put(procName, cip);
		
		return cip;
	}
	
	
	/************************************************************************
	 * Adds a name to the list of function names in this ColorTable.
	 * NOTE: Doesn't create a color for this name. All the color creating
	 * is done in setColorTable.
	 * @param name The function name to be added.
	 ************************************************************************/
	public void addProcedure(String name)
	{
		if(!procNames.contains(name))
			procNames.add(name);
	}
	
	
	/***********************************************************************
	 * create an image based on the color
	 * the caller is responsible to free the image
	 * 
	 * @param display
	 * @param color
	 * @return an image (to be freed)
	 ***********************************************************************/
	static public Image createImage(Display display, RGB color) {
		PaletteData palette = new PaletteData(new RGB[] {color} );
		ImageData imgData = new ImageData(COLOR_ICON_SIZE, COLOR_ICON_SIZE, 1, palette);
		Image image = new Image(display, imgData);
		return image;
	}
	
	/***********************************************************************
	 * retrieve color for a procedure. If the procedure has been assigned to
	 * 	a color, we'll return the allocated color, otherwise, create a new one
	 * 	randomly.
	 * 
	 * @param name name of the procedure
	 * @param colorMin minimum integer value
	 * @param colorMax maximum integer value
	 * @param r random integer
	 * 
	 * @return RGB
	 ***********************************************************************/
	private RGB getProcedureColor( String name, int colorMin, int colorMax, Random r ) {
		
		// if the name matches, we return the user-defined color
		// otherwise, we randomly create a color for this name
		
		ProcedureClassData value = this.classMap.get(name);
		final RGB rgb;
		if (value != null)
			rgb = value.getRGB();
		else 
			rgb = new RGB(	colorMin + r.nextInt(colorMax), 
							colorMin + r.nextInt(colorMax), 
							colorMin + r.nextInt(colorMax));
		return rgb;
	}

	/************************************************************************
	 * Initialize the predefined-value of white color
	 * 
	 * If the white color value is not initialize, we create a new one
	 * Otherwise, do nothing.
	 ************************************************************************/
	private void initializeWhiteColor() {
		if (IMAGE_WHITE == null || IMAGE_WHITE.getImage().isDisposed()) {
			// create our own white color so we can dispose later, instead of disposing
			//	Eclipse's white color
			final RGB rgb_white = display.getSystemColor(SWT.COLOR_WHITE).getRGB();
			final Color col_white = new Color(display, rgb_white);
			final Image img_white = createImage(display, rgb_white);
			
			IMAGE_WHITE = new ColorImagePair(col_white, img_white );
			
			colorMatcher.put(CallPath.NULL_FUNCTION, IMAGE_WHITE);
		}
	}
	
	
	/************************************************************************
	 * class to pair color and image
	 * @author laksonoadhianto
	 *
	 ************************************************************************/
	private class ColorImagePair {
		private Color color;
		private Image image;
		
		/****
		 * create a color-image pair
		 * @param color c
		 */
		ColorImagePair(Color color, Image image) {
			// create an empty image filled with color c
			this.image = image;
			this.color = color;
		}
		
		/***
		 * get the color 
		 * @return
		 */
		public Color getColor() {
			return this.color;
		}
		
		/***
		 * get the image
		 * @return
		 */
		public Image getImage() {
			return this.image;
		}
		
		public void dispose() {
			this.color.dispose();
			this.image.dispose();
		}
	}
}