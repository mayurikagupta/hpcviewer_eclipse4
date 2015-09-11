package edu.rice.cs.hpc.viewer.scope.thread;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView;
import edu.rice.cs.hpc.viewer.scope.AbstractContentProvider;
import edu.rice.cs.hpc.viewer.scope.BaseScopeViewActions;
import edu.rice.cs.hpc.viewer.scope.ScopeViewActions;
import edu.rice.cs.hpc.viewer.scope.ScopeViewActionsGUI;
import edu.rice.cs.hpc.viewer.scope.StyledScopeLabelProvider;
import edu.rice.cs.hpc.viewer.window.Database;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

/******************************************************************************************
 * 
 * View part for showing metric database of a certain thread
 *
 ******************************************************************************************/
public class ThreadView extends AbstractBaseScopeView 
{
	static final public String ID = "edu.rice.cs.hpc.viewer.scope.thread.ThreadView";
	private List<Integer> threads = new ArrayList<>();
	
	/**********
	 * Show the menu to open this view
	 * 
	 * @param mgr : parent menu manager
	 * @param window : current active window
	 * @param experiment : active experiment (should have metric database)
	 */
	static public void showMenu(IMenuManager mgr, IWorkbenchWindow window, 
			Experiment experiment)
	{
		mgr.add(new ThreadViewAction(window, experiment));
	}

	@Override
	public void updateDisplay() {
		// return immediately when there's no database or the view is closed (disposed)
        if (database == null || treeViewer == null || treeViewer.getTree().isDisposed())
        	return;

		// reassign root scope
        final Experiment experiment = getExperiment();
		RootScope rootCCT = experiment.getRootScope(RootScopeType.CallingContextTree);
		myRootScope = createRoot(rootCCT);

		if (myRootScope.getChildCount()>0) {
        	treeViewer.setInput(myRootScope);
        	objViewActions.updateContent(experiment, myRootScope);
        	
        	TreeItem item = treeViewer.getTree().getItem(1);
        	treeViewer.getTree().select(item);
        	
        	objViewActions.checkNodeButtons();
        }
	}

	@Override
	protected void initTableColumns(boolean keepColumnStatus) {
		ensureThreads();
		
		Database db 	= getDatabase();
		Experiment exp 	= db.getExperiment();
		MetricRaw []mr  = exp.getMetricRaw();
		if (mr != null) {
			boolean sort = true;
			for(MetricRaw m : mr)
			{
				MetricRaw mdup = (MetricRaw) m.duplicate();
				mdup.setThread(threads);
				treeViewer.addTreeColumn(m, sort);
				
				// sort initially the first column metric
				if (sort)
					sort = false;
			}
		}
	}

	@Override
	protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
    	IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
        return new BaseScopeViewActions(this.getViewSite().getShell(), window, parent, coolbar) {
        	protected  Composite createGUI(Composite parent, CoolBar coolbar) {
            	this.objActionsGUI = new ScopeViewActionsGUI(this.objShell, this.objWindow, parent, this, false);
            	return objActionsGUI.buildGUI(parent, coolbar);
        	}
        }; 
	}

	@Override
	protected void mouseDownEvent(Event event) {
	}

	@Override
	protected void createAdditionalContextMenu(IMenuManager mgr, Scope scope) {
	}

	@Override
	protected AbstractContentProvider getScopeContentProvider() {
		return new AbstractContentProvider() {};
	}

	@Override
	protected void enableFilter(boolean isEnabled) {
    	if (treeViewer.getTree().isDisposed())
    		return;
    	
    	Experiment experiment = getExperiment();
		
		// reassign root scope
		myRootScope = experiment.getRootScope(RootScopeType.CallingContextTree);
		// update the content of the view
		updateDisplay();
	}

	@Override
	protected CellLabelProvider getLabelProvider() {
		return new StyledScopeLabelProvider( this.getSite().getWorkbenchWindow() ); 
	}

	/***
	 * copy CCT root and duplicate its children to create a new 
	 * root scope for this thread view.
	 * 
	 * @param rootCCT : root CCT
	 * 
	 * @return RootScope
	 */
	private RootScope createRoot(RootScope rootCCT)
	{
		ensureThreads();
		
		// create and duplicate the configuration
		RootScope rootThread = (RootScope) rootCCT.duplicate();
		
		// rename the root
		StringBuffer sb = new StringBuffer();
		sb.append("Thread ");
		sb.append(threads.get(0));
		if (threads.size() > 1) {
			sb.append(" - ");
			sb.append(threads.get(threads.size()-1));
		}
		rootThread.setName(sb.toString());
		
		// duplicate the children
		for(int i=0; i<rootCCT.getChildCount(); i++)
		{
			Scope scope = (Scope) rootCCT.getChildAt(i);
			rootThread.addSubscope(scope);
		}
		return rootThread;
	}
	
	private void ensureThreads()
	{
		int threads_size = threads.size();
		if (threads_size == 0) {
			threads.add(0);
		}
	}
	/*******************************
	 * 
	 * Action class to show the menu to open this view
	 *
	 *******************************/
	static private class ThreadViewAction extends Action
	{
		final private IWorkbenchWindow window;
		final private Experiment experiment;
		
		public ThreadViewAction(IWorkbenchWindow window, Experiment experiment)
		{
			super("Show thread view");
			this.window = window;
			this.experiment = experiment;
		}
		
		@Override
		public void run()
		{
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				try {
					final String path = experiment.getDefaultDirectory().getAbsolutePath();
					
					// check if the view already exists
					final IViewReference vref = page.findViewReference(ID, path);
					if (vref != null) {
						// it's there. we need to activate it
						IViewPart view = vref.getView(true);
						page.activate(view);
					} else {
						// it doesn't exist. need to create it.
						IViewPart view = page.showView(ID, path, 
								IWorkbenchPage.VIEW_ACTIVATE);
						if (view != null && (view  instanceof ThreadView)) 
						{
							ViewerWindow vWin = ViewerWindowManager.getViewerWindow(window);
							final Database db = vWin.getDb(experiment.getDefaultDirectory().getAbsolutePath());
							RootScope scope   = experiment.getRootScope(RootScopeType.CallingContextTree);
							((ThreadView)view).setInput(db, scope, false);
						}
					}
				} catch (PartInitException e) {
					e.printStackTrace();
					MessageDialog.openError(window.getShell(), "Error", e.getMessage());
				}
			}
		}
	}
}
