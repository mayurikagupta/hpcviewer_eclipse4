package edu.rice.cs.hpc.viewer.scope.flat;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.scope.AbstractContentProvider;
import edu.rice.cs.hpc.viewer.scope.ScopeTreeViewer;

public class FlatViewContentProvider extends AbstractContentProvider {

	public FlatViewContentProvider(ScopeTreeViewer viewer) {
		super(viewer);
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


}
