package edu.rice.cs.hpc.viewer.scope.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.viewer.window.Database;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

class ThreadViewFactory 
{

	static public IViewPart build(IWorkbenchWindow window, Experiment experiment) 
	{
		return build(window, experiment, null);
	}
	
	static public IViewPart build(IWorkbenchWindow window, Experiment experiment, List<Integer> threads) 
	{
		final IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			final ViewerWindow vWin = ViewerWindowManager.getViewerWindow(window);
			final Database db = vWin.getDb(experiment.getDefaultDirectory().getAbsolutePath());
			try {
				if (threads == null) {
					// ask users to select which threads to be displayed
					threads = getThreads(window, db);
					// if users click cancel, we return immediately
					if (threads == null)
						return null;
				}
				final String path = experiment.getDefaultDirectory().getAbsolutePath();
				
				// check if the view already exists
				final IViewReference vref = page.findViewReference(ThreadView.ID, path);
				if (vref != null) {
					// it's there. we need to activate it and set the new threads
					IViewPart view = vref.getView(true);
					((ThreadView)view).addTableColumns(threads);
					page.activate(view);
					return view;
					
				} else {
					// it doesn't exist. need to create it.
					IViewPart view = page.showView(ThreadView.ID, path, 
							IWorkbenchPage.VIEW_ACTIVATE);
					if (view != null && (view  instanceof ThreadView)) 
					{

						RootScope scope   = experiment.getRootScope(RootScopeType.CallingContextTree);
						if (threads != null) {
							((ThreadView)view).setInput(db, scope, threads);
							return view;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageDialog.openError(window.getShell(), "Error", e.getMessage());
			}
		}
		return null;
	}
	
	
	static private List<Integer> getThreads(IWorkbenchWindow window, Database db) 
			throws NumberFormatException, IOException 
	{
		double []ids = db.getThreadLevelDataManager().getProcessIDsDouble(0);
		List<Double> dbList = new ArrayList<Double>(ids.length);
		for(int i=0; i<ids.length; i++) {
			dbList.add(ids[i]);
		}
		// put up a dialog with the open databases in the current window in a drop down selection box
		ListSelectionDialog dlg = new ListSelectionDialog(window.getShell(), dbList, 
			new ArrayContentProvider(), new LabelProvider(), "Select the processes/threads to view:");
		dlg.setTitle("Select processes/threads");
		dlg.open();
		Object[] selectedDatabases = dlg.getResult();
		if (selectedDatabases != null) 
		{
			List<Integer> indexOfThreads = new ArrayList<Integer>(selectedDatabases.length);
			for(int i=0; i<selectedDatabases.length; i++) 
			{
				int index = dbList.indexOf(selectedDatabases[i]);
				indexOfThreads.add(index);
			}
			return indexOfThreads;
		}
		return null;
	}

}
