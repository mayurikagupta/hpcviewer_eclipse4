package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScopeCallerView;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.util.Utilities;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;


/***
 * 
 * Class to display label on the tree of views
 * 
 * A node of the tree contains three objects: [icon] [callsite] node_label
 * Every object has colors to indicate if they are clickable or not
 * - An object is clickable if they contain further information such as file source code
 * - Otherwise it is not clickable
 * 
 */
public class StyledScopeLabelProvider extends StyledCellLabelProvider {
	
	final private Styler STYLE_ACTIVE_LINK;
	final private ViewerWindow viewerWindow;

	/**
	 * Initialization of the class: preparing the colors for each object
	 * 
	 * @param window
	 */
	public StyledScopeLabelProvider(IWorkbenchWindow window) {
		super();
		STYLE_ACTIVE_LINK = StyledString.createColorRegistryStyler(JFacePreferences.ACTIVE_HYPERLINK_COLOR, null); 
		viewerWindow = ViewerWindowManager.getViewerWindow(window);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StyledCellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
	 */
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		
		if (element instanceof Scope) {
			Scope node = (Scope) element;
			final String text = getText(node);
			
			StyledString styledString= new StyledString();

			// ----------------------------------------------
			// special case for call sites :
			// - coloring the object for call site (if exists)
			// - show the icon if exists
			// ----------------------------------------------
			if (element instanceof CallSiteScope) {
				final CallSiteScope cs = (CallSiteScope) element;
				
				// the line number in XML is 0-based, while the editor is 1-based
				int line = 1+cs.getLineScope().getFirstLineNumber();
				boolean isReadable = Utilities.isFileReadable(cs.getLineScope());
				
				// show the line number
				if (line>0) {
					if (isReadable)
						styledString.append(String.valueOf(line)+": ", StyledString.COUNTER_STYLER);
					else 
						styledString.append(String.valueOf(line)+": ", StyledString.DECORATIONS_STYLER);
				}
				
				// show the icon
				final Image image = Utilities.getScopeNavButton(node);
				cell.setImage(image);
			}
			if(Utilities.isFileReadable(node)) {
				styledString.append( text, STYLE_ACTIVE_LINK );
			} else {
				styledString.append( text );
			}
			cell.setFont(Utilities.fontGeneral);
			cell.setText(styledString.toString());
			cell.setStyleRanges(styledString.getStyleRanges());
		}
	}

	// Wrap a Scope name to provide a more pleasing string for a tool tip. Specifically, 
	// whenever a tool tip line exceeds the specified target length, wrap it to a new
	// line following the next embedded space.
	public String wrapScopeName(String s, int desiredLineLength)
	{
		String out = ""; 
		if (!s.isEmpty()) {
			String indent = "  "; // prefix for lines 1..N
			String splits[] = s.split(" ",0); // split string at spaces
			// output initial split up to first space
			out += splits[0]; 
			int lineLength = splits[0].length(); // length of current line
			// for the remaining splits
			for (int i = 1; i < splits.length; i++) {
				// append a split to the current line and update the line length
				out         += " " + splits[i]; 
				lineLength  += 1   + splits[i].length(); 
				// if the current line exceeds the target length 
				if (lineLength >= desiredLineLength) {
					// if there are more splits
					if (i != splits.length - 1) { 
						// add a newline and indent the new current line
						out += "\n" + indent;
						// initialize length for the current line
						lineLength = indent.length(); 
					}
				}
			}
		}
		return out;
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
	 */
	public String getToolTipText(Object element)
	{
		if (element instanceof Scope) 
		{	
			final int minLengthForToolTip = 50;  
			final int toolTipDesiredLineLength = 80;
			String scopeName = ((Scope)element).getName();
			if (scopeName.length() > minLengthForToolTip) {
				return wrapScopeName(scopeName, toolTipDesiredLineLength);
			}
		}
		return null; // no tool tip for this cell
	}
	
	/**
	 * Return the text of the scope tree. By default is the scope name.
	 */
	private String getText(Scope node) 
	{
		String text = "";
			
		if (viewerWindow.showCCTLabel())  
		{
			//---------------------------------------------------------------
			// label for debugging purpose
			//---------------------------------------------------------------
			if (node instanceof CallSiteScope)
			{
				CallSiteScope caller = (CallSiteScope) node;
				Scope cct = caller.getLineScope();
				if (node instanceof CallSiteScopeCallerView) 
				{
					Object merged[] = ((CallSiteScopeCallerView)caller).getMergedScopes();
					if (merged != null) {
						int mult = merged.length + 1;
						text = mult + "*";
					}
					cct = ((CallSiteScopeCallerView)caller).getScopeCCT();
				}	
				text += "[c:" + caller.getCCTIndex() +"/" + cct.getCCTIndex()  + "] " ;
			} else
				text = "[c:" + node.getCCTIndex() + "] ";
		} 
		if (viewerWindow.showFlatLabel()) {
			text += "[f: " + node.getFlatIndex() ;
			if (node instanceof CallSiteScope) {
				ProcedureScope proc = ((CallSiteScope)node).getProcedureScope();
				text += "/" + proc.getFlatIndex();
			}
			text += "] ";
		} 
		text += node.getName();	
		return text;
	}

}
