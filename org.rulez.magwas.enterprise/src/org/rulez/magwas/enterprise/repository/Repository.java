package org.rulez.magwas.enterprise.repository;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IProperty;

public class Repository {


	PersistenceManager pm;
	private List<String> basevers;
	private int versionid;
	
	
	public Repository(String reponame) throws SQLException, ClassNotFoundException, ConfigurationException {
		pm = new SQLManager(reponame);
	}


	protected String getModelProperty(IArchimateModel model, String propname) {
		EList<IProperty> props = model.getProperties();
		System.out.println("model props:"+props);
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
	

	
	public void checkin(IArchimateModel model) throws SQLException {
		String version = getModelVersion(model); //FIXME sensible error message/dialog
		String name = model.getName();
		String aclname = getModelProperty(model,"modelAcl");//FIXME sensible error message/dialog
		basevers = getBaseVersions(model);
		versionid = pm.addVersion(version, basevers, name,aclname);
		PersistedObject po = new PersistedObject(pm, versionid, model,null);
		po.persist();
	}

}
