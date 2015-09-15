package edu.rice.cs.hpc.test.viewer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.viewer.metric.ThreadLevelDataManager;

public class ThreadLevelDataManagerTest {
	private ThreadLevelDataManager manager;
	private Experiment experiment;
	
	@Before
	public void setUp() throws Exception {
		experiment = new Experiment();
		String database = System.getProperty("HPCDATA_DB");
		assertNotNull(database);
		
		final File file = new File(database);
		assertTrue(file.canRead());
		experiment.open(file, null, false);
		manager = new ThreadLevelDataManager(experiment);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsDataAvailable() {
		boolean is = manager.isDataAvailable();
		assertTrue(is);
	}

	@Test
	public void testGetSeriesName() {
		String []name = manager.getSeriesName();
		assertNotNull(name);
		
		for(String n : name) {
			assertNotNull(n);
		}
	}
	

	@Test
	public void testGetProcessIDsDouble() throws NumberFormatException, IOException {
		BaseMetric []metrics = experiment.getMetricRaw();
		assertNotNull(metrics);

		for (BaseMetric m : metrics) {
			double []ids = manager.getProcessIDsDouble(((MetricRaw)m).getID());
			assertNotNull(ids);
		}
	}

	@Test
	public void testGetMetrics() throws IOException {
		BaseMetric []metrics = experiment.getMetricRaw();
		assertNotNull(metrics);
		
		for(BaseMetric metric : metrics) {
			double []values = manager.getMetrics(((MetricRaw)metric), 0);
			assertNotNull(values);
		}
	}

	@Test
	public void testGetParallelismLevel() throws IOException {
		int i = manager.getParallelismLevel();
		assertTrue(i>0);
	}

	@Test
	public void testGetRankTitle() throws IOException {
		String title = manager.getRankTitle();
		assertNotNull(title);
	}

	@Test
	public void testGetThreadDataCollection() {
		IThreadDataCollection data = manager.getThreadDataCollection();
		assertNotNull(data);
	}

}
