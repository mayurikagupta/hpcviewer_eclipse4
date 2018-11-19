package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.swt.graphics.Image;

import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.util.Utilities;

import org.eclipse.jface.viewers.StyledString;

public class ScopeColumnLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider 
{

	@Override
	public StyledString getStyledText(Object element) {
		Scope node = (Scope) element;
		final String text = node.getName();//getText(node);
		
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
		}
		if(Utilities.isFileReadable(node)) {
			styledString.append( text, Utilities.STYLE_ACTIVE_LINK );
		} else {
			styledString.append( text );
		}
		return styledString;
	}
	
	@Override
	public Image getImage(Object element) {
		return Utilities.getScopeNavButton(element);
	}

}
