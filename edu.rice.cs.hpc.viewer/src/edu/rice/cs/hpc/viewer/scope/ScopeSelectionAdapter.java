package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

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
		// pre-sorting : 
		// we don't want to sort all expanded items, including unwanted items
		// ----------------
		
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
