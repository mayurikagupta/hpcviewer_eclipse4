package edu.rice.cs.hpc.viewer.graph;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;

import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/*********************************************************************
 * 
 * Class to handle a plotting graph
 *
 *********************************************************************/
public class GraphEditorPlot extends GraphEditor {

    public static final String ID = "edu.rice.cs.hpc.viewer.graph.GraphEditorPlot";
    
	@Override
	protected double[] getValuesX(Scope scope, MetricRaw metric) 
	throws NumberFormatException, IOException {

		double []x_values = threadData.getRankLabels();	
		int parLevel	  = threadData.getParallelismLevel();
		
		return getEvenlySpreadValues(parLevel, x_values);
	}

	@Override
	protected double[] getValuesY(Scope scope, MetricRaw metric) throws IOException {
		{
			double []y_values = threadData.getMetrics(scope.getCCTIndex(), metric.getRawID(), metric.getSize());
			return y_values;
		}
	}


	@Override
	protected String getXAxisTitle() {
		IAxisSet axisSet = this.getChart().getAxisSet();
		IAxisTick xTick = axisSet.getXAxis(0).getTick();

		xTick.setFormat(new DecimalFormat("##########"));

		try {
			if (threadData.getParallelismLevel()>1) 
			{
				xTick.setFormat(new DecimalFormat("######00.00##"));
				return threadData.getRankTitle();
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return "Rank";
	}

	/****
	 * Recompute the values of x into an evenly spread values if the application is a 
	 * hybrid parallel code.<br/><p>
	 * For instance, if the database has the following threads: 0.0, 0.1, 0.2, 2.0, and 2.1
	 * this method will return an "evenly spread" values into:  0.0, 0.3, 0.6, 2.0, and 2.5
	 * </p>
	 * This method is only used for plotting the graph. 
	 * 
	 * @param parallelismLevel : level of parallelsm
	 * @param values : the original rank labels
	 * @return the new values of rank labels
	 */
	private double[] getEvenlySpreadValues(int parallelismLevel, double[]values) {
		// we only spread the values if the parallelism is more than 1 
		
		if (parallelismLevel>1) {
			
			for(int i=0; i<values.length; i++) {
				int num_siblings = 0;
				int rank_first 	 = (int) Math.floor(values[i]);
				
				int j = i+1;
				for(; j<values.length; j++) {
					int next_rank =  (int) Math.floor(values[j]);
					num_siblings++;
										
					if (next_rank > rank_first) {
						break;
					} else if (j==values.length-1) {
						num_siblings++;
					}
				}
				for (int k=0; k<num_siblings; k++) {
					values[i+k] = (double)rank_first + ((double)k/num_siblings);
				}
				i = j-1;
			}
		}
		return values;
	}

	@Override
	protected ArrayList<Integer> translateUserSelection(
			ArrayList<Integer> selections) {
		return selections;
	}
}
