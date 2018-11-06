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
	// direction
	public static final int ASC = 1;
	public static final int NONE = 0;	// unused: for init only
	public static final int DESC = -1;

	final private ScopeTreeViewer viewer;	// viewer
	final private TreeViewerColumn column;		// column

    ScopeSelectionAdapter(ScopeTreeViewer viewer, TreeViewerColumn column) {
		this.viewer 	= viewer;
		this.column     = column;
    	new ScopeComparator();
	}
	
	public void widgetSelected(SelectionEvent e) {
		
		// ----------------
		// pre-sorting : collapse all tree items to speed up the sort.
		// we don't want to sort all expanded items, including unwanted items
		// ----------------
		viewer.collapseAll();
		
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
		int tdirection = column.getColumn().getParent().getSortDirection();
		
		if( tdirection == ASC ) {
			setSorter(DESC);
		} else  {
			setSorter(ASC);
		}
		
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
		
		if( direction == NONE ) {
			
			col.getParent().setSortColumn(null);
			
		} else {			
			col.getParent().setSortColumn(col);
			
			if( direction == ASC ) {
				swt_direction = SWT.DOWN;				
			} else {
				swt_direction = SWT.UP;
			}
		}
		
		// prepare the sorting for this column with a specific direction
		
		col.getParent().setSortDirection(swt_direction);
		viewer.setSortDirection(direction);		
		viewer.setSortColumn(column);
		
		 // have to call this before actually sorting the elements
		viewer.sort_start();
		
		// do the sort
		viewer.refresh();

		viewer.sort_end();
		
		viewer.getTree().setRedraw(true);
	}
}
