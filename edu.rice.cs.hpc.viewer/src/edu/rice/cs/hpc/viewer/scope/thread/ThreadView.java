package edu.rice.cs.hpc.viewer.scope.thread;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricManager;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.metric.MetricRawManager;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.graph.GraphMenu;
import edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView;
import edu.rice.cs.hpc.viewer.scope.AbstractContentProvider;
import edu.rice.cs.hpc.viewer.scope.ScopeViewActions;
import edu.rice.cs.hpc.viewer.scope.StyledScopeLabelProvider;
import edu.rice.cs.hpc.viewer.window.Database;

/******************************************************************************************
 * 
 * View part for showing metric database of a certain thread
 *
 ******************************************************************************************/
public class ThreadView extends AbstractBaseScopeView 
{
	static final public String ID = "edu.rice.cs.hpc.viewer.scope.thread.ThreadView";
	//private List<Integer> threads = new ArrayList<>();
	private IMetricManager metricManager;
	
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
	
	static public void showView(IWorkbenchWindow window, 
			Experiment experiment, List<Integer> threads)
	{
		ThreadViewAction action = new ThreadViewAction(window, experiment, threads);
		action.run();
	}

	/*****
	 * Customized setInput from {@link edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView}
	 * using list of threads as the additional parameter.
	 * @param db : database
	 * @param scope : the root (should be cct root)
	 * @param threads : the list of threads
	 */
	public void setInput(Database db, RootScope scope, List<Integer> threads)
	{
    	database = db;
    	myRootScope = scope;// try to get the aggregate value

        // tell the action class that we have built the tree
        this.objViewActions.setTreeViewer(treeViewer);
        
        initTableColumns(threads);
        
        // notify the children class to update the display
    	updateDisplay();
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
	
	/*****
	 * add new columns of metrics for a given list of threads<br/>
	 * If the threads already displayed in the table, we do nothing.
	 * Otherwise, we'll add new columns for these threads.
	 * 
	 * @param threads : list of threads
	 */
	void addTableColumns(List<Integer> threads) {
		// 1. check if the threads already exist in the view
		TreeColumn []columns = getTreeViewer().getTree().getColumns();
		boolean col_exist = false;
		for (TreeColumn col : columns) {
			Object obj = col.getData();
			if (obj instanceof MetricRaw) {
				List<Integer> lt = ((MetricRaw)obj).getThread();
				if (lt.size() == threads.size()) {
					for(Integer i : threads) {
						col_exist = lt.contains(i);
						if (!col_exist) {
							break;
						}
					}
				}
			}
			if (col_exist) break;
		}
		
		// 2. if the column of this thread exist, exit.
		if (col_exist)
			return;

		// 3. add the new metrics into the table
		initTableColumns(threads);
		
		// 4. update the table content, including the aggregate experiment
		updateDisplay();
	}
	
	/****
	 * customized table initialization
	 * @param threads : list of threads
	 */
	void initTableColumns(List<Integer> threads) {
		
		IMetricManager mm = getMetricManager();
		BaseMetric []mr   = mm.getMetrics();
		if (mr == null)
		{
			objViewActions.showErrorMessage("The database has no thread-level metrics.");
			objViewActions.disableButtons();
		}
		else {
			boolean sort = true;
			for(BaseMetric m : mr)
			{
				MetricRaw mdup = (MetricRaw) m.duplicate();
				mdup.setThread(threads);
				mdup.setDisplayName(threads + "-" + mdup.getDisplayName());
				treeViewer.addTreeColumn(mdup, sort);
				
				// sort initially the first column metric
				if (sort)
					sort = false;
			}
		}
	}

	@Override
	protected void initTableColumns(boolean keepColumnStatus) {	
		ArrayList<Integer> threads = new ArrayList<Integer>(1);
		threads.add(0);
		initTableColumns(threads);
	}

	@Override
	protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
    	IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
    	
        return new ThreadScopeViewAction(this, window, parent, coolbar, getMetricManager()) ;
	}

	@Override
	protected void mouseDownEvent(Event event) {
	}

	@Override
	protected void createAdditionalContextMenu(IMenuManager mgr, Scope scope) {
		GraphMenu.createAdditionalContextMenu(getViewSite().getWorkbenchWindow(), mgr, database, scope);
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
		return new StyledScopeLabelProvider( getSite().getWorkbenchWindow() ); 
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
		// create and duplicate the configuration
		RootScope rootThread = (RootScope) rootCCT.duplicate();
		
		// duplicate the children
		for(int i=0; i<rootCCT.getChildCount(); i++)
		{
			Scope scope = (Scope) rootCCT.getChildAt(i);
			rootThread.addSubscope(scope);
		}
		return rootThread;
	}
	
	
	private IMetricManager getMetricManager() 
	{
		if (metricManager != null)
			return metricManager;
		
		// create a new metric manager for this view
		Database db 	= getDatabase();
		if (db != null ){
			Experiment exp 	= db.getExperiment();
			if (exp != null) {
				metricManager = new MetricRawManager(exp);
				return metricManager;
			}
		}
		return null;
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
		private List<Integer> threads;
		
		public ThreadViewAction(IWorkbenchWindow window, Experiment experiment)
		{
			this(window, experiment, null);
		}
		
		public ThreadViewAction(IWorkbenchWindow window, Experiment experiment, List<Integer> threads)
		{
			super("Show thread view ");
			this.window 	= window;
			this.experiment = experiment;
			this.threads	= threads;
		}
		
		@Override
		public void run()
		{
			ThreadViewFactory.build(window, experiment, threads);
		}
	}
}
