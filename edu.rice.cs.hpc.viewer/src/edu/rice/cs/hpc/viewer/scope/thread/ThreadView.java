package edu.rice.cs.hpc.viewer.scope.thread;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView;
import edu.rice.cs.hpc.viewer.scope.AbstractContentProvider;
import edu.rice.cs.hpc.viewer.scope.BaseScopeViewActions;
import edu.rice.cs.hpc.viewer.scope.ScopeViewActions;
import edu.rice.cs.hpc.viewer.scope.StyledScopeLabelProvider;
import edu.rice.cs.hpc.viewer.window.Database;

public class ThreadView extends AbstractBaseScopeView {
	static final public String ID = "edu.rice.cs.hpc.viewer.scope.thread.ThreadView";
	
	public ThreadView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void updateDisplay() {
		// return immediately when there's no database or the view is closed (disposed)
        if (database == null || treeViewer == null || treeViewer.getTree().isDisposed())
        	return;

		// reassign root scope
        final Experiment experiment = getExperiment();
		myRootScope = experiment.getRootScope(RootScopeType.CallingContextTree);

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
		Database db 	= getDatabase();
		Experiment exp 	= db.getExperiment();
		MetricRaw []mr  = exp.getMetricRaw();
		
		for(MetricRaw m : mr)
		{
			treeViewer.addTreeColumn(m, false);
		}
	}

	@Override
	protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
    	IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
        return new BaseScopeViewActions(this.getViewSite().getShell(), window, parent, coolbar); 
	}

	@Override
	protected void mouseDownEvent(Event event) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createAdditionalContextMenu(IMenuManager mgr, Scope scope) {
		// TODO Auto-generated method stub

	}

	@Override
	protected AbstractContentProvider getScopeContentProvider() {
		// TODO Auto-generated method stub
		return new AbstractContentProvider() {
		};
	}

	@Override
	protected void enableFilter(boolean isEnabled) {
		// TODO Auto-generated method stub

	}

	@Override
	protected CellLabelProvider getLabelProvider() {
		// TODO Auto-generated method stub
		return new StyledScopeLabelProvider( this.getSite().getWorkbenchWindow() ); 
	}

}
