package edu.rice.cs.hpc.test.gui;

import static org.junit.Assert.*;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.rice.cs.hpc.viewer.metric.MetricColumnDialog;

public class MetricColumnDialogTest 
{
	final static private int MAX_ITEMS = 10;
	private String   []labels;
	private boolean	 []checked;
	private MetricColumnDialog dialog;
	
	@Before
	public void setUp() throws Exception {
		Display display = Display.getCurrent();
		Shell shell = new Shell(display);
		
		labels = new String[MAX_ITEMS];
		checked = new boolean[MAX_ITEMS];
		
		for(int i=0; i<MAX_ITEMS; i++) {
			labels[i] = "Column "+String.valueOf(i);
			checked[i] = (i < 5);
		}
		dialog = new MetricColumnDialog(shell, labels, checked);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOkPressed() {
		if (dialog.open() == Window.OK) {
			boolean []result = dialog.getResult();
			assertNotNull(result);
		}
	}

}
