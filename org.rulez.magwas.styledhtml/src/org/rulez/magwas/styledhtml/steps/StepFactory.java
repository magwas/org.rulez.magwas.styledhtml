package org.rulez.magwas.styledhtml.steps;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.rulez.magwas.styledhtml.EventLog;

import uk.ac.bolton.archimate.model.IArchimateModel;

/**
 * A factory for creating and executing Step objects.
 */
public class StepFactory extends Object {
	protected static HashMap<String,Step> members = new HashMap<String,Step>();
	EventLog log;
	IArchimateModel model;
	File targetdir;
	File styledir;
	List<File> dontkeep = new ArrayList<File>();
	
	/**
	 * Instantiates a new step factory.
	 *
	 * @param l is used for logging
	 * @param m is the model we are working with
	 * @param s the style directory
	 * @param t the target directory
	 */
	public StepFactory(EventLog l,IArchimateModel m, File s,File t){
		log = l;
		model = m;
		targetdir = t;
		styledir = s;
		members.put("style", new Style(this));
		members.put("diagrams", new Diagrams(this));
		members.put("copy", new Copy(this));
		members.put("export", new Export(this));
		members.put("transform", new Transform(this));
		members.put("load", new Load(this));
	}
	
	/**
	 * Gets the class corresponding to the local-name of an xml tag.
	 *
	 * @param s the local-name
	 * @return the class executing that step
	 */
	public Step get(String s) {
		return members.get(s);
	}
	
	/**
	 * Clean up after doing all steps: delete files marked with keep="false"
	 */
	public void cleanUp(){
		for(File f : dontkeep) {
			log.issueInfo("deleting", f.getAbsolutePath());
			f.delete();
		}
	}
}
