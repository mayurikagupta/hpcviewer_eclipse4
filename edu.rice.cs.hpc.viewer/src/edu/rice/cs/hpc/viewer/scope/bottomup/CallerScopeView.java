/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope.bottomup;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.scope.AbstractContentProvider;
import edu.rice.cs.hpc.viewer.scope.BaseScopeView;
import edu.rice.cs.hpc.viewer.scope.BaseScopeViewActions;
import edu.rice.cs.hpc.viewer.scope.ScopeViewActions;
import edu.rice.cs.hpc.viewer.scope.StyledScopeLabelProvider;
import edu.rice.cs.hpc.viewer.window.Database;

/**
 * View class for caller view. At the moment, the caller view is the same as calling context view
 *
 */
public class CallerScopeView extends BaseScopeView {
    public static final String ID = "edu.rice.cs.hpc.viewer.scope.CallerScopeView";

	/* (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.scope.BaseScopeView#createActions(org.eclipse.swt.widgets.Composite, org.eclipse.swt.widgets.CoolBar)
	 */
	@Override
	protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
    	final IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
    	
    	final IWorkbenchPage page = window.getActivePage();
    	page.addPartListener(new PartListener(getViewSite().getSecondaryId()));
    	
        return new BaseScopeViewActions(this.getViewSite().getShell(), window, parent, coolbar); 
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
		return new CallerViewContentProvider();
	}

	@Override
	protected void updateDatabase(Experiment newDatabase) {
		
		// ---------------------------------------------------------------------------
		// it is important to notify the content provider that we have new database
		// ---------------------------------------------------------------------------
		CallerViewContentProvider objContent = (CallerViewContentProvider) this.treeViewer.getContentProvider();
		if (objContent != null)
			objContent.setDatabase(newDatabase);
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// Private classes
	//////////////////////////////////////////////////////////////////////////////
	static private final class PartListener implements IPartListener2
	{
		final String secondaryID;
		
		public PartListener(String secondaryID) {
			this.secondaryID = secondaryID;
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			if (isMyView(partRef)) {
				partRef.getPage().removePartListener(this);
			}
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			if (isMyView(partRef)) {
				// i am visible now
				CallerScopeView view = (CallerScopeView) partRef.getPart(false);
				Database database = view.getDatabase();
				RootScope rootClt = view.getRootScope();
				
				if (database != null && !rootClt.hasChildren()) {
					Experiment experiment = database.getExperiment();
					RootScope rootCCT = experiment.getRootScope(RootScopeType.CallingContextTree);
					rootClt = experiment.createCallersView(rootCCT, rootClt);
					view.setInput(database, rootClt, true);
				}
			}
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {}

		private boolean isMyView(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);
			if (part instanceof CallerScopeView) {
				CallerScopeView view = (CallerScopeView) part;
				final String secID = view.getViewSite().getSecondaryId();
				return (secondaryID.equals(secID));
			}
			return false;
		}
	}
}
