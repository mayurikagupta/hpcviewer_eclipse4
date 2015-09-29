package edu.rice.cs.hpc.viewer.scope.thread;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.viewer.util.AbstractFilterDialog;

public class ThreadFilterDialog extends AbstractFilterDialog {

	public ThreadFilterDialog(Shell parentShell, String []labels) {
		super(parentShell, "Select threads to view", "Select threads to view", labels, null);
	}

	@Override
	protected void createAdditionalButton(Composite parent) {}

}
