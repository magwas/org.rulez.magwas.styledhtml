package org.rulez.magwas.styledhtml.steps;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.rulez.magwas.styledhtml.EventLog;

import uk.ac.bolton.archimate.model.IArchimateModel;

public class StepFactory extends Object {
	protected static HashMap<String,Step> members = new HashMap<String,Step>();
	EventLog log;
	IArchimateModel model;
	File targetdir;
	File styledir;
	List<File> dontkeep = new ArrayList<File>();
	
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
	public Step get(String s) {
		return members.get(s);
	}
	public void cleanUp(){
		for(File f : dontkeep) {
			log.issueInfo("deleting", f.getAbsolutePath());
			f.delete();
		}
	}
}
