package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/********************************
 * 
 * Base class of content provider of all views
 * All the children need to implement hasChildren method
 *
 ********************************/
public abstract class AbstractContentProvider
	implements ITreeContentProvider, ILazyTreeContentProvider 
{
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
    }
 

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {}

	@Override
	public void updateElement(Object parent, int index) {

		int child_position = index;
		
		if (parent instanceof RootScope) {
			// if the parent is a root, the first row is a header
			// this header row is not counted as a child 
			child_position = index - 1;
		}
		
		Object element = viewer.getSortScope( (Scope)parent, child_position);
		if (element != null) {
			viewer.replace(parent, index, element);
			updateChildCount(element, -1);
		}
	} 

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		Object []children = getChildren(element);
		int length = (children == null ? 0 : children.length);
		
		viewer.setChildCount(element, length);
	}	
}
