package edu.rice.cs.hpc.data.experiment.metric;

import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/****************************************
 * Raw metric class\n
 * a.k.a thread-level metric
 ****************************************/
public class MetricRaw  extends BaseMetric {

	private int ID;			 // the index of this metric as specified in XML
	private String db_glob;  // old format: the glob pattern of the metric-db file
	private int db_id;		 // sequential index of the metric in the XML. Is has to be between 0 to the number of metrics
	private int num_metrics; // number of metrics
	
	private IThreadDataCollection threadData;
	
	/*** list of threads that its metric values have to be computed **/
	private List<Integer> threads = null;
	
	/*** list of scope metric values of a certain threads. The length of the array is the number of cct nodes*/
	private double []thread_values = null;
	private MetricValue	rootValue  = null;
	
	public MetricRaw(String sID, String sDisplayName, boolean displayed, String format, AnnotationType annotationType, int index) {
		super(sID, sDisplayName, displayed, format, annotationType, index, index,  MetricType.EXCLUSIVE);
	}
	
	
	public MetricRaw(int id, String title, String db_pattern, 
			int db_num, int metrics) {
		// raw metric has no partner
		super( String.valueOf(id), title, true, null, AnnotationType.NONE, db_num, db_num, MetricType.EXCLUSIVE);
		this.ID 	 = id;
		this.db_glob = db_pattern;
		this.db_id 	 = db_num;
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
	
	public List<Integer> getThread()
	{
		return threads;
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
		MetricValue value = MetricValue.NONE;
		if (threadData != null)
		{
			try {
				if (threads != null)
				{
					if (threads.size()>1)
					{
						value = getAverageValue(s);
					} else if (threads.size()==1)
					{
						value = getSpecificValue(s, threads.get(0));
					}
					// to compute the percentage, we need to have the value of the root
					// If the root has no value, we have to recompute it only for one time
					// Once we have the root's value, we don't have to recompute it
					if (rootValue == null) {
						if (s instanceof RootScope)
							rootValue = value;
						else {
							rootValue = getValue(s.getRootScope());
						}
						if (rootValue != MetricValue.NONE) {
							// root value is available: we can show the percent
							setAnnotationType(AnnotationType.PERCENT);
						}
					}
					if (rootValue != MetricValue.NONE) {
						// if the value exist, we compute the percentage
						MetricValue.setAnnotationValue(value, value.getValue() / rootValue.getValue());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return value;
	}


	@Override
	public BaseMetric duplicate() {
		MetricRaw dup = new MetricRaw(ID, this.displayName, this.db_glob, this.db_id, this.num_metrics);
		// TODO: hack to duplicate also the thread data
		dup.threadData = threadData;
		return dup;
	}
	
	/*****
	 * compute the average value of a scope for certain threads.
	 * The number of threads cannot be null.
	 * @param s
	 * @return
	 * @throws IOException
	 */
	private MetricValue getAverageValue(Scope s) throws IOException
	{
		double val_mean = 0.0;
		final double divider  = 1.0d / threads.size();
		double []values = threadData.getMetrics(s.getCCTIndex(), getIndex(), num_metrics);
		for(Integer thread : threads)
		{
			val_mean += (values[thread] * divider);
		}
		MetricValue value = setValue(val_mean); 
		return value;
	}
	
	private MetricValue getSpecificValue(Scope s, int thread_id) throws IOException
	{
		checkValues(thread_id);
		MetricValue mv = MetricValue.NONE;
		if (thread_values != null) {
			mv = setValue(thread_values[s.getCCTIndex()-1]);
		} else {
			// there is no API implementation for reading the whold CCT metrics
			// TODO: using the old get metric for the new database
			double []values = threadData.getMetrics(s.getCCTIndex(), getIndex(), num_metrics);
			double value    = values[thread_id];
			mv  = setValue(value);
		}
		return mv;
	}
	
	
	private MetricValue setValue(double value)
	{
		MetricValue mv = MetricValue.NONE;
		if (value != 0.0)
			mv = new MetricValue(value);
		return mv;
	}
	
	private void checkValues(int thread_id) throws IOException
	{
		if (thread_values == null)
			thread_values = threadData.getScopeMetrics(thread_id, ID, num_metrics);
	}
}
