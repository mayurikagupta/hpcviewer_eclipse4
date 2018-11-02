package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public abstract class AbstractContentProvider
	implements ITreeContentProvider, ILazyTreeContentProvider 
{
    private RootScope  root;
    private ScopeTreeViewer viewer;
    
    public AbstractContentProvider(ScopeTreeViewer viewer) {
    	this.viewer = viewer;
    	new ScopeComparator();
    }
        
    /**
     * get the number of elements (called by jface)
     */
    public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
    }

    /**
     * find the list of children
     */
    public Object[] getChildren(Object parentElement) {
    	if(parentElement instanceof Scope) {
    		// normal mode
        	Scope parent = ((Scope) parentElement);
        	Object arrChildren[] = parent.getChildren();
        	// if the database has empty data, the children is null
        	if (arrChildren != null && arrChildren.length>0)
        	{
    			return arrChildren;
        	}
    	}
    	return null;
    }
    

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
    	if(element instanceof Scope)
            return ((Scope) element).getParent();
    	else
    		return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
    	if(element instanceof Scope)
            return ((Scope) element).hasChildren(); // !((Scope.Node) element).isLeaf();
    	else
    		return false;
    }

    /**
    * Notifies this content provider that the given viewer's input
    * has been switched to a different element.
    *
    * @param viewer the viewer
    * @param oldInput the old input element, or <code>null</code> if the viewer
    *   did not previously have an input
    * @param newInput the new input element, or <code>null</code> if the viewer
    *   does not have an input
    */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	this.root = (RootScope) newInput;
    }
 

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {}

	@Override
	public void updateElement(Object parent, int index) {
		Object element = null;
		
		TreeViewerColumn column = viewer.getSortColumn();
		Object data = column.getColumn().getData();
		if (data == null) {
			// sort based on the name of the scope
		} else if (data instanceof BaseMetric) {
			// sort based on the metric
		}
		int child_position = index;
		
		if (parent instanceof RootScope) {
			child_position = index - 1;
		} else {
		}
		
		element = viewer.getSortScope( (Scope)parent, child_position);
		
		viewer.replace(parent, index, element);
		updateChildCount(element, -1);
	} 

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		int length = 0;

		if (element instanceof Scope) {			
			Scope current = (Scope) element;
			length = current.getChildCount();
		}
		if (element == root) {
			length = root.getChildCount();
		}
		
		viewer.setChildCount(element, length);
	}	
}
