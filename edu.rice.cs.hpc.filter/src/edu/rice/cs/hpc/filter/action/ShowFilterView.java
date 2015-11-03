package edu.rice.cs.hpc.filter.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.filter.view.FilterView;

public class ShowFilterView extends AbstractHandler 
{
	final public static String ID = "edu.rice.cs.hpc.filter.action.ShowFilterView";

	@Override 
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		final IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		if (page != null)
		{
			boolean oldVal = HandlerUtil.toggleCommandState(event.getCommand());
			boolean newVal = !oldVal;
			
			if (newVal) {
				// show the view
				try {
					page.showView(FilterView.ID);
				} catch (PartInitException e) {
					MessageDialog.openError(page.getWorkbenchWindow().getShell(), "Unable to open Filter view", 
							e.getMessage()	);
					e.printStackTrace();
				}
			} else {
				// hide the view
				final IViewPart part = page.findView(FilterView.ID);
				if (part != null)
					page.hideView( part );
			}
		}
		return null;
	}
}
