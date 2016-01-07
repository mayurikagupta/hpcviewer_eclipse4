package edu.rice.cs.hpc.data.experiment;

import java.io.File;

import edu.rice.cs.hpc.data.experiment.xml.ExperimentFileXML;
import edu.rice.cs.hpc.data.util.IUserData;


/***********************************************
 * 
 * Database representation for local data
 *
 ***********************************************/
public class LocalDatabaseRepresentation implements IDatabaseRepresentation 
{
	private File fileExperiment;
	final private IUserData<String, String> userData; 
	final private boolean need_metric;

	public LocalDatabaseRepresentation(File fileExperiment, 
			IUserData<String, String> userData, 
			boolean need_metric)
	{
		this.fileExperiment = fileExperiment;
		this.userData		= userData;
		this.need_metric	= need_metric;
	}
	

	@Override
	public void open(BaseExperiment experiment) throws Exception
	{		
		ExperimentFileXML fileXML = new ExperimentFileXML();
		fileXML.parse(fileExperiment, experiment, need_metric, userData);	
	}

	@Override
	public IDatabaseRepresentation duplicate() {
		// we need to copy the path just in case it will be modified by the caller
		final String path   = this.fileExperiment.getAbsolutePath();
		File fileExperiment = new File(path);
		
		// create a new representation
		LocalDatabaseRepresentation dup = new LocalDatabaseRepresentation(fileExperiment, userData, need_metric);
		
		return dup;
	}

	@Override
	public File getFile() {
		return fileExperiment;
	}

	@Override
	public void setFile(File file) {
		fileExperiment = file;
	}
}
