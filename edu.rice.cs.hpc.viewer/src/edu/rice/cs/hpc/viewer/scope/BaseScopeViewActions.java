/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricManager;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;
import edu.rice.cs.hpc.viewer.window.Database;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

/**
 * @author laksonoadhianto
 *
 */
public class BaseScopeViewActions extends ScopeViewActions {

	public BaseScopeViewActions(Shell shell, IWorkbenchWindow window,
			Composite parent, CoolBar coolbar) {
		super(shell, window, parent, coolbar);
	}

	public void checkStates(Scope nodeSelected) {
    	boolean bCanZoomIn = objZoom.canZoomIn(nodeSelected);
		objActionsGUI.enableZoomIn( bCanZoomIn );
		objActionsGUI.enableHotCallPath( bCanZoomIn );
		
		checkStates();
	}

	public void checkStates() {
		objActionsGUI.enableZoomOut( objZoom.canZoomOut() );
	}

    /**
     * Each class has its own typical GUI creation
     */
	protected  Composite createGUI(Composite parent, CoolBar coolbar) {
    	this.objActionsGUI = new ScopeViewActionsGUI(this.objShell, this.objWindow, parent, this);
    	return objActionsGUI.buildGUI(parent, coolbar);
	}


	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.scope.ScopeViewActions#actionZoom(edu.rice.cs.hpc.viewer.scope.ScopeViewActions.ZoomType)
	 */
	protected void registerAction(IActionType type) {	}

	@Override
	protected IMetricManager getMetricManager() {
		return (Experiment) myRootScope.getExperiment();
	}

	@Override
	protected void addMetricColumn(DerivedMetric objMetric) {
		BaseExperiment exp = myRootScope.getExperiment();
		// get our database file and the experiment index assigned for it
		String dbPath = exp.getDefaultDirectory().getAbsolutePath();
		ViewerWindow vWin = ViewerWindowManager.getViewerWindow(this.objWindow);
		if (vWin == null) {
			System.out.printf("ScopeViewActions.addExtNewMetric: ViewerWindow class not found\n");
			return;
		}
		int dbNum = vWin.getDbNum((Experiment) exp);
		if (dbNum < 0) {
			System.out.printf("ScopeViewActions.addExtNewMetric: Database for path " + dbPath + " not found\n");
			return;
		}

		// get the views created for our database
		Database db = vWin.getDb(dbPath);
		if (db == null) {
			System.out.printf("ScopeViewActions.addExtNewMetric: Database class not found\n");
			return;
		}
		
		// add the new metric column to all registered views (cct, callers and flat)
		ExperimentView ev = db.getExperimentView();
		for(int i=0; i<ev.getViewCount(); i++) {
			BaseScopeView view = ev.getView(i);
			addMetricColumn(view, objMetric);
		}
		
	}
	
	/***
	 * Add a new metric column into a view's table
	 * @param view : the view that contains metric table
	 * @param objMetric : the new metric 
	 */
	protected void addMetricColumn(AbstractBaseScopeView view, DerivedMetric objMetric) {
		ScopeTreeViewer objTreeViewer = view.getTreeViewer();
		
		objTreeViewer.getTree().setRedraw(false);
		TreeViewerColumn colDerived = objTreeViewer.addTreeColumn(objMetric,  false);
		
		// update the viewer, to refresh its content and invoke the provider
		// bug SWT https://bugs.eclipse.org/bugs/show_bug.cgi?id=199811
		// we need to hold the UI to draw until all the data is available
		// 2012.09.21: do not refresh. It crashes on linux/gtk/ppc
		//objTreeViewer.refresh();	// we refresh to update the data model of the table
		
		// notify the GUI that we have added a new column
		ScopeViewActions objAction = view.getViewActions();
		objAction.addTreeColumn(colDerived.getColumn());
		//this.objActionsGUI.addMetricColumns(colDerived); 
		objTreeViewer.getTree().setRedraw(true);
		// adjust the column width 
		//colDerived.getColumn().pack();
		
		// instead of refresh, we use update which will reset the input and
		//	reinitialize the table. It isn't elegant, but works in all platforms
		view.updateDisplay();
	}
	

}
