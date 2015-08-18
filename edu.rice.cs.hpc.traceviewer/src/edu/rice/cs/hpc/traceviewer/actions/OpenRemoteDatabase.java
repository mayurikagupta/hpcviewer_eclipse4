package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;

public class OpenRemoteDatabase extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final IViewSite vSite = ( IViewSite ) HandlerUtil.getActiveSite(event);
		final IStatusLineManager status = vSite.getActionBars().getStatusLineManager();

		TraceDatabase.openRemoteDatabase(window, status);		
		
		return null;
	}

}
