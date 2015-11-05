package edu.rice.cs.hpc.filter.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.filter.view.FilterView;

public class ShowFilterView extends AbstractHandler 
{
	final public static String ID = "edu.rice.cs.hpc.filter.action.ShowFilterView";

	@Override 
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		final IWorkbenchWindow w  = HandlerUtil.getActiveWorkbenchWindow(event);
		final IWorkbenchPage page = w.getActivePage();
		if (page != null)
		{
			IViewReference vref = page.findViewReference(FilterView.ID);
			
			if (vref == null) {
				// show the view
				try {
					page.showView(FilterView.ID);
				} catch (PartInitException e) {
					MessageDialog.openError(page.getWorkbenchWindow().getShell(), "Unable to open Filter view", 
							e.getMessage()	);
					e.printStackTrace();
				}
			} else {
				// activate the view
				IViewPart view = vref.getView(true);
				page.activate(view);
			}
		}
		return null;
	}
}
