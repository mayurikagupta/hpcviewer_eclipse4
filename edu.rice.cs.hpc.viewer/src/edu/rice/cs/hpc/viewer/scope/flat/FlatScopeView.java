/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope.flat;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.scope.AbstractContentProvider;
import edu.rice.cs.hpc.viewer.scope.BaseScopeView;
import edu.rice.cs.hpc.viewer.scope.ScopeViewActions;
import edu.rice.cs.hpc.viewer.scope.StyledScopeLabelProvider;

/**
 * Class for flat view scope. 
 * This class has special actions differed from calling context and caller view
 *
 */
public class FlatScopeView extends BaseScopeView {
    public static final String ID = "edu.rice.cs.hpc.viewer.scope.FlatScopeView";
    

    public void createPartControl(Composite aParent) {
    	super.createPartControl(aParent);
    	
    	// ----------------------------------------------------------
    	// add a listener to check if this view is visible or not
    	// if it is visible, we need to init the tree of the view
    	// ----------------------------------------------------------
    	
    	final IWorkbenchPage page = getSite().getPage();
    	if (page != null) {
        	final String myID = getViewSite().getId();
    		final PartListener listener = new PartListener(myID);
    		page.addPartListener(listener);
    	}
    }
    
    
    protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
    	IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
        return new FlatScopeViewActions(this.getViewSite().getShell(), window, parent, coolbar); 
    }

	@Override
	protected CellLabelProvider getLabelProvider() {
		return new StyledScopeLabelProvider(this.getSite().getWorkbenchWindow());
	}

	@Override
	protected void createAdditionalContextMenu(IMenuManager mgr, Scope scope) {}

	@Override
	protected void mouseDownEvent(Event event) {}

	@Override
	protected AbstractContentProvider getScopeContentProvider() {
		return new FlatViewContentProvider();
	}

	@Override
	protected void updateDatabase(Experiment newDatabase) {}
	
	//////////////////////////////////////////////////////////////////
	// Private class
	//////////////////////////////////////////////////////////////////

	
	static private class PartListener implements IPartListener2
	{
		final private String myID;
		
		PartListener(String ID) {
			this.myID = ID;
		}
		
		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			if (partRef.getId().equals(myID)) {
				//System.out.println("FSV visible");
			}
		}
		
		@Override
		public void partOpened(IWorkbenchPartReference partRef) {}
		
		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {}
		
		@Override
		public void partHidden(IWorkbenchPartReference partRef) {}
		
		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {}
		
		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			if (partRef.getId().equals(myID)) {
				//System.out.println("FSV closed");
				partRef.getPage().removePartListener(this);
			}
		}
		
		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {}
		
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {}
	}

}
