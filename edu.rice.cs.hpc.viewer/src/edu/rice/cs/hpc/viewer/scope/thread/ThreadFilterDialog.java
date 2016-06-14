package edu.rice.cs.hpc.viewer.scope.thread;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.viewer.util.AbstractFilterDialog;

public class ThreadFilterDialog extends AbstractFilterDialog {

	public ThreadFilterDialog(Shell parentShell, String []labels) {
		super(parentShell, "Select threads to view", "Please check any threads to be viewed.\nYou can narrow the list by specifying partial name of the threads on the filter.\nFor instance, to display just the main threads, you can specify '.0'", labels, null);
	}

	@Override
	protected void createAdditionalButton(Composite parent) {}

}
