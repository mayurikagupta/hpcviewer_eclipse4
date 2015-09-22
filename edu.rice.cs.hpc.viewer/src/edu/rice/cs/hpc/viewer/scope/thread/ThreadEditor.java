package edu.rice.cs.hpc.viewer.scope.thread;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
//import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.metric.ThreadDataCollectionFactory;
import edu.rice.cs.hpc.viewer.window.Database;

public class ThreadEditor extends EditorPart implements PaintListener 
{
	static final public String ID = "edu.rice.cs.hpc.viewer.scope.thread.ThreadEditor";
	
	private Canvas canvas ;
	private Combo cbMetrics;
	
	private Database database;
	private int metricIndex = 0;
	private Image image;
	//private Display display;
	
	public ThreadEditor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		this.setSite(site);
		this.setInput(input);
		
		if (input instanceof ThreadEditorInput) {
			database = ((ThreadEditorInput)input).getDatabase();
		} else {
			throw new PartInitException("Input is not a known class: " + input.getClass());
		}
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose()
	{
		if (image != null && !image.isDisposed())
			image.dispose();
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		
		final Composite cArea = new Composite(parent, SWT.NONE);
		cbMetrics = new Combo(cArea, SWT.READ_ONLY | SWT.DROP_DOWN);
		
		final Experiment experiment = database.getExperiment();
		final BaseMetric []metrics   = experiment.getMetricRaw();
		if (metrics != null)
		{
			for(BaseMetric m : metrics)
			{
				cbMetrics.add(m.getDisplayName());
			}
			cbMetrics.select(metricIndex);
		}
		
		canvas = new Canvas(cArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(canvas);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(cArea);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(cArea);
		
		canvas.addPaintListener(this);
		//display = getSite().getShell().getDisplay();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void paintControl(PaintEvent e) {
		if (image == null) {
			paint(e.gc);
		} else {
			e.gc.drawImage(image, 0, 0);
		}
	}

	private void paint(GC gc)
	{
		final Experiment experiment = database.getExperiment();
		IThreadDataCollection manager;
		try {
			manager = ThreadDataCollectionFactory.build(experiment);
			BaseMetric []metrics = experiment.getMetricRaw();
			if (metrics == null)
				return;
			
			final Rectangle r = canvas.getClientArea();
			
			final RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
			Scope parent = root;
			int numChildren = root.getChildCount();
			
			if (image != null && !image.isDisposed())
				image.dispose();
			image = new Image(gc.getDevice(), r.width, r.height);
			GC gcImage = new GC(image);
			
			while(numChildren > 0)
			{					
				int cct_id  = parent.getCCTIndex(); 
				print(manager, metrics[metricIndex], cct_id);

				// display the children
				for(int i=0; i<numChildren; i++)
				{
					Scope scope = (Scope) parent.getChildAt(i);
					cct_id  = scope.getCCTIndex(); 
					print(manager, metrics[metricIndex], cct_id);
				}
				parent = (Scope) parent.getChildAt(0);
				numChildren = parent.getChildCount();
			}
			gcImage.dispose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void print(IThreadDataCollection data, BaseMetric metric, int cct_id)
	{
		double[] values;
		System.out.format("[%d] ", cct_id);
		try {
			values = data.getMetrics(cct_id, ((MetricRaw)metric).getRawID(), ((MetricRaw)metric).getSize()); 
			for(double v : values)
			{
				System.out.print(v + " ");
			}
			System.out.println();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Make 256 rainbow color palette - Jean-Luc Pon's algorithm.
	 */
	public static PaletteData makeRainbow1Palette(int nb){
		RGB rainbow[]=new RGB[256];
		RGB colorVal[]=new RGB[5];
		colorVal[0]=new RGB(200,0,250);
		colorVal[1]=new RGB(40,40,255);
		colorVal[2]=new RGB(40,255,40);
		colorVal[3]=new RGB(250,250,0);
		colorVal[4]=new RGB(255,0,0);
		double colorPos[]=new double[5];
		colorPos[0]=0.0;
		colorPos[1]=0.25;
		colorPos[2]=0.50;
		colorPos[3]=0.75;
		colorPos[4]=1.0;
		int colId;
		colId=0;
		for (int i=0; i < nb; i++) {
			double r1, g1, b1;
			double r2, g2, b2;
			double r=(double)i / (double)nb;
			if (colId < (colorPos.length - 2) && r >= colorPos[colId + 1])     colId++;
			r1=(double)colorVal[colId].red;
			g1=(double)colorVal[colId].green;
			b1=(double)colorVal[colId].blue;
			r2=(double)colorVal[colId + 1].red;
			g2=(double)colorVal[colId + 1].green;
			b2=(double)colorVal[colId + 1].blue;
			double rr=(r - colorPos[colId]) / (colorPos[colId + 1] - colorPos[colId]);
			if (rr < 0.0)     rr=0.0;
			if (rr > 1.0)     rr=1.0;
			rainbow[i]=new RGB((int)(r1 + (r2 - r1) * rr),(int)(g1 + (g2 - g1) * rr),(int)(b1 + (b2 - b1) * rr));
		}
		return new PaletteData(rainbow);
	}


}
