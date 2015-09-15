package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.Experiment;

/********************************************************
 * 
 * Class to handle metric raw (metric-db from hpcprof) 
 * including adding it with derived metrics
 *
 ********************************************************/
public class MetricRawManager implements IMetricManager 
{
	private BaseMetric[] metrics;
	
	public MetricRawManager(BaseMetric []metrics) {
		this.metrics = metrics;
	}
	
	public MetricRawManager(Experiment experiment) {
		this(experiment.getMetricRaw());
	}

	@Override
	public BaseMetric getMetric(String ID) {
		for (BaseMetric m : metrics ) {
			if (m.getShortName().equals(ID))
				return m;
		}
		return null;
	}

	@Override
	public BaseMetric getMetric(int index) {
		if (index >= 0 && index < metrics.length)
			return metrics[index];
		return null;
	}

	@Override
	public int getMetricCount() {
		return metrics.length;
	}

	@Override
	public BaseMetric[] getMetrics() {
		return metrics;
	}

	@Override
	public void addDerivedMetric(DerivedMetric objMetric) {
		BaseMetric []extMetrics = new BaseMetric[metrics.length+1];
		System.arraycopy(metrics, 0, extMetrics, 0, metrics.length);
		extMetrics[metrics.length] = objMetric;
		metrics = extMetrics;
	}
}
