package org.rulez.magwas.enterprise.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.eclipse.emf.ecore.EObject;

import uk.ac.bolton.archimate.model.IIdentifier;

public class PersistedObject {

	private String id=null;
	private String type=null;

	private Integer versionid;
	private PersistedObject parent;

	private Boolean persisted=false;
	static HashMap<String, PersistedObject> objdir = null;
	private List<String> dependents = new ArrayList<String>();//List of ids of objects depending on this
	private List<PersistedObject> properties = new ArrayList<PersistedObject>();
	private EObject node;
	private PersistenceManager pm;
	
	public PersistedObject(PersistenceManager pm, Integer version, EObject node, PersistedObject parent) {
		// This constructor is to persist the node to SQL
		super();
		this.pm=pm;
		this.node=node;
		if (null == objdir) {
			objdir = new HashMap<String, PersistedObject>();
		}
		versionid=version;
		if (null == parent) {
			parent = this;
		}
		this.parent=parent;
		parseNode(node);
	}
	
	public PersistedObject(PersistenceManager pm, Integer version, String nodeid, PersistedObject parent) {
		// This constructor is to load an object from persistence
		super();
		if (null == objdir) {
			objdir = new HashMap<String, PersistedObject>();
		}
		this.pm=pm;
		versionid=version;
		if (null == parent) {
			parent = this;
		}
		this.parent=parent;
		load(nodeid);
	}


	private void loadIfNeeded(String id){
		if (null != id) {
			if (!objdir.containsKey(id)) {
				new PersistedObject(pm, versionid,id,this);
			}
		}
	}
	
	private void load(String nodeid){
		//FIXME String parentid = loadObjectSQL(nodeid);
		objdir.put(id, this);
		//FIXME loadIfNeeded(parentid);
		PersistedProperty.loadproperties(nodeid, this);
	}
	

	public PersistedObject addNode(EObject node, PersistedObject parent){
		if (node instanceof IIdentifier) {
			if (objdir.containsKey(((IIdentifier) node).getId())) {
				return objdir.get(((IIdentifier) node).getId());
			}			
		}
		return new PersistedObject(pm, versionid, node,parent);
	}
	
	
	public void persist(){
		if (persisted) {
			System.out.println("already persisted "+this);
			return;
		}
		if(this != parent) {
			parent.persist();
		}
		System.out.println("persisting "+this);
		//FIXME persistSQL();
		persisted = true;
		for(PersistedObject prop : properties) {
			prop.persist();
		}
		for(String dependent : dependents) {
			PersistedObject dobj = objdir.get(dependent);
			dobj.persist();
		}
	}
	

	public void ready() {
		//System.out.println("readying"+this);
		if (null == id){
			assert(parent != this);
			parent.addToProperties(this);
		} else {
			parent.setDependent(id);
			objdir.put(id, this);
		}
	}

	public void addToProperties(PersistedObject o) {
		properties.add(o);
	}
	public void setDependent(String id) {
		dependents.add(id);
	}
	
	public void parseNode(EObject node){
	}


	
}
