package edu.rice.cs.hpc.filter.view;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.jface.viewers.TableViewerColumn;

import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.data.filter.FilterAttribute;
import edu.rice.cs.hpc.filter.action.FilterInputDialog;
import edu.rice.cs.hpc.filter.service.FilterMap;
import edu.rice.cs.hpc.filter.service.FilterStateProvider;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.window.Window;

public class FilterPropertyDialog extends TitleAreaDialog {
	private Table table;
	private Button btnEdit, btnDelete;
	
	private final FilterMap filterMap;
	private FilterStateProvider 	 serviceProvider;
	//final private FilterMap data;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public FilterPropertyDialog(Shell parentShell) {
		super(parentShell);
		filterMap = new FilterMap();
	}

	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Filter property");
		setMessage("Add a new pattern, or select a pattern to edit, or delete a pattern.\nCheck a pattern to enable, uncheck to disable.\n");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		CheckboxTableViewer checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table = checkboxTableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableViewerColumn tableViewerColumn = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new PatternLabelProvider());
		TableColumn tblclmnPattern = tableViewerColumn.getColumn();
		tblclmnPattern.setToolTipText("Select a filter to delete or check/uncheck a filter pattern to enable/disable.");
		tblclmnPattern.setWidth(149);
		tblclmnPattern.setText("Pattern");
		
		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
		tableViewerColumn_1.setLabelProvider(new TypeLabelProvider());
		TableColumn tblclmnType = tableViewerColumn_1.getColumn();
		tblclmnType.setToolTipText("Select a type of filtering: 'Self only' means only the filtered scope is elided, 'Children only' means only the children of the matched node are elided, while 'Self and children' means the filtered scope and its descendants are elided.");
		tblclmnType.setWidth(100);
		tblclmnType.setText("Type");
		
		checkboxTableViewer.setContentProvider(new ArrayContentProvider());
		checkboxTableViewer.setCheckStateProvider(new CheckStateProvider());
		checkboxTableViewer.setComparator(new PatternViewerComparator());
		checkboxTableViewer.addCheckStateListener(new CheckStateListener(filterMap));
		
		Group grpActions = new Group(container, SWT.NONE);
		grpActions.setLayout(new FillLayout(SWT.VERTICAL));
		grpActions.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1));
		
		Button btnAdd = new Button(grpActions, SWT.NONE);
		btnAdd.setToolTipText("Add a new pattern filter");
		btnAdd.setText("Add");
		btnAdd.addSelectionListener(new ButtonSelectionListener(filterMap, checkboxTableViewer, 
											ButtonSelectionListener.ButtonType.Add));
		
		btnEdit = new Button(grpActions, SWT.NONE);
		btnEdit.setText("Edit");
		btnEdit.setEnabled(false);
		btnEdit.addSelectionListener(new ButtonSelectionListener(filterMap, checkboxTableViewer, 
											ButtonSelectionListener.ButtonType.Edit));
		
		btnDelete = new Button(grpActions, SWT.NONE);
		btnDelete.setText("Delete");
		btnDelete.setEnabled(false);
		btnDelete.addSelectionListener(new ButtonSelectionListener(filterMap, checkboxTableViewer, 
											ButtonSelectionListener.ButtonType.Delete));
		
		Button []buttons = new Button[] {btnEdit, btnDelete};
		checkboxTableViewer.addSelectionChangedListener(new SelectionChangedListener(buttons));

		// set the input of the table
	    setInput(checkboxTableViewer, filterMap);

	    setListener(checkboxTableViewer);

		return area;
	}

	static void setInput(CheckboxTableViewer viewer, FilterMap map) {
		viewer.setInput(map.getEntrySet());
	}
	
	@Override
	protected void okPressed() {
		// check if there's any change compared to the original filter
		FilterMap newMap = FilterMap.getInstance();
		newMap.iterator(); // TODO: forcing to load data
		boolean same = true;
		
		if (newMap.size() == filterMap.size()) {
			same = newMap.equals(filterMap);
		} else {
			same = false;
		}
		
		if (!same) {
			filterMap.save();
			serviceProvider.refresh();
		}
		super.okPressed();
	}
	
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
	
	private void setListener(CheckboxTableViewer tableViewer) {
		IWorkbenchWindow window = Util.getActiveWindow();
		final ISourceProviderService service   = (ISourceProviderService) window.
				getService(ISourceProviderService.class);
		serviceProvider  = (FilterStateProvider) service.getSourceProvider(FilterStateProvider.FILTER_REFRESH_PROVIDER);
	}
	
	///////////////////////////////////////////////////////////////////////
	// Helper classes
	///////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * Label for pattern column
	 *
	 */
	static private class PatternLabelProvider extends CellLabelProvider
	{
		@Override
		public void update(ViewerCell cell) {
			Entry<String, FilterAttribute> item = (Entry<String, FilterAttribute>) cell.getElement();
			cell.setText(item.getKey());
		}
	}
	
	static private class TypeLabelProvider extends CellLabelProvider
	{
		@Override
		public void update(ViewerCell cell) {
			Entry<String, FilterAttribute> item = (Entry<String, FilterAttribute>) cell.getElement();
			cell.setText(item.getValue().getFilterType());
		}
	}
	
	static private class CheckStateProvider implements ICheckStateProvider
	{
		
		@Override
		public boolean isGrayed(Object element) {
			return false;
		}
		
		@Override
		public boolean isChecked(Object element) {
			if (element instanceof Entry<?,?>) 
			{
				FilterAttribute value = (FilterAttribute) ((Entry<String, FilterAttribute>)element).getValue();
				return value.enable.booleanValue();
			}
			return false;
		}
	}
	
	static private class CheckStateListener implements ICheckStateListener
	{
		final private FilterMap filterMap;
		public CheckStateListener(FilterMap filterMap) {
			this.filterMap = filterMap;
		}
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			final Entry<String, FilterAttribute> element = (Entry<String, FilterAttribute>) event.getElement();
			final String key = element.getKey();
			//FilterMap map    = FilterMap.getInstance();
			
			// get the original attribute to be modified
			FilterAttribute attribute = element.getValue();
			attribute.enable = event.getChecked();
			
			// save to the registry
			filterMap.put(key, attribute);
			//map.save();
		}	    	
	}
	
	static private class PatternViewerComparator extends ViewerComparator
	{
    	@Override
    	public int compare(Viewer viewer, Object e1, Object e2) 
    	{
    		Entry<String, FilterAttribute> item1 = (Entry<String, FilterAttribute>) e1;
    		Entry<String, FilterAttribute> item2 = (Entry<String, FilterAttribute>) e2;
    		return (item1.getKey().compareTo(item2.getKey()));
    	}
	}
	
	private static class SelectionChangedListener implements ISelectionChangedListener
	{
		final private Button []btnSelections;
		
		public SelectionChangedListener(Button []buttons) {
			btnSelections = buttons;
		}
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			boolean enable = (selection != null) && (!selection.isEmpty());
			for(Button btn : btnSelections) {
				btn.setEnabled(enable);
			}
		}	
	}
	
	
	private static class ButtonSelectionListener extends SelectionAdapter
	{
		static enum ButtonType {Add, Delete, Edit};
		private final CheckboxTableViewer viewer;
		private final ButtonType type;
		private final FilterMap filterMap;
		
		public ButtonSelectionListener( FilterMap filterMap, CheckboxTableViewer viewer, ButtonType type) {
			this.viewer = viewer;
			this.type   = type;
			this.filterMap = filterMap;
		}
		public void widgetSelected(SelectionEvent e) {
			final Shell shell = Util.getActiveShell();
			switch (type) {
			case Add: 
				add(shell); 	break;
			case Edit:
				edit(shell); 	break;
			case Delete:
				delete(shell);	break;
			}
		}
		
		private boolean add(final Shell shell) {
			final FilterInputDialog dialog = new FilterInputDialog(shell, "Add a pattern", "", null);
			
			if (dialog.open() == IDialogConstants.OK_ID) {
				FilterAttribute attribute = dialog.getAttribute();
				//final FilterMap filterMap = FilterMap.getInstance();
				
				// save the new pattern to the registry
				filterMap.put(dialog.getValue(), attribute);
				updateView(filterMap);
				return true;
			}
			return false;
		}
		
		private boolean edit(final Shell shell) {
			ISelection selection = viewer.getSelection();
			if (selection != null) {
				final StructuredSelection select = (StructuredSelection) selection;
				final Entry<String, FilterAttribute> item= (Entry<String, FilterAttribute>) select.getFirstElement();
				if (item == null)
					return false;

				final FilterInputDialog dialog = new FilterInputDialog(shell, "Editing a filter", item.getKey(), item.getValue());
				if (dialog.open() == Window.OK)
				{
					//final FilterMap filterMap = FilterMap.getInstance();
					FilterAttribute attribute = dialog.getAttribute();
					if (filterMap.update(item.getKey(), dialog.getValue(), attribute))
					{
						updateView(filterMap);
						return true;
					} else {
						MessageDialog.openWarning(shell, "Unable to update", "Failed to update the pattern.");
					}
				}
			}
			return false;
		}
		
		private boolean delete(final Shell shell) {
			ISelection selection = viewer.getSelection();
			if (selection != null) {
				final StructuredSelection select = (StructuredSelection) selection;
				int size = select.size();
				String message = null;
				if (size == 1) {
					final Entry<String, FilterAttribute> item= (Entry<String, FilterAttribute>) select.getFirstElement();
					message = "Are you sure to delete '" + item.getKey() + "' pattern?";
				} else if (size > 1) {
					message = "Are you sure to delete " + size + " pattern?" ;
				}
				if (message != null) {
					if (MessageDialog.openConfirm(shell, "Deleting a pattern", message)) {
						//final FilterMap filterMap = FilterMap.getInstance();
						Iterator<Entry<String, FilterAttribute>> iterator = select.iterator();
						while(iterator.hasNext()) {
							Entry<String, FilterAttribute> item = iterator.next();
							filterMap.remove(item.getKey());
						}
						updateView(filterMap);
					}
				}
			}
			return false;
		}
		
		private void updateView(final FilterMap filterMap) {
			FilterPropertyDialog.setInput(viewer, filterMap);
		}
	}
}
