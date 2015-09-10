package edu.rice.cs.hpc.viewer.window;

import java.io.IOException;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;
import edu.rice.cs.hpc.viewer.metric.ThreadLevelDataManager;

public class Database {
	private int winIndex;
	private Experiment experiment;
	private ExperimentView view;
	private ThreadLevelDataManager dataManager;

	/**
	 *  get the index of the viewer window in which this database is displayed.
	 * @return
	 */
	public int getWindowIndex () {
		return winIndex;
	}


	/**
	 *  get the Experiment class used for this database
	 * @return
	 */
	public Experiment getExperiment () {
		return experiment; //this.view.getExperimentData().getExperiment(); // 
	}

	/***
	 * get the thread level data manager (used by plot graphs)
	 * @return
	 */
	public ThreadLevelDataManager getThreadLevelDataManager() {
		return dataManager;
	}
	
	/**
	 *  get the ExperimentView class used for this database
	 * @param path
	 */
	public ExperimentView getExperimentView () {
		return view;
	}

	/**
	 *  set the viewer window index to record the window in which this database is displayed.
	 * @param index
	 */
	public void setWindowIndex (int index) {
		winIndex = index;
		return; 
	}


	/**
	 *  set the Experiment class used for this database
	 * @param path
	 * @throws IOException 
	 */
	public void setExperiment (Experiment exper) throws IOException {
		experiment = exper;
		dataManager = new ThreadLevelDataManager(exper);
		
		// TODO hack: since we just created the manager, we need to inform
		// MetricRaw to set the new manager
		MetricRaw []metrics = experiment.getMetricRaw();
		if (metrics != null)
			for (MetricRaw metric: metrics)
			{
				metric.setThreadData(dataManager.getThreadDataCollection());
			}
	}

	/**
	 *  set the ExperimentView class used for this database
	 * @param path
	 */
	public void setExperimentView (ExperimentView experView) {
		view = experView;
		return;
	}
	
	public void dispose() {
		dataManager.dispose();
		experiment.dispose();
	}
}
