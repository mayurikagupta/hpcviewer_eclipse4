package edu.rice.cs.hpc.test.viewer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.viewer.metric.ThreadDataCollection2;

public class ThreadDataCollection2Test {

	private ThreadDataCollection2 data;
	private Experiment experiment ;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		experiment = new Experiment();
		final String filename = System.getProperty("HPCDATA_FILE");
		assertNotNull(filename);
		final File file = new File(filename);
		assertTrue(file.canRead());
		experiment.open(file, null, false);
		data = new ThreadDataCollection2(experiment);
		data.open(experiment.getDefaultDirectory().getAbsolutePath());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetMetrics() {
		int numMetrics = experiment.getMetricRaw().length;
		try {
			double []values = data.getMetrics(2, 0, numMetrics);
			assertNotNull(values);
			
		} catch (IOException e) {
			e.printStackTrace();
			fail("error occurs");
		}
	}

	@Test
	public void testGetScopeMetrics() {
		int numMetrics = experiment.getMetricRaw().length;
		try {
			double []values = data.getScopeMetrics(0, 0, numMetrics);
			assertNotNull(values);
			
		} catch (IOException e) {
			e.printStackTrace();
			fail("error occurs");
		}
	}

	@Test
	public void testIsAvailable() {
		boolean is = data.isAvailable();
		assertTrue(is);
	}

	@Test
	public void testGetRankLabels() throws IOException {
		double []labels = data.getRankLabels();
		assertNotNull(labels);
		
	}

	@Test
	public void testGetParallelismLevel() throws IOException {
		int level = data.getParallelismLevel();
		assertTrue(level>0);
	}

	@Test
	public void testGetRankTitle() throws IOException {
		String title = data.getRankTitle();
		assertNotNull(title);
	}

}
