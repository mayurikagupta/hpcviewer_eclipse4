package edu.rice.cs.hpc.viewer.util;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


public abstract class AbstractFilterDialog extends TitleAreaDialog 
{
	final private String []label;
	final private boolean []checked;
	final private String title, message;
	protected ColumnCheckTableViewer objCheckBoxTable ;
	protected Text objSearchText;
	protected boolean[] results;

	protected ArrayList<PropertiesModel> arrElements;
	
	public AbstractFilterDialog(Shell parentShell, String title, String message, 
			String []label, boolean []checked) {
		super(parentShell);
		this.label   = label;
		this.checked = checked;
		this.title	 = title;
		this.message = message;
	}

	/**
	 * Get the list of checked and unchecked items
	 * @return the array of true/false
	 */
	public boolean[] getResult() {
		return results;
	}

	
	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(title);
		setMessage(message);
		
		return contents;
	}
	
	@Override
	protected Control createDialogArea(Composite aParent) {
		Composite composite = new Composite(aParent, SWT.BORDER);

		GridLayout grid = new GridLayout();
		grid.numColumns=1;
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(grid);

		// prepare the buttons: check and uncheck
		GridLayout gridButtons = new GridLayout();
		gridButtons.numColumns=3;
		Composite groupButtons = new Composite(composite, SWT.BORDER);
		groupButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupButtons.setLayout(gridButtons);

		// check button
		Button btnCheckAll = new Button(groupButtons, SWT.NONE);
		btnCheckAll.setText("Check all");
		btnCheckAll.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnCheckAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				objCheckBoxTable.setAllChecked(true);
			}
		});
		// uncheck button
		Button btnUnCheckAll = new Button(groupButtons, SWT.NONE);
		btnUnCheckAll.setText("Uncheck all");
		btnUnCheckAll.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnUnCheckAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				objCheckBoxTable.setAllChecked(false);
			}
		});
		
		createAdditionalButton(groupButtons);
		
		
		// set the layout for group filter
		Composite groupFilter = new Composite(composite, SWT.BORDER);
		groupFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(groupFilter);
		
		// Laks 2009.03.19: add string to match
		Label lblFilter = new Label (groupFilter, SWT.FLAT);
		lblFilter.setText("Filter:");
		
		objSearchText = new Text (groupFilter, SWT.BORDER);
		// expand the filter field as much as possible horizontally
		GridDataFactory.fillDefaults().grab(true, false).applyTo(objSearchText);
		objSearchText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// get the filter of the table
				ColumnFilter objFilter = (ColumnFilter) objCheckBoxTable.getFilters()[0];
				// reset the filter
				objFilter.setKey(objSearchText.getText());
				objCheckBoxTable.refresh();
				objCheckBoxTable.setCheckedElements(getCheckedItemsFromGlobalVariable());
			}
		});
		

		// list of columns (we use table for practical purpose)
		Table table = new Table(composite, SWT.CHECK | SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		this.objCheckBoxTable = new ColumnCheckTableViewer(table) ;
		objCheckBoxTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		// setup the content provider
		objCheckBoxTable.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object parent) {
				if (parent instanceof ArrayList<?>)
					return ((ArrayList<?>)parent).toArray();
				else
					return null;
			}
			public void dispose() {
				// nope
			}
			public void inputChanged(Viewer v, Object oldInput, Object newInput) {
				//throw it away
			}
		}); 

		// laks 2009.03.20: check user action when updating the status of the item
		objCheckBoxTable.addCheckStateListener(new ICheckStateListener() {
			// status has been changed. we need to reset the global variable too !
			public void checkStateChanged(CheckStateChangedEvent event) {
				PropertiesModel objItem = (PropertiesModel) event.getElement();
				objItem.isVisible = event.getChecked();
				// check if the selected item is in the list
				if (arrElements.get(objItem.iIndex) != objItem) {
					arrElements.get(objItem.iIndex).isVisible = objItem.isVisible;
				}
			}

		});
		this.objCheckBoxTable.setLabelProvider(new CheckLabelProvider());
		// laksono 2009.03.19: add the filter for this table
		ColumnFilter objFilter = new ColumnFilter();
		this.objCheckBoxTable.addFilter(objFilter);
		//
		this.updateContent(); // fill the table
		
		getShell().setText(title);
		
		return composite;
	}

	//--------------------------------------------------
	//	PRIVATE & PROTECTED METHODS
	//--------------------------------------------------

	/**
	 * Populate the content of the table with the new information
	 */
	private void updateContent() {
		if(label == null)
			return; // caller of this object need to set up the column first !
		int nbColumns = label.length;
		// PropertiesModel objProps[] = new PropertiesModel[nbColumns];
		arrElements = new ArrayList<PropertiesModel>(nbColumns);
		ArrayList<PropertiesModel> arrColumns = new ArrayList<PropertiesModel>();
		
		int index = 0;
		for(int i=0;i<nbColumns;i++) {
			boolean isVisible = (checked != null ? checked[i] : true);		
			PropertiesModel model = new PropertiesModel(isVisible, label[i], index);
			index++;
			
			arrElements.add( model );
			// we need to find which columns are visible
			if(isVisible) {
				arrColumns.add(model);
			}
		}
		
		this.results = new boolean[index];
		for(int i=0; i<index; i++) {
			results[i] = false; // initialize with false value
		}

		this.objCheckBoxTable.setInput(arrElements);
		this.objCheckBoxTable.setCheckedElements(arrColumns.toArray());
	}


	
	/**
	 * 
	 * @return
	 */
	protected Object[] getCheckedItemsFromGlobalVariable () {
		Object []result = null;
		if (arrElements != null) 
		{
			int nb = arrElements.size();
			ArrayList<PropertiesModel> arrCheckedElements = new ArrayList<PropertiesModel>();

			for (int i=0; i<nb; i++) {
				if (arrElements.get(i).isVisible)
					arrCheckedElements.add(arrElements.get(i));
			} 
			result = arrCheckedElements.toArray();
		}
		return result;
	}
	
	
	/*
	 * derived from the parent
	 */
	protected void okPressed() {
		// laksono 2009.04.14: bug fix, retrieving all checked elements into the results
		for (int i=0; i<arrElements.size(); i++) {
			 boolean isVisible = (this.arrElements.get(i).isVisible);
			 results[i] = isVisible;
		} 
		
		super.okPressed();	// this will shut down the window
	}


	// ======================================================================================
	//--------------------------------------------------
	//	CLASS DEFINITION
	//--------------------------------------------------
	/**
	 * Label provider for the table
	 * @author laksono
	 *
	 */
	protected class CheckLabelProvider implements ILabelProvider {
		public void addListener(ILabelProviderListener arg0) {
			// Throw it away
		}
		public void dispose() {
			// Nothing to dispose
		}
		public boolean isLabelProperty(Object arg0, String arg1) {
			return false;
		}
		public void removeListener(ILabelProviderListener arg0) {
			// Ignore
		}
		public Image getImage(Object arg0) {
			return null;
		}
		public String getText(Object arg0) {
			PropertiesModel prop = (PropertiesModel) arg0;
			return prop.sTitle;
		}
	}

	//--------------------------------------------------
	/**
	 * Data model for the column properties
	 * Containing two items: the state and the title
	 *
	 */
	protected class PropertiesModel {
		public boolean isVisible;
		public String sTitle;
		public int iIndex;

		public PropertiesModel(boolean b, String s, int i) {
			this.isVisible = b;
			this.sTitle = s;
			this.iIndex = i;
		}
	}

	/**
	 * Class to filter the content of the table of columns
	 */
	protected class ColumnFilter extends ViewerFilter {
		// the key to be matched
		private String sKeyToMatch;
		//@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			// check if the key exist
			if ( (sKeyToMatch != null) && (sKeyToMatch.length()>0) ){
				// check if the element is good
				assert (element instanceof PropertiesModel);
				PropertiesModel objProperty = (PropertiesModel) element;
				// simple string matching between the key and the column name
				boolean bSelect = objProperty.sTitle.toUpperCase().contains(sKeyToMatch);
				return bSelect;
			}
			return true;
		}

		/**
		 * Method to set the keywords to filter
		 * @param sKey
		 */
		public void setKey ( String sKey ) {
			sKeyToMatch = sKey.toUpperCase();
		}
	}

	/**
	 * Class to mimic CheckboxTableViewer to accept the update of checked items
	 *
	 */
	protected class ColumnCheckTableViewer extends CheckboxTableViewer {

		/**
		 * constructor: link to a table
		 */
		public ColumnCheckTableViewer(Table table) {
			super(table);
			// TODO Auto-generated constructor stub
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.CheckboxTableViewer#setAllChecked(boolean)
		 */
		public void setAllChecked(boolean state) {
			super.setAllChecked(state);
			// additional action: update the global variable for the new state !
			TableItem[] items = getTable().getItems();
			for (int i=0; i<items.length; i++) {
				TableItem objItem = items[i];
				PropertiesModel objModel = (PropertiesModel) objItem.getData();
				objModel.isVisible = state;
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.CheckboxTableViewer#setCheckedElements(java.lang.Object[])
		 */
		public void setCheckedElements(Object[] elements) { 
			assertElementsNotNull(elements);
			TableItem[] items = getTable().getItems();
			// collect the items from the displayed table
			Hashtable<Object, TableItem> set = new Hashtable<Object, TableItem>(items.length * 2 + 1);
			for (int i = 0; i < items.length; ++i) {
				set.put(items[i].getData(), items[i]);
			}
			// change the status of the displayed items based on the "global" variable
			for (int i=0; i<elements.length; i++) {
				TableItem objItem = set.get(elements[i]);
				if (objItem != null) {
					objItem.setChecked(true);
				}
			}
		}
	}
	
	abstract protected void createAdditionalButton(Composite parent); 
}
