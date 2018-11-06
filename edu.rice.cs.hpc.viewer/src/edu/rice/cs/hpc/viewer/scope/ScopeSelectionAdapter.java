package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import edu.rice.cs.hpc.data.util.OSValidator;
import edu.rice.cs.hpc.viewer.util.Utilities;

/*********************************************************
 * 
 * Class to handle column header selection (a.k.a sort event)
 *
 *********************************************************/
public class ScopeSelectionAdapter extends SelectionAdapter 
{
	final private ScopeTreeViewer viewer;
	final private TreeViewerColumn column;
	
	final static public int SORT_ASC  = 1;
	final static public int SORT_DESC = -1;
	
	private int current_sort_direction = SORT_DESC;

    ScopeSelectionAdapter(ScopeTreeViewer viewer, TreeViewerColumn column) {
		this.viewer 	= viewer;
		this.column     = column;
    	new ScopeComparator();
	}
	
	public void widgetSelected(SelectionEvent e) {
		
		// ----------------
		// pre-sorting 
		// ----------------
		Object []elements = null;
		if (OSValidator.isMac()) {
			// --------------------------------------------------------------------
			//Eclipse Indigo bug on Mac OS: expanding a long call path will cause
			// SWT to slowly sort tree items. Somehow Eclipse also expands other
			// collapsed tree items as well.
			// --------------------------------------------------------------------
			// save the current expaded elements to be restored after the sort
			elements = viewer.getExpandedElements();
			
			// collapse all the items
			viewer.collapseAll();
		}
		// before sorting, we need to check if the first row is an element header 
		// something like "aggregate metrics" or zoom-in item
		Tree tree = viewer.getTree();
		if (tree.getItemCount()==0)
			return; // no items: no need to sort
		
		TreeItem item = viewer.getTree().getItem(0);
		Image imgItem = item.getImage(0);
		String []sText = Utilities.getTopRowItems(viewer);
		
		// ----------------
		// sorting 
		// ----------------
		setSorter(current_sort_direction);
		
		// ----------------
		// post-sorting 
		// ----------------
		if(sText != null) {
			Utilities.insertTopRow(viewer, imgItem, sText);
		}
		if (elements != null) {
			viewer.setExpandedElements(elements);
		}
	}
	
	/**
	 * Sort the column according to the direction
	 * @param sorter
	 * @param direction
	 */
	public void setSorter(int direction) {
		// bug Eclipse no 199811 https://bugs.eclipse.org/bugs/show_bug.cgi?id=199811
		// sorting can be very slow in mac OS
		// we need to manually disable redraw before comparison and the refresh after the comparison 
				
		viewer.getTree().setRedraw(false);
		
		TreeColumn col    = column.getColumn();
		int swt_direction = SWT.NONE;
		col.getParent().setSortColumn(col);
		
		if( direction == SORT_ASC ) {
			swt_direction = SWT.UP;
			current_sort_direction = SORT_DESC;
		} else {
			swt_direction = SWT.DOWN;
			current_sort_direction = SORT_ASC;
		}
		
		// prepare the sorting for this column with a specific direction
		
		col.getParent().setSortDirection(swt_direction);
		viewer.setSortDirection(current_sort_direction);		
		viewer.setSortColumn(column);
		
		 // have to call this before actually sorting the elements
		viewer.sort_start();
		
		// do the sort
		viewer.refresh();

		viewer.sort_end();
		
		viewer.getTree().setRedraw(true);
	}
}
