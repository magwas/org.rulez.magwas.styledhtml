package org.rulez.magwas.enterprise.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

public class PersistedProperty {

	private String id=null;
	private String name=null;
	private String documentation=null, linetext = null;
	private String type=null;
	private Integer subtype=null, alignment=null, textposition=null, linewidth=null;
	private String source=null,target=null, element=null; //these are references to other objects
	private String font=null, fontcolor=null,fillcolor=null; //graphical properties
	private Integer x1=null, x2=null, y1=null, y2=null;
	private String key=null, value=null;
	private Float weight;

	private Connection con;
	private Integer versionid;
	private PersistedObject parent;

	private List<String> ovrvars;
	
	private List<String> dependents = new ArrayList<String>();//List of ids of objects depending on this
	private Boolean persisted=false;
	static HashMap<String, PersistedObject> objdir = null;
	private List<PersistedObject> properties = new ArrayList<PersistedObject>();
	private EObject node;
	
	public PersistedProperty(Connection connection, Integer version, EObject node, PersistedObject parent) throws SQLException {
		// This constructor is to persist the node to SQL
		super();
		this.node=node;
		if (null == objdir) {
			objdir = new HashMap<String, PersistedObject>();
		}
		ovrvars = new ArrayList<String>();
		con=connection;
		versionid=version;
		assert(null != parent);
		this.parent=parent;
		parseNode(node);
	}

	private void parseNode(EObject node2) {
		// TODO Auto-generated method stub
		
	}

	private PersistedProperty(ResultSet rs, PersistedObject parent) throws SQLException {
		// This constructor is to load a property from persistence
		// This is called with the given property as the actual record in the ResultSet
		super();
		if (null == objdir) {
			objdir = new HashMap<String, PersistedObject>();
		}
		ovrvars = new ArrayList<String>();
		this.parent=parent;
		loadproperty(rs);
	}

	private void loadproperty(ResultSet rs) {
		// TODO Auto-generated method stub
	}
	
	public static void loadproperties(String parentid, PersistedObject parent) {
		
	}
	
	private void loadPropertiesSQL(String nodeid) throws SQLException {
		PreparedStatement psSelectproperty = con.prepareStatement(
				"select " +
				"(type,key,value, x1, y1, x2, y2, weight )" +
				"from property_view where parent = ? and version = ?");		
		psSelectproperty.setString(1, nodeid);
		psSelectproperty.setInt(2, versionid);
		ResultSet qr = psSelectproperty.executeQuery();
		while(qr.next()) {
			new PersistedProperty(qr,parent);
		}
		
	}

}
