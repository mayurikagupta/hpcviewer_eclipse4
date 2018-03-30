package edu.rice.cs.hpc.traceviewer.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/***
 * 
 * class to manage a message label to show a message
 * for a couple of seconds, and then remove it back,
 * returns to the original empty label.
 *
 */
public class MessageLabelManager 
{
	final Label   messageLabel;
	final Display display;

	private Thread  threadAlarm;
	
	public MessageLabelManager(Display display, Label messageLabel) {

		this.messageLabel    = messageLabel;
		this.display 		 = display;
	}
	
	public void showMessage(String message) {

		display.syncExec(new Runnable() {

			@Override
			public void run() {
				Color colorYellow = display.getSystemColor(SWT.COLOR_YELLOW);
				Color colorRed    = display.getSystemColor(SWT.COLOR_DARK_RED);

				messageLabel.setForeground(colorYellow);
				messageLabel.setBackground(colorRed);

				messageLabel.setText(message);
			}
		});
		
		threadAlarm = new Thread() {
			
			/*
			 * (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			public void run() {
				try {
					// wait for a couple of seconds
					sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				restoreLabel();
			}
			
			private void restoreLabel() {
				display.asyncExec(new Runnable() {

					@Override
					public void run() {
						if (messageLabel == null || messageLabel.isDisposed()) {
							return;
						}
						Color colorBackground   = display.getActiveShell().getBackground();
						messageLabel.setText("");
						messageLabel.setBackground(colorBackground);
					}
					
				});
			}
		};
		threadAlarm.start();
	}
}
