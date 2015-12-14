package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.viewer.window.Database;

/****************************************************************************
 * 
 * Abstract class to manage a dynamic view
 * This view only creates the tree if and only if it is activated or visible.
 * <br/>
 * Otherwise, it's just an empty tree.
 * 
 * <p>The children needs to implements {@link IDynamicRootTree.createTree}
 * method to create the tree when needed.
 *
 ****************************************************************************/
abstract public class DynamicScopeView extends BaseScopeView 
implements IDynamicRootTree
{
    public void createPartControl(Composite aParent) {
    	super.createPartControl(aParent);
    	
    	// ----------------------------------------------------------
    	// add a listener to check if this view is visible or not
    	// if it is visible, we need to init the tree of the view
    	// ----------------------------------------------------------
    	
    	final IWorkbenchPage page = getSite().getPage();
    	if (page != null) {
    		final String firstID	    = getViewSite().getId();
    		final String secondID		= getViewSite().getSecondaryId();
    		final PartListener listener = new PartListener(this, firstID, secondID);
        	page.addPartListener(listener);
    	}
    }

    protected void refreshTree(RootScope root)
    {
		if (!root.hasChildren()) {
			// do not recreate the children if it's already created
			// unless if we are in filtering mode
			Experiment experiment = database.getExperiment();
			if (experiment.getRootScope() != null) {
				root = createTree(experiment);
				setInput(database, root, true);
			}
		} else {
			// check whether the view has the new created tree.
			// this special case happens when we "merge" two uncreated flat trees.
			// the merge method will force to create a flat tree WITHIN the experiment,
			//  but the view doesn't detect it.
			ScopeTreeViewer viewer = getTreeViewer();
			final Tree tree		   = viewer.getTree();
			if (tree.getItemCount() < 2) {
				// the tree is created, but the view doesn't know it.
				// let's force to reset the input
				setInput(database, root, true);
			}
		}
    }
	
	//////////////////////////////////////////////////////////////////////////////
	// Private classes
	//////////////////////////////////////////////////////////////////////////////
	static private final class PartListener implements IPartListener2
	{
		final private String firstID, secondaryID;
		final private IDynamicRootTree dynamicTree;
		
		public PartListener(IDynamicRootTree dynamicTree, String firstID, String secondaryID) {
			this.secondaryID = secondaryID;
			this.firstID	 = firstID;
			this.dynamicTree = dynamicTree;
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			partVisible(partRef);
		}

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
				DynamicScopeView view = (DynamicScopeView) partRef.getPart(false);
				Database database  = view.getDatabase();
				RootScope root = view.getRootScope();
				
				if (database != null) {
					view.refreshTree(root);
				}
			}
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {}

		private boolean isMyView(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);
			if (part instanceof DynamicScopeView) {
				DynamicScopeView view = (DynamicScopeView) part;
				
				final String ID	   = view.getViewSite().getId();
				final String secID = view.getViewSite().getSecondaryId();
				
				return (secondaryID.equals(secID) && firstID.equals(ID));
			}
			return false;
		}
	}
}
