package edu.rice.cs.hpc.viewer.scope.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;
import edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView;
import edu.rice.cs.hpc.viewer.window.Database;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

/*****************************************************************************
 * 
 * A collection of methods to build a thread view intelligently
 * The function {@link ThreadViewFactory.build} returns the thread view of
 * a given experiment and the list of threads as follows:
 * 
 * build ( experiment x threads ) -> thread_view
 * <ul>
 *  <li>if the input is unique, it will create the view with the column for the threads.
 *  <li>if the experiment is not unique but the threads is unique, it activates the view
 * 	but creates the new column for threads
 *  <li>if the input is not unique, it just activates the view
 *  <li>if the thread is null, it prompts a dialog box to ask the list of threads to display
 * </ul>
 *****************************************************************************/
class ThreadViewFactory 
{
	/****
	 * Build or activate a thread view. <br>
	 * This method will prompt a dialog box to ask users which threads to be displayed.
	 * 
	 * @param window : the current active window
	 * @param experiment : the current database
	 * 
	 * @return the thread view if successful, null otherwise
	 */
	static public IViewPart build(IWorkbenchWindow window, RootScope rootScope) 
	{
		return build(window, rootScope, null);
	}
	
	static public String getThreadViewKey(RootScope root) 
	{
		BaseExperiment experiment = root.getExperiment();
		return experiment.getDefaultDirectory().getAbsolutePath() + "." + root.getType();
	}
	
	/*****
	 * Build or activate a thread view. <br>
	 * This method will prompt a dialog box to ask users which threads to be displayed.
	 * 
	 * @param window : the current active window
	 * @param experiment : the current database
	 * @param threads : the list of threads to be displayed. If the this parameter is null,
	 * it will prompt users to choose the threads.
	 * 
	 * @return the thread view if successful, null otherwise
	 */
	static public IViewPart build(IWorkbenchWindow window, RootScope rootScope, List<Integer> threads) 
	{
		final IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			final ViewerWindow vWin     = ViewerWindowManager.getViewerWindow(window);
			final Experiment experiment = (Experiment)rootScope.getExperiment();
			
			final Database db = vWin.getDb(experiment.getDefaultDirectory().getAbsolutePath());
			try {
				if (threads == null) {
					// ask users to select which threads to be displayed
					threads = getThreads(window, db);
					// if users click cancel, we return immediately
					if (threads == null)
						return null;
				}
				
				// check if the view already exists
				IViewPart view = null;
				final String key = getThreadViewKey(rootScope);
				
				final IViewReference vref = page.findViewReference(ThreadView.ID, key);
				if (vref != null) {
					// it's there. we need to activate it and set the new threads
					view = vref.getView(true);
					
				} else {
					// it doesn't exist. need to create it.
					view = page.showView(ThreadView.ID, key, 
							IWorkbenchPage.VIEW_ACTIVATE);
					if (view != null && (view  instanceof ThreadView)) 
					{
						ExperimentView ev = db.getExperimentView();
						ev.addView((AbstractBaseScopeView) view);
					}
				}
				if (view != null) {
					if (threads != null) {
						((ThreadView)view).setInput(db, rootScope);
						((ThreadView)view).addTableColumns(threads);
						page.activate(view);
						return view;
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
		IThreadDataCollection threadData = db.getThreadDataCollection();
		double []ids = threadData.getRankLabels();
		String []labels = new String [ids.length];
		for(int i=0; i<ids.length; i++) 
		{
			labels[i] = String.valueOf(ids[i]);
		}
		ThreadFilterDialog dialog = new ThreadFilterDialog(window.getShell(), labels);
		if (dialog.open() == Window.OK) {
			boolean []result = dialog.getResult();
			if (result != null) {
				List<Integer> threads = new ArrayList<Integer>();
				for(int i=0; i<result.length; i++) {
					if (result[i]) {
						threads.add(i);
					}
				}
				if (threads.size()>0)
					return threads;
			}
			
		}
		return null;
	}

}
