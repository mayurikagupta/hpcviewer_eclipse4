package edu.rice.cs.hpc.viewer.scope.thread;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView;
import edu.rice.cs.hpc.viewer.scope.AbstractContentProvider;
import edu.rice.cs.hpc.viewer.scope.ScopeViewActions;

public class ThreadView extends AbstractBaseScopeView {

	public ThreadView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void updateDisplay() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initTableColumns(boolean keepColumnStatus) {
		// TODO Auto-generated method stub

	}

	@Override
	protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void mouseDownEvent(Event event) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createAdditionalContextMenu(IMenuManager mgr, Scope scope) {
		// TODO Auto-generated method stub

	}

	@Override
	protected AbstractContentProvider getScopeContentProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void enableFilter(boolean isEnabled) {
		// TODO Auto-generated method stub

	}

	@Override
	protected CellLabelProvider getLabelProvider() {
		// TODO Auto-generated method stub
		return null;
	}

}
