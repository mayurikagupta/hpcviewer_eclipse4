package edu.rice.cs.hpc.viewer.metric;

import java.io.IOException;

/********************************************************************************
 * 
 * Interface to collect data needed for plot graph
 *
 ********************************************************************************/
public interface IThreadDataCollection 
{
	/****
	 * Open a directory which may contain data for plot graph.
	 * If the directory has no plot data, it throws an exception.
	 *  
	 * @param directory : the database directory
	 * 
	 * @throws IOException
	 */
	public void 	open(String directory) throws IOException;
	
	/****
	 * Check if the opened directory has a plot data or not
	 * @return true if plot data exist, false otherwise
	 */
	public boolean 	isAvailable();
	
	/*****
	 * Get a list of the labels for ranks (x-axis)
	 * @return
	 * @throws IOException 
	 */
	public double[]	getRankLabels() throws IOException;	
	
	/****
	 * Get the level of parallelism. If it's a thread only or process only
	 * application, it returns 1, if it's a hybrid it returns 2. 
	 * (more than 2 is not supported at the moment).
	 * 
	 * @return
	 * @throws IOException 
	 */
	public int 		getParallelismLevel() throws IOException;
	
	/****
	 * Get the title of the rank (process, threads, ...)
	 * @return
	 * @throws IOException 
	 */
	public String   getRankTitle() throws IOException;
	
	/*****
	 * Get an array of metrics of a specified node and metrics
	 * 
	 * @param nodeIndex
	 * @param metricIndex
	 * @param numMetrics
	 * @return
	 * @throws IOException
	 */
	public double[] getMetrics(long nodeIndex, int metricIndex, int numMetrics) 
			throws IOException;

	/****
	 * get the array of metric values for all CCT of a specified metric and
	 * thread ID
	 * 
	 * @param thread_id : thread ID
	 * @param MetricIndex : metric index
	 * @param numMetrics : num metrics
	 * @return array of metric values for each CCT 
	 */
	public double[] getScopeMetrics(int thread_id, int MetricIndex, int numMetrics)
			 throws IOException;
	
	/****
	 * Method to be called at the end of the execution, needed to dispose
	 * the allocated resources.
	 */
	public void		dispose();
}
