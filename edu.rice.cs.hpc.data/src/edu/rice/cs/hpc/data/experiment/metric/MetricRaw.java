package edu.rice.cs.hpc.data.experiment.metric;

import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/****************************************
 * Raw metric class
 * @author laksonoadhianto
 *
 ****************************************/
public class MetricRaw  extends BaseMetric {

	private int ID;
	private String db_glob;
	private int db_id;
	private int num_metrics;
	
	private IThreadDataCollection threadData;
	private List<Integer> threads;
	
	public MetricRaw(String sID, String sDisplayName, boolean displayed, String format, AnnotationType annotationType, int index) {
		super(sID, sDisplayName, displayed, format, annotationType, index, index,  MetricType.EXCLUSIVE);
	}
	
	
	public MetricRaw(int id, String title, String db_pattern, 
			int db_num, int metrics) {
		// raw metric has no partner
		super( String.valueOf(id), title, true, null, AnnotationType.NONE, db_num, db_num, MetricType.EXCLUSIVE);
		this.ID = id;
		this.db_glob = db_pattern;
		this.db_id = db_num;
		this.num_metrics = metrics;
	}
	
	public void setThreadData(IThreadDataCollection threadData)
	{
		this.threadData = threadData;
	}
	
	public void setThread(List<Integer> threads)
	{
		this.threads = threads;
	}
	
	/***
	 * return the glob pattern of files of this raw metric
	 * @return
	 */
	public String getGlob() {
		return this.db_glob;
	}
	
	
	/***
	 * retrieve the "local" ID of the raw metric
	 * This ID is unique among raw metrics in the same experiment 
	 * @return
	 */
	public int getRawID() {
		return this.db_id;
	}
	
	
	/***
	 * retrieve the number of raw metrics in this experiment
	 * @return
	 */
	public int getSize() {
		return this.num_metrics;
	}
	
	
	/***
	 * return the ID of the raw metric
	 * The ID is unique for all raw metric across experiments 
	 * @return
	 */
	public int getID() {
		return this.ID;
	}


	@Override
	public MetricValue getValue(Scope s) {
		if (threadData != null)
		{
			try {
				double []values = threadData.getMetrics(s.getCCTIndex(), ID, num_metrics);
				double val_mean = 0.0;
				if (threads != null)
				{
					double divider  = 1 / threads.size();
					for(Integer thread : threads)
					{
						val_mean += (values[thread] * divider);
					}
				} else {
					val_mean = values[0];
				}
				return new MetricValue(val_mean);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return MetricValue.NONE;
	}


	//@Override
	public BaseMetric duplicate() {
		return new MetricRaw(ID, this.displayName, this.db_glob, this.db_id, this.num_metrics);
	}
	
}
