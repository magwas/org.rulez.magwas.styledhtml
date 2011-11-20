package org.rulez.magwas.enterprise.repository;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IArchimateModelElement;
import uk.ac.bolton.archimate.model.IDiagramModelComponent;
import uk.ac.bolton.archimate.model.IIdentifier;
import uk.ac.bolton.archimate.model.IProperty;

public class Repository {


	PersistenceManager pm;
	private List<String> basevers;
	private String conname;
	private HashMap<String,EObject> persistedmap = new HashMap<String,EObject>();
	private IArchimateModel model;
	
	
	public Repository(String reponame){
		conname = reponame;
		try {
			pm = new SQLManager(this);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			throw new Error("initializing repo"+ reponame);
		}
	}


	protected String getModelProperty(IArchimateModel model, String propname) {
		EList<IProperty> props = model.getProperties();
		//System.out.println("model props:"+props);
		String value = null;
		for(IProperty prop : props) {
			if(prop.getKey().equals(propname)) {
				value = prop.getValue();
			}
		}
		return value;
	}
	
	protected String getModelVersion(IArchimateModel model) {
		return getModelProperty(model, "modelVersion");
	}

	protected List<String> getBaseVersions(IArchimateModel model) {
		EList<IProperty> props = model.getProperties();
		List<String> vers = new ArrayList<String>();
		for(IProperty prop : props) {
			if(prop.getKey().equals("baseVersion")) {
				vers.add(prop.getValue());
			}
		}
		return vers;
	}
	
	public void checkModel(EObject node) {
		assert(node instanceof IIdentifier);
		IArchimateModel thismodel;
		if(node instanceof IArchimateModelElement) {
			thismodel = ((IArchimateModelElement) node).getArchimateModel();
		} else if (node instanceof IDiagramModelComponent){
			thismodel = ((IDiagramModelComponent) node).getDiagramModel().getArchimateModel();
		} else {
			throw new Error("unknown object:"+node);
		}
		if(null == model) {
			model = thismodel;
		} else if (thismodel != model){
			throw new Error("adding an object from a different model");
		}
	}

	
	public boolean isPersisted(String id){
		//System.out.println(id+ persistedmap.containsKey(id)+" in " +persistedmap.keySet());
		return persistedmap.containsKey(id);
	}
	
	public void registerPersisted(IIdentifier node) {
		//System.out.println("registerpersisted "+node.getId());
		persistedmap.put(node.getId(), node);
	}
	
	public EObject getPersisted(String id) {
		return persistedmap.get(id);
	}
	
	public void checkin(IArchimateModel model) {
		String version = getModelVersion(model); //FIXME sensible error message/dialog
		String name = model.getName();
		String aclname = getModelProperty(model,"modelAcl");//FIXME sensible error message/dialog
		basevers = getBaseVersions(model);
		pm.addVersion(version, basevers, name,aclname);
		pm.saveObject(model);
	}
	public EObject checkout(String versionname) {
		return pm.load(versionname);
	}

	public String getConname() {
		return conname;
	}

}
